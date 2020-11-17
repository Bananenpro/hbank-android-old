package de.julianhofmann.h_bank;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
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

        if (id == -1 || name == null) {
            onSupportNavigateUp();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_create_payment_plan) + " | " + name);
        }


        TextView title = findViewById(R.id.payment_plan_lbl);
        TextView amount = findViewById(R.id.log_item_amount_lbl);
        TextView schedule = findViewById(R.id.log_item_time_lbl);
        TextView next = findViewById(R.id.log_item_user_lbl);
        Button delete = findViewById(R.id.delete_payment_plan);

        Call<PaymentPlanModel> call = RetrofitService.getHbankApi().getPaymentPlan(id, RetrofitService.getAuthorization());
        call.enqueue(new Callback<PaymentPlanModel>() {
            @Override
            public void onResponse(Call<PaymentPlanModel> call, Response<PaymentPlanModel> response) {
                if (response.isSuccessful()) {
                    title.setText(response.body().getDescription());
                    amount.setText(response.body().getAmount() + getString(R.string.currency));
                    schedule.setText(response.body().getSchedule() + " " + getString(R.string.days));
                    next.setText("" + response.body().getDaysLeft() + " " + getString(R.string.days));
                    if (response.body().getAmount().startsWith("-")) {
                        delete.setVisibility(Button.VISIBLE);
                        amount.setTextColor(getColor(R.color.red));
                    }
                } else if (response.code() == 403) {
                    String name = RetrofitService.name;
                    RetrofitService.logout();
                    switchToLoginActivity(name);
                }
            }

            @Override
            public void onFailure(Call<PaymentPlanModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.offline, Toast.LENGTH_LONG).show();
            }
        });
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
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            onSupportNavigateUp();
                        } else if (response.code() == 403) {
                            String name = RetrofitService.name;
                            RetrofitService.logout();
                            switchToLoginActivity(name);
                        }
                        button.setText(R.string.delete);
                        button.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
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
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}