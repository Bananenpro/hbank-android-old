package de.julianhofmann.h_bank;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class Util {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());

    public static String getCompressed(Context context, String path, double targetSize) {

        try {
            if (context == null)
                throw new NullPointerException("Context must not be null.");
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null)
                cacheDir = context.getCacheDir();

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
}

