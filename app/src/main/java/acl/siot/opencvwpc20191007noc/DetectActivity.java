package acl.siot.opencvwpc20191007noc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


import org.ejml.data.DenseMatrix64F;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import acl.siot.opencvwpc20191007noc.objectDetect.AnchorUtil;
import acl.siot.opencvwpc20191007noc.objectDetect.Classifier;
import acl.siot.opencvwpc20191007noc.objectDetect.ImageUtils;
import acl.siot.opencvwpc20191007noc.objectDetect.ObjectDetectInfo;
import acl.siot.opencvwpc20191007noc.objectDetect.TLiteObjectDetectionAPI;
import acl.siot.opencvwpc20191007noc.util.MLog;

public class DetectActivity extends Activity implements
        CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private CameraBridgeViewBase openCvCameraView;

    public static DenseMatrix64F[] anchors;

    private Classifier detector;

    private static final int TF_OD_API_INPUT_SIZE = 260;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "face_mask_detection.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/face_mask_detection.txt";

    // 手动装载openCV库文件，以保证手机无需安装OpenCV Manager
    static {
        System.loadLibrary("opencv_java3");
    }

    private CascadeClassifier classifier;
    private Mat mGray;
    private Mat mRgba;
    private int mAbsoluteFaceSize = 0;
    private boolean isFrontCamera = true;
    private int preview_frames = 0;
    private int fps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        super.onCreate(savedInstanceState);

        anchors = AnchorUtil.getInstance().generateAnchors();

        int cropSize = TF_OD_API_INPUT_SIZE;
        try {
            detector =
                    TLiteObjectDetectionAPI.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            mLog.e(TAG, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        initWindowSettings();

        setContentView(R.layout.activity_detect);
        openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        openCvCameraView.setCvCameraViewListener(this); // 设置相机监听

        initClassifier();

        Button switchCamera = (Button) findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this); // 切换相机镜头，默认后置

        new Thread(new Runnable() {
            // 1 second
            private static final long task_minimum_tick_time_msec = 1000;

            long tick_count = 0;

            int period = 5;

            @Override
            public void run() {
                while (true) {
                    try {
                        long start_time_tick = System.currentTimeMillis();

                        if (tick_count % period == 0) {
                            mLog.d(TAG, "preview frames= " + ((float) preview_frames / period) + ", fps= " + ((float) fps / period));
                            preview_frames = 0;
                            fps = 0;
                        }

                        long end_time_tick = System.currentTimeMillis();

                        Thread.sleep(task_minimum_tick_time_msec);
                        tick_count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_camera:
                openCvCameraView.disableView();
                if (isFrontCamera) {
                    openCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                    isFrontCamera = false;
                } else {
                    openCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                    isFrontCamera = true;
                }
                openCvCameraView.enableView();
                break;
            default:
        }
    }

    // 初始化窗口设置, 包括全屏、横屏、常亮
    private void initWindowSettings() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    // 初始化人脸级联分类器，必须先初始化
    private void initClassifier() {
        mLog.d(TAG, " * initClassifier");
        try {
            InputStream is = getResources()
                    .openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading cascade", e);
        }
        openCvCameraView.enableView();
        openCvCameraView.enableFpsMeter();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mLog.d(TAG, " * onCameraViewStarted");
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mLog.d(TAG, " * onCameraViewStopped");
        mGray.release();
        mRgba.release();
    }

    @Override
    // 这里执行人脸检测的逻辑, 根据OpenCV提供的例子实现(face-detection)
    public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        mLog.d(TAG, " * onCameraFrame");
        preview_frames++;

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
//        mLog.d(TAG, " * isFrontCamera= " + isFrontCamera);
        // 翻转矩阵以适配前后置摄像头
        if (isFrontCamera) {
            Core.flip(mRgba, mRgba, 1);//flip around Y-axis
            Core.flip(mGray, mGray, 1);
        } else {
            Core.flip(mRgba, mRgba, -1);
            Core.flip(mGray, mGray, -1);
        }

        Matrix frameToCropTransform;
        Matrix cropToFrameTransform;

//        mLog.d(TAG, " * mRgba.cols()= " + mRgba.cols() + ", mRgba.rows()= " + mRgba.rows());

//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nomask);

        final Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mRgba, bitmap);

        Bitmap croppedBitmap = Bitmap.createBitmap(260, 260, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        mRgba.cols(), mRgba.rows(),
                        260, 260,
                        0, false);

//        frameToCropTransform =
//                ImageUtils.getTransformationMatrix(
//                        1080, 720,
//                        260, 260,
//                        0, false);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(bitmap, frameToCropTransform, null);
//        mLog.d(TAG, " * bitmap.getWidth= " + bitmap.getWidth() + ", bitmap.getHeight= " + bitmap.getHeight());
//        mLog.d(TAG, " * croppedBitmap.getWidth= " + croppedBitmap.getWidth() + ", croppedBitmap.getHeight= " + croppedBitmap.getHeight());

        ObjectDetectInfo results = detector.recognizeObject(croppedBitmap);
        if (results != null) {
            Scalar faceRectColor_green = new Scalar(0, 255, 0);
            Scalar faceRectColor_red = new Scalar(255, 0, 0);

//            float width = 1080;
            float width = mRgba.cols();
//            float height = 720;
            float height = mRgba.rows();
//            mLog.i(TAG, "x_min= " + results.getX_min() + ", y_min= " + results.getY_min() + ", x_max= "
//                    + results.getX_max() + ", y_max= " + results.getY_max() + ", width= " + mRgba.cols() + ", height= " + mRgba.rows()
//            );
//            mLog.i(TAG, "x_min= " + results.getX_min() * width + ", y_min= " + results.getY_min() * height + ", x_max= "
//                    + results.getX_max() * width + ", y_max= " + results.getY_max() * height + ", width= " + width + ", height= " + height
//            );

            int offset = 0;
            switch(results.getClasses()) {
                case 1: // NoMask
                    Imgproc.rectangle(mRgba, new Point(results.getX_min() * width - offset, results.getY_min() * height - offset),
                            new Point(results.getX_max() * width + offset, results.getY_max() * height + offset), faceRectColor_red, 3);
                    Imgproc.putText(mRgba, "No Mask " + String.format("(%.1f%%) ", results.getConfidence() * 100.0f) , new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(255,0,0), 2);

                    break;
                case 0: // Mask
                    Imgproc.rectangle(mRgba, new Point(results.getX_min() * width + offset, results.getY_min() * height + offset),
                            new Point(results.getX_max() * width + offset, results.getY_max() * height + offset), faceRectColor_green, 3);
                    Imgproc.putText(mRgba, "Mask " + String.format("(%.1f%%) ", results.getConfidence() * 100.0f), new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(0,255,0), 2);

                    break;
            }
            mLog.i(TAG, results.toString());
            fps++;
        }


//        float mRelativeFaceSize = 0.1f;
//        if (mAbsoluteFaceSize == 0) {
//            int height = mGray.rows();
//            if (Math.round(height * mRelativeFaceSize) > 0) {
//                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//            }
//        }
//        MatOfRect faces = new MatOfRect();
//        if (classifier != null) {
//            classifier.detectMultiScale(mGray, faces, 1.1, 3, 2,
//                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//            Rect[] facesArray = faces.toArray();
////            mLog.d(TAG, " * facesArray= " + facesArray.length);
//            Scalar faceRectColor = new Scalar(0, 255, 0, 255);
//            for (Rect faceRect : facesArray) {
//                // tl :  top-left
//                // br : bottom-right
////                mLog.d(TAG, " * tl= " + faceRect.tl() + ", br= " + faceRect.br());
//                Imgproc.rectangle(mRgba, faceRect.tl(), faceRect.br(), faceRectColor, 3);
//            }
//
//            fps++;
//        }



        return mRgba;
    }

    @Override
    protected void onResume() {
        mLog.d(TAG, " * onResume");
        super.onResume();
        initClassifier();
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        openCvCameraView.disableView();
    }
}
