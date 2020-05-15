package acl.siot.opencvwpc20191007noc.vfr.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.frsApi.login.FrsLogin;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.wbSocket.FrsWebSocketClient;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static acl.siot.opencvwpc20191007noc.api.URLConstants.FRS_SERVER_URL;
import static acl.siot.opencvwpc20191007noc.api.URLConstants.FRS_WEB_SOCKET_URL;

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

        FrsWebSocketClient c = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        try {
            c = new FrsWebSocketClient( new URI( "ws://" + FRS_WEB_SOCKET_URL + ":80/fcsrecognizedresult" ));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        c.connect();

        FrsWebSocketClient c_un = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        try {
            c_un = new FrsWebSocketClient( new URI( "ws://" + FRS_WEB_SOCKET_URL + ":80/fcsnonrecognizedresult" ));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        c_un.connect();

        HashMap<String, String> mMap = new FrsLogin("ichen", "123456");
        try {
            OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_LOGIN);
//            OKHttpAgent.getInstance().getFRSRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vfr_fragment_home, container, false);
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
