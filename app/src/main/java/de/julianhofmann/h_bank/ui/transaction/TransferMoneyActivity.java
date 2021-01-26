package de.julianhofmann.h_bank.ui.transaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.TransferMoneyModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferMoneyActivity extends AppCompatActivity {

    private String name;
    private boolean gone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money);
        gone = false;


        Intent i = getIntent();

        name = i.getStringExtra("name");

        if (name == null) {
            onSupportNavigateUp();
            return;
        }

        TextView title = findViewById(R.id.transfer_money_lbl);
        title.setText(name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gone = false;
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
        if (!gone) {
            switch (item.getItemId()) {
                case R.id.options_settings:
                    gone = true;
                    settings();
                    return true;
                case R.id.options_server_info:
                    gone = true;
                    serverInfo();
                    return true;
                case R.id.options_logout:
                    gone = true;
                    logout();
                    return true;
                case R.id.options_check_for_updates:
                    update();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
        if (!gone) {
            gone = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }

            UpdateService.update(this);
            gone = false;
        }
    }

    public void transferMoney(View v) {
        if (!gone) {
            EditText amount = findViewById(R.id.create_payment_plan_amount);
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
                                error.setText(R.string.transaction_complete);
                                error.setTextColor(getColor(R.color.green));
                                new Handler().postDelayed(() -> onSupportNavigateUp(), 1000);
                            } else if (response.code() == 400) {
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