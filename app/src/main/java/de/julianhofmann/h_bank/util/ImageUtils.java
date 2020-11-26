package de.julianhofmann.h_bank.util;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.IntIdModel;
import de.julianhofmann.h_bank.api.models.VersionModel;
import kotlin.text.Charsets;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUtils {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());
    public static boolean askedForUpdate = false;

    public static String getCompressed(Context context, String path, double targetSize) {

        try {
            if (context == null)
                throw new NullPointerException("Context must not be null.");

            File cacheDir = context.getCacheDir();

            String rootDir = cacheDir.getAbsolutePath() + "/ImageCompressor";
            File root = new File(rootDir);

            if (!root.exists())
                root.mkdirs();


            Bitmap image = BitmapFactory.decodeFile(path);

            double width = image.getWidth();
            double height = image.getHeight();

            double factor = 1;

            if (width > height) factor = height / targetSize;
            else factor = width / targetSize;

            Bitmap bitmap = decodeImageFromFiles(path, (int) (width / factor), (int) (height / factor));

            File compressed = new File(root, SDF.format(new Date()) + ".jpg");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

            FileOutputStream fileOutputStream = new FileOutputStream(compressed);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();

            fileOutputStream.close();

            return compressed.getPath();
        } catch (IOException e) {
            Log.e("error", e.getMessage());
        }
        return "";
    }

    public static Bitmap decodeImageFromFiles(String path, int width, int height) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, scaleOptions);
        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= width
                && scaleOptions.outHeight / scale / 2 >= height) {
            scale *= 2;
        }
        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, outOptions);
    }

    public static void loadProfilePicture(String name, ImageView imageView, Drawable placeholder, SharedPreferences sharedPreferences) {
        Picasso.get().load(RetrofitService.URL + "profile_picture/" + name)
                .placeholder(placeholder)
                .error(placeholder)
                .fit()
                .centerCrop()
                .into(imageView);
        Call<IntIdModel> call = RetrofitService.getHbankApi().getProfilePictureId(name);
        call.enqueue(new Callback<IntIdModel>() {
            @Override
            public void onResponse(Call<IntIdModel> call, Response<IntIdModel> response) {
                int cachedId = getProfilePictureId(name, sharedPreferences);
                if (!response.isSuccessful() || (cachedId != -1 && response.body().getId() != cachedId)) {
                    if (response.body() != null) updateProfilePictureId(name, response.body().getId(), sharedPreferences);
                    Picasso.get().invalidate(RetrofitService.URL + "profile_picture/" + name);

                    Picasso.get().load(RetrofitService.URL + "profile_picture/" + name)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .placeholder(placeholder)
                            .error(placeholder)
                            .fit()
                            .centerCrop()
                            .into(imageView);
                }
            }

            @Override
            public void onFailure(Call<IntIdModel> call, Throwable t) {
            }
        });
    }

    private static void updateProfilePictureId(String name, int id, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(name + "_profile_picture_id", id);
        edit.apply();
    }

    private static int getProfilePictureId(String name, SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(name+"_profile_picture_id", -1);
    }
}

