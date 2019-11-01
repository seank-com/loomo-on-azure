package com.example.loomoonazure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.loomoonazure.util.AzureIoT;
import com.example.loomoonazure.util.MessageFromBindState;
import com.example.loomoonazure.util.Robot;
import com.example.loomoonazure.util.RobotAction;
import com.example.loomoonazure.util.RobotCamera;
import com.example.loomoonazure.util.RobotConversation;
import com.example.loomoonazure.util.RobotTracking;
import com.example.loomoonazure.util.TeleOps;
import com.example.loomoonazure.util.Telemetry;
import com.segway.robot.sdk.emoji.BaseControlHandler;
import com.segway.robot.sdk.emoji.Emoji;
import com.segway.robot.sdk.emoji.EmojiPlayListener;
import com.segway.robot.sdk.emoji.EmojiView;
import com.segway.robot.sdk.emoji.HeadControlHandler;
import com.segway.robot.sdk.emoji.player.RobotAnimator;
import com.segway.robot.sdk.emoji.player.RobotAnimatorFactory;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;

import java.util.LinkedList;
import java.util.Random;

public class MainActivity
        extends AppCompatActivity
        implements Handler.Callback, View.OnClickListener, EmojiPlayListener, BaseControlHandler, HeadControlHandler, RobotTracking.BaseControlHandler, RobotTracking.HeadControlHandler, Robot {
    private static final String TAG = "MainActivity";

    private Base robotBase;
    private boolean isBindBase = false;
    private static final int SERVICE_BIND_BASE = 1;
    private static final int SERVICE_UNBIND_BASE = 2;

    private Head robotHead;
    private boolean isBindHead = false;
    private static final int SERVICE_BIND_HEAD = 3;
    private static final int SERVICE_UNBIND_HEAD = 4;

    private Recognizer robotRecognizer;
    private boolean isBindRecognizer = false;
    private static final int SERVICE_BIND_RECOGNIZER = 5;
    private static final int SERVICE_UNBIND_RECOGNIZER = 6;

    private Sensor robotSensor;
    private boolean isBindSensor = false;
    private static final int SERVICE_BIND_SENSOR = 7;
    private static final int SERVICE_UNBIND_SENSOR = 8;

    private Speaker robotSpeaker;
    private boolean isBindSpeaker = false;
    private static final int SERVICE_BIND_SPEAKER = 9;
    private static final int SERVICE_UNBIND_SPEAKER = 10;

    private Vision robotVision;
    private boolean isBindVision = false;
    private static final int SERVICE_BIND_VISION = 11;
    private static final int SERVICE_UNBIND_VISION = 12;

    private Emoji robotEmoji;
    private EmojiView emojiView;

    private Telemetry telemetry;

    private AzureIoT connection;
    private TeleOps teleops;
    private boolean isIoTCentral = true;
    private final String connString = "[device connection string]";
    private static final int CONNECTION_OPEN = 15;
    private static final int CONNECTION_CLOSED = 16;
    private boolean isConnected = false;

    private static final int ACTION_SEND_TELEMETRY = 17;
    private static final int ACTION_SEND_STATE = 13;
    private static final int ACTION_SEND_LOCATION = 14;

    private static final int ACTION_BEHAVE = 18;
    private static final int ACTION_MOVE = 19;
    private static final int ACTION_LOOK = 20;

    private RobotConversation robotConversation;
    private RobotTracking robotTracking;
    private RobotCamera robotCamera;
    
    private Handler handler;

    private int cadence = 5;
    private int timeout = 0;

    private Location lastLocation = null;
    private String lastState = "";

    private int peopleDetected = 0;
    private int messages = 0;
    private boolean debug = false;
    private boolean monitor = false;

    private boolean intro = true;
    private boolean canMove = true;

    private Random rand = new Random();
    //
    // TEST TEST TEST TEST TEST
    //
    private boolean integrateCameraWithTracking = true;

    RobotAction currentAction = null;
    RobotAction resumeAction = null;

    private LinkedList<RobotAction> actions = new LinkedList<RobotAction>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Log.d(TAG, String.format("onCreate threadId=%d", Thread.currentThread().getId()));

        emojiView = (EmojiView)findViewById(R.id.face);
        emojiView.setOnClickListener(this);

        handler = new Handler(this);
        robotConversation = new RobotConversation(this);
        robotCamera = new RobotCamera(this);
        if (integrateCameraWithTracking) {
        robotTracking = new RobotTracking(this, robotCamera, this, this);
        } else {
            robotTracking = new RobotTracking(this, null, this, this);
        }

        robotBase = Base.getInstance();
        robotEmoji = Emoji.getInstance();
        robotHead = Head.getInstance();
        robotSensor = Sensor.getInstance();
        robotRecognizer = Recognizer.getInstance();
        robotSpeaker = Speaker.getInstance();
        robotVision = Vision.getInstance();

        robotBase.bindService(getApplicationContext(), new MessageFromBindState(handler, SERVICE_BIND_BASE, SERVICE_UNBIND_BASE));
        robotHead.bindService(getApplicationContext(), new MessageFromBindState(handler, SERVICE_BIND_HEAD, SERVICE_UNBIND_HEAD));
        robotRecognizer.bindService(getApplicationContext(), new MessageFromBindState(handler, SERVICE_BIND_RECOGNIZER, SERVICE_UNBIND_RECOGNIZER));
        robotSensor.bindService(getApplicationContext(), new MessageFromBindState(handler, SERVICE_BIND_SENSOR, SERVICE_UNBIND_SENSOR));
        robotSpeaker.bindService(getApplicationContext(), new MessageFromBindState(handler, SERVICE_BIND_SPEAKER, SERVICE_UNBIND_SPEAKER));
        robotVision.bindService(getApplicationContext(), new MessageFromBindState(handler, SERVICE_BIND_VISION, SERVICE_UNBIND_VISION));

        telemetry = new Telemetry(this);
        telemetry.registerEvents(this);

        connection = new AzureIoT(handler, CONNECTION_OPEN, CONNECTION_CLOSED, this);
        connection.connect(connString, isIoTCentral);

        teleops = new TeleOps(this, telemetry);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d(TAG, String.format("onRestart threadId=%d", Thread.currentThread().getId()));

        if (isBindBase && isBindHead && isBindSensor && isConnected) {
            timeout = cadence * 1000;
            handler.sendEmptyMessageDelayed(ACTION_SEND_TELEMETRY, timeout);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, String.format("onStop threadId=%d", Thread.currentThread().getId()));

        timeout = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, String.format("onDestroy threadId=%d", Thread.currentThread().getId()));

        telemetry.unregisterEvents(this);

        isConnected = false;
        timeout = 0;
        connection.close();

        robotTracking.stopFollowing();
        robotTracking.stopTracking();
        robotCamera.stop();
        robotConversation.stop();

        robotBase.unbindService();
        robotHead.unbindService();
        robotRecognizer.unbindService();
        robotSensor.unbindService();
        robotSpeaker.unbindService();
        robotVision.unbindService();
    }

    private void initRobot() {
        Log.d(TAG, String.format("initRobot threadId=%d", Thread.currentThread().getId()));

        if (currentAction == null) {
            if (isBindBase && isBindHead && isBindRecognizer && isBindSensor && isBindSpeaker && isBindVision) {
                robotEmoji.init(this);
                robotEmoji.setEmojiView(emojiView);
                robotEmoji.setBaseControlHandler(this);
                robotEmoji.setHeadControlHandler(this);

                // robotTracking.startTracking calls robotCamera.start now
                if (integrateCameraWithTracking == false) {
                    robotCamera.start(null);
                }

                robotConversation.start();
                RobotAction ra = RobotAction.getTrack(Robot.TRACK_BEHAVIOR_WATCH);
                actionDo(ra);
            }
        }

        if (currentAction != null) {
            if (!isConnected) {
                if (debug) {
                actionSay("Offline systems operational.");
                }
            } else {
                timeout = cadence * 1000;
                handler.sendEmptyMessageDelayed(ACTION_SEND_TELEMETRY, timeout);
                if (debug) {
                actionSay("All systems operational.");
            }
        }
    }
    }

    // Handler.Callback
    @Override
    public boolean handleMessage(@NonNull Message message) {
        long tid = Thread.currentThread().getId();
        switch(message.what) {
            case SERVICE_BIND_BASE:
                Log.d(TAG, String.format("handleMessage what=SERVICE_BIND_BASE threadId=%d", tid));
                isBindBase = true;
                if (debug) {
                actionSay("Propulsion systems initialized.");
                }
                initRobot();
                break;
            case SERVICE_UNBIND_BASE:
                Log.d(TAG, String.format("handleMessage what=SERVICE_UNBIND_BASE threadId=%d", tid));
                isBindBase = false;
                break;
            case SERVICE_BIND_HEAD:
                Log.d(TAG, String.format("handleMessage what=SERVICE_BIND_HEAD threadId=%d", tid));
                isBindHead = true;
                if (debug) {
                actionSay("Degrees of freedom, check!");
                }
                initRobot();
                break;
            case SERVICE_UNBIND_HEAD:
                Log.d(TAG, String.format("handleMessage what=SERVICE_UNBIND_HEAD threadId=%d", tid));
                isBindHead = false;
                break;
            case SERVICE_BIND_RECOGNIZER:
                Log.d(TAG, String.format("handleMessage what=SERVICE_BIND_RECOGNIZER threadId=%d", tid));
                if (debug) {
                actionSay("Auditory systems initialized.");
                }
                isBindRecognizer = true;
                initRobot();
                break;
            case SERVICE_UNBIND_RECOGNIZER:
                Log.d(TAG, String.format("handleMessage what=SERVICE_UNBIND_RECOGNIZER threadId=%d", tid));
                isBindRecognizer = false;
                break;
            case SERVICE_BIND_SENSOR:
                Log.d(TAG, String.format("handleMessage what=SERVICE_BIND_SENSOR threadId=%d", tid));
                if (debug) {
                actionSay("Feelings, check!");
                }
                isBindSensor = true;
                initRobot();
                break;
            case SERVICE_UNBIND_SENSOR:
                Log.d(TAG, String.format("handleMessage what=SERVICE_UNBIND_SENSOR threadId=%d", tid));
                isBindSensor = false;
                break;
            case SERVICE_BIND_SPEAKER:
                Log.d(TAG, String.format("handleMessage what=SERVICE_BIND_SPEAKER threadId=%d", tid));
                if (debug) {
                actionSay("Vocalization systems initialized.");
                }
                isBindSpeaker = true;
                initRobot();
                break;
            case SERVICE_UNBIND_SPEAKER:
                Log.d(TAG, String.format("handleMessage what=SERVICE_UNBIND_SPEAKER threadId=%d", tid));
                isBindSpeaker = false;
                break;
            case SERVICE_BIND_VISION:
                Log.d(TAG, String.format("handleMessage what=SERVICE_BIND_VISION threadId=%d", tid));
                if (debug) {
                actionSay("Perception systems initialized.");
                }
                isBindVision = true;
                initRobot();
                break;
            case SERVICE_UNBIND_VISION:
                Log.d(TAG, String.format("handleMessage what=SERVICE_UNBIND_VISION threadId=%d", tid));
                isBindVision = false;
                break;
            case ACTION_SEND_STATE:
                Log.d(TAG, String.format("handleMessage what=STATE_CHANGED threadId=%d", tid));
                if (isBindBase && isBindHead && isBindSensor && isConnected) {
                    Log.d(TAG, "Sending State");
                    connection.sendMessage(telemetry.getState());
                    messages += 1;
                }
                break;
            case ACTION_SEND_LOCATION:
                Log.d(TAG, String.format("handleMessage what=LOCATION_CHANGED threadId=%d", tid));
                if (isBindBase && isBindHead && isBindSensor && isConnected) {
                    Log.d(TAG, "Sending Location");
                    connection.sendMessage(telemetry.getLocation());
                    messages += 1;
                }
                break;
            case CONNECTION_OPEN:
                Log.d(TAG, String.format("handleMessage what=CONNECTION_OPEN threadId=%d", tid));
                if (debug) {
                actionSay("Connection to cloud, check!");
                }
                isConnected = true;
                initRobot();
                break;
            case CONNECTION_CLOSED:
                Log.d(TAG, String.format("handleMessage what=CONNECTION_CLOSED threadId=%d", tid));
                if (debug) {
                actionSay("Connection to cloud has been lost");
                }
                isConnected = false;
                break;
            case ACTION_SEND_TELEMETRY:
                Log.d(TAG, String.format("handleMessage what=ACTION_SEND_TELEMETRY threadId=%d", tid));
                if (timeout > 0) {
                    if (isBindBase && isBindHead && isBindSensor && isConnected) {
                        connection.sendMessage(telemetry.getMessage());
                        messages += 1;
                    }
                    handler.sendEmptyMessageDelayed(ACTION_SEND_TELEMETRY, timeout);
                }
                break;
            case ACTION_BEHAVE:
                Log.d(TAG, String.format("handleMessage what=ACTION_BEHAVE threadId=%d", tid));
                try {
                    int behavior = (int)(Integer)message.obj;
                    RobotAnimator emotion = RobotAnimatorFactory.getReadyRobotAnimator(behavior);
                    robotEmoji.startAnimation(emotion, this);
                } catch (Exception e) {
                    Log.e(TAG, "Exception doing", e);
                    doNextAction(Robot.ACTION_TYPE_EMOTE);
                }
                break;
            case ACTION_MOVE:
                Log.d(TAG, String.format("DEBUG handleMessage what=ACTION_MOVE threadId=%d", tid));
                doNextAction(Robot.ACTION_TYPE_MOVE);
                break;
            case ACTION_LOOK:
                Log.d(TAG, String.format("handleMessage what=ACTION_LOOK threadId=%d", tid));
                doNextAction(Robot.ACTION_TYPE_LOOK);
                break;
            default:
                return false;
        }
        return true;
    }

    // View.OnClickListener
    @Override
    public void onClick(View view) {
        Log.d(TAG, String.format("onClick id=%d threadId=%d", view.getId(), Thread.currentThread().getId()));
        Intent intent = null;
        switch (view.getId()) {
            case R.id.face:
                //intent = new Intent(this, DebugActivity.class);
                //startActivity(intent);
//                if (currentAction != null && currentAction.getActionType() == Robot.ACTION_TYPE_TRACK) {
//                    int newBehavior = Robot.TRACK_BEHAVIOR_WATCH;
//                    if (currentAction.getBehavior() == Robot.TRACK_BEHAVIOR_WATCH) {
//                        newBehavior = Robot.TRACK_BEHAVIOR_FOLLOW;
//                    }
//
//                    RobotAction ra = RobotAction.getTrack(newBehavior);
//                    actionDo(ra);
//                }
//                robotCamera.takePicture();
                break;
            default:
                break;
        }
    }

    // EmojiPlayListener
    @Override
    public void onAnimationStart(RobotAnimator animator) {
        Log.d(TAG, String.format("onAnimationStart threadId=%d", Thread.currentThread().getId()));
    }

    @Override
    public synchronized void onAnimationEnd(RobotAnimator animator) {
        Log.d(TAG, String.format("onAnimationEnd threadId=%d", Thread.currentThread().getId()));
        doNextAction(Robot.ACTION_TYPE_EMOTE);
    }

    @Override
    public synchronized void onAnimationCancel(RobotAnimator animator) {
        Log.d(TAG, String.format("onAnimationCancel threadId=%d", Thread.currentThread().getId()));
        doNextAction(Robot.ACTION_TYPE_EMOTE);
    }

    // Emoji BaseControlHandler
    @Override
    public synchronized void setLinearVelocity(float velocity) {
        Log.d(TAG, String.format("setLinearVelocity threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotBase.setLinearVelocity(velocity);
    }
    }

    @Override
    public synchronized void setAngularVelocity(float velocity) {
        Log.d(TAG, String.format("setAngularVelocity threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotBase.setAngularVelocity(velocity);
    }
    }

    @Override
    public void stop() {
        Log.d(TAG, String.format("stop threadId=%d", Thread.currentThread().getId()));
        robotBase.stop();
    }

    @Override
    public Ticks getTicks() {
        Log.d(TAG, String.format("getTicks threadId=%d", Thread.currentThread().getId()));
        return null;
    }

    // Emoji HeadControlHandler
    @Override
    public int getMode() {
        Log.d(TAG, String.format("getMode threadId=%d", Thread.currentThread().getId()));
        return robotHead.getMode();
    }

    @Override
    public void setMode(int mode) {
        Log.d(TAG, String.format("setMode threadId=%d", Thread.currentThread().getId()));
        robotHead.setMode(mode);
    }

    @Override
    public synchronized void setWorldPitch(float angle) {
        Log.d(TAG, String.format("setWorldPitch threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotHead.setWorldPitch(angle);
    }
    }

    @Override
    public synchronized void setWorldYaw(float angle) {
        Log.d(TAG, String.format("setWorldYaw threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotHead.setWorldYaw(angle);
    }
    }

    @Override
    public float getWorldPitch() {
        Log.d(TAG, String.format("getWorldPitch threadId=%d", Thread.currentThread().getId()));
        return robotHead.getWorldPitch().getAngle();
    }

    @Override
    public float getWorldYaw() {
        Log.d(TAG, String.format("getWorldYaw threadId=%d", Thread.currentThread().getId()));
        return robotHead.getWorldYaw().getAngle();
    }

    // RobotTracking.BaseControlHandler
    @Override
    public synchronized void rawModeMove(float linear, float angular) {
        Log.d(TAG, String.format("moveVelocity threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotBase.setControlMode(Base.CONTROL_MODE_RAW);
        robotBase.setLinearVelocity(linear);
        robotBase.setAngularVelocity(angular);
    }
    }

    @Override
    public synchronized void targetModeMove(float distance, float angle) {
        Log.d(TAG, String.format("moveTarget threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotBase.setControlMode(Base.CONTROL_MODE_FOLLOW_TARGET);
        robotBase.updateTarget(distance, angle);
    }
    }

    // RobotTracking.HeadControlHandler
    @Override
    public float getHeadYaw() {
        Log.d(TAG, String.format("getHeadYaw threadId=%d", Thread.currentThread().getId()));
        return robotHead.getHeadJointYaw().getAngle();
    }

    @Override
    public float getHeadPitch() {
        Log.d(TAG, String.format("getHeadPitch threadId=%d", Thread.currentThread().getId()));
        return robotHead.getHeadJointPitch().getAngle();
    }

    @Override
    public synchronized void setHeadYawVelocity(float velocity) {
        Log.d(TAG, String.format("setHeadYawVelocity threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotHead.setYawAngularVelocity(velocity);
    }
    }

    @Override
    public synchronized void setHeadPitchVelocity(float velocity) {
        Log.d(TAG, String.format("setHeadPitchVelocity threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotHead.setPitchAngularVelocity(velocity);
    }
    }

    @Override
    public synchronized void smoothModeTarget(float yaw, float pitch) {
        Log.d(TAG, String.format("smoothModeTarget threadId=%d", Thread.currentThread().getId()));
        if (canMove) {
        robotHead.setMode(Head.MODE_SMOOTH_TACKING);
        robotHead.setWorldYaw(yaw);
        robotHead.setWorldPitch(pitch);
    }
    }

    @Override
    public void setHeadMode(int mode) {
        Log.d(TAG, String.format("setHeadMode threadId=%d", Thread.currentThread().getId()));
        robotHead.setMode(mode);
    }

    // Robot
    @Override
    public synchronized String getStatus() {
        Log.d(TAG, String.format("getStatus threadId=%d", Thread.currentThread().getId()));

        StringBuilder sb = new StringBuilder();

        if (isBindBase) {
            sb.append(" Propulsion systems are go. ");
        }
        if (isBindHead) {
            sb.append(" Degrees of freedom, check!");
        }
        if (isBindRecognizer) {
            sb.append(" Auditory systems, check! ");
        }
        if (isBindSensor) {
            sb.append(" Feelings, check!");
        }
        if (isBindSpeaker) {
            sb.append(" Vocalization systems. Silly Sally swiftly shooed seven silly sheep. Check!");
        }
        if (isBindVision) {
            sb.append(" Perception systems, perceiving.");
        }
        if (isConnected) {
            sb.append(" I'm connected to the cloud.");
        } else {
            sb.append(" Connection to cloud has been lost.");
        }

        sb.append(String.format(" I have sent %d messages.", messages));

        if (peopleDetected == 0) {
            sb.append(" I am all alone");
        } else if (peopleDetected == 1) {
            sb.append(" I only see one person");
        } else {
            sb.append(String.format(" I see %d people", peopleDetected));
        }

        sb.append(String.format(" My head pitch is %f radians.", robotHead.getHeadJointPitch().getAngle()));

        return sb.toString();
    }

    @Override
    public synchronized int getCadence() {
        Log.d(TAG, String.format("getCadence threadId=%d", Thread.currentThread().getId()));

        return cadence;
    }

    @Override
    public synchronized void setCadence(int cadence) {
        Log.d(TAG, String.format("setCadence threadId=%d", Thread.currentThread().getId()));

        this.cadence = cadence;
        if (timeout > 0) {
            timeout = cadence * 1000;
        }
        if (isConnected) {
            connection.updateTwin();
        }
    }

    @Override
    public synchronized Location getLocation() {
        Log.d(TAG, String.format("getLocation threadId=%d", Thread.currentThread().getId()));

        return lastLocation;
    }

    @Override
    public synchronized void setLocation(Location location) {
        Log.d(TAG, String.format("setLocation threadId=%d", Thread.currentThread().getId()));

        if (this.lastLocation == null && location != null) {
            robotConversation.speak("Hey, I know where I am now!");
        }
        this.lastLocation = location;
        handler.sendEmptyMessage(ACTION_SEND_LOCATION);
    }

    @Override
    public synchronized String getState() {
        Log.d(TAG, String.format("establishSocketConnection threadId=%d", Thread.currentThread().getId()));

        return lastState;
    }

    @Override
    public synchronized void setState(String state) {
        Log.d(TAG, String.format("establishSocketConnection threadId=%d", Thread.currentThread().getId()));

        this.lastState = state;
        handler.sendEmptyMessage(ACTION_SEND_STATE);
    }

    @Override
    public synchronized int getPeopleDetected() {
        return peopleDetected;
    }

    @Override
    public synchronized void setPeopleDetected(int peopleCount) {
        Log.d(TAG, String.format("setPeopleDetected threadId=%d", Thread.currentThread().getId()));

        if (peopleDetected != peopleCount) {
            peopleDetected = peopleCount;
            if (monitor == true) {
                if (peopleDetected == 0) {
                    actionSay("hmmmm");
                    //intro = false;
                } else {
                    if (peopleDetected > 1) {
                        actionSay("Hi everyone!");
                        canMove = false;
//                        robotConversation.waitUntilFinishedSpeaking();
                        robotCamera.takePicture();
                    } else {
                        if (!intro) {
                            actionSay("Hello, my name is Loomo.");
//                            robotConversation.waitUntilFinishedSpeaking();
                            robotCamera.takePicture();
                            //intro = true;
                        } else {
                            switch (rand.nextInt(5)) {
                                case 0:
                                    actionSay("Hi!");
                                    break;
                                case 1:
                                    actionSay("Good to see you!");
                                    break;
                                case 2:
                                    actionSay("Salutations!");
                                    break;
                                case 3:
                                    actionSay("Good day!");
                                    break;
                                default:
                                case 4:
                                    actionSay("Greetings!");
                                    break;
                            }
                            canMove = false;
//                            robotConversation.waitUntilFinishedSpeaking();
                            robotCamera.takePicture();
                        }
                    }
                }
            }
        }
    }

    @Override
    public synchronized boolean getDebug() {
        Log.d(TAG, String.format("getDebug threadId=%d", Thread.currentThread().getId()));
        return debug;
    }

    @Override
    public synchronized void setDebug(boolean debug) {
        Log.d(TAG, String.format("setDebug threadId=%d", Thread.currentThread().getId()));
        this.debug = debug;
    }

    @Override
    public synchronized boolean getMonitor() {
        Log.d(TAG, String.format("getMonitor threadId=%d", Thread.currentThread().getId()));
        return monitor;
    }

    @Override
    public synchronized void setMonitor(boolean monitor) {
        Log.d(TAG, String.format("setMonitor threadId=%d", Thread.currentThread().getId()));
        this.monitor = monitor;
        if (monitor) {
            peopleDetected = 0;
            robotConversation.setVolume(80);
        } else {
            robotConversation.setVolume(50);
        }
    }

    @Override
    public void actionSay(String phrase) {
        Log.d(TAG, String.format("actionSay threadId=%d", Thread.currentThread().getId()));
        robotConversation.speak(phrase);
    }

    @Override
    public synchronized void actionDo(RobotAction action) {
        Log.d(TAG, String.format("DEBUG actionDo threadId=%d", Thread.currentThread().getId()));
        actions.add(action);
        Log.d(TAG, String.format("DEBUG actionDo actions.size()=%d threadId=%d", actions.size(), Thread.currentThread().getId()));
        doNextAction(0);
    }

    @Override
    public synchronized void takePicture() {
        Log.d(TAG, String.format("takePicture threadId=%d", Thread.currentThread().getId()));
        if (peopleDetected > 0) {
        switch (rand.nextInt(4)) {
            case 0:
                    actionSay("How are you doing?");
                break;
            case 1:
                    actionSay("Beautiful day, isn't it?");
                break;
            case 2:
                    actionSay("What have you been up to?");
                break;
            case 3:
            default:
                    actionSay("How's it going?");
                break;
        }
//            robotConversation.waitUntilFinishedSpeaking();
        robotCamera.takePicture();
    }
    }

    @Override
    public synchronized void pauseMovement() {
        canMove = false;
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            Log.e(TAG, String.format("DEBUG pauseMovement Exception sleeping threadId=%d", Thread.currentThread().getId()), e);
        }
    }

    @Override
    public synchronized void resumeMovement() {
        canMove = true;
    }

    private synchronized void doNextAction(int lastActionType)
    {
        Log.d(TAG, String.format("DEBUG doNextAction lastActionType=%d threadId=%d", lastActionType, Thread.currentThread().getId()));

        // If we are just queueing an action and something is already going on, get out
        if (lastActionType == 0) {
            Log.d(TAG, String.format("DEBUG doNextAction lastActionType==0 threadId=%d", Thread.currentThread().getId()));
            if (currentAction != null) {
                int currentActionType = currentAction.getActionType();
                Log.d(TAG, String.format("DEBUG doNextAction currentActionType=%d threadId=%d", currentActionType, Thread.currentThread().getId()));
                if (currentActionType != Robot.ACTION_TYPE_TRACK) {
                    Log.d(TAG, String.format("DEBUG doNextAction returning threadId=%d", Thread.currentThread().getId()));
                    return;
                }
            }
        }

        RobotAction nextAction = resumeAction;
        if (!actions.isEmpty()) {
            nextAction = actions.remove();
            Log.d(TAG, String.format("DEBUG doNextAction actions.size()=%d threadId=%d", actions.size(), Thread.currentThread().getId()));
        }

        // if we were idle, stop it
        if (currentAction != null && currentAction.getActionType() == Robot.ACTION_TYPE_TRACK) {
            int nextActionType = nextAction.getActionType();
            if (nextActionType != Robot.ACTION_TYPE_TRACK) {
                Log.d(TAG, String.format("DEBUG doNextAction stopTracking threadId=%d", Thread.currentThread().getId()));
                if (currentAction.getBehavior() == Robot.TRACK_BEHAVIOR_FOLLOW) {
                    robotTracking.stopFollowing();
                }
                robotTracking.stopTracking();
                resumeAction = currentAction;
            } else {
                int currentBehavior = currentAction.getBehavior();
                int nextBehavior = nextAction.getBehavior();
                if (currentBehavior != nextBehavior) {
                    if (nextBehavior == Robot.TRACK_BEHAVIOR_WATCH) {
                        robotTracking.stopFollowing();
                    }
                }
            }
        }

        currentAction = nextAction;

        float arg1, arg2;
        Message msg;
        switch(currentAction.getActionType()) {
            case Robot.ACTION_TYPE_EMOTE:
                Log.d(TAG, String.format("DEBUG doNextAction actionType=ACTION_TYPE_EMOTE threadId=%d", Thread.currentThread().getId()));
                msg = handler.obtainMessage(ACTION_BEHAVE, nextAction.getBehavior());
                handler.sendMessage(msg);
                break;
            case Robot.ACTION_TYPE_LOOK:
                Log.d(TAG, String.format("DEBUG doNextAction actionType=ACTION_TYPE_LOOK threadId=%d", Thread.currentThread().getId()));
                if (canMove) {
                arg1 = currentAction.getYaw();
                arg2 = currentAction.getPitch();
                if (arg1 != Float.NaN || arg2 != Float.NaN) {
                    Log.d(TAG, String.format("DEBUG doNextAction valid look data threadId=%d", Thread.currentThread().getId()));
                    robotHead.setMode(Head.MODE_SMOOTH_TACKING);
                    if (arg1 != Float.NaN) {
                        robotHead.setWorldYaw(arg1);
                    }
                    if (arg2 != Float.NaN) {
                        robotHead.setWorldPitch(arg2);
                    }
                }
                try {
                    Thread.sleep(500);
                    } catch (Exception e) {
                    Log.e(TAG, String.format("DEBUG doNextAction Exception sleeping threadId=%d", Thread.currentThread().getId()), e);
                }
                }
                handler.sendEmptyMessage(ACTION_LOOK);
                break;
            case Robot.ACTION_TYPE_MOVE:
                Log.d(TAG, String.format("DEBUG doNextAction actionType=ACTION_TYPE_MOVE threadId=%d", Thread.currentThread().getId()));
                if (canMove) {
                arg1 = currentAction.getLinear();
                arg2 = currentAction.getAngular();
                if (nextAction.getBehavior() == Robot.MOVEMENT_BEHAVIOR_MOVE_VELOCITY) {
                    if (arg1 != Float.NaN || arg2 != Float.NaN) {
                        Log.d(TAG, String.format("DEBUG doNextAction valid move data threadId=%d", Thread.currentThread().getId()));
                        robotBase.setControlMode(Base.CONTROL_MODE_RAW);
                        if (arg1 != Float.NaN) {
                            robotBase.setLinearVelocity(arg1);
                        }
                        if (arg2 != Float.NaN) {
                            robotBase.setAngularVelocity(arg2);
                        }
                    }
                    } else {
                    if (arg1 != Float.NaN && arg2 != Float.NaN) {
                        Log.d(TAG, String.format("DEBUG doNextAction valid move data threadId=%d", Thread.currentThread().getId()));
                        robotBase.setControlMode(Base.CONTROL_MODE_FOLLOW_TARGET);
                        robotBase.updateTarget(arg1, arg2);
                    }
                }
                try {
                    Thread.sleep(500);
                    } catch (Exception e) {
                        Log.e(TAG, String.format("DEBUG doNextAction Exception sleeping threadId=%d", Thread.currentThread().getId()), e);
                }
                }
                handler.sendEmptyMessage(ACTION_MOVE);
                break;
            case Robot.ACTION_TYPE_TRACK:
                Log.d(TAG, String.format("DEBUG doNextAction actionType=ACTION_TYPE_TRACK threadId=%d", Thread.currentThread().getId()));
                robotTracking.startTracking();
                if (currentAction.getBehavior() == Robot.TRACK_BEHAVIOR_FOLLOW) {
                    robotTracking.startFollowing();
                }
                break;
        }
    }

    @Override
    public void establishSocketConnection(String server, int port, int cadence) {
        Log.d(TAG, String.format("establishSocketConnection threadId=%d", Thread.currentThread().getId()));
        teleops.start(server, port, cadence);
        if (debug) {
            actionSay("Teleops connection initiated");
        }
    }
}
