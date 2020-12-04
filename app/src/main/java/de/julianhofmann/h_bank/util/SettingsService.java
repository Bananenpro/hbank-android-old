package de.julianhofmann.h_bank.util;

import android.content.SharedPreferences;

public class SettingsService {
    private static boolean offlineLogin;
    private static boolean fingerprintLogin;
    private static boolean autoLogin;
    private static boolean checkForUpdates;

    private static SharedPreferences sharedPreferences;
    private static String username;

    public static void init(SharedPreferences sp, String username) {
        sharedPreferences = sp;
        SettingsService.username = username;
        offlineLogin = sp.getBoolean(username + "_settings_offline_login", true);
        fingerprintLogin = sp.getBoolean(username + "_settings_fingerprint_login", false);
        autoLogin = sp.getBoolean(username + "_settings_auto_login", false);
        checkForUpdates = sp.getBoolean(username + "_settings_check_for_updates", true);
    }

    public static boolean getOfflineLogin() {
        return offlineLogin;
    }

    public static void setOfflineLogin(boolean offlineLogin) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(username + "_settings_offline_login", offlineLogin);
        edit.apply();
        SettingsService.offlineLogin = offlineLogin;
    }

    public static boolean getFingerprintLogin() {
        return fingerprintLogin;
    }

    public static void setFingerprintLogin(boolean fingerprintLogin) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(username + "_settings_fingerprint_login", fingerprintLogin);
        edit.apply();
        SettingsService.fingerprintLogin = fingerprintLogin;
    }

    public static boolean getAutoLogin() {
        return autoLogin;
    }

    public static void setAutoLogin(boolean autoLogin) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(username + "_settings_auto_login", autoLogin);
        edit.apply();
        SettingsService.autoLogin = autoLogin;
    }

    public static boolean getCheckForUpdates() {
        return checkForUpdates;
    }

    public static void setCheckForUpdates(boolean checkForUpdates) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(username + "_settings_check_for_updates", checkForUpdates);
        edit.apply();
        SettingsService.checkForUpdates = checkForUpdates;
    }
}
