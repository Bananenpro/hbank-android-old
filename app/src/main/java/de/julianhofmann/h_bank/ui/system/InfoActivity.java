package de.julianhofmann.h_bank.ui.system;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.InfoModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.util.SettingsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoActivity extends BaseActivity {

    private final Handler refreshInfoHandler = new Handler();
    private Runnable refreshInfoRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_info);

        loadInfo();

        refreshInfoRunnable = new Runnable() {
            @Override
            public void run() {
                refreshInfo();
                refreshInfoHandler.postDelayed(this, SettingsService.getAutoRefreshInterval());
            }
        };
        if (SettingsService.getAutoRefresh()) {
            refreshInfoHandler.postDelayed(refreshInfoRunnable, SettingsService.getAutoRefreshInterval());
        }
    }

    @SuppressLint("SetTextI18n")
    public void loadInfo() {
        if (!gone) {
            TextView version = findViewById(R.id.info_version);
            TextView ipAddress = findViewById(R.id.info_ip_address);
            TextView port = findViewById(R.id.info_port);
            TextView status = findViewById(R.id.info_status);
            TextView paymentPlans = findViewById(R.id.info_payment_plans);
            TextView backups = findViewById(R.id.info_backups);
            TextView cpu = findViewById(R.id.info_cpu);
            TextView ram = findViewById(R.id.info_ram);
            TextView disk = findViewById(R.id.info_disk);
            TextView temperature = findViewById(R.id.info_temperature);

            version.setText(BuildConfig.VERSION_NAME);

            ipAddress.setText(RetrofitService.getIpAddress());
            port.setText(Integer.toString(RetrofitService.getPort()));

            Call<InfoModel> call = RetrofitService.getHbankApi().getInfo();

            gone = true;

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
                    gone = false;
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
                    gone = false;
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void refreshInfo() {
        if (!gone) {
            TextView version = findViewById(R.id.info_version);
            TextView ipAddress = findViewById(R.id.info_ip_address);
            TextView port = findViewById(R.id.info_port);
            TextView status = findViewById(R.id.info_status);
            TextView paymentPlans = findViewById(R.id.info_payment_plans);
            TextView backups = findViewById(R.id.info_backups);
            TextView cpu = findViewById(R.id.info_cpu);
            TextView ram = findViewById(R.id.info_ram);
            TextView disk = findViewById(R.id.info_disk);
            TextView temperature = findViewById(R.id.info_temperature);

            version.setText(BuildConfig.VERSION_NAME);

            ipAddress.setText(RetrofitService.getIpAddress());
            port.setText(Integer.toString(RetrofitService.getPort()));

            Call<InfoModel> call = RetrofitService.getHbankApi().getInfo();
            if (!status.getText().equals(getString(R.string.connected))) {
                status.setText(R.string.connecting);
                status.setTextColor(getColor(R.color.yellow));
            }
            call.enqueue(new Callback<InfoModel>() {
                @Override
                public void onResponse(@NotNull Call<InfoModel> call, @NotNull Response<InfoModel> response) {
                    online();
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
                }

                @Override
                public void onFailure(@NotNull Call<InfoModel> call, @NotNull Throwable t) {
                    offline();
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
                }
            });
        }
    }

    @Override
    protected void offline() {
        refreshInfoHandler.removeCallbacks(refreshInfoRunnable);
        super.offline();
    }

    @Override
    protected void online() {
        if (offline) {
            if (SettingsService.getAutoRefresh()) {
                refreshInfoHandler.postDelayed(refreshInfoRunnable, SettingsService.getAutoRefreshInterval());
            }
        }
        super.online();
    }

    @Override
    protected void onResume() {
        if (paused) {
            if (SettingsService.getAutoRefresh()) {
                refreshInfoHandler.postDelayed(refreshInfoRunnable, SettingsService.getAutoRefreshInterval());
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        refreshInfoHandler.removeCallbacks(refreshInfoRunnable);
        super.onPause();
    }
}