package at.bluephoenix.BlobDetector;

import android.util.Log;
import at.bluephoenix.BlobDetector.Utils.Blob;
import at.bluephoenix.BlobDetector.Utils.Direction;
import at.bluephoenix.BlobDetector.Utils.FiniteStateMachine;
import at.bluephoenix.BlobDetector.Utils.Hook;
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
                // TODO move robot by 45 deg
                return Scan;
            }
        },
        Advance {
            public State run() {
                NervHub data = NervHub.getInstance();

                Double targetAngle = data.getTarget().getAngle();

                Log.i(BlobDetector.TAG, "Cb: target angle " + targetAngle);

                if (targetAngle < -5) {
                    data.setDirection(Direction.Left);
                    return Advance;
                } else if (targetAngle > 5) {
                    data.setDirection(Direction.Right);
                    return Advance;
                }

                Double targetDist = data.getTarget().getDistance();

                Log.i(BlobDetector.TAG, "Cb: target distance" + targetDist);

                if (targetDist > 20) {
                    data.setDirection(Direction.Forward);
                    return Advance;
                } else {
                    data.setDirection(Direction.Stop);
                    return Capture;
                }
            }
        },
        Capture {
            public State run() {
                NervHub.getInstance().setHook(Hook.Down);
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
