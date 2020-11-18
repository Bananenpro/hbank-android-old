package de.julianhofmann.h_bank;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class BalanceCache {
    private static String lastBalance;
    private static SharedPreferences sharedPreferences;


    public static void init(SharedPreferences sp) {
        sharedPreferences = sp;
    }

    public static String getBalance(String name) {
        if (lastBalance != null) return lastBalance;
        else {
            return sharedPreferences.getString(name+"_balance", "");
        }
    }

    public static void update(String name, String newBalance) {
        if (lastBalance == null || !lastBalance.equals(newBalance)) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(name+"_balance", newBalance);

            Set<String> names = sharedPreferences.getStringSet("names", new HashSet<>());
            names.add(name);
            edit.putStringSet("names", names);

            edit.apply();
            lastBalance = newBalance;
        }
    }

    public static void clear() {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        Set<String> names = sharedPreferences.getStringSet("names", new HashSet<>());
        for (String name : names) {
            edit.remove(name + "_balance");
        }
        edit.apply();
    }
}
