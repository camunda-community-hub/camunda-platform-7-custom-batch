package org.camunda.bpm.extension.batch.testhelper;

import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.extension.batch.CustomBatchJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCustomBatchJobHandler extends CustomBatchJobHandler<String> {

  Logger logger = LoggerFactory.getLogger(TestCustomBatchJobHandler.class);

  @Override
  public void execute(List<String> data, CommandContext commandContext) {

    data.forEach(logger::debug);

    if (data.get(0) == null)
      throw new RuntimeException("Data is null");
  }

  @Override
  public String getType() {
    return "test-type";
  }
}
