package org.camunda.community.batch;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.community.batch.core.CustomBatchConfiguration;
import org.camunda.community.batch.testhelper.TestCustomBatchJobHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.managementService;
import static org.camunda.community.batch.testhelper.CustomBatchTestHelper.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CustomBatchBuilderTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  private Batch batch;

  private final TestCustomBatchJobHandler testCustomBatchJobHandler = spy(TestCustomBatchJobHandler.class);

  private ProcessEngineConfigurationImpl engineConfiguration;

  private final List<String> data = Arrays.asList("Test", "Test2", "Test3", "Test4");

  @Before
  public void setUp() {
    engineConfiguration = (ProcessEngineConfigurationImpl) processEngine().getProcessEngineConfiguration();
    engineConfiguration.setCustomBatchJobHandlers(new ArrayList<>());
    engineConfiguration.getCustomBatchJobHandlers().add(testCustomBatchJobHandler);
  }

  @After
  public void tearDown() {
    managementService().deleteBatch(batch.getId(), true);
  }

  @Test
  public void create_batch_with_defaults() {
    batch = getDefaultBatch(data);

    assertThat(batch).isNotNull();
    assertThat(batch.getType()).isEqualTo("test-type");
    assertThat(batch.getBatchJobsPerSeed()).isEqualTo(100); //Default camunda value
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(1); //Default camunda value

    final Batch batchLookup = managementService().createBatchQuery().singleResult();
    assertThat(batchLookup).isNotNull();
  }

  @Test
  public void create_batch_with_own_config() {
    batch = CustomBatchBuilder.of(data)
      .configuration(engineConfiguration)
      .jobHandler(testCustomBatchJobHandler)
      .jobsPerSeed(10)
      .invocationsPerBatchJob(5)
      .create(engineConfiguration.getCommandExecutorTxRequired());

    assertThat(batch).isNotNull();
    assertThat(batch.getType()).isEqualTo("test-type");
    assertThat(batch.getBatchJobsPerSeed()).isEqualTo(10);
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(5);

    final Batch batchLookup = managementService().createBatchQuery().singleResult();
    assertThat(batchLookup).isNotNull();
  }

  @Test
  public void seed_job_is_created() {
    batch = getDefaultBatch(data);

    final JobDefinition seedJobDefinition = getSeedJobDefinition(batch);
    assertThat(seedJobDefinition).isNotNull();

    final Job seedJob = getSeedJob(batch);
    assertThat(seedJob).isNotNull();
  }

  @Test
  public void priority_is_set_on_jobs() {
    final long jobDefinitionPriority = 3L;
    batch = CustomBatchBuilder.of(data)
      .configuration(engineConfiguration)
      .jobHandler(testCustomBatchJobHandler)
      .jobPriority(jobDefinitionPriority)
      .create(engineConfiguration.getCommandExecutorTxRequired());
    final JobDefinition generatorJobDefinition = getGeneratorJobDefinition(batch);

    assertThat(generatorJobDefinition.getOverridingJobPriority()).isEqualTo(jobDefinitionPriority);

    final Job seedJob = getSeedJob(batch);
    assertThat(seedJob.getPriority()).isEqualTo(jobDefinitionPriority);
    executeJob(seedJob.getId());

    final List<Job> jobs = getJobsForDefinition(generatorJobDefinition);
    assertThat(jobs).isNotEmpty();
    jobs.stream().map(Job::getPriority).forEach(priority -> assertThat(priority).isEqualTo(jobDefinitionPriority));

    assertThat(getSeedJob(batch)).isNull();

    final Job monitorJob = getMonitorJob(batch);
    assertThat(monitorJob.getPriority()).isEqualTo(jobDefinitionPriority);

    executeJob(monitorJob.getId());
  }

  @Test
  public void exclusive_false_is_set_to_batch_configuration() {
    batch = CustomBatchBuilder.of(data)
      .configuration(engineConfiguration)
      .jobHandler(testCustomBatchJobHandler)
      .exclusive(false)
      .create(engineConfiguration.getCommandExecutorTxRequired());

    final ArgumentCaptor<CustomBatchConfiguration> argumentCaptor = ArgumentCaptor.forClass(CustomBatchConfiguration.class);
    verify(testCustomBatchJobHandler).writeConfiguration(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue()).isNotNull();
    assertThat(argumentCaptor.getValue().isExclusive()).isFalse();
  }

  @Test
  public void exclusive_true_is_set_to_batch_configuration() {
    batch = CustomBatchBuilder.of(data)
      .configuration(engineConfiguration)
      .jobHandler(testCustomBatchJobHandler)
      .exclusive(true)
      .create(engineConfiguration.getCommandExecutorTxRequired());

    final ArgumentCaptor<CustomBatchConfiguration> argumentCaptor = ArgumentCaptor.forClass(CustomBatchConfiguration.class);
    verify(testCustomBatchJobHandler).writeConfiguration(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue()).isNotNull();
    assertThat(argumentCaptor.getValue().isExclusive()).isTrue();
  }

  @Test
  public void batch_jobs_are_created() {
    batch = CustomBatchBuilder.of(data)
      .configuration(engineConfiguration)
      .jobHandler(testCustomBatchJobHandler)
      .jobsPerSeed(4)
      .invocationsPerBatchJob(3)
      .create(engineConfiguration.getCommandExecutorTxRequired());

    executeJob(getSeedJob(batch).getId());

    final List<Job> list = getJobsForDefinition(getGeneratorJobDefinition(batch));
    assertThat(list.size()).isEqualTo(2);
    assertThat(batch.getTotalJobs()).isEqualTo(2);
  }

  private Batch getDefaultBatch(final List<String> data) {
    return CustomBatchBuilder.of(data)
      .configuration(engineConfiguration)
      .jobHandler(testCustomBatchJobHandler)
      .create(engineConfiguration.getCommandExecutorTxRequired());
  }
}
