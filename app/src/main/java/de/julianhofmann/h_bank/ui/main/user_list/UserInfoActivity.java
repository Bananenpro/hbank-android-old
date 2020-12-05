package de.julianhofmann.h_bank.ui.main.user_list;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.UserModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.ui.transaction.PaymentPlanActivity;
import de.julianhofmann.h_bank.ui.transaction.TransferMoneyActivity;
import de.julianhofmann.h_bank.util.BalanceCache;
import de.julianhofmann.h_bank.util.ImageUtils;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends AppCompatActivity {

    private String name;
    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Intent i = getIntent();

        name = i.getStringExtra("name");

        if (name == null) {
            onSupportNavigateUp();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(name);
        }

        loadUserInfo();
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
        switch (item.getItemId()) {
            case R.id.options_settings:
                settings();
                return true;
            case R.id.options_server_info:
                serverInfo();
                return true;
            case R.id.options_logout:
                logout();
                return true;
            case R.id.options_check_for_updates:
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void loadUserInfo() {
        ImageView profilePicture = findViewById(R.id.user_profile_picture);


        Call<UserModel> call = RetrofitService.getHbankApi().getUser(name, RetrofitService.getAuthorization());
        TextView balance = findViewById(R.id.user_balance_lbl);

        String newBalance = getString(R.string.balance) + " " + BalanceCache.getBalance(name) + getString(R.string.currency);
        balance.setText(newBalance);

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NotNull Call<UserModel> call, @NotNull Response<UserModel> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getBalance() != null) {
                        String newBalance = getString(R.string.balance) + " " + response.body().getBalance() + getString(R.string.currency);
                        balance.setText(newBalance);
                        balance.setVisibility(View.VISIBLE);
                        BalanceCache.update(name, response.body().getBalance());
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserModel> call, @NotNull Throwable t) {
                Toast.makeText(UserInfoActivity.this, getString(R.string.offline), Toast.LENGTH_SHORT).show();
            }
        });

        ImageUtils.loadProfilePicture(name, profilePicture, profilePicture.getDrawable(), getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE));
    }

    public void transferMoney(View v) {
        Intent i = new Intent(this, TransferMoneyActivity.class);
        i.putExtra("name", name);
        startActivity(i);
    }

    public void paymentPlans(View v) {
        Intent i = new Intent(this, PaymentPlanActivity.class);
        i.putExtra("name", name);
        startActivity(i);
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("name", name);
        i.putExtra("logout", true);
        startActivity(i);
        finishAffinity();
    }

    private void update() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        UpdateService.update(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            loadUserInfo();
            paused = false;
        }
    }
}