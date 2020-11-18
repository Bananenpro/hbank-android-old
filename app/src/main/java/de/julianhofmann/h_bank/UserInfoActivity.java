package de.julianhofmann.h_bank;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getBalance() != null) {
                        String newBalance = getString(R.string.balance) + " " + response.body().getBalance() + getString(R.string.currency);
                        if (balance != null) {
                            balance.setText(newBalance);
                            balance.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
            }
        });

        Picasso.get().invalidate(RetrofitService.URL + "profile_picture/" + name);
        Picasso.get()
                .load(RetrofitService.URL + "profile_picture/" + name)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .placeholder(R.mipmap.empty_profile_picture)
                .error(R.mipmap.empty_profile_picture)
                .fit()
                .centerCrop()
                .into(profilePicture);
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