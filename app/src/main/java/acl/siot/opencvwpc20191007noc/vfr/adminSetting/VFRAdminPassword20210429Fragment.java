package acl.siot.opencvwpc20191007noc.vfr.adminSetting;

import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import acl.siot.opencvwpc20191007noc.util.LogWriter;
import acl.siot.opencvwpc20191007noc.util.MessageTools;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.AppUtils;

import java.util.Date;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.cache.VFRAppSetting;
import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.util.MLog;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.SimpleTextChangedWatcher;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

import static acl.siot.opencvwpc20191007noc.App.TIME_TICK;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.App.isVmsConnected;

/**
 * Created by IChen.Chu on 2020/05/25
 * A fragment to show admin setting password page.
 */
public class VFRAdminPassword20210429Fragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private TextView appVersion;
    private Button confirmBtn;

    private TextFieldBoxes text_field_boxes;
    private ExtendedEditText extended_edit_text;

    private String passwordInput = "";
    private ImageView thermoConnectStatus;

    private Button adminHomeBtn;
    private TextView time_left;
    private TextView time_right;



    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public VFRAdminPassword20210429Fragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static VFRAdminPassword20210429Fragment newInstance() {
        VFRAdminPassword20210429Fragment fragment = new VFRAdminPassword20210429Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        if (isDebugRecordMode) LogWriter.storeLogToDebugFile(" * onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        // register event Bus
        AppBus.getInstance().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.vfr_fragment_admin_password_20210429, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        confirmBtn = rootView.findViewById(R.id.confirmBtn);

        text_field_boxes = rootView.findViewById(R.id.text_field_boxes);
        extended_edit_text = rootView.findViewById(R.id.extended_edit_text);

        adminHomeBtn = rootView.findViewById(R.id.adminHomeBtn);
        time_left = rootView.findViewById(R.id.time_left);
        time_right = rootView.findViewById(R.id.time_right);

        thermoConnectStatus = rootView.findViewById(R.id.thermoConnectStatus);
    }


    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VMSEdgeCache.getInstance().getVms_kiosk_settingPassword().equals(passwordInput.toString())) {
//                    extended_edit_text.requestFocus();
                    onFragmentInteractionListener.clickConfirmPWD();
                } else {
                    text_field_boxes.setLabelText("");
                    text_field_boxes.setError("The password is incorrect, please try again.", true);
//                    Toast.makeText(getContext(), "App Setting Password is not correct.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adminHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickBackToDetectPage();
            }
        });

        text_field_boxes.getEndIconImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLog.d(TAG, "onClick:> " + extended_edit_text.getInputType());
                text_field_boxes.setEndIcon(extended_edit_text.getInputType() == 1 ? R.drawable.icon_eye_open :  R.drawable.icon_eye_close);

                extended_edit_text.setInputType(extended_edit_text.getInputType() == 1 ?
                        65665 :
                        InputType.TYPE_CLASS_TEXT);
            }
        });


        text_field_boxes.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
            @Override
            public void onTextChanged(String theNewText, boolean isError) {
                mLog.d(TAG, "theNewText:> " + theNewText);
                if (theNewText.equals("")) {
                    text_field_boxes.setLabelText("Please Enter Password");
                }
                passwordInput = theNewText;
            }
        });

        thermoConnectStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MessageTools.showLongToast(getContext(), "v " + AppUtils.getAppVersionName());
                clickTimes ++;
                if (clickTimes > 5) {
                    if (!isDebugRecordMode)
                        LogWriter.storeLogToDebugFile(" ===== Start Records Debug Logs ===== ");
                    isDebugRecordMode = true;
                    MessageTools.showLongToast(getContext(), "已開啟Debug 紀錄模式，" + " v " + AppUtils.getAppVersionName());
                } else {
                    MessageTools.showLongToast(getContext(), "再 " + (6-clickTimes) + " 次，開啟Debug 紀錄模式，"+" v " + AppUtils.getAppVersionName());
                }

            }
        });
    }

    int clickTimes = 0;

    public static boolean isDebugRecordMode = false;

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
            extended_edit_text.setText("");
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
        AppBus.getInstance().unregister(this);
        super.onDestroy();
    }

    // -------------------------------------------
    public interface OnFragmentInteractionListener {
        void clickBackToDetectPage();

        void clickConfirmPWD();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onEventMainThread(BusEvent event) {
        switch (event.getEventType()) {
            case TIME_TICK:
                Date d = new Date();
                CharSequence s_1 = android.text.format.DateFormat.format("yyyy-MM-dd", d.getTime());
                CharSequence s_2 = android.text.format.DateFormat.format("hh:mm", d.getTime());
                time_left.setText(s_1);
                time_right.setText(s_2);
                break;
        }
        if (null != thermoConnectStatus)
            thermoConnectStatus.setImageDrawable(isVmsConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));
    }

}
