package com.example.loomoonazure.util;

import android.util.Log;
import android.view.Surface;

import com.segway.robot.algo.dts.BaseControlCommand;
import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonDetectListener;
import com.segway.robot.algo.dts.PersonTrackingProfile;
import com.segway.robot.algo.dts.PersonTrackingWithPlannerListener;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.vision.DTS;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.support.control.HeadPIDController;

import java.util.Timer;
import java.util.TimerTask;

public class RobotTracking extends TimerTask implements HeadPIDController.HeadControlHandler, PersonDetectListener, PersonTrackingWithPlannerListener {
    private static final String TAG = "RobotTracking";

    public interface BaseControlHandler {
        void rawModeMove(float linear, float angular);
        void targetModeMove(float distance, float angle);
    }

    public interface HeadControlHandler {
        float getHeadYaw();
        float getHeadPitch();
        void setHeadYawVelocity(float velocity);
        void setHeadPitchVelocity(float velocity);
        void smoothModeTarget(float yaw, float pitch);
        void setHeadMode(int mode);
    }

    private Vision robotVision;

    private static final int TIME_OUT = 10 * 1000;

    private DTS dts = null;
    private HeadPIDController headPIDController = null;
    private PersonTrackingProfile personTrackingProfile = null;
    private long startTime = System.currentTimeMillis();

    private Robot robot;
    private RobotCamera robotCamera;
    private BaseControlHandler baseControlHandler;
    private HeadControlHandler headControlHandler;

    private Timer monitor;
    private int lastLook;

    public RobotTracking(Robot robot, RobotCamera robotCamera, BaseControlHandler baseControlHandler, HeadControlHandler headControlHandler) {
        Log.d(TAG, String.format("constructor threadId=%d", Thread.currentThread().getId()));

        this.robot = robot;
        this.robotCamera = robotCamera;
        this.baseControlHandler = baseControlHandler;
        this.headControlHandler = headControlHandler;

        robotVision = Vision.getInstance();
    }

    private void lookAround() {
        Log.d(TAG, String.format("resetHead threadId=%d", Thread.currentThread().getId()));

        lastLook += 1;
        if (lastLook == 1) {
            headControlHandler.smoothModeTarget(0,0.7f);
        } else if (lastLook == 2) {
            headControlHandler.smoothModeTarget(-0.8f,0.7f);
        } else if (lastLook == 3) {
            headControlHandler.smoothModeTarget(0,0.7f);
        } else {
            headControlHandler.smoothModeTarget(0.8f,0.7f);
            lastLook = 0;
        }
    }
    public Surface getSurface() {
        if (dts == null) {
            return null;
        }
        return dts.getSurface();
    }

    public synchronized void startTracking() {
        Log.d(TAG, String.format("startTracking threadId=%d", Thread.currentThread().getId()));

        if (dts == null && headPIDController == null) {
            dts = robotVision.getDTS();

            if (robotCamera == null) {
                dts.setVideoSource(DTS.VideoSource.CAMERA);
                dts.start();
                beginTracking();
            } else {
                dts.setVideoSource(DTS.VideoSource.SURFACE);
                dts.start();
                robotCamera.start(this);
            }
        }
    }

    public synchronized void beginTracking() {
        Log.d(TAG, String.format("startTracking threadId=%d", Thread.currentThread().getId()));

        if (dts != null  && headPIDController == null) {
            dts.setPoseRecognitionEnabled(true);

            lastLook = 0;
            lookAround();

            headPIDController = new HeadPIDController();
            headPIDController.init(this);
            headPIDController.setHeadFollowFactor(1.0f);

            startTime = System.currentTimeMillis();
            dts.startDetectingPerson(this);

            if (monitor == null) {
                monitor = new Timer();
                monitor.schedule(this, 5000, 5000);
            }
        }
    }

    public synchronized void startFollowing() {
        Log.d(TAG, String.format("startFollowing threadId=%d", Thread.currentThread().getId()));

        if (dts != null && headPIDController != null && personTrackingProfile == null) {
            // Loomo will detect obstacles and avoid them when invoke startPlannerPersonTracking()
            personTrackingProfile = new PersonTrackingProfile(3, 1.0f);
            dts.startPlannerPersonTracking(null, personTrackingProfile, 60 * 1000 * 1000, this);
        }
    }

    public synchronized void stopFollowing() {
        Log.d(TAG, String.format("startFollowing threadId=%d", Thread.currentThread().getId()));

        if (dts != null && headPIDController != null && personTrackingProfile != null) {
            dts.stopPlannerPersonTracking();
            personTrackingProfile = null;

            startTime = System.currentTimeMillis();
            dts.startDetectingPerson(this);
        }
    }

    public synchronized void stopTracking() {
        Log.d(TAG, String.format("stopTracking threadId=%d", Thread.currentThread().getId()));

        if (dts != null) {
            dts.stopDetectingPerson();
            dts.stop();
            dts = null;
        }

        if (headPIDController != null) {
            headPIDController.stop();
            headPIDController = null;
        }

        if (robotCamera != null) {
            robotCamera.stop();
        }
    }

    // PersonDetectListener
    @Override
    public synchronized void onPersonDetected(DTSPerson[] people) {
        Log.d(TAG, String.format("onPersonDetected threadId=%d", Thread.currentThread().getId()));

        int personCount = 0;
        DTSPerson person = null;

        if (people != null && people.length != 0) {
            personCount = people.length;
            person = people[0];
            int personality = person.getType();
            int pose = person.getPoseRecognitionIndex();
            float score = person.getPoseRecognitionScore();
            Log.d(TAG, String.format("getPoseRecognitionIndex personality=%d pose=%d score=%f", personality, pose, score));
        }

        robot.setPeopleDetected(personCount);

        if (personCount == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > TIME_OUT) {
                lookAround();
                startTime = System.currentTimeMillis();
            }
            return;
        }

        startTime = System.currentTimeMillis();
        headControlHandler.setHeadMode(Head.MODE_ORIENTATION_LOCK);
        headPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);
    }

    @Override
    public void onPersonDetectionResult(DTSPerson[] person) {
        Log.d(TAG, String.format("onPersonDetectionResult threadId=%d", Thread.currentThread().getId()));
    }

    @Override
    public void onPersonDetectionError(int errorCode, String message) {
        Log.e(TAG, String.format("onPersonDetectionError threadId=%d", Thread.currentThread().getId()));
    }

    // HeadPIDController.HeadControlHandler
    @Override
    public float getJointYaw() {
        Log.d(TAG, String.format("getJointYaw threadId=%d", Thread.currentThread().getId()));
        return headControlHandler.getHeadYaw();
    }

    @Override
    public float getJointPitch() {
        Log.d(TAG, String.format("getJointPitch threadId=%d", Thread.currentThread().getId()));
        return headControlHandler.getHeadPitch();
    }

    @Override
    public void setYawAngularVelocity(float velocity) {
        Log.d(TAG, String.format("setYawAngularVelocity threadId=%d", Thread.currentThread().getId()));
        headControlHandler.setHeadYawVelocity(velocity);
    }

    @Override
    public void setPitchAngularVelocity(float velocity) {
        Log.d(TAG, String.format("setPitchAngularVelocity threadId=%d", Thread.currentThread().getId()));
        headControlHandler.setHeadPitchVelocity(velocity);
    }

    // PersonTrackingWithPlannerListener
    @Override
    public synchronized void onPersonTrackingWithPlannerResult(DTSPerson person, BaseControlCommand baseControlCommand) {
        Log.d(TAG, String.format("onPersonTrackingWithPlannerResult threadId=%d", Thread.currentThread().getId()));
        if (person == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > TIME_OUT) {
                lookAround();
                robot.setPeopleDetected(0);
                startTime = System.currentTimeMillis();
            }
            return;
        }

        startTime = System.currentTimeMillis();

        headControlHandler.setHeadMode(Head.MODE_ORIENTATION_LOCK);
        headPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);

        switch (baseControlCommand.getFollowState()) {
            case BaseControlCommand.State.NORMAL_FOLLOW:
                baseControlHandler.rawModeMove(baseControlCommand.getLinearVelocity(), baseControlCommand.getAngularVelocity());
                break;
            case BaseControlCommand.State.HEAD_FOLLOW_BASE:
                baseControlHandler.targetModeMove(0, person.getTheta());
                break;
            case BaseControlCommand.State.SENSOR_ERROR:
                baseControlHandler.rawModeMove(0,0);
                break;
        }
    }

    @Override
    public void onPersonTrackingWithPlannerError(int errorCode, String message) {
        Log.d(TAG, String.format("onPersonTrackingWithPlannerError threadId=%d", Thread.currentThread().getId()));
    }

    @Override
    public synchronized void run() {
        if (dts != null && headPIDController != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > TIME_OUT) {
                robot.setPeopleDetected(0);
                lookAround();
                startTime = System.currentTimeMillis();
            }
        }
    }
}
