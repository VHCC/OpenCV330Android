package acl.siot.opencvwpc20191007noc.objectDetect;

import org.opencv.core.Point;

import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.NonNull;

/**
 * Created by IChen.Chu on 2020/5/6
 */
public class ObjectDetectInfo {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private int index;
    private int classes;
    private Float confidence;
    private Float x_min;
    private Float y_min;
    private Float x_max;
    private Float y_max;

    public ObjectDetectInfo (int index,
                             int classes,
                             Float confidence,
                             Float x_min,
                             Float y_min,
                             Float x_max,
                             Float y_max) {
        this.index = index;
        this.classes = classes;
        this.confidence = confidence;
        this.x_min = x_min;
        this.y_min = y_min;
        this.x_max = x_max;
        this.y_max = y_max;
    }

    @NonNull
    @Override
    public String toString() {
        String resultString = "";
        resultString += "[" + index + "] ";

        resultString += classes + " ";

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);

        }

//        resultString += String.format("[x_min %.1f%] [y_min %.1f%] [x_max %.1f%] [y_max %.1f%]", x_min, y_min, x_max, y_max);
        return resultString.trim();
    }

    public Float getConfidence() {
        return confidence;
    }

    public int getClasses() {
        return classes;
    }

    public Float getX_min() {
        return x_min;
    }

    public Float getY_min() {
        return y_min;
    }

    public Float getX_max() {
        return x_max;
    }

    public Float getY_max() {
        return y_max;
    }

    public Point getTl() {
        return new Point(x_min, y_min);
    }

    public Point getBr() {
        return new Point(x_max, y_max);
    }
}
