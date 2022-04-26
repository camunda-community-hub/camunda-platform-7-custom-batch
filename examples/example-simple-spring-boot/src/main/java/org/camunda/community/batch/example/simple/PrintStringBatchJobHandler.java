package org.camunda.community.batch.example.simple;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.community.batch.CustomBatchJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PrintStringBatchJobHandler extends CustomBatchJobHandler<String> {

  private static final Logger logger = LoggerFactory.getLogger(PrintStringBatchJobHandler.class.getSimpleName());

  public static final String TYPE = "simple-batch-handler";

  @Override
  public void execute(final List<String> data, final CommandContext commandContext) {
    data.forEach(dataEntry -> logger.info("Work on data: " + dataEntry));
  }

  @Override
  public String getType() { return TYPE; }
}
