package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private float headYaw = 0;
    private float headPitch = 0;
    private float headYawDelta = 0.1f;
    private float headPitchDelta = 0.1f;
    private float maxYawRadians = 1.5708f;
    private float maxPitchRadians = 0.7854f;

    private static final int FORWARD = 0;
    private static final int BACK = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;

    private int baseDirection = FORWARD;

    private Head robotHead;
    private boolean isHeadBind = false;
    private Base robotBase;
    private boolean isBaseBind = false;

    TextView output;
    ScrollView container;
    Button process;
    Button direction;

    Timer timer = null;
    TimerTask timerTask = null;

    private void scheduleTimer() {
        if (isHeadBind && isBaseBind) {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            float yaw = 0;
                            float pitch = 0;
                            float angularVelocity = 0;
                            float linearVelocity = 0;

                            if (isHeadBind) {
                                yaw = robotHead.getWorldYaw().getAngle();
                                pitch = robotHead.getWorldPitch().getAngle();

                                print("DETECT yaw: " + String.valueOf(yaw) + ", pitch: " + String.valueOf(pitch));

                                headYaw += headYawDelta;
                                if (Math.abs(headYaw) > maxYawRadians) {
                                    headYawDelta *= -1;
                                    headYaw = (headYaw / Math.abs(headYaw)) * maxYawRadians;
                                }

                                headPitch += headPitchDelta;
                                if (Math.abs(headPitch) > maxPitchRadians) {
                                    headPitchDelta *= -1;
                                    headPitch = (headPitch / Math.abs(headPitch)) * maxPitchRadians;
                                }

                                print("SET yaw: " + String.valueOf(headYaw) + ", pitch: " + String.valueOf(headPitch));

                                robotHead.setWorldYaw(headYaw);
                                robotHead.setWorldPitch(headPitch);
                            }

                            if (isBaseBind) {
                                angularVelocity = robotBase.getAngularVelocity().getSpeed();
                                linearVelocity = robotBase.getLinearVelocity().getSpeed();

                                print("DETECT angularVelocity: " + String.valueOf(angularVelocity) + ", linearVelocity: " + String.valueOf(linearVelocity));
                            }
                        }
                    });
                }
            };

            timer.schedule(timerTask, 250, 250);
            process.setText(R.string.process_stop);
            process.setEnabled(true);
        }
    }

    private void unscheduleTimer() {
        if (!isHeadBind && !isBaseBind) {
            process.setText(R.string.process_start);
            process.setEnabled(true);
        }
    }

    private ServiceBinder.BindStateListener headServiceBindListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            print("Bind Head");
            isHeadBind = true;
            robotHead.setMode(Head.MODE_SMOOTH_TACKING);
            robotHead.resetOrientation();
            scheduleTimer();
        }
        @Override
        public void onUnbind(String reason) {
            print("Unbind Head");
            isHeadBind = false;
            unscheduleTimer();
        }
    };

    private ServiceBinder.BindStateListener baseServiceBindListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            print("Bind Base");
            isBaseBind = true;
            scheduleTimer();
        }
        @Override
        public void onUnbind(String reason) {
            print("Unbind Base");
            isBaseBind = false;
            unscheduleTimer();
        }
    };

    public void print(String msg) {
        String out = output.getText().toString();
        out += "\n" + msg;
        output.setText(out);

        int height = output.getHeight();
        container.scrollTo(0, height);
    }

    private void startProcess() {
        robotHead.bindService(getApplicationContext(), headServiceBindListener);
        robotBase.bindService(getApplicationContext(), baseServiceBindListener);
    }

    private void stopProcess() {
        robotHead.unbindService();
        robotBase.unbindService();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void move(final float linear, final float angular) {
        print("move(" + String.valueOf(linear) + ", " + String.valueOf(angular) + ")");
        new Thread()  {
            @Override
            public void run() {
                robotBase.setLinearVelocity(linear);
                robotBase.setAngularVelocity(angular);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    print(e.getMessage());
                }

                robotBase.setLinearVelocity(0);
                robotBase.setAngularVelocity(0);
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView)findViewById(R.id.output);
        container = (ScrollView)findViewById(R.id.container);
        process = (Button)findViewById(R.id.process);
        direction = (Button)findViewById(R.id.direction);

        output.setText("");
        print("onCreate");

        robotHead = Head.getInstance();
        robotBase = Base.getInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        print("onDestroy");

        stopProcess();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.process:
                process.setEnabled(false);
                if (!isHeadBind && !isBaseBind) {
                    startProcess();
                } else {
                    stopProcess();
                }
                break;
            case R.id.clear:
                output.setText("");
                break;
            case R.id.direction:
                switch (baseDirection) {
                    case FORWARD:
                        baseDirection = BACK;
                        direction.setText(R.string.direction_back);
                        break;
                    case BACK:
                        baseDirection = LEFT;
                        direction.setText(R.string.direction_left);
                        break;
                    case LEFT:
                        baseDirection = RIGHT;
                        direction.setText(R.string.direction_right);
                        break;
                    case RIGHT:
                        baseDirection = FORWARD;
                        direction.setText(R.string.direction_forward);
                        break;
                }
                break;
            case R.id.move:
                if (isBaseBind) {
                    switch (baseDirection) {
                        case FORWARD:
                            move(1f, 0);
                            break;
                        case BACK:
                            move(-1f, 0);
                            break;
                        case LEFT:
                            move(0, 1f);
                            break;
                        case RIGHT:
                            move(0, -1f);
                            break;
                    }
                }
                break;
        }
    }
}
