package at.bluephoenix.BlobDetector;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import at.bluephoenix.BlobDetector.Utils.Blob;
import at.bluephoenix.BlobDetector.Utils.Direction;
import at.bluephoenix.BlobDetector.Utils.Hook;

public class NervHub {

    // ------------------------------------------------------------ IMAGE
    /**
     * this member holds the current rgba image matrix.
     */
    private Mat image = null;

    public Mat getImage() {
        return image;
    }

    public synchronized void setImage(Mat image) {
        this.image = image;
    }

    // ------------------------------------------------------------ TARGET COLOR
    /**
     * this member holds the hsv color vector of the desired target.
     */
    private Scalar targetColor = new Scalar(0, 100, 100);

    public Scalar getTargetColor() {
        return targetColor;
    }

    public synchronized void setTargetColor(Scalar targetColor) {
        this.targetColor = targetColor;
    }

    // ------------------------------------------------------------ BLOBS
    /**
     * this list contains all found blobs sorted from biggest to smallest.
     */
    private List<Blob> blobs = null;

    public List<Blob> getBlobs() {
        return blobs;
    }

    public synchronized void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }

    // ------------------------------------------------------------ HOMOGRAPHY
    /**
     * this member contains the homography matrix.
     */
    private Mat homography = null;

    public Mat getHomography() {
        return homography;
    }

    public synchronized void setHomography(Mat homography) {
        this.homography = homography;
    }

    // ------------------------------------------------------------ TARGET BLOB
    /**
     * this member is a reference to his target.
     */
    private Blob targetBlob = null;

    public Blob getTargetBlob() {
        return targetBlob;
    }

    public synchronized void setTargetBlob(Blob targetBlob) {
        this.targetBlob = targetBlob;
    }

    // ------------------------------------------------------------ DIRECTION
    /**
     * this member is used to determine the robots desired movement.
     */
    private Direction direction = Direction.Stop;

    public Direction getDirection() {
        return direction;
    }

    public synchronized void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    // ------------------------------------------------------------ HOOK
    /**
     * this member is used to determine the robots desired arm / bar state.
     */
    private Hook hook = Hook.Up;

    public Hook getHook() {
        return hook;
    }

    public synchronized void setHook(Hook hook) {
        this.hook = hook;
    }

    // ------------------------------------------------------------ SINGELTON
    private final static NervHub instance = new NervHub();

    public static NervHub getInstance() {
        return instance;
    }
}
