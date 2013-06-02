package at.bluephoenix.BlobDetector;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

public class BlobDetectorView extends JavaCameraView {

    public BlobDetectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setWhiteBalance(String whiteBalance) {
        Camera.Parameters params = mCamera.getParameters();
        params.setWhiteBalance(whiteBalance);
        mCamera.setParameters(params);
    }
}
