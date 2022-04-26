package org.camunda.community.batch;

import java.io.Serializable;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.camunda.community.batch.core.CustomBatchConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomBatchBuilder<T extends Serializable> {

  private final BatchEntity batch = new BatchEntity();

  private ProcessEngineConfigurationImpl engineConfiguration;

  private BatchJobHandler<CustomBatchConfiguration> batchJobHandler;

  private List<T> batchData;

  private Optional<Long> jobPriority = Optional.empty();

  private boolean exclusive = true;

  protected CustomBatchBuilder() {}

  protected CustomBatchBuilder(final List<T> data) {
    this.batchData = data;
  }

  public static <T extends Serializable> CustomBatchBuilder<T> of() {
    return new CustomBatchBuilder<>();
  }

  public static <T extends Serializable> CustomBatchBuilder<T> of(final List<T> data) {
    return new CustomBatchBuilder<>(data);
  }

  /**
   * If you don't want to use the default Configuration (Context.getProcessEngineConfiguration()), provide it here.
   *
   * @param configuration ... The engine configuration
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> configuration(final ProcessEngineConfiguration configuration) {
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
  public CustomBatchBuilder<T> jobHandler(final BatchJobHandler<CustomBatchConfiguration> jobHandler) {
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
  public CustomBatchBuilder<T> jobsPerSeed(final int jobsPerSeed) {
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
  public CustomBatchBuilder<T> invocationsPerBatchJob(final int invocationsPerBatchJob) {
    this.batch.setInvocationsPerBatchJob(invocationsPerBatchJob);
    return this;
  }

  /**
   * List of data which should be processed by the batch jobs.
   *
   * @param data ... Data for Batch
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> batchData(final List<T> data) {
    this.batchData = data;
    return this;
  }

  /**
   * Define a jobPriority which is used by job executor when acquiring jobs.
   * <p>
   * This is only considered if jobExecutorAcquireByPriority is active.
   *
   * @param jobPriority ... jobPriority for generated batch jobs
   * @return CustomBatchBuilder
   */
  public CustomBatchBuilder<T> jobPriority(final Long jobPriority) {
    this.jobPriority = Optional.ofNullable(jobPriority);
    return this;
  }

  public CustomBatchBuilder<T> exclusive(final boolean exclusive) {
    this.exclusive = exclusive;
    return this;
  }

  public Batch create(CommandExecutor executor) {
    initDefaults();

    if (executor == null) {
      executor = this.engineConfiguration.getCommandExecutorTxRequired();
    }

    return executor.execute((commandContext) -> {
      final CustomBatchConfiguration<T> configuration = new CustomBatchConfiguration<>(this.batchData, this.exclusive);

      this.batch.setConfigurationBytes(this.batchJobHandler.writeConfiguration(configuration));
      this.batch.setTotalJobs(calculateTotalJobs());

      commandContext.getBatchManager().insert(this.batch);

      final JobDefinitionEntity seedJobDefinition = this.batch.createSeedJobDefinition(null);
      final JobDefinitionEntity batchJobDefinition = this.batch.createBatchJobDefinition();
      final JobDefinitionEntity monitorJobDefinition = this.batch.createMonitorJobDefinition();
      this.batch.fireHistoricStartEvent();
      this.jobPriority.ifPresent(((Consumer<Long>) batchJobDefinition::setJobPriority)
        .andThen(seedJobDefinition::setJobPriority)
        .andThen(monitorJobDefinition::setJobPriority));
      this.batch.createSeedJob();

      return this.batch;
    });
  }

  public Batch create() {
    return create(null);
  }

  private int calculateTotalJobs() {
    return Double.valueOf(Math.ceil((double) this.batchData.size() / this.batch.getInvocationsPerBatchJob())).intValue();
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

