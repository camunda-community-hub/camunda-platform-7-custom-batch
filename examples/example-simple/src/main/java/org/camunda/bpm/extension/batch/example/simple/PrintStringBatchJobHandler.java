package org.camunda.bpm.extension.batch.example.simple;

import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.extension.batch.CustomBatchJobHandler;

public class PrintStringBatchJobHandler extends CustomBatchJobHandler<String> {

  private static final Logger logger = Logger.getLogger(PrintStringBatchJobHandler.class.getSimpleName());

  public static final String TYPE = "simple-batch-handler";

  @Override
  public void execute(List<String> data, CommandContext commandContext) {
    logger.info("Work on data: " + data.get(0));
  }

  @Override
  public String getType() { return TYPE; }
}
