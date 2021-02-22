package de.julianhofmann.h_bank.ui.main.user_list;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

    private final Handler refreshBalanceHandler = new Handler();
    private Runnable refreshBalanceRunnable;
    private String name;
    private boolean paused = false;
    private boolean gone = true;
    private boolean balanceAccess = true;
    private boolean offlineToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        gone = false;

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
        refreshBalanceRunnable = new Runnable() {
            @Override
            public void run() {
                refreshBalance();
                refreshBalanceHandler.postDelayed(this, 2000);
            }
        };
        refreshBalanceHandler.postDelayed(refreshBalanceRunnable, 2000);
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

    public void loadUserInfo() {
        ImageView profilePicture = findViewById(R.id.user_profile_picture);


        Call<UserModel> call = RetrofitService.getHbankApi().getUser(name, RetrofitService.getAuthorization());
        TextView balance = findViewById(R.id.user_balance_lbl);

        String newBalance = getString(R.string.balance) + " " + BalanceCache.getBalance(name) + getString(R.string.currency);
        balance.setText(newBalance);

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NotNull Call<UserModel> call, @NotNull Response<UserModel> response) {
                if (offlineToast) {
                    Toast.makeText(UserInfoActivity.this, getString(R.string.connection_established), Toast.LENGTH_SHORT).show();
                    offlineToast = false;
                }
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getBalance() != null) {
                        String newBalance = getString(R.string.balance) + " " + response.body().getBalance() + getString(R.string.currency);
                        balance.setText(newBalance);
                        balance.setVisibility(View.VISIBLE);
                        BalanceCache.update(name, response.body().getBalance());
                    } else {
                        balanceAccess = false;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserModel> call, @NotNull Throwable t) {
                if (!offlineToast) {
                    Toast.makeText(UserInfoActivity.this, getString(R.string.cannot_reach_server), Toast.LENGTH_SHORT).show();
                    offlineToast = true;
                }
            }
        });

        ImageUtils.loadProfilePicture(name, profilePicture, profilePicture.getDrawable(), getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE));
    }

    public void refreshBalance() {
        if (balanceAccess && !gone) {
            Call<UserModel> call = RetrofitService.getHbankApi().getUser(name, RetrofitService.getAuthorization());
            TextView balance = findViewById(R.id.user_balance_lbl);

            call.enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(@NotNull Call<UserModel> call, @NotNull Response<UserModel> response) {
                    if (offlineToast) {
                        Toast.makeText(UserInfoActivity.this, getString(R.string.connection_established), Toast.LENGTH_SHORT).show();
                        offlineToast = false;
                    }
                    if (response.isSuccessful()) {
                        if (response.body() != null && response.body().getBalance() != null) {
                            String newBalance = getString(R.string.balance) + " " + response.body().getBalance() + getString(R.string.currency);
                            balance.setText(newBalance);
                            BalanceCache.update(name, response.body().getBalance());
                        } else {
                            balanceAccess = false;
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<UserModel> call, @NotNull Throwable t) {
                    if (!offlineToast) {
                        Toast.makeText(UserInfoActivity.this, getString(R.string.cannot_reach_server), Toast.LENGTH_SHORT).show();
                        offlineToast = true;
                    }
                }
            });
        }
    }

    public void transferMoney(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, TransferMoneyActivity.class);
            i.putExtra("name", name);
            startActivity(i);
        }
    }

    public void paymentPlans(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, PaymentPlanActivity.class);
            i.putExtra("name", name);
            startActivity(i);
        }
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("name", name);
        i.putExtra("logout", true);
        startActivity(i);
        finishAffinity();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshBalanceHandler.removeCallbacks(refreshBalanceRunnable);
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            loadUserInfo();
            refreshBalanceHandler.postDelayed(refreshBalanceRunnable, 2000);
            paused = false;
        }
        gone = false;
    }
}