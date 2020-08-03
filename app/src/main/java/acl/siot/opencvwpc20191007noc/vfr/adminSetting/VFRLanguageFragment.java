package acl.siot.opencvwpc20191007noc.vfr.adminSetting;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;

import java.util.Locale;

import acl.siot.opencvwpc20191007noc.MainActivity;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.cache.VFRAppSetting;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by IChen.Chu on 2020/05/29
 * A fragment to show admin setting password page.
 */
public class VFRLanguageFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private TextView appVersion;
    private Button confirmBtn;
    private Button backBtn;

    private EditText pwdEditTxt;
    private Spinner spinner;

    private ImageButton changeBtn;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public VFRLanguageFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static VFRLanguageFragment newInstance() {
        VFRLanguageFragment fragment = new VFRLanguageFragment();
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
        View rootView = inflater.inflate(R.layout.vfr_fragment_language, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        confirmBtn = rootView.findViewById(R.id.confirmBtn);
        backBtn = rootView.findViewById(R.id.backBtn);


        pwdEditTxt = rootView.findViewById(R.id.pwdEditTxt);
        spinner = rootView.findViewById(R.id.spinner);
        changeBtn = rootView.findViewById(R.id.changeBtn);

    }

    private static final String[] paths = {"item 1", "item 2", "item 3"};

    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VFRAppSetting.getInstance().getPwd().equals(pwdEditTxt.getText().toString())) {
                    onFragmentInteractionListener.clickConfirmPWD();
                } else {
                    Toast.makeText(getContext(), "App Setting Password is not correct.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickBackToDetectPage();
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.vfr_spinner_item, paths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLog.d(TAG, "QQQQQQ");
//                LocaleHelper.setLocale(getActivity(), mLanguageCode);
//                Resources res = getContext().getResources();
//// Change locale settings in the app.
//                DisplayMetrics dm = res.getDisplayMetrics();
//                Configuration conf = res.getConfiguration();
//                conf.setLocale(new Locale("zh_TW".toLowerCase())); // API 17+ only.
//// Use conf.locale = new Locale(...) if targeting lower versions
//                res.updateConfiguration(conf, dm);

                Locale locale = new Locale("en");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getContext().getResources().updateConfiguration(config,
                        getActivity().getResources().getDisplayMetrics());

                //It is required to recreate the activity to reflect the change in UI.
//                getActivity().recreate();
            }
        });
    }

    private String mLanguageCode = "zh_rTW";

    int languageType = 0;

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    languageType = 0;
                    // Whatever you want to happen when the first item gets selected
                    break;
                case 1:
                    languageType = 1;
                    // Whatever you want to happen when the second item gets selected
                    break;
                case 2:
                    languageType = 2;
                    // Whatever you want to happen when the thrid item gets selected
                    break;

            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

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

        void clickConfirmPWD();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

}
