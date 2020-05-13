package acl.siot.opencvwpc20191007noc.objectDetect;

import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;

import acl.siot.opencvwpc20191007noc.util.MLog;

/**
 * Created by IChen.Chu on 2020/5/6
 */
public class NMSUtil {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static NMSUtil mNMSUtil;

    private double conf_thresh = 0.6d;
    private double iou_thresh = 0.5d;
    private double keep_top_k = -1d;


    private NMSUtil() {

    }

    public static NMSUtil getInstance() {
        if (mNMSUtil == null) {
            mNMSUtil = new NMSUtil();
        }
        return mNMSUtil;
    }

    /**
     *     do nms on single class.
     *     Hint: for the specific class, given the bbox and its confidence,
     *     1) sort the bbox according to the confidence from top to down, we call this a set
     *     2) select the bbox with the highest confidence, remove it from set, and do IOU calculate with the rest bbox
     *     3) remove the bbox whose IOU is higher than the iou_thresh from the set,
     *     4) loop step 2 and 3, util the set is empty.
     *     :param bboxes: numpy array of 2D, [num_bboxes, 4]
     *     :param confidences: numpy array of 1D. [num_bboxes]
     *     :param conf_thresh:
     *     :param iou_thresh:
     *     :param keep_top_k:
     *     :return:
     */
    Integer singleClassNonMaxSuppression(DenseMatrix64F y_bboxes, // [4][5972]
                                      DenseMatrix64F bbox_max_scores, // [1][5972]
                                      DenseMatrix64F bbox_max_scores_classes // [1][5972]
    ) {
        double target = 0.0d;
        int target_index = -1;
//        mLog.i(TAG, "singleClassNonMaxSuppression");
        ArrayList<Integer> conf_keep_idx = new ArrayList<>();
//        for (int index = 0 ; index < 5972; index ++) {
//            if (bbox_max_scores.get(0, index) > conf_thresh) {
//                conf_keep_idx.add(index);
//            }
//        }
        for (int index = 0 ; index < 5972; index ++) {
            if (bbox_max_scores.get(0, index) > target && bbox_max_scores.get(0, index) > conf_thresh) {
                target = bbox_max_scores.get(0, index);
                target_index = index;
            }
        }

        mLog.i(TAG, "conf_keep_idx= " + conf_keep_idx);

//        for (int confArray_index = 0; confArray_index < conf_keep_idx.size(); confArray_index++) {
//            mLog.i(TAG, "conf= " + bbox_max_scores.get(0, conf_keep_idx.get(confArray_index)));
//        }
//
//        for (int confArray_index = 0; confArray_index < conf_keep_idx.size(); confArray_index++) {
//            mLog.i(TAG, "classes= " + bbox_max_scores_classes.get(0, conf_keep_idx.get(confArray_index)));
//        }

        mLog.i(TAG, "target_index= " + target_index);
        return target_index;
    }
}
