package de.julianhofmann.h_bank.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.ui.system.InfoActivity;
import de.julianhofmann.h_bank.ui.system.SettingsActivity;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LogModel;
import de.julianhofmann.h_bank.api.models.UserModel;
import de.julianhofmann.h_bank.ui.auth.LoginActivity;
import de.julianhofmann.h_bank.ui.main.log.LogItemInfoActivity;
import de.julianhofmann.h_bank.ui.main.log.LogListItem;
import de.julianhofmann.h_bank.ui.main.user_list.UserInfoActivity;
import de.julianhofmann.h_bank.ui.main.user_list.UserListItem;
import de.julianhofmann.h_bank.ui.transaction.PaymentPlanActivity;
import de.julianhofmann.h_bank.util.BalanceCache;
import de.julianhofmann.h_bank.util.ImageUtils;
import de.julianhofmann.h_bank.util.SettingsService;
import de.julianhofmann.h_bank.util.UpdateService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    private ImagePicker imagePicker;
    private ImagePickerCallback imagePickerCallback;
    private int logPage = 0;
    private boolean allLogPages = false;
    private boolean loadingLog = false;
    private boolean paused = false;
    private boolean offline = false;
    private boolean spinning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_user_list, R.id.navigation_log)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        SettingsService.init(getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE), RetrofitService.getName());

        imagePickerCallback = new ImagePickerCallback() {
            @Override
            @SuppressWarnings("ResultOfMethodCallIgnored")
            public void onImagesChosen(List<ChosenImage> list) {
                ChosenImage image = list.get(0);
                uploadImage(ImageUtils.getCompressed(getApplicationContext(), image.getOriginalPath(), 500));
                new File(image.getTempFile()).delete();
                new File(image.getThumbnailPath()).delete();
                new File(image.getThumbnailSmallPath()).delete();
                new File(image.getOriginalPath()).delete();
                Objects.requireNonNull(new File(image.getOriginalPath()).getParentFile()).delete();
                Objects.requireNonNull(Objects.requireNonNull(new File(image.getOriginalPath()).getParentFile()).getParentFile()).delete();
            }

            @Override
            public void onError(String s) {
            }
        };

        if (SettingsService.getCheckForUpdates() && !ImageUtils.askedForUpdate) {
            update(true);
            ImageUtils.askedForUpdate = true;
        }
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
                update(false);
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

    public void resetLogPages() {
        logPage = 0;
        allLogPages = false;
        loadingLog = false;
        LinearLayout layout = findViewById(R.id.log_list_layout);
        layout.removeAllViews();
    }

    private void uploadImage(String path) {

        Toast.makeText(getApplicationContext(), getString(R.string.wait), Toast.LENGTH_SHORT).show();

        File sourceFile = new File(path);

        if (!sourceFile.exists()) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_file_does_not_exist), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("profile_picture", path, RequestBody.create(MEDIA_TYPE_PNG, sourceFile))
                    .build();

            Request request = new Request.Builder()
                    .url(RetrofitService.URL + "profile_picture")
                    .post(requestBody)
                    .addHeader("Authorization", RetrofitService.getAuthorization())
                    .addHeader("Content-Type", "application/x-www-formurlencoded")
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) {
                    if (response.isSuccessful()) {
                        reloadActivity();
                    }
                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    offline();
                }
            });
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }

    private void reloadActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("logout", true);
        i.putExtra("name", name);
        startActivity(i);
        finishAffinity();
    }

    public void loadUserInfo(View v) {
        if (!spinning) {

            TextView username = findViewById(R.id.user_name_lbl);
            username.setText(RetrofitService.getName());

            FloatingActionButton refreshBtn = findViewById(R.id.user_refresh_button);
            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            if (v != null) {
                spinning = true;
                ViewCompat.animate(refreshBtn).
                        rotation(720).
                        withLayer().
                        setDuration(1125).
                        setInterpolator(interpolator).setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        view.setRotation(0);
                        spinning = false;
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        view.setRotation(0);
                        spinning = false;
                    }
                }).
                        start();
            }
            Call<UserModel> call = RetrofitService.getHbankApi().getUser(RetrofitService.getName(), RetrofitService.getAuthorization());
            TextView balance = findViewById(R.id.user_balance_lbl);

            String newBalance = getString(R.string.balance) + " " + BalanceCache.getBalance(RetrofitService.getName()) + getString(R.string.currency);
            balance.setText(newBalance);

            call.enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(@NotNull Call<UserModel> call, @NotNull Response<UserModel> response) {
                    online();
                    if (response.isSuccessful()) {
                        if (response.body() != null && response.body().getBalance() != null) {
                            String newBalance = getString(R.string.balance) + " " + response.body().getBalance() + getString(R.string.currency);
                            balance.setText(newBalance);
                            BalanceCache.update(RetrofitService.getName(), response.body().getBalance());
                        } else {
                            logout();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<UserModel> call, @NotNull Throwable t) {
                    offline();
                }
            });

            ImageView profilePicture = findViewById(R.id.user_profile_picture);


            ImageUtils.loadProfilePicture(RetrofitService.getName(), profilePicture, profilePicture.getDrawable(), getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE));
        }
    }


    private void update(boolean autoUpdate) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        UpdateService.update(this, autoUpdate);
    }

    public void changeProfilePicture(View v) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        imagePicker = new ImagePicker(this);
        imagePicker.setImagePickerCallback(imagePickerCallback);
        imagePicker.pickImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Picker.PICK_IMAGE_DEVICE) {
            if (imagePicker == null) {
                imagePicker = new ImagePicker(this);
                imagePicker.setImagePickerCallback(imagePickerCallback);
            }
            imagePicker.submit(data);
        }
    }


    public void loadUsers() {
        Call<List<UserModel>> call = RetrofitService.getHbankApi().getUsers();

        TextView emptyLbl = findViewById(R.id.user_list_empty_lbl);
        emptyLbl.setVisibility(View.VISIBLE);
        emptyLbl.setText(R.string.loading);

        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(@NotNull Call<List<UserModel>> call, @NotNull Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    online();
                    LinearLayout layout = findViewById(R.id.user_list_layout);

                    if (layout != null) {
                        layout.removeAllViews();
                        if (response.body().size() > 0) {
                            emptyLbl.setVisibility(View.GONE);
                        } else {
                            emptyLbl.setVisibility(View.VISIBLE);
                            emptyLbl.setText(R.string.no_other_users);
                        }
                        for (UserModel user : response.body()) {
                            if (!user.getName().equals(RetrofitService.getName())) {
                                addUserListItem(layout, user.getName());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<UserModel>> call, @NotNull Throwable t) {
                offline();
                emptyLbl.setVisibility(View.VISIBLE);
                emptyLbl.setText(R.string.offline);
            }
        });
    }

    public void addUserListItem(LinearLayout layout, String name) {
        UserListItem userListItem = new UserListItem(this);

        userListItem.getNameButton().setText(name);
        userListItem.getNameButton().setOnClickListener(v -> {
            Button button = (Button) v;
            goToUser(button.getText().toString());
        });

        ImageUtils.loadProfilePicture(name, userListItem.getProfilePictureImageView(), userListItem.getProfilePictureImageView().getDrawable(), getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE));

        layout.addView(userListItem);
    }

    public void goToUser(String name) {
        Intent i = new Intent(this, UserInfoActivity.class);
        i.putExtra("name", name);
        startActivity(i);
    }

    public void loadLog() {
        if (!allLogPages && !loadingLog) {
            loadingLog = true;
            Call<List<LogModel>> call = RetrofitService.getHbankApi().getLog(logPage, RetrofitService.getAuthorization());
            TextView emptyLbl = findViewById(R.id.log_empty_lbl);
            if (logPage == 0) {
                emptyLbl.setVisibility(View.VISIBLE);
                emptyLbl.setText(R.string.loading);
            }
            logPage++;
            call.enqueue(new Callback<List<LogModel>>() {
                @Override
                public void onResponse(@NotNull Call<List<LogModel>> call, @NotNull Response<List<LogModel>> response) {
                    online();
                    if (response.isSuccessful() && response.body() != null) {

                        if (response.body().size() == 0 && logPage == 1) {
                            emptyLbl.setVisibility(View.VISIBLE);
                            emptyLbl.setText(R.string.empty_log);
                        } else {
                            emptyLbl.setVisibility(View.GONE);
                        }

                        if (response.body().size() == 0) {
                            allLogPages = true;
                            return;
                        }

                        LinearLayout layout = findViewById(R.id.log_list_layout);

                        for (LogModel item : response.body()) {
                            addLogItem(layout, item);
                        }

                    } else if (response.code() == 403) {
                        logout();
                    }

                    loadingLog = false;
                }

                @Override
                public void onFailure(@NotNull Call<List<LogModel>> call, @NotNull Throwable t) {
                    offline();
                    logPage--;
                    loadingLog = false;
                    if (logPage == 0) {
                        emptyLbl.setVisibility(View.VISIBLE);
                        emptyLbl.setText(R.string.offline);
                    }
                }
            });
        }
    }

    public void addLogItem(LinearLayout layout, LogModel model) {
        if (layout == null) return;

        LogListItem item = new LogListItem(this);
        item.getDate().setText(model.getDate());
        item.getDescription().setText(model.getDescription());

        item.getAmount().setText(String.format("%s%s", model.getAmount(), getString(R.string.currency)));
        if (model.getAmount().startsWith("-")) {
            item.getAmount().setTextColor(getColor(R.color.red));
        }

        item.getButton().setOnClickListener(v -> goToLogItemInfo(model.getId()));

        layout.addView(item);
    }

    private void goToLogItemInfo(int id) {
        Intent i = new Intent(this, LogItemInfoActivity.class);
        i.putExtra("id", id);
        startActivity(i);
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
            if (SettingsService.getCheckForUpdates() && !ImageUtils.askedForUpdate) {
                update(true);
                ImageUtils.askedForUpdate = true;
            }
            paused = false;
        }
    }

    private void offline() {
        FloatingActionButton editProfilePicture = findViewById(R.id.home_change_profile_picture_button);
        if (editProfilePicture != null) {
            editProfilePicture.setVisibility(View.INVISIBLE);
        }

        Button paymentPlansBtn = findViewById(R.id.home_payment_plans_btn);
        if (paymentPlansBtn != null) {
            paymentPlansBtn.setVisibility(View.INVISIBLE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.no_connection_icon);
        } else if (!offline) {
            Toast.makeText(getApplicationContext(), R.string.offline, Toast.LENGTH_SHORT).show();
        }
        offline = true;
    }

    private void online() {
        FloatingActionButton editProfilePicture = findViewById(R.id.home_change_profile_picture_button);
        if (editProfilePicture != null) {
            editProfilePicture.setVisibility(View.VISIBLE);
        }
        Button paymentPlansBtn = findViewById(R.id.home_payment_plans_btn);
        if (paymentPlansBtn != null) {
            paymentPlansBtn.setVisibility(View.VISIBLE);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setIcon(null);
        } else if (offline) {
            Toast.makeText(getApplicationContext(), R.string.online, Toast.LENGTH_SHORT).show();
        }
        offline = false;
    }

    public void paymentPlans(View v) {
        Intent i = new Intent(this, PaymentPlanActivity.class);
        startActivity(i);
    }
}