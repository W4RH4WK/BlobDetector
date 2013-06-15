package at.bluephoenix.BlobDetector;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import at.bluephoenix.BlobDetector.Utils.Blob;
import at.bluephoenix.BlobDetector.Utils.Motion;

public class NervHub {

    public enum appMode {
        CaptureBall, GoHQ
    };

    private Mat image = null;
    private Blob target = null;
    private Scalar targetColor = new Scalar(0, 100, 100);
    private List<Blob> blobs = null;
    private Mat homography = null;
    private Motion motion = new Motion();
    private double hqDist = 0.0;
    private double hqAngle = 0.0;
    private Enum<appMode> mode = NervHub.appMode.CaptureBall;
    private final static NervHub instance = new NervHub();

    public static NervHub getInstance() {
        return instance;
    }

    public Mat getImage() {
        return image;
    }

    public synchronized void setImage(Mat image) {
        this.image = image;
    }

    public Blob getTarget() {
        return target;
    }

    public void setTarget(Blob target) {
        this.target = target;
    }

    public Scalar getTargetColor() {
        return targetColor;
    }

    public void setTargetColor(Scalar targetColor) {
        this.targetColor = targetColor;
    }

    public List<Blob> getBlobs() {
        return blobs;
    }

    public synchronized void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }

    public Mat getHomography() {
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

    public Motion getMotion() {
        return motion;
    }

    public void setMotion(Motion motion) {
        this.motion = motion;
    }

    public double getHqDist() {
        return hqDist;
    }

    public void setHqDist(double hqDist) {
        this.hqDist = hqDist;
    }

    public double getHqAngle() {
        return hqAngle;
    }

    public void setHqAngle(double hqAngle) {
        this.hqAngle = hqAngle;
    }

    public Enum<appMode> getMode() {
        return mode;
    }

    public void setMode(Enum<appMode> mode) {
        this.mode = mode;
    }

}
