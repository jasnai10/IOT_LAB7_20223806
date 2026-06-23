package com.example.bicipucp.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton de Retrofit. Expone una única instancia de {@link BiciApi}
 * apuntando a {@link ApiConfig#BASE_URL}.
 */
public final class ApiClient {

    private static volatile BiciApi biciApi;

    private ApiClient() {
    }

    public static BiciApi getBiciApi() {
        if (biciApi == null) {
            synchronized (ApiClient.class) {
                if (biciApi == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(ApiConfig.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    biciApi = retrofit.create(BiciApi.class);
                }
            }
        }
        return biciApi;
    }
}
