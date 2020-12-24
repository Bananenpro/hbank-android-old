package de.julianhofmann.h_bank.ui.system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.util.UpdateService;

public class ConnectionSettingsActivity extends AppCompatActivity {
    private boolean gone = true;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_settings);
        gone = false;

        EditText ip = findViewById(R.id.connection_ip_address);
        EditText port = findViewById(R.id.connection_port);
        ip.setText(RetrofitService.getIpAddress());
        port.setText(""+RetrofitService.getPort());
    }

    public void apply(View v) {
        if (!gone) {
            EditText ip = findViewById(R.id.connection_ip_address);
            EditText port = findViewById(R.id.connection_port);
            TextView error = findViewById(R.id.connection_settings_error_lbl);
            error.setText("");
            error.setTextColor(getColor(R.color.red));
            if (ip.getText().length() > 0 && port.getText().length() > 0) {
                RetrofitService.changeUrl(ip.getText().toString(), Integer.parseInt(port.getText().toString()));
                error.setTextColor(getColor(R.color.green));
                error.setText(R.string.changes_saved);
                new Handler().postDelayed(this::onSupportNavigateUp, 1000);
            } else {
                error.setText(R.string.empty_fields);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    update();
                    return true;
                case R.id.options_connection_settings:
                    gone = true;
                    connectionSettings();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectionSettings() {
        Intent i = new Intent(this, ConnectionSettingsActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    private void settings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void serverInfo() {
        Intent i = new Intent(this, InfoActivity.class);
        startActivity(i);
    }

    private void logout() {
        String name = RetrofitService.getName();
        RetrofitService.logout();
        switchToLoginActivity(name);
    }

    private void update() {
        if (!gone) {
            gone = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }

            UpdateService.update(this);
            gone = false;
        }
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("name", name);
        i.putExtra("logout", true);
        startActivity(i);
        finishAffinity();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}