package de.julianhofmann.h_bank;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

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
        Intent i = getIntent();

        String name = i.getStringExtra("name");

        boolean logout = i.getBooleanExtra("logout", false);

        if (name != null) {
            EditText name_edit = findViewById(R.id.login_username);
            name_edit.setText(name);
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
    }

    public void switchToRegisterActivity(View v) {
        Intent i = new Intent(this, RegisterActivity.class);

        EditText name = findViewById(R.id.login_username);
        EditText password = findViewById(R.id.login_password);

        i.putExtra("name", name.getText().toString());
        i.putExtra("password", password.getText().toString());

        startActivity(i);
    }

    private void switchToDashboardActivity() {
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
                        switchToDashboardActivity();
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