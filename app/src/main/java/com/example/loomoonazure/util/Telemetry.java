package com.example.loomoonazure.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.tf.AlgoTfData;
import com.segway.robot.sdk.locomotion.head.Angle;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.AngularVelocity;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.google.gson.JsonObject;
import com.segway.robot.sdk.locomotion.sbv.BasePose;
import com.segway.robot.sdk.locomotion.sbv.BaseTicks;
import com.segway.robot.sdk.locomotion.sbv.BaseWheelInfo;
import com.segway.robot.sdk.locomotion.sbv.LinearVelocity;
import com.segway.robot.sdk.perception.sensor.InfraredData;
import com.segway.robot.sdk.perception.sensor.RobotAllSensors;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.perception.sensor.UltrasonicData;

import java.util.ArrayList;
import java.util.LinkedList;

public class Telemetry extends BroadcastReceiver implements LocationListener {
    private static final String TAG = "Telemetry";

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

    private Robot robot;
    private Base robotBase;
    private Head robotHead;
    private Sensor robotSensor;

    private LocationManager locationManager;

    private void populateBase(JsonObject telemetry)
    {
        Log.d(TAG, String.format("populateBase threadId=%d", Thread.currentThread().getId()));

        if (!robotBase.isBind()) {
            return;
        }

        JsonObject base = new JsonObject();
        AngularVelocity angularVelocity = robotBase.getAngularVelocity();
        LinearVelocity linearVelocity = robotBase.getLinearVelocity();

        if (robotBase.isVLSStarted()) {
            JsonObject pose = new JsonObject();
            PoseVLS vlsPose = robotBase.getVLSPose(linearVelocity.getTimestamp());
            pose.addProperty("PathLengthSinceLastRelocation", vlsPose.getPathLengthSinceLastRelocation());
            pose.addProperty("PathLengthSinceStart", vlsPose.getPathLengthSinceStart());
            pose.addProperty("UncertaintyInPercentage", vlsPose.getUncertaintyInPercentage());
            pose.addProperty("VIOQuality", vlsPose.getVIOQuality());
            pose.addProperty("AngularVelocity", vlsPose.getAngularVelocity());
            pose.addProperty("LinearVelocity", vlsPose.getLinearVelocity());
            pose.addProperty("Theta", vlsPose.getTheta());
            pose.addProperty("X", vlsPose.getX());
            pose.addProperty("Y", vlsPose.getY());
            base.add("VLSPose", pose);

        } else {
            JsonObject pose = new JsonObject();
            Pose2D odometryPose = robotBase.getOdometryPose(linearVelocity.getTimestamp());
            pose.addProperty("AngularVelocity", odometryPose.getAngularVelocity());
            pose.addProperty("LinearVelocity", odometryPose.getLinearVelocity());
            pose.addProperty("Theta", odometryPose.getTheta());
            pose.addProperty("X", odometryPose.getX());
            pose.addProperty("Y", odometryPose.getY());
            base.add("OdometryPose", pose);
        }

        base.addProperty("AngularVelocity", angularVelocity.getSpeed());
        base.addProperty("AngularVelocityLimit", robotBase.getAngularVelocityLimit());
        base.addProperty("CartMile", robotBase.getCartMile());
        base.addProperty("ControlMode", robotBase.getControlMode());
        base.addProperty("LightBrightness", robotBase.getLightBrightness());
        base.addProperty("LinearVelocity", linearVelocity.getSpeed());
        base.addProperty("LinearVelocityLimit", robotBase.getLinearVelocityLimit());
        base.addProperty("Mileage", robotBase.getMileage());
        base.addProperty("RidingSpeedLimit", robotBase.getRidingSpeedLimit());
        base.addProperty("RobotPower", robotBase.getRobotPower());
        base.addProperty("UltrasonicDistance", robotBase.getUltrasonicDistance().getDistance());
        base.addProperty("UltrasonicObstacleAvoidanceDistance", robotBase.getUltrasonicObstacleAvoidanceDistance());

        base.addProperty("BodyLightOpen", robotBase.isBodyLightOpen());
        base.addProperty("CartModeWheelSlip", robotBase.isCartModeWheelSlip());
        base.addProperty("InCartMode", robotBase.isInCartMode());
        base.addProperty("RidingInSpeedLimit", robotBase.isRidingInSpeedLimit());
        base.addProperty("UltrasonicObstacleAvoidanceEnabled", robotBase.isUltrasonicObstacleAvoidanceEnabled());
        telemetry.add("Base", base);

        // IoT Central properties need to be top-level (not nested). Fortunately, unknown properties
        // are ignored, so if you want this robot to talk to IoT Central, you'll need to promote the
        // properties that you would like to be able to access.
        telemetry.addProperty("AngularVelocity",angularVelocity.getSpeed());
        telemetry.addProperty("LinearVelocity", linearVelocity.getSpeed());
        telemetry.addProperty("CartMile", robotBase.getCartMile());
        telemetry.addProperty("Mileage", robotBase.getMileage());
        telemetry.addProperty("RobotPower", robotBase.getRobotPower());
        telemetry.addProperty("LightBrightness", robotBase.getLightBrightness());
        telemetry.addProperty("UltrasonicDistance", robotBase.getUltrasonicDistance().getDistance());
        telemetry.addProperty("ControlMode", robotBase.getControlMode());
    }

    private void populateHead(JsonObject telemetry)
    {
        Log.d(TAG, String.format("populateHead threadId=%d", Thread.currentThread().getId()));

        if (!robotHead.isBind()) {
            return;
        }
        JsonObject head = new JsonObject();

        head.addProperty("HeadJointPitch", robotHead.getHeadJointPitch().getAngle());
        head.addProperty("HeadJointRoll", robotHead.getHeadJointRoll().getAngle());
        head.addProperty("HeadJointYaw", robotHead.getHeadJointYaw().getAngle());
        head.addProperty("HeadMode", robotHead.getMode());
        head.addProperty("HeadPitchAngularVelocity", robotHead.getPitchAngularVelocity().getVelocity());
        head.addProperty("HeadRollAngularVelocity", robotHead.getRollAngularVelocity().getVelocity());
        head.addProperty("HeadWorldPitch", robotHead.getWorldPitch().getAngle());
        head.addProperty("HeadWorldRoll", robotHead.getWorldRoll().getAngle());
        head.addProperty("HeadWorldYaw", robotHead.getWorldYaw().getAngle());
        head.addProperty("HeadYawAngularVelocity", robotHead.getYawAngularVelocity().getVelocity());
        telemetry.add("Head", head);

        // IoT Central properties need to be top-level (not nested). Fortunately, unknown properties
        // are ignored, so if you want this robot to talk to IoT Central, you'll need to promote the
        // properties that you would like to be able to access.
        telemetry.addProperty("HeadMode", robotHead.getMode());
        telemetry.addProperty("HeadJointPitch", robotHead.getHeadJointPitch().getAngle());
        telemetry.addProperty("HeadJoingYaw", robotHead.getHeadJointYaw().getAngle());
    }

    private void populateSensor(JsonObject telemetry)
    {
        Log.d(TAG, String.format("populateSensor threadId=%d", Thread.currentThread().getId()));

        if (!robotSensor.isBind()) {
            return;
        }

        JsonObject sensor = new JsonObject();

        RobotAllSensors robotAllSensors = robotSensor.getRobotAllSensors();

        BasePose basePose = robotAllSensors.getBasePose();
        JsonObject pose = new JsonObject();
        pose.addProperty("Yaw", basePose.getYaw());
        pose.addProperty("Pitch", basePose.getPitch());
        pose.addProperty("Roll", basePose.getRoll());
        sensor.add("BasePose", pose);

        BaseTicks baseTicks = robotAllSensors.getBaseTicks();
        JsonObject ticks = new JsonObject();
        ticks.addProperty("LeftTicks", baseTicks.getLeftTicks());
        ticks.addProperty("RightTicks", baseTicks.getRightTicks());
        sensor.add("BaseTicks", ticks);

        BaseWheelInfo baseWheelInfo = robotAllSensors.getBaseWheelInfo();
        JsonObject wheelInfo = new JsonObject();
        wheelInfo.addProperty("LeftSpeed", baseWheelInfo.getLeftSpeed());
        wheelInfo.addProperty("RightSpeed", baseWheelInfo.getRightSpeed());
        sensor.add("BaseWheelInfo", wheelInfo);

        sensor.addProperty("HeadJointYaw", robotAllSensors.getHeadJointYaw().getAngle());
        sensor.addProperty("HeadJointPitch", robotAllSensors.getHeadJointPitch().getAngle());
        sensor.addProperty("HeadJointRoll", robotAllSensors.getHeadJointRoll().getAngle());

        Angle headYaw = robotAllSensors.getHeadWorldYaw();
        Angle headPitch = robotAllSensors.getHeadWorldPitch();
        Angle headRoll = robotAllSensors.getHeadWorldRoll();
        sensor.addProperty("HeadWorldYaw", headYaw.getAngle());
        sensor.addProperty("HeadWorldYawTimestamp", headYaw.getTimestamp());
        sensor.addProperty("HeadWorldPitch", headPitch.getAngle());
        sensor.addProperty("HeadWorldPitchTimestamp", headPitch.getTimestamp());
        sensor.addProperty("HeadWorldRoll", headRoll.getAngle());
        sensor.addProperty("HeadWorldRollTimestamp", headRoll.getTimestamp());

        InfraredData infraredData = robotAllSensors.getInfraredData();
        JsonObject data = new JsonObject();
        data.addProperty("LeftDistance", infraredData.getLeftDistance());
        data.addProperty("RightDistance", infraredData.getRightDistance());
        sensor.add("InfraredData", data);

        Pose2D pose2D = robotAllSensors.getPose2D();
        pose = new JsonObject();
        pose.addProperty("AngularVelocity", pose2D.getAngularVelocity());
        pose.addProperty("LinearVelocity", pose2D.getLinearVelocity());
        pose.addProperty("Theta", pose2D.getTheta());
        pose.addProperty("X", pose2D.getX());
        pose.addProperty("Y", pose2D.getY());
        sensor.add("Pose2D", pose);

        AlgoTfData tfBase = robotSensor.getTfData(Sensor.BASE_ODOM_FRAME, Sensor.WORLD_ODOM_ORIGIN, pose2D.getTimestamp(), 100);
        AlgoTfData tfHead = robotSensor.getTfData(Sensor.HEAD_POSE_P_R_FRAME, Sensor.WORLD_ODOM_ORIGIN, headYaw.getTimestamp(), 100);

        JsonObject frame = new JsonObject();
        frame.addProperty("BaseX", tfBase.t.x);
        frame.addProperty("BaseY", tfBase.t.y);
        frame.addProperty("BaseTheta", tfBase.q.getYawRad());
        frame.addProperty("BaseErrCode", tfBase.err_code);
        frame.addProperty("HeadYaw", tfHead.q.getYawRad());
        frame.addProperty("HeadPitch", tfHead.q.getPitchRad());
        frame.addProperty("HeadErrCode", tfHead.err_code);
        sensor.add("Frame", frame);

        sensor.addProperty("UltrasonicDistance", robotAllSensors.getUltrasonicData().getDistance());
        telemetry.add("Sensor", sensor);
    }

    public Telemetry(Robot robot)
    {
        Log.d(TAG, String.format("constructor threadId=%d", Thread.currentThread().getId()));

        this.robot = robot;
        this.robotBase = Base.getInstance();
        this.robotHead = Head.getInstance();
        this.robotSensor = Sensor.getInstance();
    }

    public void registerEvents(Context context) {
        Log.d(TAG, String.format("registerEvents threadId=%d", Thread.currentThread().getId()));

        IntentFilter filter = new IntentFilter();
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

        context.getApplicationContext().registerReceiver(this, filter);

        try {
            locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                Log.d(TAG, String.format("lastLocation=%s", lastLocation.toString()));
                robot.setLocation(lastLocation);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Exception locating", e);
        }
    }

    public void unregisterEvents(Context context) {
        Log.d(TAG, String.format("registerEvents threadId=%d", Thread.currentThread().getId()));

        locationManager.removeUpdates(this);
        context.getApplicationContext().unregisterReceiver(this);
    }

    public String getMessage() {
        Log.d(TAG, String.format("getMessage threadId=%d", Thread.currentThread().getId()));

        JsonObject telemetry = new JsonObject();
        telemetry.addProperty("type", "telemetry");

        populateBase(telemetry);
        populateHead(telemetry);
        populateSensor(telemetry);

        return telemetry.toString();
    }

    public String getState() {
        Log.d(TAG, String.format("getMessage threadId=%d", Thread.currentThread().getId()));

        JsonObject state = new JsonObject();
        state.addProperty("type", "state_change");
        state.addProperty("RobotState", robot.getState());
        return state.toString();
    }

    public String getLocation() {
        Log.d(TAG, String.format("getLocation threadId=%d", Thread.currentThread().getId()));

        JsonObject state = new JsonObject();
        JsonObject location =  new JsonObject();
        Location lastLocation = robot.getLocation();
        location.addProperty("lat", lastLocation.getLatitude());
        location.addProperty("lon", lastLocation.getLongitude());
        state.addProperty("type", "location_change");
        state.add("location", location);
        return state.toString();
    }

    public ArrayList<String> getLive() {
        ArrayList<String> results = new ArrayList<String>();

        if (robotSensor.isBind()) {
            JsonObject envelope;
            JsonObject payload;

            RobotAllSensors robotAllSensors = robotSensor.getRobotAllSensors();

            payload = new JsonObject();
            Pose2D pose2D = robotAllSensors.getPose2D();
            payload.addProperty("angularVelocity", pose2D.getAngularVelocity());
            payload.addProperty("linearVelocity", pose2D.getLinearVelocity());
            payload.addProperty("theta", pose2D.getTheta());
            payload.addProperty("x", pose2D.getX());
            payload.addProperty("y", pose2D.getY());

            envelope = new JsonObject();
            envelope.addProperty("source", "/loomo/base");
            envelope.add("payload", payload);

            results.add(envelope.toString());

            payload = new JsonObject();
            payload.addProperty("yaw", robotAllSensors.getHeadJointYaw().getAngle());
            payload.addProperty("pitch", robotAllSensors.getHeadJointPitch().getAngle());
            payload.addProperty("roll", robotAllSensors.getHeadJointRoll().getAngle());

            envelope = new JsonObject();
            envelope.addProperty("source", "/loomo/head");
            envelope.add("payload", payload);

            results.add(envelope.toString());

            payload = new JsonObject();
            InfraredData infraredData = robotAllSensors.getInfraredData();
            payload.addProperty("infraredLeftDistance", infraredData.getLeftDistance());
            payload.addProperty("infraredRightDistance", infraredData.getRightDistance());
            payload.addProperty("ultrasonicDistance", robotAllSensors.getUltrasonicData().getDistance());

            envelope = new JsonObject();
            envelope.addProperty("source", "/loomo/sensors");
            envelope.add("payload", payload);

            results.add(envelope.toString());
        }
        return results;
    }

    // BroadcastReceiver
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        String state;
        long tid = Thread.currentThread().getId();
        switch (action) {
            case BATTERY_CHANGED:
                state = "BATTERY_CHANGED";
                break;
            case POWER_DOWN:
                state = "POWER_DOWN";
                break;
            case POWER_BUTTON_PRESSED:
                state = "POWER_BUTTON_PRESSED";
                break;
            case POWER_BUTTON_RELEASED:
                state = "POWER_BUTTON_RELEASED";
                break;
            case TO_SBV:
                state = "TO_SBV";
                break;
            case TO_ROBOT:
                state = "TO_ROBOT";
                break;
            case PITCH_LOCK:
                state = "PITCH_LOCK";
                break;
            case PITCH_UNLOCK:
                state = "PITCH_UNLOCK";
                break;
            case YAW_LOCK:
                state = "YAW_LOCK";
                break;
            case YAW_UNLOCK:
                state = "YAW_UNLOCK";
                break;
            case STEP_ON:
                state = "STEP_ON";
                break;
            case STEP_OFF:
                state = "STEP_OFF";
                break;
            case LIFT_UP:
                state = "LIFT_UP";
                break;
            case PUT_DOWN:
                state = "PUT_DOWN";
                break;
            case PUSHING:
                state = "PUSHING";
                break;
            case PUSH_RELEASE:
                state = "PUSH_RELEASE";
                break;
            case BASE_LOCK:
                state = "BASE_LOCK";
                break;
            case BASE_UNLOCK:
                state = "BASE_UNLOCK";
                break;
            case STAND_UP:
                state = "STAND_UP";
                break;
            default:
                Log.d(TAG, String.format("onReceive action=%s threadId=%d", action, tid));
                return;
        }
        Log.d(TAG, String.format("onReceive action=%s threadId=%d", state, tid));
        robot.setState(state);
    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, String.format("onLocationChanged location=%s threadId=%d", location.toString(), Thread.currentThread().getId()));

        robot.setLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, String.format("onProviderEnabled s=%s threadId=%d", s, Thread.currentThread().getId()));
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, String.format("onProviderEnabled s=%s threadId=%d", s, Thread.currentThread().getId()));
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, String.format("onProviderDisabled s=%s threadId=%d", s, Thread.currentThread().getId()));

    }
}
