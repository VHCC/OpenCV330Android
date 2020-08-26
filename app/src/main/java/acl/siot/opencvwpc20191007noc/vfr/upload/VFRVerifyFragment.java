package acl.siot.opencvwpc20191007noc.vfr.upload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;

import org.ejml.data.DenseMatrix64F;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;
import acl.siot.opencvwpc20191007noc.frsApi.getFaceImage.FrsGetFaceImage;
import acl.siot.opencvwpc20191007noc.frsApi.modifyPersonInfo.FrsModifyPersonInfo;
import acl.siot.opencvwpc20191007noc.frsApi.verify.FrsVerify;
import acl.siot.opencvwpc20191007noc.objectDetect.AnchorUtil;
import acl.siot.opencvwpc20191007noc.objectDetect.Classifier;
import acl.siot.opencvwpc20191007noc.objectDetect.ImageUtils;
import acl.siot.opencvwpc20191007noc.objectDetect.ObjectDetectInfo;
import acl.siot.opencvwpc20191007noc.objectDetect.TLiteObjectDetectionAPI;
import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import static acl.siot.opencvwpc20191007noc.App.isFRServerConnected;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_GET_FACE_ORIGINAL_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY_UN_RECOGNIZED;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.vfr.detect.VFRDetectFragment.person_temp_static;
import static acl.siot.opencvwpc20191007noc.vfr.detect.VFRDetectFragment.vfrFaceCacheArray;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.staticPersonsArray;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.staticPersonsEmployeeNoArray;
import static acl.siot.opencvwpc20191007noc.wbSocket.FrsWebSocketClient.staticPersonID;
import static acl.siot.opencvwpc20191007noc.wbSocket.FrsWebSocketClient.staticPersonInfo;

/**
 * Created by IChen.Chu on 2020/05/13
 * A fragment to show welcome page.
 */
public class VFRVerifyFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Constants

    // View
    private TextView appVersion;
    private Button retryBtn;
    private Button verifyBtn;

    private TextView recognizedTime;
    private TextView personRole;
    private TextView personName;
    private TextView personNameReal;
    private TextView personTemperature;

    private ImageView img1;
    private ImageView imgOrigin;
    private ImageView faceStatus;
    private ImageView thermoStatus;

    private FrameLayout verifyBg;
//    private ImageView img2;
//    private ImageView img3;

    public static boolean staticVerifySwitch = false;

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Fields

    // Constructor
    public VFRVerifyFragment() {
    }



    public static DenseMatrix64F[] anchors;

    private Classifier detector;

    private static final int TF_OD_API_INPUT_SIZE = 260;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "face_mask_detection.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/face_mask_detection.txt";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WelcomeFragment.
     */
    public static VFRVerifyFragment newInstance() {
        VFRVerifyFragment fragment = new VFRVerifyFragment();
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

        anchors = AnchorUtil.getInstance().generateAnchors();

        int cropSize = TF_OD_API_INPUT_SIZE;
        try {
            detector =
                    TLiteObjectDetectionAPI.create(
                            getContext().getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            mLog.e(TAG, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.vfr_fragment_verify, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
        appVersion = rootView.findViewById(R.id.appVersion);
        retryBtn = rootView.findViewById(R.id.retryBtn);
        verifyBtn = rootView.findViewById(R.id.verifyBtn);

        recognizedTime = rootView.findViewById(R.id.recognizedTime);
        personRole = rootView.findViewById(R.id.personRole);
        personName = rootView.findViewById(R.id.personName);
        personNameReal = rootView.findViewById(R.id.personNameReal);
        personTemperature = rootView.findViewById(R.id.personTemperature);



        img1 = rootView.findViewById(R.id.img1);
        imgOrigin = rootView.findViewById(R.id.imgOrigin);

        verifyBg = rootView.findViewById(R.id.verifyBg);
//        img2 = rootView.findViewById(R.id.img2);
//        img3 = rootView.findViewById(R.id.img3);
        faceStatus = rootView.findViewById(R.id.faceStatus);
        thermoStatus = rootView.findViewById(R.id.thermoStatus);

    }


    private void initViewsFeature() {
        appVersion.setText("v " + AppUtils.getAppVersionName());
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickRetry();
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLog.d(TAG, " * vfrFaceSelected= " + vfrFaceSelected);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                vfrFaceCacheArray.get(vfrFaceSelected).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);
//                HashMap<String, String> mMap = new UpdateImage("5de8a9b11cce9e1a10b14391", encoded);
                FrsVerify mMap = new FrsVerify(encoded);
                writeToFile(encoded);
                try {
                    OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        verifyBtn.setVisibility(View.INVISIBLE);


        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vfrFaceSelected = 0;
                mLog.d(TAG, " * faceSelected= " + vfrFaceSelected);
                img1.setSelected(true);
//                img2.setSelected(false);
//                img3.setSelected(false);
            }
        });

//        img2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                vfrFaceSelected = 1;
//                mLog.d(TAG, " * faceSelected= " + vfrFaceSelected);
//                img1.setSelected(false);
//                img2.setSelected(true);
//                img3.setSelected(false);
//            }
//        });
//
//        img3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                vfrFaceSelected = 2;
//                mLog.d(TAG, " * faceSelected= " + vfrFaceSelected);
//                img1.setSelected(false);
//                img2.setSelected(false);
//                img3.setSelected(true);
//            }
//        });

        personTemperature.setText(df.format(person_temp_static));
        if (!isThermometerServerConnected) {
            personTemperature.setText("N/A");
        }
    }

    public static int vfrFaceSelected = 0;

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
        if (getUserVisibleHint() && vfrFaceCacheArray.size() > 0) {
            img1.setImageBitmap(vfrFaceCacheArray.get(0));

            Matrix frameToCropTransform;
            Matrix cropToFrameTransform;

            Bitmap croppedBitmap = Bitmap.createBitmap(260, 260, Bitmap.Config.ARGB_8888);

            frameToCropTransform =
                    ImageUtils.getTransformationMatrix(
                            vfrFaceCacheArray.get(0).getWidth(), vfrFaceCacheArray.get(0).getHeight(),
                            260, 260,
                            0, false);

//        frameToCropTransform =
//                ImageUtils.getTransformationMatrix(
//                        1080, 720,
//                        260, 260,
//                        0, false);

            cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);

            final Canvas canvas = new Canvas(croppedBitmap);
            canvas.drawBitmap(vfrFaceCacheArray.get(0), frameToCropTransform, null);

            ObjectDetectInfo results = detector.recognizeObject(croppedBitmap);
            if (results != null) {
                switch(results.getClasses()) {
                    case 1: // NoMask
//                        Imgproc.rectangle(mRgba, new Point(results.getX_min() * width - offset, results.getY_min() * height - offset),
//                                new Point(results.getX_max() * width + offset, results.getY_max() * height + offset), faceRectColor_red, 3);
//                        Imgproc.putText(mRgba, "No Mask " + String.format("(%.1f%%) ", results.getConfidence() * 100.0f) , new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(255,0,0), 2);
                        Toast.makeText(getContext(), "No Mask", Toast.LENGTH_LONG).show();
                        break;
                    case 0: // Mask
//                        Imgproc.rectangle(mRgba, new Point(results.getX_min() * width + offset, results.getY_min() * height + offset),
//                                new Point(results.getX_max() * width + offset, results.getY_max() * height + offset), faceRectColor_green, 3);
//                        Imgproc.putText(mRgba, "Mask " + String.format("(%.1f%%) ", results.getConfidence() * 100.0f), new Point(results.getX_min() * width, results.getY_min() * height), Core.TYPE_MARKER, 5.0, new Scalar(0,255,0), 2);
                        Toast.makeText(getContext(), "Mask On", Toast.LENGTH_LONG).show();
                        break;
                }
                mLog.i(TAG, "mask result= " + results.toString());
            }




//            img2.setImageBitmap(vfrFaceCacheArray.get(1));
//            img3.setImageBitmap(vfrFaceCacheArray.get(2));
//            verifySwitch = true;
//            verifyBtn.performClick();
            imgOrigin.setPadding(0,0,0,0);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(10, 10, 10, 10);
            imgOrigin.setLayoutParams(lp);
            imgOrigin.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.vfr_no_register_face));
            Date d = new Date();
            CharSequence s = android.text.format.DateFormat.format("yyyy/MM/dd hh:mm:ss",d.getTime());
            recognizedTime.setText(s);

            if (person_temp_static > VFRThermometerCache.getInstance().getAlertTemp()) {
                MediaPlayer mPlayer = MediaPlayer.create(getContext(), R.raw.alarm20200819);
                mPlayer.start();
                mLog.d(TAG, "WARN, person_temp_static= " + df.format(person_temp_static));
                if(android.os.Build.VERSION.SDK_INT >= 21){
                    verifyBg.setBackground(getContext().getDrawable(R.drawable.vfr_finished_ng_bg));
                } else {
                    verifyBg.setBackground(getResources().getDrawable(R.drawable.vfr_finished_ng_bg));
                }

            } else {
                mLog.d(TAG, "SAFE, person_temp_static= " + df.format(person_temp_static));
                if(android.os.Build.VERSION.SDK_INT >= 21){
                    verifyBg.setBackground(getContext().getDrawable(R.drawable.vfr_finished_ok_bg));
                } else {
                    verifyBg.setBackground(getResources().getDrawable(R.drawable.vfr_finished_ok_bg));
                }
            }

            personTemperature.setText(df.format(person_temp_static));
            if (!isThermometerServerConnected) {
                personTemperature.setText("N/A");
            }
            if(android.os.Build.VERSION.SDK_INT >= 21){
                faceStatus.setImageDrawable(isFRServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));
                thermoStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.vfr_online) : getContext().getDrawable(R.drawable.vfr_offline));
            } else {
                faceStatus.setImageDrawable(isFRServerConnected ? getResources().getDrawable(R.drawable.vfr_online) : getResources().getDrawable(R.drawable.vfr_offline));
                thermoStatus.setImageDrawable(isThermometerServerConnected ? getResources().getDrawable(R.drawable.vfr_online) : getResources().getDrawable(R.drawable.vfr_offline));
            }


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
//        mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
        switch (event.getEventType()) {
            case APP_CODE_FRS_VERIFY_SUCCESS:
                mLog.d(TAG, " $$$ verify face Success! $$$ ");
                isDetectTemperature = false;
                try {
                    int index = staticPersonsEmployeeNoArray.indexOf(staticPersonInfo.getString("employeeno"));
                    mLog.i(TAG, " --- person index= " + index);
                    JSONObject targetPerson = (JSONObject) staticPersonsArray.get(index);
                    mLog.i(TAG, " --- targetPerson= " + targetPerson);
                    JSONObject person_info = (JSONObject) targetPerson.get("person_info");
                    String face_id = (String) ((JSONArray)targetPerson.getJSONArray("face_id_numbers")).get(0);
                    mLog.i(TAG, " --- face_id= " + face_id);
                    mLog.i(TAG, " --- face_id.length(= " + person_info.getString("fullname").length());

                    personNameReal.setText(person_info.getString("fullname"));
                    personName.setText("Name");
                    personRole.setText("Employee");

                    HashMap<String, String> mMap = new FrsGetFaceImage( face_id);
                    try {
                        OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_GET_FACE_ORIGINAL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_FRS_GET_FACE_ORIGINAL_SUCCESS:
                mLog.d(TAG, " * get face Success!");
                try {
                    JSONObject response = new JSONObject(event.getMessage());
                    String encodedImage = response.getString("image");

                    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgOrigin.setPadding(1,1,1,1);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    lp.setMargins(20, 20, 20, 20);
                    imgOrigin.setLayoutParams(lp);
                    imgOrigin.setImageBitmap(decodedByte);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    staticPersonInfo.put("cardno", df.format(person_temp_static));

                    JSONArray groupList = staticPersonInfo.getJSONArray("group_list");
                    JSONArray department_list = staticPersonInfo.getJSONArray("department_list");

                    HashMap<String, Object> mMap = new FrsModifyPersonInfo(staticPersonID, staticPersonInfo);
                    try {
                        OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_MODIFY_PERSON_INFO);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    isDetectTemperature = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case APP_CODE_FRS_VERIFY_UN_RECOGNIZED:
                personNameReal.setText("");
                personName.setText("Visitor");
                personRole.setText("");
                imgOrigin.setPadding(0,0,0,0);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(10, 10, 10, 10);
                imgOrigin.setLayoutParams(lp);
                imgOrigin.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.vfr_no_register_face));
                break;
            case APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS:
                if (!isFRServerConnected) {
                    personNameReal.setText("");
                    personName.setText("Visitor");
                    personRole.setText("");
                }

//                if (!isDetectTemperature) {
//                    try {
//                        String response = event.getMessage();
//                        JSONObject jsonObj = new JSONObject(response);
//                        person_temp = (float) jsonObj.getDouble("Temperature");
////                        mLog.d(TAG, "person_temp= " + person_temp);
////                    mLog.d(TAG, "person_temp= " + df.format(person_temp));
//
//                        if (person_temp > VFRThermometerCache.getInstance().getAlertTemp()) {
//                            mLog.d(TAG, "WARN, person_temp= " + df.format(person_temp));
//                            if(android.os.Build.VERSION.SDK_INT >= 21){
//                                verifyBg.setBackground(getContext().getDrawable(R.drawable.vfr_finished_ng_bg));
//                            } else {
//                                verifyBg.setBackground(getResources().getDrawable(R.drawable.vfr_finished_ng_bg));
//                            }
//
//                        } else {
//                            mLog.d(TAG, "SAFE, person_temp= " + df.format(person_temp));
//                            if(android.os.Build.VERSION.SDK_INT >= 21){
//                                verifyBg.setBackground(getContext().getDrawable(R.drawable.vfr_finished_ok_bg));
//                            } else {
//                                verifyBg.setBackground(getResources().getDrawable(R.drawable.vfr_finished_ok_bg));
//                            }
//
//                        }
//
//                        personTemperature.setText(df.format(person_temp));
//
////                        mLog.d(TAG, "staticPersonInfo= " + staticPersonInfo.toString());
////                        mLog.d(TAG, "cardno= " + staticPersonInfo.getString("cardno"));
//                        staticPersonInfo.put("cardno", df.format(person_temp));
////                        mLog.d(TAG, "staticPersonInfo= " + staticPersonInfo.toString());
////                        mLog.d(TAG, "cardno= " + staticPersonInfo.getString("cardno"));
//
//                        JSONArray groupList = staticPersonInfo.getJSONArray("group_list");
//                        JSONArray department_list = staticPersonInfo.getJSONArray("department_list");
//
////                        mLog.d(TAG, "groupList= " + groupList.toString());
////                        mLog.d(TAG, "groupList length= " + groupList.length());
////                        mLog.d(TAG, "department_list length= " + department_list.length());
//
//                        HashMap<String, Object> mMap = new FrsModifyPersonInfo(staticPersonID, staticPersonInfo);
//                        try {
//                            OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_MODIFY_PERSON_INFO);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        isDetectTemperature = true;
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }

                break;
        }
    }

    boolean isDetectTemperature = false;

    static double person_temp = 0.0f;
    private DecimalFormat df = new DecimalFormat("0.0");

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


    // Thread
    private Thread thermometerThread;
    private Runnable thermometerRunnable = new Runnable() {

        private static final long task_minimum_tick_time_msec = 1000; // 1 second

        @Override
        public void run() {
            long tick_count = 0;
            mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));

            while (true) {
                try {
                    long start_time_tick = System.currentTimeMillis();
                    // real-time task

                    if (tick_count % 2 == 0) {
//                        mLog.d(TAG, " * heartBeat * ");
                    }

                    long end_time_tick = System.currentTimeMillis();

                    if (end_time_tick - start_time_tick > task_minimum_tick_time_msec) {
                        mLog.w(TAG, "Over time process " + (end_time_tick - start_time_tick));
                    } else {
                        Thread.sleep(task_minimum_tick_time_msec);
                    }
                    tick_count++;
                } catch (InterruptedException e) {
                    mLog.d(TAG, "appRunnable interrupted");
                }
            }
        }
    };


}
