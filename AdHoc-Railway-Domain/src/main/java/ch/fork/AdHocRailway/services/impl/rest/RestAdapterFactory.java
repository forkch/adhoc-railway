package ch.fork.AdHocRailway.services.impl.rest;

import com.google.gson.*;
import com.google.gson.internal.bind.DateTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.Header;

import java.lang.reflect.Type;

/**
 * Created by fork on 4/4/14.
 */
public class RestAdapterFactory {


    public static RestAdapter createRestAdapter(final String appId) {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Boolean.class, new BooleanSerializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://localhost:3000")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("adhoc-railway-appid", appId);
                    }
                })
                .build();
        return restAdapter;
    }

    private static class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if(json == null) {
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
