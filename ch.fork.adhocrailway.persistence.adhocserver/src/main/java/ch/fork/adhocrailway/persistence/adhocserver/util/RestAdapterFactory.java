package ch.fork.adhocrailway.persistence.adhocserver.util;

import ch.fork.adhocrailway.persistence.adhocserver.impl.rest.AdHocServerError;
import ch.fork.adhocrailway.services.AdHocServiceException;
import com.google.gson.Gson;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

/**
 * Created by fork on 4/4/14.
 */
public class RestAdapterFactory {


    public static Retrofit createRestAdapter(String endpointURL, final String appId) {

        Gson gson = GsonFactory.createGson();
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                final Request requestWithAppId = request.newBuilder().addHeader("adhoc-railway-appid", appId).build();
                return chain.proceed(requestWithAppId);
            }
        });

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.interceptors().add(interceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endpointURL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit;

    }

}
