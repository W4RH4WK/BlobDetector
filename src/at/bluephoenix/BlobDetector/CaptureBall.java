package at.bluephoenix.BlobDetector;

import org.opencv.core.Point;

public class CaptureBall extends FiniteStateMachine {
    private enum State {
        Scan {
            @SuppressWarnings("finally")
            public State run(NervHub data) {
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
            public State run(NervHub data) {
                // TODO move robot by 45 deg
                return Scan;
            }
        },
        Advance {
            public State run(NervHub data) {                
                Double targetAngle = BlobDetector.calcAngle(data.getTargetBlob().getCenter(), data.getImage().width());
                
                // TODO rotate robot until target is in center
                
                Point target = BlobDetector.displayToWorld(data.getTargetBlob().getContact(), data.getHomography());
                Double targetDist = Math.sqrt(target.x * target.x + target.y * target.y);
                
                // TODO move forward
                
                return Capture;
            }
        },
        Capture {
            public State run(NervHub data) {
                // TODO lower bar
                return Verify;
            }
        },
        Verify {
            public State run(NervHub data) {
                // TODO verify ball is captured
                return End;
            }
        },
        End {
            public State run(NervHub data) {
                return End;
            }
        };

        abstract public State run(NervHub data);
    }
    
    private State state = State.Scan;
    private NervHub data;
    
    public CaptureBall(NervHub data) {
        this.data = data;
    }
    
    @Override
    public void exec() {
        state = state.run(data);
    }
    
    @Override
    public boolean isFinished() {
        return state.equals(State.End);
    }
}
