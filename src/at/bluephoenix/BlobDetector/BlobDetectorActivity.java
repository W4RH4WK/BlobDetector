package at.bluephoenix.BlobDetector;

import java.text.DecimalFormat;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
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

    // looper sensor info
    private String analog[] = new String[9];
    @SuppressWarnings("unused")
    private short xPos;
    @SuppressWarnings("unused")
    private short yPos;
    @SuppressWarnings("unused")
    private short anglePos;

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
        mSetHomgraphy = menu.add("Set homgraphy");
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
            float homography[] = new float[] { 0.024397933159663418f,
                    0.11604084560949147f, 20.214011072917067f,
                    0.15628670103383063f, 0.3225423235139476f,
                    3.181678904735975f, 0.007862597447162556f,
                    0.012476325460466571f, 1.0f };

            Mat m = new Mat(3, 3, CvType.CV_32FC1);
            m.put(0, 0, homography);

            data.setHomography(m);
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
        Core.putText(frame, data.getMotion().getMotorState().toString(),
                new Point(20, 105), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255,
                        0, 0));

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

    class Looper extends BaseIOIOLooper {
        private TwiMaster twi;
        private NervHub data;
        private PwmOutput servo_;

        @Override
        protected void setup() throws ConnectionLostException,
                InterruptedException {
            super.setup();
            twi = ioio_.openTwiMaster(1, TwiMaster.Rate.RATE_100KHz, false);
            servo_ = ioio_.openPwmOutput(10, 50);
            data = NervHub.getInstance();
        }

        protected void robotForward(int fwd) {
            byte[] request = new byte[2];
            byte[] response = new byte[1];

            request[0] = 0x1C;
            request[1] = (byte) fwd;
            synchronized (twi) {
                try {
                    twi.writeRead(0x69, false, request, request.length,
                            response, 0);
                } catch (ConnectionLostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        protected void robotForward(double fwd) {
            robotForward((int) fwd);
        }

        protected void robotRotate(int grad) {
            byte[] request = new byte[2];
            byte[] response = new byte[1];
            request[0] = 0x1D; // cmd
            request[1] = (byte) grad;
            synchronized (twi) {
                try {
                    twi.writeRead(0x69, false, request, request.length,
                            response, 0);
                } catch (ConnectionLostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void robotRotate(double grad) {
            robotRotate((int) grad);
        }

        protected void robotMove(int leftMotorSpeed, int rightMotorSpeed) {
            byte[] request = new byte[3];
            byte[] response = new byte[1];
            request[0] = 0x1A;
            request[1] = (byte) rightMotorSpeed;
            request[2] = (byte) leftMotorSpeed;
            synchronized (twi) {
                try {
                    twi.writeRead(0x69, false, request, request.length,
                            response, 0);
                } catch (ConnectionLostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        protected void robotMove(int speed) {
            robotMove(speed, speed);
        }

        protected void robotLED(int blueLED, int redLED) {
            byte[] request = new byte[3];
            byte[] response = new byte[1];
            request[0] = 0x20;
            request[1] = (byte) redLED;// set between 0 and 100
            request[2] = (byte) blueLED;// set between 0 and 100
            synchronized (twi) {
                try {
                    twi.writeRead(0x69, false, request, request.length,
                            response, 0);
                } catch (ConnectionLostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        protected void robotLED(int intensity) {
            robotLED(intensity, intensity);
        }

        protected void gripper(boolean gripperState) {
            try {
                if (gripperState) {
                    servo_.setDutyCycle(0.0001f);

                } else {
                    servo_.setDutyCycle(0.0528f);

                }
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            }

        }

        protected void robotReadSensor() {
            byte[] request = new byte[] { 0x10 };
            byte[] response = new byte[8];
            synchronized (twi) {
                try {
                    twi.writeRead(0x69, false, request, request.length,
                            response, response.length);
                } catch (ConnectionLostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (int l = 0; l < 7; l++) {
                int i = 0xFF & response[l + 1];
                if (l != 0)
                    analog[l] = i + "cm";
                else
                    analog[l] = new DecimalFormat("#.#").format(i / 10.0) + "V";
            }

            request[0] = 0x1A; // get velocity
            synchronized (twi) {
                try {
                    twi.writeRead(0x69, false, request, request.length,
                            response, 2);
                } catch (ConnectionLostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            analog[7] = response[0] + "";
            analog[8] = response[1] + "";

            /* get position */
            request[0] = 0x1B; // get position
            synchronized (twi) {
                try {
                    twi.writeRead(0x69, false, request, request.length,
                            response, 6);
                } catch (ConnectionLostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            xPos = (short) (((response[1] & 0xFF) << 8) | (response[0] & 0xFF));
            yPos = (short) (((response[3] & 0xFF) << 8) | (response[2] & 0xFF));
            anglePos = (short) (((response[5] & 0xFF) << 8) | (response[4] & 0xFF));

        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            super.loop();

            // switch (data.getMotion().getHookState()) {
            // case Down:
            // gripper(true);
            // break;
            // case Up:
            // gripper(false);
            // break;
            // default:
            // break;
            // }

            switch (data.getMotion().getMotorState()) {
            case Backward:
                robotMove(-15);
                break;
            case Forward:
                robotMove(-15);
                break;
            case Left:
                robotMove(0, 15);
                break;
            case Right:
                robotMove(15, 0);
                break;
            case Stop:
                robotMove(0);
                break;
            default:
                break;
            }
            robotLED(100);

        }

        @Override
        public void disconnected() {
            super.disconnected();
        }

        @Override
        public void incompatible() {
            super.incompatible();
        }

    }

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

}
