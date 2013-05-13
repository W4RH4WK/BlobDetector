package at.bluephoenix.BlobDetector;

abstract public class FiniteStateMachine implements Runnable {

    abstract public void exec();

    abstract public boolean isFinished();
    
    abstract public void run();
}