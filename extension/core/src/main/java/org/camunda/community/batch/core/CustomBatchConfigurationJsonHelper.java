package org.camunda.community.batch.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.util.StringUtil;

import java.io.Serializable;

public class CustomBatchConfigurationJsonHelper<T extends Serializable> implements CustomBatchConfigurationHelper<T> {

  private final Gson gson = createGsonMapper();

  private CustomBatchConfigurationJsonHelper() {
  }

  public static <T extends Serializable> CustomBatchConfigurationJsonHelper<T> of() {
    return new CustomBatchConfigurationJsonHelper<>();
  }

  private Gson createGsonMapper() {
    return new GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(CustomBatchConfiguration.class, new CustomBatchConfigurationTypeAdapter<T>())
      .create();
  }

  @Override
  public CustomBatchConfiguration<T> readConfiguration(final byte[] serializedConfiguration) {
    return gson.fromJson(new String(serializedConfiguration), CustomBatchConfiguration.class);
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
    return StringUtil.toByteArray(gson.toJson(configuration));
  }

}
