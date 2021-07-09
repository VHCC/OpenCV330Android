package acl.siot.opencvwpc20191007noc.vfr.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.AppUtils;
import com.chaos.view.PinView;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskSync;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_STATUS_INACTIVE_SUCCESS;

/**
 * Created by IChen.Chu on 2020/05/13
 * A fragment to show home page.
 */
public class VFRHomeFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants
    private static final String ARG_HOME_DELAY = "arg_home_delay";
    private static final long ARG_HOME_DELAY_2000_MS = 2000L;

    // Handler
    private Handler mHandler = new MainHandler();
    private Runnable mFragmentRunnable = new FragmentRunnable();

    // Listener
    private OnHomeFragmentInteractionListener mHomeFragmentListener;

    // Fields
    private long mDelay = ARG_HOME_DELAY_2000_MS;

    public static JSONArray staticPersonsArray;
    public static ArrayList<String> staticPersonsEmployeeNoArray = new ArrayList<>();
    public static Boolean isGetStaticPersonsEmployeeNoArray = false;

    // View
    TextView appVersion;

    public VFRHomeFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param delay for fragment change (ms).
     * @return A new fragment instance of HomeFragment.
     */
    public static VFRHomeFragment newInstance(long delay) {
        VFRHomeFragment fragment = new VFRHomeFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_HOME_DELAY, (delay > 01L) ? delay : ARG_HOME_DELAY_2000_MS);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDelay = getArguments().getLong(ARG_HOME_DELAY, ARG_HOME_DELAY_2000_MS);
        }
        mLog.d(TAG, VMSEdgeCache.getInstance().showInfoAll());
        // register event Bus
        AppBus.getInstance().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vfr_fragment_home, container, false);
        appVersion = rootView.findViewById(R.id.appVersion);
        initViewsFeature();
        return rootView;
    }

    private void initViewsFeature() {
        appVersion.setText("v" + AppUtils.getAppVersionName());
    }

    @Override
    public void onResume() {
        // register event Bus
//        AppBus.getInstance().register(this);
        AppBus.getInstance().post(new BusEvent("sync vms Data", APP_CODE_VMS_KIOSK_DEVICE_SYNC));
        super.onResume();
//        mHandler.postDelayed(mFragmentRunnable, mDelay);
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mFragmentRunnable);
//        AppBus.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        mLog.d(TAG, " * onDestroy");
        AppBus.getInstance().unregister(this);
        super.onDestroy();
    }

    // -------------------------------------------
    public interface OnHomeFragmentInteractionListener {
        void onShowEnd();
    }

    public void setHomeFragmentListener(OnHomeFragmentInteractionListener listener) {
        mHomeFragmentListener = listener;
    }

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
            if (mHomeFragmentListener != null) {
                syncDialog.hide();
                mHomeFragmentListener.onShowEnd();
            }
        }
    }

    private SweetAlertDialog syncDialog;

    public void onEventMainThread(BusEvent event) {
        switch (event.getEventType()) {
            case APP_CODE_VMS_KIOSK_DEVICE_SYNC:
                mLog.d(TAG, "APP_CODE_VMS_KIOSK_DEVICE_SYNC");
                syncDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE)
                        .setTitleText("Loading...")
                        .setContentText("Sync VMS Data!");
                syncDialog.setCancelable(true);
                syncDialog.show();
                syncDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        mHandler.postDelayed(mFragmentRunnable, 100L);
                    }
                });

                VmsKioskSync mMap = new VmsKioskSync(VMSEdgeCache.getInstance().getVmsKioskUuid());
                try {
                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_SYNC);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_SYNC_SUCCESS:
                if (atDialog_kiosk_inactive != null && atDialog_kiosk_inactive.isShowing()) {
                    atDialog_kiosk_inactive.dismiss();
                }
                mHandler.postDelayed(mFragmentRunnable, mDelay);
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL:
                VMSEdgeCache.getInstance().setVms_kiosk_mode(0);
                syncDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                syncDialog.setTitleText("Warn");
                syncDialog.setContentText("Device isn't connect to VMS yet");
                syncDialog.setConfirmText("OK");
                syncDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        mHandler.postDelayed(mFragmentRunnable, 0);
                    }
                });
                break;
            case APP_CODE_VMS_KIOSK_STATUS_INACTIVE_SUCCESS:
                mLog.d(TAG, "APP_CODE_VMS_KIOSK_STATUS_INACTIVE_SUCCESS");
                if (syncDialog != null && syncDialog.isShowing()) {
                    syncDialog.dismiss();
                }
                if (atDialog_kiosk_inactive != null && atDialog_kiosk_inactive.isShowing()) {
                    return;
                }

                LayoutInflater inflater_change_connect = getLayoutInflater();
                myView_alertDialog = inflater_change_connect.inflate(R.layout.dialog_kiosk_inactive_content, null);

                builder = new AlertDialog.Builder(getContext());
                builder.setView(myView_alertDialog);
                atDialog_kiosk_inactive = builder.create();
                atDialog_kiosk_inactive.setCancelable(false);
                atDialog_kiosk_inactive.show();
                break;
        }
    }

    private AlertDialog atDialog_kiosk_inactive;
    AlertDialog.Builder builder;
    View myView_alertDialog;
}
