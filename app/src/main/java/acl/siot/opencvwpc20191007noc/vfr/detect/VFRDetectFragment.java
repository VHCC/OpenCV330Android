package acl.siot.opencvwpc20191007noc.vfr.detect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;
import acl.siot.opencvwpc20191007noc.frsApi.verify.FrsVerify;
import acl.siot.opencvwpc20191007noc.objectDetect.AnchorUtil;
import acl.siot.opencvwpc20191007noc.objectDetect.Classifier;
import acl.siot.opencvwpc20191007noc.objectDetect.ImageUtils;
import acl.siot.opencvwpc20191007noc.objectDetect.ObjectDetectInfo;
import acl.siot.opencvwpc20191007noc.objectDetect.TLiteObjectDetectionAPI;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.view.overLay.OverLayLinearLayout;
import acl.siot.opencvwpc20191007noc.wbSocket.AvaloWebSocketClient;

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
    static final int FACE_THRESHOLD = 200;
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
    private TextView tempStatus;
    private TextView detectStatus;
    private Button cancelDetectBtn;
    private Button adminSettingBtn;
    private OverLayLinearLayout circleOverlay;
    private OverLayLinearLayout circleOverlay_green;
    private TextView recognizedTime;

    private FrameLayout detectBg;
    private FrameLayout detectView;

    private ImageView img1;
    private ImageView faceStatus;
    private ImageView tempStatusBg;
    private ImageView themoStatus;
    private ImageView maskStatus;




    // abnormal
    private FrameLayout abnormal_1;
    private ImageView abnormal_1_viewCache;
    private ImageView abnormal_1_viewCache_thermo;
    private ImageView abnormal_1_maskStatus;
    private ImageView abnormal_1_tempStatusBg;
    private TextView abnormal_1_recognizedTime;
    private TextView abnormal_1_tempStatus;

    // abnormal 2
    private FrameLayout abnormal_2;
    private ImageView abnormal_2_viewCache;
    private ImageView abnormal_2_viewCache_thermo;
    private ImageView abnormal_2_maskStatus;
    private ImageView abnormal_2_tempStatusBg;
    private TextView abnormal_2_recognizedTime;
    private TextView abnormal_2_tempStatus;

    // abnormal 3
    private FrameLayout abnormal_3;
    private ImageView abnormal_3_viewCache;
    private ImageView abnormal_3_viewCache_thermo;
    private ImageView abnormal_3_maskStatus;
    private ImageView abnormal_3_tempStatusBg;
    private TextView abnormal_3_recognizedTime;
    private TextView abnormal_3_tempStatus;

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

    public static boolean staticDetectMaskSwitch = false;
    public static boolean addCacheFlag = false;
    public static boolean addCache_DelayFlag = false;

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
        tempStatus = rootView.findViewById(R.id.tempStatus);
        detectStatus = rootView.findViewById(R.id.detectStatus);

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
        tempStatusBg = rootView.findViewById(R.id.tempStatusBg);
        themoStatus = rootView.findViewById(R.id.thermoStatus);
        maskStatus = rootView.findViewById(R.id.maskStatus);

        thermo_view = rootView.findViewById(R.id.thermo_view);
        recognizedTime = rootView.findViewById(R.id.recognizedTime);




        abnormal_1 = rootView.findViewById(R.id.abnormal_1);
        abnormal_1_viewCache = rootView.findViewById(R.id.abnormal_1_viewCache);
        abnormal_1_viewCache_thermo = rootView.findViewById(R.id.abnormal_1_viewCache_thermo);
        abnormal_1_tempStatus = rootView.findViewById(R.id.abnormal_1_tempStatus);
        abnormal_1_maskStatus = rootView.findViewById(R.id.abnormal_1_maskStatus);
        abnormal_1_tempStatusBg = rootView.findViewById(R.id.abnormal_1_tempStatusBg);
        abnormal_1_recognizedTime = rootView.findViewById(R.id.abnormal_1_recognizedTime);

        abnormal_2 = rootView.findViewById(R.id.abnormal_2);
        abnormal_2_viewCache = rootView.findViewById(R.id.abnormal_2_viewCache);
        abnormal_2_viewCache_thermo = rootView.findViewById(R.id.abnormal_2_viewCache_thermo);
        abnormal_2_tempStatus = rootView.findViewById(R.id.abnormal_2_tempStatus);
        abnormal_2_maskStatus = rootView.findViewById(R.id.abnormal_2_maskStatus);
        abnormal_2_tempStatusBg = rootView.findViewById(R.id.abnormal_2_tempStatusBg);
        abnormal_2_recognizedTime = rootView.findViewById(R.id.abnormal_2_recognizedTime);

        abnormal_3 = rootView.findViewById(R.id.abnormal_3);
        abnormal_3_viewCache = rootView.findViewById(R.id.abnormal_3_viewCache);
        abnormal_3_viewCache_thermo = rootView.findViewById(R.id.abnormal_3_viewCache_thermo);
        abnormal_3_tempStatus = rootView.findViewById(R.id.abnormal_3_tempStatus);
        abnormal_3_maskStatus = rootView.findViewById(R.id.abnormal_3_maskStatus);
        abnormal_3_tempStatusBg = rootView.findViewById(R.id.abnormal_3_tempStatusBg);
        abnormal_3_recognizedTime = rootView.findViewById(R.id.abnormal_3_recognizedTime);
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
//            mLog.d(TAG, " ### isFRServerConnected= " + isFRServerConnected);
            mLog.d(TAG, " ### isThermometerServerConnected= " + isThermometerServerConnected);

//            faceStatus.setImageDrawable(isFRServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));
            themoStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));

//            if (isFRServerConnected || isThermometerServerConnected) {
            if (isThermometerServerConnected) {
//            if (true) {
                detectBg.setBackground(getContext().getResources().getDrawable(R.drawable.vfr_detection_bg));
                detectView.setVisibility(View.VISIBLE);
                staticDetectMaskSwitch = true;
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
            try {
                OKHttpAgent.getInstance().postAvaloWebsocket(true);
//            OKHttpAgent.getInstance().getFRSRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectFRSServer();
//            openCvCameraView.setVisibility(standardMode ? View.VISIBLE : View.GONE);
            thermo_view.setVisibility(!VFREdgeCache.getInstance().isImageStandardMode() ? View.VISIBLE : View.GONE);
            mPlayer = MediaPlayer.create(getActivity().getBaseContext(), R.raw.alarm_20210524);
        }
    }

    private AvaloWebSocketClient c;

    private void connectFRSServer () {
        mLog.d(TAG, " * connect Themo Server");
        if (c != null) {
            c.close();
        }
        c = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        try {
            c = new AvaloWebSocketClient( new URI("ws://" + VFRThermometerCache.getInstance().getIpAddress() + ":9999"));
            c.setListener(new AvaloWebSocketClient.avaloListener() {
                @Override
                public void onMessage(byte[] bytesResult) {
                    Bitmap bitmap = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888);
                    ByteBuffer buffer = ByteBuffer.wrap(bytesResult);

                    bitmap.copyPixelsFromBuffer(buffer);

                    Message msg = new Message();
                    msg.obj = bitmap;
                    mHandler.sendMessage(msg);
                    return;
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        c.connect();
    }

    // field
    private ImageView thermo_view;

    private boolean isTempAbnormal = false;

    MediaPlayer mPlayer;

    private BleHandler mHandler = new BleHandler(getContext());
    class BleHandler extends Handler {

        WeakReference weakReference;

        public BleHandler(Context context) {
            weakReference = new WeakReference(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (weakReference == null) return;

            thermo_view.setImageBitmap((Bitmap) msg.obj);
            if (!addCache_DelayFlag) {
                NumberFormat formatter = new DecimalFormat("#00.0");
//                mLog.d(TAG, "person_temp_static:> " + person_temp_static);
//                tempStatus.setText(person_temp_static > 0.1f ? String.valueOf(formatter.format(person_temp_static)) : "");
//                tempStatusBg.setBackgroundColor(person_temp_static > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));
                Date d = new Date();
                CharSequence s = android.text.format.DateFormat.format("yyyy/MM/dd hh:mm:ss",d.getTime());
                recognizedTime.setText(s);
                if (!thermo_is_human) {
                    detectStatus.setText("Detecting");
                    detectStatus.setTextColor(Color.parseColor("#ffffff"));
                    tempStatus.setText("");
                    tempStatusBg.setBackgroundColor(Color.parseColor("#00000000"));
                    maskStatus.setImageBitmap(maskResults == 1 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok));
                    maskStatus.setVisibility(maskResults == 2 ? View.GONE : View.VISIBLE);
                } else {
                    mLog.d(TAG, "person_temp_static:> " + person_temp_static);
                    if (person_temp_static > 32.0f) {
                        tempStatus.setText(person_temp_static > 0.1f ? String.valueOf(formatter.format(person_temp_static)) : "");
                        tempStatusBg.setBackgroundColor(person_temp_static > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));

//                if (person_temp_static > VFRThermometerCache.getInstance().getAlertTemp() || maskResults == 1) {
                        if (person_temp_static > VFRThermometerCache.getInstance().getAlertTemp()) {
                            isTempAbnormal = true;
                        } else {
                            isTempAbnormal = false;
                        }

                        if (isTempAbnormal || maskResults == 1) {
                            detectStatus.setText("Abnormal");
                            detectStatus.setTextColor(Color.parseColor("#ff4d4d"));
                            maskStatus.setImageBitmap(maskResults == 1 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok));
                            maskStatus.setVisibility(maskResults == 2 ? View.GONE : View.VISIBLE);
//                        if (addCacheFlag && !addCache_DelayFlag) {
                            if (addCacheFlag) {
                                addCacheFlag = false;
                                AbnormalItem abnormalItem = new AbnormalItem();
                                abnormalItem.viewCache = VFREdgeCache.getInstance().isImageStandardMode() ? maskProcessBitmap : (Bitmap) msg.obj;
                                mLog.d(TAG, " *** temperature:> " + person_temp_static);
                                mLog.d(TAG, " *** maskResults:> " + maskResults);
//                        abnormalItem.temperature = String.valueOf(formatter.format(person_temp_static));
                                abnormalItem.temperature = person_temp_static;
                                abnormalItem.timestamp = String.valueOf(s);
//                        abnormalItem.timestamp = String.valueOf(detectResult.getConfidence());
                                abnormalItem.maskType = maskResults;
                                mLog.d(TAG, " *** face Confidence:> " + detectResult.getConfidence());
                                if (detectResult.getConfidence() > 0.94) {
                                    if (VFREdgeCache.getInstance().isImageStandardMode()) {
                                        if (maskProcessBitmap != null) {
                                            abnormalList.add(abnormalItem);
                                            addCache_DelayFlag = true;
                                        }
                                    } else {
                                        abnormalList.add(abnormalItem);
                                        addCache_DelayFlag = true;
                                    }
                                    if(!mPlayer.isPlaying()) {
                                        mPlayer.start();
                                    }
                                }
                            }
                        }

                        if (!isTempAbnormal && maskResults == 0) {
                            detectStatus.setText("Pass");
                            detectStatus.setTextColor(Color.parseColor("#2db100"));
                            maskStatus.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok));
                            maskStatus.setVisibility(View.VISIBLE);
                        }
                    }

                }
                if (abnormalList.size() == 1) {
                    abnormal_1.setVisibility(View.VISIBLE);
                    AbnormalItem abnormalItemTemp_1 = abnormalList.get(abnormalList.size() - 1);
                    if (VFREdgeCache.getInstance().isImageStandardMode()) {
                        abnormal_1_viewCache.setImageBitmap(abnormalItemTemp_1.viewCache);
                        abnormal_1_viewCache.setVisibility(View.VISIBLE);
                        abnormal_1_viewCache_thermo.setVisibility(View.INVISIBLE);
                    } else {
                        abnormal_1_viewCache_thermo.setImageBitmap(abnormalItemTemp_1.viewCache);
                        abnormal_1_viewCache.setVisibility(View.INVISIBLE);
                        abnormal_1_viewCache_thermo.setVisibility(View.VISIBLE);
                    }

//                abnormal_1_tempStatus.setText(abnormalItemTemp_1.temperature);
                    abnormal_1_tempStatus.setText(String.valueOf(formatter.format(abnormalItemTemp_1.temperature)));
                    abnormal_1_maskStatus.setImageBitmap(abnormalItemTemp_1.maskType == 0 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng));
                    abnormal_1_tempStatusBg.setBackgroundColor(abnormalItemTemp_1.temperature > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));
                    abnormal_1_recognizedTime.setText(abnormalItemTemp_1.timestamp);
                }

                if (abnormalList.size() == 2) {
                    abnormal_1.setVisibility(View.VISIBLE);
                    AbnormalItem abnormalItemTemp_1 = abnormalList.get(abnormalList.size() - 1);
                    if (VFREdgeCache.getInstance().isImageStandardMode()) {
                        abnormal_1_viewCache.setImageBitmap(abnormalItemTemp_1.viewCache);
                        abnormal_1_viewCache.setVisibility(View.VISIBLE);
                        abnormal_1_viewCache_thermo.setVisibility(View.INVISIBLE);
                    } else {
                        abnormal_1_viewCache_thermo.setImageBitmap(abnormalItemTemp_1.viewCache);
                        abnormal_1_viewCache.setVisibility(View.INVISIBLE);
                        abnormal_1_viewCache_thermo.setVisibility(View.VISIBLE);
                    }
//                abnormal_1_tempStatus.setText(abnormalItemTemp_1.temperature);
                    abnormal_1_tempStatus.setText(String.valueOf(formatter.format(abnormalItemTemp_1.temperature)));
                    abnormal_1_maskStatus.setImageBitmap(abnormalItemTemp_1.maskType == 0 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng));
                    abnormal_1_tempStatusBg.setBackgroundColor(abnormalItemTemp_1.temperature > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));
                    abnormal_1_recognizedTime.setText(abnormalItemTemp_1.timestamp);

                    abnormal_2.setVisibility(View.VISIBLE);
                    AbnormalItem abnormalItemTemp_2 = abnormalList.get(abnormalList.size()-2);
                    if (VFREdgeCache.getInstance().isImageStandardMode()) {
                        abnormal_2_viewCache.setImageBitmap(abnormalItemTemp_2.viewCache);
                        abnormal_2_viewCache.setVisibility(View.VISIBLE);
                        abnormal_2_viewCache_thermo.setVisibility(View.INVISIBLE);
                    } else {
                        abnormal_2_viewCache_thermo.setImageBitmap(abnormalItemTemp_2.viewCache);
                        abnormal_2_viewCache.setVisibility(View.INVISIBLE);
                        abnormal_2_viewCache_thermo.setVisibility(View.VISIBLE);
                    }
//                abnormal_2_tempStatus.setText(abnormalItemTemp_2.temperature);
                    abnormal_2_tempStatus.setText(String.valueOf(formatter.format(abnormalItemTemp_2.temperature)));
                    abnormal_2_maskStatus.setImageBitmap(abnormalItemTemp_2.maskType == 0 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng));
                    abnormal_2_tempStatusBg.setBackgroundColor(abnormalItemTemp_2.temperature > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));
                    abnormal_2_recognizedTime.setText(abnormalItemTemp_2.timestamp);
                }

                if (abnormalList.size() >= 3) {
                    abnormal_1.setVisibility(View.VISIBLE);
                    AbnormalItem abnormalItemTemp_1 = abnormalList.get(abnormalList.size()-1);
                    if (VFREdgeCache.getInstance().isImageStandardMode()) {
                        abnormal_1_viewCache.setImageBitmap(abnormalItemTemp_1.viewCache);
                        abnormal_1_viewCache.setVisibility(View.VISIBLE);
                        abnormal_1_viewCache_thermo.setVisibility(View.INVISIBLE);
                    } else {
                        abnormal_1_viewCache_thermo.setImageBitmap(abnormalItemTemp_1.viewCache);
                        abnormal_1_viewCache.setVisibility(View.INVISIBLE);
                        abnormal_1_viewCache_thermo.setVisibility(View.VISIBLE);
                    }
//                abnormal_1_tempStatus.setText(abnormalItemTemp_1.temperature);
                    abnormal_1_tempStatus.setText(String.valueOf(formatter.format(abnormalItemTemp_1.temperature)));
                    abnormal_1_maskStatus.setImageBitmap(abnormalItemTemp_1.maskType == 0 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng));
                    abnormal_1_tempStatusBg.setBackgroundColor(abnormalItemTemp_1.temperature > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));
                    abnormal_1_recognizedTime.setText(abnormalItemTemp_1.timestamp);

                    abnormal_2.setVisibility(View.VISIBLE);
                    AbnormalItem abnormalItemTemp_2 = abnormalList.get(abnormalList.size()-2);
                    if (VFREdgeCache.getInstance().isImageStandardMode()) {
                        abnormal_2_viewCache.setImageBitmap(abnormalItemTemp_2.viewCache);
                        abnormal_2_viewCache.setVisibility(View.VISIBLE);
                        abnormal_2_viewCache_thermo.setVisibility(View.INVISIBLE);
                    } else {
                        abnormal_2_viewCache_thermo.setImageBitmap(abnormalItemTemp_2.viewCache);
                        abnormal_2_viewCache.setVisibility(View.INVISIBLE);
                        abnormal_2_viewCache_thermo.setVisibility(View.VISIBLE);
                    }
//                abnormal_2_tempStatus.setText(abnormalItemTemp_2.temperature);
                    abnormal_2_tempStatus.setText(String.valueOf(formatter.format(abnormalItemTemp_2.temperature)));
                    abnormal_2_maskStatus.setImageBitmap(abnormalItemTemp_2.maskType == 0 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng));
                    abnormal_2_tempStatusBg.setBackgroundColor(abnormalItemTemp_2.temperature > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));
                    abnormal_2_recognizedTime.setText(abnormalItemTemp_2.timestamp);

                    abnormal_3.setVisibility(View.VISIBLE);
                    AbnormalItem abnormalItemTemp_3 = abnormalList.get(abnormalList.size()-3);
                    if (VFREdgeCache.getInstance().isImageStandardMode()) {
                        abnormal_3_viewCache.setImageBitmap(abnormalItemTemp_3.viewCache);
                        abnormal_3_viewCache.setVisibility(View.VISIBLE);
                        abnormal_3_viewCache_thermo.setVisibility(View.INVISIBLE);
                    } else {
                        abnormal_3_viewCache_thermo.setImageBitmap(abnormalItemTemp_3.viewCache);
                        abnormal_3_viewCache.setVisibility(View.INVISIBLE);
                        abnormal_3_viewCache_thermo.setVisibility(View.VISIBLE);
                    }
//                abnormal_3_tempStatus.setText(abnormalItemTemp_3.temperature);
                    abnormal_3_tempStatus.setText(String.valueOf(formatter.format(abnormalItemTemp_3.temperature)));
                    abnormal_3_maskStatus.setImageBitmap(abnormalItemTemp_3.maskType == 0 ? BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ok) : BitmapFactory.decodeResource(getResources(), R.drawable.vfr_mask_ng));
                    abnormal_3_tempStatusBg.setBackgroundColor(abnormalItemTemp_3.temperature > VFRThermometerCache.getInstance().getAlertTemp() ? Color.parseColor("#ff4d4d") : Color.parseColor("#0b7e5d"));
                    abnormal_3_recognizedTime.setText(abnormalItemTemp_3.timestamp);
                }
            }


        }
    }


    private ArrayList<AbnormalItem> abnormalList = new ArrayList<>();

    class AbnormalItem {
        String timestamp;
        Bitmap viewCache;
        int maskType;
        float temperature;
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
//        openCvCameraView.bringToFront();
        openCvCameraView.enableView();
        openCvCameraView.enableFpsMeter();
//        openCvCameraView.setCameraDistance(0.8f);
    }

    Display display;
    Point size = new Point();
    int screenWidth;
    int screenHeight;

    int noDetectCount = 0;

    static Bitmap maskProcessBitmap;
    static int maskResults = 1;
    static ObjectDetectInfo detectResult;

    private class OpenCVCameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

        @Override
        public void onCameraViewStarted(int width, int height) {
            mLog.d(TAG, " * onCameraViewStarted");
            vfrFaceCacheArray = new ArrayList<>();
            cacheIndex = 0;
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
                }

                // tl : top-left
                // br : bottom-right
                if (theLargeFace != null) {
//                    mLog.d(TAG, "theLargeFace.width:> " + theLargeFace.width + ", theLargeFace.height:> " + theLargeFace.height + ", FACE_THRESHOLD:> " + FACE_THRESHOLD);
                }
                try {
                    if (theLargeFace != null && theLargeFace.width > FACE_THRESHOLD && theLargeFace.height > FACE_THRESHOLD && theLargeFace.area() > faceArea) {
                        faceArea = theLargeFace.area() * 0.85d;

                        AppBus.getInstance().post(new BusEvent("hide overlay", OVER_LAY_GREEN));
                        noDetectCount = 0;

                        final Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);

                        Utils.matToBitmap(mRgba, bitmap);
                        Bitmap faceImageBitmap = Bitmap.createBitmap(bitmap, theLargeFace.x, theLargeFace.y, theLargeFace.width, theLargeFace.height);
//                    mLog.d(TAG, "*****maskProcessBitmap:> " + maskProcessBitmap);
                        if (faceImageBitmap != null) {
                            maskProcessBitmap = faceImageBitmap;
                        }
//                    if (getBitmapFlag) {
//////                    if (true) {
//////                        mLog.d(TAG, " ***** cacheIndex= " + cacheIndex);
//                        vfrFaceCacheArray.add(0, faceImageBitmap);
//                        cacheIndex++;
//                        getBitmapFlag = false;
//                        if (cacheIndex == processGap) {
//                            mLog.d(TAG, " ***** start processing...");
//                            staticDetectMaskSwitch = true;
//                            cacheIndex = 0;
//
//                            Matrix frameToCropTransform;
//                            Matrix cropToFrameTransform;
//
//                            Bitmap croppedBitmap = Bitmap.createBitmap(260, 260, Bitmap.Config.ARGB_8888);
//
//                            frameToCropTransform =
//                                    ImageUtils.getTransformationMatrix(
//                                            faceImageBitmap.getWidth(), faceImageBitmap.getHeight(),
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
//                                    maskResults = results.getClasses();
//                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
////                                    vfrFaceCacheArray.get(0).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                    faceImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                                    byte[] byteArray = byteArrayOutputStream.toByteArray();
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

//                    Imgproc.rectangle(mRgba, theLargeFace.tl(), theLargeFace.br(), faceRectColor, 3);
//                    org.opencv.core.Point position = new org.opencv.core.Point(theLargeFace.tl().x, theLargeFace.tl().y);
//                    org.opencv.core.Point position2 = new org.opencv.core.Point(theLargeFace.tl().x, theLargeFace.tl().y + 75);
//                    org.opencv.core.Point position3 = new org.opencv.core.Point(theLargeFace.tl().x, theLargeFace.br().y);
//                    Imgproc.putText(mRgba, person_temp_static == 0.0f ? "pls closer" : String.valueOf(person_temp_static), position, 1, 9, new Scalar(255, 0, 0, 0), 12);
//                    Imgproc.putText(mRgba, maskResults == 1 ? "No Mask" : "Mask On", position3, 1, 7, maskResults == 1 ? new Scalar(255, 0, 0, 0) : new Scalar(0, 255, 0, 0), 7);
                    } else {
//                        mLog.d(TAG, "NO OVER FACE_THRESHOLD" + FACE_THRESHOLD + ", * width= " + faceRect.width + ", height= " + faceRect.height);
                        noDetectCount++;
//                        cacheIndex = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (facesArray.length == 0) {
                    noDetectCount++;
                }

                if (noDetectCount > FACE_THRESHOLD_COUNT) {
                    AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_BLUE));
                    cacheIndex = 0;
                    faceArea = 0.0d;
                    maskProcessBitmap = null;
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

    public void onEventMainThread(BusEvent event) {
        mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
        switch (event.getEventType()) {
            case OVER_LAY_GREEN:
                circleOverlay.setVisibility(View.GONE);
                circleOverlay_green.setVisibility(View.VISIBLE);
                promptTv.setText("Please Stay the Position for 3 Seconds");
                break;
            case OVER_LAY_BLUE:
                maskResults = 2;
                if (!addCache_DelayFlag) {
                    maskStatus.setVisibility(View.GONE);
                }
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
                    themoStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));
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
//                mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
//                mLog.i(TAG, " maskProcessBitmap:> " + maskProcessBitmap);
//                mLog.i(TAG, " staticDetectMaskSwitch:> " + staticDetectMaskSwitch);
                maskResults = 2;
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

                    if(!staticDetectMaskSwitch) return;
                    mLog.d(TAG, "try mask detect...");
                    ObjectDetectInfo results = detector.recognizeObject(croppedBitmap);
                    if (results != null) {
                        maskResults = results.getClasses();
                        detectResult = results;
                        addCacheFlag = true;
                        switch (results.getClasses()) {
                            case 1: // NoMask
//                        Imgproc.rectangle(mRgba, new Point(results.getX_min() * width - offset, results.getY_min() * height - offset),
//                                new Point(results.getX_max() * width + offset, results.getY_max() * height + offset), faceRectColor_red, 3);
//                        Imgproc.putText(mRgba, "No Mask " + String.format("{"Attachments":[{"__type":"ItemIdAttachment:#Exchange","ItemId":{"__type":"ItemId:#Exchange","ChangeKey":null,"Id":"AAMkADA3N2YzZWEwLWIzYjMtNDgxOS1iZTgyLWVjNDNiMjY3ZDM1YQBGAAAAAAAcl1sCtwkBSbeQy2pznjzTBwBbCJtwEhi3QbCQIbpG/X6PAAAAAAEMAABbCJtwEhi3QbCQIbpG/X6PAAJAiVVQAAA="},"Name":"大家的尾牙善款_成為雪中的熱炭","IsInline":false},{"__type":"ItemIdAttachment:#Exchange","ItemId":{"__type":"ItemId:#Exchange","ChangeKey":null,"Id":"AAMkADA3N2YzZWEwLWIzYjMtNDgxOS1iZTgyLWVjNDNiMjY3ZDM1YQBGAAAAAAAcl1sCtwkBSbeQy2pznjzTBwBbCJtwEhi3QbCQIbpG/X6PAAAAAAEMAABbCJtwEhi3QbCQIbpG/X6PAAJAiVVPAAA="},"Name":"大家的尾牙善款_成為雪中的熱炭","IsInline":false}]}(%.1f%%) ", results.getConfidence() * 100.0f) , new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(255,0,0), 2);
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
                    } else {
//                        mLog.w(TAG, "maskResults is NULL!!!!!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mLog.d(TAG, "e:>" + e);
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
//                        mLog.d(TAG, "preview frames= " + ((float) preview_frames / period) + ", fps= " + ((float) fps / period));
                        preview_frames = 0;
                        fps = 0;
                    }

                    if (tick_count % 1 == 0 && (noDetectCount == 0)) {
//                        mLog.d(TAG, "getFace, vfrFaceCacheArray= " + vfrFaceCacheArray.size());
                        getBitmapFlag = true;
                    }

                    if (tick_count % 3 == 0) {
//                        mLog.d(TAG, "getFace, vfrFaceCacheArray= " + vfrFaceCacheArray.size());
                        AppBus.getInstance().post(new BusEvent("mask detect", MASK_DETECT));
                    }

                    if (addCache_DelayFlag) {
                        if (tick_count % 10 == 0) {
                            addCache_DelayFlag = false;
                        }
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
