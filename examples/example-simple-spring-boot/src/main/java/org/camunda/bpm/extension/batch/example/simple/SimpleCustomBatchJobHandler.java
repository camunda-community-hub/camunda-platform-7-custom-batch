package org.camunda.bpm.extension.batch.example.simple;

import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.extension.batch.CustomBatchJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCustomBatchJobHandler extends CustomBatchJobHandler<String> {

  public static final String TYPE = "simple-batch-handler";

  Logger logger = LoggerFactory.getLogger(SimpleCustomBatchJobHandler.class);

  @Override
  public void execute(List<String> data, CommandContext commandContext) {
      logger.info("Work on data: {}", data.get(0));
  }

  @Override
  public String getType() {
      return TYPE;
  }
}
