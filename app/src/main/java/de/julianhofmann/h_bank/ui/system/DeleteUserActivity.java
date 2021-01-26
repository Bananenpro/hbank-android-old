package de.julianhofmann.h_bank.ui.system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LoginModel;
import de.julianhofmann.h_bank.api.models.LoginResponseModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.util.SettingsService;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteUserActivity extends AppCompatActivity {

    private boolean gone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);
        gone = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        gone = false;
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
                    gone = true;
                    call.enqueue(new Callback<LoginResponseModel>() {
                        @Override
                        public void onResponse(@NotNull Call<LoginResponseModel> call, @NotNull Response<LoginResponseModel> response) {
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
                                    }

                                    @Override
                                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                                        error.setText(getString(R.string.cannot_reach_server));
                                        button.setEnabled(true);
                                        button.setText(getString(R.string.delete));
                                        gone = false;
                                    }
                                });
                            } else {
                                error.setText(getString(R.string.wrong_password));
                                button.setEnabled(true);
                                button.setText(getString(R.string.delete));
                                gone = false;
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<LoginResponseModel> call, @NotNull Throwable t) {
                            error.setText(getString(R.string.cannot_reach_server));
                            button.setEnabled(true);
                            button.setText(getString(R.string.delete));
                            gone = false;
                        }
                    });

                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.sure).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
        }
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