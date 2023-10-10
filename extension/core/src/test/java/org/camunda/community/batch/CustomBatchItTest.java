package org.camunda.community.batch;

import org.awaitility.Awaitility;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.camunda.community.batch.testhelper.TestCustomBatchJobHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.managementService;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine;
import static org.camunda.community.batch.testhelper.CustomBatchTestHelper.getGeneratorJobDefinition;
import static org.camunda.community.batch.testhelper.CustomBatchTestHelper.getJobsForDefinition;

public class CustomBatchItTest {

  @RegisterExtension
  public static final ProcessEngineExtension processEngineExtension = ProcessEngineExtension.builder()
    .configurationResource("camundaITTest.cfg.xml")
    .build();

  private Batch batch;

  private final TestCustomBatchJobHandler testCustomBatchJobHandler = new TestCustomBatchJobHandler();

  private ProcessEngineConfigurationImpl configuration;

  @BeforeEach
  public void setUp() {
    configuration = (ProcessEngineConfigurationImpl) processEngine().getProcessEngineConfiguration();
    configuration.setCustomBatchJobHandlers(new ArrayList<>());
    configuration.getCustomBatchJobHandlers().add(testCustomBatchJobHandler);
  }

  @AfterEach
  public void tearDown() {
    managementService().deleteBatch(batch.getId(), true);
  }

  @AfterAll
  public static void close() {
    processEngineExtension.getProcessEngine().close();
  }

  @Test
  public void jobIsInErrorStateAfterException() {
    batch = getDefaultBatch(Arrays.asList(null, null));

    Awaitility.await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
        List<Job> list = getJobsForDefinition(getGeneratorJobDefinition(batch));
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getExceptionMessage()).isNotEmpty();
    });
  }

  private Batch getDefaultBatch(final List<String> data) {
    return CustomBatchBuilder.of(data)
      .configuration(configuration)
      .jobHandler(testCustomBatchJobHandler)
      .create(configuration.getCommandExecutorTxRequired());
  }

}
