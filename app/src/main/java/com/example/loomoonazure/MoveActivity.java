package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.locomotion.head.Angle;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;

public class MoveActivity extends AppCompatActivity {

    private Base robotBase;
    private Head robotHead;
    private static final float step = (float)(Math.PI / 18f);

    private TextView position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);

        robotBase = Base.getInstance();
        robotHead = Head.getInstance();

        position = (TextView)findViewById(R.id.position);
        position.setText("");

        onReset(null);
    }

    public void onBaseBack(View view)
    {
        if (robotBase.isBind())
        {
            robotBase.setAngularVelocity(0);
            robotBase.setLinearVelocity(-1f);
        }
    }

    public void onBaseForward(View view)
    {
        if (robotBase.isBind())
        {
            robotBase.setAngularVelocity(0);
            robotBase.setLinearVelocity(1f);
        }
    }

    public void onBaseLeft(View view)
    {
        if (robotBase.isBind())
        {
            robotBase.setLinearVelocity(0);
            robotBase.setAngularVelocity(-1f);
        }
    }

    public void onBaseRight(View view)
    {
        if (robotBase.isBind())
        {
            robotBase.setLinearVelocity(0);
            robotBase.setAngularVelocity(1f);
        }
    }

    public void onHeadUp(View view)
    {
        if (robotHead.isBind()) {
            float current = robotHead.getWorldPitch().getAngle();
            robotHead.setWorldPitch(current + step);
        }
    }

    public void onHeadDown(View view)
    {
        if (robotHead.isBind()) {
            float current = robotHead.getWorldPitch().getAngle();
            robotHead.setWorldPitch(current - step);
        }
    }

    public void onHeadLeft(View view)
    {
        if (robotHead.isBind()) {
            float current = robotHead.getHeadJointYaw().getAngle();
            robotHead.setHeadJointYaw(current - step);
        }
    }

    public void onHeadRight(View view)
    {
        if (robotHead.isBind()) {
            float current = robotHead.getHeadJointYaw().getAngle();
            robotHead.setHeadJointYaw(current + step);
        }
    }

    public void onReset(View view)
    {
        if (robotHead.isBind()) {
            robotHead.setMode(Head.MODE_SMOOTH_TACKING);
            robotHead.resetOrientation();
        }

        if (robotBase.isBind()) {
            int mode = robotBase.getControlMode();
            if (mode != Base.CONTROL_MODE_RAW) {
                robotBase.setControlMode(Base.CONTROL_MODE_RAW);
            }
            robotBase.cleanOriginalPoint();
            Pose2D pose = robotBase.getOdometryPose(-1);
            robotBase.setOriginalPoint(pose);
        }

        onUpdate(null);
    }

    public void onUpdate(View view)
    {
        Pose2D odometryPose;
        float x = 0, y = 0, theta = 0, yaw = 0, pitch = 0;

        if (robotBase.isBind()) {
            odometryPose = robotBase.getOdometryPose(-1);
            x = odometryPose.getX();
            y = odometryPose.getY();
            theta = odometryPose.getTheta();
        }
        if (robotHead.isBind()) {
            yaw = robotHead.getWorldYaw().getAngle();
            pitch = robotHead.getWorldPitch().getAngle();
        }

        String pos = String.format("X: %f, Y: %f, Theta: %f\nYaw: %f, Pitch:%f", x, y, theta, yaw, pitch);
        position.setText(pos);
    }
}
