package acl.siot.opencvwpc20191007noc.vfr.welcome;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.AppUtils;

import java.lang.ref.WeakReference;
import java.util.Date;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.util.MLog;

import static acl.siot.opencvwpc20191007noc.App.VFR_HEART_BEATS;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
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

    private FrameLayout frameWelcome;
    private FrameLayout frameRFID;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.vfr_fragment_welcome_20210308, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        frameWelcome = rootView.findViewById(R.id.frameWelcome);
        frameHere = rootView.findViewById(R.id.frameHere);
        frameRFID = rootView.findViewById(R.id.frameRFID);

        appVersion = rootView.findViewById(R.id.appVersion);

        time_left = rootView.findViewById(R.id.time_left);
        time_right = rootView.findViewById(R.id.time_right);
        msg_big = rootView.findViewById(R.id.msg_big);
        msg_small = rootView.findViewById(R.id.msg_small);

        adminSettingBtn = rootView.findViewById(R.id.adminSettingBtn);
        thermoConnectStatus = rootView.findViewById(R.id.thermoConnectStatus);
        hereBtn = rootView.findViewById(R.id.hereBtn);
        adminHomeBtn = rootView.findViewById(R.id.adminHomeBtn);

    }


    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());

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

        adminHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminHomeBtn.setVisibility(View.INVISIBLE);
                frameHere.setVisibility(View.INVISIBLE);
                frameWelcome.setVisibility(View.VISIBLE);
            }
        });

        frameRFID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminHomeBtn.setVisibility(View.VISIBLE);
                frameWelcome.setVisibility(View.INVISIBLE);
                frameHere.setVisibility(View.VISIBLE);
            }
        });

        hereBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickToDetectPage();
            }
        });
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


    public void onEventMainThread(BusEvent event) {
//        mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
        switch (event.getEventType()) {
            case VFR_HEART_BEATS:
                Date d = new Date();
                CharSequence s_1 = android.text.format.DateFormat.format("yyyy-MM-dd",d.getTime());
                CharSequence s_2 = android.text.format.DateFormat.format("HH:mm",d.getTime());
                time_left.setText(s_1);
                time_right.setText(s_2);
                break;
            case APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS:
                thermoConnectStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));
                break;
        }
    }
}
