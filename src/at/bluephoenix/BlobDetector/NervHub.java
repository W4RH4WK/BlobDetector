package at.bluephoenix.BlobDetector;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class NervHub {

    private Mat image = null;

    public Mat getImage() {
        return image;
    }

    public synchronized void setImage(Mat image) {
        this.image = image;
    }

    private Scalar targetColor = new Scalar(0, 100, 100);

    public Scalar getTargetColor() {
        return targetColor;
    }

    public synchronized void setTargetColor(Scalar targetColor) {
        this.targetColor = targetColor;
    }

    private List<Blob> blobs = null;

    public List<Blob> getBlobs() {
        return blobs;
    }

    public synchronized void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }

    private Mat homography = null;

    public Mat getHomography() {
        return homography;
    }

    public synchronized void setHomography(Mat homography) {
        this.homography = homography;
    }

    private Blob targetBlob = null;

    public Blob getTargetBlob() {
        return targetBlob;
    }

    public synchronized void setTargetBlob(Blob targetBlob) {
        this.targetBlob = targetBlob;
    }

    private final static NervHub instance = new NervHub();

    public static NervHub getInstance() {
        return instance;
    }
}
