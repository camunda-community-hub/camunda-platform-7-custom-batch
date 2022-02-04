package org.camunda.bpm.extension.batch.example.simple;

import org.apache.commons.lang3.ThreadUtils;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.extension.batch.CustomBatchJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class SomeOtherImportantHeavyStuffJobHandler extends CustomBatchJobHandler<String> {

  private static final Logger logger = LoggerFactory.getLogger(SomeOtherImportantHeavyStuffJobHandler.class.getSimpleName());

  public static final String TYPE = "other-high-load-batch-handler";

  @Override
  public void execute(final List<String> data, final CommandContext commandContext) {
    data.forEach(dataEntry -> logger.info("Work on data: " + dataEntry));
    try {
      ThreadUtils.sleep(Duration.ofSeconds(30));
    } catch (InterruptedException e) {}
    logger.info("Work on data finished!");
  }

  @Override
  public String getType() { return TYPE; }
}
