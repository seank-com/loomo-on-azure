package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.segway.robot.sdk.perception.sensor.Sensor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int FORWARD = 0;
    private static final int BACK = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;

    private int baseDirection = FORWARD;

    private Head robotHead;
    private Base robotBase;
    private Sensor robotSensor;
    private boolean isBindHead = false;
    private boolean isBindBase = false;
    private boolean isBindSensor = false;

    private final String connString = "[device connection string]";
    private boolean isConnected = false;
    private DeviceClient client;
    private MessageCallback callback = new MessageCallback();

    private Telemetry telemetry;

    TextView output;
    ScrollView container;
    Button direction;

    Timer timer = null;
    TimerTask timerTask = null;

    public enum ServiceType {
        BASE, HEAD, SENSOR
    }

    private void initRobot() {
        // only schedule the timer when all services are bound
        if (isBindHead && isBindBase && isBindSensor && isConnected) {

            robotHead.resetOrientation();
            robotHead.setMode(Head.MODE_SMOOTH_TACKING);
            robotBase.setControlMode(Base.CONTROL_MODE_RAW);

            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isBindHead && isBindBase && isConnected) {
                                String msgStr = telemetry.getMessage();
                                Message msg = new Message(msgStr);
                                msg.setMessageId(UUID.randomUUID().toString());
                                //print("Sending: " + msgStr);
                                client.sendEventAsync(msg, null, null);
                            }
                        }
                    });
                }
            };

            timer.schedule(timerTask, 5000, 5000);
        }
    }

    class RobotBindStateListener implements ServiceBinder.BindStateListener {
        private ServiceType service;

        public RobotBindStateListener(ServiceType service)
        {
            this.service = service;
        }

        @Override
        public void onBind() {
            switch(service) {
                case HEAD:
                    print("Bind Head");
                    isBindHead = true;
                    initRobot();
                    break;
                case BASE:
                    print("Bind Base");
                    isBindBase = true;
                    initRobot();
                    break;
                case SENSOR:
                    print("Bind Sensor");
                    isBindSensor = true;
                    initRobot();
                    break;
            }
        }

        @Override
        public void onUnbind(String reason) {
            switch(service) {
                case HEAD:
                    print("Unbind Head");
                    isBindHead = false;
                    break;
                case BASE:
                    print("Unbind Base");
                    isBindBase = false;
                    break;
                case SENSOR:
                    print("Unbind Sensor");
                    isBindSensor = false;
                    break;
            }
        }
    }

    public void print(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String out = output.getText().toString();
                out += "\n" + msg;
                output.setText(out);

                int height = output.getHeight();
                container.scrollTo(0, height);
            }
        });
    }

    static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        JsonParser parser = new JsonParser();

        public IotHubMessageResult execute(Message msg, Object context) {
            String strMsg = new String(msg.getBytes());
            JsonElement element = parser.parse(strMsg);

            if (element.isJsonObject()) {
                JsonObject command = element.getAsJsonObject();
                if (command.has("type")) {
                    if (command.get("type").getAsString().equals("move")) {
                        float linear = command.get("linear").getAsFloat();
                        float angular = command.get("angular").getAsFloat();

                        MainActivity main = (MainActivity)context;
                        if (main.robotBase.isBind()) {
                            main.robotBase.setLinearVelocity(linear);
                            main.robotBase.setAngularVelocity(angular);
                        }
                    }
                    else if (command.get("type").getAsString().equals("look")) {
                        float yaw = command.get("yaw").getAsFloat();
                        float pitch = command.get("pitchr").getAsFloat();

                        MainActivity main = (MainActivity)context;
                        if (main.robotHead.isBind()) {
                            main.robotHead.setWorldYaw(yaw);
                            main.robotHead.setWorldPitch(pitch);
                        }
                    }

                }
            }
            return IotHubMessageResult.COMPLETE;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView)findViewById(R.id.output);
        container = (ScrollView)findViewById(R.id.container);
        direction = (Button)findViewById(R.id.demo);

        output.setText("");
        print("onCreate");

        robotBase = Base.getInstance();
        robotHead = Head.getInstance();
        robotSensor = Sensor.getInstance();

        robotBase.bindService(getApplicationContext(), bindStateListenerBase);
        robotHead.bindService(getApplicationContext(), bindStateListenerHead);
        robotSensor.bindService(getApplicationContext(), bindStateListenerSensor);
        telemetry = new Telemetry(robotBase, robotHead, robotSensor);

        try {
            client = new DeviceClient(connString, IotHubClientProtocol.MQTT);
            client.open();
            client.setMessageCallback(callback, this);
            isConnected = true;
            print("Connected to IoTHub");
            initRobot();
        } catch(Exception e) {
            print("Exception opening IoTHub connection: " + e.getMessage() + e.toString());
            try {
                client.closeNow();
            } catch(Exception e2) {

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        print("onDestroy");

        robotHead.unbindService();
        robotBase.unbindService();
        robotSensor.unbindService();
        isConnected = false;
        try {
            client.closeNow();
        } catch(Exception e) {

        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear:
                output.setText("");
                break;
            case R.id.move:
                Intent intent = new Intent(this, MoveActivity.class);
                startActivity(intent);
                break;
            case R.id.reset:
                if (isBindBase) {
                    robotBase.cleanOriginalPoint();
                    Pose2D pose2D = robotBase.getOdometryPose(-1);
                    robotBase.setOriginalPoint(pose2D);
                }
                break;
            case R.id.demo:
                break;
        }
    }
}
