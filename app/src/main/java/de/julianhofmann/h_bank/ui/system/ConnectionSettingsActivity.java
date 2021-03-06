package de.julianhofmann.h_bank.ui.system;

import androidx.appcompat.app.ActionBar;

import android.annotation.SuppressLint;
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

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectionSettingsActivity extends BaseActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_connection_settings);

        EditText ip = findViewById(R.id.connection_ip_address);
        EditText port = findViewById(R.id.connection_port);
        EditText password = findViewById(R.id.connection_password);
        Spinner protocol = findViewById(R.id.protocol_dropdown);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.protocols, R.layout.support_simple_spinner_dropdown_item);
        protocol.setAdapter(adapter);

        if (RetrofitService.getProtocol() != null) {
            protocol.setSelection(RetrofitService.getProtocol().equals("http") ? 0 : 1);
        }

        ip.setText(RetrofitService.getIpAddress());
        port.setText("" + (RetrofitService.getPort() != -1 ? RetrofitService.getPort() : ""));

        if (RetrofitService.getRetrofit() == null || RetrofitService.getHbankApi() == null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setHomeButtonEnabled(false);
            }
            Button apply = findViewById(R.id.apply_connection_settings);
            apply.setText(R.string.connect);
        } else if (getIntent().getBooleanExtra("hide_back_arrow", false)) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setHomeButtonEnabled(false);
            }
        }

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

        ip.addTextChangedListener(textWatcher);
        port.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);

        checkSubmitButton();
    }

    private void checkSubmitButton() {
        Button submit = findViewById(R.id.apply_connection_settings);
        EditText ip = findViewById(R.id.connection_ip_address);
        EditText port = findViewById(R.id.connection_port);
        EditText password = findViewById(R.id.connection_password);
        submit.setEnabled(ip.getText().toString().trim().length() > 0 && port.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0);
    }

    public void apply(View v) {
        if (!gone) {
            Spinner protocol = findViewById(R.id.protocol_dropdown);
            EditText ip = findViewById(R.id.connection_ip_address);
            EditText port = findViewById(R.id.connection_port);
            TextView error = findViewById(R.id.connection_settings_error_lbl);
            TextView password = findViewById(R.id.connection_password);
            Button  apply = findViewById(R.id.apply_connection_settings);
            error.setText("");
            error.setTextColor(getColor(R.color.red));
            if (ip.getText().toString().trim().length() > 0 && port.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0) {
                if (Integer.parseInt(port.getText().toString()) <= 0) {
                    error.setText(R.string.cannot_reach_server);
                    return;
                }
                String protocolBefore = RetrofitService.getProtocol();
                String ipBefore = RetrofitService.getIpAddress();
                int portBefore = RetrofitService.getPort();
                String serverPasswordBefore = RetrofitService.getServerPassword();
                RetrofitService.changeUrl(protocol.getSelectedItem().toString(), ip.getText().toString().trim(), Integer.parseInt(port.getText().toString().trim()), password.getText().toString().trim());
                Call<Void> call = RetrofitService.getHbankApi().connect();
                Runnable navigateUp = this::onSupportNavigateUp;
                Runnable switchToLoginActivity = this::switchToLoginActivity;
                apply.setEnabled(false);
                apply.setText(R.string.connecting);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            error.setTextColor(getColor(R.color.green));
                            error.setText(R.string.connection_established);
                            RetrofitService.reset();
                            gone = true;
                            new Handler().postDelayed(switchToLoginActivity, 1000);
                        } else if (response.code() == 403) {
                            error.setText(R.string.wrong_password);
                            RetrofitService.changeUrl(protocolBefore, ipBefore, portBefore, serverPasswordBefore);
                        } else if (response.code() == 400) {
                            error.setText(R.string.wrong_protocol);
                            RetrofitService.changeUrl(protocolBefore, ipBefore, portBefore, serverPasswordBefore);
                        }
                        apply.setEnabled(true);
                        apply.setText(RetrofitService.getRetrofit() != null && RetrofitService.getHbankApi() != null ? R.string.apply : R.string.connect);
                    }

                    @Override
                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                        error.setText(R.string.cannot_reach_server);
                        RetrofitService.changeUrl(protocolBefore, ipBefore, portBefore, serverPasswordBefore);
                        apply.setEnabled(true);
                        apply.setText(RetrofitService.getRetrofit() != null && RetrofitService.getHbankApi() != null ? R.string.apply : R.string.connect);
                    }
                });
            } else {
                error.setText(R.string.empty_fields);
            }
        }
    }


    private void switchToLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finishAffinity();
    }
}