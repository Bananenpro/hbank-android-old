package de.julianhofmann.h_bank;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.lang.annotation.Annotation;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.RegisterModel;
import de.julianhofmann.h_bank.api.models.RegisterResponseModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

    private void switchToLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        EditText name = findViewById(R.id.register_username);
        i.putExtra("name", name.getText().toString());
        startActivity(i);
    }

    public void register(View view) {

        Button registerButton = findViewById(R.id.register_btn);

        EditText name = findViewById(R.id.register_username);
        EditText password = findViewById(R.id.register_password);
        EditText repeatPassword = findViewById(R.id.register_password_repeat);
        SwitchCompat switchCompat = findViewById(R.id.is_parent_switch);
        TextView error_text = findViewById(R.id.register_error_text);
        error_text.setText("");

        if(name.getText().length() > 0 && password.getText().length() > 0) {
            if (password.getText().toString().equals(repeatPassword.getText().toString())) {
                RegisterModel model = new RegisterModel(name.getText().toString(), password.getText().toString(), switchCompat.isChecked());

                Call<RegisterResponseModel> call = RetrofitService.getHbankApi().register(model);

                registerButton.setText(getString(R.string.loading));
                registerButton.setEnabled(false);

                call.enqueue(new Callback<RegisterResponseModel>() {
                    @Override
                    public void onResponse(Call<RegisterResponseModel> call, Response<RegisterResponseModel> response) {
                        if (response.isSuccessful()) {
                            switchToLoginActivity();
                        } else if (response.code() == 500) {

                            try {
                                Converter<ResponseBody, RegisterResponseModel> converter =  RetrofitService.getRetrofit().responseBodyConverter(RegisterResponseModel.class, new Annotation[0]);
                                RegisterResponseModel body = converter.convert(response.errorBody());
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
                            } catch (IOException e) {
                                Log.e("ERROR", e.getMessage());
                            }

                        }
                        registerButton.setEnabled(true);
                        registerButton.setText(getString(R.string.register_btn));
                    }

                    @Override
                    public void onFailure(Call<RegisterResponseModel> call, Throwable t) {
                        TextView text = findViewById(R.id.register_error_text);
                        text.setText(getString(R.string.offline));

                        registerButton.setEnabled(true);
                        registerButton.setText(getString(R.string.register_btn));
                    }
                });
            } else {
                error_text.setText(getString(R.string.passwords_dont_match));
            }
        } else {
            error_text.setText(getString(R.string.empty_fields));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}