package at.bluephoenix.BlobDetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.view.MotionEvent;

import at.bluephoenix.BlobDetector.Utils.Blob;

public class BlobDetector {

    // TAG for debug messages
    public static final String TAG = "BlobDetector";

    private static final Integer defaultAreaThreshold = 1000;
    private static final Scalar defaultColorTolerance = new Scalar(5, 40, 40);
    private static final Scalar calibrationColorTolerance = new Scalar(15, 100,
            100);
    private static final Double fov = 47.5;
    private static final Integer displayWidth = 800;

    /*
     * Following parameters are used for the calibration process. color defines
     * the HSV vector for the according color blob pos holds to postion in real
     * world and are used to calculate the homography matrix
     */
    private static final Scalar colorRed = new Scalar(8, 214, 186);
    private static final Scalar colorGreen = new Scalar(102, 181, 74);
    private static final Scalar colorBlue = new Scalar(150, 255, 75);
    private static final Scalar colorYellow = new Scalar(30, 230, 161);
    private static final Point posYellow = new Point(-8.6, 32.0);
    private static final Point posBlue = new Point(-5.3, 19.4);
    private static final Point posRed = new Point(1.4, 29.5);
    private static final Point posGreen = new Point(9.1, 22.5);

    /**
     * wrapper for findBlobs using defaultAreaThreshold and
     * defaultColorTolerance.
     */
    public static List<Blob> findBlobs(Mat rgbaFrame, Scalar color) {
        return findBlobs(rgbaFrame, color, defaultAreaThreshold,
                defaultColorTolerance);
    }

    /**
     * returns a sorted list of blobs with given color. The first element is the
     * blob with the biggest area.
     * 
     * @param rgbaFrame
     *            rgba matrix of the frame
     * @param color
     *            color to look for
     * @param areaThreshold
     *            blobs with area lower than this will be ignored
     * @param colorTolerance
     *            color tolerance for blob searching
     * 
     * @return sorted list (biggest first)
     */
    public static List<Blob> findBlobs(Mat rgbaFrame, Scalar color,
            Integer areaThreshold, Scalar colorTolerance) {
        // blur image
        Mat mPyrDown = new Mat();
        Imgproc.pyrDown(rgbaFrame, mPyrDown);
        Imgproc.pyrDown(mPyrDown, mPyrDown);

        // get HSV
        Mat mHsv = new Mat();
        Imgproc.cvtColor(mPyrDown, mHsv, Imgproc.COLOR_RGB2HSV_FULL);
        mPyrDown.release();

        // calc lower / upper color boundaries
        Scalar lower = new Scalar(color.val[0] - colorTolerance.val[0],
                color.val[1] - colorTolerance.val[1], color.val[2]
                        - colorTolerance.val[2]);

        Scalar upper = new Scalar(color.val[0] + colorTolerance.val[0],
                color.val[1] + colorTolerance.val[1], color.val[2]
                        + colorTolerance.val[2]);

        // calc threshold
        Mat mMask = new Mat();
        Core.inRange(mHsv, lower, upper, mMask);
        mHsv.release();

        // dilates
        Mat mDilate = new Mat();
        Imgproc.dilate(mMask, mDilate, new Mat());

        // get contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilate, contours, new Mat(),
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        mDilate.release();

        // fill blobs
        List<Blob> blobs = new ArrayList<Blob>();
        for (MatOfPoint m : contours) {
            Core.multiply(m, new Scalar(4, 4), m);
            Blob b = new Blob(m, color);
            if (b.getArea() >= areaThreshold)
                blobs.add(b);
        }
        Collections.sort(blobs, new Blob.compareArea());
        Collections.reverse(blobs);

        return blobs;
    }

    /**
     * find color of touched frame.
     * 
     * @param rgbaFrame
     *            the frame to search in
     * @param event
     *            touch event
     * @param width
     *            frame width
     * @param height
     *            frame height
     * 
     * @return a scalar containing the color (HSV)
     */
    public static Scalar findTouchedColor(Mat rgbaFrame, MotionEvent event,
            Integer width, Integer height) {

        int cols = rgbaFrame.cols();
        int rows = rgbaFrame.rows();

        int xOffset = (width - cols) / 2;
        int yOffset = (height - rows) / 2;

        int x = (int) event.getX() - xOffset;
        int y = (int) event.getY() - yOffset;

        // check if coords are good
        if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
            return null;

        Rect touchedRect = new Rect();

        // to left corner
        touchedRect.x = (x > 4) ? x - 4 : 0;
        touchedRect.y = (y > 4) ? y - 4 : 0;

        // size
        touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols
                - touchedRect.x;
        touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows
                - touchedRect.y;

        // get sub matrix from frame
        Mat touchedRegionRgba = rgbaFrame.submat(touchedRect);

        // convert to HSV
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv,
                Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        Scalar color = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < color.val.length; i++)
            color.val[i] /= pointCount;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return color;
    }

    /**
     * calibrate camera.
     * 
     * @param rgbaFrame
     *            rgba matrix of a frame
     * 
     * @return homography matrix
     */
    public static Mat calibrateCamera(Mat rgbaFrame) {
        Blob red;
        Blob green;
        Blob blue;
        Blob yellow;

        try {
            red = findBlobs(rgbaFrame, colorRed, 100, calibrationColorTolerance)
                    .get(0);
            green = findBlobs(rgbaFrame, colorGreen, 100,
                    calibrationColorTolerance).get(0);
            blue = findBlobs(rgbaFrame, colorBlue, 100,
                    calibrationColorTolerance).get(0);
            yellow = findBlobs(rgbaFrame, colorYellow, 100,
                    calibrationColorTolerance).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        Point[] pixels = { yellow.getCenter(), blue.getCenter(),
                red.getCenter(), green.getCenter(), };

        Point[] blobs = { posYellow, posBlue, posRed, posGreen };

        MatOfPoint2f src = new MatOfPoint2f();
        MatOfPoint2f dst = new MatOfPoint2f();
        src.fromArray(pixels);
        dst.fromArray(blobs);

        Mat h = Imgproc.getPerspectiveTransform(src, dst);

        src.release();
        dst.release();

        return h;
    }

    /**
     * get real world coords from display coords.
     * 
     * @param src
     *            point with image coords
     * @param homography
     *            homography matrix used for transformation
     * 
     * @return point with real world coords
     */
    public static Point displayToWorld(Point src, Mat homography) {
        Mat m1 = new Mat(1, 1, CvType.CV_32FC2);
        Mat m2 = new Mat(1, 1, CvType.CV_32FC2);
        m1.put(0, 0, new double[] { src.x, src.y });

        Core.perspectiveTransform(m1, m2, homography);

        Point ret = new Point(m2.get(0, 0)[0], m2.get(0, 0)[1]);

        m1.release();
        m2.release();

        return ret;
    }

    /**
     * calculate angle of a given point in the real world.
     * 
     * @param src
     *            source point with ego centric coords
     * 
     * @return angle relative to display center (- left / + right)
     */
    public static Double calcAngle(Point src) {
        double fact = fov / ((double) displayWidth);
        return src.x * fact - (fov / 2);
    }

}
