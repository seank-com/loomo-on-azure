package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.head.Head;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private float headYaw = 0;
    private float headPitch = 0;
    private float headYawDelta = 0.1f;
    private float headPitchDelta = 0.03f;
    private float maxRadians = 1.5708f;

    Head robotHead;
    boolean isHeadBind = false;

    TextView output;
    ScrollView container;

    Timer headTimer = new Timer();
    TimerTask headTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isHeadBind) {
                        float yaw = robotHead.getWorldYaw().getAngle();
                        float pitch = robotHead.getWorldPitch().getAngle();

                        print("yaw: " + String.valueOf(yaw) + ", pitch: " + String.valueOf(pitch));

                        headYaw += headYawDelta;
                        headPitch += headPitchDelta;
                        if (headYaw > maxRadians) {
                            headYawDelta *= -1;
                            headYaw = maxRadians;
                        }
                        if (headYaw < -maxRadians) {
                            headYawDelta *= -1;
                            headYaw = -maxRadians;
                        }

                        if (headPitch > maxRadians) {
                            headPitchDelta *= -1;
                            headPitch = maxRadians;
                        }
                        if (headPitch < -maxRadians) {
                            headPitchDelta *= -1;
                            headPitch = -maxRadians;
                        }

                        robotHead.setWorldYaw(headYaw);
                        robotHead.setWorldPitch(headPitch);
                    }
                }
            });
        }
    };

    private ServiceBinder.BindStateListener headServiceBindListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            isHeadBind = true;
            robotHead.setMode(Head.MODE_SMOOTH_TACKING);
            robotHead.resetOrientation();

            headTimer.schedule(headTimerTask, 250, 250);
        }
        @Override
        public void onUnbind(String reason) {
            isHeadBind = false;
        }
    };

    private void print(String msg) {
        String out = output.getText().toString();
        out += "\n" + msg;
        output.setText(out);

        int height = output.getHeight();
        container.scrollTo(0, height);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView)findViewById(R.id.output);
        container = (ScrollView)findViewById(R.id.container);

        print("This is a test");
        print("This is another test");

        robotHead = Head.getInstance();

        robotHead.bindService(getApplicationContext(), headServiceBindListener);
    }

    @Override
    protected void onDestroy() {
        robotHead.unbindService();
        if (headTimer != null) {
            headTimer.cancel();
            headTimer = null;
        }
        if (headTimerTask != null) {
            headTimerTask.cancel();
            headTimerTask = null;
        }
    }
}
