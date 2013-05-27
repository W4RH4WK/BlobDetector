package at.bluephoenix.BlobDetector.Utils;

import java.util.Comparator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import at.bluephoenix.BlobDetector.BlobDetector;
import at.bluephoenix.BlobDetector.NervHub;

public class Blob {

    // -------------------------------------------------- BASIC MEMBERS
    private Rect box = null;
    private Scalar color = null;

    public Rect getBox() {
        return box;
    }

    public Scalar getColor() {
        return color;
    }

    // -------------------------------------------------- DYNAMIC MEMBERS
    private Integer area = null;
    private Point center = null;
    private Point contact = null;
    private Point coords = null;
    private Double angle = null;
    private Double distance = null;

    /**
     * calculates the blobs area upon call.
     * 
     * @return blob area
     */
    public Integer getArea() {
        if (area == null)
            area = box.height * box.width;

        return area;
    }

    /**
     * calculates the blobs center of mass upon call.
     * 
     * @return center of mass
     */
    public Point getCenter() {
        if (center == null)
            center = new Point(box.x + box.width / 2, box.y + box.height / 2);

        return center;
    }

    /**
     * calculates the blobs contact point upon call.
     * 
     * @return contact point
     */
    public Point getContact() {
        if (contact == null)
            contact = new Point(box.x + box.width / 2, box.y + box.height);

        return contact;
    }

    /**
     * calculates real world coords of the contact point. Throws a
     * NullPointerException if no homography is set.
     * 
     * @return real world coords
     * @throws NullPointerException
     */
    public Point getCoords() throws NullPointerException {
        if (coords == null)
            coords = BlobDetector.calcEgoCentCoords(getContact(), NervHub
                    .getInstance().getHomography());

        return coords;
    }

    /**
     * calculates the angle respectivly to the view points center.
     * 
     * @return angle
     */
    public Double getAngle() {
        if (angle == null)
            angle = BlobDetector.calcEgoCentAngle(getCenter());

        return angle;
    }

    /**
     * calucaltes the distance to the object. Throws a NullPointerException if
     * no homography is set.
     * 
     * @return distance to object
     */
    public Double getDistance() throws NullPointerException {
        if (distance == null) {
            Point c = getCoords();
            distance = Math.sqrt(c.x * c.x + c.y * c.y);
        }

        return distance;
    }

    // -------------------------------------------------- CONSTRUCTOR
    public Blob(MatOfPoint contour, Scalar color) {
        this(Imgproc.boundingRect(contour), color);
    }

    public Blob(Rect box, Scalar color) {
        this.color = color;
        this.box = box;
    }

    // -------------------------------------------------- METHODS
    public void drawTo(Mat Rgba) {
        Core.rectangle(Rgba, box.tl(), box.br(), new Scalar(255, 0, 0, 255));
        Core.circle(Rgba, getCenter(), 5, new Scalar(0, 0, 255, 255));
        Core.circle(Rgba, getContact(), 5, new Scalar(0, 255, 0, 255));
    }

    // -------------------------------------------------- COMPARATORS
    public static class compareArea implements Comparator<Blob> {
        @Override
        public int compare(Blob arg0, Blob arg1) {
            return arg0.getArea().compareTo(arg1.getArea());
        }
    }
}
