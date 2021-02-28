package de.julianhofmann.h_bank.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.system.ConnectionSettingsActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.util.SettingsService;
import de.julianhofmann.h_bank.util.UpdateService;

public class BaseActivity extends AppCompatActivity {

    protected boolean gone = true;
    protected boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void init(@LayoutRes int layoutId) {
        setContentView(layoutId);
        gone = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (RetrofitService.isLoggedIn())
            inflater.inflate(R.menu.options_menu, menu);
        else
            inflater.inflate(R.menu.login_options_menu, menu);
        return true;
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!gone) {
            switch (item.getItemId()) {
                case R.id.options_settings:
                    gone = true;
                    settings();
                    return true;
                case R.id.options_server_info:
                    gone = true;
                    serverInfo();
                    return true;
                case R.id.options_logout:
                    gone = true;
                    logout();
                    return true;
                case R.id.options_check_for_updates:
                    update(false);
                    return true;
                case R.id.options_connection_settings:
                    gone = true;
                    connectionSettings();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected void connectionSettings() {
        Intent i = new Intent(this, ConnectionSettingsActivity.class);
        startActivity(i);
        if (this instanceof ConnectionSettingsActivity) {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    protected void settings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        if (this instanceof SettingsActivity) {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    protected void serverInfo() {
        Intent i = new Intent(this, InfoActivity.class);
        startActivity(i);
        if (this instanceof InfoActivity) {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    protected void update(boolean autoUpdate) {
        if (!gone) {
            gone = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }

            UpdateService.update(this, autoUpdate);
            gone = false;
        }
    }

    protected void logout() {
        String name = RetrofitService.getName();
        RetrofitService.logout();
        switchToLoginActivity(name);
    }

    protected void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("logout", true);
        i.putExtra("name", name);
        startActivity(i);
        finishAffinity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        gone = false;
        paused = false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}

