package de.julianhofmann.h_bank.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.PaymentPlanModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentPlanActivity extends BaseActivity {

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_payment_plan);

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
        LinearLayout layout = findViewById(R.id.payment_plan_list_layout);
        layout.removeAllViews();
        Call<List<PaymentPlanModel>> call = RetrofitService.getHbankApi().getPaymentPlans(name, RetrofitService.getAuthorization());
        call.enqueue(new Callback<List<PaymentPlanModel>>() {
            @Override
            public void onResponse(@NotNull Call<List<PaymentPlanModel>> call, @NotNull Response<List<PaymentPlanModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    online();
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
                offline();
                emptyLbl.setVisibility(View.VISIBLE);
                emptyLbl.setText(R.string.cannot_reach_server);
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

        item.getButton().setOnClickListener(v -> {
            if (!gone) {
                gone = true;
                showPaymentPlan(p.getId());
            }
        });

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

    @Override
    protected void onResume() {
        if (paused) {
            loadPaymentPlans();
            paused = false;
        }
        gone = false;
        super.onResume();
    }
}