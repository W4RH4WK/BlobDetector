package at.bluephoenix.BlobDetector;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class NervHub {

    private Mat image = null;

    public Mat getImage() {
        return image;
    }

    public void setImage(Mat image) {
        synchronized (this.image) {
            this.image = image;
        }
    }

    private Scalar targetColor = new Scalar(0, 100, 100);

    public Scalar getTargetColor() {
        return targetColor;
    }

    public void setTargetColor(Scalar targetColor) {
        synchronized (this.targetColor) {
            this.targetColor = targetColor;
        }
    }

    private List<Blob> blobs = null;

    public List<Blob> getBlobs() {
        return blobs;
    }

    public void setBlobs(List<Blob> blobs) {
        synchronized (this.blobs) {
            this.blobs = blobs;
        }
    }

    private Mat homography = null;

    public Mat getHomography() {
        return homography;
    }

    public void setHomography(Mat homography) {
        synchronized (this.homography) {
            this.homography = homography;   
        }
    }
    
    private Blob targetBlob = null;

    public Blob getTargetBlob() {
        return targetBlob;
    }

    public void setTargetBlob(Blob targetBlob) {
        synchronized (this.targetBlob) {
            this.targetBlob = targetBlob;   
        }
    }
}
