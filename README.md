# loomo-on-azure
A quick integration test for the Segway Loomo

## Approach

Following the [Loomo developer docs](https://developer.segwayrobotics.com/developer/documents/segway-robots-sdk.html) I'm incorporating elements of the [LocomotionSample](https://github.com/SegwayRoboticsSamples/LocomotionSample) together with the [TrackImitator_Robot](https://github.com/SegwayRoboticsSamples/TrackImitator_Robot) and [Azure's IoT Client SDK for Android Devices](https://github.com/Azure/azure-iot-sdk-java/blob/master/doc/java-devbox-setup.md#building-for-android-device). The initial idea is to feed telemetry as demonstrated in the first sample to Azure IoTHub and accept Cloud to Device messages to turn (yaw and pitch) the head and move and turn the base.

## Connecting to Azure

Be sure to edit the ```MainActivity.java``` file and set the ```connString``` near the top.
## Cloud to Device messages

```json
{
  "type": "move", 
  "linear": 0, 
  "angular": 1
}
```

## Current Issues

Uncommenting one of these lines in ```app/build.gradle```

```
//1-implementation 'com.segway.robot:robot-connectivity-sdk:0.5.104'
//2-implementation 'com.segway.robot:mobile-connectivity-sdk:0.5.104'
```

Will cause the following build errors when you also include the Azure IoT SDK.

```bash

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:checkDebugDuplicateClasses'.
> 1 exception was raised by workers:
  java.lang.RuntimeException: java.lang.RuntimeException: Duplicate class org.slf4j.ILoggerFactory found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.IMarkerFactory found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.Logger found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.LoggerFactory found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.MDC found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.MDC$1 found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.MDC$MDCCloseable found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.Marker found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.MarkerFactory found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.event.EventConstants found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.event.EventRecodingLogger found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.event.Level found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.event.LoggingEvent found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.event.SubstituteLoggingEvent found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.BasicMDCAdapter found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.BasicMDCAdapter$1 found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.BasicMarker found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.BasicMarkerFactory found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.FormattingTuple found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.MarkerIgnoringBase found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.MessageFormatter found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.NOPLogger found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.NOPLoggerFactory found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.NOPMDCAdapter found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.NamedLoggerBase found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.SubstituteLogger found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.SubstituteLoggerFactory found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.Util found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.Util$1 found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.helpers.Util$ClassContextSecurityManager found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.spi.LocationAwareLogger found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.spi.LoggerFactoryBinder found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.spi.MDCAdapter found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  Duplicate class org.slf4j.spi.MarkerFactoryBinder found in modules slf4j-api-1.7.21.jar (com.segway.robot:base-connectivity-sdk:0.5.104) and slf4j-api-1.7.25.jar (org.slf4j:slf4j-api:1.7.25)
  
  Go to the documentation to learn how to <a href="d.android.com/r/tools/classpath-sync-errors">Fix dependency resolution errors</a>.
```
