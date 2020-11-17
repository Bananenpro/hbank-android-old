package de.julianhofmann.h_bank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LogModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogItemInfoActivity extends AppCompatActivity {


    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_item_info);

        Intent i = getIntent();

        int id = i.getIntExtra("id", -1);

        if (id == -1) {
            onSupportNavigateUp();
            return;
        }

        TextView amount = findViewById(R.id.log_item_amount_lbl);
        TextView newBalance = findViewById(R.id.log_item_new_balance_lbl);
        TextView time = findViewById(R.id.log_item_time_lbl);
        TextView user = findViewById(R.id.log_item_user_lbl);

        Call<LogModel> call = RetrofitService.getHbankApi().getLogItem(id, RetrofitService.getAuthorization());
        call.enqueue(new Callback<LogModel>() {
            @Override
            public void onResponse(Call<LogModel> call, Response<LogModel> response) {
                if (response.isSuccessful()) {
                    amount.setText(response.body().getAmount() + getString(R.string.currency));

                    if (response.body().getAmount().startsWith("-")) {
                        amount.setTextColor(getColor(R.color.red));
                    }

                    newBalance.setText(response.body().getNewBalance() + getString(R.string.currency));
                    time.setText(response.body().getDate());
                    user.setText(response.body().getUsername());
                } else if (response.code() == 403) {
                    String name = RetrofitService.name;
                    RetrofitService.logout();
                    switchToLoginActivity(name);
                } else {
                    Log.e("ERROR", id+"");
                }
            }

            @Override
            public void onFailure(Call<LogModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.offline, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("name", name);
        i.putExtra("logout", true);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}