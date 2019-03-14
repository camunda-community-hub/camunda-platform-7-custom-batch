package org.camunda.bpm.extension.batch.util;

import com.google.gson.JsonObject;

/**
 * Since camunda uses camundajar.com.google.gson.JsonObject; we need our own converter class
 */
public abstract class JsonObjectConverter <T> {

  public String toJson(final T object) {
    return toJsonObject(object).toString();
  }

  public abstract JsonObject toJsonObject(T object);

  public abstract T toObject(JsonObject jsonString);
}
