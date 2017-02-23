package org.camunda.bpm.extension.batch.core;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

public abstract class CustomBatchCreateJobsHandler<T> implements BatchJobHandler<CustomBatchConfiguration> {

    public final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(getType());

    @Override
    public boolean createJobs(BatchEntity batch) {
        final JobManager jobManager = Context.getCommandContext().getJobManager();

        final CustomBatchConfiguration configuration = readConfiguration(batch.getConfigurationBytes());

        List<T> data = configuration.getData();
        // view of process instances to process
        int batchJobsPerSeed = batch.getBatchJobsPerSeed();
        int invocationsPerBatchJob = batch.getInvocationsPerBatchJob();
        int numberOfItemsToProcess = Math.min(invocationsPerBatchJob * batchJobsPerSeed, data.size());
        // view of process instances to process
        List<T> dataSubSet = data.subList(0, numberOfItemsToProcess);

        int createdJobs = 0;
        while (!dataSubSet.isEmpty()) {
            int lastIdIndex = Math.min(batch.getInvocationsPerBatchJob(), dataSubSet.size());
            List<T> dataForJob = dataSubSet.subList(0, lastIdIndex);

            final JobEntity job = createBatchJob(batch, dataForJob);
            jobManager.insertAndHintJobExecutor(job);

            dataForJob.clear();
            createdJobs++;
        }

        // update created jobs for batch
        batch.setJobsCreated(batch.getJobsCreated() + createdJobs);

        // update batch configuration
        batch.setConfigurationBytes(writeConfiguration(configuration));

        return data.isEmpty();
    }

    @Override
    public JobDeclaration<?, MessageEntity> getJobDeclaration() {
        return JOB_DECLARATION;
    }

    @Override
    public void deleteJobs(BatchEntity batch) {
        Context.getCommandContext().getJobManager()
            .findJobsByJobDefinitionId(batch.getBatchJobDefinitionId())
            .stream().peek(JobEntity::delete);
    }

    private JobEntity createBatchJob(BatchEntity batch, List<T> dataForJob) {
        final CustomBatchConfiguration<T> jobConfiguration = new CustomBatchConfiguration(dataForJob);
        final ByteArrayEntity configurationEntity = CustomBatchConfigurationHelper.saveConfiguration(jobConfiguration);

        final BatchJobContext creationContext = new BatchJobContext(batch, configurationEntity);
        return JOB_DECLARATION.createJobInstance(creationContext);
    }

    @Override
    public BatchJobConfiguration newConfiguration(String canonicalString) {
        return new BatchJobConfiguration(canonicalString);
    }

    @Override
    public byte[] writeConfiguration(CustomBatchConfiguration configuration) {
        return CustomBatchConfigurationHelper.writeConfiguration(configuration);
    }

    @Override
    public CustomBatchConfiguration readConfiguration(byte[] serializedConfiguration) {
        return CustomBatchConfigurationHelper.readConfiguration(serializedConfiguration);
    }
}
