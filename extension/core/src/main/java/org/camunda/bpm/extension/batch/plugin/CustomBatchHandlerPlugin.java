package org.camunda.bpm.extension.batch.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

public class CustomBatchHandlerPlugin extends AbstractProcessEnginePlugin {

    private final List<BatchJobHandler<?>> batchJobHandler;

    public CustomBatchHandlerPlugin(List<BatchJobHandler<?>> batchJobHandler) {
        this.batchJobHandler = batchJobHandler;
    }

    public CustomBatchHandlerPlugin(BatchJobHandler<?> batchJobHandler) {
      this.batchJobHandler = Collections.singletonList(batchJobHandler);
    }

    public static CustomBatchHandlerPlugin of(List<BatchJobHandler<?>> batchJobHandler) {
        return new CustomBatchHandlerPlugin(batchJobHandler);
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        if (processEngineConfiguration.getCustomBatchJobHandlers() == null) {
            processEngineConfiguration.setCustomBatchJobHandlers(new ArrayList<>());
        }

        processEngineConfiguration.getCustomBatchJobHandlers().addAll(batchJobHandler);
    }

}
