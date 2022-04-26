package org.camunda.community.batch.core;

import org.camunda.bpm.engine.impl.batch.*;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;

import java.io.Serializable;
import java.util.List;

public abstract class CustomBatchCreateJobsHandler<T extends Serializable> implements BatchJobHandler<CustomBatchConfiguration> {

  private final BatchJobDeclaration JOB_DECLARATION = new BatchJobDeclaration(getType());

  private final CustomBatchConfigurationHelper configurationHelper = createConfigurationHelper();

  @Override
  public boolean createJobs(final BatchEntity batch) {
    final JobManager jobManager = Context.getCommandContext().getJobManager();

    final CustomBatchConfiguration<T> configuration = readConfiguration(batch.getConfigurationBytes());

    final List<T> data = configuration.getData();
    // view of process instances to process
    final int batchJobsPerSeed = batch.getBatchJobsPerSeed();
    final int invocationsPerBatchJob = batch.getInvocationsPerBatchJob();
    final int numberOfItemsToProcess = Math.min(invocationsPerBatchJob * batchJobsPerSeed, data.size());
    // view of process instances to process
    final List<T> dataSubSet = data.subList(0, numberOfItemsToProcess);

    int createdJobs = 0;
    while (!dataSubSet.isEmpty()) {
      final int lastIdIndex = Math.min(batch.getInvocationsPerBatchJob(), dataSubSet.size());
      final List<T> dataForJob = dataSubSet.subList(0, lastIdIndex);

      final JobEntity job = createBatchJob(batch, dataForJob);
      job.setExclusive(configuration.isExclusive());
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
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  public void deleteJobs(final BatchEntity batch) {
    Context.getCommandContext().getJobManager()
      .findJobsByJobDefinitionId(batch.getBatchJobDefinitionId())
      .forEach(JobEntity::delete);
  }

  private JobEntity createBatchJob(final BatchEntity batch, final List<T> dataForJob) {
    final CustomBatchConfiguration<T> jobConfiguration = new CustomBatchConfiguration<>(dataForJob);
    final ByteArrayEntity configurationEntity = configurationHelper().saveConfiguration(jobConfiguration);

    final BatchJobContext creationContext = new BatchJobContext(batch, configurationEntity);
    return getJobDeclaration().createJobInstance(creationContext);
  }

  @Override
  public BatchJobConfiguration newConfiguration(final String canonicalString) {
    return new BatchJobConfiguration(canonicalString);
  }

  @Override
  public byte[] writeConfiguration(final CustomBatchConfiguration configuration) {
    final CustomBatchConfigurationHelper<T> configurationHelper = configurationHelper();
    return configurationHelper.writeConfiguration(configuration);
  }

  @Override
  public CustomBatchConfiguration<T> readConfiguration(final byte[] serializedConfiguration) {
    return configurationHelper().readConfiguration(serializedConfiguration);
  }

  public CustomBatchConfigurationHelper<T> configurationHelper() {
    return configurationHelper;
  }

  public CustomBatchConfigurationHelper<T> createConfigurationHelper() {
    return CustomBatchConfigurationDownwardCompatibleWrapper.of(CustomBatchConfigurationJsonHelper.of());
  }
}
