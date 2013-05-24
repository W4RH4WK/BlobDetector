package at.bluephoenix.BlobDetector.Utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Beacon {

    private Blob top = null;
    private Blob bot = null;

    private Point coords = null;

    public Point getCoords() {
        return coords;
    }

    public void setCoords(Point coords) {
        this.coords = coords;
    }

    public Beacon(Blob bot, Blob top) {
        this.top = top;
        this.bot = bot;
    }

    public Double getDistance() {
        return bot.getDistance();
    }

    public Double getAngle() {
        return top.getAngle();
    }

    public void drawTo(Mat Rgba) {
        Core.rectangle(Rgba, top.getBox().tl(), bot.getBox().br(), new Scalar(
                0, 0, 255, 255));
        Core.circle(Rgba, bot.getContact(), 5, new Scalar(0, 255, 0, 255));
    }

    @Override
    public String toString() {
        return "[" + coords.x + "/" + coords.y + "]";
    }

}
