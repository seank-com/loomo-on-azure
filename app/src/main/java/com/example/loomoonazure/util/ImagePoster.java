package com.example.loomoonazure.util;

import android.media.Image;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.net.ssl.HttpsURLConnection;

public class ImagePoster implements Runnable {
    private static final String TAG = "ImagePoster";

    private final Image image;

    ImagePoster(Image image, int fileno) {
        Log.d(TAG, String.format("constructor threadId=%d", Thread.currentThread().getId()));
        this.image = image;
    }

    @Override
    public void run() {
        Log.d(TAG, String.format("run threadId=%d", Thread.currentThread().getId()));

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        DataOutputStream dos = null;
        try {
            URL url = new URL("https://loomo.azurewebsites.net/api/recognize");
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "image/jpeg");

            OutputStream out = conn.getOutputStream();
            out.write(bytes);

            int responseCode = conn.getResponseCode();
            String msg = conn.getResponseMessage();
            Log.d(TAG, String.format("response = %d %s", responseCode, msg));
        } catch (Exception e) {
            Log.d(TAG, "Exception PUTing", e);
        } finally {
            image.close();
        }
    }
}
