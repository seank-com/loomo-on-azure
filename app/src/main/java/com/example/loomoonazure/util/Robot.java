package com.example.loomoonazure.util;

import android.location.Location;

public interface Robot {

    int EMOTE_BEHAVIOR_RANDOM = -1;
    int EMOTE_BEHAVIOR_LOOK_AROUND = 1;
    int EMOTE_BEHAVIOR_LOOK_COMFORT = 2;
    int EMOTE_BEHAVIOR_LOOK_CURIOUS = 3;
    int EMOTE_BEHAVIOR_LOOK_NO_NO = 4;
    int EMOTE_PHONE_CONNECT_SUCCESS = 11;
    int EMOTE_PHONE_CONNECT_FAIL = 12;
    int EMOTE_BEHAVIOR_ROBOT_WAKE_UP = 21;
    int EMOTE_BEHAVIOR_BOOT_WAKEUP = 22;
    int EMOTE_BEHAVIOR_LOOK_UP = 31;
    int EMOTE_BEHAVIOR_LOOK_DOWN = 32;
    int EMOTE_BEHAVIOR_LOOK_LEFT = 33;
    int EMOTE_BEHAVIOR_LOOK_RIGHT = 34;
    int EMOTE_BEHAVIOR_TURN_LEFT = 35;
    int EMOTE_BEHAVIOR_TURN_RIGHT = 36;
    int EMOTE_BEHAVIOR_TURN_AROUND = 37;
    int EMOTE_BEHAVIOR_TURN_FULL = 38;
    int EMOTE_BEHAVIOR_WOW_EMOTION = 101;
    int EMOTE_BEHAVIOR_LIKE_EMOTION = 102;
    int EMOTE_BEHAVIOR_LOVE_EMOTION = 103;
    int EMOTE_BEHAVIOR_LOSE_EMOTION = 104;
    int EMOTE_BEHAVIOR_HALO_EMOTION = 105;
    int EMOTE_BEHAVIOR_HELLO_EMOTION = 151;
    int EMOTE_BEHAVIOR_CURIOUS_EMOTION = 152;
    int EMOTE_BEHAVIOR_BLINK_EMOTION = 153;

    int MOVEMENT_BEHAVIOR_MOVE_VELOCITY = 1;
    int MOVEMENT_BEHAVIOR_MOVE_TARGET = 2;

    int LOOK_BEHAVIOR_TARGET = 1;

    int TRACK_BEHAVIOR_WATCH = 1;
    int TRACK_BEHAVIOR_FOLLOW = 2;

    int ACTION_TYPE_EMOTE = 1;
    int ACTION_TYPE_MOVE = 2;
    int ACTION_TYPE_LOOK = 3;
    int ACTION_TYPE_TRACK = 4;

    String getStatus();

    // Robot Properties
    int getCadence();
    void setCadence(int cadence);
    Location getLocation();
    void setLocation(Location location);
    String getState();
    void setState(String state);
    int getPeopleDetected();
    void setPeopleDetected(int peopleCount);
    boolean getDebug();
    void setDebug(boolean debug);

    // higher order functions
    void actionSay(String phrase);
    void actionDo(RobotAction action);

    // Other
    void establishSocketConnection(String server, int port, int cadence);
}
