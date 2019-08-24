package com.example.loomoonazure;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.VLSPoseListener;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.locomotion.sbv.StartVLSListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class NavigationActivity extends AppCompatActivity implements StartVLSListener, VLSPoseListener, CheckPointStateListener {
    private Switch vlsSwitch;
    private Button setStart;
    private Button setCheckpoint;
    private Button goToOrigin;
    private Button run;
    private TextView status;

    private Base robotBase;

    private int step;

    private ArrayList<CheckPoint> checkpoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        step = -1;
        checkpoints = new ArrayList<>();

        vlsSwitch = (Switch)findViewById(R.id.vls);
        setStart = (Button)findViewById(R.id.setStart);
        setCheckpoint = (Button)findViewById(R.id.setCheckpoint);
        goToOrigin = (Button)findViewById(R.id.goToOrigin);
        run = (Button)findViewById(R.id.run);
        status = (TextView)findViewById(R.id.status);

        robotBase = Base.getInstance();
        if (robotBase.isBind()) {
            robotBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
            robotBase.setOnCheckPointArrivedListener(this);
            status.setText("Robot Initialized");
        }
    }

    private void enableUI(boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vlsSwitch.setEnabled(enable);
                setStart.setEnabled(enable);
                setCheckpoint.setEnabled(enable);
                goToOrigin.setEnabled(enable);
                run.setEnabled(enable);
            }
        });
    }

    private void setStatus(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(msg);
            }
        });
    }

    public void onVLSSwitch(View view) {
        enableUI(false);
        if (robotBase.isBind()) {
            if (vlsSwitch.isChecked()) {
                robotBase.startVLS( true, true, this);
            } else {
                robotBase.stopVLS();
                robotBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_ODOM);
                robotBase.setVLSPoseListener(this);
                enableUI(true);
                step = -1;
                checkpoints.clear();
                onSetStart(null);
            }
        }
    }
    public void onSetStart(View view) {
        if (robotBase.isBind()) {
            Pose2D pose;
            robotBase.cleanOriginalPoint();
            if (robotBase.isVLSStarted()) {
                pose = robotBase.getVLSPose(-1);
            } else {
                pose = robotBase.getOdometryPose(-1);
            }
            robotBase.setOriginalPoint(pose);
            step = -1;
            checkpoints = new ArrayList<>();
            setStatus("Robot Zeroed");
        }
    }
    public void onSetCheckpoint(View view) {
        if (robotBase.isBind()) {
            Pose2D pose;
            if (robotBase.isVLSStarted()) {
                pose = robotBase.getVLSPose(-1);
            } else {
                pose = robotBase.getOdometryPose(-1);
            }
            checkpoints.add(new CheckPoint(pose.getX(), pose.getY(), pose.getTheta()));
            setStatus(String.format("%d Checkpoints Recorded", checkpoints.size()));
        }

    }

    private void doNextStep() {
        step += 1;
        CheckPoint dest = checkpoints.get(step);
        robotBase.addCheckPoint(dest.getX(), dest.getY(), dest.getOrientation());
    }

    public void onGoToOrigin(View view) {
        enableUI(false);
        step = -1;
        robotBase.addCheckPoint(0,0,0);
    }

    public void onRun(View view) {
        enableUI(false);
        step = -1;
        doNextStep();
    }

    // Implement StartVLSListener
    @Override
    public void onOpened() {
        robotBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS);
        enableUI( true);
        onSetStart(null);
    }

    @Override
    public void onError(String errorMessage) {
        setStatus(String.format("onError: %s", errorMessage));
    }

    // Implement CheckPointStateListener
    @Override
    public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
        setStatus(String.format("Arrived at (%f, %f, %f)", checkPoint.getX(), checkPoint.getY(), checkPoint.getOrientation()));
        if (step == -1 || step == checkpoints.size()) {
            robotBase.clearCheckPointsAndStop();
            enableUI(true);
        } else {
            robotBase.clearCheckPointsAndStop();
            doNextStep();
        }
    }

    @Override
    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
        robotBase.clearCheckPointsAndStop();
        step = -1;
        checkpoints = new ArrayList<>();
        setStatus(String.format("Missed (%f, %f, %f) for reason %d", checkPoint.getX(), checkPoint.getY(), checkPoint.getOrientation(), reason));
        enableUI(true);
    }

    @Override
    public void onVLSPoseUpdate(long timestamp, float x, float y, float theta, float v, float w) {
        setStatus(String.format("(%f, %f, %f)", x, y, theta));
    }
}