package at.bluephoenix.BlobDetector;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;


class IOIOcontrol extends BaseIOIOLooper {
    private TwiMaster twi;
    private NervHub data;
    private PwmOutput servo_;

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else
            try {
                servo_.setDutyCycle(0.0528f + 0 * 0.0005f);
                helpGrippter = 10;
            } catch (ConnectionLostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    @Override
    public void loop() throws ConnectionLostException, InterruptedException {
        super.loop();

        switch (data.getMotion().getHookState()) {
        case Down:
            robotLED(100, 0);
            robotGripper(false);
            break;
        case Up:
            robotLED(0, 100);
            robotGripper(true);
            onceFwd = true;
            break;
        default:
            break;
        }

        switch (data.getMotion().getMotorState()) {
        case Forward:
            robotMove(14);
            break;
        case HelpForward:
            if (onceFwd) {
                robotForward(14);
                onceFwd = false;
            }
            break;
        case Left:
            robotMove(0, 14);
            break;
        case Right:
            robotMove(14, 0);
            break;
        case Stop:
            robotMove(0);
            break;
        case ForwardHQ:
            if (onceFwdHq) {
                robotRotate(data.getHqAngle());
                onceFwdHq = false;
                onceRotateHq = true;
            }
            break;
        case RotateHQ:
            if (onceRotateHq) {
                robotForward(data.getHqDist());
                onceFwdHq = true;
                onceRotateHq = false;
            }
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
