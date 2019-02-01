/*
 * Copyright © 2012 - 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.extension.batch.core;

import java.util.Base64;
import org.apache.commons.lang3.SerializationUtils;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.io.Serializable;

public class CustomBatchConfigurationJsonConverter<T extends Serializable> extends JsonObjectConverter<CustomBatchConfiguration<T>> {

  private static final String EXCLUSIVE = "exclusive";
  private static final String DATA_SERIALIZED = "data_serialized";

  public static <T extends Serializable> CustomBatchConfigurationJsonConverter<T> of() {
    return new CustomBatchConfigurationJsonConverter<>();
  }

  @Override
  public JSONObject toJsonObject(final CustomBatchConfiguration<T> customBatchConfiguration) {
    final JSONObject json = new JSONObject();

    JsonUtil.addField(json, EXCLUSIVE, customBatchConfiguration.isExclusive());
    JsonUtil.addField(json, DATA_SERIALIZED, Base64.getEncoder().encodeToString(SerializationUtils.serialize((Serializable) customBatchConfiguration.getData())));

    return json;
  }

  @Override
  public CustomBatchConfiguration<T> toObject(final JSONObject json) {
    final String jsonSerializedData = json.getString(DATA_SERIALIZED);
    final byte[] byteArray = Base64.getDecoder().decode(jsonSerializedData);

    return CustomBatchConfiguration.of(
      SerializationUtils.deserialize(byteArray),
      json.getBoolean(EXCLUSIVE));
  }

}
