package at.bluephoenix.BlobDetector;

public class CaptureBall extends FiniteStateMachine {
    private enum State {
        Find {
            public State run() {
                // TODO find ball
                return Advance;
            }
        },
        Advance {
            public State run() {
                // TODO go to ball
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
                return End;
            }
        };

        abstract public State run();
    }
    
    private State state = State.Find;
    
    @Override
    public void exec() {
        state = state.run();
    }
    
    @Override
    public boolean isFinished() {
        return state.equals(State.End);
    }
}
