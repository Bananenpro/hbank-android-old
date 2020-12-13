package de.julianhofmann.h_bank.ui.transaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentPlanActivity extends AppCompatActivity {

    private String name;
    private boolean paused = false;
    private boolean gone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_plan);
        gone = false;

        Intent i = getIntent();

        name = i.getStringExtra("name");
        if (name == null) name = "";

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (!name.equals(""))
                actionBar.setTitle(getString(R.string.title_activity_payment_plan) + " | " + name);
            else
                actionBar.setTitle(getString(R.string.title_activity_my_payment_plans));
        }

        loadPaymentPlans();
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
            }
        }
        return super.onOptionsItemSelected(item);
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

    private void loadPaymentPlans() {
        TextView emptyLbl = findViewById(R.id.no_payment_plans_lbl);
        emptyLbl.setVisibility(View.VISIBLE);
        emptyLbl.setText(R.string.loading);
        LinearLayout layout = findViewById(R.id.payment_plan_list_layout);
        layout.removeAllViews();
        Call<List<PaymentPlanModel>> call = RetrofitService.getHbankApi().getPaymentPlans(name, RetrofitService.getAuthorization());
        call.enqueue(new Callback<List<PaymentPlanModel>>() {
            @Override
            public void onResponse(@NotNull Call<List<PaymentPlanModel>> call, @NotNull Response<List<PaymentPlanModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PaymentPlanModel> paymentPlans = response.body();
                    if (paymentPlans.size() > 0) {
                        emptyLbl.setVisibility(View.GONE);
                    } else {
                        emptyLbl.setVisibility(View.VISIBLE);
                        emptyLbl.setText(R.string.no_payment_plans);
                    }
                    for (PaymentPlanModel p : paymentPlans) {
                        addPaymentPlanListItem(layout, p);
                    }
                } else if (response.code() == 403) {
                    String name = RetrofitService.getName();
                    RetrofitService.logout();
                    switchToLoginActivity(name);
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<PaymentPlanModel>> call, @NotNull Throwable t) {
                emptyLbl.setVisibility(View.VISIBLE);
                emptyLbl.setText(R.string.offline);
            }
        });
    }

    private void addPaymentPlanListItem(LinearLayout layout, PaymentPlanModel p) {
        PaymentPlanListItem item = new PaymentPlanListItem(this);

        item.getDescription().setText(p.getDescription());

        String scheduleUnit = "d";

        switch (p.getScheduleUnit()) {
            case "weeks":
                scheduleUnit = "w";
                break;
            case "months":
                scheduleUnit = "m";
                break;
            case "years":
                scheduleUnit = "a";
                break;
        }

        String schedule = p.getSchedule() + scheduleUnit;
        item.getSchedule().setText(schedule);

        String amount = p.getAmount() + getString(R.string.currency);
        item.getAmount().setText(amount);

        item.getButton().setOnClickListener(v -> showPaymentPlan(p.getId()));

        if (p.getAmount().startsWith("+")) {
            item.getAmount().setTextColor(getColor(R.color.green));
        } else {
            item.getAmount().setTextColor(getColor(R.color.red));
        }

        layout.addView(item);
    }

    public void showPaymentPlan(int id) {
        Intent i;
        if (!name.equals("")) {
            i = new Intent(this, PaymentPlanInfoActivity.class);
            i.putExtra("name", name);
        } else {
            i = new Intent(this, PaymentPlanInfoActivity.class);
        }
        i.putExtra("id", id);
        startActivity(i);
    }

    public void createPaymentPlan(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, CreatePaymentPlanActivity.class);
            if (!name.equals("")) {
                i.putExtra("name", name);
            }
            startActivity(i);
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

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            loadPaymentPlans();
            paused = false;
        }
        gone = false;
    }
}