package org.camunda.bpm.extension.batch.util;

import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Since camunda uses camundajar.com.google.gson.Gson; we need our own utils class
 */
public final class JsonUtil {

  private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

  private static final Gson gsonMapper = createGsonMapper();

  public static void addField(final JsonObject jsonObject, final String name, final String value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static void addField(final JsonObject jsonObject, final String name, final Boolean value) {
    if (jsonObject != null && name != null && value != null) {
      jsonObject.addProperty(name, value);
    }
  }

  public static byte[] asBytes(final JsonElement jsonObject) {
    String jsonString = null;

    if (jsonObject != null) {
      try {
        jsonString = getGsonMapper().toJson(jsonObject);

      } catch (final JsonIOException e) {
        log.error("Error happens during json handling: ", e);
      }
    }

    if (jsonString == null) {
      jsonString = "";
    }

    return StringUtil.toByteArray(jsonString);
  }

  public static JsonObject asObject(final byte[] byteArray) {
    String stringValue = null;

    if (byteArray != null) {
      stringValue = StringUtil.fromBytes(byteArray);
    }

    if (stringValue == null) {
      return createObject();

    }

    JsonObject jsonObject = null;
    try {
      jsonObject = getGsonMapper().fromJson(stringValue, JsonObject.class);

    } catch (final JsonParseException e) {
      log.error("Error happens during json handling: ", e);
    }

    if (jsonObject != null) {
      return jsonObject;

    } else {
      return createObject();

    }
  }

  public static Object asPrimitiveObject(final JsonPrimitive jsonValue) {
    if (jsonValue == null) {
      return null;
    }

    Object rawObject = null;

    if (jsonValue.isNumber()) {
      LazilyParsedNumber numberValue = null;

      try {
        numberValue = (LazilyParsedNumber) jsonValue.getAsNumber();

      } catch (final ClassCastException | NumberFormatException e) {
        log.error("Error happens during json handling: ", e);
      }

      if (numberValue != null) {

        final String numberString = numberValue.toString();
        if (numberString != null) {
          rawObject = parseNumber(numberString);
        }

      }
    } else { // string, boolean
      try {
        rawObject = getGsonMapper().fromJson(jsonValue, Object.class);

      } catch (final JsonSyntaxException | JsonIOException e) {
        log.error("Error happens during json handling: ", e);
      }

    }

    if (rawObject != null) {
      return rawObject;

    } else {
      return null;

    }
  }

  protected static Number parseNumber(final String numberString) {
    if (numberString == null) {
      return null;
    }

    try {
      return Integer.parseInt(numberString);

    } catch (final NumberFormatException ignored) { }

    try {
      return Long.parseLong(numberString);

    } catch (final NumberFormatException ignored) { }

    try {
      return Double.parseDouble(numberString);

    } catch (final NumberFormatException ignored) { }

    return null;
  }

  public static boolean getBoolean(final JsonObject json, final String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      try {
        return json.get(memberName).getAsBoolean();

      } catch (final ClassCastException | IllegalStateException e) {
        log.error("Error happens during json handling: ", e);

        return false;

      }
    } else {
      return false;

    }
  }

  public static String getString(final JsonObject json, final String memberName) {
    if (json != null && memberName != null && json.has(memberName)) {
      return getString(json.get(memberName));

    } else {
      return "";

    }
  }

  public static String getString(final JsonElement jsonElement) {
    if (jsonElement == null) {
      return "";
    }

    try {
      return jsonElement.getAsString();

    } catch (final ClassCastException | IllegalStateException e) {
      log.error("Error happens during json handling: ", e);

      return "";

    }
  }

  public static JsonObject getObject(final JsonElement json) {
    if (json != null && json.isJsonObject()) {
      return json.getAsJsonObject();

    } else {
      return createObject();

    }
  }

  public static JsonObject createObject() {
    return new JsonObject();

  }

  public static Gson getGsonMapper() {
    return gsonMapper;
  }

  public static Gson createGsonMapper() {
    return new GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(Map.class, (JsonDeserializer<Map<String, Object>>) (json, typeOfT, context) -> {

        final Map<String, Object> map = new HashMap<>();

        for (final Map.Entry<String, JsonElement> entry : getObject(json).entrySet()) {
          if (entry != null) {
            final String key = entry.getKey();
            final JsonElement jsonElement = entry.getValue();

            if (jsonElement != null && jsonElement.isJsonNull()) {
              map.put(key, null);

            } else if (jsonElement != null && jsonElement.isJsonPrimitive()) {

              final Object rawValue = asPrimitiveObject((JsonPrimitive) jsonElement);
              if (rawValue != null) {
                map.put(key, rawValue);

              }
            }
          }
        }

        return map;
      })
      .create();
  }

}
