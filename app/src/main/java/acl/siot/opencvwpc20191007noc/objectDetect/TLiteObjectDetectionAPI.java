package acl.siot.opencvwpc20191007noc.objectDetect;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;

import org.ejml.data.DenseMatrix64F;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import acl.siot.opencvwpc20191007noc.util.MLog;

import static acl.siot.opencvwpc20191007noc.vfr.detect.VFRDetect20210303Fragment.anchors;


/**
 * Created by IChen.Chu on 2020/5/6
 */
public class TLiteObjectDetectionAPI implements Classifier {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Only return this many results.
    private static final int NUM_DETECTIONS = 5972;

    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;

    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    private boolean isModelQuantized;
    // Config values.
    private int inputSize;
    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private float[][][] outputLocations;
    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private float[][][] outputClasses;
    //  private float[][] outputClasses;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private float[][] outputScores;
    // numDetections: array of shape [Batchsize]
    // contains the number of detected boxes
    private float[] numDetections;

    // The multi-tensor ready storage
    private HashMap outputProbabilityBuffers = new HashMap<>();

    private ByteBuffer imgData;

    private Interpreter tfLite;

    private TLiteObjectDetectionAPI() {}

    /** Memory-map the model file in Assets. */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize The size of image input
     * @param isQuantized Boolean representing model is quantized or not
     */
    public static Classifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {
        final TLiteObjectDetectionAPI classifier = new TLiteObjectDetectionAPI();

        String actualFilename = labelFilename.split("file:///android_asset/")[1];
//        mLog.i("Classifier", modelFilename);
//        mLog.i("Classifier", actualFilename);
        InputStream labelsInput = assetManager.open(actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
//            mLog.w("Classifier", line);
            classifier.labels.add(line);
        }
        br.close();

        classifier.inputSize = inputSize;

        try {
            classifier.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        classifier.isModelQuantized = isQuantized;
        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (isQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }
        classifier.imgData = ByteBuffer.allocateDirect(1 * classifier.inputSize * classifier.inputSize * 3 * numBytesPerChannel);
        classifier.imgData.order(ByteOrder.nativeOrder());
//    classifier.intValues = new int[classifier.inputSize * classifier.inputSize];
        classifier.intValues = new int[classifier.inputSize * classifier.inputSize];

        classifier.tfLite.setNumThreads(NUM_THREADS);
        classifier.outputLocations = new float[1][NUM_DETECTIONS][4];
        classifier.outputClasses = new float[1][NUM_DETECTIONS][2];
//    classifier.outputClasses = new float[1][NUM_DETECTIONS];
        classifier.outputScores = new float[1][NUM_DETECTIONS];
        classifier.numDetections = new float[1];

        return classifier;
    }

    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        return null;
    }

    @Override
    public ObjectDetectInfo recognizeObject(Bitmap bitmap) {

        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        Trace.endSection(); // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        outputLocations = new float[1][NUM_DETECTIONS][4];
        outputClasses = new float[1][NUM_DETECTIONS][2];
//    outputClasses = new float[1][NUM_DETECTIONS];
//        outputScores = new float[1][NUM_DETECTIONS];
//        numDetections = new float[1];

        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
//    outputMap.put(2, outputScores);
//    outputMap.put(3, numDetections);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
//    tfLite.runForMultipleInputsOutputs(inputArray, this.outputProbabilityBuffers);
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
//    tfLite.run(imgData, outputLocations);
        Trace.endSection();

//        float[][][] classe = (float[][][]) outputMap.get(1);

        DenseMatrix64F y_bboxes = DecodeUtil.getInstance().decodeBBox(anchors, (float[][][]) outputMap.get(0));
        DenseMatrix64F bbox_max_scores = ClassesUtil.getInstance().getMaxScore((float[][][]) outputMap.get(1));
        DenseMatrix64F bbox_max_scores_classes = ClassesUtil.getInstance().getMaxScoreClasses((float[][][]) outputMap.get(1));

        int keep_idxs = NMSUtil.getInstance().singleClassNonMaxSuppression(y_bboxes, bbox_max_scores, bbox_max_scores_classes);
        if (keep_idxs > 0 ) {
            int class_id = (int) bbox_max_scores_classes.get(0, keep_idxs);
            float conf = (float) bbox_max_scores.get(0, keep_idxs);
            float x_min = (float) y_bboxes.get(0, keep_idxs);
            float y_min = (float) y_bboxes.get(1, keep_idxs);
            float x_max = (float) y_bboxes.get(2, keep_idxs);
            float y_max = (float) y_bboxes.get(3, keep_idxs);

            mLog.i(TAG, "getWidth= " + bitmap.getWidth() + ", getHeight= " + bitmap.getHeight());
            mLog.i(TAG, "x_min= " + x_min + ", y_min= " + y_min + ", x_max= " + x_max + ", y_max= " + y_max);
            mLog.i(TAG, "conf= " + conf + ", class_id= " + class_id);
            ObjectDetectInfo result = new ObjectDetectInfo(
                    keep_idxs,
                    class_id,
                    conf,
                    x_min,
                    y_min,
                    x_max,
                    y_max);
            return result;
        } else {
            return null;
        }

    }

    @Override
    public void enableStatLogging(boolean debug) {

    }

    @Override
    public String getStatString() {
        return "";
    }

    @Override
    public void close() {

    }

    @Override
    public void setNumThreads(int num_threads) {
        if (tfLite != null) tfLite.setNumThreads(num_threads);
    }

    @Override
    public void setUseNNAPI(boolean isChecked) {
//        if (tfLite != null) tfLite.setUseNNAPI(isChecked);
    }
}
