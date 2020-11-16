package de.julianhofmann.h_bank;

import android.content.SharedPreferences;

public class BalanceCache {
    private static String lastBalance;
    private static SharedPreferences sharedPreferences;


    public static void init(SharedPreferences sp) {
        sharedPreferences = sp;
    }

    public static String getBalance() {
        if (lastBalance != null) return lastBalance;
        else {
            return sharedPreferences.getString("balance", null);
        }
    }

    public static void update(String newBalance) {
        if (lastBalance == null || !lastBalance.equals(newBalance)) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("balance", newBalance);
            edit.apply();
            lastBalance = newBalance;
        }
    }

    public static void clear() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove("balance");
        edit.apply();
    }
}
