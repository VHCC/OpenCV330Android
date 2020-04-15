package acl.siot.opencvwpc20191007noc.page.upload;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.api.updateImage.UpdateImage;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.page.detect.DetectFragment.faceCacheArray;
import static acl.siot.opencvwpc20191007noc.page.welcome.WelcomeFragment.TARGET_USER;
import static acl.siot.opencvwpc20191007noc.page.welcome.WelcomeFragment.userName;

/**
 * Created by IChen.Chu on 2019/12/06
 * A fragment to show welcome page.
 */
public class UploadFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private TextView appVersion;
    private TextView uploadtxt;
    private ImageButton retryBtn;
    private ImageButton confirmBtn;

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public UploadFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static UploadFragment newInstance() {
        UploadFragment fragment = new UploadFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_upload, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        uploadtxt = rootView.findViewById(R.id.uploadtxt);
        retryBtn = rootView.findViewById(R.id.retryBtn);
        confirmBtn = rootView.findViewById(R.id.confirmBtn);

        img1 = rootView.findViewById(R.id.img1);
        img2 = rootView.findViewById(R.id.img2);
        img3 = rootView.findViewById(R.id.img3);

    }


    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickRetry();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBtn.setClickable(false);
                mLog.d(TAG, " * faceSelected= " + faceSelected);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                faceCacheArray.get(faceSelected).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
//                HashMap<String, String> mMap = new UpdateImage("5de8a9b11cce9e1a10b14391", encoded);
                HashMap<String, String> mMap = new UpdateImage(TARGET_USER, encoded);
                writeToFile(encoded);
                try {
                    OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceSelected = 0;
                mLog.d(TAG, " * faceSelected= " + faceSelected);
                img1.setSelected(true);
                img2.setSelected(false);
                img3.setSelected(false);
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceSelected = 1;
                mLog.d(TAG, " * faceSelected= " + faceSelected);
                img1.setSelected(false);
                img2.setSelected(true);
                img3.setSelected(false);
            }
        });

        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceSelected = 2;
                mLog.d(TAG, " * faceSelected= " + faceSelected);
                img1.setSelected(false);
                img2.setSelected(false);
                img3.setSelected(true);
            }
        });
    }

    public static int faceSelected = 0;

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
            img1.setImageBitmap(faceCacheArray.get(0));
            img2.setImageBitmap(faceCacheArray.get(1));
            img3.setImageBitmap(faceCacheArray.get(2));

            uploadtxt.setText("Hi, " + userName + ", Please Select One Photo to Upload");
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

    public void onEventMainThread(BusEvent event){
        switch (event.getEventType()) {
            case APP_CODE_UPDATE_IMAGE_SUCCESS:
                confirmBtn.setClickable(true);
                mLog.d(TAG, " * upload face Success!");
                onFragmentInteractionListener.uploadImageFinish();
                break;
        }
    }

    // -------------------------------------------
    public interface OnFragmentInteractionListener {
        void clickRetry();

        void uploadImageFinish();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

    public void writeToFile(String data) {
        // Get the directory for the user's public pictures directory.
        final File path = Environment.getExternalStoragePublicDirectory(
                //Environment.DIRECTORY_PICTURES
                Environment.DIRECTORY_DOWNLOADS);

        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "log_upload.txt");

        // Save your stream, don't forget to flush() it before closing it.

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
