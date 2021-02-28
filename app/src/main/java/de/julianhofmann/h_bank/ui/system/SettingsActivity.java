package de.julianhofmann.h_bank.ui.system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.util.SettingsService;
import de.julianhofmann.h_bank.util.UpdateService;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_settings);
        SwitchCompat offlineLoginSwitch = findViewById(R.id.offline_login_switch);
        offlineLoginSwitch.setChecked(SettingsService.getOfflineLogin());
        SwitchCompat fingerprintLoginSwitch = findViewById(R.id.fingerprint_switch);
        fingerprintLoginSwitch.setChecked(SettingsService.getFingerprintLogin());
        SwitchCompat autoLoginSwitch = findViewById(R.id.auto_login_switch);
        autoLoginSwitch.setChecked(SettingsService.getAutoLogin());
        SwitchCompat checkForUpdatesSwitch = findViewById(R.id.check_for_updates_switch);
        checkForUpdatesSwitch.setChecked(SettingsService.getCheckForUpdates());
    }

    public void connectionSettings(View v) {
        Intent i = new Intent(this, ConnectionSettingsActivity.class);
        startActivity(i);
    }

    public void deleteUser(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, DeleteUserActivity.class);
            startActivity(i);
        }
    }

    public void setOfflineLogin(View v) {
        SwitchCompat sc = (SwitchCompat)v;
        boolean value = sc.isChecked();
        SettingsService.setOfflineLogin(value);
        if (!value) {
            SwitchCompat fingerprintLogin = findViewById(R.id.fingerprint_switch);
            fingerprintLogin.setChecked(false);
            SettingsService.setFingerprintLogin(false);
            SwitchCompat autoLogin = findViewById(R.id.auto_login_switch);
            autoLogin.setChecked(false);
            SettingsService.setAutoLogin(false);
            RetrofitService.clearPreferences();
        } else {
            RetrofitService.login(RetrofitService.getName(), RetrofitService.getToken());
        }
    }

    public void setFingerprintLogin(View v) {
        SwitchCompat sc = (SwitchCompat)v;
        boolean value = sc.isChecked();
        SettingsService.setFingerprintLogin(value);
        if (value) {
            SwitchCompat offlineLogin = findViewById(R.id.offline_login_switch);
            offlineLogin.setChecked(true);
            SettingsService.setOfflineLogin(true);
            SwitchCompat autoLogin = findViewById(R.id.auto_login_switch);
            autoLogin.setChecked(false);
            SettingsService.setAutoLogin(false);
            RetrofitService.login(RetrofitService.getName(), RetrofitService.getToken());
        }
    }

    public void setAutoLogin(View v) {
        SwitchCompat sc = (SwitchCompat)v;
        boolean value = sc.isChecked();
        SettingsService.setAutoLogin(value);
        if (value) {
            SwitchCompat offlineLogin = findViewById(R.id.offline_login_switch);
            offlineLogin.setChecked(true);
            SettingsService.setOfflineLogin(true);
            SwitchCompat fingerprintLogin = findViewById(R.id.fingerprint_switch);
            fingerprintLogin.setChecked(false);
            SettingsService.setFingerprintLogin(false);
            RetrofitService.login(RetrofitService.getName(), RetrofitService.getToken());
        }
    }

    public  void setCheckForUpdates(View v) {
        SwitchCompat sc = (SwitchCompat)v;
        boolean value = sc.isChecked();
        SettingsService.setCheckForUpdates(value);
    }
}