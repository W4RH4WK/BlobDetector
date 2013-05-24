package at.bluephoenix.BlobDetector;

import java.util.List;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

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
    private CameraBridgeViewBase mOpenCvCameraView;

    // menu items
    private MenuItem mCalibrate;
    private MenuItem mSetHomgraphy;
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
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.blob_detector_view);
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
        mRun = menu.add("Run");
        mCalibrate = menu.add("Calibrate");
        mSetHomgraphy = menu.add("Set homgraphy");
        mBeaconMode = menu.add("Beacon mode");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(BlobDetector.TAG, "called onOptionsItemSelected; selected item: "
                + item);
        if (item == mCalibrate) {
            Log.i(BlobDetector.TAG, "calibrating camera");
            data.setHomography(BlobDetector.calibrateCamera(data.getImage()));
            if (data.getHomography() == null)
                Log.w(BlobDetector.TAG, "calibration not successful");
        } else if (item == mSetHomgraphy) {
            Log.i(BlobDetector.TAG, "set homography");
            // float homography[] = new float[] { 0.024397933159663418f,
            // 0.11604084560949147f, 20.214011072917067f,
            // 0.15628670103383063f, 0.3225423235139476f,
            // 3.181678904735975f, 0.007862597447162556f,
            // 0.012476325460466571f, 1.0f };
            float homography[] = new float[] { 0.021040694258707436f,
                    0.013574380850614673f, -9.12896273953139f,
                    0.018800192214116166f, -0.12060655806359954f,
                    35.527793344275615f, 9.272118259780662E-4f,
                    -0.0034178208136264937f, 1.0f };
            Mat m = new Mat(3, 3, CvType.CV_32FC1);
            m.put(0, 0, homography);

            data.setHomography(m);
            if (data.getHomography() == null)
                Log.w(BlobDetector.TAG, "calibration not successful");

        } else if (item == mRun) {
            new Thread(new CaptureBall()).start();
        } else if (item == mBeaconMode) {
            this.displayBeacon = true;
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
    }

    @Override
    public void onCameraViewStopped() {
        data.getImage().release();
    }

    private Beacon tempBeacon(Mat frame, Scalar top, Scalar bottom) {
        List<Blob> blues = BlobDetector.findBlobs(frame, top);
        List<Blob> yellows = BlobDetector.findBlobs(frame, bottom);

        Beacon beacon = BlobDetector.findBeacon(blues, yellows);

        for (Blob b : blues)
            b.drawTo(frame);

        for (Blob b : yellows)
            b.drawTo(frame);

        if (beacon != null)
            beacon.drawTo(frame);
        return beacon;

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        data.setImage(inputFrame.rgba());
        Mat frame;
        if (!displayBeacon) {
            data.setBlobs(BlobDetector.findBlobs(data.getImage(),
                    data.getTargetColor()));

            // draw some blobs
            frame = data.getImage().clone();
            for (int i = 0; i < 3; i++) {
                try {
                    data.getBlobs().get(i).drawTo(frame);
                    break;
                } catch (IndexOutOfBoundsException e) {

                }
            }

            // draw info
            if (data.getTargetColor() != null) {
                Scalar c = data.getTargetColor();
                String sz = String.format("[ %3.0f %3.0f %3.0f ]", c.val[0],
                        c.val[1], c.val[2]);
                Core.putText(frame, sz, new Point(20, 55),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
            }
            if (data.getHomography() == null) {
                Core.putText(frame, "not calibrated", new Point(20, 80),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
            } else {
                try {
                    data.setTarget(data.getBlobs().get(0));

                    Point p = BlobDetector.displayToWorld(data.getTarget()
                            .getContact(), data.getHomography());

                    String sz = String.format("[ %3.2f %3.2f ]", p.x, p.y);
                    Core.putText(frame, sz, new Point(20, 80),
                            Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
                } catch (IndexOutOfBoundsException e) {
                    data.setTarget(null);
                }
            }

            Core.putText(frame, data.getMotion().getMotorState().toString(),
                    new Point(20, 105), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(
                            255, 0, 0));

            if (data.getTarget() != null) {
                Core.putText(frame, data.getTarget().getDistance().toString(),
                        new Point(20, 125), Core.FONT_HERSHEY_PLAIN, 1,
                        new Scalar(255, 0, 0));
            }
        } else {
            frame = inputFrame.rgba();

             Scalar blue = new Scalar(150, 254, 72);
             Scalar yellow = new Scalar(36, 216, 148);
             Beacon left = tempBeacon(frame, blue, yellow);
             Beacon right = tempBeacon(frame, yellow, blue);

            if (right != null && left != null)
                Core.putText(frame, BlobDetector.getPosition(left, right)
                        .toString(), new Point(20, 55),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
            else
                Core.putText(frame, "Beacons not found", new Point(20, 55),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
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
