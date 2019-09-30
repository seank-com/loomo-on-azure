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
    private final int fileno;

    ImagePoster(Image image, int fileno) {
        Log.d(TAG, String.format("constructor threadId=%d", Thread.currentThread().getId()));
        this.image = image;
        this.fileno = fileno;
    }

    @Override
    public void run() {
        Log.d(TAG, String.format("run threadId=%d", Thread.currentThread().getId()));

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        String uri = String.format("https://seank.blob.core.windows.net/loomo/loomo-%d.jpg", fileno) +
            "?sv=2018-03-28&ss=b&srt=o&sp=rwdlac&se=2019-10-04T07:10:33Z&st=2019-09-24T23:10:33Z&" +
            "spr=https&sig=eskNyO63t3PqmOBte5gBM6OruQI%2FODqAMJJDg8QYOyg%3D";

        DataOutputStream dos = null;
        try {
            URL url = new URL(uri);
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("x-ms-blob-type", "BlockBlob");
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
