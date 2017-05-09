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

public class CustomBatchBuilder<T> {

  private final BatchEntity batch = new BatchEntity();

  private ProcessEngineConfigurationImpl engineConfiguration;

  private BatchJobHandler<CustomBatchConfiguration> batchJobHandler;

  private List<T> batchData;

  protected CustomBatchBuilder() {}

  protected CustomBatchBuilder(List<T> data) {
    this.batchData = data;
  }

  public static <T> CustomBatchBuilder<T> of() {
    return new CustomBatchBuilder<>();
  }

  public static <T> CustomBatchBuilder<T> of(List<T> data) {
    return new CustomBatchBuilder<>(data);
  }

  /**
   * If you don't want to use the default Configuration (Context.getProcessEngineConfiguration()), provide it here.
   *
   * @param configuration ... The engine configuration
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> configuration(ProcessEngineConfiguration configuration) {
    this.engineConfiguration = (ProcessEngineConfigurationImpl) configuration;
    return this;
  }

  /**
   * Batch Job Handler for this batch.
   * Make sure that this handler is registered as custom batch job handler in engine!
   *
   * @param jobHandler ... Batch job handler which should be used
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> jobHandler(BatchJobHandler<CustomBatchConfiguration> jobHandler) {
    this.batchJobHandler = jobHandler;
    this.batch.setType(jobHandler.getType());
    return this;
  }

  /**
   * The number of batch execution jobs created per seed job invocation.
   * The batch seed job is invoked until it has created all batch execution jobs required by the batch
   * (see totalJobs ... is calculated from size of input list and invocationsPerBatchJob).
   *
   * @param jobsPerSeed ... The number of batch execution jobs created per seed job invocation
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> jobsPerSeed(int jobsPerSeed) {
    this.batch.setBatchJobsPerSeed(jobsPerSeed);
    return this;
  }

  /**
   * Every batch execution job invokes the command executed by the batch invocationsPerBatchJob times.
   * <p>
   * E.g., for a process instance migration batch this specifies the number of process instances which
   * are migrated per batch execution job.
   *
   * @param invocationsPerBatchJob ... The amount of data which should pe processed per batch job
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> invocationsPerBatchJob(int invocationsPerBatchJob) {
    this.batch.setInvocationsPerBatchJob(invocationsPerBatchJob);
    return this;
  }

  /**
   * List of data which should be processed by the batch jobs.
   *
   * @param data ... Data for Batch
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> batchData(List<T> data) {
    this.batchData = data;
    return this;
  }

  public Batch create(CommandExecutor executor) {
    initDefaults();

    if (executor == null)
      executor = this.engineConfiguration.getCommandExecutorTxRequired();

    return executor.execute((commandContext) -> {
      final CustomBatchConfiguration<T> configuration = new CustomBatchConfiguration<>(this.batchData);

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
    return create(null);
  }

  private int calculateTotalJobs() {
    return Integer.divideUnsigned(this.batchData.size(), this.batch.getInvocationsPerBatchJob());
  }

  private void initDefaults() {
    if (this.engineConfiguration == null) {
      this.engineConfiguration = Context.getProcessEngineConfiguration();
    }

    if (this.batch.getBatchJobsPerSeed() == 0) {
      this.batch.setBatchJobsPerSeed(this.engineConfiguration.getBatchJobsPerSeed());
    }

    if (this.batch.getInvocationsPerBatchJob() == 0) {
      this.batch.setInvocationsPerBatchJob(this.engineConfiguration.getInvocationsPerBatchJob());
    }
  }
}

