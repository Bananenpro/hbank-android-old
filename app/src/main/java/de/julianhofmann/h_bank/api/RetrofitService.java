package de.julianhofmann.h_bank.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    public static String token;

    private static Retrofit retrofit;
    private static HBankApi hBankApi;

    private static void init() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.200:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        hBankApi = retrofit.create(HBankApi.class);
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
