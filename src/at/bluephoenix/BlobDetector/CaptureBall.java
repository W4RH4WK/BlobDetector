package at.bluephoenix.BlobDetector;

import android.util.Log;
import at.bluephoenix.BlobDetector.Utils.Blob;
import at.bluephoenix.BlobDetector.Utils.FiniteStateMachine;
import at.bluephoenix.BlobDetector.Utils.Motion.HookState;
import at.bluephoenix.BlobDetector.Utils.Motion.MotorState;
import at.bluephoenix.BlobDetector.Utils.Target;

public class CaptureBall extends FiniteStateMachine {
    private enum State {
        Scan {
            public State run() {
                NervHub data = NervHub.getInstance();
                Blob b = null;

                try {
                    b = data.getBlobs().get(0);
                } catch (IndexOutOfBoundsException e) {
                    return Rotate;
                }

                // check for minimum area
                if (b.getArea() >= 1000) {
                    data.setTarget(new Target(b));
                    return Advance;
                } else {
                    return Rotate;
                }
            }
        },
        Rotate {
            public State run() {
                NervHub.getInstance().getMotion().setMotorState(MotorState.Left);
                return Scan;
            }
        },
        Advance {
            public State run() {
                NervHub data = NervHub.getInstance();

                Double targetAngle = data.getTarget().getAngle();

                Log.i(BlobDetector.TAG, "Cb: target angle " + targetAngle);

                if (targetAngle < -5) {
                    data.getMotion().setMotorState(MotorState.Left);
                    return Advance;
                } else if (targetAngle > 5) {
                    data.getMotion().setMotorState(MotorState.Right);
                    return Advance;
                }

                Double targetDist = data.getTarget().getDistance();

                Log.i(BlobDetector.TAG, "Cb: target distance" + targetDist);

                if (targetDist > 20) {
                    data.getMotion().setMotorState(MotorState.Forward);
                    return Advance;
                } else {
                    data.getMotion().setMotorState(MotorState.Stop);
                    return Capture;
                }
            }
        },
        Capture {
            public State run() {
                NervHub.getInstance().getMotion().setHookState(HookState.Down);
                return Verify;
            }
        },
        Verify {
            public State run() {
                // TODO verify ball is captured
                return End;
            }
        },
        End {
            public State run() {
                NervHub.getInstance().setTarget(null);
                return End;
            }
        };

        abstract public State run();
    }

    private State state = State.Scan;

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
