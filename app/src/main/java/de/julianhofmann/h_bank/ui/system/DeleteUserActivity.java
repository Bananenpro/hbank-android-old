package de.julianhofmann.h_bank.ui.system;

import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LoginModel;
import de.julianhofmann.h_bank.api.models.LoginResponseModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.util.SettingsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteUserActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_delete_user);

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

        EditText password = findViewById(R.id.delete_password);
        password.addTextChangedListener(textWatcher);
        checkSubmitButton();
    }

    private void checkSubmitButton() {
        Button submit = findViewById(R.id.delete_account_btn);
        EditText password = findViewById(R.id.delete_password);
        submit.setEnabled(password.getText().toString().trim().length() > 0);
        submit.setBackgroundTintList(submit.isEnabled() ? ColorStateList.valueOf(getColor(R.color.red)) : ColorStateList.valueOf(getColor(R.color.dark_gray)));
    }

    public void deleteUser(View v) {
        if (!gone) {
            Button button = (Button) v;

            TextView error = findViewById(R.id.delete_user_error);
            error.setText("");

            EditText password = findViewById(R.id.delete_password);
            if (password.getText().toString().trim().length() == 0) {
                error.setText(R.string.empty_fields);
                return;
            }

            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                if (which == dialog.BUTTON_POSITIVE) {

                    Call<LoginResponseModel> call = RetrofitService.getHbankApi().login(new LoginModel(RetrofitService.getName(), password.getText().toString().trim()));
                    button.setEnabled(false);
                    button.setText(R.string.loading);
                    button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.dark_gray)));
                    gone = true;
                    call.enqueue(new Callback<LoginResponseModel>() {
                        @Override
                        public void onResponse(@NotNull Call<LoginResponseModel> call, @NotNull Response<LoginResponseModel> response) {
                            online();
                            if (response.isSuccessful() && response.body() != null) {
                                RetrofitService.login(RetrofitService.getName(), response.body().getToken());
                                Call<Void> call2 = RetrofitService.getHbankApi().delete(RetrofitService.getName(), RetrofitService.getAuthorization());
                                call2.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            SettingsService.deleteSettings(RetrofitService.getName());
                                            RetrofitService.logout();
                                            switchToLoginActivity("");
                                        } else if (response.code() == 403) {
                                            logout();
                                        }
                                        button.setEnabled(true);
                                        button.setText(R.string.delete);
                                        gone = false;
                                        button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                                    }

                                    @Override
                                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                                        error.setText(getString(R.string.cannot_reach_server));
                                        button.setEnabled(true);
                                        button.setText(getString(R.string.delete));
                                        gone = false;
                                        button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                                    }
                                });
                            } else {
                                error.setText(getString(R.string.wrong_password));
                                button.setEnabled(true);
                                button.setText(getString(R.string.delete));
                                gone = false;
                                button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<LoginResponseModel> call, @NotNull Throwable t) {
                            offline();
                            error.setText(getString(R.string.cannot_reach_server));
                            button.setEnabled(true);
                            button.setText(getString(R.string.delete));
                            gone = false;
                            button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
                        }
                    });

                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
        }
    }
}