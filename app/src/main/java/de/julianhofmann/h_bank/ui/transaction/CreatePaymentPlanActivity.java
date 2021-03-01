package de.julianhofmann.h_bank.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import de.julianhofmann.h_bank.api.models.UserModel;
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
        } else if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_create_payment_plan);
        }

        Spinner receiver = findViewById(R.id.receiver_dropdown);
        TextView receiverLbl = findViewById(R.id.receiver_dropdown_lbl);
        if (name == null) {
            receiver.setVisibility(View.VISIBLE);
            receiverLbl.setVisibility(View.VISIBLE);
            Call<List<UserModel>> call = RetrofitService.getHbankApi().getUsers();
            call.enqueue(new Callback<List<UserModel>>() {
                @Override
                public void onResponse(@NotNull Call<List<UserModel>> call, @NotNull Response<List<UserModel>> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            List<CharSequence> usernames = new ArrayList<>();
                            for (UserModel user : response.body()) {
                                if (!user.getName().equals(RetrofitService.getName())) {
                                    usernames.add(user.getName());
                                }
                            }
                            receiver.setAdapter(new ArrayAdapter<>(CreatePaymentPlanActivity.this, R.layout.support_simple_spinner_dropdown_item, usernames));
                        }
                    } else if (response.code() == 403) {
                        String name = RetrofitService.getName();
                        RetrofitService.logout();
                        switchToLoginActivity(name);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<List<UserModel>> call, @NotNull Throwable t) {
                    receiver.setEnabled(false);
                    Toast.makeText(CreatePaymentPlanActivity.this, getString(R.string.cannot_reach_server), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            receiver.setVisibility(View.GONE);
            receiverLbl.setVisibility(View.GONE);
        }

        Spinner dropdown = findViewById(R.id.create_payment_plan_schedule_unit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.schedule_units, R.layout.support_simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        EditText amount = findViewById(R.id.create_payment_plan_amount);
        amount.addTextChangedListener(textWatcher);
        EditText schedule = findViewById(R.id.create_payment_plan_schedule);
        schedule.addTextChangedListener(textWatcher);

        checkSubmitButton();
    }

    private void checkSubmitButton() {
        Button submit = findViewById(R.id.create_payment_plan_btn);
        EditText amount = findViewById(R.id.create_payment_plan_amount);
        EditText schedule = findViewById(R.id.create_payment_plan_schedule);
        Spinner receiver = findViewById(R.id.receiver_dropdown);
        submit.setEnabled(amount.getText().toString().trim().length() > 0 && schedule.getText().toString().trim().length() > 0 && (name != null || receiver.getSelectedItem() != null));
    }

    public void createPaymentPlan(View v) {
        if (!gone) {
            EditText amount = findViewById(R.id.create_payment_plan_amount);
            EditText schedule = findViewById(R.id.create_payment_plan_schedule);
            EditText description = findViewById(R.id.create_payment_plan_description);
            TextView error = findViewById(R.id.create_payment_plan_error);
            Spinner dropdown = findViewById(R.id.create_payment_plan_schedule_unit);
            error.setTextColor(getColor(R.color.red));
            Button submit = findViewById(R.id.create_payment_plan_btn);
            Spinner receiver = findViewById(R.id.receiver_dropdown);

            if ((name != null || receiver.getSelectedItem() != null) && amount.getText().length() > 0 && schedule.getText().length() > 0) {
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
                            model = new PaymentPlanModel(receiver.getSelectedItem().toString(), amount.getText().toString(), schedule_int, unit, description.getText().toString());

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