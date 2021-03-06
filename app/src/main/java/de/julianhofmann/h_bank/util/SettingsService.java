package de.julianhofmann.h_bank.util;

import android.content.SharedPreferences;

public class SettingsService {
    private static boolean offlineLogin;
    private static boolean fingerprintLogin;
    private static boolean autoLogin;
    private static boolean checkForUpdates;
    private static boolean cashNoteFunction;
    private static boolean autoRefresh;
    private static int autoRefreshInterval;

    private static SharedPreferences sharedPreferences;
    private static String username;

    public static void init(SharedPreferences sp, String username) {
        sharedPreferences = sp;
        SettingsService.username = username;
        offlineLogin = sp.getBoolean(username + "_settings_offline_login", true);
        fingerprintLogin = sp.getBoolean(username + "_settings_fingerprint_login", false);
        autoLogin = sp.getBoolean(username + "_settings_auto_login", false);
        checkForUpdates = sp.getBoolean(username + "_settings_check_for_updates", true);
        cashNoteFunction = sp.getBoolean(username + "_settings_cash_note_function", false);
        autoRefresh = sp.getBoolean(username + "_settings_auto_refresh", true);
        autoRefreshInterval = sp.getInt(username + "_settings_auto_refresh_interval", 2000);
    }

    public static void deleteSettings(String username) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(username+"_settings_offline_login");
        edit.remove(username+"_settings_fingerprint_login");
        edit.remove(username+"_settings_auto_login");
        edit.remove(username+"_settings_check_for_updates");
        edit.remove(username + "_settings_auto_refresh");
        edit.remove(username + "_settings_auto_refresh_interval");
        edit.remove(username + "_settings_cash_note_function");
        edit.apply();
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

    public static boolean getAutoRefresh() {
        return autoRefresh;
    }

    public static void setAutoRefresh(boolean autoRefresh) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(username + "_settings_auto_refresh", autoRefresh);
        edit.apply();
        SettingsService.autoRefresh = autoRefresh;
    }

    public static int getAutoRefreshInterval() {
        return autoRefreshInterval;
    }

    public static void setAutoRefreshInterval(int autoRefreshInterval) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(username + "_settings_auto_refresh_interval", autoRefreshInterval);
        edit.apply();
        SettingsService.autoRefreshInterval = autoRefreshInterval;
    }

    public static boolean getCashNoteFunction() {
        return cashNoteFunction;
    }

    public static void setCashNoteFunction(boolean cashNoteFunction) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(username + "_settings_cash_note_function", cashNoteFunction);
        edit.apply();
        SettingsService.cashNoteFunction = cashNoteFunction;
    }
}
