package org.camunda.bpm.extension.batch;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.extension.batch.core.CustomBatchConfiguration;

public class CustomBatchBuilder {

    private final ProcessEngineConfigurationImpl engineConfiguration;

    private final BatchEntity batch = new BatchEntity();

    private BatchJobHandler batchJobHandler;

    private List<?> batchData;

    public CustomBatchBuilder() {
        this(Context.getProcessEngineConfiguration());
    }

    public CustomBatchBuilder(ProcessEngineConfiguration configuration) {
        this.engineConfiguration = (ProcessEngineConfigurationImpl)configuration;
        this.batch.setBatchJobsPerSeed(((ProcessEngineConfigurationImpl)configuration).getBatchJobsPerSeed());
        this.batch.setInvocationsPerBatchJob(((ProcessEngineConfigurationImpl)configuration).getInvocationsPerBatchJob());
    }

    public static CustomBatchBuilder of() {
        return new CustomBatchBuilder();
    }

    public static CustomBatchBuilder of(ProcessEngineConfiguration configuration) {
        return new CustomBatchBuilder(configuration);
    }

    /**
     * The Batch needs an batch job handler which is registered in engine.
     *
     * @param jobHandler
     * @return CustomBatchBuilder
     */
    public CustomBatchBuilder jobHandler(BatchJobHandler jobHandler) {
        this.batchJobHandler = jobHandler;
        this.batch.setType(jobHandler.getType());
        return this;
    }

    /**
     * The number of batch execution jobs created per seed job invocation.
     * The batch seed job is invoked until it has created all batch execution jobs required by the batch
     * (see totalJobs ... is calculated from size of input list and invocationsPerBatchJob).
     *
     * @param jobsPerSeed
     * @return CustomBatchBuilder
     */
    public CustomBatchBuilder jobsPerSeed(int jobsPerSeed) {
        this.batch.setBatchJobsPerSeed(jobsPerSeed);
        return this;
    }

    /**
     * Every batch execution job invokes the command executed by the batch invocationsPerBatchJob times.
     *
     * E.g., for a process instance migration batch this specifies the number of process instances which
     * are migrated per batch execution job.
     *
     * @param invocationsPerBatchJob
     * @return CustomBatchBuilder
     */
    public CustomBatchBuilder invocationsPerBatchJob(int invocationsPerBatchJob) {
        this.batch.setInvocationsPerBatchJob(invocationsPerBatchJob);
        return this;
    }

    /**
     * List of data which should be processed by the batch jobs.
     *
     * @param data
     * @return
     */
    public CustomBatchBuilder batchData(List<?> data) {
        this.batchData = data;
        return this;
    }

    public Batch create(CommandExecutor executor) {
        return executor.execute((commandContext) -> {
            final CustomBatchConfiguration configuration = new CustomBatchConfiguration(this.batchData);
            this.batch.setConfigurationBytes(this.batchJobHandler.writeConfiguration(configuration));
            this.batch.setTotalJobs(calculateTotalJobs());

            commandContext.getBatchManager().insert(this.batch);

            this.batch.createSeedJobDefinition();
            this.batch.createBatchJobDefinition();
            this.batch.createMonitorJobDefinition();
            this.batch.fireHistoricStartEvent();
            this.batch.createSeedJob();

            return this.batch;
        });
    }

    public Batch create() {
        return create(engineConfiguration.getCommandExecutorTxRequired());
    }

    private int calculateTotalJobs() {
        return Integer.divideUnsigned(this.batchData.size(), this.batch.getInvocationsPerBatchJob());
    }
}

