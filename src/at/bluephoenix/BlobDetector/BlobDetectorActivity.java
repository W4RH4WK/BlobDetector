package at.bluephoenix.BlobDetector;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

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
    private MenuItem mCali;
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
        mCali = menu.add("Calibrate");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(BlobDetector.TAG, "called onOptionsItemSelected; selected item: "
                + item);
        if (item == mRun) {
            new Thread(new CaptureBall()).start();
        } else if (item == mBeaconMode) {
            if (displayBeacon)
                this.displayBeacon = false;
            else
                this.displayBeacon = true;
        } else if (item == mCali) {
            float homography[] = new float[] { -0.48386050279626275f,
                    0.042196191784106295f, 179.741367424205f,
                    -0.022681435707994146f, 0.18180566396258296f,
                    -349.760864270296f, -0.0010800683670473294f,
                    -0.02812441920320001f, 1.0f };

            Mat h = new Mat(3, 3, CvType.CV_32FC1);
            h.put(0, 0, homography);
            data.setHomography(h);
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

    private Beacon tempBeacon(Mat frame, Scalar topcolor, Scalar botcolor) {
        List<Blob> top = BlobDetector.findBlobs(frame, topcolor);
        List<Blob> bot = BlobDetector.findBlobs(frame, botcolor);

        Beacon beacon = BlobDetector.findBeacon(bot, top);

        for (Blob b : top)
            b.drawTo(frame);

        for (Blob b : bot)
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

                    Point p = BlobDetector.calcEgoCentCoords(data.getTarget()
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

            Scalar red = new Scalar(5, 199, 131);
            Scalar blue = new Scalar(158, 255, 145);
            Scalar green = new Scalar(112, 255, 35);

            Beacon right = tempBeacon(frame, blue, red);
            Beacon left = tempBeacon(frame, green, blue);

            if (left != null)
                Core.putText(frame, "Left Beacon Okey", new Point(20, 80),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
            if (right != null)
                Core.putText(frame, "Right Beacon Okey", new Point(20, 100),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));

            if (right != null && left != null) {
                left.setAbsCoords(new Point(225, 150));
                right.setAbsCoords(new Point(300, 150));
                Point p = BlobDetector.calcAbsCoords(left, right);
                Core.putText(frame, p.x + "/" + p.y, new Point(20, 55),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
                Log.i("beacon", p.x + "/" + p.y + "");
            } else {
                Core.putText(frame, "Beacons not found", new Point(20, 55),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
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
