package acl.siot.opencvwpc20191007noc.vfr.detect;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import acl.siot.opencvwpc20191007noc.camera.CameraCallbacks;
import acl.siot.opencvwpc20191007noc.camera.CameraPreview2;
import acl.siot.opencvwpc20191007noc.util.LogWriter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.seeta.*;

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
import org.opencv.imgproc.Imgproc;
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
import acl.siot.opencvwpc20191007noc.vms.CheckVmsPersonSerial;
import acl.siot.opencvwpc20191007noc.vms.VmsUpload;
import acl.siot.opencvwpc20191007noc.vms.VmsUpload_TPE;
import acl.siot.opencvwpc20191007noc.wbSocket.AvaloWebSocketClient;

import static acl.siot.opencvwpc20191007noc.App.TIME_TICK;
import static acl.siot.opencvwpc20191007noc.App.isCameraUnavailable;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.App.isVmsConnected;
import static acl.siot.opencvwpc20191007noc.App.uploadPersonData;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_AVALO_THERMAL_POST_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_STATUS_INACTIVE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_SERVER_UPLOAD;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_SERVER_UPLOAD_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_SERVER_UPLOAD_TPE;
import static acl.siot.opencvwpc20191007noc.vfr.adminSetting.VFRAdminPassword20210429Fragment.isDebugRecordMode;

/**
 * Created by IChen.Chu on 2021/03/03
 * A fragment to show detect page.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class VFRDetect20210303Fragment extends Fragment implements VerificationContract.View {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants
    static final int FACE_THRESHOLD = 100;
    static final int FACE_THRESHOLD_COUNT = 9;

    // handler event
    final int OVER_LAY_GREEN = 1001;
    final int OVER_LAY_BLUE = 1002;

    final int MASK_DETECT = 3001;

    final int FACE_DETECT_DONE = 2001;

    final int DETECT_DONE_SHOW_SNAPSHOT = 4001;

    final int SHOW_NO_VMS_SERVER_QRCODE = 5009;

    // Device Setting
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

    // Process Flags
    private boolean isFullDetectProcessDone = false; // 完整偵測流程結束與否
    private boolean isQRCodeViewShow = false; // QRCode 顯示畫面與否
    private boolean canStartToDetectGate = false; // 控制 每次偵測與每次偵測之間間隔
    private boolean isDetectValidate = false; // 結果合格與否

    //    public static boolean staticDetectMaskSwitch = false; // 控制 每次偵測與每次偵測之間間隔
    public static boolean canShowTempFlag = false; // 顯示溫度與否。規格為：確認完成臉部後，才顯示溫度

    // View
    private OverLayLinearLayout circleOverlay;
    private OverLayLinearLayout circleOverlay_green;
    private TextView time_left;
    private TextView time_right;
    private TextView msg_big;
    private TextView msg_small;
    private FrameLayout detect_bg;

    // Core View
    private FrameLayout detectFrame;

    private ImageView faceStatus;
    private ImageView faceCapture; // 主畫面抓臉結果
    private ImageView thermalDetect_bg; // 溫度結果 背景
    private ImageView thermalConnectStatus; // 右上角 thermal 連線狀態
    private ImageView maskDetectResults; // 口罩偵測結果

    // Thermal Function
    private ImageView thermal_view; // 熱區圖
    private NumberFormat tempDetectFormatter = new DecimalFormat("#00.0");
    private float person_temp_static = 0.0f;
    private final float TEMP_BASIC_THRESHOLD = 30.0f;
    private final float TEMP_UPPER_THRESHOLD = 40.0f;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;
    private OpenCVCameraListener openCVCameraListener = new OpenCVCameraListener();

    // camera ftp value
    private int preview_frames = 0;
    private int fps = 0;

    // OpenCV Objects
    private CameraBridgeViewBase openCvCameraView;


    private CascadeClassifier classifier;
    private int mAbsoluteFaceSize = 0;
    private Mat mGray;
    private Mat mRgba;

    // Show compared score and start tip. Add by linhx 20170428 end
    private VerificationContract.Presenter mPresenter;


    // Constructor
    public VFRDetect20210303Fragment() {
    }

    public static DenseMatrix64F[] anchors;

    private Classifier detector;

    private static final int TF_OD_API_INPUT_SIZE = 260;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "face_mask_detection.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/face_mask_detection.txt";

    // Audio
    private AudioManager audioManager;
    private MediaPlayer mPlayer;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of DetectFragment.
     */
    public static VFRDetect20210303Fragment newInstance() {
        VFRDetect20210303Fragment fragment = new VFRDetect20210303Fragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
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

        mFaceRectPaint = new Paint();
        mFaceRectPaint.setTextSize(18f);
        mFaceRectPaint.setColor(Color.argb(150, 0, 255, 0));
        mFaceRectPaint.setStrokeWidth(3);
        mFaceRectPaint.setStyle(Paint.Style.STROKE);
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
        detect_bg = rootView.findViewById(R.id.detect_bg);

        appVersion = rootView.findViewById(R.id.appVersion);
        promptTv = rootView.findViewById(R.id.promptTv);
        tempDetected = rootView.findViewById(R.id.tempDetected);
        tempUnit = rootView.findViewById(R.id.tempUnit);

        openCvCameraView = rootView.findViewById(R.id.camera_view);
        mCameraPreview = rootView.findViewById(R.id.camera_preview);
        mOverlap = rootView.findViewById(R.id.surfaceViewOverlap);

        cancelDetectBtn = rootView.findViewById(R.id.cancelDetectBtn);
        adminSettingBtn = rootView.findViewById(R.id.adminSettingBtn);
        adminHomeBtn = rootView.findViewById(R.id.adminHomeBtn);
        confirmBtn = rootView.findViewById(R.id.confirmBtn);
        reCheckBtn = rootView.findViewById(R.id.reCheckBtn);
        scan_png = rootView.findViewById(R.id.scan_png);

        // OverLay
        circleOverlay = rootView.findViewById(R.id.circleOverlay);
        circleOverlay_green = rootView.findViewById(R.id.circleOverlay_green);

        detectFrame = rootView.findViewById(R.id.detectView);

        faceStatus = rootView.findViewById(R.id.faceStatus);
        thermalConnectStatus = rootView.findViewById(R.id.thermoConnectStatus);
        thermalDetect_bg = rootView.findViewById(R.id.thermoStatus);
        maskDetectResults = rootView.findViewById(R.id.maskStatus);

        thermal_view = rootView.findViewById(R.id.thermo_view);
        time_left = rootView.findViewById(R.id.time_left);
        time_right = rootView.findViewById(R.id.time_right);
        msg_big = rootView.findViewById(R.id.msg_big);
        msg_small = rootView.findViewById(R.id.msg_small);

        faceCapture = rootView.findViewById(R.id.faceCapture);
    }

    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());

        openCvCameraView.setCvCameraViewListener(openCVCameraListener);
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
                if (VMSEdgeCache.getInstance().getVms_kiosk_mode() == 1) {
                    backToWelcomePage();
                } else {
                    MessageTools.showToast(getContext(), "It's Normal Mode");
                }
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isQRCodeViewShow) {
                    switch (VMSEdgeCache.getInstance().getVms_kiosk_mode()) {
                        case 0: // Normal
                            reCheck();
                            break;
                        case 1: // Advanced
                            backToWelcomePage();
                            break;
                    }
                } else {
                    reCheck(); // 結果不合格一律 recheck
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

        mPlayer = MediaPlayer.create(getActivity().getBaseContext(), R.raw.alarm_20210524);


        mOverlap.setZOrderOnTop(true);
        mOverlap.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mOverlapHolder = mOverlap.getHolder();
        mCameraPreview.setCameraCallbacks(mCameraCallbacks);
    }

    // Upload data to vms backend
    private void uploadData() throws JSONException {
        try {
            Bitmap uploadTargetBitmap = VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? maskProcessBitmapShowOn : thermalBitmap;

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
            if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask()) {
                if (!isWearMask) {
                    exception = "No-Wear-Mask".toLowerCase();
                    status = "Exception".toLowerCase();
                }
            }

            if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                if (Float.valueOf(String.valueOf(tempDetectFormatter.format(person_temp_static))) >=
                        VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()) {
                    exception = "High-Fever".toLowerCase();
                    status = "Exception".toLowerCase();
                }
            }


            mLog.d(TAG, "uploadPersonData:> " + uploadPersonData);
            VmsUpload mMap = new VmsUpload(encoded,
                    isWearMask,
                    String.valueOf(tempDetectFormatter.format(person_temp_static)),
                    uploadPersonData,
                    interfaceMethod,
                    isVisitor,
                    dataUploadMode,
                    exception,
                    status
            );
            OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_SERVER_UPLOAD);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void uploadDataTPE() throws JSONException {
        try {
            Bitmap uploadTargetBitmap = VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? maskProcessBitmapShowOn : thermalBitmap;

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
            if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask()) {
                if (!isWearMask) {
                    exception = "No-Wear-Mask".toLowerCase();
                    status = "Exception".toLowerCase();
                }
            }

            if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                if (Float.valueOf(String.valueOf(tempDetectFormatter.format(person_temp_static))) >=
                        VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()) {
                    exception = "High-Fever".toLowerCase();
                    status = "Exception".toLowerCase();
                }
            }


            VmsUpload_TPE mMap = new VmsUpload_TPE(encoded,
                    isWearMask,
                    String.valueOf(tempDetectFormatter.format(person_temp_static)),
                    uploadPersonData,
                    interfaceMethod,
                    isVisitor,
                    dataUploadMode,
                    exception,
                    status
            );
            OKHttpAgent.getInstance().postTPERequest(mMap, APP_CODE_VMS_SERVER_UPLOAD_TPE);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    // including start detect process
    private void reCheck() {
        mLog.d(TAG, " *** reCheck *** ");
        if (VMSEdgeCache.getInstance().getVms_kiosk_mode() == 0) {
            uploadPersonData = null;
        }

        maskProcessBitmap = null;
        reCheckHandler.removeCallbacks(mFragmentRunnable);
        tick_count = 0;
        isFullDetectProcessDone = false;
        canStartToDetectGate = false;
        isQRCodeViewShow = false;
        canShowTempFlag = false;

        if (detectPageThread != null && detectPageThread.isAlive()) {
            detectPageThread.interrupt();
            threadObject.setRunning(false);
        }

        threadObject.setRunning(true);
        detectPageThread = new Thread(detectPageRunnable);
        detectPageThread.start();

        confirmBtn.setVisibility(View.INVISIBLE);
        reCheckBtn.setVisibility(View.INVISIBLE);
        detectFrame.setVisibility(View.VISIBLE);
        faceCapture.setVisibility(View.INVISIBLE);
        tempUnit.setVisibility(View.INVISIBLE);

        scan_png.setVisibility(View.INVISIBLE);

        msg_big.setText("Please wait for few second");
        msg_small.setText("Temperature detection distance 20 cm");

        thermal_view.setVisibility(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 1 ? View.VISIBLE : View.INVISIBLE);
        tempDetected.setText("");
        maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
        thermalDetect_bg.setImageBitmap(getBitmap(R.drawable.ic_temp_detect));
        tempUnit.setVisibility(View.INVISIBLE);

        if (null == uploadPersonData) {
            try {
                CheckVmsPersonSerial mMap = new CheckVmsPersonSerial("00000000");
                OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        detect_bg.setBackground(getResources().getDrawable(R.drawable.bg_basic));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
            mLog.d(TAG, " ### VMSEdgeCache.getInstance().getVms_kiosk_video_type()= " + VMSEdgeCache.getInstance().getVms_kiosk_video_type());
            detectFrame.setVisibility(View.VISIBLE);
//            staticDetectMaskSwitch = true;
            lazyLoad();
            connectAvaloThermalSocket();
        } else {
            threadObject.setRunning(false);
            reCheckHandler.removeCallbacks(mFragmentRunnable);
        }
    }

    // -------------- Lazy Load --------------
    private void lazyLoad() {
        mLog.d(TAG, "lazyLoad(), getUserVisibleHint()= " + getUserVisibleHint());
        if (getUserVisibleHint()) {
            if (VMSEdgeCache.getInstance().getVms_kiosk_mode() == 0) {
                uploadPersonData = null;
            }

            tick_count = 0;
            isFullDetectProcessDone = false;
            canStartToDetectGate = false;
            isQRCodeViewShow = false;
            canShowTempFlag = false;

            faceCapture.setVisibility(View.INVISIBLE);
            tempUnit.setVisibility(View.INVISIBLE);
            confirmBtn.setVisibility(View.INVISIBLE);
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

//            initClassifier();

            if (detectPageThread != null && detectPageThread.isAlive()) {
                detectPageThread.interrupt();
                threadObject.setRunning(false);
            }

            threadObject.setRunning(true);
            // Task Schedule handle thread
            detectPageThread = new Thread(detectPageRunnable);
            detectPageThread.start();

            thermal_view.setVisibility(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 1 ? View.VISIBLE : View.INVISIBLE);
            tempDetected.setText("");
            maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
            thermalDetect_bg.setImageBitmap(getBitmap(R.drawable.ic_temp_detect));
            tempUnit.setVisibility(View.INVISIBLE);

            if (null == uploadPersonData) {
                try {
                    CheckVmsPersonSerial mMap = new CheckVmsPersonSerial("00000000");
                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL);
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

            detect_bg.setBackground(getResources().getDrawable(R.drawable.bg_basic));

        }
    }


    private AvaloWebSocketClient c;
    private final Object mSyncObject = new Object();

    private void connectAvaloThermalSocket() {
        synchronized (mSyncObject) {
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
//                        mLog.d(TAG, " *** start ***");
                        Bitmap bitmap = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888);
                        ByteBuffer buffer = ByteBuffer.wrap(bytesResult);

                        bitmap.copyPixelsFromBuffer(buffer);

                        Message msg = new Message();
//                        msg.obj = bitmap;
//                        msg.obj = bitmap;
                        thermalBitmap_detect = bitmap;
                        mHandler.sendMessage(msg);
//                        mLog.d(TAG, " *** end *** ");
                        return;
                    }
                });
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            c.connect();
        }
    }

    static Bitmap thermalBitmap_detect = null;


    // 處理 avalo websocket data
    // 連到 avalo 才有資料
    private BleHandler mHandler = new BleHandler(getContext());

    private class BleHandler extends Handler {

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

            if (isFullDetectProcessDone) return; // 偵測完成後 不動作

//            thermal_view.setImageBitmap((Bitmap) msg.obj); // preview
            thermal_view.setImageBitmap((Bitmap) thermalBitmap_detect); // preview
//            thermalBitmap = (Bitmap) msg.obj; // results
            thermalBitmap = (Bitmap) thermalBitmap_detect; // results

            String detectLog = "";

            if (!canShowTempFlag) {
                tempDetected.setText("");
                tempUnit.setVisibility(View.INVISIBLE);
                thermalDetect_bg.setImageBitmap(getBitmap(R.drawable.ic_temp_detect));
                maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
            } else {
                if (isVmsConnected && null == uploadPersonData) {
                    try {
                        CheckVmsPersonSerial mMap = new CheckVmsPersonSerial("00000000");
                        OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL);
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                    return;
                }

                mLog.d(TAG, "origin temp:> " + person_temp_static + ", compensation:> " + VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation());
                person_temp_static = person_temp_static + VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation();
                boolean tempValidate = person_temp_static > VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp();
//                mLog.d(TAG, "tempValidate:> " + tempValidate + ", maskResults:> " + maskResults + ", temp-detect:> " + person_temp_static);
                // maskResults = 1, 2 算一次完整偵測
                switch (maskResults) {
                    case 2: // detecting
                        detectLog += "maskResult:> DETECTING, detect temp:> " + person_temp_static;
//                        startRecheckProcess();
                        maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
                        break;
                    case 1: // NoMask
                        detectLog += "maskResult:> No-MASK(1), detect temp:> " + person_temp_static;
                        if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask()) {
                            maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_off));
                            isDetectValidate = false; // 沒戴口罩 一定不合格
                        } else {
                            // disable mask
                            maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
                            if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                                isDetectValidate = tempValidate ? false : true; // 體溫小於門檻 才合格
                            } else {
                                // disable mask and temp
                                isDetectValidate = true;
                            }
                        }

                        if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                            if (person_temp_static <= TEMP_BASIC_THRESHOLD) return;
                            if (person_temp_static > TEMP_UPPER_THRESHOLD) {
                                tempDetected.setText("40 +");
                            } else {
                                tempDetected.setText(person_temp_static > TEMP_BASIC_THRESHOLD ? String.valueOf(tempDetectFormatter.format(person_temp_static)) : "");
                            }
                            thermalDetect_bg.setImageBitmap(tempValidate ? getBitmap(R.drawable.ic_temp_fail) : getBitmap(R.drawable.ic_temp_ok));
                        }

                        detectLog += ", enable Mask:> " + VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask() + ", enable temp:> "
                                + VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp();

                        mLog.d(TAG, detectLog);
                        isFullDetectProcessDone = true; // 算一次完整偵測
                        msg_big.setText(isDetectValidate ? "PASS" : "FAIL");

                        AppBus.getInstance().post(new BusEvent("show re-check check view", DETECT_DONE_SHOW_SNAPSHOT));

                        if (isVmsConnected) {
                            try {
                                uploadData();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            startRecheckProcess();
                        }

                        if (VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable()) {
                            try {
                                uploadDataTPE();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        LogWriter.storeLogToFile("," + detectLog + "," + new Date().getTime() / 1000);

                        if (!isDetectValidate) {
                            if (!mPlayer.isPlaying()) {
                                mPlayer.start();
                            }
                        }
                        stopDetectThread();
                        break;
                    case 0: // Wear Mask on
                        detectLog += "maskResult:> Wear-MASK(0), detect temp:> " + person_temp_static;
                        if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask()) {
                            maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_on));
                        } else {
                            maskDetectResults.setImageBitmap(getBitmap(R.drawable.ic_mask_detect));
                        }

                        if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                            if (person_temp_static <= TEMP_BASIC_THRESHOLD) return;
                            tempDetected.setText(person_temp_static > TEMP_BASIC_THRESHOLD ? String.valueOf(tempDetectFormatter.format(person_temp_static)) : "");
                            thermalDetect_bg.setImageBitmap(tempValidate ? getBitmap(R.drawable.ic_temp_fail) : getBitmap(R.drawable.ic_temp_ok));
                        }

                        detectLog += ", enable Mask:> " + VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask() + ", enable temp:> "
                                + VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp();

                        mLog.d(TAG, detectLog);
                        isFullDetectProcessDone = true; // 算一次完整偵測

                        if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                            isDetectValidate = tempValidate ? false : true; // 體溫小於門檻 才合格
                        } else {
                            isDetectValidate = true;
                        }

                        if (!isDetectValidate) {
                            AppBus.getInstance().post(new BusEvent("show re-check check view", DETECT_DONE_SHOW_SNAPSHOT));
                        }

                        msg_big.setText(isDetectValidate ? "PASS" : "FAIL");
                        if (isVmsConnected) {
                            try {
                                uploadData();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            AppBus.getInstance().post(new BusEvent("show re-check check view", DETECT_DONE_SHOW_SNAPSHOT));
                            startRecheckProcess();
//                            AppBus.getInstance().post(new BusEvent("", SHOW_NO_VMS_SERVER_QRCODE));
                        }

                        if (VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable()) {
                            try {
                                uploadDataTPE();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        LogWriter.storeLogToFile("," + detectLog + "," + new Date().getTime() / 1000);

                        if (!isDetectValidate) {
                            if (!mPlayer.isPlaying()) {
                                mPlayer.start();
                            }
                        }
                        stopDetectThread();
                        break;
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
        stopDetectThread();
        threadObject.setRunning(false);
        reCheckHandler.removeCallbacks(mFragmentRunnable);
    }

    @Override
    public void onStop() {
        mLog.d(TAG, " * onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        AppBus.getInstance().unregister(this);
        threadObject.setRunning(false);
        reCheckHandler.removeCallbacks(mFragmentRunnable);
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
    float offset = 5.0f;

    int noDetectCount = 0;

    private Bitmap maskProcessBitmap;
    private Bitmap maskProcessBitmapShowOn;
    private Bitmap maskProcessBitmapClone;
    private Bitmap thermalBitmap;
    private int maskResults = 2;
    private ObjectDetectInfo detectResult;

    private class OpenCVCameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

        @Override
        public void onCameraViewStarted(int width, int height) {
            mLog.d(TAG, " * onCameraViewStarted");
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

        double faceArea = 0.0d;

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mLog.d(TAG, "onCameraFrame");
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
                classifier.detectMultiScale(mGray, faces, 1.05, 2, 0,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                Rect[] facesArray = faces.toArray();
                Scalar faceRectColor = new Scalar(0, 255, 0, 255);
                Scalar faceRectColor_no_detect = new Scalar(0, 255, 255, 255);
                Rect theLargeFace = null;
                if (isDebugRecordMode) {
                    mLog.d(TAG, "facesArray length:> " + facesArray.length);
                }
                for (Rect faceRect : facesArray) {
                    if (null != theLargeFace && faceRect.area() > theLargeFace.area()) {
                        theLargeFace = faceRect;
                    }
                    if (null == theLargeFace) {
                        theLargeFace = faceRect;
                    }
                }
                // tl : top-left
                // br : bottom-right
                if (null != theLargeFace) {
                    if (isDebugRecordMode) {
                        mLog.d(TAG, "theLargeFace.width:> " + theLargeFace.width + ", theLargeFace.height:> " + theLargeFace.height + ", FACE_THRESHOLD:> " + FACE_THRESHOLD);
                    }
                }
                try {
                    // 選擇畫面中最大的臉做運算
                    if (null != theLargeFace && theLargeFace.width > FACE_THRESHOLD && theLargeFace.height > FACE_THRESHOLD && theLargeFace.area() > faceArea) {
                        faceArea = theLargeFace.area() * 0.85d;

                        AppBus.getInstance().post(new BusEvent("hide overlay", OVER_LAY_GREEN));
                        noDetectCount = 0;

                        final Bitmap bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.RGB_565);

                        if (isDebugRecordMode) {
                            Imgproc.rectangle(mRgba, theLargeFace.tl(), theLargeFace.br(), faceRectColor, 3);
                            Imgproc.putText(mRgba, "width:> " + theLargeFace.width + ", height:> " + theLargeFace.height, theLargeFace.tl(), Core.TYPE_MARKER, 2.0, new Scalar(0,255,0), 2);
                            Imgproc.putText(mRgba, "size:> " + FACE_THRESHOLD , theLargeFace.br() , Core.TYPE_MARKER, 2.0, new Scalar(0,0,255), 2);
                        }

                        Utils.matToBitmap(mRgba, bitmap);
                        Bitmap faceImageBitmap = Bitmap.createBitmap(bitmap, theLargeFace.x, theLargeFace.y, theLargeFace.width, theLargeFace.height);
                        if (faceImageBitmap != null) {
                            maskProcessBitmap = faceImageBitmap;
                            maskProcessBitmapClone = faceImageBitmap; // 得到運算的臉
                        }
                    } else {
                        if (isDebugRecordMode) {
                            mLog.d(TAG, "NO OVER FACE_THRESHOLD:> " + FACE_THRESHOLD + ", noDetectCount:> " + noDetectCount);
                        }
                        noDetectCount++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (facesArray.length == 0) { //畫面中沒有臉
                    noDetectCount++;
                }

                if (noDetectCount > FACE_THRESHOLD_COUNT) { // 多少frame後，顯示藍色框框
                    AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_BLUE));
                    faceArea = 0.0d;
                    maskProcessBitmap = null; // 清空cache
                }
                fps++;
            }
            return mRgba;
        }
    }

    public void onEventMainThread(BusEvent event) {
//        mLog.d(TAG, "event:> " + event.getEventType());
        switch (event.getEventType()) {
            case TIME_TICK:
                Date d = new Date();
                CharSequence s_1 = android.text.format.DateFormat.format("yyyy-MM-dd", d.getTime());
                CharSequence s_2 = android.text.format.DateFormat.format("HH:mm", d.getTime());
                time_left.setText(s_1);
                time_right.setText(s_2);
                break;
            case OVER_LAY_GREEN:
                circleOverlay.setVisibility(View.GONE);
                circleOverlay_green.setVisibility(View.VISIBLE);
                promptTv.setText("Please Stay the Position for 3 Seconds");
                if (isQRCodeViewShow) return;
                if (!isFullDetectProcessDone) msg_big.setText("Please wait for few second");
                break;
            case OVER_LAY_BLUE:
                maskResults = 2;
                circleOverlay.setVisibility(View.VISIBLE);
                circleOverlay_green.setVisibility(View.GONE);
                promptTv.setText("Please Come Closer to Camera");
                if (isQRCodeViewShow) return;
                if (!isFullDetectProcessDone)
                    msg_big.setText("Please get closer to the screen for testing");
                break;
            case FACE_DETECT_DONE:
                stopCameraFunction();
                break;
            case DETECT_DONE_SHOW_SNAPSHOT:
                detectFrame.setVisibility(View.INVISIBLE);
                faceCapture.setVisibility(View.VISIBLE);
                faceCapture.setImageBitmap(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? maskProcessBitmapShowOn : thermalBitmap);
                detect_bg.setBackground(getResources().getDrawable(R.drawable.bg_frame_20210304));

                if (isVmsConnected) {
                    confirmBtn.setVisibility(View.INVISIBLE);
                    reCheckBtn.setVisibility(View.VISIBLE);
                } else {
                    confirmBtn.setVisibility(isDetectValidate ? View.VISIBLE : View.INVISIBLE);
                    reCheckBtn.setVisibility(isDetectValidate ? View.INVISIBLE : View.VISIBLE);
                }

                if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                    tempUnit.setVisibility(View.VISIBLE);
                }
                msg_small.setText(isDetectValidate ? "Check-in Success !! " : "Please click re-check button to recheck");
                break;
            case APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS: // get temp
                try {
                    String response = event.getMessage();
                    JSONObject jsonObj = new JSONObject(response);
                    person_temp_static = (float) jsonObj.getDouble("Temperature");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_AVALO_THERMAL_POST_TEMP_SUCCESS:
                String response = event.getMessage();
                mLog.d(TAG, "response:> " + response + ", isThermometerServerConnected:> " + isThermometerServerConnected);
                person_temp_static = (float) Float.parseFloat(response);
                break;
            case MASK_DETECT:
                // *** CORE MASK DETECT PROCESS ***
                try {
                    if (maskProcessBitmap == null) return;
                    isStartAnalysisMask = true;
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
                    if (!canStartToDetectGate) return;

                    ObjectDetectInfo results = detector.recognizeObject(croppedBitmap);
                    if (results != null) {
                        if (results.getConfidence() < 0.5) {
                            mLog.d(TAG, "results.getConfidence():> " + results.getConfidence());
                            return;
                        }
                        if (maskProcessBitmap == null) return;
                        maskProcessBitmapShowOn = maskProcessBitmap;
                        maskResults = results.getClasses();
                        detectResult = results;
                        switch (results.getClasses()) {
                            case 1: // NoMask
                                canShowTempFlag = true;
//                                mLog.d(TAG, "results.getX_min():> " + results.getX_min() + ", results.getX_max():> " + results.getX_max());
//                                mLog.d(TAG, "(int)(results.getX_min() * screenWidth - offset):> " + (int)(results.getX_min() * screenWidth - offset) +
//                                        ", (int)(results.getY_min() * screenHeight - offset):> " + (int)(results.getY_min() * screenHeight - offset));
////                                mLog.d(TAG, "results.getY_min():> " + results.getY_min() + ", results.getY_max():> " + results.getY_max());
//                                mLog.d(TAG, "(int)(results.getX_max() * screenWidth + offset):> " + (int)(results.getX_max() * screenWidth + offset) +
//                                        ", (int)(results.getY_max() * screenHeight + offset):> " + (int)(results.getY_max() * screenHeight + offset));
//                        Imgproc.rectangle(mRgba,
////                                new org.opencv.core.Point( (int) 5, (int)5),
////                                new org.opencv.core.Point( (int)5, (int)5),
//                                new org.opencv.core.Point( (int)(results.getX_min() * screenWidth - offset), (int)(results.getY_min() * screenHeight - offset) ),
//                                new org.opencv.core.Point( (int)(results.getX_max() * screenWidth + offset), (int)(results.getY_max() * screenHeight + offset) ),
//                                new Scalar(0, 255, 0, 255), 3);
//                        Imgproc.putText(mRgba, "No Mask " + String.format("{"Attachments":[{"__type":"ItemIdAttachment:#Exchange","ItemId":{"__type":"ItemId:#Exchange","ChangeKey":null,"Id":"AAMkADA3N2YzZWEwLWIzYjMtNDgxOS1iZTgyLWVjNDNiMjY3ZDM1YQBGAAAAAAAcl1sCtwkBSbeQy2pznjzTBwBbCJtwEhi3QbCQIbpG/X6PAAAAAAEMAABbCJtwEhi3QbCQIbpG/X6PAAJAiVVQAAA="},"Name":"大家的尾牙善款_成為雪中的熱炭","IsInline":false},{"__type":"ItemIdAttachment:#Exchange","ItemId":{"__type":"ItemId:#Exchange","ChangeKey":null,"Id":"AAMkADA3N2YzZWEwLWIzYjMtNDgxOS1iZTgyLWVjNDNiMjY3ZDM1YQBGAAAAAAAcl1sCtwkBSbeQy2pznjzTBwBbCJtwEhi3QbCQIbpG/X6PAAAAAAEMAABbCJtwEhi3QbCQIbpG/X6PAAJAiVVPAAA="},"Name":"大家的尾牙善款_成為雪中的熱炭","IsInline":false}]}(%.1f%%) ", results.getConfidence() * 100.0f) , new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(255,0,0), 2);
//                                MessageTools.showToast(getContext(), "No Mask");
                                break;
                            case 0: // Mask
                                canShowTempFlag = true;
//                                //                                mLog.d(TAG, "results.getX_min():> " + results.getX_min() + ", results.getX_max():> " + results.getX_max());
//                                mLog.d(TAG, "(int)(results.getX_min() * screenWidth - offset):> " + (int)(results.getX_min() * screenWidth - offset) +
//                                        ", (int)(results.getY_min() * screenHeight - offset):> " + (int)(results.getY_min() * screenHeight - offset));
////                                mLog.d(TAG, "results.getY_min():> " + results.getY_min() + ", results.getY_max():> " + results.getY_max());
//                                mLog.d(TAG, "(int)(results.getX_max() * screenWidth + offset):> " + (int)(results.getX_max() * screenWidth + offset) +
//                                        ", (int)(results.getY_max() * screenHeight + offset):> " + (int)(results.getY_max() * screenHeight + offset));

//                                Imgproc.rectangle(mRgba,
//                                new org.opencv.core.Point(results.getX_min() * screenWidth + offset, results.getY_min() * screenHeight + offset),
//                                new org.opencv.core.Point(results.getX_max() * screenWidth + offset, results.getY_max() * screenHeight + offset),
//                                new Scalar(0, 255, 255, 0), 3);
//                        Imgproc.putText(mRgba, "Mask " + String.format("(%.1f%%) ", results.getConfidence() * 100.0f), new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(0,255,0), 2);
//                                MessageTools.showToast(getContext(), "Mask On");
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + results.getClasses());
                        }
                        mLog.i(TAG, "mask result:> " + results.toString() + ", confidence:> " + results.getConfidence());
                    } else {
                        maskResults = 2; // 2 means not detected yet
                        canShowTempFlag = false;
                        mLog.w(TAG, "* detect frame NO RESULTS *");
                    }
                } catch (Exception e) {
                    maskResults = 2; // 2 means not detected yet
                    canShowTempFlag = false;
                    mLog.e(TAG, "MASK_DETECT, e:> " + e);
                    e.printStackTrace();
                } finally {
                    isStartAnalysisMask = false;
                }
                break;
            case APP_CODE_VMS_SERVER_UPLOAD_SUCCESS:
                mLog.d(TAG, "isDetectValidate:> " + isDetectValidate);
                if (!isDetectValidate) {
                    // FAIL Process
                    startRecheckProcess();
                    return;
                }
                isQRCodeViewShow = true;
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

                    detectFrame.setVisibility(View.INVISIBLE);
                    faceCapture.setVisibility(View.VISIBLE);
                    if (VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp()) {
                        tempUnit.setVisibility(View.VISIBLE);
                    }
                    startRecheckProcess();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case SHOW_NO_VMS_SERVER_QRCODE: // Deprecated on 20210503
                startRecheckProcess();
                detectFrame.setVisibility(View.INVISIBLE);
                faceCapture.setVisibility(View.VISIBLE);

                Date today = new Date();
                Date today_end = new Date(today.getYear(), today.getMonth(), today.getDate(), 23, 59, 59);
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

                String startDate_display = (String) android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", today.getTime());
                String endDate_display = (String) android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", today_end.getTime());

                msg_big.setText("Allow access " + startDate_display + " ~ " + endDate_display);
                msg_small.setText("");
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL_SUCCESS:
                mLog.d(TAG, "APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL_SUCCESS");
                break;
            case APP_CODE_VMS_KIOSK_STATUS_INACTIVE_SUCCESS:
                onPause();
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_SYNC:
                if (getUserVisibleHint()) {
                    onResume();
                }
                break;
        }
        if (null != thermalConnectStatus)
            thermalConnectStatus.setImageDrawable(isVmsConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));

        if (!isThermometerServerConnected) {
            person_temp_static = 0.0f;
        }
    }

    private void startRecheckProcess() {
        mLog.d(TAG, "startRecheckProcess, timeout:> " + VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout() + " s");
        reCheckHandler.removeCallbacks(mFragmentRunnable);
        reCheckHandler.postDelayed(mFragmentRunnable, VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout() * 1000);
    }

    private void stopCameraFunction() {
        if (openCvCameraView != null) {
            openCvCameraView.disableView();
        }
        stopDetectThread();
    }

    private void stopDetectThread() {
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
        threadObject.setRunning(false);
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
        int period = 10;

        @Override
        public void run() {
            while (threadObject.isRunning()) {
//                mLog.d(TAG, "tick_count:> " + tick_count);
                try {
                    long start_time_tick = System.currentTimeMillis();
                    if (tick_count % period == 0) {
                        if (isDebugRecordMode) {
                            mLog.d(TAG, "preview frames= " + ((float) preview_frames / period) + ", fps= " + ((float) fps / period));
                        }
                        preview_frames = 0;
                        fps = 0;
                    }

                    if (tick_count % 5 == 1) { // 0.3 * 5 = 1.5s
                        if (!isFullDetectProcessDone) {
                            canStartToDetectGate = true;
                        }
                    }

                    if (tick_count % 10 == 5) {
                        if (!isThermometerServerConnected) {
                            if (null != c && !c.isOpen()) {
                                connectAvaloThermalSocket();
                            }
                            if (null != c) {
                                c.close();
                            }
                        }
                    }

                    if (tick_count % 2 == 1) {
                        Message msg = new Message();
                        mHandler.sendMessage(msg);
                    }

                    if (tick_count % 3 == 0) { // 0.3 * 3 = 0.9s，運算一次口罩偵測
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
    private Handler reCheckHandler = new MainHandler();
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

    private Camera.Size mPreviewSize;
    private CameraPreview2 mCameraPreview;
    protected SurfaceView mOverlap;

    private SurfaceHolder mOverlapHolder;
    private android.graphics.Rect focusRect = new android.graphics.Rect();
    private Paint mFaceRectPaint = null;

    private float mPreviewScaleX = 1.0f;
    private float mPreviewScaleY = 1.0f;

    private int padx = 30;
    private int pady = -20;

    private CameraCallbacks mCameraCallbacks = new CameraCallbacks() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mPreviewSize == null) {
                mPreviewSize = camera.getParameters().getPreviewSize();
                mLog.d(TAG, "mCameraPreview.getWidth():> " + mCameraPreview.getWidth() + ", mCameraPreview.getHeight():> " + mCameraPreview.getHeight());
                mLog.d(TAG, "mPreviewSize.width:> " + mPreviewSize.width + ", mPreviewSize.height:> " + mPreviewSize.height);
//                mPreviewScaleX = (float) (mCameraPreview.getHeight()) / mPreviewSize.width;
//                mPreviewScaleY = (float) (mCameraPreview.getWidth()) / mPreviewSize.height;
            }

//            Log.d(TAG, "data.length.:> " + data.length);
//            Log.d(TAG, "mPreviewScaleX:> " + mPreviewScaleX + ", mPreviewScaleY:> " + mPreviewScaleY);
            preview_frames++;
            mPresenter.detect(data, mPreviewSize.width, mPreviewSize.height, mCameraPreview.getCameraRotation());
        }

        @Override
        public void onCameraUnavailable(int errorCode) {
            isCameraUnavailable = true;
            mLog.e(TAG, "camera unavailable, reason=%d " + errorCode);
        }
    };

    @Override
    public void drawFaceRect(Rect faceRect) {
        if (!isActive()) {
            return;
        }
        Canvas canvas = mOverlapHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        fps++;
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if (isFullDetectProcessDone) {
            mOverlapHolder.unlockCanvasAndPost(canvas);
            AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_BLUE));
            return;
        }
        if (faceRect != null) {
//            faceRect.x *= mPreviewScaleX;
//            faceRect.y *= mPreviewScaleY;
//            faceRect.width *= mPreviewScaleX;
//            faceRect.height *= mPreviewScaleY;

            focusRect.left = faceRect.x - padx;
            focusRect.right = faceRect.x + faceRect.width - padx;
            focusRect.top = faceRect.y + pady;
            focusRect.bottom = faceRect.y + faceRect.height + pady;

            int face_width = faceRect.width;
            int face_height = faceRect.height;

            if (faceRect.width < FACE_THRESHOLD || faceRect.height < faceRect.width) {
                mLog.d(TAG, "face_width:> " + faceRect.width + ", face_height:> " + faceRect.height +
                        ", FACE_THRESHOLD:> " + FACE_THRESHOLD);
                mOverlapHolder.unlockCanvasAndPost(canvas);
                return;
            }
            if (isDebugRecordMode) {
                mLog.d(TAG, "face_width:> " + faceRect.width + ", face_height:> " + faceRect.height);
//                Log.d(TAG, "faceRect.x:> " + faceRect.x +
//                        ", faceRect.y:> " + faceRect.y +
//                        ", faceRect.width:> " + faceRect.width +
//                        ", faceRect.height:> " + faceRect.height +
//                        ", mPreviewScaleX:> " + mPreviewScaleX +
//                        ", mPreviewScaleY:> " + mPreviewScaleY);
                canvas.drawRect(focusRect, mFaceRectPaint);
                canvas.drawText("face_width:> " + faceRect.width + ", face_height:> " + faceRect.height
                        , faceRect.x, faceRect.y, mFaceRectPaint);
            }
            AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_GREEN));
        } else {
            noDetectCount++;
//            mLog.d(TAG, " *** NO FACE *** ");
            if (noDetectCount > FACE_THRESHOLD_COUNT) { // 多少frame後，顯示藍色框框
                AppBus.getInstance().post(new BusEvent("show overlay", OVER_LAY_BLUE));
                maskProcessBitmap = null; // 清空cache
            }
        }
        mOverlapHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void drawTestMat(Bitmap src) {
        if (!isStartAnalysisMask) {
            maskProcessBitmap = src;
        }
    }

    @Override
    public void toastMessage(String msg) {

    }

    @Override
    public void showCameraUnavailableDialog(int errorCode) {

    }

    @Override
    public TextureView getTextureView() {
        return mCameraPreview;
    }

    @Override
    public boolean isActive() {
        return getView() != null && isAdded() && !isDetached();
    }

    @Override
    public void setPresenter(VerificationContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    private boolean isStartAnalysisMask = false;
}
