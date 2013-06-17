package at.bluephoenix.BlobDetector;

import android.util.Log;
import at.bluephoenix.BlobDetector.Utils.FiniteStateMachine;
import at.bluephoenix.BlobDetector.Utils.Motion.HookState;
import at.bluephoenix.BlobDetector.Utils.Motion.MotorState;

public class CaptureBall extends FiniteStateMachine {
    private enum State {
        ScanHQ {
            public State run() {
                NervHub data = NervHub.getInstance();
//
//                if (data.getHqDist() != 0.0)
//                    return RotateHQ;
//
                data.getMotion().setMotorState(MotorState.Right);
                return ScanHQ;

            }
        },
        RotateHQ {
            public State run() {
                NervHub data = NervHub.getInstance();
                data.getMotion().setMotorState(MotorState.RotateHQ);
                return ForwardHQ;
            }
        },
        ForwardHQ {
            public State run() {
                NervHub data = NervHub.getInstance();
                data.getMotion().setMotorState(MotorState.ForwardHQ);
                return End;
            }
        },
        End {
            public State run() {
                NervHub data = NervHub.getInstance();
                data.getMotion().setHookState(HookState.Up);
                data.getMotion().setMotorState(MotorState.Stop);
                data.setTarget(null);
                return End;
            }
            
            
        };

        abstract public State run();
    }

    private State state = State.ScanHQ;

    @Override
    public void exec() {
        state = state.run();
        Log.i(BlobDetector.TAG, "Cb: " + state);
    }

    @Override
    public boolean isFinished() {
        return state.equals(State.End);
    }

    @Override
    public void run() {
        CaptureBall cb = new CaptureBall();
        NervHub.getInstance().getMotion().setHookState(HookState.Up);
        while (!cb.isFinished()) {
            cb.exec();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
