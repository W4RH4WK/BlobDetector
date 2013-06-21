package at.bluephoenix.BlobDetector;

import java.text.DecimalFormat;

import at.bluephoenix.BlobDetector.Utils.Blob;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

class IOIOcontrol extends BaseIOIOLooper {
    private TwiMaster twi;
    private NervHub data;
    private PwmOutput servo_;

    private enum Robot {
        Scan, ScanRotate, Rotate, Advance, HelpAdvance, Capture, ScanHQ, RotateHQ, ForwardHQ, End
    }

    private Robot robot = Robot.ScanHQ;

    // for help
    private int helpGrippter = 10;
    private boolean onceFwd = false;
    private boolean onceFwdHq = true;
    private boolean onceRotateHq = true;

    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {
        super.setup();
        twi = ioio_.openTwiMaster(1, TwiMaster.Rate.RATE_100KHz, false);
        servo_ = ioio_.openPwmOutput(10, 50);
        data = NervHub.getInstance();
        servo_.setDutyCycle(0.0001f);
    }

    protected void robotForward(int fwd) {
        byte[] request = new byte[2];
        byte[] response = new byte[1];

        request[0] = 0x1C;
        request[1] = (byte) fwd;
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response, 0);
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
                twi.writeRead(0x69, false, request, request.length, response, 0);
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
                twi.writeRead(0x69, false, request, request.length, response, 0);
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
                twi.writeRead(0x69, false, request, request.length, response, 0);
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

    protected void robotGripper(boolean b) {
        if (b)
            try {
                servo_.setDutyCycle(0.0528f - helpGrippter * 0.0005f);
                if (helpGrippter < 100)
                    helpGrippter += 10;
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            }
        else
            try {
                servo_.setDutyCycle(0.0528f + 0 * 0.0005f);
                helpGrippter = 10;
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            }
    }

    protected String[] robotReadSensor() {
        byte[] request = new byte[] { 0x10 };
        byte[] response = new byte[8];
        String analog[] = new String[12];

        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response,
                        response.length);
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
                twi.writeRead(0x69, false, request, request.length, response, 2);
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
                twi.writeRead(0x69, false, request, request.length, response, 6);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        analog[9] = (short) (((response[1] & 0xFF) << 8) | (response[0] & 0xFF))
                + "";
        analog[10] = (short) (((response[3] & 0xFF) << 8) | (response[2] & 0xFF))
                + "";
        analog[11] = (short) (((response[5] & 0xFF) << 8) | (response[4] & 0xFF))
                + "";
        return analog;
    }

    @Override
    public void loop() throws ConnectionLostException, InterruptedException {

        switch (robot) {
        case Scan:
            Blob b = null;

            try {
                b = data.getBlobs().get(0);
            } catch (IndexOutOfBoundsException e) {
                this.robot = Robot.ScanRotate;
            }

            // check for minimum area
            if (b.getArea() >= 1000) {
                data.setTarget(b);
                this.robot = Robot.Rotate;
            } else {
                this.robot = Robot.ScanRotate;
            }

            break;
        case ScanRotate:
            robotMove(0, 14);
            this.robot = Robot.Scan;
            break;
        case Rotate:
            Double targetAngle = data.getTarget().getAngle();

            if (targetAngle < -8) {
                robotMove(0, 14);
                this.robot = Robot.Scan;
            } else if (targetAngle > 8) {
                robotMove(14, 0);
                this.robot = Robot.Scan;
            }
            this.robot = Robot.Advance;

            break;
        case Advance:
            if (data.getTarget().getDistance() > 22) {
                robotMove(14);
                this.robot = Robot.Scan;
            } else {
                robotMove(0);
                this.robot = Robot.HelpAdvance;
            }

            break;
        case HelpAdvance:
            if (onceFwd) {
                robotForward(14);
                onceFwd = false;
            }

            break;
        case Capture:

            robotLED(100, 0);
            robotGripper(false);
            data.setMode(NervHub.appMode.GoHQ);
            this.robot = Robot.ScanHQ;

            break;
        case ScanHQ:
            if (data.getHqDist() != 0.0) {
                robotMove(14, 0);
                this.robot = Robot.Scan;
            } else
                this.robot = Robot.RotateHQ;

            break;
        case RotateHQ:
            if (onceRotateHq) {
                robotForward(data.getHqDist());
                onceFwdHq = true;
                onceRotateHq = false;
            }

            this.robot = Robot.ForwardHQ;

            break;
        case ForwardHQ:
            if (onceFwdHq) {
                robotRotate(data.getHqAngle());
                onceFwdHq = false;
                onceRotateHq = true;
            }
            this.robot = Robot.End;

            break;
        case End:
            robotMove(0);
            robotLED(0, 100);
            robotGripper(true);
            onceFwd = true;
            this.robot = Robot.End;

            break;
        default:
            break;
        }
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
