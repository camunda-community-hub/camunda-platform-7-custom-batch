package org.camunda.bpm.extension.batch.core;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.extension.batch.util.JsonObjectConverter;
import org.camunda.bpm.extension.batch.util.JsonUtil;

import java.io.Serializable;

public class CustomBatchConfigurationJsonHelper<T extends Serializable> implements CustomBatchConfigurationHelper<T> {

  private final JsonObjectConverter<CustomBatchConfiguration<T>> converter;

  private CustomBatchConfigurationJsonHelper(final JsonObjectConverter<CustomBatchConfiguration<T>> converter) {
    this.converter = converter;
  }

  public static <T extends Serializable> CustomBatchConfigurationJsonHelper<T> of(final JsonObjectConverter<CustomBatchConfiguration<T>> converter) {
    return new CustomBatchConfigurationJsonHelper<>(converter);
  }

  @Override
  public CustomBatchConfiguration<T> readConfiguration(final byte[] serializedConfiguration) {
    return converter.toObject(JsonUtil.asObject(serializedConfiguration));
  }

  @Override
  public ByteArrayEntity saveConfiguration(final CustomBatchConfiguration<T> jobConfiguration) {
    final ByteArrayManager byteArrayManager = Context.getCommandContext().getByteArrayManager();

    final ByteArrayEntity configurationEntity = new ByteArrayEntity();
    configurationEntity.setBytes(writeConfiguration(jobConfiguration));
    byteArrayManager.insert(configurationEntity);
    return configurationEntity;
  }

  @Override
  public byte[] writeConfiguration(final CustomBatchConfiguration<T> configuration) {
    return JsonUtil.asBytes(converter.toJsonObject(configuration));
  }

}
