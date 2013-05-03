package at.bluephoenix.BlobDetector;

import org.opencv.core.Point;

public class CaptureBall extends FiniteStateMachine {
    private enum State {
        Scan {
            @SuppressWarnings("finally")
            public State run() {
                NervHub data = NervHub.getInstance();
                
                try {
                    Blob b = data.getBlobs().get(0);

                    // check for minimum area
                    if (b.getArea() >= 1000)
                        data.setTargetBlob(b);

                    return Advance;
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                } finally {
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
                
                Double targetAngle = BlobDetector.calcAngle(data
                        .getTargetBlob().getCenter(), data.getImage().width());

                // TODO rotate robot until target is in center

                Point target = BlobDetector.displayToWorld(data.getTargetBlob()
                        .getContact(), data.getHomography());
                Double targetDist = Math.sqrt(target.x * target.x + target.y
                        * target.y);

                // TODO move forward

                return Capture;
            }
        },
        Capture {
            public State run() {
                // TODO lower bar
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
                NervHub.getInstance().setTargetBlob(null);
                return End;
            }
        };

        abstract public State run();
    }

    private State state = State.Scan;

    @Override
    public void exec() {
        state = state.run();
    }

    @Override
    public boolean isFinished() {
        return state.equals(State.End);
    }
}
