package de.julianhofmann.h_bank.ui.main.log;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LogModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.util.UpdateService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogItemInfoActivity extends AppCompatActivity {

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

        TextView title = findViewById(R.id.log_item_lbl);
        TextView amount = findViewById(R.id.log_item_amount_lbl);
        TextView newBalance = findViewById(R.id.log_item_new_balance_lbl);
        TextView time = findViewById(R.id.log_item_time_lbl);
        TextView user = findViewById(R.id.log_item_next_lbl);
        TextView userLbl = findViewById(R.id.log_item_next_lbl_lbl);

        Call<LogModel> call = RetrofitService.getHbankApi().getLogItem(id, RetrofitService.getAuthorization());
        call.enqueue(new Callback<LogModel>() {
            @Override
            public void onResponse(@NotNull Call<LogModel> call, @NotNull Response<LogModel> response) {
                if (response.isSuccessful() && response.body() != null) {

                    if (title != null)
                        title.setText(response.body().getDescription());

                    if (amount != null)
                        amount.setText(String.format("%s%s", response.body().getAmount(), getString(R.string.currency)));

                    if (response.body().getAmount().startsWith("-") && amount != null) {
                        amount.setTextColor(getColor(R.color.red));
                        userLbl.setText(R.string.receiver_lbl);
                    }

                    if (newBalance != null)
                        newBalance.setText(String.format("%s%s", response.body().getNewBalance(), getString(R.string.currency)));
                    if (time != null)
                        time.setText(response.body().getDate());
                    if (user != null)
                        user.setText(response.body().getUsername());
                } else if (response.code() == 403) {
                    logout();
                } else {
                    Log.e("ERROR", id + "");
                }
            }

            @Override
            public void onFailure(@NotNull Call<LogModel> call, @NotNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.offline, Toast.LENGTH_LONG).show();
            }
        });
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

    private void update() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        UpdateService.update(this);
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("name", name);
        i.putExtra("logout", true);
        startActivity(i);
        finishAffinity();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }
}