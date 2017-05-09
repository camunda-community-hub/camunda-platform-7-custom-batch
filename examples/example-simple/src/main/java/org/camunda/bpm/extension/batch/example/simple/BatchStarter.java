package org.camunda.bpm.extension.batch.example.simple;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.extension.batch.CustomBatchBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class BatchStarter implements Runnable, InitializingBean {

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  private SimpleCustomBatchJobHandler simpleCustomBatchJobHandler;

  private ProcessEngineConfiguration processEngineConfiguration;

  private SecureRandom random = new SecureRandom();

  private int count = 0;

  @Autowired
  public BatchStarter(ProcessEngineConfiguration configuration, SimpleCustomBatchJobHandler jobHandler) {
    this.simpleCustomBatchJobHandler = jobHandler;
    this.processEngineConfiguration = configuration;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    new Thread(this).start();
  }

  @Override
  public void run() {
    while (true) {
      logger.info("Create new Batch" + String.valueOf(count));
      final List<String> simpleStringList = getSimpleStringList("Batch" + String.valueOf(count) + "_");

      CustomBatchBuilder.of(simpleStringList)
        .configuration(processEngineConfiguration)
        .jobHandler(simpleCustomBatchJobHandler)
        .create();

      count++;

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
      }
    }
  }

  private List<String> getSimpleStringList(String prefix) {
    final List<String> data = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      data.add(prefix + nextRandomId());
    }
    return data;
  }

  private String nextRandomId() {
    return new BigInteger(130, random).toString(32);
  }
}
