package acl.siot.opencvwpc20191007noc.vfr.detect;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.AppUtils;

import net.glxn.qrgen.android.QRCode;

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
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import acl.siot.opencvwpc20191007noc.App;
import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.objectDetect.AnchorUtil;
import acl.siot.opencvwpc20191007noc.objectDetect.Classifier;
import acl.siot.opencvwpc20191007noc.objectDetect.ImageUtils;
import acl.siot.opencvwpc20191007noc.objectDetect.ObjectDetectInfo;
import acl.siot.opencvwpc20191007noc.objectDetect.TLiteObjectDetectionAPI;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.MessageTools;
import acl.siot.opencvwpc20191007noc.view.overLay.OverLayLinearLayout;
import acl.siot.opencvwpc20191007noc.vms.VmsUpload;
import acl.siot.opencvwpc20191007noc.wbSocket.AvaloWebSocketClient;

import static acl.siot.opencvwpc20191007noc.App.TIME_TICK;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.App.isVmsConnected;
import static acl.siot.opencvwpc20191007noc.App.uploadPersonData;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_SERVER_UPLOAD;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_SERVER_UPLOAD_SUCCESS;

/**
 * Created by IChen.Chu on 2021/03/03
 * A fragment to show detect page.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class VFRDetect20210303Fragment extends Fragment {

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

    final int DETECT_ROUND_DONE = 4001;

    final int SHOW_NO_VMS_SERVER_QRCODE = 5009;

    // Flag
    private boolean isFrontCamera = true;

    // View
    private TextView appVersion;
    private TextView promptTv;
    private TextView tempDetected;
    private TextView tempUnit;
    private Button cancelDetectBtn;
    private Button adminSettingBtn;
    private Button adminHomeBtn;
    private Button confirmBtn;
    private Button reCheckBtn;
    private ImageView scan_png;

    // Flags
    private boolean isCaptureFaceDone = false;
    private boolean isQRCodeView = false;
    private boolean startToDetectGate = false;
    private boolean isDetectValidate = false;

    private OverLayLinearLayout circleOverlay;
    private OverLayLinearLayout circleOverlay_green;
    private TextView time_left;
    private TextView time_right;
    private TextView msg_big;
    private TextView msg_small;

    private FrameLayout detectBg;
    private FrameLayout detectView;

    private ImageView faceStatus;
    private ImageView faceCapture;
    private ImageView thermoStatus;
    private ImageView thermoConnectStatus;
    private ImageView maskStatus;

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
    public static boolean canCaptureFaceFlag = false;

    // Constructor
    public VFRDetect20210303Fragment() {
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
    public static VFRDetect20210303Fragment newInstance() {
        VFRDetect20210303Fragment fragment = new VFRDetect20210303Fragment();
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
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.vfr_fragment_detect_20210303, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        promptTv = rootView.findViewById(R.id.promptTv);
        tempDetected = rootView.findViewById(R.id.tempDetected);
        tempUnit = rootView.findViewById(R.id.tempUnit);
//        detectStatus = rootView.findViewById(R.id.detectStatus);

        openCvCameraView = rootView.findViewById(R.id.camera_view);
        cancelDetectBtn = rootView.findViewById(R.id.cancelDetectBtn);
        adminSettingBtn = rootView.findViewById(R.id.adminSettingBtn);
        adminHomeBtn = rootView.findViewById(R.id.adminHomeBtn);
        confirmBtn = rootView.findViewById(R.id.confirmBtn);
        reCheckBtn = rootView.findViewById(R.id.reCheckBtn);
        scan_png = rootView.findViewById(R.id.scan_png);

        // OverLay
        circleOverlay = rootView.findViewById(R.id.circleOverlay);
        circleOverlay_green = rootView.findViewById(R.id.circleOverlay_green);

        detectBg = rootView.findViewById(R.id.detectBg);
        detectView = rootView.findViewById(R.id.detectView);

        faceStatus = rootView.findViewById(R.id.faceStatus);
        thermoConnectStatus = rootView.findViewById(R.id.thermoConnectStatus);
        thermoStatus = rootView.findViewById(R.id.thermoStatus);
        maskStatus = rootView.findViewById(R.id.maskStatus);

        thermo_view = rootView.findViewById(R.id.thermo_view);
        time_left = rootView.findViewById(R.id.time_left);
        time_right = rootView.findViewById(R.id.time_right);
        msg_big = rootView.findViewById(R.id.msg_big);
        msg_small = rootView.findViewById(R.id.msg_small);

        faceCapture = rootView.findViewById(R.id.faceCapture);
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
                goToAdminSettingPage();
            }
        });

        adminHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageTools.showToast(getContext(), "Home Btn Click");
                if (VMSEdgeCache.getInstance().getVms_kiosk_mode() == 1) {
                    backToWelcomePage();
                }
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isQRCodeView) {
                    switch (VMSEdgeCache.getInstance().getVms_kiosk_mode()) {
                        case 0:
                            reCheck();
                            break;
                        case 1:
                            backToWelcomePage();
                            break;
                    }
                } else {
                    reCheck();
                }
            }
        });

        confirmBtn.setVisibility(View.INVISIBLE);
        reCheckBtn.setVisibility(View.INVISIBLE);
        scan_png.setVisibility(View.INVISIBLE);

        reCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reCheck();
            }
        });
    }

    private void uploadData() throws JSONException {
        //TODO send data to VMS
        Bitmap uploadTargetBitmap = VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? maskProcessBitmapClone : thermalBitmap;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        uploadTargetBitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);

        boolean isVisitor = false;

        // Interface including: rfid, barcode, card
        String interfaceMethod = "";

        // mode including: normal, realname, invitation, event
        String dataUploadMode = "Normal";

        switch (VMSEdgeCache.getInstance().getVms_kiosk_mode()) {
            case 0:
                isVisitor = true;
                interfaceMethod = "";
                dataUploadMode = "Normal";
                break;
            case 1:
                isVisitor = false;
                if (App.isRFIDFunctionOn) {
                    interfaceMethod = "rfid";
                } else if (App.isBarCodeFunctionOn) {
                    interfaceMethod = "barcode";
                }
                dataUploadMode = "RealName";
                break;
        }

        boolean isWearMask = false;

        switch (maskResults) {
            case 0: // wear mask
                isWearMask = true;
                break;
            case 1: // no wear mask
                isWearMask = false;
                break;
        }

        // status including: Filling-out, Exception
        // exception including: No-Wear-Mask, High-Fever, "" (empty), Invalid-Barcode,
        String status = "Filling-out".toLowerCase();
        String exception = "";
        if (!isWearMask) {
            exception = "No-Wear-Mask".toLowerCase();
            status = "Exception".toLowerCase();
        }
        if (Float.valueOf(String.valueOf(formatter.format(person_temp_static))) >=
                VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()) {
            exception = "High-Fever".toLowerCase();
            status = "Exception".toLowerCase();
        }

        VmsUpload mMap = new VmsUpload(encoded,
                isWearMask,
                String.valueOf(formatter.format(person_temp_static)),
                uploadPersonData,
                interfaceMethod,
                isVisitor,
                dataUploadMode,
                exception,
                status
        );
        try {
            OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_SERVER_UPLOAD);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reCheck() {
        recheckHandler.removeCallbacks(mFragmentRunnable);
        tick_count = 0;
        isCaptureFaceDone = false;
        startToDetectGate = false;
        isQRCodeView = false;

        threadObject.setRunning(true);

        if (detectPageThread != null && detectPageThread.isAlive()) {
            detectPageThread.interrupt();
        }

        detectPageThread = new Thread(detectPageRunnable);
        detectPageThread.start();

        confirmBtn.setVisibility(View.INVISIBLE);
        reCheckBtn.setVisibility(View.INVISIBLE);
        detectView.setVisibility(View.VISIBLE);
        thermo_view.setVisibility(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 1 ? View.VISIBLE : View.INVISIBLE);
        faceCapture.setVisibility(View.INVISIBLE);
        tempUnit.setVisibility(View.INVISIBLE);

        maskStatus.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));

        scan_png.setVisibility(View.INVISIBLE);

        msg_big.setText("Please wait for few second");
        msg_small.setText("Temperature detection distance 20 cm");

        tempDetected.setText("");
        maskStatus.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
        thermoStatus.setImageBitmap(getBitmap(R.drawable.ic_temp_detect));
        tempUnit.setVisibility(View.INVISIBLE);
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
            mLog.d(TAG, " ### isThermometerServerConnected= " + isThermometerServerConnected);
            thermoConnectStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));
//            if (isFRServerConnected || isThermometerServerConnected) {
//            if (isThermometerServerConnected) {
            if (true) {
                mLog.d(TAG, "VMSEdgeCache.getInstance().getVms_kiosk_video_type():> " + VMSEdgeCache.getInstance().getVms_kiosk_video_type());
                detectView.setVisibility(View.VISIBLE);
                thermo_view.setVisibility(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 1 ? View.VISIBLE : View.INVISIBLE);
                staticDetectMaskSwitch = true;
                lazyLoad();
            } else {
//                detectView.setVisibility(View.INVISIBLE);
//                thermo_view.setVisibility(View.INVISIBLE);
            }
        } else {
        }
    }

    // -------------- Lazy Load --------------
    private void lazyLoad() {
        mLog.d(TAG, "lazyLoad(), getUserVisibleHint()= " + getUserVisibleHint());
        if (getUserVisibleHint()) {
            tick_count = 0;
            isCaptureFaceDone = false;
            startToDetectGate = false;
            isQRCodeView = false;

            faceCapture.setVisibility(View.INVISIBLE);
            tempUnit.setVisibility(View.INVISIBLE);
            reCheckBtn.setVisibility(View.INVISIBLE);
            scan_png.setVisibility(View.INVISIBLE);

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

            initClassifier();

            threadObject.setRunning(true);

            if (detectPageThread != null && detectPageThread.isAlive()) {
                detectPageThread.interrupt();
            }

            // Task Schedule handle thread
            detectPageThread = new Thread(detectPageRunnable);
            detectPageThread.start();

            connectAvaloThermalSocket();
//            openCvCameraView.setVisibility(standardMode ? View.VISIBLE : View.GONE);
//            thermo_view.setVisibility(!VFREdgeCache.getInstance().isImageStandardMode() ? View.VISIBLE : View.GONE);

            tempDetected.setText("");
            maskStatus.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
            thermoStatus.setImageBitmap(getBitmap(R.drawable.ic_temp_detect));
            tempUnit.setVisibility(View.INVISIBLE);

            mPlayer = MediaPlayer.create(getActivity().getBaseContext(), R.raw.alarm_20210524);
        }
    }

    MediaPlayer mPlayer;

    private AvaloWebSocketClient c;
    private final Object mSyncObject = new Object();

    private void connectAvaloThermalSocket() {
        synchronized(mSyncObject) {
            try {
                OKHttpAgent.getInstance().postAvaloWebsocket(true);
//            OKHttpAgent.getInstance().getFRSRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mLog.d(TAG, " * connect to Thermo Socket");
            if (c != null) {
                c.close();
            }
            c = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
            try {
//            c = new AvaloWebSocketClient( new URI("ws://192.168.4.1:9999"));
                c = new AvaloWebSocketClient(new URI("ws://" + VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host() + ":9999"));
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
    }

    // field
    private ImageView thermo_view;
    NumberFormat formatter = new DecimalFormat("#00.0");

//    private boolean isTempAbnormal = false;

    private BleHandler mHandler = new BleHandler(getContext());

    class BleHandler extends Handler {

        WeakReference weakReference;

        public BleHandler(Context context) {
            weakReference = new WeakReference(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (weakReference == null) {
                mLog.d(TAG, "weakReference == null");
                return;
            }
            thermo_view.setImageBitmap((Bitmap) msg.obj);
            thermalBitmap = (Bitmap) msg.obj;

            if (!startToDetectGate) {
                tempDetected.setText("");
                maskStatus.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
                thermoStatus.setImageBitmap(getBitmap(R.drawable.ic_temp_detect));
                tempUnit.setVisibility(View.INVISIBLE);
                return;
            }
            if (isCaptureFaceDone) return;

            if (!isCaptureFaceDone)
                tempDetected.setText(person_temp_static > 0.1f ? String.valueOf(formatter.format(person_temp_static)) : "");
            if (!thermo_is_human && !isCaptureFaceDone) {
                thermoStatus.setImageBitmap(getBitmap(R.drawable.ic_temp_detect));
                maskStatus.setImageBitmap(maskResults == 2 ? getBitmap(R.drawable.ic_mask_detect) : maskResults == 1 ? getBitmap(R.drawable.ic_mask_off) : getBitmap(R.drawable.ic_mask_on));
            } else {
                if (!isCaptureFaceDone)
                    thermoStatus.setImageBitmap(person_temp_static > VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp() ? getBitmap(R.drawable.ic_temp_fail) : getBitmap(R.drawable.ic_temp_ok));

                if (maskResults == 1 && !isCaptureFaceDone) { // NoMask
                    maskStatus.setImageBitmap(getBitmap(R.drawable.ic_mask_off));
                    AppBus.getInstance().post(new BusEvent("show confirm check view", DETECT_ROUND_DONE));
                    isCaptureFaceDone = true;
                    isDetectValidate = false;
                    msg_big.setText("FAIL");
                    reCheckBtn.setVisibility(View.VISIBLE);
                    if (isVmsConnected) {
                        try {
                            uploadData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        startRecheckProcess();
                    }
                    stopTickThread();
                } else if (maskResults == 0 && !isCaptureFaceDone) { // MASK
                    maskStatus.setImageBitmap(getBitmap(R.drawable.ic_mask_on));
//                    AppBus.getInstance().post(new BusEvent("face capture done", FACE_CAPTURE_DONE));
                    isCaptureFaceDone = true;
                    msg_big.setText(person_temp_static < VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp() ? "PASS" : "FAIL");
                    isDetectValidate = (person_temp_static < VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()) ? true : false;
                    mLog.d(TAG, "isDetectValidate:> " + isDetectValidate);
                    if (!isDetectValidate) {
                        AppBus.getInstance().post(new BusEvent("show confirmcheck  view", DETECT_ROUND_DONE));
                    }
                    reCheckBtn.setVisibility(View.VISIBLE);
                    if (isVmsConnected) {
                        try {
                            uploadData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        AppBus.getInstance().post(new BusEvent("", SHOW_NO_VMS_SERVER_QRCODE));
                    }
                    stopTickThread();
                }
            }
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
        stopTickThread();
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
        void onClickConfirmBackToHome();

        void onClickAdminSetting();
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
        openCvCameraView.enableView();
        openCvCameraView.enableFpsMeter();
    }

    Display display;
    Point size = new Point();
    int screenWidth;
    int screenHeight;

    int noDetectCount = 0;

    static Bitmap maskProcessBitmap;
    static Bitmap maskProcessBitmapClone;
    static Bitmap thermalBitmap;
    static int maskResults = 1;
    static ObjectDetectInfo detectResult;

    private class OpenCVCameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

        @Override
        public void onCameraViewStarted(int width, int height) {
            mLog.d(TAG, " * onCameraViewStarted");
            vfrFaceCacheArray = new ArrayList<>();
            display = getActivity().getWindowManager().getDefaultDisplay();
            display.getSize(size);
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
        double faceArea = 0.0d;

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
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
//                            mLog.d(TAG, "maskProcessBitmap OK");
                            maskProcessBitmap = faceImageBitmap;
                            maskProcessBitmapClone = faceImageBitmap;
                        }
                    } else {
//                        mLog.d(TAG, "NO OVER FACE_THRESHOLD" + FACE_THRESHOLD + ", * width= " + faceRect.width + ", height= " + faceRect.height);
                        noDetectCount++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (facesArray.length == 0) {
                    noDetectCount++;
                }

                if (noDetectCount > FACE_THRESHOLD_COUNT) {
                    AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_BLUE));
                    faceArea = 0.0d;
                    maskProcessBitmap = null;
                }
                fps++;
            }
            return mRgba;
        }
    }

    public static ArrayList<Bitmap> vfrFaceCacheArray = new ArrayList<>();

    public static float person_temp_static = 0.0f;
    public static Boolean thermo_is_human = false;

    public void onEventMainThread(BusEvent event) {
//        mLog.d(TAG, "event:> " + event.getEventType());
        if (null != c) {
//            mLog.d(TAG, "c isOpen:> " + c.isOpen() + ", c isClosed:> " + c.isClosed());
        }
        switch (event.getEventType()) {
            case TIME_TICK:
                Date d = new Date();
                CharSequence s_1 = android.text.format.DateFormat.format("yyyy-MM-dd", d.getTime());
                CharSequence s_2 = android.text.format.DateFormat.format("hh:mm", d.getTime());
                time_left.setText(s_1);
                time_right.setText(s_2);
                break;
            case OVER_LAY_GREEN:
                circleOverlay.setVisibility(View.GONE);
                circleOverlay_green.setVisibility(View.VISIBLE);
                promptTv.setText("Please Stay the Position for 3 Seconds");
                if (isQRCodeView) return;
                if (!isCaptureFaceDone) msg_big.setText("Please wait for few second");
                break;
            case OVER_LAY_BLUE:
                maskResults = 2;
                circleOverlay.setVisibility(View.VISIBLE);
                circleOverlay_green.setVisibility(View.GONE);
                promptTv.setText("Please Come Closer to Camera");
                if (isQRCodeView) return;
                if (!isCaptureFaceDone)
                    msg_big.setText("Please get closer to the screen for testing");
                break;
            case FACE_DETECT_DONE:
                stopCameraFunction();
                break;
            case DETECT_ROUND_DONE:
                detectView.setVisibility(View.INVISIBLE);
                faceCapture.setVisibility(View.VISIBLE);
                faceCapture.setImageBitmap(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? maskProcessBitmapClone : thermalBitmap);
                confirmBtn.setVisibility(View.INVISIBLE);
                reCheckBtn.setVisibility(View.VISIBLE);
                tempUnit.setVisibility(View.VISIBLE);
                msg_small.setText("Please click confirm button to save the data");
                break;
            case APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS:
                try {
                    String response = event.getMessage();
                    JSONObject jsonObj = new JSONObject(response);
                    person_temp_static = (float) jsonObj.getDouble("Temperature");
                    thermo_is_human = (Boolean) jsonObj.getBoolean("ishuman");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MASK_DETECT:
                // *** CORE MASK DETECT PROCESS ***
                maskResults = 2;
                try {
                    if (maskProcessBitmap == null) return;
                    Matrix frameToCropTransform;
                    Matrix cropToFrameTransform;

                    Bitmap croppedBitmap = Bitmap.createBitmap(260, 260, Bitmap.Config.ARGB_8888);

                    frameToCropTransform =
                            ImageUtils.getTransformationMatrix(
                                    maskProcessBitmapClone.getWidth(), maskProcessBitmapClone.getHeight(),
                                    260, 260,
                                    0, false);

                    cropToFrameTransform = new Matrix();
                    frameToCropTransform.invert(cropToFrameTransform);

                    final Canvas canvas = new Canvas(croppedBitmap);
                    canvas.drawBitmap(maskProcessBitmapClone, frameToCropTransform, null);
                    if (!staticDetectMaskSwitch) return;
//                    mLog.d(TAG, "try mask detect...");
                    ObjectDetectInfo results = detector.recognizeObject(croppedBitmap);
                    if (results != null && results.getConfidence() > 0.94) {
                        maskResults = results.getClasses();
                        detectResult = results;
                        canCaptureFaceFlag = true;
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
                        mLog.i(TAG, "mask result= " + results.toString() + ", confidence:> " + results.getConfidence());
                    } else {
                        mLog.w(TAG, "maskResults is NULL!!!!!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mLog.e(TAG, "MASK_DETECT, e:> " + e);
                }
                break;
            case APP_CODE_VMS_SERVER_UPLOAD_SUCCESS:
                mLog.d(TAG, "isDetectValidate:> " + isDetectValidate);
                if (!isDetectValidate) {
                    // FAIL Process
                    startRecheckProcess();
                    return;
                }
                isQRCodeView = true;
                try {
                    JSONObject kioskUploadResponse = new JSONObject(event.getMessage());
                    String url = kioskUploadResponse.getString("url");
                    mLog.d(TAG, "url:> " + url);
                    scan_png.setVisibility(View.VISIBLE);
                    scan_png.setImageDrawable(getResources().getDrawable(R.drawable.png_scan));
                    confirmBtn.setVisibility(View.VISIBLE);
                    reCheckBtn.setVisibility(View.INVISIBLE);

                    msg_big.setText("Please scan the QR code to reach the questionnaire");
                    msg_small.setText("(The system will return to the home page after " + VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout() + " seconds)");

                    Bitmap urlBitmap = QRCode.from(url).bitmap();
                    faceCapture.setImageBitmap(urlBitmap);
                    detectView.setVisibility(View.INVISIBLE);
//                    thermo_view.setVisibility(View.INVISIBLE);
                    faceCapture.setVisibility(View.VISIBLE);
                    tempUnit.setVisibility(View.VISIBLE);
                    startRecheckProcess();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case SHOW_NO_VMS_SERVER_QRCODE: // Deprecated
                startRecheckProcess();
                detectView.setVisibility(View.INVISIBLE);
//                thermo_view.setVisibility(View.INVISIBLE);
                faceCapture.setVisibility(View.VISIBLE);

                Date today = new Date();
                Date today_end = new Date(today.getYear(), today.getMonth(), today.getDate(), 23, 59, 59);
//                String startDate = toRFC3339(today);
                String startDate = (String) android.text.format.DateFormat.format("yyyy-MM-dd'T'HH:mm:ssZ", today.getTime());
                String endDate = (String) android.text.format.DateFormat.format("yyyy-MM-dd'T'HH:mm:ssZ", today_end.getTime());

//                String qucodeString = "Advantech-VMS-Code,v1.0.0,0,9,2021-03-23T11:43:57Z,2021-03-23T17:59:59Z,00000000,fTd46eW";
                String qucodeString = "Advantech-VMS-Code,v1.0.0,0,9," + startDate +
//                        "," + endDate + "," + detectSerialNumber + ",fTd46eW";
                        "," + endDate + "," + "00000000" + ",fTd46eW";
                Bitmap myBitmap = QRCode.from(qucodeString).bitmap();
                faceCapture.setImageBitmap(myBitmap);

                confirmBtn.setVisibility(View.VISIBLE);
                reCheckBtn.setVisibility(View.INVISIBLE);
                tempUnit.setVisibility(View.VISIBLE);

                scan_png.setImageDrawable(getResources().getDrawable(R.drawable.icon_pic));
                scan_png.setVisibility(View.VISIBLE);

//                String startDate_display = toDisplay(today);
                String startDate_display = (String) android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", today.getTime());
                String endDate_display = (String) android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", today_end.getTime());

                msg_big.setText("Allow access " + startDate_display + " ~ " + endDate_display);
                msg_small.setText("");
                break;
        }
        if (null != thermoConnectStatus)
            thermoConnectStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));

        if (!isThermometerServerConnected) {
            if (null != c && !c.isOpen()) {
                connectAvaloThermalSocket();
            }
            if (null != c) {
                c.close();
            }
        }
        if (!isThermometerServerConnected) {
            person_temp_static = 0.0f;
            thermo_is_human = false;
        }
    }

    private void startRecheckProcess() {
        mLog.d(TAG, "timeout" + VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout());
        recheckHandler.removeCallbacks(mFragmentRunnable);
        recheckHandler.postDelayed(mFragmentRunnable, VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout() * 1000);
    }

    private void stopCameraFunction() {
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
        stopTickThread();
    }

    private void stopTickThread() {
        if (detectPageThread != null && detectPageThread.isAlive()) {
            detectPageThread.interrupt();
            threadObject.setRunning(false);
        }
    }

    private void backToWelcomePage() {
        stopCameraFunction();
        onFragmentInteractionListener.onClickConfirmBackToHome();
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

    long tick_count = 0;

    // Thread
    private Thread detectPageThread;
    private Runnable detectPageRunnable = new Runnable() {
        // 1 second
        private static final long task_minimum_tick_time_msec = 300; // one tick 0.3s
        int period = 5;

        @Override
        public void run() {
            while (threadObject.isRunning()) {
//                mLog.d(TAG, "tick_count:> " + tick_count);
                try {
                    long start_time_tick = System.currentTimeMillis();
                    if (tick_count % period == 0) {
//                        mLog.d(TAG, "preview frames= " + ((float) preview_frames / period) + ", fps= " + ((float) fps / period));
                        preview_frames = 0;
                        fps = 0;
                    }

                    if (tick_count % 10 == 9) {
                        if (!isCaptureFaceDone) {
                            startToDetectGate = true;
                        }
                    }

                    if (tick_count % 3 == 0) {
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

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // Handler
    private Handler recheckHandler = new MainHandler();
    private Runnable mFragmentRunnable = new FragmentRunnable();

    // -------------------------------------------
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private class FragmentRunnable implements Runnable {
        @Override
        public void run() {
            switch (VMSEdgeCache.getInstance().getVms_kiosk_mode()) {
                case 0:
                    reCheck();
                    break;
                case 1:
                    backToWelcomePage();
                    break;
            }
        }
    }

    SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.TAIWAN);
    SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.TAIWAN);

    String toRFC3339(Date d) {
        return rfc3339.format(d).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

    String toDisplay(Date d) {
        return displayFormat.format(d).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

}
