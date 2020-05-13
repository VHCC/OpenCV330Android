package acl.siot.opencvwpc20191007noc.objectDetect;

import org.ejml.data.DenseMatrix64F;

import acl.siot.opencvwpc20191007noc.util.MLog;

/**
 * Created by IChen.Chu on 2020/5/6
 */
public class ClassesUtil {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static ClassesUtil mClassesUtil;

    private ClassesUtil() {

    }

    public static ClassesUtil getInstance() {
        if (mClassesUtil == null) {
            mClassesUtil = new ClassesUtil();
        }
        return mClassesUtil;
    }

    DenseMatrix64F getMaxScore(float[][][] classesObject) {
        DenseMatrix64F bbox_max_scores = new DenseMatrix64F(1,5972);
        double target = 0.0d;
        for(int index = 0; index < 5972; index ++) {
            if (classesObject[0][index][0] > classesObject[0][index][1]) {
                target = classesObject[0][index][0];
            } else {
                target = classesObject[0][index][1];
            }
            bbox_max_scores.set(0, index, target);
        }
        return bbox_max_scores;
    }

    DenseMatrix64F getMaxScoreClasses(float[][][] classesObject) {
        DenseMatrix64F bbox_max_scores = new DenseMatrix64F(1,5972);
        double target = 0.0d;
        for(int index = 0; index < 5972; index ++) {
            if (classesObject[0][index][0] > classesObject[0][index][1]) {
                target = 0;
            } else {
                target = 1;
            }
            bbox_max_scores.set(0, index, target);
        }
        return bbox_max_scores;
    }

}
