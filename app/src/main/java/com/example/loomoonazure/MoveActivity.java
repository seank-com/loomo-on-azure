package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.locomotion.head.Angle;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;

public class MoveActivity extends AppCompatActivity {

    private Base robotBase;
    private Head robotHead;
    private static final float step = (float)(Math.PI / 18f);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);

        robotBase = Base.getInstance();
        robotHead = Head.getInstance();
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
}
