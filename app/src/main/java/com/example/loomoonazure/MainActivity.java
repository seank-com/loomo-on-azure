package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LocationListener, MessageCallback {

    private Base robotBase;
    private Head robotHead;
    private Sensor robotSensor;
    private Vision robotVision;
    private Recognizer robotRecognizer;
    private Speaker robotSpeaker;
    private boolean isBindBase = false;
    private boolean isBindHead = false;
    private boolean isBindSensor = false;
    private boolean isBindVision = false;
    private boolean isBindRecognizer = false;
    private boolean isBindSpeaker = false;

    private final String connString = "[device connection string]";
    private boolean isConnected = false;
    private DeviceClient client;

    private Telemetry telemetry;

    private Location lastLocation;

    private RobotBroadcastReceiver broadcastReceiver;

    private Button move;
    private Button navigation;
    private Button vision;
    private Button speech;
    private Button reset;
    private Button demo;
    private TextView output;
    private TextView msgCount;
    private ScrollView container;

    private int messages = 0;
    private Timer timer = null;
    private TimerTask timerTask = null;
    private JsonParser parser = new JsonParser();

    private Timer rocosTimer = null;
    private TimerTask rocosTimerTask = null;
    private Socket rocosSocket = null;

    private final String BATTERY_CHANGED = "com.segway.robot.action.BATTERY_CHANGED";
    private final String POWER_DOWN = "com.segway.robot.action.POWER_DOWN";
    private final String POWER_BUTTON_PRESSED = "com.segway.robot.action.POWER_BUTTON_PRESSED";
    private final String POWER_BUTTON_RELEASED = "com.segway.robot.action.POWER_BUTTON_RELEASED";
    private final String TO_SBV = "com.segway.robot.action.TO_SBV";
    private final String TO_ROBOT = "com.segway.robot.action.TO_ROBOT";
    private final String PITCH_LOCK = "com.segway.robot.action.PITCH_LOCK";
    private final String PITCH_UNLOCK = "com.segway.robot.action.PITCH_UNLOCK";
    private final String YAW_LOCK = "com.segway.robot.action.YAW_LOCK";
    private final String YAW_UNLOCK = "com.segway.robot.action.YAW_UNLOCK";
    private final String STEP_ON = "com.segway.robot.action.STEP_ON";
    private final String STEP_OFF = "com.segway.robot.action.STEP_OFF";
    private final String LIFT_UP = "com.segway.robot.action.LIFT_UP";
    private final String PUT_DOWN = "com.segway.robot.action.PUT_DOWN";
    private final String PUSHING = "com.segway.robot.action.PUSHING";
    private final String PUSH_RELEASE = "com.segway.robot.action.PUSH_RELEASE";
    private final String BASE_LOCK = "com.segway.robot.action.BASE_LOCK";
    private final String BASE_UNLOCK = "com.segway.robot.action.BASE_UNLOCK";
    private final String STAND_UP = "com.segway.robot.action.STAND_UP";

    // Unfortunately the BroadcastReceiver is not an interface
    private class RobotBroadcastReceiver extends BroadcastReceiver {
        private IntentFilter filter;

        public RobotBroadcastReceiver() {

            filter = new IntentFilter();
            filter.addAction(BATTERY_CHANGED);
            filter.addAction(POWER_DOWN);
            filter.addAction(POWER_BUTTON_PRESSED);
            filter.addAction(POWER_BUTTON_RELEASED);
            filter.addAction(TO_SBV);
            filter.addAction(TO_ROBOT);
            filter.addAction(PITCH_LOCK);
            filter.addAction(PITCH_UNLOCK);
            filter.addAction(YAW_LOCK);
            filter.addAction(YAW_UNLOCK);
            filter.addAction(STEP_ON);
            filter.addAction(STEP_OFF);
            filter.addAction(LIFT_UP);
            filter.addAction(PUT_DOWN);
            filter.addAction(PUSHING);
            filter.addAction(PUSH_RELEASE);
            filter.addAction(BASE_LOCK);
            filter.addAction(BASE_UNLOCK);
            filter.addAction(STAND_UP);
        }

        public IntentFilter getFilter() {
            return filter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BATTERY_CHANGED:
                    print("Received BATTERY_CHANGED");
                    break;
                case POWER_DOWN:
                    print("Received POWER_DOWN");
                    break;
                case POWER_BUTTON_PRESSED:
                    print("Received POWER_BUTTON_PRESSED");
                    break;
                case POWER_BUTTON_RELEASED:
                    print("Received POWER_BUTTON_RELEASED");
                    break;
                case TO_SBV:
                    print("Received TO_SBV");
                    break;
                case TO_ROBOT:
                    print("Received TO_ROBOT");
                    break;
                case PITCH_LOCK:
                    print("Received PITCH_LOCK");
                    break;
                case PITCH_UNLOCK:
                    print("Received PITCH_UNLOCK");
                    break;
                case YAW_LOCK:
                    print("Received YAW_LOCK");
                    break;
                case YAW_UNLOCK:
                    print("Received YAW_UNLOCK");
                    break;
                case STEP_ON:
                    print("Received STEP_ON");
                    break;
                case STEP_OFF:
                    print("Received STEP_OFF");
                    break;
                case LIFT_UP:
                    print("Received LIFT_UP");
                    break;
                case PUT_DOWN:
                    print("Received PUT_DOWN");
                    break;
                case PUSHING:
                    print("Received PUSHING");
                    break;
                case PUSH_RELEASE:
                    print("Received PUSH_RELEASE");
                    break;
                case BASE_LOCK:
                    print("Received BASE_LOCK");
                    break;
                case BASE_UNLOCK:
                    print("Received BASE_UNLOCK");
                    break;
                case STAND_UP:
                    print("Received STAND_UP");
                    break;
            }
        }
    }

    private enum ServiceType {
        BASE, HEAD, SENSOR, VISION, RECOGNIZER, SPEAKER
    }

    private class RobotBindStateListener implements ServiceBinder.BindStateListener {

        private ServiceType service;

        public RobotBindStateListener(ServiceType service) {
            this.service = service;
        }

        @Override
        public void onBind() {
            switch(service) {
                case BASE:
                    print("Bind Base");
                    isBindBase = true;
                    initRobot();
                    break;
                case HEAD:
                    print("Bind Head");
                    isBindHead = true;
                    initRobot();
                    break;
                case SENSOR:
                    print("Bind Sensor");
                    isBindSensor = true;
                    initRobot();
                    break;
                case VISION:
                    print("Bind Vision");
                    isBindVision = true;
                    initRobot();
                    break;
                case RECOGNIZER:
                    print("Bind Recognizer");
                    isBindRecognizer = true;
                    initRobot();
                    break;
                case SPEAKER:
                    print("Bind Speaker");
                    isBindSpeaker = true;
                    initRobot();
                    break;
            }
        }

        @Override
        public void onUnbind(String reason) {
            switch(service) {
                case BASE:
                    print("Unbind Base");
                    isBindBase = false;
                    break;
                case HEAD:
                    print("Unbind Head");
                    isBindHead = false;
                    break;
                case SENSOR:
                    print("Unbind Sensor");
                    isBindSensor = false;
                    break;
                case VISION:
                    print("Unbind Vision");
                    isBindVision = false;
                    break;
                case RECOGNIZER:
                    print("Unbind Recognizer");
                    isBindRecognizer = false;
                    break;
                case SPEAKER:
                    print("Unbind Speaker");
                    isBindSpeaker = false;
                    break;
            }
        }
    }

    private void initRobot() {
        // only schedule the timer when all services are bound
        if (isBindBase && isBindHead && isBindSensor &&
            isBindVision && isBindRecognizer && isBindSpeaker &&
            isConnected) {

            print("Zeroing Robot");
            robotHead.setMode(Head.MODE_SMOOTH_TACKING);
            robotHead.resetOrientation();
            robotBase.setControlMode(Base.CONTROL_MODE_RAW);

            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isBindBase && isBindHead && isBindSensor && isConnected) {
                                String msgStr = telemetry.getMessage();
                                Message msg = new Message(msgStr);
                                msg.setMessageId(UUID.randomUUID().toString());
                                messages += 1;
                                updateMessageCount();
                                client.sendEventAsync(msg, null, null);
                            }
                        }
                    });
                }
            };

            timer.schedule(timerTask, 5000, 5000);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    move.setEnabled(true);
                    navigation.setEnabled(true);
                    vision.setEnabled(true);
                    speech.setEnabled(true);
                    reset.setEnabled(true);
                    demo.setEnabled(true);
                }
            });
        }
    }

    private void establishSocketConnection(String server, int port, int cadence) {
        if (rocosSocket == null) {
            rocosSocket = new Socket();
            new Thread()  {
                @Override
                public void run() {
                    try {
                        InetAddress serverAddr = InetAddress.getByName(server);
                        rocosSocket = new Socket(serverAddr, port);
                        print(String.format("Socket created to %s:%d", server, port));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    rocosTimer = new Timer();
                    rocosTimerTask = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                OutputStream out = rocosSocket.getOutputStream();
                                JsonObject jo = new JsonObject();
                                Pose2D pose = robotBase.getOdometryPose(-1);
                                jo.addProperty("theta", pose.getTheta());
                                jo.addProperty("x", pose.getX());
                                jo.addProperty("y", pose.getY());
                                out.write(jo.toString().getBytes());
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    rocosTimer.schedule(rocosTimerTask, cadence, cadence);
                }
            }.start();
        }
    }

    // Implement LocationListener
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        print("Got new location: " + lastLocation.toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        print("Status changed: " + s);
    }

    @Override
    public void onProviderEnabled(String s) {
        print("Provider Enabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        print("Provider Disabled: " + s);
    }

    // Implement MessageCallback
    @Override
    public IotHubMessageResult execute(Message message, Object callbackContext) {
        String strMsg = new String(message.getBytes());
        JsonElement element = parser.parse(strMsg);

        if (element.isJsonObject()) {
            JsonObject command = element.getAsJsonObject();
            if (command.has("type")) {
                if (command.get("type").getAsString().equals("move")) {
                    float linear = command.get("linear").getAsFloat();
                    float angular = command.get("angular").getAsFloat();

                    if (robotBase.isBind()) {
                        robotBase.setLinearVelocity(linear);
                        robotBase.setAngularVelocity(angular);
                    }
                }
                else if (command.get("type").getAsString().equals("look")) {
                    float yaw = command.get("yaw").getAsFloat();
                    float pitch = command.get("pitch").getAsFloat();

                    if (robotHead.isBind()) {
                        robotHead.setWorldYaw(yaw);
                        robotHead.setWorldPitch(pitch);
                    }
                }
                else if (command.get("type").getAsString().equals("socket")) {
                    String server = command.get("address").getAsString();
                    int port = command.get("port").getAsInt();
                    int cadence = command.get("cadence").getAsInt();
                    establishSocketConnection(server, port, cadence);
                }
            }
        }
        return IotHubMessageResult.COMPLETE;
    }

    public void updateMessageCount() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgCount.setText(String.format("%d Msgs", messages));
            }
        });
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        move = (Button)findViewById(R.id.move);
        navigation = (Button)findViewById(R.id.navigation);
        vision = (Button)findViewById(R.id.vision);
        speech = (Button)findViewById(R.id.speech);
        reset = (Button)findViewById(R.id.reset);
        demo = (Button)findViewById(R.id.demo);
        output = (TextView)findViewById(R.id.output);
        container = (ScrollView)findViewById(R.id.container);
        msgCount = (TextView)findViewById(R.id.msgCount);

        move.setEnabled(false);
        navigation.setEnabled(false);
        vision.setEnabled(false);
        speech.setEnabled(false);
        reset.setEnabled(false);
        demo.setEnabled(false);
        output.setText("");
        print("\n\nonCreate");

        robotBase = Base.getInstance();
        robotHead = Head.getInstance();
        robotSensor = Sensor.getInstance();
        robotVision = Vision.getInstance();
        robotRecognizer = Recognizer.getInstance();
        robotSpeaker = Speaker.getInstance();

        robotBase.bindService(getApplicationContext(), new RobotBindStateListener(ServiceType.BASE));
        robotHead.bindService(getApplicationContext(), new RobotBindStateListener(ServiceType.HEAD));
        robotSensor.bindService(getApplicationContext(), new RobotBindStateListener(ServiceType.SENSOR));
        robotVision.bindService(getApplicationContext(), new RobotBindStateListener(ServiceType.VISION));
        robotRecognizer.bindService(getApplicationContext(), new RobotBindStateListener(ServiceType.RECOGNIZER));
        robotSpeaker.bindService(getApplicationContext(), new RobotBindStateListener(ServiceType.SPEAKER));

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            print(lastLocation.toString());
        }

        broadcastReceiver = new RobotBroadcastReceiver();
        getApplicationContext().registerReceiver(broadcastReceiver, broadcastReceiver.getFilter());

        telemetry = new Telemetry(robotBase, robotHead, robotSensor);

        try {
            client = new DeviceClient(connString, IotHubClientProtocol.MQTT);
            client.open();
            client.setMessageCallback(this, null);
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

        getApplicationContext().unregisterReceiver(broadcastReceiver);
        robotHead.unbindService();
        robotBase.unbindService();
        robotSensor.unbindService();
        robotVision.unbindService();
        robotRecognizer.unbindService();
        robotSpeaker.unbindService();

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
        Intent intent = null;
        switch (view.getId()) {
            case R.id.move:
                intent = new Intent(this, MoveActivity.class);
                startActivity(intent);
                break;
            case R.id.navigation:
                intent = new Intent(this, NavigationActivity.class);
                startActivity(intent);
                break;
            case R.id.vision:
                intent = new Intent(this, VisionActivity.class);
                startActivity(intent);
                break;
            case R.id.speech:
                intent = new Intent(this, SpeechActivity.class);
                startActivity(intent);
                break;
            case R.id.clear:
                output.setText("");
                break;
            case R.id.reset:
                if (isBindBase) {
                    robotBase.cleanOriginalPoint();
                    Pose2D pose2D = robotBase.getOdometryPose(-1);
                    robotBase.setOriginalPoint(pose2D);
                }
                break;
            case R.id.demo:
                intent = new Intent(this, DemoActivity.class);
                startActivity(intent);
                break;
        }
    }
}
