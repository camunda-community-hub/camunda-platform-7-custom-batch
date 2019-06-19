package org.camunda.bpm.extension.batch.core;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.SerializationUtils;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/*
 * This class was used in fewer version to serialize batch data and should only guarantee downward compatibility.
 * It will be removed in later versions!
 **/
@Deprecated
public class CustomBatchConfigurationDownwardCompatibleWrapper<T extends Serializable> implements CustomBatchConfigurationHelper<T> {

  private final Logger log = LoggerFactory.getLogger(CustomBatchConfigurationDownwardCompatibleWrapper.class);

  private final CustomBatchConfigurationHelper<T> delegate;

  public CustomBatchConfigurationDownwardCompatibleWrapper(final CustomBatchConfigurationHelper<T> delegate) {
    this.delegate = delegate;
  }

  public static <T extends Serializable> CustomBatchConfigurationHelper<T> of(final CustomBatchConfigurationHelper<T> delegate) {
    return new CustomBatchConfigurationDownwardCompatibleWrapper<>(delegate);
  }

  @Override
  public CustomBatchConfiguration<T> readConfiguration(final byte[] serializedConfiguration) {
    try {
      return delegate.readConfiguration(serializedConfiguration);
    } catch (final JsonSyntaxException exception) {
      log.warn("could not read config as json / trying to parse plain list of serialized data (deprecated)");
      return CustomBatchConfiguration.of(SerializationUtils.deserialize(serializedConfiguration));
    }
  }

  @Override
  public ByteArrayEntity saveConfiguration(final CustomBatchConfiguration<T> jobConfiguration) {
    return delegate.saveConfiguration(jobConfiguration);
  }

  @Override
  public byte[] writeConfiguration(final CustomBatchConfiguration<T> configuration) {
    return delegate.writeConfiguration(configuration);
  }
}
