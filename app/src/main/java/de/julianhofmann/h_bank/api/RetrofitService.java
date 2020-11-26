package de.julianhofmann.h_bank.api;

import android.content.SharedPreferences;

import de.julianhofmann.h_bank.util.PasswordCache;
import de.julianhofmann.h_bank.util.BalanceCache;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    public static final String URL = "http://192.168.0.200:5000/";
    public static String name = null;
    public static String token = null;

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

    public static void login(String loginName, String loginToken) {
        name = loginName;
        token = loginToken;

        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString("name", RetrofitService.name);
        edit.putString("token", RetrofitService.token);

        edit.apply();
    }

    public static void logout() {
        name = null;
        token = null;

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove("name");
        edit.remove("token");

        edit.apply();

        PasswordCache.clearPassword(sharedPreferences);
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
