package at.bluephoenix.BlobDetector;

import java.util.Comparator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Blob {

    private Integer area;

    public Integer getArea() {
        return area;
    }

    private Scalar color;

    public Scalar getColor() {
        return color;
    }

    private Point center;

    public Point getCenter() {
        return center;
    }

    private Point contact;

    public Point getContact() {
        return contact;
    }

    private Rect box;

    public Rect getBox() {
        return box;
    }

    private MatOfPoint contour;

    public MatOfPoint getContour() {
        return contour;
    }

    public Blob(MatOfPoint contour, Scalar color) {
        this.contour = contour;
        this.color = color;
        this.box = Imgproc.boundingRect(contour);
        this.area = box.width * box.height;
        this.center = new Point(box.x + box.width / 2, box.y + box.height / 2);
        this.contact = new Point(box.x + box.width / 2, box.y + box.height);
    }

    public void drawTo(Mat Rgba) {
        Core.rectangle(Rgba, box.tl(), box.br(), new Scalar(255, 0, 0, 255));
        Core.circle(Rgba, center, 5, new Scalar(0, 0, 255, 255));
        Core.circle(Rgba, contact, 5, new Scalar(0, 255, 0, 255));
    }

    public static class compareArea implements Comparator<Blob> {
        @Override
        public int compare(Blob arg0, Blob arg1) {
            return arg0.getArea().compareTo(arg1.getArea());
        }
    }
}
