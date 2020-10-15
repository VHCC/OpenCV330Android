package acl.siot.opencvwpc20191007noc.objectDetect;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;
import java.util.List;

import acl.siot.opencvwpc20191007noc.util.MLog;

/**
 * Created by IChen.Chu on 2020/5/4
 */
public class AnchorUtil {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static AnchorUtil mAnchorUtil;
    ArrayList<Integer[]> featureMap = new ArrayList<>();
    ArrayList<Float[]> anchorSizeMap = new ArrayList<>();
    ArrayList<Float[]> anchorRatioMap = new ArrayList<>();
    int D = 3;

    private AnchorUtil() {
        featureMap.add(new Integer[]{33, 33});
        featureMap.add(new Integer[]{17, 17});
        featureMap.add(new Integer[]{9, 9});
        featureMap.add(new Integer[]{5, 5});
        featureMap.add(new Integer[]{3, 3});

        anchorSizeMap.add(new Float[]{0.04f, 0.056f});
        anchorSizeMap.add(new Float[]{0.08f, 0.11f});
        anchorSizeMap.add(new Float[]{0.16f, 0.22f});
        anchorSizeMap.add(new Float[]{0.32f, 0.45f});
        anchorSizeMap.add(new Float[]{0.64f, 0.72f});

        anchorRatioMap.add(new Float[]{1f, 0.62f, 0.42f});
        anchorRatioMap.add(new Float[]{1f, 0.62f, 0.42f});
        anchorRatioMap.add(new Float[]{1f, 0.62f, 0.42f});
        anchorRatioMap.add(new Float[]{1f, 0.62f, 0.42f});
        anchorRatioMap.add(new Float[]{1f, 0.62f, 0.42f});
    }

    public static AnchorUtil getInstance() {
        if (mAnchorUtil == null) {
            mAnchorUtil = new AnchorUtil();
        }
        return mAnchorUtil;
    }

    /*
     generate anchors.
    :param feature_map_sizes: list of list, for example: [[40,40], [20,20]]
    :param anDecodeUtilchor_sizes: list of list, for example: [[0.05, 0.075], [0.1, 0.15]]
    :param anchor_ratios: list of list, for example: [[1, 0.5], [1, 0.5]]
    :param offset: default to 0.5
    :return:
     */
    public DenseMatrix64F[] generateAnchors() {

        DenseMatrix64F[] anchor_bboxes = new DenseMatrix64F[23888 / 4];
        int bboxes_index = 0;
        List<Float> result = new ArrayList<Float>();
//        mLog.i(TAG, "featureMap.size()= " + featureMap.size());
        for (int index = 0; index < featureMap.size(); index++) {
            Integer[] feature_array = featureMap.get(index);
//            mLog.i(TAG, "array.length()= " + array.length);
//            mLog.i(TAG, "array[0]= " + array[0]);
//            mLog.i(TAG, "array[1]= " + array[1]);
            DenseMatrix64F cx = new DenseMatrix64F(1, feature_array[0]);//initialized to 0
            cx = linspace(cx);
//            mLog.i(TAG, "cx:");
//            mLog.i(TAG, "" + cx);
            DenseMatrix64F cy = new DenseMatrix64F(1, feature_array[0]);//initialized to 0
            cy = linspace(cy);
//            mLog.i(TAG, "cy:");
//            mLog.i(TAG, "" + cy);

            DenseMatrix64F cx_grid = new DenseMatrix64F(feature_array[0], feature_array[0]);//initialized to 0
            cx_grid = meshiGrid(cx, 0, cx_grid);
//            mLog.i(TAG, "cx_grid:");
//            mLog.i(TAG, "" + cx_grid);

            DenseMatrix64F cy_grid = new DenseMatrix64F(feature_array[0], feature_array[0]);//initialized to 0
            cy_grid = meshiGrid(cy, 1, cy_grid);
//            mLog.i(TAG, "cy_grid:");
//            mLog.i(TAG, "" + cy_grid);

            DenseMatrix64F[] cx_grid_expend = new DenseMatrix64F[feature_array[0]];  //切分矩阵
            CommonOps.rowsToVector(cx_grid, cx_grid_expend);
            DenseMatrix64F[] cy_grid_expend = new DenseMatrix64F[feature_array[0]];  //切分矩阵
            CommonOps.rowsToVector(cy_grid, cy_grid_expend);
            DenseMatrix64F[] center = concatenate(cx_grid_expend, cy_grid_expend);  //获取均值
//            mLog.i(TAG, "center:");
//            for (DenseMatrix64F vec : center) { // check data
//                mLog.i(TAG, "element of center: " + vec);
//            }
            DenseMatrix64F[] center_tiled = tile(center, 8);
//            mLog.i(TAG, "center_tiled:");
//            for (DenseMatrix64F vec : center_tiled) { // check data
//                mLog.i(TAG, "element of center_tiled: " + vec);
//            }

            int num_anchors = anchorSizeMap.size() + anchorRatioMap.size() - 1;
//            mLog.i(TAG, "num_anchors= " + num_anchors);

//            # different scales with the first aspect ratio
            DenseMatrix64F anchor_width_heights = new DenseMatrix64F(feature_array[0], 16);//initialized to 0
            for (int anchor_index = 0; anchor_index < feature_array[0]; anchor_index ++) {
                Float[] anchor_size_array = anchorSizeMap.get(index);
                Float[] anchor_ratio_array = anchorRatioMap.get(index);
                anchor_width_heights.set(anchor_index, 0, -1 * anchor_size_array[0] / 2.0f);
                anchor_width_heights.set(anchor_index, 1, -1 * anchor_size_array[0] / 2.0f);
                anchor_width_heights.set(anchor_index, 2, 1 * anchor_size_array[0] / 2.0f);
                anchor_width_heights.set(anchor_index, 3, 1 * anchor_size_array[0] / 2.0f);
                anchor_width_heights.set(anchor_index, 4, -1 * anchor_size_array[1] / 2.0f);
                anchor_width_heights.set(anchor_index, 5, -1 * anchor_size_array[1] / 2.0f);
                anchor_width_heights.set(anchor_index, 6, 1 * anchor_size_array[1] / 2.0f);
                anchor_width_heights.set(anchor_index, 7, 1 * anchor_size_array[1] / 2.0f);

                float s1 = anchor_size_array[0];
                double width = s1 * Math.sqrt(anchor_ratio_array[1]);
                double height = s1 / Math.sqrt(anchor_ratio_array[1]);
                double width_2 = s1 * Math.sqrt(anchor_ratio_array[2]);
                double height_2 = s1 / Math.sqrt(anchor_ratio_array[2]);
                anchor_width_heights.set(anchor_index, 8, -1 * width / 2.0f);
                anchor_width_heights.set(anchor_index, 9, -1 * height / 2.0f);
                anchor_width_heights.set(anchor_index, 10, 1 * width / 2.0f);
                anchor_width_heights.set(anchor_index, 11, 1 * height / 2.0f);
                anchor_width_heights.set(anchor_index, 12, -1 * width_2 / 2.0f);
                anchor_width_heights.set(anchor_index, 13, -1 * height_2 / 2.0f);
                anchor_width_heights.set(anchor_index, 14, 1 * width_2 / 2.0f);
                anchor_width_heights.set(anchor_index, 15, 1 * height_2 / 2.0f);
            }


            mLog.i(TAG, "" + anchor_width_heights);

//            mLog.i(TAG, "center_tiled add:");
//            for (DenseMatrix64F vec : center_tiled) { // check data
////                mLog.i(TAG, "vec: " + vec);
////                mLog.i(TAG, "anchor_width_heights: " + anchor_width_heights);
//                DenseMatrix64F add_result = new DenseMatrix64F(feature_array[0], 16);  //矩阵对应元素相加
//                CommonOps.add(vec, anchor_width_heights, add_result);
//                mLog.i(TAG, "add_result: " + add_result);
//            }
            DenseMatrix64F[] bbox_coords = new DenseMatrix64F[center_tiled.length];//initialized to 0
            for (int index_add = 0; index_add < center_tiled.length; index_add ++) {
                DenseMatrix64F add_result = new DenseMatrix64F(feature_array[0], 16);  //矩阵对应元素相加
                for (int index_sub_add = 0; index_sub_add < center_tiled.length; index_sub_add ++) {
                    CommonOps.add(center_tiled[index_add], anchor_width_heights, add_result);
                }
                bbox_coords [index_add] = add_result;
            }

//            mLog.i(TAG, "center_tiled add:");
//            for (DenseMatrix64F vec : bbox_coords) { // check data
//                mLog.i(TAG, "add_result: " + vec);
//            }

            DenseMatrix64F[] bbox_coords_reshape = reShape(bbox_coords, feature_array[0]);
//            mLog.i(TAG, "bbox_coords_reshape add:");
//            for (DenseMatrix64F vec : bbox_coords_reshape) { // check data
//                mLog.i(TAG, "bbox_coords_reshape: " + vec);
//                mLog.i(TAG, "bbox_coords_reshape getNumElements: " + vec.getNumElements());
//            }
//            mLog.i(TAG, "bbox_coords_reshape add:");
            for (DenseMatrix64F vec : bbox_coords_reshape) { // check data
//                mLog.i(TAG, "bbox_coords_reshape vec: " + vec);
                anchor_bboxes[bboxes_index++] = vec.copy();
            }
        }

//        mLog.i(TAG, "anchor_bboxes:");
//        for (DenseMatrix64F vec : anchor_bboxes) { // check data
//            mLog.i(TAG, "anchor_bboxes vec: " + vec);
//        }
//        mLog.i(TAG, "anchor_bboxes anchor_bboxes:" + anchor_bboxes.length);
        return anchor_bboxes;
    }

    DenseMatrix64F linspace(DenseMatrix64F input) {
        for (int index = 0; index < input.getNumCols(); index ++) {
            input.set(0, index, (index + 0.5) / input.getNumCols());
        }
//        mLog.i(TAG, "" + input);
        return input;
    }

    DenseMatrix64F meshiGrid(DenseMatrix64F input, int type, DenseMatrix64F output) {
        switch (type) {
            case 0: // x vector
                for (int index_y = 0; index_y < output.getNumRows(); index_y ++) {
                    for (int index_x = 0; index_x < output.getNumCols(); index_x ++) {
                        output.set(index_y, index_x, input.get(index_x));
                    }
                }
                break;
            case 1: // y vector
                for (int index_x = 0; index_x < output.getNumCols(); index_x ++) {
                    for (int index_y = 0; index_y < output.getNumRows(); index_y ++) {
                        output.set(index_x, index_y, input.get(index_x));
                    }
                }
                break;
        }
        return output;
    }

    private DenseMatrix64F[] concatenate(DenseMatrix64F[] cx_expend, DenseMatrix64F[] cy_expend) {
        DenseMatrix64F[] result = new DenseMatrix64F[cx_expend.length];//initialized to 0
        for (int index = 0; index < cx_expend.length; index ++) {
            DenseMatrix64F element = new DenseMatrix64F(cx_expend.length, 2);
            for (int index_expend = 0; index_expend < cx_expend.length; index_expend++) {
                DenseMatrix64F vec_x = cx_expend[index];//获取每一行数据的值
                element.set(index_expend, 0, vec_x.get(index_expend));
                DenseMatrix64F vec_y = cy_expend[index];//获取每一行数据的值
                element.set(index_expend, 1, vec_y.get(index_expend));
            }
            result[index] = element;
        }

        return result;
    }

    private DenseMatrix64F[] tile(DenseMatrix64F[] center, int size) {
        DenseMatrix64F[] result = new DenseMatrix64F[center.length];//initialized to 0
        for (int index = 0; index < center.length; index ++) {
            DenseMatrix64F element = new DenseMatrix64F(center.length, 2 * size);
            for (int index_element = 0; index_element < center.length; index_element++) {
                DenseMatrix64F vec_x = center[index];//获取每一行数据的值
//                mLog.i(TAG, "" + vec_x);
                double element_x = vec_x.get(index_element, 0);
                element.set(index_element, 0, element_x);
                element.set(index_element, 2, element_x);
                element.set(index_element, 4, element_x);
                element.set(index_element, 6, element_x);
                element.set(index_element, 8, element_x);
                element.set(index_element, 10, element_x);
                element.set(index_element, 12, element_x);
                element.set(index_element, 14, element_x);
                DenseMatrix64F vec_y = center[index];//获取每一行数据的值
//                mLog.i(TAG, "" + vec_y);
                double element_y = vec_y.get(index_element, 1);
                element.set(index_element, 1, element_y);
                element.set(index_element, 3, element_y);
                element.set(index_element, 5, element_y);
                element.set(index_element, 7, element_y);
                element.set(index_element, 9, element_y);
                element.set(index_element, 11, element_y);
                element.set(index_element, 13, element_y);
                element.set(index_element, 15, element_y);
            }
            result[index] = element;
        }
        return result;
    }

    DenseMatrix64F[] reShape(DenseMatrix64F[] bbox_coords, int dimension) {
//        mLog.i(TAG, "" + dimension);
        DenseMatrix64F[] output = new DenseMatrix64F[dimension * dimension * 4];
        DenseMatrix64F unit_element = new DenseMatrix64F(1,4);
        int output_index = 0;
        int unit_index = 0;
        for (int index = 0; index < bbox_coords.length; index ++) {
            DenseMatrix64F vec = bbox_coords[index];
//            mLog.i(TAG, "vec.getNumElements()= " + vec.getNumElements());
            for (int element_index = 0; element_index < vec.getNumElements(); element_index++) {
//                mLog.i(TAG, "" + vec.get(element_index));
                unit_element.set(0, unit_index++, vec.get(element_index));
                if (unit_index == 4) {
                    unit_index = 0;
//                    mLog.i(TAG, "output_index= " + output_index);
                    output[output_index++] = unit_element.copy();
                }
            }
        }
        return output;
    }

}
