package at.bluephoenix.BlobDetector.Utils;

import org.opencv.core.Point;

import at.bluephoenix.BlobDetector.BlobDetector;
import at.bluephoenix.BlobDetector.NervHub;

public class Target {

    private Blob blob;
    
    public Blob getBlob() {
        return blob;
    }
    
    private Point coords;

    public Point getCoords() {
        return coords;
    }

    public Target(Blob blob) {
        this.blob = blob;
        this.coords = BlobDetector.displayToWorld(blob.getContact(), NervHub
                .getInstance().getHomography());
    }

    public Double getAngle() {
        return BlobDetector.calcAngle(blob.getCenter());
    }

    public Double getDistance() {
        return Math.sqrt(coords.x * coords.x + coords.y * coords.y);
    }
}
