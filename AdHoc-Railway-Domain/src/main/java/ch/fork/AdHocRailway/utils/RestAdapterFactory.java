package ch.fork.AdHocRailway.utils;

import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.services.impl.rest.AdHocServerError;
import com.google.gson.Gson;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

/**
 * Created by fork on 4/4/14.
 */
public class RestAdapterFactory {


    public static RestAdapter createRestAdapter(final String appId) {

        Gson gson = GsonFactory.createGson();
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

    private static class AdHocRailwayRetrofitErrorHandler implements ErrorHandler {

        @Override
        public Throwable handleError(RetrofitError cause) {
            if (cause.getResponse().getStatus() == 400) {
                AdHocServerError error = (AdHocServerError) cause.getBodyAs(AdHocServerError.class);
                return new ManagerException(error.getMsg(), cause);
            }
            throw new IllegalStateException("unknown error during the communication with the server");
        }
    }
}
