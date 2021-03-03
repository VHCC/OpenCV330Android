package acl.siot.opencvwpc20191007noc.vfr.adminSetting;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.cache.VFRAppSetting;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;
import acl.siot.opencvwpc20191007noc.thc11001huApi.getTemp.GetTemp;
import acl.siot.opencvwpc20191007noc.util.MLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static acl.siot.opencvwpc20191007noc.App.FRS_SERVER_CONNECT_TRY;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP;

/**
 * Created by IChen.Chu on 2020/05/25
 * A fragment to show admin setting password page.
 */
public class VFRAdminSettingFragment extends Fragment {

    private static final MLog mLog = new MLog(false);
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
    private EditText matchScoreEditTxt;

    private EditText thermalIpEditTxt;
    private EditText alertTempEditTxt;
    private EditText httpCMDEditTxt;

    private EditText changePWDEditTxt;
    private EditText changePWDAgainEditTxt;

    private TextView kkkk;

    private ImageView standard_mode;
    private ImageView thermo_mode;

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
        matchScoreEditTxt = rootView.findViewById(R.id.matchScoreEditTxt);

        thermalIpEditTxt = rootView.findViewById(R.id.thermalIpEditTxt);
        alertTempEditTxt = rootView.findViewById(R.id.alertTempEditTxt);
        httpCMDEditTxt = rootView.findViewById(R.id.httpCMDEditTxt);

        changePWDEditTxt = rootView.findViewById(R.id.changePWDEditTxt);
        changePWDAgainEditTxt = rootView.findViewById(R.id.changePWDAgainEditTxt);

        kkkk = rootView.findViewById(R.id.kkkk);

        standard_mode = rootView.findViewById(R.id.standard_mode);
        thermo_mode = rootView.findViewById(R.id.thermo_mode);

    }


    private void initViewsFeature() {
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VFREdgeCache.getInstance().setIpAddress(ipAddressEditTxt.getText().toString());
                VFREdgeCache.getInstance().setPort(portEditTxt.getText().toString());
                VFREdgeCache.getInstance().setUserAccount(userAccountEditTxt.getText().toString());
                mLog.i(TAG, userPWDEditTxt.getText().toString());
                VFREdgeCache.getInstance().setUserPwd(userPWDEditTxt.getText().toString());
                VFREdgeCache.getInstance().setTabletID(tabletIDEditTxt.getText().toString());
                VFREdgeCache.getInstance().setMatchScore(matchScoreEditTxt.getText().toString());

                VFRThermometerCache.getInstance().setIpAddress(thermalIpEditTxt.getText().toString());
                VFRThermometerCache.getInstance().setAlertTemp(Float.valueOf(alertTempEditTxt.getText().toString()));

                if (!changePWDEditTxt.getText().toString().isEmpty() && !changePWDAgainEditTxt.getText().toString().isEmpty()) {
                    if ( changePWDEditTxt.getText() != null && changePWDAgainEditTxt.getText() != null ) {
                        if (changePWDEditTxt.getText().toString().isEmpty() || changePWDAgainEditTxt.getText().toString().isEmpty()) {
                            Toast.makeText(getContext(), "App setting password must not be empty.", Toast.LENGTH_SHORT).show();
                            return;
                        }
//                    mLog.i(TAG, changePWDEditTxt.getText().toString());
//                    mLog.i(TAG, changePWDAgainEditTxt.getText().toString());
                        if (!changePWDEditTxt.getText().toString().equals(changePWDAgainEditTxt.getText().toString())) {
                            Toast.makeText(getContext(), "App setting password are not consistent in main and retype.", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            VFRAppSetting.getInstance().setPwd(changePWDEditTxt.getText().toString());
                        }
                    }
                }


                Toast.makeText(getContext(), "save Setting Succeed", Toast.LENGTH_SHORT).show();
                AppBus.getInstance().post(new BusEvent("try connect FRS Server", FRS_SERVER_CONNECT_TRY));

                HashMap<String, String> mMap = new GetTemp();
                try {
                    OKHttpAgent.getInstance().getRequest(mMap, APP_CODE_THC_1101_HU_GET_TEMP);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                onFragmentInteractionListener.clickConfirm();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickBackToDetectPage();
            }
        });

        kkkk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickLanguagePage();
            }
        });

        standard_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VFREdgeCache.getInstance().setImageStandardMode(true);
                standard_mode.setBackgroundColor(VFREdgeCache.getInstance().isImageStandardMode() ? Color.parseColor("#19b3cc") : Color.parseColor("#00000000"));
                thermo_mode.setBackgroundColor(!VFREdgeCache.getInstance().isImageStandardMode() ? Color.parseColor("#19b3cc") : Color.parseColor("#00000000"));
            }
        });

        thermo_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VFREdgeCache.getInstance().setImageStandardMode(false);
                standard_mode.setBackgroundColor(VFREdgeCache.getInstance().isImageStandardMode() ? Color.parseColor("#19b3cc") : Color.parseColor("#00000000"));
                thermo_mode.setBackgroundColor(!VFREdgeCache.getInstance().isImageStandardMode() ? Color.parseColor("#19b3cc") : Color.parseColor("#00000000"));
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
//            mLog.i(TAG, VFREdgeCache.getInstance().getUserPwd());
            userPWDEditTxt.setText(VFREdgeCache.getInstance().getUserPwd());
            tabletIDEditTxt.setText(VFREdgeCache.getInstance().getTabletID());
            matchScoreEditTxt.setText(VFREdgeCache.getInstance().getMatchScore());

            thermalIpEditTxt.setText(VFRThermometerCache.getInstance().getIpAddress());
            alertTempEditTxt.setText(String.valueOf(VFRThermometerCache.getInstance().getAlertTemp()));

            standard_mode.setBackgroundColor(VFREdgeCache.getInstance().isImageStandardMode() ? Color.parseColor("#19b3cc") : Color.parseColor("#00000000"));
            thermo_mode.setBackgroundColor(!VFREdgeCache.getInstance().isImageStandardMode() ? Color.parseColor("#19b3cc") : Color.parseColor("#00000000"));
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

        void clickLanguagePage();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

}
