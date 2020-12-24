package de.julianhofmann.h_bank.ui.auth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;

import de.julianhofmann.h_bank.ui.system.ConnectionSettingsActivity;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.RegisterModel;
import de.julianhofmann.h_bank.api.models.RegisterResponseModel;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.util.UpdateService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private boolean gone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        gone = false;

        Intent i = getIntent();

        String name = i.getStringExtra("name");
        String password = i.getStringExtra("password");

        if (name != null) {
            EditText name_edit = findViewById(R.id.register_username);
            name_edit.setText(name);
        }

        if (password != null) {
            EditText password_edit = findViewById(R.id.register_password);
            password_edit.setText(password);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gone = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_options_menu, menu);
        return true;
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!gone) {
            switch (item.getItemId()) {
                case R.id.options_server_info:
                    gone = true;
                    serverInfo();
                    return true;
                case R.id.options_check_for_updates:
                    update();
                    return true;
                case R.id.options_connection_settings:
                    gone = true;
                    connectionSettings();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectionSettings() {
        Intent i = new Intent(this, ConnectionSettingsActivity.class);
        startActivity(i);
    }

    private void serverInfo() {
        Intent i = new Intent(this, InfoActivity.class);
        startActivity(i);
    }

    private void update() {
        if (!gone) {
            gone = true;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }

            UpdateService.update(this, false);
            gone = false;
        }
    }

    private void switchToLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        EditText name = findViewById(R.id.register_username);
        i.putExtra("name", name.getText().toString());
        startActivity(i);
        finishAffinity();
    }

    public void register(View view) {
        if (!gone) {
            Button registerButton = findViewById(R.id.register_btn);

            EditText name = findViewById(R.id.register_username);
            EditText password = findViewById(R.id.register_password);
            EditText repeatPassword = findViewById(R.id.register_password_repeat);
            SwitchCompat switchCompat = findViewById(R.id.is_parent_switch);
            TextView error_text = findViewById(R.id.register_error_text);
            error_text.setText("");

            if (name.getText().length() > 0 && password.getText().length() > 0) {
                if (password.getText().toString().equals(repeatPassword.getText().toString())) {
                    RegisterModel model = new RegisterModel(name.getText().toString(), password.getText().toString(), switchCompat.isChecked());

                    Call<RegisterResponseModel> call = RetrofitService.getHbankApi().register(model);

                    registerButton.setText(getString(R.string.loading));
                    registerButton.setEnabled(false);
                    gone = true;

                    call.enqueue(new Callback<RegisterResponseModel>() {
                        @Override
                        public void onResponse(@NotNull Call<RegisterResponseModel> call, @NotNull Response<RegisterResponseModel> response) {
                            if (response.isSuccessful()) {
                                RetrofitService.logout();
                                switchToLoginActivity();
                            } else if (response.code() == 500 && response.errorBody() != null) {
                                try {
                                    Converter<ResponseBody, RegisterResponseModel> converter = RetrofitService.getRetrofit().responseBodyConverter(RegisterResponseModel.class, new Annotation[0]);
                                    RegisterResponseModel body = converter.convert(response.errorBody());
                                    if (body != null) {
                                        if (!body.getNameLength()) {
                                            String newText = error_text.getText().toString() + "\n" + getString(R.string.name_too_short);
                                            error_text.setText(newText);
                                        }
                                        if (!body.getPasswordLength()) {
                                            String newText = error_text.getText().toString() + "\n" + getString(R.string.password_length_error_part_1) + " " + body.getRequiredPasswordLength() + " " + getString(R.string.password_length_error_part_2);
                                            error_text.setText(newText);
                                        }
                                        if (body.getAlreadyExists()) {
                                            String newText = error_text.getText().toString() + "\n" + getString(R.string.user_already_exists);
                                            error_text.setText(newText);
                                        }
                                    }
                                } catch (IOException e) {
                                    Log.e("ERROR", e.getMessage());
                                }

                            }
                            registerButton.setEnabled(true);
                            registerButton.setText(getString(R.string.register_btn));
                            gone = false;
                        }

                        @Override
                        public void onFailure(@NotNull Call<RegisterResponseModel> call, @NotNull Throwable t) {
                            TextView text = findViewById(R.id.register_error_text);
                            text.setText(getString(R.string.offline));

                            registerButton.setEnabled(true);
                            registerButton.setText(getString(R.string.register_btn));
                            gone = false;
                        }
                    });
                } else {
                    error_text.setText(getString(R.string.passwords_dont_match));
                }
            } else {
                error_text.setText(getString(R.string.empty_fields));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}