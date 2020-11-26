package de.julianhofmann.h_bank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.LogModel;
import de.julianhofmann.h_bank.api.models.UserModel;
import de.julianhofmann.h_bank.ui.log.LogListItem;
import de.julianhofmann.h_bank.ui.user_list.UserListItem;
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

        Intent i = getIntent();

        imagePickerCallback = new ImagePickerCallback() {
            @Override
            public void onImagesChosen(List<ChosenImage> list) {
                ChosenImage image = list.get(0);
                uploadImage(Util.getCompressed(getApplicationContext(), image.getOriginalPath(), 500));
                new File(image.getTempFile()).delete();
                new File(image.getThumbnailPath()).delete();
                new File(image.getThumbnailSmallPath()).delete();
                new File(image.getOriginalPath()).delete();
                new File(image.getOriginalPath()).getParentFile().delete();
                new File(image.getOriginalPath()).getParentFile().getParentFile().delete();
            }

            @Override
            public void onError(String s) {
            }
        };

        if (!Util.askedForUpdate) {
            update();
            Util.askedForUpdate = true;
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
                    .url(RetrofitService.URL+"profile_picture")
                    .post(requestBody)
                    .addHeader("Authorization", RetrofitService.getAuthorization())
                    .addHeader("Content-Type", "application/x-www-formurlencoded")
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        reloadActivity(true);
                    }
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    offline();
                }
            });
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }


    }

    private void reloadActivity(boolean newPicture) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("newProfilePicture", newPicture);
        startActivity(i);
        overridePendingTransition(0,0);
        finish();
        overridePendingTransition(0,0);
    }

    private void switchToLoginActivity(String name) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("logout", true);
        i.putExtra("name", name);
        startActivity(i);
    }

    public void loadUserInfo(View v) {
        if (!spinning) {
            FloatingActionButton refreshBtn = findViewById(R.id.user_refresh_button);
            AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            if (v != null) {
                spinning = true;
                ViewCompat.animate(refreshBtn).
                        rotation(720).
                        withLayer().
                        setDuration(2250).
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
            Call<UserModel> call = RetrofitService.getHbankApi().getUser(RetrofitService.name, RetrofitService.getAuthorization());
            TextView balance = findViewById(R.id.user_balance_lbl);

            String newBalance = getString(R.string.balance) + " " + BalanceCache.getBalance(RetrofitService.name) + getString(R.string.currency);
            balance.setText(newBalance);

            call.enqueue(new Callback<UserModel>() {
                @Override
                public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                    online();
                    if (response.isSuccessful()) {
                        if (response.body() != null && response.body().getBalance() != null) {
                            String newBalance = getString(R.string.balance) + " " + response.body().getBalance() + getString(R.string.currency);
                            if (balance != null)
                                balance.setText(newBalance);
                            BalanceCache.update(RetrofitService.name, response.body().getBalance());
                        } else {
                            String name = RetrofitService.name;
                            RetrofitService.logout();
                            switchToLoginActivity(name);
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserModel> call, Throwable t) {
                    offline();
                }
            });

            ImageView profilePicture = findViewById(R.id.user_profile_picture);


            Util.loadProfilePicture(RetrofitService.name, profilePicture, profilePicture.getDrawable(), getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE));
        }
    }


    private void update() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        Util.update(this);
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
        if(requestCode == Picker.PICK_IMAGE_DEVICE) {
            if(imagePicker == null) {
                imagePicker = new ImagePicker(this);
                imagePicker.setImagePickerCallback(imagePickerCallback);
            }
            imagePicker.submit(data);
        }
    }


    public void loadUsers() {
        Call<List<UserModel>> call = RetrofitService.getHbankApi().getUsers();

        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful()) {
                    online();
                    LinearLayout layout = findViewById(R.id.user_list_layout);

                    if (layout != null) {
                        layout.removeAllViews();
                        for (UserModel user : response.body()) {
                            if (!user.getName().equals(RetrofitService.name)) {
                                addUserListItem(layout, user.getName());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                offline();
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

        Util.loadProfilePicture(name, userListItem.getProfilePictureImageView(), userListItem.getProfilePictureImageView().getDrawable(), getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE));

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
            logPage++;
            call.enqueue(new Callback<List<LogModel>>() {
                @Override
                public void onResponse(Call<List<LogModel>> call, Response<List<LogModel>> response) {
                    online();
                    if (response.isSuccessful() && response.body() != null) {

                        if (response.body().size() == 0) {
                            allLogPages = true;
                            return;
                        }

                        LinearLayout layout = findViewById(R.id.log_list_layout);

                        for (LogModel item : response.body()) {
                            addLogItem(layout, item);
                        }

                    } else if (response.code() == 403) {
                        String name = RetrofitService.name;
                        RetrofitService.logout();
                        switchToLoginActivity(name);
                    }

                    loadingLog = false;
                }

                @Override
                public void onFailure(Call<List<LogModel>> call, Throwable t) {
                    offline();
                    logPage--;
                    loadingLog = false;
                }
            });
        }
    }

    public void addLogItem(LinearLayout layout, LogModel model) {
        if (layout == null) return;

        LogListItem item = new LogListItem(this);
        item.getDate().setText(model.getDate());
        item.getDescription().setText(model.getDescription());

        item.getAmount().setText(model.getAmount()+getString(R.string.currency));
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
            if (!Util.askedForUpdate) {
                update();
                Util.askedForUpdate = true;
            }
            paused = false;
        }
    }

    private void offline() {
        FloatingActionButton editProfilePicture = findViewById(R.id.home_change_profile_picture_button);
        if (editProfilePicture != null) {
            editProfilePicture.setVisibility(View.INVISIBLE);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.ic_baseline_wifi_off_24);
        } else if (!offline){
            Toast.makeText(getApplicationContext(), R.string.offline, Toast.LENGTH_SHORT).show();
        }
        offline = true;
    }

    private void online() {
        FloatingActionButton editProfilePicture = findViewById(R.id.home_change_profile_picture_button);
        if (editProfilePicture != null) {
            editProfilePicture.setVisibility(View.VISIBLE);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setIcon(null);
        } else if (offline){
            Toast.makeText(getApplicationContext(), R.string.online, Toast.LENGTH_SHORT).show();
        }
        offline = false;
    }
}