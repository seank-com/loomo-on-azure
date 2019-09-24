package com.example.loomoonazure.util;

public class RobotAction {

    private int actionType;
    private int behavior;
    private float arg1;
    private float arg2;

    private RobotAction(int actionType, int behavior, float arg1, float arg2) {
        this.actionType = actionType;
        this.behavior = behavior;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public static RobotAction getEmote(int emoteBehavior) {
        return new RobotAction(Robot.ACTION_TYPE_EMOTE, emoteBehavior, Float.NaN, Float.NaN);
    }

    public static RobotAction getMovement(int movementBehavior, float linear, float angular) {
        return new RobotAction(Robot.ACTION_TYPE_MOVE, movementBehavior, linear, angular);
    }

    public static RobotAction getLook(float yaw, float pitch) {
        return new RobotAction(Robot.ACTION_TYPE_LOOK, Robot.LOOK_BEHAVIOR_TARGET, yaw, pitch);
    }

    public static RobotAction getTrack(int trackBehavior) {
        return new RobotAction(Robot.ACTION_TYPE_TRACK, trackBehavior, 0 ,0);
    }

    public int getActionType() {
        return actionType;
    }

    public int getBehavior() {
        return behavior;
    }

    public float getLinear() {
        if (actionType == Robot.ACTION_TYPE_MOVE) {
            return arg1;
        }
        return 0;
    }

    public float getAngular() {
        if (actionType == Robot.ACTION_TYPE_MOVE) {
            return arg2;
        }
        return 0;
    }

    public float getYaw() {
        if (actionType == Robot.ACTION_TYPE_LOOK) {
            return arg1;
        }
        return 0;
    }

    public float getPitch() {
        if (actionType == Robot.ACTION_TYPE_LOOK) {
            return arg2;
        }
        return 0;
    }
}
