package de.julianhofmann.h_bank.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePaymentPlanActivity extends BaseActivity {

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_create_payment_plan);

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

    public void createPaymentPlan(View v) {
        if (!gone) {
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
                        gone = true;

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
                                gone = false;
                            }

                            @Override
                            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                                error.setText(R.string.cannot_reach_server);
                                submit.setEnabled(true);
                                submit.setText(R.string.create);
                                gone = false;
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
    }
}