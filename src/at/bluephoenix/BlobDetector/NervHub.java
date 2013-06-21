package at.bluephoenix.BlobDetector;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import at.bluephoenix.BlobDetector.Utils.Blob;

public class NervHub {

    public enum appMode {
        CaptureBall, GoHQ
    };

    private volatile Mat image = null;
    private volatile Blob target = null;
    private volatile Scalar targetColor = new Scalar(0, 100, 100);
    private volatile List<Blob> blobs = null;
    private volatile Mat homography = null;
    private volatile double hqDist = 0.0;
    private volatile double hqAngle = 0.0;
    private volatile appMode mode = NervHub.appMode.GoHQ;
    private final static NervHub instance = new NervHub();

    public synchronized static NervHub getInstance() {
        return instance;
    }

    public synchronized Mat getImage() {
        return image;
    }

    public synchronized void setImage(Mat image) {
        this.image = image;
    }

    public synchronized Blob getTarget() {
        return target;
    }

    public synchronized void setTarget(Blob target) {
        this.target = target;
    }

    public synchronized Scalar getTargetColor() {
        return targetColor;
    }

    public synchronized void setTargetColor(Scalar targetColor) {
        this.targetColor = targetColor;
    }

    public synchronized List<Blob> getBlobs() {
        return blobs;
    }

    public synchronized void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }

    public synchronized Mat getHomography() {
        if (homography == null) {
            float values[] = new float[] { -8.61411095e-01f, -2.21361369e-02f,
                    3.63964233e+02f, -4.41004671e-02f, 1.95918664e-01f,
                    -6.04419312e+02f, -1.69617182e-03f, -4.29790355e-02f, 1.0f };

            Mat h = new Mat(3, 3, CvType.CV_32FC1);
            h.put(0, 0, values);
            homography = h;
        }

        return homography;
    }

    public synchronized void setHomography(Mat homography) {
        this.homography = homography;
    }

    public synchronized double getHqDist() {
        return hqDist;
    }

    public synchronized void setHqDist(double hqDist) {
        this.hqDist = hqDist;
    }

    public synchronized double getHqAngle() {
        return hqAngle;
    }

    public synchronized void setHqAngle(double hqAngle) {
        this.hqAngle = hqAngle;
    }

    public synchronized appMode getMode() {
        return mode;
    }

    public synchronized void setMode(appMode mode) {
        this.mode = mode;
    }

}