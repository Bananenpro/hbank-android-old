package de.julianhofmann.h_bank;

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

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.IntIdModel;
import de.julianhofmann.h_bank.api.models.VersionModel;
import kotlin.text.Charsets;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Util {

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

    public static void update(Context context) {
        Call<VersionModel> call = RetrofitService.getHbankApi().getVersion();
        call.enqueue(new Callback<VersionModel>() {
            @Override
            public void onResponse(Call<VersionModel> call, Response<VersionModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getVersion() > BuildConfig.VERSION_CODE) {
                        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                            if (which == dialog.BUTTON_POSITIVE) {
                                installUpdate(context);
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.update_available).setMessage(R.string.update_question).setPositiveButton(R.string.yes, dialogClickListener).setNegativeButton(R.string.no, dialogClickListener).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<VersionModel> call, Throwable t) {
                Toast.makeText(context, R.string.offline, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void installUpdate(Context context) {
        String destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/apk/h-bank.apk";

        Uri uri = Uri.parse("file://" + destination);

        File file = new File(destination);
        if (file.exists()) file.delete();

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(RetrofitService.URL + "apk");

        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setMimeType("application/vnd.android.package-archive");
        request.setTitle(context.getString(R.string.update));
        request.setDescription(context.getString(R.string.downloading));
        request.setDestinationUri(uri);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                i.setDataAndType(fileUri, "application/vnd.android.package-archive");
                context.startActivity(i);
                context.unregisterReceiver(this);
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager.enqueue(request);
    }

    public static void storePassword(String password, SharedPreferences sp) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            SecureRandom secureRandom = new SecureRandom();
            byte[] salt = new byte[32];

            secureRandom.nextBytes(salt);

            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
            SecretKey secretKey = keyFactory.generateSecret(keySpec);

            SharedPreferences.Editor edit = sp.edit();
            edit.putString("salt", new String(salt, Charsets.ISO_8859_1));
            edit.putString("password_hash", new String(secretKey.getEncoded(), Charsets.ISO_8859_1));
            edit.apply();
        } catch (InvalidKeySpecException e) {
            Log.e("ERROR", "Cannot generate password hash: Invalid key spec!");
        } catch (NoSuchAlgorithmException ignored) {}
    }

    public static String checkPassword(String name, String password, SharedPreferences sp) {

        if (!sp.getString("name", "").equals(name)) return null;

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            String saltStr = sp.getString("salt", null);

            if (saltStr != null) {

                byte[] salt = saltStr.getBytes(Charsets.ISO_8859_1);

                KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
                SecretKey secretKey = keyFactory.generateSecret(keySpec);

                String passwordHash = sp.getString("password_hash", null);

                if (passwordHash != null && new String(secretKey.getEncoded(), Charsets.ISO_8859_1).equals(passwordHash)) {
                    return sp.getString("token", null);

                }
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException ignored) { }

        return null;
    }

    public static void clearPassword(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove("salt");
        edit.remove("password_hash");
        edit.apply();
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

