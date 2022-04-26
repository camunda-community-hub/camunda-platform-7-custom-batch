package org.camunda.community.batch.example.simple;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.community.batch.CustomBatchBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class BatchStarter implements InitializingBean {

  private static final Logger logger = Logger.getLogger(BatchStarter.class.getSimpleName());

  private PrintStringBatchJobHandler printStringBatchJobHandler;

  private ProcessEngineConfiguration processEngineConfiguration;

  private SecureRandom random = new SecureRandom();

  @Autowired
  public BatchStarter(ProcessEngineConfiguration configuration, PrintStringBatchJobHandler jobHandler) {
    this.printStringBatchJobHandler = jobHandler;
    this.processEngineConfiguration = configuration;
  }

  @Override
  public void afterPropertiesSet() {
    createAndStartBatch();
  }

  public void createAndStartBatch() {
      logger.info("Create new Batch");
      final List<String> simpleStringList = getSimpleStringList();

      CustomBatchBuilder.of(simpleStringList)
        .configuration(processEngineConfiguration)
        .jobHandler(printStringBatchJobHandler)
        .create();
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
