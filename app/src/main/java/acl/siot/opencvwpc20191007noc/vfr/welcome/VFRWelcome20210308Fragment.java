package acl.siot.opencvwpc20191007noc.vfr.welcome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.AppUtils;

import java.lang.ref.WeakReference;
import java.util.Date;

import acl.siot.opencvwpc20191007noc.App;
import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.util.MLog;

import static acl.siot.opencvwpc20191007noc.App.VFR_HEART_BEATS;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.App.uploadPersonData;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;

/**
 * Created by IChen.Chu on 2021/03/08
 * A fragment to show welcome page.
 */
public class VFRWelcome20210308Fragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private TextView appVersion;

    private TextView time_left;
    private TextView time_right;
    private TextView msg_big;
    private TextView msg_small;

    private Button adminSettingBtn;
    private ImageView thermoConnectStatus;
    private ImageView hereBtn;
    private Button adminHomeBtn;

    private FrameLayout frameWelcomeTxt;


    private FrameLayout frameMain;
    private ImageView mainBtn;
    private TextView mainTxtMid;
    private TextView mainTxt;
    private EditText mainEdit;

    private FrameLayout frameRFID;
    private ImageView rfidBtn;
    private FrameLayout frameBarCode;
    private ImageView barCodeBtn;
    private FrameLayout frameHere;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public VFRWelcome20210308Fragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static VFRWelcome20210308Fragment newInstance() {
        VFRWelcome20210308Fragment fragment = new VFRWelcome20210308Fragment();
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
        AppBus.getInstance().register(this);
    }

    private View mainView;

    private void setViewLayout(int id){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(id, null);
        ViewGroup mRootView = (ViewGroup) getView();
        mRootView.removeAllViews();
        mRootView.addView(mainView);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        mainView = inflater.inflate(R.layout.vfr_fragment_welcome_20210308, null);
        initViewIDs(mainView);
        initViewsFeature();

        return mainView;
    }

    private void initViewIDs(View mainView) {
        frameWelcomeTxt = mainView.findViewById(R.id.frameWelcomeText);
        frameHere = mainView.findViewById(R.id.frameHere);

        frameMain = mainView.findViewById(R.id.frameMain);
        mainBtn = mainView.findViewById(R.id.mainBtn);
        mainTxtMid = mainView.findViewById(R.id.mainTxtMid);
        mainTxt = mainView.findViewById(R.id.mainTxt);
        mainEdit = mainView.findViewById(R.id.mainEdit);

        frameRFID = mainView.findViewById(R.id.frameRFID);
        rfidBtn = mainView.findViewById(R.id.rfidBtn);

        appVersion = mainView.findViewById(R.id.appVersion);

        time_left = mainView.findViewById(R.id.time_left);
        time_right = mainView.findViewById(R.id.time_right);
        msg_big = mainView.findViewById(R.id.msg_big);
        msg_small = mainView.findViewById(R.id.msg_small);

        adminSettingBtn = mainView.findViewById(R.id.adminSettingBtn);
        thermoConnectStatus = mainView.findViewById(R.id.thermoConnectStatus);
        hereBtn = mainView.findViewById(R.id.hereBtn);
        adminHomeBtn = mainView.findViewById(R.id.adminHomeBtn);

    }


    private void initViewsFeature() {
        Date d = new Date();
        CharSequence s_1 = android.text.format.DateFormat.format("yyyy-MM-dd", d.getTime());
        CharSequence s_2 = android.text.format.DateFormat.format("HH:mm", d.getTime());
        time_left.setText(s_1);
        time_right.setText(s_2);
        appVersion.setText("v " + AppUtils.getAppVersionName());

        adminSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAdminSettingPage();
            }
        });

        adminHomeBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                lazyLoad();
            }
        });

        rfidBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                mLog.d(TAG, "rfidBtn onclick");
                mainEdit.requestFocus();
                mainEdit.setInputType(InputType.TYPE_NULL);

                App.isRFIDFunctionOn = true;
                App.isBarCodeFunctionOn = false;

                adminHomeBtn.setVisibility(View.VISIBLE);
                frameWelcomeTxt.setVisibility(View.INVISIBLE);

                frameRFID.setVisibility(View.INVISIBLE);
                frameBarCode.setVisibility(View.INVISIBLE);

                frameMain.setVisibility(View.VISIBLE);
                mainBtn.setImageBitmap(getBitmap(R.drawable.ic_rfid_btn));
                mainTxtMid.setText("RFID");
                mainTxt.setText("RFID");
                frameHere.setVisibility(View.VISIBLE);
            }
        });

        mainEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String[] scannedInput = charSequence.toString().split(",");
//                if (scannedInput.length == 7) {
//                    if (scannedInput[6].trim().length() == 8) {
//                        mLog.d(TAG, "CARD ID:> " + scannedInput[6].trim());
//                    }
//                }
                if (scannedInput.length == 8) {
                    if (scannedInput[7].trim().length() == 24) {
                        mLog.d(TAG, "personUUID:> " + scannedInput[7].trim());
                        mLog.d(TAG, "vmsPersonSyncMap:> " + App.vmsPersonSyncMapUUID.size());
                        if (App.vmsPersonSyncMapUUID.containsKey( scannedInput[7].trim())) {
                            uploadPersonData = App.vmsPersonSyncMapUUID.get(scannedInput[7].trim());
                            mLog.d(TAG, "barCodeScanned Person:> " + uploadPersonData);
                            mGotoDetectHandler.removeCallbacks(mFragmentRunnable);
                            mGotoDetectHandler.postDelayed(mFragmentRunnable, 300L);
                        }
                    }
                }
//                if (App.isBarCodeFunctionOn) {
//                    MessageTools.showToast(getContext(), charSequence.toString());
//                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
//                mLog.d(TAG, "afterTextChanged");
            }
        });

//        hereBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                onFragmentInteractionListener.clickToDetectPage();
//            }
//        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    private void goToAdminSettingPage() {
        onFragmentInteractionListener.onClickAdminSetting();
    }

    /**
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mLog.d(TAG, "isVisibleToUser= " + isVisibleToUser);
        if (isVisibleToUser) {
            mLog.d(TAG, "barCode:> " + VMSEdgeCache.getInstance().getVms_kiosk_device_input_bar_code_scanner());
            mLog.d(TAG, "card reader:> " + VMSEdgeCache.getInstance().getVms_kiosk_device_input_card_reader());
            lazyLoad();
        } else {
        }
    }

    // -------------- Lazy Load --------------
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void lazyLoad() {
        mLog.d(TAG, "lazyLoad(), getUserVisibleHint()= " + getUserVisibleHint());
        if (getUserVisibleHint()) {
        }

        App.isRFIDFunctionOn = false;
        App.isBarCodeFunctionOn = false;

        if (VMSEdgeCache.getInstance().getVms_kiosk_device_input_bar_code_scanner() && VMSEdgeCache.getInstance().getVms_kiosk_device_input_card_reader()) {

        } else if (VMSEdgeCache.getInstance().getVms_kiosk_device_input_bar_code_scanner()) {
            setViewLayout(R.layout.vfr_fragment_welcome_20210308_barcode_only);
            frameBarCode = mainView.findViewById(R.id.frameBarCode);
            barCodeBtn = mainView.findViewById(R.id.barCodeBtn);
            barCodeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLog.d(TAG, "frameRFID onclick");
                    mainEdit.requestFocus();
                    mainEdit.setInputType(InputType.TYPE_NULL);

                    App.isRFIDFunctionOn = false;
                    App.isBarCodeFunctionOn = true;

                    adminHomeBtn.setVisibility(View.VISIBLE);
                    frameWelcomeTxt.setVisibility(View.VISIBLE);
                    msg_big.setText("Please scan the qrCode to login");

                    frameRFID.setVisibility(View.INVISIBLE);
                    frameBarCode.setVisibility(View.INVISIBLE);

                    frameMain.setVisibility(View.VISIBLE);
                    mainBtn.setImageBitmap(getBitmap(R.drawable.ic_barcode));
                    mainTxtMid.setText("");
                    mainTxt.setText("BARCODE");

                    frameHere.setVisibility(View.GONE);
                }
            });
        } else if (VMSEdgeCache.getInstance().getVms_kiosk_device_input_card_reader()) {
        } else {
            setViewLayout(R.layout.vfr_fragment_welcome_20210308);
        }

        initViewIDs(mainView);
        initViewsFeature();
        mainEdit.requestFocus();
        mainEdit.setInputType(InputType.TYPE_NULL);

        msg_big.setText("Please choose a way to login");
//        adminHomeBtn.callOnClick();
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
    }

    @Override
    public void onStop() {
        mLog.d(TAG, " * onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mLog.d(TAG, " * onDestroy");
        super.onDestroy();
    }

    // -------------------------------------------
    public interface OnFragmentInteractionListener {
        void clickToDetectPage();

        void onClickAdminSetting();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }


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
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onEventMainThread(BusEvent event) {
//        mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
        switch (event.getEventType()) {
            case VFR_HEART_BEATS:
//                lazyLoad();
                Date d = new Date();
                CharSequence s_1 = android.text.format.DateFormat.format("yyyy-MM-dd", d.getTime());
                CharSequence s_2 = android.text.format.DateFormat.format("HH:mm", d.getTime());
                time_left.setText(s_1);
                time_right.setText(s_2);
                break;
            case APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS:
                thermoConnectStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));
                break;
        }
    }

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
    private Handler mGotoDetectHandler = new MainHandler();
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
            onFragmentInteractionListener.clickToDetectPage();
        }
    }
}
