package de.julianhofmann.h_bank.ui.auth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.os.CancellationSignal;
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

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.ui.system.ConnectionSettingsActivity;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LoginModel;
import de.julianhofmann.h_bank.api.models.LoginResponseModel;
import de.julianhofmann.h_bank.ui.main.MainActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.util.BalanceCache;
import de.julianhofmann.h_bank.util.PasswordCache;
import de.julianhofmann.h_bank.util.SettingsService;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_login);

        SharedPreferences sp = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);

        RetrofitService.init(sp);

        if (RetrofitService.getRetrofit() == null || RetrofitService.getHbankApi() == null) {
            switchToConnectionSettingsAcitvity();
            return;
        }

        BalanceCache.init(sp);

        if (RetrofitService.isLoggedIn()) {
            switchToMainActivity();
            return;
        }

        Intent i = getIntent();

        String name = i.getStringExtra("name");

        boolean logout = i.getBooleanExtra("logout", false);

        if (name != null) {
            EditText nameEdit = findViewById(R.id.login_username);
            nameEdit.setText(name);
        }

        if (logout) {
            TextView error = findViewById(R.id.login_error_text);
            error.setText(getString(R.string.logout));
        }

        if (Picasso.get() == null) {
            Picasso.Builder builder = new Picasso.Builder(this);
            builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);
        }

        String spName = sp.getString("name", "");
        String spToken = sp.getString("token", "");

        if (!spName.equals("")) {
            SettingsService.init(getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE), spName);
            EditText nameEdit = findViewById(R.id.login_username);
            nameEdit.setText(spName);
        }

        if (!spName.equals("") && !spToken.equals("")) {
            if (SettingsService.getOfflineLogin()) {
                if (SettingsService.getAutoLogin()) {
                    new Handler().postDelayed(() -> autoLogin(spName, spToken), 500);
                } else if (SettingsService.getFingerprintLogin()) {
                    biometricAuthentication(spName, spToken);
                }
            } else {
                RetrofitService.logout();
            }
        }
    }

    private void switchToConnectionSettingsAcitvity() {
        Intent i = new Intent(this, ConnectionSettingsActivity.class);
        startActivity(i);
        finish();
    }

    public void autoLogin(String name, String token) {
        RetrofitService.login(name, token);
        switchToMainActivity();
    }

    public void biometricAuthentication(String name, String token) {
        KeyguardManager keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        PackageManager packageManager = this.getPackageManager();

        if (keyguardManager.isKeyguardSecure() && packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED) {

            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setSubtitle(getString(R.string.authentication_required))
                    .setDescription(getString(R.string.biometric_description))
                    .setNegativeButton(getString(R.string.cancel), this.getMainExecutor(), (dialogInterface, i) -> {
                    })
                    .build();

            biometricPrompt.authenticate(new CancellationSignal(), getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    RetrofitService.login(name, token);
                    switchToMainActivity();
                }
            });
        }
    }

    public void switchToRegisterActivity(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, RegisterActivity.class);

            EditText name = findViewById(R.id.login_username);
            EditText password = findViewById(R.id.login_password);

            i.putExtra("name", name.getText().toString());
            i.putExtra("password", password.getText().toString());

            startActivity(i);
        }
    }

    private void switchToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finishAffinity();
    }

    public void login(View v) {
        if (!gone) {
            Button loginButton = findViewById(R.id.login_btn);
            Button registerButton = findViewById(R.id.switch_to_register_btn);

            EditText name = findViewById(R.id.login_username);
            EditText password = findViewById(R.id.login_password);
            TextView error_text = findViewById(R.id.login_error_text);
            error_text.setText("");

            if (name.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0) {

                SharedPreferences sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
                if (SettingsService.getOfflineLogin()) {
                    String token = PasswordCache.checkPassword(name.getText().toString().trim(), password.getText().toString().trim(), sharedPreferences);
                    if (token != null) {
                        RetrofitService.login(name.getText().toString().trim(), token);
                        switchToMainActivity();
                        return;
                    }
                } else {
                    RetrofitService.logout();
                }

                LoginModel model = new LoginModel(name.getText().toString().trim(), password.getText().toString().trim());

                Call<LoginResponseModel> call = RetrofitService.getHbankApi().login(model);

                loginButton.setText(getString(R.string.loading));
                loginButton.setEnabled(false);
                registerButton.setEnabled(false);
                gone = true;

                call.enqueue(new Callback<LoginResponseModel>() {
                    @Override
                    public void onResponse(@NotNull Call<LoginResponseModel> call, @NotNull Response<LoginResponseModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RetrofitService.login(name.getText().toString().trim(), response.body().getToken().trim());

                            PasswordCache.storePassword(password.getText().toString().trim(), sharedPreferences);

                            switchToMainActivity();
                        } else {
                            error_text.setText(getString(R.string.wrong_credentials));
                        }
                        loginButton.setEnabled(true);
                        registerButton.setEnabled(true);
                        loginButton.setText(getString(R.string.login_btn));
                        gone = false;
                    }

                    @Override
                    public void onFailure(@NotNull Call<LoginResponseModel> call, @NotNull Throwable t) {
                        error_text.setText(getString(R.string.cannot_reach_server));
                        loginButton.setEnabled(true);
                        registerButton.setEnabled(true);
                        loginButton.setText(getString(R.string.login_btn));
                        gone = false;
                    }
                });
            } else {
                error_text.setText(getString(R.string.empty_fields));
            }
        }
    }
}