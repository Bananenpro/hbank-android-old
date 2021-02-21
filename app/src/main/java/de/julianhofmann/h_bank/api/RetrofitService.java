package de.julianhofmann.h_bank.api;

import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import de.julianhofmann.h_bank.util.BalanceCache;
import de.julianhofmann.h_bank.util.PasswordCache;
import de.julianhofmann.h_bank.util.SettingsService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static String ip;
    private static int port;
    private static String protocol;
    private static String serverPassword;

    private static String name = null;
    private static String token = null;

    private static SharedPreferences sharedPreferences;

    private static Retrofit retrofit;
    private static HBankApi hBankApi;

    public static void init(SharedPreferences sp) {

        if (sharedPreferences == null) {
            sharedPreferences = sp;
        }

        protocol = sharedPreferences.getString("protocol", null);
        ip = sharedPreferences.getString("ip_address", null);
        port = sharedPreferences.getInt("port", -1);
        serverPassword = sharedPreferences.getString("server_password", null);

        if (ip != null && port != -1 && serverPassword != null && retrofit == null) {
            OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(10, TimeUnit.SECONDS).connectTimeout(3, TimeUnit.SECONDS).addInterceptor(chain -> {
                Request request = chain.request().newBuilder().addHeader("Password", serverPassword).build();
                return chain.proceed(request);
            }).build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(getUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        if (retrofit != null && hBankApi == null) {
            hBankApi = retrofit.create(HBankApi.class);
        }
    }

    public static boolean isLoggedIn() {
        return name != null && token != null;
    }

    public static void login(String loginName, String loginToken) {
        name = loginName;
        token = loginToken;

        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString("name", RetrofitService.getName());
        if (SettingsService.getOfflineLogin()) edit.putString("token", RetrofitService.token);

        edit.apply();
    }

    public static void clearPreferences() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove("token");
        edit.apply();

        PasswordCache.clearPassword(sharedPreferences);
    }

    public static void reset() {
        name = null;
        token = null;

        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.clear();

        edit.putString("protocol", protocol);
        edit.putString("ip_address", ip);
        edit.putInt("port", port);
        edit.putString("server_password", serverPassword);

        edit.apply();

        PasswordCache.clearPassword(sharedPreferences);
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

    public static String getUrl() {
        return protocol + "://"+ip+":"+port+"/";
    }

    public static String getIpAddress() {
        return ip;
    }

    public static String getProtocol() {
        return protocol;
    }

    public static int getPort() {
        return port;
    }

    public static void changeUrl(String protocol, String ip_address, int port, String serverPassword) {
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString("protocol", protocol);
        edit.putString("ip_address", ip_address);
        edit.putInt("port", port);
        edit.putString("server_password", serverPassword);
        edit.apply();

        retrofit = null;
        hBankApi = null;

        init(sharedPreferences);
    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static HBankApi getHbankApi() {
        return hBankApi;
    }

    public static String getName() {
        return name;
    }

    public static String getToken() {
        return token;
    }

    public static String getServerPassword() {
        return serverPassword;
    }
}
