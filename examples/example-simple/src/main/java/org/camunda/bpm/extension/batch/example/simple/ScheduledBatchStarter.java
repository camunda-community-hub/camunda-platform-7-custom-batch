package org.camunda.bpm.extension.batch.example.simple;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.extension.batch.CustomBatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledBatchStarter {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private SimpleCustomBatchJobHandler simpleCustomBatchJobHandler;

  private ProcessEngineConfiguration processEngineConfiguration;

  private SecureRandom random = new SecureRandom();

  private int count = 0;

  @Autowired
  private ScheduledBatchStarter(ProcessEngineConfiguration configuration, SimpleCustomBatchJobHandler jobHandler) {
    this.simpleCustomBatchJobHandler = jobHandler;
    this.processEngineConfiguration = configuration;
  }

  @Scheduled(initialDelay = 5000L, fixedDelay = 5000L)
  public void exitApplicationWhenProcessIsFinished() {
    logger.info("Create new batch");
    final List<String> demoData = getDemoData("Batch"+String.valueOf(count++)+"_");

    CustomBatchBuilder.of(demoData)
      .configuration(processEngineConfiguration)
      .jobHandler(simpleCustomBatchJobHandler)
      .create();
  }

  private List<String> getDemoData(String prefix) {
    final List<String> data = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      data.add(prefix+nextRandomId());
    }
    return data;
  }

  private String nextRandomId() {
    return new BigInteger(130, random).toString(32);
  }
}
