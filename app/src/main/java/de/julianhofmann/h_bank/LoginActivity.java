package de.julianhofmann.h_bank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LoginModel;
import de.julianhofmann.h_bank.api.models.LoginResponseModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sp = getSharedPreferences("de.julianhofmann.h-bank", MODE_PRIVATE);

        RetrofitService.init(sp);
        BalanceCache.init(sp);

        if (RetrofitService.name != null && RetrofitService.token != null) {
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
            EditText nameEdit = findViewById(R.id.login_username);
            nameEdit.setText(spName);
        }

        if (!spName.equals("") && !spToken.equals("")) {
            biometricAuthentication(spName, spToken);
        }
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
                    .setNegativeButton(getString(R.string.cancel), this.getMainExecutor(), (dialogInterface, i) -> { })
                    .build();

            biometricPrompt.authenticate(new CancellationSignal(), getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    super.onAuthenticationHelp(helpCode, helpString);
                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    RetrofitService.name = name;
                    RetrofitService.token = token;
                    switchToMainActivity();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
            });
        }
    }

    public void switchToRegisterActivity(View v) {
        Intent i = new Intent(this, RegisterActivity.class);

        EditText name = findViewById(R.id.login_username);
        EditText password = findViewById(R.id.login_password);

        i.putExtra("name", name.getText().toString());
        i.putExtra("password", password.getText().toString());

        startActivity(i);
    }

    private void switchToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        finish();
        startActivity(i);
    }

    public void login(View v) {

        Button loginButton = findViewById(R.id.login_btn);
        Button registerButton = findViewById(R.id.switch_to_register_btn);

        EditText name = findViewById(R.id.login_username);
        EditText password = findViewById(R.id.login_password);
        TextView error_text = findViewById(R.id.login_error_text);
        error_text.setText("");

        if (name.getText().length() > 0 && password.getText().length() > 0) {
            LoginModel model = new LoginModel(name.getText().toString(), password.getText().toString());

            Call<LoginResponseModel> call = RetrofitService.getHbankApi().login(model);

            loginButton.setText(getString(R.string.loading));
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);

            call.enqueue(new Callback<LoginResponseModel>() {
                @Override
                public void onResponse(Call<LoginResponseModel> call, Response<LoginResponseModel> response) {
                    if (response.isSuccessful()) {
                        RetrofitService.token = response.body().getToken();
                        RetrofitService.name = name.getText().toString();


                        SharedPreferences sharedPreferences = getSharedPreferences("de.julianhofmann.h-bank", MODE_PRIVATE);

                        SharedPreferences.Editor edit = sharedPreferences.edit();

                        edit.putString("name", RetrofitService.name);
                        edit.putString("token", RetrofitService.token);

                        edit.apply();

                        switchToMainActivity();
                    } else {
                        error_text.setText(getString(R.string.wrong_credentials));
                    }
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                    loginButton.setText(getString(R.string.login_btn));
                }

                @Override
                public void onFailure(Call<LoginResponseModel> call, Throwable t) {
                    error_text.setText(getString(R.string.offline));
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                    loginButton.setText(getString(R.string.login_btn));
                }
            });
        } else {
            error_text.setText(getString(R.string.empty_fields));
        }
    }
}