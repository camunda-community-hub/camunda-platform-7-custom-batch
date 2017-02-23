package org.camunda.bpm.extension.batch;

import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCustomBatchJobHandler extends CustomBatchJobHandler<String> {

    Logger logger = LoggerFactory.getLogger(TestCustomBatchJobHandler.class);

    @Override
    public void execute(List<String> data, CommandContext commandContext) {

        logger.debug("Work on data", data);
    }

    @Override
    public String getType() {
        // TODO provide sensible implementation
        return "test-type";
    }
}
