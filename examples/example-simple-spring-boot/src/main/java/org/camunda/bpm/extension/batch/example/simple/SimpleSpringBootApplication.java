package org.camunda.bpm.extension.batch.example.simple;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.extension.batch.CustomBatchBuilder;
import org.camunda.bpm.extension.batch.plugin.CustomBatchHandlerPlugin;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
@EnableProcessApplication
public class SimpleSpringBootApplication {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSpringBootApplication.class);
  public static void main(final String... args) throws Exception {
    SpringApplication.run(SimpleSpringBootApplication.class, args);
  }

  @Bean
  public PrintStringBatchJobHandler simpleCustomBatchJobHandler() {
    return new PrintStringBatchJobHandler();
  }

  @Bean
  public ProcessEnginePlugin customBatchHandlerPlugin(PrintStringBatchJobHandler printStringBatchJobHandler) {
    return new CustomBatchHandlerPlugin(Collections.singletonList(printStringBatchJobHandler));
  }

  @Autowired
  private PrintStringBatchJobHandler printStringBatchJobHandler;

  @EventListener
  public void afterEngineStarted(PostDeployEvent event) {
      logger.info("Create new Batch");
      final List<String> simpleStringList = IntStream.range(0,200)
        .mapToObj(i -> "SomeRandomBatchData_" + UUID.randomUUID())
        .collect(toList());

      CustomBatchBuilder.of(simpleStringList)
        .configuration(event.getProcessEngine().getProcessEngineConfiguration())
        .jobHandler(printStringBatchJobHandler)
        .create();
  }
}
