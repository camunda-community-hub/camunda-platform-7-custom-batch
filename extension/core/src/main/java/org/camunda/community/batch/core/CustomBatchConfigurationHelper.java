package org.camunda.community.batch.core;

import java.io.Serializable;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;

public interface CustomBatchConfigurationHelper<T extends Serializable> {

  CustomBatchConfiguration<T> readConfiguration(final byte[] serializedConfiguration);

  ByteArrayEntity saveConfiguration(final CustomBatchConfiguration<T> jobConfiguration);

  byte[] writeConfiguration(final CustomBatchConfiguration<T> configuration);
}
