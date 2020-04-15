package acl.siot.opencvwpc20191007noc.page.detect;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;

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
import java.util.ArrayList;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.view.overLay.OverLayLinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

/**
 * Created by IChen.Chu on 2018/9/25
 * A fragment to show detect page.
 */
public class DetectFragment extends Fragment {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants
    static final int FACE_THRESHOLD = 200;
    static final int FACE_THRESHOLD_COUNT = 10;

    // handler event
    final int OVER_LAY_GREEN = 1001;
    final int OVER_LAY_BLUE = 1002;

    final int FACE_DETECT_DONE = 2001;

    // Flag
    private boolean isFrontCamera = true;

    // View
    private TextView appVersion;
    private TextView promptTv;
    private Button cancelDetectBtn;
    private OverLayLinearLayout circleOverlay;
    private OverLayLinearLayout circleOverlay_green;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;
    private OpenCVCameraListener openCVCameraListener = new OpenCVCameraListener();

    // Fields
    private int preview_frames = 0;
    private int fps = 0;

    // OpenCV Objects
    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier classifier;
    private int mAbsoluteFaceSize = 0;
    private Mat mGray;
    private Mat mRgba;

    // Constructor
    public DetectFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of DetectFragment.
     */
    public static DetectFragment newInstance() {
        DetectFragment fragment = new DetectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        initWindowSettings();
        AppBus.getInstance().register(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_detect, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        promptTv = rootView.findViewById(R.id.promptTv);

        openCvCameraView = rootView.findViewById(R.id.camera_view);
        cancelDetectBtn = rootView.findViewById(R.id.cancelDetectBtn);

        // OverLay
        circleOverlay = rootView.findViewById(R.id.circleOverlay);
        circleOverlay_green = rootView.findViewById(R.id.circleOverlay_green);
    }

    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());

        openCvCameraView.setCvCameraViewListener(openCVCameraListener); // 设置相机监听
        cancelDetectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLog.d(TAG, " * isClickable= " + cancelDetectBtn.isClickable());
                if (cancelDetectBtn.isClickable()) {
//                    backToWelcomePage();
                }
            }
        });

        cancelDetectBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mLog.d(TAG, " * onTouch, event= " + event);
                if ((event.getAction()) == (MotionEvent.ACTION_DOWN)) {
                    backToWelcomePage();
                }
                return false;
            }
        });

        cancelDetectBtn.setClickable(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mLog.d(TAG, "isVisibleToUser= " + isVisibleToUser);
        if (isVisibleToUser) {
            lazyLoad();
        } else {
        }
    }

    // -------------- Lazy Load --------------
    private void lazyLoad() {
        mLog.d(TAG, "lazyLoad(), getUserVisibleHint()= " + getUserVisibleHint());
        if (getUserVisibleHint()) {
            initClassifier();

            threadObject.setRunning(true);

            if (detectPageThread != null && detectPageThread.isAlive()) {
                detectPageThread.interrupt();
            }

            // Task Schedule handle thread
            detectPageThread = new Thread(detectPageRunnable);
            detectPageThread.start();
        }
    }

    @Override
    public void onStart() {
        mLog.d(TAG, " * onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        mLog.d(TAG, " * onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        mLog.d(TAG, " * onPause");
        super.onPause();
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }

        if (detectPageThread != null && detectPageThread.isAlive()) {
            detectPageThread.interrupt();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        AppBus.getInstance().unregister(this);
        super.onDestroy();
    }

    // -------------------------------------------
    public interface OnFragmentInteractionListener {
        void onClickCancelDetect();

        void onDetectThreeFaces();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

    // ***********************************
    // 初始化窗口设置, 包括全屏、横屏、常亮
    private void initWindowSettings() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    // 初始化人脸级联分类器，必须先初始化
    private void initClassifier() {
        mLog.d(TAG, " * initClassifier");
//        openCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        try {
            InputStream is = getResources()
                    .openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getActivity().getDir("cascade", Context.MODE_PRIVATE);
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
            mLog.e(TAG, "Error loading cascade", e);
        }
        openCvCameraView.enableView();
//        openCvCameraView.enableFpsMeter();
    }

    Display display;
    Point size = new Point();
    int screenWidth;
    int screenHeight;

    int noDetectCount = 0;

    private class OpenCVCameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

        @Override
        public void onCameraViewStarted(int width, int height) {
            mLog.d(TAG, " * onCameraViewStarted");
            faceCacheArray = new ArrayList<>();
            cacheIndex = 0;
            display = getActivity().getWindowManager().getDefaultDisplay();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
            mGray = new Mat();
            mRgba = new Mat();
        }

        @Override
        public void onCameraViewStopped() {
            mLog.d(TAG, " * onCameraViewStopped");
            mGray.release();
            mRgba.release();
        }

        private final int bitmapPadding = 110;

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            //        Log.d(TAG, " * onCameraFrame");
            preview_frames++;
            mRgba = inputFrame.rgba();

            mGray = inputFrame.gray();
//        Log.d(TAG, " * isFrontCamera= " + isFrontCamera);
            // 翻转矩阵以适配前后置摄像头
            if (isFrontCamera) {
                Core.flip(mRgba, mRgba, 1);//flip around Y-axis
                Core.flip(mGray, mGray, 1);
            } else {
                Core.flip(mRgba, mRgba, -1);
                Core.flip(mGray, mGray, -1);
            }

//            # rotate 90º counter-clockwise
//            Core.flip(mRgba.t(), mRgba, 1); //mRgba.t() is the transpose

//            Mat dst = new Mat();
//            Mat rotateMat = Imgproc.getRotationMatrix2D(new org.opencv.core.Point(mGray.rows()/2,mGray.cols()/2), 270, 1);
//            Imgproc.warpAffine(mRgba, dst, rotateMat, dst.size());


            float mRelativeFaceSize = 0.1f;
            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
            }

//            Mat mGrayT = mGray.t();
//            Core.flip(mGray.t(), mGrayT, 1);
//            Imgproc.resize(mGrayT, mGrayT, mGray.size());
//
//            Mat mRgbaT = mRgba.t();
//            Core.flip(mRgba.t(), mRgbaT, 1);
//            Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());


            MatOfRect faces = new MatOfRect();
            if (classifier != null) {
                classifier.detectMultiScale(mGray, faces, 1.1, 3, 2,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                Rect[] facesArray = faces.toArray();
//                mLog.d(TAG, " * facesArray= " + facesArray.length);
                Scalar faceRectColor = new Scalar(0, 255, 0, 255);
                Scalar faceRectColor_no_detect = new Scalar(0, 255, 255, 255);

                Rect faceRect = null;

                if (facesArray.length > 0) {
                    faceRect = facesArray[0];
                }

                for (int index = 0; index < facesArray.length; index ++) {
                    if (facesArray[index].height > faceRect.height) {
                        faceRect = facesArray[index];
                    }
                }

//                for (Rect faceRect : facesArray) {
                    if (faceRect != null) {
                        // tl : top-left
                        // br : bottom-right
                        if (faceRect.width > FACE_THRESHOLD && faceRect.height > FACE_THRESHOLD &&
                                faceRect.y > 0 && faceRect.x > 0) {
//                        mLog.d(TAG, " * width= " + faceRect.width + ", height= " + faceRect.height);
//                        mLog.d(TAG, " * faceRect.x= " + faceRect.x + ", faceRect.y= " + faceRect.y);
//                        circleOverlay.setVisibility(View.GONE);
                            AppBus.getInstance().post(new BusEvent("hide overlay", OVER_LAY_GREEN));
                            noDetectCount = 0;
//                        mLog.d(TAG, " * width= " + faceRect.width + ", height= " + faceRect.height);

                            final Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
                            Utils.matToBitmap(mRgba, bitmap);

                            mLog.d(TAG, " * bitmap.getWidth= " + bitmap.getWidth() + ", bitmap.getHeight= " + bitmap.getHeight());
                            mLog.d(TAG, " * faceRect.x= " + faceRect.x + ", faceRect.y= " + faceRect.y + ", width= " + faceRect.width + ", height= " + faceRect.height);
                            Bitmap faceImageBitmap = null;
                            try {
                                faceImageBitmap = Bitmap.createBitmap(bitmap,
                                        faceRect.x - 40 < 0 ? 0 : faceRect.x - 40,
                                        faceRect.y - 80 < 0 ? 0 : faceRect.y - 80,
//                                    faceRect.width + faceRect.x >= bitmap.getWidth() ? bitmap.getWidth() - faceRect.x :
                                        faceRect.width + 40 + (faceRect.x - 40)>= bitmap.getWidth() ? bitmap.getWidth() - faceRect.x : faceRect.width + 40,
                                        faceRect.height + 100 + (faceRect.y - 80)>= bitmap.getHeight() ? bitmap.getHeight() - faceRect.y : faceRect.height + 100);

                                if (getBitmapFlag) {
                                    mLog.d(TAG, " * cacheIndex= " + cacheIndex);
                                    mLog.d(TAG, " * faceImageBitmap.getWidth= " + faceImageBitmap.getWidth() +
                                            ", faceImageBitmap.getHeight= " + faceImageBitmap.getHeight());
                                    faceCacheArray.add(cacheIndex++, faceImageBitmap);
                                    getBitmapFlag = false;
                                    if (cacheIndex == 3) {
                                        cacheIndex = 0;
                                        AppBus.getInstance().post(new BusEvent("face detect done", FACE_DETECT_DONE));
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }






                            Imgproc.rectangle(mRgba, faceRect.tl(), faceRect.br(), faceRectColor, 3);
                        } else {
//                        mLog.d(TAG, " **** not suit, width= " + faceRect.width + ", height= " + faceRect.height);
                            noDetectCount ++;
//                        cacheIndex = 0;
                        }
                    }


//                }

                if (facesArray.length == 0) {
                    noDetectCount ++;
                }

                if (noDetectCount > FACE_THRESHOLD_COUNT) {
                    AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_BLUE));
                    cacheIndex = 0;
//                        Imgproc.rectangle(mRgba,
//                                new org.opencv.core.Point(0,0),
//                                new org.opencv.core.Point(screenWidth, screenHeight),
//                                faceRectColor_no_detect, 5);
                }
                fps++;
            }


            return mRgba;
        }
    }

    private int cacheIndex = 0;

    public static ArrayList<Bitmap> faceCacheArray = new ArrayList<>();

    public void onEventMainThread(BusEvent event){
        switch (event.getEventType()) {
            case OVER_LAY_GREEN:
                circleOverlay.setVisibility(View.GONE);
                circleOverlay_green.setVisibility(View.VISIBLE);
                promptTv.setText("Please Stay the Position for 3 Seconds");
                break;
            case OVER_LAY_BLUE:
                circleOverlay.setVisibility(View.VISIBLE);
                circleOverlay_green.setVisibility(View.GONE);
                promptTv.setText("Please Come Closer to Camera");
                break;
            case FACE_DETECT_DONE:
                if (openCvCameraView != null) {
                    openCvCameraView.disableView();
                }
                if (detectPageThread.isAlive()) {
                    detectPageThread.interrupt();
                    threadObject.setRunning(false);
                }
                onFragmentInteractionListener.onDetectThreeFaces();
                break;
        }
    }

    private void backToWelcomePage() {
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
        if (detectPageThread.isAlive()) {
            detectPageThread.interrupt();
            threadObject.setRunning(false);
        }
        onFragmentInteractionListener.onClickCancelDetect();
    }

    private class ThreadObject extends Object {

        boolean isRunning = true;

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }

    final ThreadObject threadObject = new ThreadObject();


    private boolean getBitmapFlag = false;

    // Thread
    private Thread detectPageThread;
    private Runnable detectPageRunnable = new Runnable() {
        // 1 second
        private static final long task_minimum_tick_time_msec = 1000;

        long tick_count = 0;

        int period = 5;

        @Override
        public void run() {
            while (threadObject.isRunning()) {
                try {
                    long start_time_tick = System.currentTimeMillis();

                    if (tick_count % period == 0) {
                        mLog.d(TAG, "preview frames= " + ((float) preview_frames / period) + ", fps= " + ((float) fps / period));
                        preview_frames = 0;
                        fps = 0;
                    }

                    if (tick_count % 1 == 0 && (noDetectCount == 0)) {
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
    };

}
