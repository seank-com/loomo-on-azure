package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.example.loomoonazure.view.AutoFitDrawableView;
import com.segway.robot.algo.dts.BaseControlCommand;
import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonDetectListener;
import com.segway.robot.algo.dts.PersonTrackingListener;
import com.segway.robot.algo.dts.PersonTrackingProfile;
import com.segway.robot.algo.dts.PersonTrackingWithPlannerListener;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.vision.DTS;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.support.control.HeadPIDController;

public class VisionActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener, HeadPIDController.HeadControlHandler, PersonDetectListener, PersonTrackingListener, PersonTrackingWithPlannerListener {
    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_HEIGHT = 480;
    private static final int TIME_OUT = 10 * 1000;

    private AutoFitDrawableView autoFitDrawableView;

    private Vision robotVision;
    private Head robotHead;
    private Base robotBase;
    private DTS dts;
    private HeadPIDController headPIDController = new HeadPIDController();

    private boolean isObstacleAvoidanceOpen = false;
    private PersonTrackingProfile personTrackingProfile;
    private long startTime;

    private void resetHead() {
        robotHead.setMode(Head.MODE_SMOOTH_TACKING);
        robotHead.setWorldYaw(0);
        robotHead.setWorldPitch(0.7f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision);

        autoFitDrawableView = (AutoFitDrawableView) findViewById(R.id.autoDrawable);
        autoFitDrawableView.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        autoFitDrawableView.setPreviewSizeAndRotation(PREVIEW_WIDTH, PREVIEW_HEIGHT, rotation);
        autoFitDrawableView.setSurfaceTextureListenerForPreview(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (dts != null) {
            dts.stop();
            dts = null;
        }

        headPIDController.stop();
    }

    // TextureView.SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        robotVision = Vision.getInstance();
        robotHead = Head.getInstance();
        robotBase = Base.getInstance();

        personTrackingProfile = new PersonTrackingProfile(3, 1.0f);

        dts = robotVision.getDTS();
        dts.setVideoSource(DTS.VideoSource.CAMERA);

        Surface surface = new Surface(autoFitDrawableView.getPreview().getSurfaceTexture());
        dts.setPreviewDisplay(surface);

        dts.start();

        resetHead();

        headPIDController.init(this);
        headPIDController.setHeadFollowFactor(1.0f);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    // View.OnClickListener
    @Override
    public void onClick(View view) {
        if (isObstacleAvoidanceOpen) {
            dts.stopPlannerPersonTracking();
        } else {
            dts.stopPersonTracking();
        }

        dts.stopDetectingPerson();

        isObstacleAvoidanceOpen = !isObstacleAvoidanceOpen;

        startTime = System.currentTimeMillis();
        dts.startDetectingPerson(this);

        if (isObstacleAvoidanceOpen) {
            // Loomo will detect obstacles and avoid them when invoke startPlannerPersonTracking()
            dts.startPlannerPersonTracking(null, personTrackingProfile, 60 * 1000 * 1000, this);
        } else {
            // Without obstacle detection and avoidance
            dts.startPersonTracking(null, 60 * 1000 * 1000, this);
        }
    }

    // PersonDetectListener
    @Override
    public void onPersonDetected(DTSPerson[] person) {
        if (person == null || person.length == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > TIME_OUT) {
                resetHead();
            }
            return;
        }
        startTime = System.currentTimeMillis();
        autoFitDrawableView.drawRect(person);

        robotHead.setMode(Head.MODE_ORIENTATION_LOCK);
        headPIDController.updateTarget(person[0].getTheta(), person[0].getDrawingRect(), 480);
    }

    @Override
    public void onPersonDetectionResult(DTSPerson[] person) {

    }

    @Override
    public void onPersonDetectionError(int errorCode, String message) {

    }

    // HeadPIDController.HeadControlHandler
    @Override
    public float getJointYaw() {
        return robotHead.getHeadJointYaw().getAngle();
    }

    @Override
    public float getJointPitch() {
        return robotHead.getHeadJointPitch().getAngle();
    }

    @Override
    public void setYawAngularVelocity(float velocity) {
        robotHead.setYawAngularVelocity(velocity);
    }

    @Override
    public void setPitchAngularVelocity(float velocity) {
        robotHead.setPitchAngularVelocity(velocity);
    }

    // PersonTrackingListener
    @Override
    public void onPersonTracking(DTSPerson person) {
        if (person == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > TIME_OUT) {
                resetHead();
            }
            return;
        }
        startTime = System.currentTimeMillis();
        autoFitDrawableView.drawRect(person.getDrawingRect());

        robotHead.setMode(Head.MODE_ORIENTATION_LOCK);
        headPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);

        robotBase.setControlMode(Base.CONTROL_MODE_FOLLOW_TARGET);
        float personDistance = person.getDistance();
        // There is a bug in DTS, while using person.getDistance(), please check the result
        // The correct distance is between 0.35 meters and 5 meters
        if (personDistance > 0.35 && personDistance < 5) {
            float followDistance = (float) (personDistance - 1.2);
            float theta = person.getTheta();
            robotBase.updateTarget(followDistance, theta);
        }
    }

    @Override
    public void onPersonTrackingResult(DTSPerson person) {

    }

    @Override
    public void onPersonTrackingError(int errorCode, String message) {

    }

    // PersonTrackingWithPlannerListener
    @Override
    public void onPersonTrackingWithPlannerResult(DTSPerson person, BaseControlCommand baseControlCommand) {
        if (person == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > TIME_OUT) {
                resetHead();
            }
            return;
        }

        startTime = System.currentTimeMillis();
        autoFitDrawableView.drawRect(person.getDrawingRect());

        robotHead.setMode(Head.MODE_ORIENTATION_LOCK);
        headPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);

        switch (baseControlCommand.getFollowState()) {
            case BaseControlCommand.State.NORMAL_FOLLOW:
                robotBase.setControlMode(Base.CONTROL_MODE_RAW);
                robotBase.setLinearVelocity(baseControlCommand.getLinearVelocity());
                robotBase.setAngularVelocity(baseControlCommand.getAngularVelocity());
                break;
            case BaseControlCommand.State.HEAD_FOLLOW_BASE:
                robotBase.setControlMode(Base.CONTROL_MODE_FOLLOW_TARGET);
                robotBase.updateTarget(0, person.getTheta());
                break;
            case BaseControlCommand.State.SENSOR_ERROR:
                robotBase.setControlMode(Base.CONTROL_MODE_RAW);
                robotBase.setLinearVelocity(0);
                robotBase.setAngularVelocity(0);
                break;
        }
    }

    @Override
    public void onPersonTrackingWithPlannerError(int errorCode, String message) {

    }
}
