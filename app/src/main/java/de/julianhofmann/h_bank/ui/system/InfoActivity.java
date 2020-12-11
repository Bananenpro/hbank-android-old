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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.InfoModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoActivity extends AppCompatActivity {

    private boolean loggedOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Intent i = getIntent();
        loggedOut = i.getBooleanExtra("loggedOut", false);

        loadInfo(null);
    }

    public void loadInfo(View v) {
        Button button = findViewById(R.id.info_refresh_btn);
        TextView version = findViewById(R.id.info_version);
        TextView status = findViewById(R.id.info_status);
        TextView paymentPlans = findViewById(R.id.info_payment_plans);
        TextView backups = findViewById(R.id.info_backups);
        TextView cpu = findViewById(R.id.info_cpu);
        TextView ram = findViewById(R.id.info_ram);
        TextView disk = findViewById(R.id.info_disk);
        TextView temperature = findViewById(R.id.info_temperature);

        version.setText(BuildConfig.VERSION_NAME);

        Call<InfoModel> call = RetrofitService.getHbankApi().getInfo();

        button.setEnabled(false);
        button.setText(R.string.loading);

        status.setText(R.string.connecting);
        status.setTextColor(getColor(R.color.yellow));
        paymentPlans.setText(R.string.dash);
        paymentPlans.setTextColor(getColor(R.color.foreground));
        backups.setText(R.string.dash);
        backups.setTextColor(getColor(R.color.foreground));
        cpu.setText(R.string.dash);
        ram.setText(R.string.dash);
        disk.setText(R.string.dash);
        temperature.setText(R.string.dash);

        call.enqueue(new Callback<InfoModel>() {
            @Override
            public void onResponse(@NotNull Call<InfoModel> call, @NotNull Response<InfoModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    status.setTextColor(getColor(R.color.green));
                    status.setText(R.string.connected);
                    if (response.body().isPaymentPlans()) {
                        paymentPlans.setText(R.string.active);
                        paymentPlans.setTextColor(getColor(R.color.green));
                    } else {
                        paymentPlans.setText(R.string.inactive);
                        paymentPlans.setTextColor(getColor(R.color.red));
                    }
                    if (response.body().isBackups()) {
                        backups.setText(R.string.active);
                        backups.setTextColor(getColor(R.color.green));
                    } else {
                        backups.setText(R.string.inactive);
                        backups.setTextColor(getColor(R.color.red));
                    }

                    cpu.setText(response.body().getCpu());
                    ram.setText(response.body().getRam());
                    disk.setText(response.body().getDisk());
                    temperature.setText(response.body().getTemperature());
                } else {
                    status.setTextColor(getColor(R.color.red));
                    status.setText(R.string.error);
                    paymentPlans.setText(R.string.dash);
                    paymentPlans.setTextColor(getColor(R.color.foreground));
                    backups.setText(R.string.dash);
                    backups.setTextColor(getColor(R.color.foreground));
                    cpu.setText(R.string.dash);
                    ram.setText(R.string.dash);
                    disk.setText(R.string.dash);
                    temperature.setText(R.string.dash);
                }
                button.setEnabled(true);
                button.setText(R.string.update_btn);
            }

            @Override
            public void onFailure(@NotNull Call<InfoModel> call, @NotNull Throwable t) {
                status.setTextColor(getColor(R.color.red));
                status.setText(R.string.not_connected);
                paymentPlans.setText(R.string.dash);
                paymentPlans.setTextColor(getColor(R.color.foreground));
                backups.setText(R.string.dash);
                backups.setTextColor(getColor(R.color.foreground));
                cpu.setText(R.string.dash);
                ram.setText(R.string.dash);
                disk.setText(R.string.dash);
                temperature.setText(R.string.dash);
                button.setEnabled(true);
                button.setText(R.string.update_btn);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!loggedOut)
            inflater.inflate(R.menu.options_menu, menu);
        else
            inflater.inflate(R.menu.login_options_menu, menu);
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
    }

    private void serverInfo() {
        Intent i = new Intent(this, InfoActivity.class);
        i.putExtra("loggedOut", loggedOut);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}