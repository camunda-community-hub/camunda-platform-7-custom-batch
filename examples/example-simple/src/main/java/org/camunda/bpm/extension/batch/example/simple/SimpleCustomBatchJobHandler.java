package org.camunda.bpm.extension.batch.example.simple;

import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.extension.batch.CustomBatchJobHandler;

public class SimpleCustomBatchJobHandler extends CustomBatchJobHandler<String> {

  public static final String TYPE = "simple-batch-handler";

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  @Override
  public void execute(List<String> data, CommandContext commandContext) {
    logger.info("Work on data: " + data.get(0));
  }

  @Override
  public String getType() { return TYPE; }
}
