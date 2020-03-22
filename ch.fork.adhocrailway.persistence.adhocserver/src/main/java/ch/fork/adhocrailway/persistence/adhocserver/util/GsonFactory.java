package ch.fork.adhocrailway.persistence.adhocserver.util;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by fork on 4/5/14.
 */
public class GsonFactory {

    public static Gson createGson() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Boolean.class, new BooleanSerializer())
                .create();
        return gson;
    }

    private static class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null) {
                return Boolean.FALSE;
            }
            return Boolean.parseBoolean(json.getAsString());
        }

        @Override
        public JsonElement serialize(Boolean src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

}
