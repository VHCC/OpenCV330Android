package acl.siot.opencvwpc20191007noc.vfr.adminSetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;

import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by IChen.Chu on 2020/05/25
 * A fragment to show admin setting password page.
 */
public class VFRAdminSettingFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private Button confirmBtn;
    private Button backBtn;

    private EditText ipAddressEditTxt;
    private EditText portEditTxt;
    private EditText userAccountEditTxt;
    private EditText userPWDEditTxt;
    private EditText tabletIDEditTxt;

    private EditText thermalIpEditTxt;
    private EditText alertTempEditTxt;
    private EditText httpCMDEditTxt;

    private EditText changePWDEditTxt;
    private EditText changePWDAgainEditTxt;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public VFRAdminSettingFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static VFRAdminSettingFragment newInstance() {
        VFRAdminSettingFragment fragment = new VFRAdminSettingFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.vfr_fragment_admin_setting, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        confirmBtn = rootView.findViewById(R.id.confirmBtn);
        backBtn = rootView.findViewById(R.id.backBtn);

        ipAddressEditTxt = rootView.findViewById(R.id.ipAddressEditTxt);
        portEditTxt = rootView.findViewById(R.id.portEditTxt);
        userAccountEditTxt = rootView.findViewById(R.id.userAccountEditTxt);
        userPWDEditTxt = rootView.findViewById(R.id.userPWDEditTxt);
        tabletIDEditTxt = rootView.findViewById(R.id.tabletIDEditTxt);

        thermalIpEditTxt = rootView.findViewById(R.id.thermalIpEditTxt);
        alertTempEditTxt = rootView.findViewById(R.id.alertTempEditTxt);
        httpCMDEditTxt = rootView.findViewById(R.id.httpCMDEditTxt);

        changePWDEditTxt = rootView.findViewById(R.id.changePWDEditTxt);
        changePWDAgainEditTxt = rootView.findViewById(R.id.changePWDAgainEditTxt);

    }


    private void initViewsFeature() {
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VFREdgeCache.getInstance().setIpAddress(ipAddressEditTxt.getText().toString());
                VFREdgeCache.getInstance().setPort(portEditTxt.getText().toString());
                VFREdgeCache.getInstance().setUserAccount(userAccountEditTxt.getText().toString());
                VFREdgeCache.getInstance().setUserPwd(userPWDEditTxt.getText().toString());
                VFREdgeCache.getInstance().setTabletID(tabletIDEditTxt.getText().toString());

                VFRThermometerCache.getInstance().setIpAddress(thermalIpEditTxt.getText().toString());
                VFRThermometerCache.getInstance().setAlertTemp(Float.valueOf(alertTempEditTxt.getText().toString()));

                mLog.i(TAG, changePWDEditTxt.getText().toString());
                mLog.i(TAG, changePWDAgainEditTxt.getText().toString());

                Toast.makeText(getContext(), "save Setting Succeed", Toast.LENGTH_SHORT).show();
                onFragmentInteractionListener.clickConfirm();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickBackToDetectPage();
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
            lazyLoad();
        } else {
        }
    }

    // -------------- Lazy Load --------------
    private void lazyLoad() {
        mLog.d(TAG, "lazyLoad(), getUserVisibleHint()= " + getUserVisibleHint());
        if (getUserVisibleHint()) {
            ipAddressEditTxt.setText(VFREdgeCache.getInstance().getIpAddress());
            portEditTxt.setText(VFREdgeCache.getInstance().getPort());
            userAccountEditTxt.setText(VFREdgeCache.getInstance().getUserAccount());
            userPWDEditTxt.setText(VFREdgeCache.getInstance().getUserPwd());
            tabletIDEditTxt.setText(VFREdgeCache.getInstance().getTabletID());

            thermalIpEditTxt.setText(VFRThermometerCache.getInstance().getIpAddress());
            alertTempEditTxt.setText(String.valueOf(VFRThermometerCache.getInstance().getAlertTemp()));
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
        void clickBackToDetectPage();

        void clickConfirm();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

}
