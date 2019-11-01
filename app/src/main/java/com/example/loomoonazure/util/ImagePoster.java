package com.example.loomoonazure.util;

import android.media.Image;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.net.ssl.HttpsURLConnection;

public class ImagePoster implements Runnable {
    private static final String TAG = "ImagePoster";

    private final Image image;
    private final Robot robot;
    private JsonParser parser = new JsonParser();

    ImagePoster(Image image, Robot robot) {
        Log.d(TAG, String.format("constructor threadId=%d", Thread.currentThread().getId()));
        this.image = image;
        this.robot = robot;
    }

    @Override
    public void run() {
        Log.d(TAG, String.format("run threadId=%d", Thread.currentThread().getId()));

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        DataOutputStream dos = null;
        try {
            URL url = new URL("https://<webapp name>.azurewebsites.net/api/recognize");
            HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "image/jpeg");

            OutputStream out = conn.getOutputStream();
            out.write(bytes);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            int responseCode = conn.getResponseCode();
            String msg = conn.getResponseMessage();

            if (responseCode >= 200 && responseCode < 300) {
                JsonElement element = parser.parse(sb.toString());
                if (element.isJsonObject()) {
                    JsonObject results = element.getAsJsonObject();
                    if (results.has("faces")) {
                        JsonArray faces = results.getAsJsonArray("faces");
                        if (faces.size() == 0) {
                            robot.takePicture();
                        }
                    }
                }
            }

            Log.d(TAG, String.format("response = %d %s", responseCode, msg));
        } catch (Exception e) {
            Log.d(TAG, "Exception PUTing", e);
        } finally {
            image.close();
        }
    }
}
