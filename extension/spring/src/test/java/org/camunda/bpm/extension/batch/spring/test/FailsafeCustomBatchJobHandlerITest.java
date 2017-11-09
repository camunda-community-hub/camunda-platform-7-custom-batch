package org.camunda.bpm.extension.batch.spring.test;

import org.awaitility.Duration;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.extension.batch.plugin.CustomBatchHandlerPlugin;
import org.camunda.bpm.extension.batch.spring.CustomBatchBuilderSupplier;
import org.camunda.bpm.extension.batch.spring.FailsafeCustomBatchJobHandler;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FailsafeCustomBatchJobHandlerITest.FailsafeApplication.class)
public class FailsafeCustomBatchJobHandlerITest {

  private static final Logger logger = LoggerFactory.getLogger(FailsafeCustomBatchJobHandlerITest.class);

  @SpringBootApplication
  @EnableProcessApplication
  @Import(CustomBatchBuilderSupplier.class)
  static class FailsafeApplication {
    @Component
    public static class FailingBatchJobHandler extends FailsafeCustomBatchJobHandler<String> {

      public static final String TYPE = "failing-batch-handler";

      public FailingBatchJobHandler(PlatformTransactionManager transactionManager) {
        super(transactionManager);
      }

      @Override
      public void executeFailsafe(String taskId, CommandContext commandContext) {
        commandContext.getProcessEngineConfiguration()
          .getProcessEngine()
          .getTaskService()
          .claim(taskId, null);
      }

      @Override
      public String getType() {
        return TYPE;
      }
    }

    @Component("onAssignmentListener")
    public static class OnAssignmentListener implements TaskListener {

      @Override
      public void notify(DelegateTask task) {
        if (task.getAssignee() == null) {
          throw new RuntimeException("st went wrong while assigning the task!");
        }
      }
    }

    public static void main(final String... args) throws Exception {
      SpringApplication.run(FailsafeApplication.class, args);
    }

    @Bean
    public ProcessEnginePlugin customBatchHandlerPlugin(FailingBatchJobHandler failingBatchJobHandler) {
      return CustomBatchHandlerPlugin.of(failingBatchJobHandler);
    }

    @Bean
    public ProcessEnginePlugin noJobRetriesAndHistoryLevel() {
      return new AbstractProcessEnginePlugin() {
        @Override
        public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

          processEngineConfiguration.setDefaultNumberOfRetries(1);
          processEngineConfiguration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
        }

        @Override
        public String toString() {
          return "jobRetries=1";
        }
      };
    }
  }

  @Autowired
  private FailsafeApplication.FailingBatchJobHandler jobHandler;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private TaskService taskService;

  @Autowired
  private CustomBatchBuilderSupplier<String> customBatchBuilderSupplier;

  @Autowired
  private HistoryService historyService;

  @Test
  public void no_failed_jobs() throws InterruptedException {

    repositoryService.createDeployment()
      .addModelInstance("dummy.bpmn",
        Bpmn.createExecutableProcess("dummy")
          .startEvent()
          .userTask("task").camundaTaskListenerDelegateExpression(TaskListener.EVENTNAME_ASSIGNMENT, "${onAssignmentListener}").camundaAssignee("user")
          .endEvent()
          .done())
      .deploy();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dummy");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    customBatchBuilderSupplier.get().batchData(singletonList(task.getId())).jobHandler(jobHandler).create();

    // Fails when no successful batch was written after 5 seconds
    await()
      .pollDelay(Duration.FIVE_HUNDRED_MILLISECONDS)
      .atMost(Duration.FIVE_SECONDS)
      .untilAsserted(() -> {
        HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
        assertThat(historicBatch).isNotNull();
        assertThat(historicBatch.getType()).isEqualTo(FailsafeApplication.FailingBatchJobHandler.TYPE);
        assertThat(historicBatch.getTotalJobs()).isEqualTo(1);
        assertThat(historicBatch.getEndTime()).isNotNull();
      });
  }
}
