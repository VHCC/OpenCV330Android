package acl.siot.opencvwpc20191007noc.objectDetect;

import org.ejml.data.DenseMatrix64F;

import acl.siot.opencvwpc20191007noc.util.MLog;

/**
 * Created by IChen.Chu on 2020/5/5
 */
public class DecodeUtil {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static DecodeUtil mDecodeUtil;

    private DecodeUtil() {

    }

    public static DecodeUtil getInstance() {
        if (mDecodeUtil == null) {
            mDecodeUtil = new DecodeUtil();
        }
        return mDecodeUtil;
    }



    DenseMatrix64F decodeBBox(DenseMatrix64F[] anchor, float[][][] locationObject) {
        DenseMatrix64F predict_bbox = new DenseMatrix64F(4,5972);

        DenseMatrix64F anchor_centers_x = new DenseMatrix64F(1,5972);
        DenseMatrix64F anchor_centers_y = new DenseMatrix64F(1,5972);
        DenseMatrix64F anchors_w  = new DenseMatrix64F(1,5972);
        DenseMatrix64F anchors_h = new DenseMatrix64F(1,5972);

        DenseMatrix64F predict_center_x = new DenseMatrix64F(1,5972);
        DenseMatrix64F predict_center_y = new DenseMatrix64F(1,5972);
        DenseMatrix64F predict_w = new DenseMatrix64F(1,5972);
        DenseMatrix64F predict_h = new DenseMatrix64F(1,5972);

        DenseMatrix64F predict_xmin = new DenseMatrix64F(1,5972);
        DenseMatrix64F predict_ymin = new DenseMatrix64F(1,5972);
        DenseMatrix64F predict_xmax = new DenseMatrix64F(1,5972);
        DenseMatrix64F predict_ymax = new DenseMatrix64F(1,5972);

        float[][][] result_predict = new float[1][5972][4];

        for (int index = 0; index < anchor.length; index ++) {
            DenseMatrix64F vec = anchor[index];

            double m_anchor_centers_x = (vec.get(0) + vec.get(2)) / 2.0d;
//            anchor_centers_x.set(0, index, m_anchor_centers_x);

            double m_anchor_centers_y = (vec.get(1) + vec.get(3)) / 2.0d;
//            anchor_centers_y.set(0, index, m_anchor_centers_y);

            double m_anchors_w = (vec.get(2) - vec.get(0));
//            anchors_w.set(0, index, m_anchors_w);

            double m_anchors_h = (vec.get(3) - vec.get(1));
//            anchors_h.set(0, index, m_anchors_h);



            double m_predict_center_x = locationObject[0][index][0] * 0.1d * m_anchors_w + m_anchor_centers_x;
//            predict_center_x.set(0, index, m_predict_center_x);

            double m_predict_center_y = locationObject[0][index][1] * 0.1d * m_anchors_h + m_anchor_centers_y;
//            predict_center_y.set(0, index, m_predict_center_y);

            double m_predict_w = Math.exp(locationObject[0][index][2] * 0.2d)  * m_anchors_w;
//            predict_w.set(0, index, m_predict_w);

            double m_predict_h= Math.exp(locationObject[0][index][3] * 0.2d) * m_anchors_h;
//            predict_h.set(0, index, m_predict_h);


//            predict_xmin.set(0, index, m_predict_center_x - m_predict_w / 2.0d);
//            predict_ymin.set(0, index, m_predict_center_y - m_predict_h / 2.0d);
//            predict_xmax.set(0, index, m_predict_center_x + m_predict_w / 2.0d);
//            predict_ymax.set(0, index, m_predict_center_y + m_predict_h / 2.0d);


            predict_bbox.set(0, index, m_predict_center_x - m_predict_w / 2.0d);
            predict_bbox.set(1, index, m_predict_center_y - m_predict_h / 2.0d);
            predict_bbox.set(2, index, m_predict_center_x + m_predict_w / 2.0d);
            predict_bbox.set(3, index, m_predict_center_y + m_predict_h / 2.0d);
        }
//        mLog.i(TAG, "anchor_centers_x: " + anchor_centers_x);
//        mLog.i(TAG, "anchor_centers_y: " + anchor_centers_y);
//        mLog.i(TAG, "anchors_w: " + anchors_w);
//        mLog.i(TAG, "anchors_h: " + anchors_h);

//        mLog.i(TAG, "anchor_centers_x: " + anchor_centers_x);
//        mLog.i(TAG, "anchor_centers_y: " + anchor_centers_y);
//        mLog.i(TAG, "anchors_w: " + anchors_w);
//        mLog.i(TAG, "anchors_h: " + anchors_h);

//        mLog.i(TAG, "predict_center_x: " + predict_center_x);
//        mLog.i(TAG, "predict_center_y: " + predict_center_y);
//        mLog.i(TAG, "predict_w: " + predict_w);
//        mLog.i(TAG, "predict_h: " + predict_h);

//        mLog.i(TAG, "predict_bbox: " + predict_bbox);
        return predict_bbox;
//        mLog.i(TAG, "anchor_centers_x: " + anchor_centers_x);
//        mLog.i(TAG, "anchor_centers_y: " + anchor_centers_y);
//        mLog.i(TAG, "anchors_w: " + anchors_w);
//        mLog.i(TAG, "anchors_h: " + anchors_h);
//        mLog.i(TAG, "locationObject: " + locationObject);

//        mLog.i(TAG, "" + predict_xmax);
//        mLog.i(TAG, "" + predict_ymax);
    }
}
