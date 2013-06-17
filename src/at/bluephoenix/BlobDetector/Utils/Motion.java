package at.bluephoenix.BlobDetector.Utils;

public class Motion {

    public enum HookState {
        Up, Down;
    }

    public enum MotorState {
        Forward, HelpForward, Left, Right, Stop, ForwardHQ, RotateHQ;
    }

    private HookState hookState;

    public synchronized HookState getHookState() {
        return hookState;
    }

    public synchronized void setHookState(HookState hookState) {
        this.hookState = hookState;
    }

    private MotorState motorState;

    public synchronized MotorState getMotorState() {
        return motorState;
    }

    public synchronized void setMotorState(MotorState motorState) {
        this.motorState = motorState;
    }

    public Motion() {
        setHookState(HookState.Up);
        setMotorState(MotorState.Stop);
    }

}
