package de.julianhofmann.h_bank.ui.main.log;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LogModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.ui.transaction.PaymentPlanInfoActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogItemInfoActivity extends BaseActivity {

    private int paymentPlanId = -1;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_log_item_info);

        Intent i = getIntent();

        id = i.getIntExtra("id", -1);

        if (id == -1) {
            onSupportNavigateUp();
            return;
        }

        loadLogItem();
    }

    private void loadLogItem() {
        TextView title = findViewById(R.id.log_item_lbl);
        TextView amount = findViewById(R.id.log_item_amount_lbl);
        TextView newBalance = findViewById(R.id.log_item_new_balance_lbl);
        TextView date = findViewById(R.id.log_item_date_lbl);
        TextView time = findViewById(R.id.log_item_time_lbl);
        TextView user = findViewById(R.id.log_item_next_lbl);
        TextView userLbl = findViewById(R.id.log_item_next_lbl_lbl);

        Button gotoPaymentPlan = findViewById(R.id.goto_payment_plan_btn);

        Call<LogModel> call = RetrofitService.getHbankApi().getLogItem(id, RetrofitService.getAuthorization());
        call.enqueue(new Callback<LogModel>() {
            @Override
            public void onResponse(@NotNull Call<LogModel> call, @NotNull Response<LogModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    online();

                    if (title != null)
                        title.setText(response.body().getDescription());

                    if (amount != null)
                        amount.setText(String.format("%s%s", response.body().getAmount(), getString(R.string.currency)));

                    if (response.body().getAmount().startsWith("-") && amount != null) {
                        amount.setTextColor(getColor(R.color.red));
                        userLbl.setText(R.string.receiver_lbl);
                    }

                    if (newBalance != null)
                        newBalance.setText(String.format("%s%s", response.body().getNewBalance(), getString(R.string.currency)));
                    if (time != null && date != null) {
                        String[] datetime = response.body().getDate().split(" - ");
                        date.setText(datetime[0]);
                        time.setText(datetime[1]);
                    }
                    if (user != null)
                        user.setText(response.body().getUsername());

                    gotoPaymentPlan.setVisibility(response.body().isPaymentPlan() ? Button.VISIBLE : Button.GONE);
                    paymentPlanId = response.body().getPaymentPlanId();
                } else if (response.code() == 403) {
                    logout();
                } else {
                    Log.e("ERROR", id + "");
                }
            }

            @Override
            public void onFailure(@NotNull Call<LogModel> call, @NotNull Throwable t) {
                offline();
            }
        });
    }

    public void gotoPaymentPlan(View v) {
        if (paymentPlanId != -1) {
            Intent i = new Intent(this, PaymentPlanInfoActivity.class);
            i.putExtra("id", paymentPlanId);
            startActivity(i);
        }
    }

    @Override
    protected void online() {
        if (offline) {
            loadLogItem();
        }
        super.online();
    }
}