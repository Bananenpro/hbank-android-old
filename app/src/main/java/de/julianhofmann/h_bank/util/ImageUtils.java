package de.julianhofmann.h_bank.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.IntIdModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUtils {

    @SuppressLint("ConstantLocale")
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getCompressed(Context context, String path, double targetSize, int rotation) {

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

            double factor;

            if (width > height) factor = height / targetSize;
            else factor = width / targetSize;

            Bitmap bitmap = decodeImageFromFiles(path, (int) (width / factor), (int) (height / factor));

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

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
        Picasso.get().load(RetrofitService.getUrl() + "profile_picture/" + name)
                .placeholder(placeholder)
                .error(placeholder)
                .resize(500, 500)
                .centerCrop()
                .into(imageView);
        Call<IntIdModel> call = RetrofitService.getHbankApi().getProfilePictureId(name);
        call.enqueue(new Callback<IntIdModel>() {
            @Override
            public void onResponse(@NotNull Call<IntIdModel> call, @NotNull Response<IntIdModel> response) {
                int cachedId = getProfilePictureId(name, sharedPreferences);
                if (response.body() != null) {
                    if (response.isSuccessful() && response.body().getId() != cachedId) {
                        if (response.body() != null)
                            updateProfilePictureId(name, response.body().getId(), sharedPreferences);
                        Picasso.get().invalidate(RetrofitService.getUrl() + "profile_picture/" + name);

                        Picasso.get().load(RetrofitService.getUrl() + "profile_picture/" + name)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .placeholder(placeholder)
                                .error(placeholder)
                                .centerCrop()
                                .resize(500, 500)
                                .into(imageView);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<IntIdModel> call, @NotNull Throwable t) {
            }
        });
    }

    private static void updateProfilePictureId(String name, int id, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(name + "_profile_picture_id", id);
        edit.apply();
    }

    private static int getProfilePictureId(String name, SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt(name + "_profile_picture_id", -1);
    }
}

