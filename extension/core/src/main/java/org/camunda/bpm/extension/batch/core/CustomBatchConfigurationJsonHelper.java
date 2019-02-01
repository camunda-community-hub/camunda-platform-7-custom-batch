package org.camunda.bpm.extension.batch.core;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.SerializationUtils;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.impl.util.json.JSONException;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.impl.util.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBatchConfigurationJsonHelper<T extends Serializable> implements CustomBatchConfigurationHelper<T> {

  Logger LOGGER = LoggerFactory.getLogger(CustomBatchConfiguration.class);

  private final JsonObjectConverter<CustomBatchConfiguration<T>> converter;

  /*
   * This function was used in fewer version to serialize batch data and should only guarantee downward compatibility
   * It will be removed in later versions!
   **/
  @Deprecated
  private final Function<byte[], CustomBatchConfiguration<T>> oldConverter = bytes -> CustomBatchConfiguration.of(SerializationUtils.deserialize(bytes));

  private CustomBatchConfigurationJsonHelper(final JsonObjectConverter<CustomBatchConfiguration<T>> converter) {
    this.converter = converter;
  }

  public static <T extends Serializable> CustomBatchConfigurationJsonHelper<T> of(final JsonObjectConverter<CustomBatchConfiguration<T>> converter) {
    return new CustomBatchConfigurationJsonHelper<>(converter);
  }

  @Override
  public CustomBatchConfiguration<T> readConfiguration(final byte[] serializedConfiguration) {
    try {
      final Reader jsonReader = StringUtil.readerFromBytes(serializedConfiguration);
      return converter.toObject(new JSONObject(new JSONTokener(jsonReader)));
    } catch (JSONException exception) {
      LOGGER.warn("could not read config as json / trying to parse plain list of serialized data (deprecated)");
      return oldConverter.apply(serializedConfiguration);
    }
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
