package de.julianhofmann.h_bank.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.TransferMoneyModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferMoneyActivity extends BaseActivity {

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_transfer_money);


        Intent i = getIntent();

        name = i.getStringExtra("name");

        if (name == null) {
            onSupportNavigateUp();
            return;
        }

        TextView title = findViewById(R.id.transfer_money_lbl);
        title.setText(name);

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

        EditText amount = findViewById(R.id.transfer_money_amount);
        amount.addTextChangedListener(textWatcher);

        checkSubmitButton();
    }

    private void checkSubmitButton() {
        Button submit = findViewById(R.id.transfer_money_submit_btn);
        EditText amount = findViewById(R.id.transfer_money_amount);
        submit.setEnabled(amount.getText().toString().trim().length() > 0);
    }

    public void transferMoney(View v) {
        if (!gone) {
            EditText amount = findViewById(R.id.transfer_money_amount);
            EditText description = findViewById(R.id.create_payment_plan_description);
            TextView error = findViewById(R.id.transfer_money_error);
            error.setTextColor(getColor(R.color.red));
            Button submit = findViewById(R.id.transfer_money_submit_btn);

            if (amount.getText().length() > 0) {
                try {
                    Double.parseDouble(amount.getText().toString());

                    TransferMoneyModel model = new TransferMoneyModel(name, amount.getText().toString(), description.getText().toString());
                    Call<Void> call = RetrofitService.getHbankApi().transferMoney(model, RetrofitService.getAuthorization());
                    submit.setEnabled(false);
                    submit.setText(R.string.loading);
                    gone = true;
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                online();
                                error.setText(R.string.transaction_complete);
                                error.setTextColor(getColor(R.color.green));
                                new Handler().postDelayed(() -> onSupportNavigateUp(), 1000);
                            } else if (response.code() == 400) {
                                online();
                                error.setText(R.string.not_enough_money);
                            } else if (response.code() == 403) {
                                logout();
                            }
                            submit.setEnabled(true);
                            submit.setText(R.string.transfer_money_btn);
                            gone = false;
                        }

                        @Override
                        public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                            offline();
                            error.setText(R.string.cannot_reach_server);
                            submit.setEnabled(true);
                            submit.setText(R.string.transfer_money_btn);
                            gone = false;
                        }
                    });

                } catch (NumberFormatException e) {
                    error.setText(getString(R.string.amount_not_a_number));
                }
            } else {
                error.setText(getString(R.string.empty_amount_field));
            }
        }
    }
}