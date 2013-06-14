package at.bluephoenix.BlobDetector;

import android.util.Log;
import at.bluephoenix.BlobDetector.Utils.Blob;
import at.bluephoenix.BlobDetector.Utils.FiniteStateMachine;
import at.bluephoenix.BlobDetector.Utils.Motion.HookState;
import at.bluephoenix.BlobDetector.Utils.Motion.MotorState;

public class CaptureBall extends FiniteStateMachine {
    private enum State {
        Scan {
            public State run() {
                NervHub data = NervHub.getInstance();
                Blob b = null;

                try {
                    b = data.getBlobs().get(0);
                } catch (IndexOutOfBoundsException e) {
                    return ScanRotate;
                }

                // check for minimum area
                if (b.getArea() >= 1000) {
                    data.setTarget(b);
                    return Rotate;
                } else {
                    return ScanRotate;
                }
            }
        },
        ScanRotate {
            public State run() {
                NervHub.getInstance().getMotion()
                        .setMotorState(MotorState.Left);
                return Scan;
            }
        },
        Rotate {
            public State run() {
                NervHub data = NervHub.getInstance();

                Double targetAngle = data.getTarget().getAngle();

                Log.i(BlobDetector.TAG, "Cb: target angle " + targetAngle);

                if (targetAngle < -8) {
                    data.getMotion().setMotorState(MotorState.Left);
                    return Scan;
                } else if (targetAngle > 8) {
                    data.getMotion().setMotorState(MotorState.Right);
                    return Scan;
                }

                return Advance;
            }
        },
        Advance {
            public State run() {
                NervHub data = NervHub.getInstance();

                Double targetDist = data.getTarget().getDistance();

                Log.i(BlobDetector.TAG, "Cb: target distance" + targetDist);

                if (targetDist > 22) {
                    data.getMotion().setMotorState(MotorState.Forward);
                    return Scan;
                } else {
                    data.getMotion().setMotorState(MotorState.Stop);
                    return HelpAdvance;
                }
            }
        },
        HelpAdvance {
            public State run() {
                NervHub.getInstance().getMotion()
                        .setMotorState(MotorState.HelpForward);
                return Capture;
            }
        },
        Capture {
            public State run() {
                NervHub data = NervHub.getInstance();
                data.getMotion().setHookState(HookState.Down);
                data.setMode(NervHub.appMode.GoHQ);
                return ScanHQ;
            }
        },
        ScanHQ {
            public State run() {
                NervHub data = NervHub.getInstance();
                if (data.getHqDist() != 0.0)
                    data.getMotion().setMotorState(MotorState.Right);
                return RotateHQ;
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
