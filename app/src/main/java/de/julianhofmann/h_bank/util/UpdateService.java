package de.julianhofmann.h_bank.util;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;

import de.julianhofmann.h_bank.BuildConfig;
import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.VersionModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateService {
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

        Toast.makeText(context, R.string.downloading, Toast.LENGTH_SHORT).show();
        downloadManager.enqueue(request);

    }
}
