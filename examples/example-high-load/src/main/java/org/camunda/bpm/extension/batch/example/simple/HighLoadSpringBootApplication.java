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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
@EnableProcessApplication
public class HighLoadSpringBootApplication {

  private static final Logger logger = LoggerFactory.getLogger(HighLoadSpringBootApplication.class);

  public static void main(final String... args) {
    SpringApplication.run(HighLoadSpringBootApplication.class, args);
  }

  @Bean
  public SomeImportantHeavyStuffJobHandler someImportantHeavyStuffJobHandler() {
    return new SomeImportantHeavyStuffJobHandler();
  }

  @Bean
  public SomeOtherImportantHeavyStuffJobHandler someOtherImportantHeavyStuffJobHandler() {
    return new SomeOtherImportantHeavyStuffJobHandler();
  }

  @Bean
  public ProcessEnginePlugin customBatchHandlerPlugin(
    final SomeImportantHeavyStuffJobHandler someImportantHeavyStuffJobHandler,
    final SomeOtherImportantHeavyStuffJobHandler someOtherImportantHeavyStuffJobHandler) {
    return new CustomBatchHandlerPlugin(Arrays.asList(someImportantHeavyStuffJobHandler, someOtherImportantHeavyStuffJobHandler));
  }

  @Autowired
  private SomeImportantHeavyStuffJobHandler someImportantHeavyStuffJobHandler;

  @Autowired
  private SomeOtherImportantHeavyStuffJobHandler someOtherImportantHeavyStuffJobHandler;

  @EventListener
  public void afterEngineStarted(final PostDeployEvent event) {
    logger.info("Create new Batch");
    final List<String> simpleStringList = IntStream.range(0,10000)
      .mapToObj(i -> "SomeRandomBatchData_" + UUID.randomUUID())
      .collect(toList());

    CustomBatchBuilder.of(simpleStringList)
      .configuration(event.getProcessEngine().getProcessEngineConfiguration())
      .jobHandler(someImportantHeavyStuffJobHandler)
      .create();

    CustomBatchBuilder.of(simpleStringList)
      .configuration(event.getProcessEngine().getProcessEngineConfiguration())
      .jobHandler(someOtherImportantHeavyStuffJobHandler)
      .create();

    CustomBatchBuilder.of(simpleStringList)
      .configuration(event.getProcessEngine().getProcessEngineConfiguration())
      .jobHandler(someImportantHeavyStuffJobHandler)
      .create();

    CustomBatchBuilder.of(simpleStringList)
      .configuration(event.getProcessEngine().getProcessEngineConfiguration())
      .jobHandler(someOtherImportantHeavyStuffJobHandler)
      .create();
  }
}
