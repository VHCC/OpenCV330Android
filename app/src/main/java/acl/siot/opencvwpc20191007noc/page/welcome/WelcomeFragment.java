package acl.siot.opencvwpc20191007noc.page.welcome;

import android.app.Application;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.api.getUser.GetUser;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_EVENT_QRCODE_ID_RESET;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER_SUCCESS;

/**
 * Created by IChen.Chu on 2018/9/25
 * A fragment to show welcome page.
 */
public class WelcomeFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private TextView appVersion;
    private ImageButton scanBadgeBtn;
    private EditText qrcodeID;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public WelcomeFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static WelcomeFragment newInstance() {
        WelcomeFragment fragment = new WelcomeFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        scanBadgeBtn = rootView.findViewById(R.id.scanBadgeBtn);
        qrcodeID = rootView.findViewById(R.id.qrcodeID);

    }

    boolean isStartCollectData = false;
    String resultString = "";

    public static String TARGET_USER = "";

    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());
        scanBadgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrcodeID.requestFocus();
//                onFragmentInteractionListener.clickToDetectPage();
            }
        });

//        qrcodeID.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if (!isStartCollectData) {
//                    resultString = "";
//                }
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                mLog.d(TAG, " * onTextChanged= " + s);
//                if (isStartCollectData) {
//                    resultString += s.charAt(start);
//                }
//
//                if (String.valueOf(s).indexOf("\\000026") > -1) {
//                    isStartCollectData = true;
//                }
//
//                if (resultString.length() == 25) {
//                    isStartCollectData = false;
//                    mLog.d(TAG, " * resultString= " + resultString);
//
//                    resultString.replace("\n", "");
//
//                    HashMap<String, String> mMap = new GetUser(resultString.replace("\n", ""));
//                    try {
//                        OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_GET_USER);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    qrcodeID.setText("");
//                }
//                mLog.d(TAG, " * isStartCollectData= " + isStartCollectData);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                mLog.d(TAG, " * afterTextChanged= " + s.toString());
//                if (!isStartCollectData) {
//                    resultString = "";
//                }
//
//            }
//        });

        qrcodeID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                mLog.d(TAG, " * beforeTextChanged= " + s);
                if (!isStartCollectData) {
                    resultString = "";
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLog.d(TAG, " * onTextChanged= " + s);
//                mLog.d(TAG, " * s= " + s.charAt(start));
//                if (String.valueOf(s.charAt(start)).equals("{")) {
//                    isStartCollectData = true;
//                }
//
//                if (isStartCollectData) {
//                    resultString += s.charAt(start);
//                }
//
//                if (String.valueOf(s.charAt(start)).equals("}")) {
//                    isStartCollectData = false;
//                    try {
//                        jsonObject = new JSONObject(resultString);
//                        mLog.d(TAG, " * jsonObject= " + jsonObject.toString(4));
//
//                        HashMap<String, String> mMap = new GetUser("5de8a9b11cce9e1a10b14391");
//                        try {
//                            OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_GET_USER);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }

                if (s.length() > 0) {
                    isStartCollectData = true;
                }

                if (isStartCollectData) {
                    resultString += s.charAt(start);
                    mLog.d(TAG, " * resultString length= " + resultString.length());
                }

//                mLog.d(TAG, " * indexOf= " + String.valueOf(s).indexOf("\\000026"));
//                if (String.valueOf(s).indexOf("\\000026") > -1) {
//                    isStartCollectData = true;
//                }

//                if (resultString.length() == 25) {
                if (resultString.length() == 25) {
                    isStartCollectData = false;
                    mLog.d(TAG, " * resultString= " + resultString);

                    HashMap<String, String> mMap = new GetUser(resultString.replace("\n", ""));
                    try {
                        OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_GET_USER);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    qrcodeID.setText("");
                }
                mLog.d(TAG, " * isStartCollectData= " + isStartCollectData);
            }

            @Override
            public void afterTextChanged(Editable s) {
                mLog.d(TAG, " * afterTextChanged= " + s.toString());
//                mLog.d(TAG, " *** resultString= " + resultString.toString());
                if (!isStartCollectData) {
                    resultString = "";
                }

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
            qrcodeID.requestFocus();
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
        qrcodeID.requestFocus();
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
        void clickToDetectPage();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

    // Event Bus
    public void onEventMainThread(BusEvent event){
        switch (event.getEventType()) {
            case APP_CODE_GET_USER_SUCCESS:
                mLog.d(TAG, " * get user Success!");
//                mLog.d(TAG, " * event= " + event.getMessage());
                try {
                    JSONObject jsonObject = new JSONObject(event.getMessage());
                    mLog.d(TAG, " * jsonObject= " + jsonObject.toString(4));

                    JSONObject user = jsonObject.getJSONObject("user");

                    userName = user.getString("firstname") + " " + user.getString("lastname");

                    String id = user.getString("id");
                    TARGET_USER = id;
                    onFragmentInteractionListener.clickToDetectPage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_GET_USER_FAIL:
                break;
            case APP_CODE_EVENT_QRCODE_ID_RESET:
                isStartCollectData = false;
                resultString = "";
                qrcodeID.setText("");
                break;

        }
    }

    public static String userName = "";

}
