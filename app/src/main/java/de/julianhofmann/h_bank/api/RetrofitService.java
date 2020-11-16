package de.julianhofmann.h_bank.api;

import android.content.SharedPreferences;
import android.util.Log;

import de.julianhofmann.h_bank.BalanceCache;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class RetrofitService {
    public static final String URL = "http://192.168.0.200:5000/";
    public static String name;
    public static String token;

    private static SharedPreferences sharedPreferences;

    private static Retrofit retrofit;
    private static HBankApi hBankApi;

    public static void init(SharedPreferences sp) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        if (hBankApi == null) {
            hBankApi = retrofit.create(HBankApi.class);
        }

        if (sharedPreferences == null) {
            sharedPreferences = sp;
        }
    }


    public static void logout() {
        name = null;
        token = null;

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove("name");
        edit.remove("token");
        edit.apply();

        BalanceCache.clear();
    }

    public static String getAuthorization() {
        if (token != null) {
            return "Bearer " + token;
        }
        return "";
    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static HBankApi getHbankApi() {
        return hBankApi;
    }
}
