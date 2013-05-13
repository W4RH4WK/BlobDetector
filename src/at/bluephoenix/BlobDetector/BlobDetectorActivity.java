// Alex Hirsch
// (c) 2013 | UIBK

package at.bluephoenix.BlobDetector;

// java stuff
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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

/**
 * This class holds android activities as well as the camera handling
 */
public class BlobDetectorActivity extends Activity implements OnTouchListener,
        CvCameraViewListener2 {

    // information hub
    private NervHub data;

    // camera handler
    private CameraBridgeViewBase mOpenCvCameraView;

    // menu items
    private MenuItem mCalibrate;

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
        mCalibrate = menu.add("Calibrate");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(BlobDetector.TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mCalibrate) {
            Log.i(BlobDetector.TAG, "calibrating camera");
            data.setHomography(BlobDetector.calibrateCamera(data.getImage()));
            if (data.getHomography() == null)
                Log.w(BlobDetector.TAG, "calibration not successful");
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(BlobDetector.TAG, "touch event invoke");

        data.setTargetColor(BlobDetector.findTouchedColor(data.getImage(),
                event, mOpenCvCameraView.getWidth(),
                mOpenCvCameraView.getHeight()));

        Log.i(BlobDetector.TAG, "new targetcolor = " + data.getTargetColor().toString());

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

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        data.setImage(inputFrame.rgba());

        data.setBlobs(BlobDetector.findBlobs(data.getImage(),
                data.getTargetColor()));

        // draw some blobs
        Mat frame = data.getImage().clone();
        for (int i = 0; i < 3; i++) {
            try {
                data.getBlobs().get(i).drawTo(frame);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        // draw info
        if (data.getTargetColor() != null) {
            Scalar c = data.getTargetColor();
            String sz = String.format("[ %3.0f %3.0f %3.0f ]", c.val[0],
                    c.val[1], c.val[2]);
            Core.putText(frame, sz, new Point(20, 55), Core.FONT_HERSHEY_PLAIN,
                    1, new Scalar(255, 0, 0));
        }
        if (data.getHomography() == null) {
            Core.putText(frame, "not calibrated", new Point(20, 80),
                    Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
        } else {
            try {
                Point p = BlobDetector.displayToWorld(data.getBlobs().get(0)
                        .getContact(), data.getHomography());
                String sz = String.format("[ %3.2f %3.2f ]", p.x, p.y);
                Core.putText(frame, sz, new Point(20, 80),
                        Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0));
            } catch (IndexOutOfBoundsException e) {
                // ignore
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
}
