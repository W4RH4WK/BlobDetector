package at.bluephoenix.BlobDetector;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.FileOutputStream;
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

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.text.TextUtils;
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
    private MenuItem mCalibrate;
    private MenuItem mSetHomography;
    private MenuItem mSaveHomography;
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
        mRun = menu.add("Run");
        mCalibrate = menu.add("Calibrate");
        mSetHomography = menu.add("Set homography");
        mSaveHomography = menu.add("Save homography");
        mBeaconMode = menu.add("Beacon mode");
        return true;
    }

    private static String pack(float[] d) {
        StringBuilder sb = new StringBuilder();
        final int length = d.length;
        for (int i = 0; i < length; i++) {
            sb.append(d[i]);
            if (i < (length - 1)) {
                sb.append(':');
            }
        }
        return sb.toString();
    }

    private static float[] unpack(String str) {
        if (TextUtils.isEmpty(str)) {
            return new float[0];
        } else {
            String[] srtData = TextUtils.split(str, ":");
            final int length = srtData.length;
            float[] result = new float[length];
            for (int i = 0; i < length; i++) {
                result[i] = Float.parseFloat(srtData[i]);
            }
            return result;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(BlobDetector.TAG, "called onOptionsItemSelected; selected item: "
                + item);
        if (item == mCalibrate) {
            Log.i(BlobDetector.TAG, "calibrating camera");
            data.setHomography(BlobDetector.calibrateCamera(data.getImage()));

            if (data.getHomography() == null)
                Log.w(BlobDetector.TAG, "calibration not successful");
        } else if (item == mSetHomography) {
            Log.i("homography", "homography loading");
            //
            // FileInputStream inputStream;
            // String str = "";
            // try {
            // inputStream = new FileInputStream("homography");
            // byte[] input = new byte[inputStream.available()];
            // while (inputStream.read(input) != -1) {
            // str += new String(input);
            // }
            // inputStream.close();
            // Log.i("homography", "homography loaded");
            // } catch (FileNotFoundException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            //
            // Log.i(BlobDetector.TAG, "set homography");
            // float homography[] = unpack(str);

            // float homography[] = new float[] { 0.021040694258707436f,
            // 0.013574380850614673f, -9.12896273953139f,
            // 0.018800192214116166f, -0.12060655806359954f,
            // 35.527793344275615f, 9.272118259780662E-4f,
            // -0.0034178208136264937f, 1.0f };

            float homography[] = new float[] { 0.046508994519266864f,
                    -0.0027245021079429187f, -19.106040042251394f,
                    -0.006494253301982257f, -0.03466917822971036f,
                    38.78890952690519f, -1.5722799071954852E-4f,
                    0.0013833986549901976f, 1.0f };

            Mat h = new Mat(3, 3, CvType.CV_32FC1);
            h.put(0, 0, homography);

            Log.i("homoRead",
                    h.get(0, 0)[0] + " " + h.get(0, 1)[0] + " "
                            + h.get(0, 2)[0] + " " + h.get(1, 0)[0] + " "
                            + h.get(1, 1)[0] + " " + h.get(1, 2)[0] + " "
                            + h.get(2, 0)[0] + " " + h.get(2, 1)[0] + " "
                            + h.get(2, 2)[0] + " ");

            data.setHomography(h);
            if (data.getHomography() == null)
                Log.w(BlobDetector.TAG, "calibration not successful");

        } else if (item == mSaveHomography) {
            FileOutputStream outputStream;
            Log.i("homography", "try to save homography");

            try {
                outputStream = openFileOutput("homography",
                        Context.MODE_PRIVATE);
                outputStream.write(("").getBytes());

                Mat h = BlobDetector.calibrateCamera(data.getImage());
                Log.i("homoWrite",
                        h.get(0, 0)[0] + " " + h.get(0, 1)[0] + " "
                                + h.get(0, 2)[0] + " " + h.get(1, 0)[0] + " "
                                + h.get(1, 1)[0] + " " + h.get(1, 2)[0] + " "
                                + h.get(2, 0)[0] + " " + h.get(2, 1)[0] + " "
                                + h.get(2, 2)[0] + " ");

                float homography[] = new float[] { (float) h.get(0, 0)[0],
                        (float) h.get(0, 1)[0], (float) h.get(0, 2)[0],
                        (float) h.get(1, 0)[0], (float) h.get(1, 1)[0],
                        (float) h.get(1, 2)[0], (float) h.get(2, 0)[0],
                        (float) h.get(2, 1)[0], (float) h.get(2, 2)[0] };
                outputStream.write(pack(homography).getBytes());
                outputStream.close();
                Log.i("homography", "homography saved");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (item == mRun) {
            new Thread(new CaptureBall()).start();
        } else if (item == mBeaconMode) {
            if (displayBeacon)
                this.displayBeacon = false;
            else
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

        try {
            Log.i(BlobDetector.TAG, "new targetcolor = "
                    + data.getTargetColor().toString());
        } catch (NullPointerException e) {
            // ignore
        }

        // skip subsequent touch events
        return false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        data.setImage(new Mat(height, width, CvType.CV_8UC4));
        mOpenCvCameraView.setMaxFrameSize(50, 50);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView
                .setWhiteBalance(Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
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

            // Scalar blue = new Scalar(152.3125, 255.0, 98.171875);
            // Scalar green = new Scalar(135.3125, 246.46875, 118.328125, 0.0);
            Scalar green = new Scalar(118.8125, 200.3125, 84.046875);
            Scalar blue = new Scalar(145.890625, 255.0, 238.25);
            Scalar red = new Scalar(251.765625, 175.921875, 245.265625);

            Beacon right = tempBeacon(frame, red, blue);
            Beacon left = tempBeacon(frame, blue, green);
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

//        Scalar calibrationColorTolerance = new Scalar(15, 100, 100);
//
//        List<Blob> Pred = BlobDetector.findBlobs(frame, new Scalar(5, 255, 51),
//                calibrationColorTolerance);
//        List<Blob> Pyellow = BlobDetector.findBlobs(frame, new Scalar(33, 188,
//                210), calibrationColorTolerance);
//        List<Blob> Pgreen = BlobDetector.findBlobs(frame, new Scalar(118, 185,
//                23), calibrationColorTolerance);
//        List<Blob> Pblue = BlobDetector.findBlobs(frame, new Scalar(149, 255,
//                36), calibrationColorTolerance);
//
//        for (Blob b : Pred)
//            b.drawTo(frame);
//
//        for (Blob b : Pyellow)
//            b.drawTo(frame);
//
//        for (Blob b : Pgreen)
//            b.drawTo(frame);
//
//        for (Blob b : Pblue)
//            b.drawTo(frame);

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
