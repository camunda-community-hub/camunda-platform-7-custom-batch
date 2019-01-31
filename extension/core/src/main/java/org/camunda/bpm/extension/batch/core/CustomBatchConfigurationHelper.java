package org.camunda.bpm.extension.batch.core;

import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;

public interface CustomBatchConfigurationHelper<T> {

  CustomBatchConfiguration<T> readConfiguration(final byte[] serializedConfiguration);

  ByteArrayEntity saveConfiguration(final CustomBatchConfiguration<T> jobConfiguration);

  byte[] writeConfiguration(final CustomBatchConfiguration<T> configuration);
}
