package de.julianhofmann.h_bank.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.system.ConnectionSettingsActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseActivity extends AppCompatActivity {

    private final Handler connectHandler = new Handler();
    private Runnable connectRunnable;
    protected boolean gone = true;
    protected boolean paused = false;
    protected boolean offline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectRunnable = this::tryConnect;
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
        gone = true;
        Intent i = new Intent(this, ConnectionSettingsActivity.class);
        startActivity(i);
        if (this instanceof ConnectionSettingsActivity) {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    protected void settings() {
        gone = true;
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        if (this instanceof SettingsActivity) {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    protected void serverInfo() {
        gone = true;
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
        gone = true;
        String name = RetrofitService.getName();
        RetrofitService.logout();
        switchToLoginActivity(name);
    }

    protected void switchToLoginActivity(String name) {
        gone = true;
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("logout", true);
        i.putExtra("name", name);
        startActivity(i);
        finishAffinity();
    }

    protected void offline() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.no_connection_icon);
        } else if (!offline) {
            Toast.makeText(getApplicationContext(), R.string.cannot_reach_server, Toast.LENGTH_SHORT).show();
        }

        connectHandler.postDelayed(connectRunnable, 1000);

        offline = true;
    }

    protected void online() {
        if (getSupportActionBar() != null) {
            if (offline) {
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setIcon(R.drawable.connection_established_icon);
                new Handler().postDelayed(() -> {
                    getSupportActionBar().setDisplayShowHomeEnabled(false);
                    getSupportActionBar().setIcon(null);
                }, 2000);
            }
        } else if (offline) {
            Toast.makeText(getApplicationContext(), R.string.connection_established, Toast.LENGTH_SHORT).show();
        }
        offline = false;
        connectHandler.removeCallbacks(connectRunnable);
    }

    private void tryConnect() {
        Call<Void> call = RetrofitService.getHbankApi().connect();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if (response.isSuccessful()) {
                    online();
                } else if (response.code() == 403) {
                    switchToConnectionSettingsActivity();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                offline();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        connectHandler.removeCallbacks(connectRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gone = false;
        paused = false;

        if (!(this instanceof ConnectionSettingsActivity) && !(this instanceof SettingsActivity)) {
            tryConnect();
        }
    }

    private void switchToConnectionSettingsActivity() {
        gone = true;
        Intent i = new Intent(this, ConnectionSettingsActivity.class);
        i.putExtra("hide_back_arrow", true);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        gone = true;
        onBackPressed();
        finish();
        return true;
    }

    public boolean isOffline() {
        return offline;
    }
}

