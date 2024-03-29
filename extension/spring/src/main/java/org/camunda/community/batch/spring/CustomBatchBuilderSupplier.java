package org.camunda.community.batch.spring;

import java.io.Serializable;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.community.batch.CustomBatchBuilder;

import java.util.function.Supplier;

/**
 * Supplier that returns a new instance of {@link CustomBatchBuilder} when called.
 *
 * Use to avoid static access of builder in beans - increase testability.
 *
 * @param <T> the type of the batch builder
 */
public class CustomBatchBuilderSupplier<T extends Serializable> implements Supplier<CustomBatchBuilder<T>>{

  private final ProcessEngineConfiguration processEngineConfiguration;

  public CustomBatchBuilderSupplier(final ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public CustomBatchBuilder<T> get() {
    return (CustomBatchBuilder<T>) CustomBatchBuilder.of().configuration(processEngineConfiguration);
  }
}
