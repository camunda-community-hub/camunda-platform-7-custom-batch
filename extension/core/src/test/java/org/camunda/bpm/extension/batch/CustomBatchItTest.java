package org.camunda.bpm.extension.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.managementService;
import static org.camunda.bpm.extension.batch.testhelper.CustomBatchTestHelper.getGeneratorJobDefinition;
import static org.camunda.bpm.extension.batch.testhelper.CustomBatchTestHelper.getJobsForDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.batch.testhelper.TestCustomBatchJobHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * //FIXME (patrick) This is not working on camundas jenkins
 */
public class CustomBatchItTest {

  @Rule
  public final ProcessEngineRule processEngineRule = new ProcessEngineRule("camundaITTest.cfg.xml");

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
  @Ignore
  public void jobIsInErrorStateAfterException() throws Exception {
    batch = getDefaultBatch(Arrays.asList(null, null));

    //Wait for Jobexecuter
    List<Job> list = new ArrayList<>();
    for(int i = 0; i<60; i++) {
      Thread.sleep(500);
      list = getJobsForDefinition(getGeneratorJobDefinition(batch));
      if(list.size() > 0) {
        break;
      }
    }

    assertThat(list.get(0).getExceptionMessage()).isNotEmpty();
  }

  private Batch getDefaultBatch(List<String> data) {
    return CustomBatchBuilder.of(data)
      .configuration(configuration)
      .jobHandler(testCustomBatchJobHandler)
      .create(configuration.getCommandExecutorTxRequired());
  }

}
