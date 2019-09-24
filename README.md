# loomo-on-azure
A quick integration test for the Segway Loomo

## Background Informatio

- [Developer Docs](https://developer.segwayrobotics.com/developer/documents/segway-robots-sdk.html)
- [Loomo Samples](https://github.com/SegwayRoboticsSamples/)
- [Azure's IoT Client SDK for Android Devices](https://github.com/Azure/azure-iot-sdk-java/blob/master/doc/java-devbox-setup.md#building-for-android-device)
- [Android Docs](https://developer.android.com/training/basics/firstapp/)
- [Java Language Tutorials](https://docs.oracle.com/javase/tutorial/java/TOC.html)
- [IoT Central Sample](https://docs.microsoft.com/en-us/azure/iot-central/howto-connect-nodejs#add-a-real-device)

## Loomo's Coordinate System

![Reference Frame](docs/robot-reference-frame.jpg)

![Head Yaw Pitch Roll](docs/head-yaw-pitch-angle-range.jpg)

## Connecting to Azure

Be sure to edit the ```MainActivity.java``` file and set the ```connString``` near the top. IoTHub connection strings can be found in the [portal](https://portal.azure.com), for IoT Central you will need to [generate the connection string](https://docs.microsoft.com/en-us/azure/iot-central/tutorial-add-device#generate-the-connection-string)

## Telemetry

Loomo sends the following telemetry data to the cloud

```json
{
  "type":"telemetry",
  "Base":{
    "OdometryPose":{
      "AngularVelocity":2.9422954E-06,
      "LinearVelocity":-7.0900177E-07,
      "Theta":1.2450294,
      "X":7.1827145,
      "Y":18.051867
    },
    "AngularVelocity":0.0029737677,
    "AngularVelocityLimit":0.0,
    "CartMile":56,
    "ControlMode":0,
    "LightBrightness":68,
    "LinearVelocity":0.0,
    "LinearVelocityLimit":5.0,
    "Mileage":388773.0,
    "RidingSpeedLimit":5.0,
    "RobotPower":100,
    "UltrasonicDistance":529.0,
    "UltrasonicObstacleAvoidanceDistance":1.2,
    "BodyLightOpen":0,
    "CartModeWheelSlip":1,
    "InCartMode":0,
    "RidingInSpeedLimit":0,
    "UltrasonicObstacleAvoidanceEnabled":0
  },
  "AngularVelocity":0.0029737677,
  "LinearVelocity":0.0,
  "CartMile":56,
  "Mileage":388773.0,
  "RobotPower":100,
  "LightBrightness":68,
  "UltrasonicDistance":529.0,
  "ControlMode":0,
  "Head":{
    "HeadJointPitch":-1.4084424,
    "HeadJointRoll":0.0,
    "HeadJointYaw":-0.35932684,
    "HeadMode":8,
    "HeadPitchAngularVelocity":0.0,
    "HeadRollAngularVelocity":0.0,
    "HeadWorldPitch":-1.3213676,
    "HeadWorldRoll":0.06760753,
    "HeadWorldYaw":0.0,
    "HeadYawAngularVelocity":0.0
  },
  "HeadMode":8,
  "HeadJointPitch":-1.4084424,
  "HeadJoingYaw":-0.35932684,
  "Sensor":{
    "BasePose":{
      "Yaw":-1.4720912,
      "Pitch":-0.025982592,
      "Roll":0.0028763018
    },
    "BaseTicks":{
      "LeftTicks":7034,
      "RightTicks":6770
    },
    "BaseWheelInfo":{
      "LeftSpeed":0,
      "RightSpeed":0
    },
    "HeadJointYaw":-0.35932684,
    "HeadJointPitch":-1.4084424,
    "HeadJointRoll":0.0,
    "HeadWorldYaw":0.0,
    "HeadWorldYawTimestamp":1568354397671000,
    "HeadWorldPitch":-1.3213676,
    "HeadWorldPitchTimestamp":1568354397671000,
    "HeadWorldRoll":0.06760753,
    "HeadWorldRollTimestamp":1568354397671000,
    "InfraredData":{
      "LeftDistance":766.0,
      "RightDistance":1250.0
    },
    "Pose2D":{
      "AngularVelocity":0.0029737677,
      "LinearVelocity":-0.000685636,
      "Theta":1.2450294,
      "X":7.1827145,
      "Y":18.051867
    },
    "Frame":{
      "BaseX":7.1827145,
      "BaseY":18.051867,
      "BaseTheta":0.0,
      "BaseErrCode":0,
      "HeadYaw":1.2531079,
      "HeadPitch":-0.85681164,
      "HeadErrCode":0
    },
    "UltrasonicDistance":529.0
  },
  "EventProcessedUtcTime":"2019-09-13T05:59:57.4725206Z",
  "PartitionId":1,
  "EventEnqueuedUtcTime":"2019-09-13T05:59:57.3890000Z",
  "IoTHub":{
    "MessageId":"7cfa5987-acdc-4fa3-b6f5-26e838390bc2",
    "CorrelationId":"b3bc02a3-9dba-444e-a368-a61fc7389411",
    "ConnectionDeviceId":"robot",
    "ConnectionDeviceGenerationId":"637008302286304863",
    "EnqueuedTime":"2019-09-13T05:59:57.3840000Z",
    "StreamId":null
  }
}
```

## Cloud to Device messages

Loomo can respond to messages from the cloud in the following form

```json
{
  "type": "move", 
  "linear": 0, 
  "angular": 1
}

{
  "type": "look",
  "yaw": 1.5,
  "pitch": 0.7
}

{
  "type": "socket",
  "address": "192.168.0.100",
  "port": 9000,
  "cadence": 1000
}
```

## Current Issues

See [issues](https://github.com/seank-com/loomo-on-azure/issues)

## Explanation of Segway APIs by package

### Base

