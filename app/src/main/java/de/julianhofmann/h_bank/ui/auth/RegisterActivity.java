package de.julianhofmann.h_bank.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;

import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.RegisterModel;
import de.julianhofmann.h_bank.api.models.RegisterResponseModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_register);

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

        @SuppressLint("CutPasteId")
        EditText username = findViewById(R.id.register_username);
        username.addTextChangedListener(textWatcher);
        @SuppressLint("CutPasteId")
        EditText passwordEditText = findViewById(R.id.register_password);
        passwordEditText.addTextChangedListener(textWatcher);
        EditText passwordRepeat = findViewById(R.id.register_password_repeat);
        passwordRepeat.addTextChangedListener(textWatcher);

        EditText parentPassword = findViewById(R.id.register_parent_password);
        parentPassword.addTextChangedListener(textWatcher);

        SwitchCompat isParent = findViewById(R.id.is_parent_switch);
        isParent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            parentPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            checkSubmitButton();
        });

        checkSubmitButton();
    }

    private void checkSubmitButton() {
        Button submit = findViewById(R.id.register_btn);
        EditText username = findViewById(R.id.register_username);
        EditText password = findViewById(R.id.register_password);
        EditText passwordRepeat = findViewById(R.id.register_password_repeat);
        EditText parentPassword = findViewById(R.id.register_parent_password);
        SwitchCompat isParent = findViewById(R.id.is_parent_switch);
        submit.setEnabled(username.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0 && passwordRepeat.getText().toString().equals(password.getText().toString())
            && (!isParent.isChecked() || parentPassword.getText().length() > 0));
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
            EditText parentPassword = findViewById(R.id.register_parent_password);
            TextView errorText = findViewById(R.id.register_error_text);
            errorText.setText("");

            if (name.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0 && (!switchCompat.isChecked() || parentPassword.getText().length() > 0)) {
                if (password.getText().toString().equals(repeatPassword.getText().toString())) {
                    RegisterModel model = new RegisterModel(name.getText().toString().trim(), password.getText().toString().trim(), switchCompat.isChecked(), parentPassword.getText().toString());

                    Call<RegisterResponseModel> call = RetrofitService.getHbankApi().register(model);

                    registerButton.setText(getString(R.string.loading));
                    registerButton.setEnabled(false);
                    gone = true;

                    call.enqueue(new Callback<RegisterResponseModel>() {
                        @Override
                        public void onResponse(@NotNull Call<RegisterResponseModel> call, @NotNull Response<RegisterResponseModel> response) {
                            if (response.isSuccessful()) {
                                online();
                                RetrofitService.logout();
                                switchToLoginActivity();
                            } else if (response.code() == 500 && response.errorBody() != null) {
                                online();
                                try {
                                    Converter<ResponseBody, RegisterResponseModel> converter = RetrofitService.getRetrofit().responseBodyConverter(RegisterResponseModel.class, new Annotation[0]);
                                    RegisterResponseModel body = converter.convert(response.errorBody());
                                    if (body != null) {
                                        if (!body.getNameLength()) {
                                            String newText = errorText.getText().toString() + "\n" + getString(R.string.name_too_short);
                                            errorText.setText(newText);
                                        }
                                        if (!body.getPasswordLength()) {
                                            String newText = errorText.getText().toString() + "\n" + getString(R.string.password_length_error_part_1) + " " + body.getRequiredPasswordLength() + " " + getString(R.string.password_length_error_part_2);
                                            errorText.setText(newText);
                                        }
                                        if (body.getAlreadyExists()) {
                                            String newText = errorText.getText().toString() + "\n" + getString(R.string.user_already_exists);
                                            errorText.setText(newText);
                                        }
                                        if (body.getWrongParentPassword()) {
                                            String newText = errorText.getText().toString() + "\n" + getString(R.string.wrong_parent_password);
                                            errorText.setText(newText);
                                        }
                                    }
                                } catch (IOException e) {
                                    Log.e("ERROR", e.getMessage());
                                }
                                gone = false;
                            } else {
                                gone = false;
                            }
                            registerButton.setEnabled(true);
                            registerButton.setText(getString(R.string.register_btn));
                        }

                        @Override
                        public void onFailure(@NotNull Call<RegisterResponseModel> call, @NotNull Throwable t) {
                            offline();
                            TextView text = findViewById(R.id.register_error_text);
                            text.setText(getString(R.string.cannot_reach_server));

                            registerButton.setEnabled(true);
                            registerButton.setText(getString(R.string.register_btn));
                            gone = false;
                        }
                    });
                } else {
                    errorText.setText(getString(R.string.passwords_dont_match));
                }
            } else {
                errorText.setText(getString(R.string.empty_fields));
            }
        }
    }
}