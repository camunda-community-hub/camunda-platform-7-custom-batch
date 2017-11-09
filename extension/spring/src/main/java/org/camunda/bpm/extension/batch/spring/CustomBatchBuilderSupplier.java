package org.camunda.bpm.extension.batch.spring;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.extension.batch.CustomBatchBuilder;

import java.util.function.Supplier;

/**
 * Supplier that returns a new instance of {@link CustomBatchBuilder} when called.
 *
 * Use to avoid static access of builder in beans - increase testability.
 *
 * @param <T> the type of the batch builder
 */
public class CustomBatchBuilderSupplier<T> implements Supplier<CustomBatchBuilder<T>>{

  private final ProcessEngineConfiguration processEngineConfiguration;

  public CustomBatchBuilderSupplier(final ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public CustomBatchBuilder<T> get() {
    return (CustomBatchBuilder<T>) CustomBatchBuilder.of().configuration(processEngineConfiguration);
  }
}
