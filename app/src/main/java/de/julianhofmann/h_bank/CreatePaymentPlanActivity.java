package de.julianhofmann.h_bank;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
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
        if (name == null) {
            onSupportNavigateUp();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_create_payment_plan) + " | " + name);
        }
    }

    public void createPaymentPlan(View v) {

        EditText amount = findViewById(R.id.create_payment_plan_amount);
        EditText schedule = findViewById(R.id.create_payment_plan_schedule);
        EditText description = findViewById(R.id.create_payment_plan_description);
        TextView error = findViewById(R.id.create_payment_plan_error);
        error.setTextColor(getColor(R.color.red));
        Button submit = findViewById(R.id.create_payment_plan);

        if (amount.getText().length() > 0 && schedule.getText().length() > 0) {

            try {
                Double.parseDouble(amount.getText().toString());
                try {
                    int schedule_int = Integer.parseInt(schedule.getText().toString());

                    PaymentPlanModel model = new PaymentPlanModel(name, amount.getText().toString(), schedule_int, description.getText().toString());

                    submit.setEnabled(false);
                    submit.setText(R.string.loading);

                    Call<Void> call = RetrofitService.getHbankApi().createPaymentPlan(model, RetrofitService.getAuthorization());
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                error.setTextColor(getColor(R.color.green));
                                error.setText(R.string.create_success);
                                new Handler().postDelayed(() -> onSupportNavigateUp(), 1000);
                            } else if (response.code() == 403) {
                                String name = RetrofitService.name;
                                RetrofitService.logout();
                                switchToLoginActivity(name);
                            }
                            submit.setEnabled(true);
                            submit.setText(R.string.create);
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            error.setText(R.string.offline);
                            submit.setEnabled(true);
                            submit.setText(R.string.create);
                        }
                    });

                } catch (NumberFormatException e) {
                    error.setText(R.string.schedule_not_a_number);
                }
            }catch(NumberFormatException e) {
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
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}