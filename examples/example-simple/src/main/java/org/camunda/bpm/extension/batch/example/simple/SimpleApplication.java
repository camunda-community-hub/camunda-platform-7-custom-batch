package org.camunda.bpm.extension.batch.example.simple;

import java.util.Collections;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.extension.batch.plugin.CustomBatchHandlerPlugin;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableProcessApplication("mySimpleApplication")
public class SimpleApplication {

  public static void main(final String... args) throws Exception {
    SpringApplication.run(SimpleApplication.class, args);
  }

  @Bean
  public SimpleCustomBatchJobHandler simpleCustomBatchJobHandler() {
    return new SimpleCustomBatchJobHandler();
  }

  @Bean
  public ProcessEnginePlugin simpleEnginePlugin(SimpleCustomBatchJobHandler simpleCustomBatchJobHandler) {
    return new CustomBatchHandlerPlugin(Collections.singletonList(simpleCustomBatchJobHandler));
  }
}
