package acl.siot.opencvwpc20191007noc.vfr.adminSetting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by IChen.Chu on 2020/05/13
 * A fragment to show admin setting page.
 */
public class VFRAdminSettingFragment extends Fragment {

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

    public VFRAdminSettingFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param delay for fragment change (ms).
     * @return A new fragment instance of HomeFragment.
     */
    public static VFRAdminSettingFragment newInstance(long delay) {
        VFRAdminSettingFragment fragment = new VFRAdminSettingFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.postDelayed(mFragmentRunnable, mDelay);
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mFragmentRunnable);
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
                mHomeFragmentListener.onShowEnd();
            }
        }
    }
}
