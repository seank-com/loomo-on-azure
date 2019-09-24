package com.example.loomoonazure.util;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionState;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class AzureIoT implements MessageCallback, DeviceMethodCallback, TwinPropertyCallBack, IotHubEventCallback, IotHubConnectionStatusChangeCallback {
    private static final String TAG = "AzureIoT";

    public static final int METHOD_SUCCESS = 200;
    public static final int METHOD_NOT_DEFINED = 404;
    public static final int METHOD_INVALID_ARG = 500;

    public static final String TWIN_CONTEXT = "DeviceTwin";
    public static final String METHOD_CONTEXT = "DirectMethod";

    private DeviceClient client;
    private static AtomicBoolean Continue = new AtomicBoolean(false);
    private Handler handler;
    private int CONNECTION_OPEN;
    private int CONNECTION_CLOSED;
    private Robot robot;
    private boolean updateReported;
    private boolean isIoTCentral;

    private JsonParser parser = new JsonParser();

    public AzureIoT(Handler handler, int CONNECTION_OPEN, int CONNECTION_CLOSED, Robot robot)
    {
        Log.d(TAG, String.format("constructor threadId=%d", Thread.currentThread().getId()));

        this.handler = handler;
        this.CONNECTION_OPEN = CONNECTION_OPEN;
        this.CONNECTION_CLOSED = CONNECTION_CLOSED;
        this.robot = robot;
    }

    public void connect(String connectionString, boolean isIoTCentral) {
        Log.d(TAG, String.format("connect threadId=%d", Thread.currentThread().getId()));

        AzureIoT that = this;
        that.isIoTCentral = isIoTCentral;

        new Thread()  {
            @Override
            public void run() {
                try {
                    client = new DeviceClient(connectionString, IotHubClientProtocol.MQTT);
                    client.open();

                    client.registerConnectionStatusChangeCallback((IotHubConnectionStatusChangeCallback) that, null);

                    client.setMessageCallback(that, null);

                    client.subscribeToDeviceMethod(that, null, (IotHubEventCallback) that, METHOD_CONTEXT);

                    Continue.set(false);
                    updateReported = true;
                    client.startDeviceTwin((IotHubEventCallback) that, TWIN_CONTEXT, that, null);

                    do
                    {
                        Thread.sleep(1000);
                    }
                    while(!Continue.get());

                    client.subscribeToTwinDesiredProperties(null);

                    handler.sendEmptyMessage(CONNECTION_OPEN);
                } catch(Exception e) {
                    Log.e(TAG, String.format("Exception connnecting threadId=%d", Thread.currentThread().getId()), e);
                }
            }
        }.start();
    }

    public void close() {
        Log.d(TAG, String.format("close threadId=%d", Thread.currentThread().getId()));

        try {
            client.closeNow();
        } catch(Exception e) {
            Log.e(TAG, "Exception closing", e);
        }
    }

    public void sendMessage(String content) {
        Log.d(TAG, String.format("sendMessage threadId=%d", Thread.currentThread().getId()));

        Message msgObj = new Message(content);
        msgObj.setMessageId(UUID.randomUUID().toString());
        client.sendEventAsync(msgObj, this, "sendEventAsync");
    }

    public void updateTwin() {
        Log.d(TAG, String.format("sendMessage threadId=%d", Thread.currentThread().getId()));

        try {
            Set<Property> reported = new HashSet<Property>();
            Property cadence;
            if (isIoTCentral) {
                Set<Property> value = new HashSet<Property>();
                value.add(new Property("value", robot.getCadence()));
                // hopefully nothing else needs to go here
                cadence = new Property("cadence", value);
            } else {
                cadence = new Property("cadence", robot.getCadence());
            }
            reported.add(cadence);
            client.sendReportedProperties(reported);
        } catch(Exception e) {
            Log.e(TAG, "Exception reporting", e);
        }
    }

    // MessageCallback
    @Override
    public IotHubMessageResult execute(Message message, Object context) {
        Log.d(TAG, String.format("MessageCallback threadId=%d", Thread.currentThread().getId()));

        String txt = new String(message.getBytes());
        JsonElement element = parser.parse(txt);

        if (element.isJsonObject()) {
            JsonObject command = element.getAsJsonObject();
            if (command.has("type")) {
                if (command.get("type").getAsString().equals("move")) {
                    float linear = command.get("linear").getAsFloat();
                    float angular = command.get("angular").getAsFloat();
                    RobotAction ra = RobotAction.getMovement(Robot.MOVEMENT_BEHAVIOR_MOVE_VELOCITY, linear, angular);
                    robot.actionDo(ra);
                }
                else if (command.get("type").getAsString().equals("look")) {
                    float yaw = command.get("yaw").getAsFloat();
                    float pitch = command.get("pitch").getAsFloat();
                    RobotAction ra = RobotAction.getLook(yaw, pitch);
                    robot.actionDo(ra);
                }
                else if (command.get("type").getAsString().equals("socket")) {
                    String server = command.get("address").getAsString();
                    int port = command.get("port").getAsInt();
                    int cadence = command.get("cadence").getAsInt();
                    robot.establishSocketConnection(server, port, cadence);
                }
            }
        }
        return IotHubMessageResult.COMPLETE;
    }

    // DeviceMethodCallback
    @Override
    public DeviceMethodData call(String methodName, Object methodData, Object context) {
        Log.d(TAG, String.format("DeviceMethodCallback threadId=%d", Thread.currentThread().getId()));

        JsonObject parameters = new JsonObject();
        if (methodData != null) {
            String content = new String((byte[])methodData);
            JsonElement element = parser.parse(content);
            if (element.isJsonObject()) {
                parameters = element.getAsJsonObject();
            }
        }

        int result = METHOD_NOT_DEFINED;
        switch(methodName) {
            case "move":
                if (parameters.has("linear") && parameters.has("angular")) {
                    float linear = parameters.get("linear").getAsFloat();
                    float angular = parameters.get("angular").getAsFloat();
                    RobotAction ra = RobotAction.getMovement(Robot.MOVEMENT_BEHAVIOR_MOVE_VELOCITY, linear, angular);
                    robot.actionDo(ra);
                    result = METHOD_SUCCESS;
                } else {
                    result = METHOD_INVALID_ARG;
                }
                break;
            case "look":
                if (parameters.has("yaw") && parameters.has("pitch")) {
                    float yaw = parameters.get("yaw").getAsFloat();
                    float pitch = parameters.get("pitch").getAsFloat();
                    RobotAction ra = RobotAction.getLook(yaw, pitch);
                    robot.actionDo(ra);
                    result = METHOD_SUCCESS;
                } else {
                    result = METHOD_INVALID_ARG;
                }
                break;
            case "socket":
                if (parameters.has("address") && parameters.has("port") && parameters.has("cadence")) {
                    String server = parameters.get("address").getAsString();
                    int port = parameters.get("port").getAsInt();
                    int cadence = parameters.get("cadence").getAsInt();
                    robot.establishSocketConnection(server, port, cadence);
                    result = METHOD_SUCCESS;
                } else {
                    result = METHOD_INVALID_ARG;
                }
                break;
        }

        return new DeviceMethodData(result, "please see result code");
    }

    // TwinPropertyCallBack
    @Override
    public void TwinPropertyCallBack(Property property, Object context) {
        Log.d(TAG, String.format("TwinPropertyCallBack type=%s name=%s threadId=%d", (property.getIsReported()?"reported": "desired"), property.getKey(), Thread.currentThread().getId()));

        String propertyName = property.getKey();
        if (propertyName.equals("cadence")) {
            int cadence;
            if (isIoTCentral) {
                TwinCollection props = (TwinCollection)property.getValue();
                cadence = (int)Double.valueOf((Double)props.get("value")).doubleValue();
            } else {
                cadence = (int)Double.valueOf(property.getValue().toString()).doubleValue();
            }

            if (!property.getIsReported() || updateReported) {
                updateReported = false;
                robot.setCadence(cadence);
            }
        } else if (propertyName.equals("location")) {
            if (robot.getLocation() == null) {
                String value = property.getValue().toString();
                JsonElement payload = parser.parse(value);
                if (payload.isJsonObject()) {
                    JsonObject desiredLocation = payload.getAsJsonObject();
                    Location location = new Location(LocationManager.GPS_PROVIDER);

                    location.setLatitude(desiredLocation.get("lat").getAsDouble());
                    location.setLongitude(desiredLocation.get("lon").getAsDouble());

                    robot.setLocation(location);
                }
            }
        }
    }

    // IotHubEventCallback
    @Override
    public void execute(IotHubStatusCode responseStatus, Object context) {
        Log.d(TAG, String.format("IotHubEventCallback status=%s, context=%s threadId=%d",
            responseStatus.toString(), context.toString(), Thread.currentThread().getId()));

        if (context.toString() == TWIN_CONTEXT) {
            if((responseStatus == IotHubStatusCode.OK) || (responseStatus == IotHubStatusCode.OK_EMPTY))
            {
                Continue.set(true);
            }
        }
    }

    // IotHubConnectionStatusChangeCallback
    @Override
    public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
        Log.d(TAG, String.format("IotHubConnectionStateCallback status=%s statusChangeReason=%s threadId=%s", status.toString(), statusChangeReason.toString(), Thread.currentThread().getId()));

        if (status == IotHubConnectionStatus.DISCONNECTED) {
            handler.sendEmptyMessage(CONNECTION_CLOSED);
        }

    }
}
