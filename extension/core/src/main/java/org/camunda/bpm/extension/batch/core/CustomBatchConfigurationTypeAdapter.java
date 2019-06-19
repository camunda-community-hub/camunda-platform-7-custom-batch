package org.camunda.bpm.extension.batch.core;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class CustomBatchConfigurationTypeAdapter<T extends Serializable> extends TypeAdapter<CustomBatchConfiguration> {

  private static final String EXCLUSIVE = "exclusive";
  private static final String DATA_SERIALIZED = "data_serialized";

  private final Logger log = LoggerFactory.getLogger(CustomBatchConfigurationTypeAdapter.class);

  @Override
  public void write(final JsonWriter jsonWriter, final CustomBatchConfiguration customBatchConfiguration) throws IOException {
    jsonWriter.beginObject()
      .name(EXCLUSIVE)
      .value(customBatchConfiguration.isExclusive())
      .name(DATA_SERIALIZED)
      .value(Base64.getEncoder().encodeToString(SerializationUtils.serialize((Serializable) customBatchConfiguration.getData())))
      .endObject();
  }

  @Override
  public CustomBatchConfiguration read(final JsonReader reader) throws IOException {
    reader.beginObject();

    List<T> data = null;
    Boolean exclusive = null;

    while (reader.hasNext()) {
      final JsonToken token = reader.peek();

      String fieldname = null;

      if (token.equals(JsonToken.NAME)) {
        fieldname = reader.nextName();
      }

      if (EXCLUSIVE.equals(fieldname)) {
        reader.peek();
        exclusive = reader.nextBoolean();
      } else if (DATA_SERIALIZED.equals(fieldname)) {
        reader.peek();
        data = SerializationUtils.deserialize(Base64.getDecoder().decode(reader.nextString()));
      } else {
        log.warn("Unknown property `{}`. It will be ignored.", fieldname);
      }
    }

    reader.endObject();

    if (Objects.nonNull(data) && Objects.nonNull(exclusive)) {
      return CustomBatchConfiguration.of(data, exclusive);
    }
    throw new MalformedJsonException("No data field or exclusive flag found");
  }
}
