package acl.siot.opencvwpc20191007noc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import acl.siot.opencvwpc20191007noc.util.MLog;

public class DetectActivity extends Activity implements
        CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private CameraBridgeViewBase openCvCameraView;

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


        float mRelativeFaceSize = 0.1f;
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }
        MatOfRect faces = new MatOfRect();
        if (classifier != null) {
            classifier.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            Rect[] facesArray = faces.toArray();
//            mLog.d(TAG, " * facesArray= " + facesArray.length);
            Scalar faceRectColor = new Scalar(0, 255, 0, 255);
            for (Rect faceRect : facesArray) {
                // tl :  top-left
                // br : bottom-right
//                mLog.d(TAG, " * tl= " + faceRect.tl() + ", br= " + faceRect.br());
                Imgproc.rectangle(mRgba, faceRect.tl(), faceRect.br(), faceRectColor, 3);
            }

            fps++;
        }

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
