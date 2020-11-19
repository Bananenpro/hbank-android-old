package de.julianhofmann.h_bank;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.VersionModel;
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
}

