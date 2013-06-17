package at.bluephoenix.BlobDetector;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import at.bluephoenix.BlobDetector.Utils.Beacon;
import at.bluephoenix.BlobDetector.Utils.Blob;

/**
 * This class holds android activities as well as the camera handling
 */
public class BlobDetectorActivity extends IOIOActivity implements
        OnTouchListener, CvCameraViewListener2 {

    // information hub
    private NervHub data;

    // camera handler
    private BlobDetectorView mOpenCvCameraView;

    // menu items
    private MenuItem mRun;
    private MenuItem mBeaconMode;
    private boolean displayBeacon = false;

    public BlobDetectorActivity() {
        Log.i(BlobDetector.TAG, "Instantiated new" + this.getClass());
        data = NervHub.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.blob_detector_view);
        mOpenCvCameraView = (BlobDetectorView) findViewById(R.id.blob_detector_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
                mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(BlobDetector.TAG, "called onCreateOptionsMenu");
        mRun = menu.add("Catch them all");
        mBeaconMode = menu.add("Beacon mode");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(BlobDetector.TAG, "called onOptionsItemSelected; selected item: "
                + item);
        if (item == mRun) {
            new Thread(new CaptureBall()).start();
        } else if (item == mBeaconMode) {
            displayBeacon = !displayBeacon;
        }

        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(BlobDetector.TAG, "touch event invoke");

        data.setTargetColor(BlobDetector.findTouchedColor(data.getImage(),
                event, mOpenCvCameraView.getWidth(),
                mOpenCvCameraView.getHeight()));

        Log.i(BlobDetector.TAG, "new targetcolor = "
                + data.getTargetColor().toString());

        // skip subsequent touch events
        return false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        data.setImage(new Mat(height, width, CvType.CV_8UC4));
        mOpenCvCameraView.setMaxFrameSize(50, 50);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView
                .setWhiteBalance(Parameters.WHITE_BALANCE_INCANDESCENT);
    }

    @Override
    public void onCameraViewStopped() {
        data.getImage().release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        data.setImage(inputFrame.rgba());

        Mat frame = inputFrame.rgba().clone();

        if (!displayBeacon) {
            Core.putText(frame, "Catch ball mode", new Point(20, 30),
                    Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));

            data.setBlobs(BlobDetector.findBlobs(data.getImage(),
                    data.getTargetColor()));

            // draw some blobs
            for (int i = 0; i < 3; i++) {
                try {
                    data.getBlobs().get(i).drawTo(frame);
                    break;
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }

            // target color info
            if (data.getTargetColor() != null) {
                Scalar color = data.getTargetColor();
                Core.putText(frame, String.format(
                        "Color: [ %3.0f %3.0f %3.0f ]", color.val[0],
                        color.val[1], color.val[2]), new Point(20, 55),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
            }

            // target handling
            try {
                Blob target = data.getBlobs().get(0);
                data.setTarget(target);

                // print info
                Core.putText(frame, String.format(
                        "Target: [ %3.2f %3.2f ] -- %3.2f",
                        target.getCoords().x, target.getCoords().y,
                        target.getDistance()), new Point(20, 80),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
            } catch (IndexOutOfBoundsException e) {
                data.setTarget(null);
            }

            // motor state info
            Core.putText(frame, "Motor: "
                    + data.getMotion().getMotorState().toString(), new Point(
                    20, 105), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
        } else {
            Core.putText(frame, "Becon mode", new Point(20, 30),
                    Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));

            // color values IIS
            // Scalar red = new Scalar(5, 199, 131);
            // Scalar blue = new Scalar(151, 255, 116);
            // Scalar green = new Scalar(103, 207, 45);

            // home
            Scalar red = new Scalar(9, 203, 117);
            Scalar blue = new Scalar(153, 176, 63);
            Scalar green = new Scalar(80, 82, 36);

            // get blobs
            List<Blob> redBlobs = BlobDetector.findBlobs(frame, red);
            List<Blob> blueBlobs = BlobDetector.findBlobs(frame, blue);
            List<Blob> greenBlobs = BlobDetector.findBlobs(frame, green);

            // draw blobs
            for (Blob b : redBlobs)
                b.drawTo(frame);
            for (Blob b : blueBlobs)
                b.drawTo(frame);
            for (Blob b : greenBlobs)
                b.drawTo(frame);

            // get beacons
            Beacon blueGreen = BlobDetector.findBeacon(blueBlobs, greenBlobs);
            Beacon redBlue = BlobDetector.findBeacon(redBlobs, blueBlobs);
            Beacon greenBlue = BlobDetector.findBeacon(greenBlobs, blueBlobs);

            Beacon redGreen = BlobDetector.findBeacon(redBlobs, greenBlobs);
            Beacon blueRed = BlobDetector.findBeacon(blueBlobs, redBlobs);
            Beacon greenRed = BlobDetector.findBeacon(greenBlobs, redBlobs);

            // if found draw them and set pos
            List<Beacon> beacons = new ArrayList<Beacon>();
            if (blueGreen != null) {
                beacons.add(blueGreen);
                blueGreen.drawTo(frame);
                blueGreen.setAbsCoords(new Point(0, 0));
            }
            if (redBlue != null) {
                beacons.add(redBlue);
                redBlue.drawTo(frame);
                redBlue.setAbsCoords(new Point(75, 0));
            }
            if (greenBlue != null) {
                beacons.add(greenBlue);
                greenBlue.drawTo(frame);
                greenBlue.setAbsCoords(new Point(150, 0));
            }
            if (redGreen != null) {
                beacons.add(redGreen);
                redGreen.drawTo(frame);
                redGreen.setAbsCoords(new Point(0, 150));
            }
            if (blueRed != null) {
                beacons.add(blueRed);
                blueRed.drawTo(frame);
                blueRed.setAbsCoords(new Point(75, 150));
            }
            if (greenRed != null) {
                beacons.add(greenRed);
                greenRed.drawTo(frame);
                greenRed.setAbsCoords(new Point(150, 150));
            }

            // calc way to hq only once
            if (beacons.size() >= 2) {
                Beacon left = beacons.get(0);
                Beacon right = beacons.get(1);
                if (left.getAngle() > right.getAngle()) {
                    Beacon help = left;
                    left = right;
                    right = help;
                }

                // calc position
                Point pos = BlobDetector.calcAbsCoords(left, right);
                Double angle = BlobDetector.calcAbsViewAngle(pos, left);

                // print info
                Core.putText(frame, String.format(
                        "Pos: [ %3.2f %3.2f ] < %3.1f", pos.x, pos.y, angle),
                        new Point(20, 55), Core.FONT_HERSHEY_PLAIN, 1,
                        new Scalar(255, 0, 0));

                if (data.getHqDist() == 0.0) {
                    Point hq = new Point(13, 37);
                    data.setHqDist(Math.sqrt(Math.pow(pos.x - hq.x, 2)
                            + Math.pow(pos.y - hq.y, 2)));

                    data.setHqAngle(BlobDetector.calcAbsAngle(pos, hq)
                            - BlobDetector.calcAbsViewAngle(pos, left));
                }
                Core.putText(frame, data.getHqDist() + " <" + data.getHqDist(),
                        new Point(20, 80), Core.FONT_HERSHEY_PLAIN, 1,
                        new Scalar(255, 0, 0));

            }

        }

        return frame;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
            case LoaderCallbackInterface.SUCCESS:
                Log.i(BlobDetector.TAG, "OpenCV loaded successfully");
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.setOnTouchListener(BlobDetectorActivity.this);
                break;

            default:
                super.onManagerConnected(status);
                break;
            }
        }
    };

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new IOIOcontrol();
    }

}
