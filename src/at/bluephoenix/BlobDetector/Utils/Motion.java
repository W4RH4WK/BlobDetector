package at.bluephoenix.BlobDetector.Utils;

public class Motion {

    public enum HookState {
        Up, Down;
    }

    public enum MotorState {
        Forward, Backward, Left, Right, Stop;
    }

    private HookState hookState;

    public HookState getHookState() {
        return hookState;
    }

    public void setHookState(HookState hookState) {
        this.hookState = hookState;
    }

    private MotorState motorState;

    public MotorState getMotorState() {
        return motorState;
    }

    public void setMotorState(MotorState motorState) {
        this.motorState = motorState;
    }

    public Motion() {
        hookState = HookState.Up;
        motorState = MotorState.Stop;
    }
}
