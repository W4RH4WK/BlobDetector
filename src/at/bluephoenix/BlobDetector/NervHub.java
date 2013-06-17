package at.bluephoenix.BlobDetector;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import at.bluephoenix.BlobDetector.Utils.Blob;
import at.bluephoenix.BlobDetector.Utils.Motion;

public class NervHub {

    // ------------------------------------------------------------ IMAGE
    /**
     * this member holds the current rgba image matrix.
     */
    private Mat image = null;

    public synchronized Mat getImage() {
        return image;
    }

    public synchronized void setImage(Mat image) {
        this.image = image;
    }

    // ------------------------------------------------------------ TARGET
    /**
     * this member holds the current target.
     */
    private Blob target = null;

    public synchronized Blob getTarget() {
        return target;
    }

    public synchronized void setTarget(Blob target) {
        this.target = target;
    }

    // ------------------------------------------------------------ TARGET COLOR
    /**
     * this member holds the color we look for.
     */
    private Scalar targetColor = new Scalar(0, 100, 100);

    public synchronized Scalar getTargetColor() {
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

    public synchronized List<Blob> getBlobs() {
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

    // ------------------------------------------------------------ MOTION
    /**
     * this object determines the robots desired motion.
     */
    private Motion motion = null;

    public synchronized Motion getMotion() {
        if (motion == null)
            motion = Motion.getInstance();

        return motion;
    }

    // ------------------------------------------------------------ go hq

    private double hqDist = 0.0;
    private double hqAngle = 0.0;

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

    // ------------------------------------------------------------ SINGELTON
    private final static NervHub instance = new NervHub();

    public static NervHub getInstance() {
        return instance;
    }
}
