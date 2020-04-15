package acl.siot.opencvwpc20191007noc.page.result;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;

import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static acl.siot.opencvwpc20191007noc.page.detect.DetectFragment.faceCacheArray;
import static acl.siot.opencvwpc20191007noc.page.upload.UploadFragment.faceSelected;
import static acl.siot.opencvwpc20191007noc.page.welcome.WelcomeFragment.userName;

/**
 * Created by IChen.Chu on 2019/12/10
 * A fragment to show welcome page.
 */
public class ResultFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private TextView appVersion;
    private ImageButton finishedBtn;
    private ImageView resultImage;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public ResultFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static ResultFragment newInstance() {
        ResultFragment fragment = new ResultFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        finishedBtn = rootView.findViewById(R.id.scanBadgeBtn);
        resultImage = rootView.findViewById(R.id.resultImage);
    }

    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());
        finishedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickFinished();
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
            mLog.d(TAG, " * faceSelected= " + faceSelected + ", faceCacheArray= " + faceCacheArray.size());
            resultImage.setImageBitmap(faceCacheArray.get(faceSelected));
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
        void clickFinished();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

}
