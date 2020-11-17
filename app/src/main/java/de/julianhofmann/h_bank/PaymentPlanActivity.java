package de.julianhofmann.h_bank;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentPlanActivity extends AppCompatActivity {

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_plan);

        Intent i = getIntent();

        name = i.getStringExtra("name");

        if(name == null) {
            onSupportNavigateUp();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_payment_plan) + " | " + name);
        }

        Call<List<PaymentPlanModel>> call = RetrofitService.getHbankApi().getPaymentPlans(name, RetrofitService.getAuthorization());
        call.enqueue(new Callback<List<PaymentPlanModel>>() {
            @Override
            public void onResponse(Call<List<PaymentPlanModel>> call, Response<List<PaymentPlanModel>> response) {
                if (response.isSuccessful()) {
                    List<PaymentPlanModel> paymentPlans = response.body();
                    LinearLayout layout = findViewById(R.id.payment_plan_list_layout);
                    for (PaymentPlanModel p : paymentPlans) {
                        addPaymentPlanListItem(layout, p);
                    }
                } else if (response.code() == 403) {
                    String name = RetrofitService.name;
                    RetrofitService.logout();
                    switchToLoginActivity(name);
                }
            }

            @Override
            public void onFailure(Call<List<PaymentPlanModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.offline), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addPaymentPlanListItem(LinearLayout layout, PaymentPlanModel p) {
        if (layout == null) return;
        PaymentPlanListItem item = new PaymentPlanListItem(this);

        item.getDescription().setText(p.getDescription());

        String schedule = p.getSchedule() + "d";
        item.getSchedule().setText(schedule);

        String amount = p.getAmount() + getString(R.string.currency);
        item.getAmount().setText(amount);

        item.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentPlan(p.getId());
            }
        });

        if(p.getAmount().startsWith("+")) {
            item.getAmount().setTextColor(getColor(R.color.green));
        } else {
            item.getAmount().setTextColor(getColor(R.color.red));
        }

        layout.addView(item);
    }

    public void showPaymentPlan(int id) {
        Intent i = new Intent(this, PaymentPlanInfoActivity.class);
        i.putExtra("name", name);
        i.putExtra("id", id);
        startActivity(i);
    }

    public void createPaymentPlan(View v) {
        Intent i = new Intent(this, CreatePaymentPlanActivity.class);
        i.putExtra("name", name);
        startActivity(i);
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