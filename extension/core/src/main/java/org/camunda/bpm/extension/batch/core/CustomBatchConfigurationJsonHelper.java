package org.camunda.bpm.extension.batch.core;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.impl.util.json.JSONTokener;

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
    final Reader jsonReader = StringUtil.readerFromBytes(serializedConfiguration);
    return converter.toObject(new JSONObject(new JSONTokener(jsonReader)));
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
    final JSONObject jsonObject = converter.toJsonObject(configuration);

    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final Writer writer = StringUtil.writerForStream(outStream);

    jsonObject.write(writer);
    IoUtil.flushSilently(writer);

    return outStream.toByteArray();
  }

}
