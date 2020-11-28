package de.julianhofmann.h_bank.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentPlanActivity extends AppCompatActivity {

    private String name;
    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_plan);

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

    private void loadPaymentPlans() {
        TextView emptyLbl = findViewById(R.id.no_payment_plans_lbl);
        emptyLbl.setVisibility(View.VISIBLE);
        emptyLbl.setText(R.string.loading);
        Call<List<PaymentPlanModel>> call = RetrofitService.getHbankApi().getPaymentPlans(name, RetrofitService.getAuthorization());
        call.enqueue(new Callback<List<PaymentPlanModel>>() {
            @Override
            public void onResponse(@NotNull Call<List<PaymentPlanModel>> call, @NotNull Response<List<PaymentPlanModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PaymentPlanModel> paymentPlans = response.body();
                    LinearLayout layout = findViewById(R.id.payment_plan_list_layout);
                    if (layout != null) {
                        layout.removeAllViews();
                        if (paymentPlans.size() > 0) {
                            emptyLbl.setVisibility(View.GONE);
                        } else {
                            emptyLbl.setVisibility(View.VISIBLE);
                            emptyLbl.setText(R.string.no_payment_plans);
                        }
                        for (PaymentPlanModel p : paymentPlans) {
                            addPaymentPlanListItem(layout, p);
                        }
                    }
                } else if (response.code() == 403) {
                    String name = RetrofitService.name;
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

        String schedule = p.getSchedule() + "d";
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
        Intent i = new Intent(this, CreatePaymentPlanActivity.class);
        if (!name.equals("")) {
            i.putExtra("name", name);
        }
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
    }
}