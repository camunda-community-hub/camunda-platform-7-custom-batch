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
public class BatchStarter {

  private static final Logger logger = LoggerFactory.getLogger(BatchStarter.class.getSimpleName());

  private PrintStringBatchJobHandler printStringBatchJobHandler;

  private ProcessEngineConfiguration processEngineConfiguration;

  private SecureRandom random = new SecureRandom();

  private boolean batchAlreadyStarted = false;

  @Autowired
  private BatchStarter(ProcessEngineConfiguration configuration, PrintStringBatchJobHandler jobHandler) {
    this.printStringBatchJobHandler = jobHandler;
    this.processEngineConfiguration = configuration;
  }

  @Scheduled(fixedDelay = 1500L)
  public void createAndStartBatch() {
    if(batchAlreadyStarted)
      return;

    logger.info("Create new Batch");
    final List<String> simpleStringList = getSimpleStringList();

    CustomBatchBuilder.of(simpleStringList)
      .configuration(processEngineConfiguration)
      .jobHandler(printStringBatchJobHandler)
      .create();

    batchAlreadyStarted = true;
  }

  /**
   * Just some list with random string data
   */
  private List<String> getSimpleStringList() {
    final List<String> data = new ArrayList<>();
    for (int i = 0; i < 200; i++) {
      data.add("SomeRandomBatchData_" + nextRandomId());
    }
    return data;
  }

  private String nextRandomId() {
    return new BigInteger(130, random).toString(32);
  }
}
