package org.camunda.community.batch.example.simple;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.community.batch.CustomBatchBuilder;
import org.camunda.community.batch.plugin.CustomBatchHandlerPlugin;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableProcessApplication
public class SimpleSpringBootWithWebappApplication {

  private static final Logger logger = LoggerFactory.getLogger(SimpleSpringBootWithWebappApplication.class);

  public static void main(final String... args) {
    SpringApplication.run(SimpleSpringBootWithWebappApplication.class, args);
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
