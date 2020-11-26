package de.julianhofmann.h_bank.ui.main.user_list;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.julianhofmann.h_bank.util.BalanceCache;
import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.ui.transaction.PaymentPlanActivity;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.ui.transaction.TransferMoneyActivity;
import de.julianhofmann.h_bank.util.ImageUtils;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.UserModel;
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

        if(name == null) {
            onSupportNavigateUp();
        }


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(name);
        }

        loadUserInfo();
    }


    public void loadUserInfo() {
        ImageView profilePicture = findViewById(R.id.user_profile_picture);


        Call<UserModel> call = RetrofitService.getHbankApi().getUser(name, RetrofitService.getAuthorization());
        TextView balance = findViewById(R.id.user_balance_lbl);

        String newBalance = getString(R.string.balance) + " " + BalanceCache.getBalance(name) + getString(R.string.currency);
        balance.setText(newBalance);

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
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
            public void onFailure(Call<UserModel> call, Throwable t) {
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