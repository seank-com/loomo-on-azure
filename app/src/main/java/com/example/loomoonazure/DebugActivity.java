package com.example.loomoonazure;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class DebugActivity extends AppCompatActivity implements Handler.Callback {

    private static final String TAG = "DebugActivity";

    private Handler handler;

    public static final int ACTION_PRINT = 1;

    @Override
    public boolean handleMessage(@NonNull android.os.Message msg) {
        long tid = Thread.currentThread().getId();
        switch(msg.what) {
            case ACTION_PRINT:
                String strMsg = msg.obj.toString();
                Log.d(TAG, String.format("handleMessage what=ACTION_PRINT msg=%s threadId=%d", strMsg, tid));
                break;
            default:
                Log.d(TAG, String.format("handleMessage what=default threadId=%d", tid));
                return false;
        }
        return true;
    }

    public void print(String txt) {
        Log.d(TAG, String.format("print threadId=%d", Thread.currentThread().getId()));

        android.os.Message msg = handler.obtainMessage(ACTION_PRINT, txt);
        handler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        Log.d(TAG, String.format("onCreate threadId=%d", Thread.currentThread().getId()));

        handler = new Handler(this);

        print("\n\nonCreate");
    }

}
