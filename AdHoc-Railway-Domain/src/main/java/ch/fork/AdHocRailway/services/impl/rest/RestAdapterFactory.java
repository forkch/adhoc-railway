package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.manager.ManagerException;
import com.google.gson.*;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

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
                .setErrorHandler(new AdHocRailwayRetrofitErrorHandler())
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

    private static class AdHocRailwayRetrofitErrorHandler implements ErrorHandler {

        @Override
        public Throwable handleError(RetrofitError cause) {
            if(cause.getResponse().getStatus() == 400) {
                AdHocServerError error = (AdHocServerError) cause.getBodyAs(AdHocServerError.class);
                return new ManagerException(error.getMsg(), cause);
            }
            throw new IllegalStateException("unknown error during the communication with the server");
        }
    }
}
