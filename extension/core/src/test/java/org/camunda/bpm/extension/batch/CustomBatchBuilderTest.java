package org.camunda.bpm.extension.batch;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.batch.testhelper.TestCustomBatchJobHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.managementService;
import static org.camunda.bpm.extension.batch.testhelper.CustomBatchTestHelper.executeJob;
import static org.camunda.bpm.extension.batch.testhelper.CustomBatchTestHelper.getGeneratorJobDefinition;
import static org.camunda.bpm.extension.batch.testhelper.CustomBatchTestHelper.getJobsForDefinition;
import static org.camunda.bpm.extension.batch.testhelper.CustomBatchTestHelper.getSeedJob;
import static org.camunda.bpm.extension.batch.testhelper.CustomBatchTestHelper.getSeedJobDefinition;

public class CustomBatchBuilderTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  private Batch batch;

  private TestCustomBatchJobHandler testCustomBatchJobHandler = new TestCustomBatchJobHandler();

  private ProcessEngineConfigurationImpl configuration;

  private List<String> data = Arrays.asList("Test", "Test2", "Test3", "Test4");

  @Before
  public void setUp() throws Exception {
    configuration = (ProcessEngineConfigurationImpl) processEngine().getProcessEngineConfiguration();
    configuration.setCustomBatchJobHandlers(new ArrayList<>());
    configuration.getCustomBatchJobHandlers().add(testCustomBatchJobHandler);
  }

  @After
  public void tearDown() throws Exception {
    managementService().deleteBatch(batch.getId(), true);
  }

  @Test
  public void create_batch_with_defaults() throws Exception {
    batch = getDefaultBatch(data);

    assertThat(batch).isNotNull();
    assertThat(batch.getType()).isEqualTo("test-type");
    assertThat(batch.getBatchJobsPerSeed()).isEqualTo(100); //Default camunda value
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(1); //Default camunda value

    final Batch batchLookup = managementService().createBatchQuery().singleResult();
    assertThat(batchLookup).isNotNull();
  }

  @Test
  public void create_batch_with_own_config() throws Exception {
    batch = CustomBatchBuilder.of(data)
      .configuration(configuration)
      .jobHandler(testCustomBatchJobHandler)
      .jobsPerSeed(10)
      .invocationsPerBatchJob(5)
      .create(configuration.getCommandExecutorTxRequired());

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

    JobDefinition seedJobDefinition = getSeedJobDefinition(batch);
    assertThat(seedJobDefinition).isNotNull();

    Job seedJob = getSeedJob(batch);
    assertThat(seedJob).isNotNull();
  }

  @Test
  public void batch_jobs_are_created() {
    batch = CustomBatchBuilder.of(data)
      .configuration(configuration)
      .jobHandler(testCustomBatchJobHandler)
      .jobsPerSeed(4)
      .invocationsPerBatchJob(3)
      .create(configuration.getCommandExecutorTxRequired());

    executeJob(getSeedJob(batch).getId());

    final List<Job> list = getJobsForDefinition(getGeneratorJobDefinition(batch));
    assertThat(list.size()).isEqualTo(2);
    assertThat(batch.getTotalJobs()).isEqualTo(2);
  }

  private Batch getDefaultBatch(List<String> data) {
    return CustomBatchBuilder.of(data)
      .configuration(configuration)
      .jobHandler(testCustomBatchJobHandler)
      .create(configuration.getCommandExecutorTxRequired());
  }
}
