package de.julianhofmann.h_bank.api;

import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    public static final String URL = "http://192.168.0.200:5000/";
    public static String name;
    public static String token;

    private static Retrofit retrofit;
    private static HBankApi hBankApi;

    private static void init() {
        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        hBankApi = retrofit.create(HBankApi.class);
    }

    public static String getAuthorization() {
        if (token != null) {
            return "Bearer " + token;
        }
        return "";
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) init();
        return retrofit;
    }

    public static HBankApi getHbankApi() {
        if (hBankApi == null) init();
        return hBankApi;
    }
}
