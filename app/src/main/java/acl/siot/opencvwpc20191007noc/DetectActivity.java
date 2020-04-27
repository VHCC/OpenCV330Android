package acl.siot.opencvwpc20191007noc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.api.getUser.GetUser;
import acl.siot.opencvwpc20191007noc.thc11001huApi.getFace.GetTemp;
import acl.siot.opencvwpc20191007noc.util.MLog;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_EVENT_QRCODE_ID_RESET;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_THC_1101_HU_GET_TEMP;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE_SUCCESS;

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

    // Http Mechanism
    private OnRequestListener mOnRequestListener = new OnRequestListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        super.onCreate(savedInstanceState);

        initWindowSettings();

        // HTTP Mechanism
        OKHttpAgent.getInstance().setRequestListener(mOnRequestListener);

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

                        if (tick_count % 2 == 0) {
                            HashMap<String, String> mMap = new GetTemp();
                            OKHttpAgent.getInstance().getRequest(mMap, APP_CODE_THC_1101_HU_GET_TEMP);
                        }

                        if (tick_count % 1 == 0) {
                            mLog.d(TAG, "getFace, faceCacheArray= " + faceCacheArray.size());
                            getBitmapFlag = true;
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
                Intent i = new Intent(getApplicationContext(), DisplayActivity.class);
                startActivity(i);
//                openCvCameraView.disableView();
//                if (isFrontCamera) {
//                    openCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
//                    isFrontCamera = false;
//                } else {
//                    openCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
//                    isFrontCamera = true;
//                }
//                openCvCameraView.enableView();
                break;
            default:
        }
    }

    // 初始化窗口设置, 包括全屏、横屏、常亮
    @SuppressLint("SourceLockedOrientationActivity")
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

    private int cacheIndex = 0;
    public static ArrayList<Bitmap> faceCacheArray = new ArrayList<>();
    private boolean getBitmapFlag = false;

    // Constants
    static final int FACE_THRESHOLD = 200;
    static final int FACE_THRESHOLD_COUNT = 10;

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
            classifier.detectMultiScale(mGray, faces, 1.1, 3, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            Rect[] facesArray = faces.toArray();
//            mLog.d(TAG, " * facesArray= " + facesArray.length);
            Scalar faceRectColor = new Scalar(0, 255, 0, 255);

            Rect faceRect = null;

            if (facesArray.length > 0) {
                faceRect = facesArray[0];
            }

            for (int index = 0; index < facesArray.length; index ++) {
                if (facesArray[index].height > faceRect.height) {
                    faceRect = facesArray[index];
                }
            }


            if (faceRect != null) {
                if (faceRect.width > FACE_THRESHOLD && faceRect.height > FACE_THRESHOLD &&
                        faceRect.y > 0 && faceRect.x > 0) {
                    // tl :  top-left
                    // br : bottom-right
//                mLog.d(TAG, " * tl= " + faceRect.tl() + ", br= " + faceRect.br());
                    final Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
                    Utils.matToBitmap(mRgba, bitmap);

                    faceImageBitmap = null;
                    faceImageBitmap = Bitmap.createBitmap(bitmap,
                            faceRect.x - 40 < 0 ? 0 : faceRect.x - 40,
                            faceRect.y - 80 < 0 ? 0 : faceRect.y - 80,
//                                    faceRect.width + faceRect.x >= bitmap.getWidth() ? bitmap.getWidth() - faceRect.x :
                            faceRect.width + 40 + (faceRect.x - 40) >= bitmap.getWidth() ? bitmap.getWidth() - faceRect.x : faceRect.width + 40,
                            faceRect.height + 100 + (faceRect.y - 80) >= bitmap.getHeight() ? bitmap.getHeight() - faceRect.y : faceRect.height + 100);

                    if (getBitmapFlag) {
                        mLog.d(TAG, " * cacheIndex= " + cacheIndex);
                        mLog.d(TAG, " * faceImageBitmap.getWidth= " + faceImageBitmap.getWidth() +
                                ", faceImageBitmap.getHeight= " + faceImageBitmap.getHeight());
                        faceCacheArray.add(cacheIndex++, faceImageBitmap);
                        getBitmapFlag = false;
                        if (cacheIndex == 3) {
                            cacheIndex = 0;
                            Intent i = new Intent(getApplicationContext(), DisplayActivity.class);
                            startActivity(i);
                        }
                    }

                    Imgproc.rectangle(mRgba, faceRect.tl(), faceRect.br(), faceRectColor, 3);
//                Imgproc.putText(mRgba, df.format(person_temp), faceRect.tl(), Core.TYPE_MARKER, 10.0, new Scalar(255,0,0), 3);

                }
            }





            fps++;
        }

        return mRgba;
    }

    static Bitmap faceImageBitmap = null;

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

    static double person_temp = 0.0f;
    static DecimalFormat df = new DecimalFormat("0.0");

    /**
     * Http Mechanism Receiver
     */
    private class OnRequestListener implements OKHttpAgent.IRequestInterface {

        @Override
        public void onRequestSuccess(String response, int requestCode) {
            mLog.d(TAG, "onRequestSuccess(), requestCode= " + requestCode);
            switch (requestCode) {
                case APP_CODE_THC_1101_HU_GET_TEMP:
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        person_temp = (float) jsonObj.getDouble("Temperature");
//                        mLog.d(TAG, "person_temp= " + person_temp);
                        mLog.d(TAG, "person_temp= " + df.format(person_temp));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS));
                    break;
            }
        }

        @Override
        public void onRequestFail(String errorResult, int requestCode) {
            mLog.d(TAG, "onRequestFail(), errorResult= " + errorResult);
            switch (requestCode) {
            }
        }

    }

}
