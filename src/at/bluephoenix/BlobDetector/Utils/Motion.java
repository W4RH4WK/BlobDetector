package at.bluephoenix.BlobDetector.Utils;

public class Motion {

    public enum HookState {
        Up, Down;
    }

    public enum MotorState {
        Forward, HelpForward, Backward, Left, Right, Stop, Return;
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
        this.hookState = HookState.Up;
        this.motorState = MotorState.Stop;
    }

}
