package de.julianhofmann.h_bank.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.exifinterface.media.ExifInterface;
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
import de.julianhofmann.h_bank.api.models.CashModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LogModel;
import de.julianhofmann.h_bank.api.models.UserModel;
import de.julianhofmann.h_bank.ui.main.log.LogItemInfoActivity;
import de.julianhofmann.h_bank.ui.main.log.LogListItem;
import de.julianhofmann.h_bank.ui.main.user_list.UserInfoActivity;
import de.julianhofmann.h_bank.ui.main.user_list.UserListItem;
import de.julianhofmann.h_bank.ui.tools.CalculatorActivity;
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

public class MainActivity extends BaseActivity {


    private ImagePicker imagePicker;
    private ImagePickerCallback imagePickerCallback;
    private int logPage = 0;
    private boolean allLogPages = false;
    private boolean loadingLog = false;
    private boolean spinning = false;
    private String currentFragment = "";
    private String serverCash = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_user_list, R.id.navigation_log)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getLabel() != null) {
                currentFragment = destination.getLabel().toString();
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        SettingsService.init(getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE), RetrofitService.getName());

        imagePickerCallback = new ImagePickerCallback() {
            @Override
            @SuppressWarnings("ResultOfMethodCallIgnored")
            public void onImagesChosen(List<ChosenImage> list) {
                ChosenImage image = list.get(0);
                int rotation;
                switch (image.getOrientation()) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;
                    default:
                        rotation = 0;
                        break;
                }
                uploadImage(ImageUtils.getCompressed(getApplicationContext(), image.getOriginalPath(), 500, rotation));
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

        if (SettingsService.getCheckForUpdates() && !UpdateService.askedForUpdate) {
            update(true);
        }
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
                    .url(RetrofitService.getUrl() + "profile_picture")
                    .post(requestBody)
                    .addHeader("Authorization", RetrofitService.getAuthorization())
                    .addHeader("Content-Type", "application/x-www-formurlencoded")
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) {
                    online();
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

    public void loadUserInfo(View v) {
        if (!spinning && !gone && !offline) {
            TextView username = findViewById(R.id.user_name_lbl);
            if (username != null) {
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
                    }).start();
                }


                Call<UserModel> call = RetrofitService.getHbankApi().getUser(RetrofitService.getName(), RetrofitService.getAuthorization());
                TextView balance = findViewById(R.id.user_balance);

                String newBalance = BalanceCache.getBalance(RetrofitService.getName()) + (SettingsService.getCashNoteFunction() ? "" : getString(R.string.currency));
                balance.setText(newBalance);

                TextView cash = findViewById(R.id.cash_input);
                TextView lastCashEdit = findViewById(R.id.last_cash_edit);

                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(@NotNull Call<UserModel> call, @NotNull Response<UserModel> response) {
                        online();
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().getBalance() != null) {
                                String newBalance = response.body().getBalance() + (SettingsService.getCashNoteFunction() ? "" : getString(R.string.currency));
                                balance.setText(newBalance);
                                BalanceCache.update(RetrofitService.getName(), response.body().getBalance());
                                if (!cash.isFocused()) {
                                    serverCash = response.body().getCash();
                                    cash.setText(response.body().getCash());
                                }
                                String lastEditText = getString(R.string.last_edit_lbl) + " " + response.body().getLastCashEdit();
                                lastCashEdit.setText(lastEditText);
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
    }

    public void updateCash() {
        EditText cash = findViewById(R.id.cash_input);
        if (cash != null) {
            String cashStr = cash.getText().toString();
            if (cash.getText().length() == 0 || cash.getText().toString().equals(".")) {
                cashStr = "0";
            }
            if (cashStr.endsWith(".")) {
                cashStr += "00";
            }
            if (cashStr.endsWith(".0")) {
                cashStr += "0";
            }
            if (!cashStr.contains(".")) {
                cashStr += ".00";
            }

            if (!cashStr.equals(serverCash)) {
                Call<Void> call = RetrofitService.getHbankApi().updateCash(new CashModel(cashStr), RetrofitService.getAuthorization());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                        online();
                        loadUserInfo(null);
                    }

                    @Override
                    public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                        offline();
                    }
                });
            }
        }
    }

    public void changeProfilePicture(View v) {
        if (!gone) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }

            imagePicker = new ImagePicker(this);
            imagePicker.setImagePickerCallback(imagePickerCallback);
            imagePicker.pickImage();
            gone = true;
        }
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
        LinearLayout layout = findViewById(R.id.user_list_layout);
        layout.removeAllViews();
        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(@NotNull Call<List<UserModel>> call, @NotNull Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    online();
                    if (response.body().size() > 1) {
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

            @Override
            public void onFailure(@NotNull Call<List<UserModel>> call, @NotNull Throwable t) {
                offline();
                emptyLbl.setVisibility(View.VISIBLE);
                emptyLbl.setText(R.string.cannot_reach_server);
            }
        });
    }

    public void addUserListItem(LinearLayout layout, String name) {
        UserListItem userListItem = new UserListItem(this);

        userListItem.getNameButton().setText(name);
        userListItem.getNameButton().setOnClickListener(v -> {
            if (!gone) {
                gone = true;
                Button button = (Button) v;
                goToUser(button.getText().toString());
            }
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
        if (!allLogPages && !loadingLog && !gone) {
            loadingLog = true;
            Call<List<LogModel>> call = RetrofitService.getHbankApi().getLog(logPage, RetrofitService.getAuthorization());
            TextView emptyLbl = findViewById(R.id.log_empty_lbl);
            if (logPage == 0) {
                emptyLbl.setVisibility(View.VISIBLE);
                emptyLbl.setText(R.string.loading);
                LinearLayout layout = findViewById(R.id.log_list_layout);
                layout.removeAllViews();
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
                        emptyLbl.setText(R.string.cannot_reach_server);
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

        item.getButton().setOnClickListener(v -> {
            if (!gone) {
                gone = true;
                goToLogItemInfo(model.getId());
            }
        });

        layout.addView(item);
    }

    private void goToLogItemInfo(int id) {
        Intent i = new Intent(this, LogItemInfoActivity.class);
        i.putExtra("id", id);
        startActivity(i);
    }

    public void offline() {
        FloatingActionButton editProfilePicture = findViewById(R.id.home_change_profile_picture_button);
        if (editProfilePicture != null) {
            editProfilePicture.setVisibility(View.INVISIBLE);
        }

        FloatingActionButton refresh = findViewById(R.id.user_refresh_button);
        if (refresh != null) {
            refresh.setEnabled(false);
        }

        EditText cash = findViewById(R.id.cash_input);
        if (cash != null) {
            cash.setEnabled(false);
        }

        super.offline();
    }

    public void online() {
        FloatingActionButton editProfilePicture = findViewById(R.id.home_change_profile_picture_button);
        if (editProfilePicture != null) {
            editProfilePicture.setVisibility(View.VISIBLE);
        }

        FloatingActionButton refresh = findViewById(R.id.user_refresh_button);
        if (refresh != null) {
            refresh.setEnabled(true);
        }

        EditText cash = findViewById(R.id.cash_input);
        if (cash != null) {
            cash.setEnabled(true);
        }

        if (offline) {
            if (currentFragment.equals(getString(R.string.title_fragment_home))) {
                loadUserInfo(null);
            } else if (currentFragment.equals(getString(R.string.title_fragment_user_list))) {
                loadUsers();
            } else if (currentFragment.equals(getString(R.string.title_fragment_log))) {
                resetLogPages();
                loadLog();
            }
        }

        super.online();
    }

    public void paymentPlans(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, PaymentPlanActivity.class);
            startActivity(i);
        }
    }

    public void calculator(View v) {
        if (!gone) {
            gone = true;
            Intent i = new Intent(this, CalculatorActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        if (paused) {
            if (SettingsService.getCheckForUpdates() && !UpdateService.askedForUpdate) {
                update(true);
                UpdateService.askedForUpdate = true;
            }
            paused = false;
        }
        super.onResume();
    }
}