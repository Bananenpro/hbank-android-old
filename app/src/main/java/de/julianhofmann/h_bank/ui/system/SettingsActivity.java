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
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.util.SettingsService;
import de.julianhofmann.h_bank.util.UpdateService;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SwitchCompat offlineLoginSwitch = findViewById(R.id.offline_login_switch);
        offlineLoginSwitch.setChecked(SettingsService.getOfflineLogin());
        SwitchCompat fingerprintLoginSwitch = findViewById(R.id.fingerprint_switch);
        fingerprintLoginSwitch.setChecked(SettingsService.getFingerprintLogin());
        SwitchCompat autoLoginSwitch = findViewById(R.id.auto_login_switch);
        autoLoginSwitch.setChecked(SettingsService.getAutoLogin());
        SwitchCompat checkForUpdatesSwitch = findViewById(R.id.check_for_updates_switch);
        checkForUpdatesSwitch.setChecked(SettingsService.getCheckForUpdates());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_settings:
                settings();
                return true;
            case R.id.options_server_info:
                serverInfo();
                return true;
            case R.id.options_logout:
                logout();
                return true;
            case R.id.options_check_for_updates:
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void settings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    private void serverInfo() {
        Intent i = new Intent(this, ServerInfoActivity.class);
        startActivity(i);
    }

    private void update() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        UpdateService.update(this);
    }

    private void logout() {
        String name = RetrofitService.getName();
        RetrofitService.logout();
        switchToLoginActivity(name);
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("name", name);
        i.putExtra("logout", true);
        startActivity(i);
        finishAffinity();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}