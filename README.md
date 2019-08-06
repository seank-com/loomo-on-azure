# loomo-on-azure
A quick integration test for the Segway Loomo

## Approach

Following the [Loomo developer docs](https://developer.segwayrobotics.com/developer/documents/segway-robots-sdk.html) I'm incorporating elements of the [LocomotionSample](https://github.com/SegwayRoboticsSamples/LocomotionSample) together with the [TrackImitator_Robot](https://github.com/SegwayRoboticsSamples/TrackImitator_Robot) and [Azure's IoT Client SDK for Android Devices](https://github.com/Azure/azure-iot-sdk-java/blob/master/doc/java-devbox-setup.md#building-for-android-device). The initial idea is to feed telemetry as demonstrated in the first sample to Azure IoTHub and accept Cloud to Device messages to turn (yaw and pitch) the head and move and turn the base.
