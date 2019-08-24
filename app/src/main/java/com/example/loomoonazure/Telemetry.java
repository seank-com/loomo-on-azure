package com.example.loomoonazure;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.tf.AlgoTfData;
import com.segway.robot.sdk.locomotion.head.Head;
//import com.segway.robot.sdk.locomotion.sbv.AngularVelocity;
import com.segway.robot.sdk.locomotion.sbv.AngularVelocity;
import com.segway.robot.sdk.locomotion.sbv.Base;
//import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
import com.segway.robot.sdk.locomotion.sbv.BasePose;
import com.segway.robot.sdk.locomotion.sbv.BaseTicks;
import com.segway.robot.sdk.locomotion.sbv.BaseWheelInfo;
import com.segway.robot.sdk.locomotion.sbv.LinearVelocity;
import com.segway.robot.sdk.perception.sensor.InfraredData;
import com.segway.robot.sdk.perception.sensor.RobotAllSensors;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.perception.sensor.UltrasonicData;

class Telemetry {

    private Base robotBase;
    private Head robotHead;
    private Sensor robotSensor;


    Telemetry(Base robotBase, Head robotHead, Sensor robotSensor)
    {
        this.robotBase = robotBase;
        this.robotHead = robotHead;
        this.robotSensor = robotSensor;
    }

    private void populateBase(JsonObject telemetry)
    {
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
    }

    private void populateHead(JsonObject telemetry)
    {
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
    }

    private void populateSensor(JsonObject telemetry)
    {
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

        sensor.addProperty("HeadWorldYaw", robotAllSensors.getHeadWorldYaw().getAngle());
        sensor.addProperty("HeadWorldPitch", robotAllSensors.getHeadWorldPitch().getAngle());
        sensor.addProperty("HeadWorldRoll", robotAllSensors.getHeadWorldRoll().getAngle());

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

        AlgoTfData tfBase = robotSensor.getTfData(Sensor.WORLD_ODOM_ORIGIN, Sensor.BASE_ODOM_FRAME, pose2D.getTimestamp(), 100);
        AlgoTfData tfHead = robotSensor.getTfData(Sensor.WORLD_ODOM_ORIGIN, Sensor.HEAD_POSE_P_R_FRAME, pose2D.getTimestamp(), 100);

        JsonObject frame = new JsonObject();
        frame.addProperty("BaseX", tfBase.t.x);
        frame.addProperty("BaseY", tfBase.t.y);
        frame.addProperty( "BaseTheta", tfBase.q.getYawRad());
        frame.addProperty("HeadYaw", tfHead.q.getYawRad());
        frame.addProperty("HeadPitch", tfHead.q.getPitchRad());
        sensor.add("Frame", frame);

        sensor.addProperty("UltrasonicDistance", robotAllSensors.getUltrasonicData().getDistance());
        telemetry.add("Sensor", sensor);
    }

    public String getMessage() {
        JsonObject telemetry = new JsonObject();

        populateBase(telemetry);
        populateHead(telemetry);
        populateSensor(telemetry);

        return telemetry.toString();
    }
}
