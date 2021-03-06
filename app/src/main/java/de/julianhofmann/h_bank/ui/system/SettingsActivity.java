package de.julianhofmann.h_bank.ui.system;

import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.annotations.SerializedName;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.util.SettingsService;

public class SettingsActivity extends BaseActivity {

    @Override
    @SuppressLint("SetTextI18n")
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
        SwitchCompat cashNoteFunctionSwitch = findViewById(R.id.cash_note_function_switch);
        cashNoteFunctionSwitch.setChecked(SettingsService.getCashNoteFunction());
        SwitchCompat autoRefreshSwitch = findViewById(R.id.auto_refresh_switch);
        autoRefreshSwitch.setChecked(SettingsService.getAutoRefresh());
        EditText autoRefreshInterval = findViewById(R.id.auto_refresh_interval);
        autoRefreshInterval.setText(Integer.toString(SettingsService.getAutoRefreshInterval()));
        autoRefreshInterval.setEnabled(autoRefreshSwitch.isChecked());
        autoRefreshInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkChangeAutoRefreshIntervalButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        checkChangeAutoRefreshIntervalButton();
    }

    private void checkChangeAutoRefreshIntervalButton() {
        SwitchCompat autoRefreshSwitch = findViewById(R.id.auto_refresh_switch);
        Button changeAutoRefreshInterval = findViewById(R.id.change_refresh_interval);
        EditText autoRefreshInterval = findViewById(R.id.auto_refresh_interval);
        boolean enabled = autoRefreshInterval.getText().length() > 0 && autoRefreshSwitch.isChecked();
        if (enabled) {
            int interval = Integer.parseInt(autoRefreshInterval.getText().toString());
            enabled = interval >= 1000 && interval != SettingsService.getAutoRefreshInterval();
        }
        changeAutoRefreshInterval.setEnabled(enabled);
    }

    @SuppressLint("SetTextI18n")
    public void setAutoRefreshInterval(View v) {
        EditText autoRefreshInterval = findViewById(R.id.auto_refresh_interval);
        int interval = Integer.parseInt(autoRefreshInterval.getText().toString());
        SettingsService.setAutoRefreshInterval(interval);
        autoRefreshInterval.setText(Integer.toString(SettingsService.getAutoRefreshInterval()));
        checkChangeAutoRefreshIntervalButton();
    }

    public void setAutoRefresh(View v) {
        EditText autoRefreshInterval = findViewById(R.id.auto_refresh_interval);
        SwitchCompat autoRefreshSwitch = findViewById(R.id.auto_refresh_switch);
        SettingsService.setAutoRefresh(autoRefreshSwitch.isChecked());
        autoRefreshInterval.setEnabled(autoRefreshSwitch.isChecked());
        checkChangeAutoRefreshIntervalButton();
    }

    public void connectionSettings(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, ConnectionSettingsActivity.class);
            startActivity(i);
        }
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
            RetrofitService.clearPassword();
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

    public void setCashNoteFunction(View v) {
        SwitchCompat sc = (SwitchCompat)v;
        boolean value = sc.isChecked();
        SettingsService.setCashNoteFunction(value);
    }
}