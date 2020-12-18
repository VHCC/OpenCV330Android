package acl.siot.opencvwpc20191007noc.vfr.detect;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;

import org.ejml.data.DenseMatrix64F;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.frsApi.verify.FrsVerify;
import acl.siot.opencvwpc20191007noc.objectDetect.AnchorUtil;
import acl.siot.opencvwpc20191007noc.objectDetect.Classifier;
import acl.siot.opencvwpc20191007noc.objectDetect.ImageUtils;
import acl.siot.opencvwpc20191007noc.objectDetect.ObjectDetectInfo;
import acl.siot.opencvwpc20191007noc.objectDetect.TLiteObjectDetectionAPI;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.MessageTools;
import acl.siot.opencvwpc20191007noc.view.overLay.OverLayLinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static acl.siot.opencvwpc20191007noc.App.isFRServerConnected;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.vfr.upload.VFRVerifyFragment.staticVerifySwitch;

/**
 * Created by IChen.Chu on 2020/05/13
 * A fragment to show detect page.
 */
public class VFRDetectFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants
    static final int FACE_THRESHOLD = 560;
    static final int FACE_THRESHOLD_COUNT = 9;

    // handler event
    final int OVER_LAY_GREEN = 1001;
    final int OVER_LAY_BLUE = 1002;

    final int MASK_DETECT = 3001;

    final int FACE_DETECT_DONE = 2001;
    final int FACE_DETECT_TEST = 20019;

    // Flag
    private boolean isFrontCamera = true;

    // View
    private TextView appVersion;
    private TextView promptTv;
    private Button cancelDetectBtn;
    private Button adminSettingBtn;
    private OverLayLinearLayout circleOverlay;
    private OverLayLinearLayout circleOverlay_green;

    private FrameLayout detectBg;
    private FrameLayout detectView;

    private ImageView img1;
    private ImageView faceStatus;
    private ImageView thermoStatus;

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
    public VFRDetectFragment() {
    }

    public static DenseMatrix64F[] anchors;

    private Classifier detector;

    private static final int TF_OD_API_INPUT_SIZE = 260;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "face_mask_detection.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/face_mask_detection.txt";

    AudioManager audioManager;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of DetectFragment.
     */
    public static VFRDetectFragment newInstance() {
        VFRDetectFragment fragment = new VFRDetectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        initWindowSettings();
        AppBus.getInstance().register(this);


        anchors = AnchorUtil.getInstance().generateAnchors();

        int cropSize = TF_OD_API_INPUT_SIZE;
        try {
            detector =
                    TLiteObjectDetectionAPI.create(
                            getContext().getAssets(),
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
                            getContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.vfr_fragment_detect, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        promptTv = rootView.findViewById(R.id.promptTv);

        openCvCameraView = rootView.findViewById(R.id.camera_view);
        cancelDetectBtn = rootView.findViewById(R.id.cancelDetectBtn);
        adminSettingBtn = rootView.findViewById(R.id.adminSettingBtn);

        // OverLay
        circleOverlay = rootView.findViewById(R.id.circleOverlay);
        circleOverlay_green = rootView.findViewById(R.id.circleOverlay_green);

        detectBg = rootView.findViewById(R.id.detectBg);
        detectView = rootView.findViewById(R.id.detectView);

        img1 = rootView.findViewById(R.id.img1);
        faceStatus = rootView.findViewById(R.id.faceStatus);
        thermoStatus = rootView.findViewById(R.id.thermoStatus);
    }

    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());

        openCvCameraView.setCvCameraViewListener(openCVCameraListener); // 设置相机监听
        cancelDetectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToWelcomePage();
            }
        });

        adminSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//                // 獲取當前音量
//                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                // 獲取最大音量
//                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                mLog.d(TAG, "currentVolume= " + currentVolume + ", maxVolume= " + maxVolume);
                goToAdminSettingPage();
            }
        });
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
            mLog.d(TAG, " ### isFRServerConnected= " + isFRServerConnected);
            mLog.d(TAG, " ### isThermometerServerConnected= " + isThermometerServerConnected);

            faceStatus.setImageDrawable(isFRServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));
            thermoStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));

            if (isFRServerConnected || isThermometerServerConnected) {
//            if (true) {
                detectBg.setBackground(getContext().getResources().getDrawable(R.drawable.vfr_detection_bg));
                detectView.setVisibility(View.VISIBLE);
                lazyLoad();
            } else {
                detectBg.setBackground(getContext().getResources().getDrawable(R.drawable.vfr_not_connect_bk));
                detectView.setVisibility(View.INVISIBLE);
            }
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
        lazyLoad();
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
            threadObject.setRunning(false);
        }
    }

    @Override
    public void onStop() {
        mLog.d(TAG, " * onStop");
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

        void onClickAdminSetting();

        void onDetectThreeFaces();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

    // ***********************************
    // 初始化窗口设置, 包括全屏、横屏、常亮
    private void initWindowSettings() {
        getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
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
//        openCvCameraView.setMaxFrameSize(1366, 720);
        openCvCameraView.enableView();
        openCvCameraView.enableFpsMeter();
//        openCvCameraView.setCameraDistance(0.8f);
    }

    Display display;
    Point size = new Point();
    int screenWidth;
    int screenHeight;

    int noDetectCount = 0;

    static Bitmap maskProcessBitmap ;
    static int maskResults = 1;

    private class OpenCVCameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

        @Override
        public void onCameraViewStarted(int width, int height) {
            mLog.d(TAG, " * onCameraViewStarted");
            vfrFaceCacheArray = new ArrayList<>();
            cacheIndex= 0;
            display = getActivity().getWindowManager().getDefaultDisplay();
            display.getSize(size);
//            mLog.d(TAG, " * screenWidth= " + screenWidth + ", screenHeight= " + screenHeight);
            screenWidth = size.x;
            screenHeight = size.y;
            mLog.d(TAG, " * screenWidth= " + screenWidth + ", screenHeight= " + screenHeight);
            mGray = new Mat();
            mRgba = new Mat();
        }

        @Override
        public void onCameraViewStopped() {
            mLog.d(TAG, " * onCameraViewStopped");
            mGray.release();
            mRgba.release();
        }

        private int numberOfFace = 1;
        private FaceDetector.Face[] myFace = new FaceDetector.Face[numberOfFace];
        private FaceDetector faceDetector;
        int detectedFaceCount;
        double faceArea = 0.0d;

        int processGap = 8;

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

            float mRelativeFaceSize = 0.1f;
            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
            }
            MatOfRect faces = new MatOfRect();
            if (classifier != null) {
                classifier.detectMultiScale(mGray, faces, 1.1, 2, 0,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                Rect[] facesArray = faces.toArray();
                if (facesArray.length != 0) {
//                    mLog.d(TAG, " * facesArray= " + facesArray.length);
                }
                Scalar faceRectColor = new Scalar(0, 255, 0, 255);
                Scalar faceRectColor_no_detect = new Scalar(0, 255, 255, 255);
                Rect theLargeFace = null;
                for (Rect faceRect : facesArray) {
//                    mLog.d(TAG, "faceRect.area()= " + faceRect.area());

                    if (null != theLargeFace && faceRect.area() > theLargeFace.area()) {
                        theLargeFace = faceRect;
                    }
                    if (null == theLargeFace) {
                        theLargeFace = faceRect;
                    }
//                    // tl : top-left
//                    // br : bottom-right
//                    if (faceRect.width > FACE_THRESHOLD && faceRect.height > FACE_THRESHOLD) {
////                        circleOverlay.setVisibility(View.GONE);
//                        AppBus.getInstance().post(new BusEvent("hide overlay", OVER_LAY_GREEN));
//                        noDetectCount = 0;
//
//                        final Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);
//
//                        Utils.matToBitmap(mRgba, bitmap);
//                        Bitmap faceImageBitmap = Bitmap.createBitmap(bitmap, faceRect.x, faceRect.y, faceRect.width, faceRect.height);
//
//                        if (getBitmapFlag) {
//                            mLog.d(TAG, " ***** cacheIndex= " + cacheIndex);
////                            vfrFaceCacheArray.add(cacheIndex++, faceImageBitmap);
//                            vfrFaceCacheArray.add(0, faceImageBitmap);
//                            cacheIndex++;
//                            getBitmapFlag = false;
//                            if (cacheIndex == 3) {
//
////                                myFaceDetect = new FaceDetector(faceImageBitmap.getWidth() , faceImageBitmap.getHeight() , numberOfFace);
////                                numberOfFaceDetected = myFaceDetect.findFaces(faceImageBitmap, myFace);
////                                mLog.d(TAG, " ***** faceImageBitmap numberOfFaceDetected= " + numberOfFaceDetected);
////                                AppBus.getInstance().post(new BusEvent("face detect done", FACE_DETECT_TEST));
////                                if (numberOfFaceDetected == 0) {
//                                    cacheIndex = 0;
//
//                                    staticVerifySwitch = true;
//                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
////                                    vfrFaceCacheArray.get(0).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                    faceImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                    byte[] byteArray = byteArrayOutputStream .toByteArray();
//
//                                    String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
////                HashMap<String, String> mMap = new UpdateImage("5de8a9b11cce9e1a10b14391", encoded);
//                                    FrsVerify mMap = new FrsVerify(encoded);
//                                    writeToFile(encoded);
////                                    try {
////                                        OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY);
////                                    } catch (IOException e) {
////                                        e.printStackTrace();
////                                    }
//                                    mLog.d(TAG, " * detect done ");
////                                    AppBus.getInstance().post(new BusEvent("face detect done", FACE_DETECT_DONE));
////                                }
//                            } else if (cacheIndex > 3) {
//                                cacheIndex = 0;
//                            }
////                            cacheIndex = 0; // for debug
//                        }
//
//                        Imgproc.rectangle(mRgba, faceRect.tl(), faceRect.br(), faceRectColor, 3);
//                    } else {
////                        mLog.d(TAG, "NO OVER FACE_THRESHOLD" + FACE_THRESHOLD + ", * width= " + faceRect.width + ", height= " + faceRect.height);
//                        noDetectCount ++;
////                        cacheIndex = 0;
//                    }
                }

                // tl : top-left
                // br : bottom-right
                if (theLargeFace != null && theLargeFace.width > FACE_THRESHOLD && theLargeFace.height > FACE_THRESHOLD && theLargeFace.area() > faceArea) {
                    faceArea = theLargeFace.area() * 0.85d;

                    AppBus.getInstance().post(new BusEvent("hide overlay", OVER_LAY_GREEN));
                    noDetectCount = 0;

                    final Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);

                    Utils.matToBitmap(mRgba, bitmap);
                    Bitmap faceImageBitmap = Bitmap.createBitmap(bitmap, theLargeFace.x, theLargeFace.y, theLargeFace.width, theLargeFace.height);
                    maskProcessBitmap = faceImageBitmap;
//                    if (getBitmapFlag) {
////                    if (true) {
////                        mLog.d(TAG, " ***** cacheIndex= " + cacheIndex);
//                        vfrFaceCacheArray.add(0, faceImageBitmap);
//                        cacheIndex++;
//                        getBitmapFlag = false;
//                        if (cacheIndex == processGap) {
//                            mLog.d(TAG, " ***** start processing...");
//                            cacheIndex = 0;
//
//                            Matrix frameToCropTransform;
//                            Matrix cropToFrameTransform;
//
//                            Bitmap croppedBitmap = Bitmap.createBitmap(260, 260, Bitmap.Config.ARGB_8888);
//
//                            frameToCropTransform =
//                                    ImageUtils.getTransformationMatrix(
//                                             faceImageBitmap.getWidth(),faceImageBitmap.getHeight(),
//                                            260, 260,
//                                            0, false);
//
//                            cropToFrameTransform = new Matrix();
//                            frameToCropTransform.invert(cropToFrameTransform);
//
//                            final Canvas canvas = new Canvas(croppedBitmap);
//                            canvas.drawBitmap(faceImageBitmap, frameToCropTransform, null);
//
//                            ObjectDetectInfo results = detector.recognizeObject(croppedBitmap);
//                            if (results != null) {
//                                mLog.d(TAG, " ---- results.getConfidence()= " + results.getConfidence());
//                                if (results != null && results.getConfidence() < 0.94f) {
//                                    AppBus.getInstance().post(new BusEvent("face detect done", FACE_DETECT_TEST));
//                                } else {
//                                    staticVerifySwitch = true;
//                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
////                                    vfrFaceCacheArray.get(0).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                    faceImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                    byte[] byteArray = byteArrayOutputStream .toByteArray();
//
//                                    String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
//                                    FrsVerify mMap = new FrsVerify(encoded);
//                                    writeToFile(encoded);
//                                    try {
//                                        OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    mLog.d(TAG, " * detect done ");
//                                    AppBus.getInstance().post(new BusEvent("face detect done", FACE_DETECT_DONE));
//                                }
//                            } else {
//                                AppBus.getInstance().post(new BusEvent("face detect done", FACE_DETECT_TEST));
//                            }
//                        } else if (cacheIndex > processGap) {
//                            cacheIndex = 0;
//                        }
////                            cacheIndex = 0; // for debug
//                    }

                    Imgproc.rectangle(mRgba, theLargeFace.tl(), theLargeFace.br(), faceRectColor, 3);
//                    Point position = new Point((int) theLargeFace.tl().x, (int) theLargeFace.br().y);
//                    org.opencv.core.Point position = new  org.opencv.core.Point(theLargeFace.tl().x, theLargeFace.br().y);
                    org.opencv.core.Point position = new  org.opencv.core.Point(theLargeFace.tl().x, theLargeFace.tl().y);
                    org.opencv.core.Point position2 = new  org.opencv.core.Point(theLargeFace.tl().x, theLargeFace.tl().y + 75);
                    org.opencv.core.Point position3 = new  org.opencv.core.Point(theLargeFace.tl().x, theLargeFace.br().y);
                    Imgproc.putText(mRgba, person_temp_static == 0.0f ? "pls closer" : String.valueOf(person_temp_static), position, 1, 9, new Scalar(255, 0, 0, 0), 12);
//                    Imgproc.putText(mRgba, String.valueOf(FACE_THRESHOLD), position2, 1, 7, new Scalar(0, 255, 0, 0), 7);
                    Imgproc.putText(mRgba, maskResults == 1 ? "No Mask" : "Mask On", position3, 1, 7, maskResults == 1 ? new Scalar(255, 0, 0, 0) : new Scalar(0, 255, 0, 0), 7);
                } else {
//                        mLog.d(TAG, "NO OVER FACE_THRESHOLD" + FACE_THRESHOLD + ", * width= " + faceRect.width + ", height= " + faceRect.height);
                    noDetectCount ++;
//                        cacheIndex = 0;
                }

                if (facesArray.length == 0) {
                    noDetectCount ++;
                }

                if (noDetectCount > FACE_THRESHOLD_COUNT) {
                    AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_BLUE));
                    cacheIndex = 0;
                    faceArea = 0.0d;
                }
                fps++;
            }
            return mRgba;
        }
    }

    private int cacheIndex = 0;

    public static ArrayList<Bitmap> vfrFaceCacheArray = new ArrayList<>();

    public static float person_temp_static = 0.0f;
    public static Boolean thermo_is_human = false;

    public void onEventMainThread(BusEvent event){
//        mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
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
                stopCameraFunction();
                if (getUserVisibleHint()) {
                    onFragmentInteractionListener.onDetectThreeFaces();
                }
                break;
            case FACE_DETECT_TEST:
//                Toast.makeText(getContext(), "Please modified your face angle to detect again.", Toast.LENGTH_LONG).show();
                break;
            case APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS:
                try {
                    thermoStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));
                    String response = event.getMessage();
                    JSONObject jsonObj = new JSONObject(response);
                    person_temp_static = (float) jsonObj.getDouble("Temperature");
//                    mLog.d(TAG, "person_temp_static= " + person_temp_static);
                    thermo_is_human = (Boolean) jsonObj.getBoolean("ishuman");
//                    mLog.d(TAG, "thermo_is_human= " + thermo_is_human);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MASK_DETECT:
                mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
                try {
                    if (maskProcessBitmap == null) return;

                    Matrix frameToCropTransform;
                    Matrix cropToFrameTransform;

                    Bitmap croppedBitmap = Bitmap.createBitmap(260, 260, Bitmap.Config.ARGB_8888);

                    frameToCropTransform =
                            ImageUtils.getTransformationMatrix(
                                    maskProcessBitmap.getWidth(), maskProcessBitmap.getHeight(),
                                    260, 260,
                                    0, false);

                    cropToFrameTransform = new Matrix();
                    frameToCropTransform.invert(cropToFrameTransform);

                    final Canvas canvas = new Canvas(croppedBitmap);
                    canvas.drawBitmap(maskProcessBitmap, frameToCropTransform, null);

                    ObjectDetectInfo results = detector.recognizeObject(croppedBitmap);
                    if (results != null) {
                        maskResults = results.getClasses();
                        switch(results.getClasses()) {
                            case 1: // NoMask
//                        Imgproc.rectangle(mRgba, new Point(results.getX_min() * width - offset, results.getY_min() * height - offset),
//                                new Point(results.getX_max() * width + offset, results.getY_max() * height + offset), faceRectColor_red, 3);
//                        Imgproc.putText(mRgba, "No Mask " + String.format("(%.1f%%) ", results.getConfidence() * 100.0f) , new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(255,0,0), 2);
//                                MessageTools.showToast(getContext(), "No Mask");
                                break;
                            case 0: // Mask
//                        Imgproc.rectangle(mRgba, new Point(results.getX_min() * width + offset, results.getY_min() * height + offset),
//                                new Point(results.getX_max() * width + offset, results.getY_max() * height + offset), faceRectColor_green, 3);
//                        Imgproc.putText(mRgba, "Mask " + String.format("(%.1f%%) ", results.getConfidence() * 100.0f), new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(0,255,0), 2);
//                                MessageTools.showToast(getContext(), "Mask On");
                                break;
                        }
                        mLog.i(TAG, "mask result= " + results.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void stopCameraFunction() {
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
        if (detectPageThread != null && detectPageThread.isAlive()) {
            detectPageThread.interrupt();
            threadObject.setRunning(false);
        }
    }

    private void backToWelcomePage() {
        stopCameraFunction();
        onFragmentInteractionListener.onClickCancelDetect();
    }

    private void goToAdminSettingPage() {
        stopCameraFunction();
        onFragmentInteractionListener.onClickAdminSetting();
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
        private static final long task_minimum_tick_time_msec = 300;

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
//                        mLog.d(TAG, "getFace, vfrFaceCacheArray= " + vfrFaceCacheArray.size());
                        getBitmapFlag = true;
                    }

                    if (tick_count % 8 == 0 ) {
//                        mLog.d(TAG, "getFace, vfrFaceCacheArray= " + vfrFaceCacheArray.size());
                        AppBus.getInstance().post(new BusEvent("mask detect", MASK_DETECT));
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

    public void writeToFile(String data) {
        // Get the directory for the user's public pictures directory.
        final File path = Environment.getExternalStoragePublicDirectory(
                //Environment.DIRECTORY_PICTURES
                Environment.DIRECTORY_DOWNLOADS);

        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "log_upload.txt");

        // Save your stream, don't forget to flush() it before closing it.

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            mLog.e(TAG, "File write failed: " + e.toString());
        }
    }
}
