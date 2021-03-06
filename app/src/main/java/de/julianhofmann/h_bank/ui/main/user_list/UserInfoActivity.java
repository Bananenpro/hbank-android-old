package de.julianhofmann.h_bank.ui.main.user_list;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.UserModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.ui.transaction.PaymentPlanActivity;
import de.julianhofmann.h_bank.ui.transaction.TransferMoneyActivity;
import de.julianhofmann.h_bank.util.BalanceCache;
import de.julianhofmann.h_bank.util.ImageUtils;
import de.julianhofmann.h_bank.util.SettingsService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends BaseActivity {

    private final Handler refreshBalanceHandler = new Handler();
    private Runnable refreshBalanceRunnable;
    private String name;
    private boolean paused = false;
    private boolean balanceAccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_user_info);

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
                loadUserInfo();
                refreshBalanceHandler.postDelayed(this, SettingsService.getAutoRefreshInterval());
            }
        };
        if (SettingsService.getAutoRefresh()) {
            refreshBalanceHandler.postDelayed(refreshBalanceRunnable, SettingsService.getAutoRefreshInterval());
        }
    }

    public void loadUserInfo() {
        ImageView profilePicture = findViewById(R.id.user_profile_picture);


        Call<UserModel> call = RetrofitService.getHbankApi().getUser(name, RetrofitService.getAuthorization());
        TextView balance = findViewById(R.id.user_balance_lbl);
        TextView cash = findViewById(R.id.user_cash_lbl);

        String newBalance = getString(R.string.balance) + " " + BalanceCache.getBalance(name) + getString(R.string.currency);
        balance.setText(newBalance);

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NotNull Call<UserModel> call, @NotNull Response<UserModel> response) {
                online();
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getBalance() != null) {
                        String newBalance = getString(R.string.balance) + " " + response.body().getBalance() + getString(R.string.currency);
                        balance.setText(newBalance);
                        balance.setVisibility(View.VISIBLE);
                        BalanceCache.update(name, response.body().getBalance());

                        String newCash = getString(R.string.cash_lbl) + " " + response.body().getCash() + getString(R.string.currency);
                        cash.setText(newCash);
                        cash.setVisibility(View.VISIBLE);
                    } else {
                        balanceAccess = false;
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserModel> call, @NotNull Throwable t) {
                offline();
            }
        });

        ImageUtils.loadProfilePicture(name, profilePicture, profilePicture.getDrawable(), getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE));
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

    @Override
    protected void offline() {
        refreshBalanceHandler.removeCallbacks(refreshBalanceRunnable);
        super.offline();
    }

    @Override
    protected void online() {
        if (offline) {
            loadUserInfo();
            if (SettingsService.getAutoRefresh()) {
                refreshBalanceHandler.postDelayed(refreshBalanceRunnable, SettingsService.getAutoRefreshInterval());
            }
        }
        super.online();
    }

    @Override
    protected void onPause() {
        refreshBalanceHandler.removeCallbacks(refreshBalanceRunnable);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (paused) {
            loadUserInfo();
            if (SettingsService.getAutoRefresh()) {
                refreshBalanceHandler.postDelayed(refreshBalanceRunnable, SettingsService.getAutoRefreshInterval());
            }
            paused = false;
        }
        super.onResume();
    }
}