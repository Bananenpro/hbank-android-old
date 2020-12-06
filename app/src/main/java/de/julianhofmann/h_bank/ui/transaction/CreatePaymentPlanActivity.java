package de.julianhofmann.h_bank.ui.transaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

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

public class CreatePaymentPlanActivity extends AppCompatActivity {

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_payment_plan);

        Intent i = getIntent();
        name = i.getStringExtra("name");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && name != null) {
            actionBar.setTitle(getString(R.string.title_activity_create_payment_plan) + " | " + name);
        } else if (name == null) {
            actionBar.setTitle(R.string.title_activity_create_payment_plan);
        }

        EditText receiver = findViewById(R.id.create_payment_plan_receiver);
        if (name == null) {
            receiver.setVisibility(View.VISIBLE);
        } else {
            receiver.setVisibility(View.GONE);
        }

        Spinner dropdown = findViewById(R.id.schedule_unit_dropdown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.schedule_units, R.layout.support_simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
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

    public void createPaymentPlan(View v) {

        EditText amount = findViewById(R.id.create_payment_plan_amount);
        EditText schedule = findViewById(R.id.create_payment_plan_schedule);
        EditText description = findViewById(R.id.create_payment_plan_description);
        TextView error = findViewById(R.id.create_payment_plan_error);
        EditText receiver = findViewById(R.id.create_payment_plan_receiver);
        Spinner dropdown = findViewById(R.id.schedule_unit_dropdown);
        error.setTextColor(getColor(R.color.red));
        Button submit = findViewById(R.id.create_payment_plan);

        if (receiver.getText().toString().equals(RetrofitService.getName())) {
            error.setTextColor(getColor(R.color.red));
            error.setText(R.string.sender_cannot_be_the_receiver);
            return;
        }

        if ((name != null || receiver.getText().length() > 0) && amount.getText().length() > 0 && schedule.getText().length() > 0) {

            try {
                Double.parseDouble(amount.getText().toString());
                try {
                    int schedule_int = Integer.parseInt(schedule.getText().toString());

                    PaymentPlanModel model;

                    String unit = "days";
                    String text = dropdown.getSelectedItem().toString();
                    if (text.equals(getString(R.string.weeks))) {
                        unit = "weeks";
                    } else if (text.equals(getString(R.string.months))) {
                        unit = "months";
                    } else if (text.equals(getString(R.string.years))) {
                        unit = "years";
                    }

                    if (name != null)
                        model = new PaymentPlanModel(name, amount.getText().toString(), schedule_int, unit, description.getText().toString());
                    else
                        model = new PaymentPlanModel(receiver.getText().toString(), amount.getText().toString(), schedule_int, unit, description.getText().toString());

                    submit.setEnabled(false);
                    submit.setText(R.string.loading);

                    Call<Void> call = RetrofitService.getHbankApi().createPaymentPlan(model, RetrofitService.getAuthorization());
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                error.setTextColor(getColor(R.color.green));
                                error.setText(R.string.create_success);
                                new Handler().postDelayed(() -> onSupportNavigateUp(), 1000);
                            } else if (response.code() == 403) {
                                logout();
                            } else if (response.code() == 400) {
                                error.setTextColor(getColor(R.color.red));
                                error.setText(R.string.user_does_not_exist);
                            }
                            submit.setEnabled(true);
                            submit.setText(R.string.create);
                        }

                        @Override
                        public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                            error.setText(R.string.offline);
                            submit.setEnabled(true);
                            submit.setText(R.string.create);
                        }
                    });

                } catch (NumberFormatException e) {
                    error.setText(R.string.schedule_not_a_number);
                }
            } catch (NumberFormatException e) {
                error.setText(R.string.amount_not_a_number);
            }

        } else {
            error.setText(R.string.empty_fields);
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