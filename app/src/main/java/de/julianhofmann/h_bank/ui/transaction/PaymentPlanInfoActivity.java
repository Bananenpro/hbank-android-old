package de.julianhofmann.h_bank.ui.transaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.util.BalanceCache;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentPlanInfoActivity extends AppCompatActivity {

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_plan_info);

        Intent i = getIntent();

        String name = i.getStringExtra("name");
        id = i.getIntExtra("id", -1);

        if (id == -1) {
            onSupportNavigateUp();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && name != null) {
            actionBar.setTitle(getString(R.string.title_activity_create_payment_plan) + " | " + name);
        } else if (name == null) {
            actionBar.setTitle(R.string.title_activity_create_payment_plan);
        }


        TextView title = findViewById(R.id.payment_plan_lbl);
        TextView amount = findViewById(R.id.log_item_amount_lbl);
        TextView schedule = findViewById(R.id.log_item_time_lbl);
        TextView next = findViewById(R.id.log_item_next_lbl);
        TextView user = findViewById(R.id.log_item_user_lbl);
        TextView userLbl = findViewById(R.id.log_item_user_lbl_lbl);
        Button delete = findViewById(R.id.delete_payment_plan);

        Call<PaymentPlanModel> call = RetrofitService.getHbankApi().getPaymentPlan(id, RetrofitService.getAuthorization());
        call.enqueue(new Callback<PaymentPlanModel>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(@NotNull Call<PaymentPlanModel> call, @NotNull Response<PaymentPlanModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (title != null)
                        title.setText(response.body().getDescription());

                    double balance = Double.parseDouble(BalanceCache.getBalance(RetrofitService.getName()));
                    int missingPayments = (int) Math.floor((response.body().getSchedule() - (double) response.body().getLeft()) / (double) response.body().getSchedule());

                    if (missingPayments > 0 && balance < Math.abs(Double.parseDouble(response.body().getAmount())) * missingPayments) {
                        delete.setText(R.string.no_money);
                        delete.setEnabled(false);
                    } else {
                        delete.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                    }

                    String scheduleUnit = getString(R.string.days);

                    switch (response.body().getScheduleUnit()) {
                        case "weeks":
                            scheduleUnit = getString(R.string.weeks);
                            break;
                        case "months":
                            scheduleUnit = getString(R.string.months);
                            break;
                        case "years":
                            scheduleUnit = getString(R.string.years);
                            break;
                    }

                    if (amount != null)
                        amount.setText(String.format("%s%s", response.body().getAmount(), getString(R.string.currency)));
                    if (schedule != null)
                        schedule.setText(String.format("%d %s", response.body().getSchedule(), scheduleUnit));
                    if (next != null) {
                        next.setText(String.format("%d %s", response.body().getLeft(), scheduleUnit));
                        if (response.body().getLeft() <= 0) {
                            next.setTextColor(getColor(R.color.red));
                        }
                    }
                    if (user != null && userLbl != null) {
                        if (response.body().getAmount().startsWith("-")) {
                            userLbl.setText(R.string.receiver_lbl);
                        } else {
                            userLbl.setText(R.string.sender_lbl);
                        }
                        user.setText(response.body().getUser());
                    }
                    if (response.body().getAmount().startsWith("-")) {
                        delete.setVisibility(Button.VISIBLE);
                        if (amount != null)
                            amount.setTextColor(getColor(R.color.red));
                    }
                } else if (response.code() == 403) {
                    logout();
                }
            }

            @Override
            public void onFailure(@NotNull Call<PaymentPlanModel> call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.offline, Toast.LENGTH_LONG).show();
            }
        });
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        UpdateService.update(this);
    }

    public void deletePaymentPlan(View v) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == dialog.BUTTON_POSITIVE) {
                Call<Void> call = RetrofitService.getHbankApi().deletePaymentPlan(id, RetrofitService.getAuthorization());
                Button button = (Button) v;
                button.setEnabled(false);
                button.setText(R.string.loading);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            onSupportNavigateUp();
                        } else if (response.code() == 403) {
                            String name = RetrofitService.getName();
                            RetrofitService.logout();
                            switchToLoginActivity(name);
                        }
                        button.setText(R.string.delete);
                        button.setEnabled(true);
                    }

                    @Override
                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                        Toast.makeText(getApplicationContext(), R.string.offline, Toast.LENGTH_LONG).show();
                        button.setText(R.string.delete);
                        button.setEnabled(true);
                    }
                });
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
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