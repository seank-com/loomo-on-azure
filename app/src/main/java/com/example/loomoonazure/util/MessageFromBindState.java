package com.example.loomoonazure.util;

import com.segway.robot.sdk.base.bind.ServiceBinder;

import android.os.Handler;
import android.util.Log;

public class MessageFromBindState implements ServiceBinder.BindStateListener {
    private static final String TAG = "MessageFromBindState";

    private Handler handler;
    private int bind;
    private int unbind;

    public MessageFromBindState(Handler handler, int bind, int unbind) {
        Log.d(TAG, String.format("constructor handler=%s bind=%d unbind=%d threadId=%d", handler.toString(), bind, unbind, Thread.currentThread().getId()));

        this.handler = handler;
        this.bind = bind;
        this.unbind = unbind;
    }

    @Override
    public void onBind() {
        Log.d(TAG, String.format("onBind bind=%d threadId=%d", bind, Thread.currentThread().getId()));

        handler.sendEmptyMessage(bind);
    }

    @Override
    public void onUnbind(String reason) {
        Log.d(TAG, String.format("onUnbind unbind=%d threadId=%d", unbind, Thread.currentThread().getId()));

        handler.sendEmptyMessage(unbind);
    }
}
