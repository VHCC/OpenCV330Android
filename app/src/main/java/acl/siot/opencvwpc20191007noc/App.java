/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package acl.siot.opencvwpc20191007noc;

import android.app.ActivityManager;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Build;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.cache.VFRAppSetting;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;
import acl.siot.opencvwpc20191007noc.frsApi.login.FrsLogin;
import acl.siot.opencvwpc20191007noc.thc11001huApi.getTemp.GetTemp;
import acl.siot.opencvwpc20191007noc.theme.AppTheme;
import acl.siot.opencvwpc20191007noc.theme.AppThemeManager;
import acl.siot.opencvwpc20191007noc.util.AppStateTracker;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.NullHostNameVerifier;
import acl.siot.opencvwpc20191007noc.util.NullX509TrustManager;
import acl.siot.opencvwpc20191007noc.util.SystemPropertiesProxy;
import acl.siot.opencvwpc20191007noc.wbSocket.FrsWebSocketClient;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_GET_FACE_ORIGINAL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_GET_FACE_ORIGINAL_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_LOGIN;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_LOGIN_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_MODIFY_PERSON_INFO;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_MODIFY_PERSON_INFO_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.isGetStaticPersonsEmployeeNoArray;


/**
 * Application class.
 */
public class App extends Application {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // 手动装载openCV库文件，以保证手机无需安装OpenCV Manager
    static {
        System.loadLibrary("opencv_java3");
    }

    final int DEVICE_NOT_SUPPORT = 9066;

    // Http Mechanism
    private OnRequestListener mOnRequestListener = new OnRequestListener();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        super.onCreate();
        mLog.i(TAG, "========== app start ==========");
        // HTTP Mechanism
        OKHttpAgent.getInstance().setRequestListener(mOnRequestListener);

        // register event Bus
        AppBus.getInstance().register(this);
//        mLog.d(TAG, LocaleList.getDefault().toLanguageTags());

//        LocaleUtils.setLocale(new Locale("zh", "TW"));
//        LocaleUtils.updateConfig(this, getBaseContext().getResources().getConfiguration());
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = new Locale("zh", "TW");
        getBaseContext().getResources()
                .updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        VFREdgeCache.getInstance().newInstance(this);
        VFRThermometerCache.getInstance().newInstance(this);
        VFRAppSetting.getInstance().newInstance(this);

        trustHost();

        // Task Schedule handle thread
        appThread = new Thread(appRunnable);
        appThread.start();

        setupTheme();
        setupAppStateTracking();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLog.d(TAG, "onConfigurationChanged");
//        LocaleUtils.updateConfig(this, newConfig);
    }

    private void setupTheme() {
        mLog.d(TAG, "setupTheme");
        AppTheme defaultTheme = new AppTheme(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent));
        AppThemeManager.init(defaultTheme);
    }

    private void setupAppStateTracking() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        AppStateTracker.init(this, AppBus.getInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (activityManager.getAppTasks().size() > 0) {
                AppStateTracker.getInstance().trackTask(activityManager.getAppTasks().get(0).getTaskInfo());
            }
        }
    }

    @Override
    public void onTerminate() {
        mLog.i(TAG, "========== app ShutDown ==========");

        if (appThread.isAlive()) {
            appThread.interrupt();
        }

        super.onTerminate();
    }

    /**
     * Trust all the https host.
     * //TODO it might be dangerous.
     */
    private void trustHost() {
        mLog.i(TAG, "trustHost()");
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        SSLContext context = null;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    // Thread
    private Thread appThread;
    private Runnable appRunnable = new Runnable() {

        private static final long task_minimum_tick_time_msec = 800; // 1 second

        @Override
        public void run() {
            long tick_count = 0;
            mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));

            while (true) {
                try {
                    long start_time_tick = System.currentTimeMillis();
                    // real-time task

                    if (tick_count % 60 == 5) {
//                        mLog.d(TAG, " * heartBeat * ");
                    }

                    if (tick_count % 60 == 6) {
//                        HashMap<String, String> mMap = new ListUser("test");
//                        OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_LIST_USER);
                    }

                    if (tick_count % 60 == 6) {
                        if (!isGetStaticPersonsEmployeeNoArray) {
                            mLog.d(TAG, " *** try connect to FRS Server");
                            AppBus.getInstance().post(new BusEvent("try connect FRS Server", FRS_SERVER_CONNECT_TRY));
                        }
//                        HashMap<String, String> mMap = new GetFace("5de8a9b11cce9e1a10b14391");
//                        OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_GET_FACE);
                    }

                    if (tick_count % 1 == 0) {
                        HashMap<String, String> mMap = new GetTemp();
                        OKHttpAgent.getInstance().getTempRequest(mMap, APP_CODE_THC_1101_HU_GET_TEMP);
                    }

                    if (tick_count % 10 == 0) {
                        mLog.d(TAG, getDeviceModel());
                        // Check Device Model
                        switch (getDeviceModel()) {
                            case "usc_130_160":
                            case "UTC-115G":
                            case "HIT-507":
                            case "HIT-512":
                                break;
                            default:
                                AppBus.getInstance().post(new BusEvent("face detect done", DEVICE_NOT_SUPPORT));
                                break;
                        }
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
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    mLog.d(TAG, "appRunnable interrupted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void onEventMainThread(BusEvent event){
        switch (event.getEventType()) {
            case DEVICE_NOT_SUPPORT:
                Toast.makeText(getApplicationContext(),
                        "this device is not supported.", Toast.LENGTH_LONG).show();
        }
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String staticFRSSessionID;

    static double person_temp = 0.0f;
    private DecimalFormat df = new DecimalFormat("0.00");


    /**
     * Http Mechanism Receiver
     */
    private class OnRequestListener implements OKHttpAgent.IRequestInterface {

        @Override
        public void onRequestSuccess(String response, int requestCode) {
            mLog.d(TAG, "onRequestSuccess(), requestCode= " + requestCode);
            switch (requestCode) {
                case APP_CODE_UPDATE_IMAGE:
                    AppBus.getInstance().post(new BusEvent("hide overlay", APP_CODE_UPDATE_IMAGE_SUCCESS));
                    break;
                case APP_CODE_FRS_LOGIN:
                    try {
                        JSONObject loginResponse = new JSONObject(response);
                        staticFRSSessionID = loginResponse.getString("sessionId");
                        mLog.i(TAG, "getFRS Customer List, sessionId= " + staticFRSSessionID);
                        OKHttpAgent.getInstance().getFRSRequest();
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case APP_CODE_FRS_GET_FACE_ORIGINAL:
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_FRS_GET_FACE_ORIGINAL_SUCCESS));
                    break;
                case APP_CODE_THC_1101_HU_GET_TEMP:
                    isThermometerServerConnected = true;
//                    try {
//                        JSONObject jsonObj = new JSONObject(response);
//                        person_temp = (float) jsonObj.getDouble("Temperature");
////                        mLog.d(TAG, "person_temp= " + person_temp);
//                        mLog.d(TAG, "person_temp= " + df.format(person_temp));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS));
                    break;
                case APP_CODE_FRS_MODIFY_PERSON_INFO:
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_FRS_MODIFY_PERSON_INFO_SUCCESS));
                    break;
            }
        }

        @Override
        public void onRequestFail(String errorResult) {
            mLog.d(TAG, "onRequestFail(), errorResult= " + errorResult);
        }

        @Override
        public void onRequestFail(String errorResult, int requestCode) {
            mLog.d(TAG, "onRequestFail(), errorResult= " + errorResult + ", requestCode= " + requestCode);
            switch (requestCode) {
                case APP_CODE_FRS_LOGIN:
                    break;
                case APP_CODE_THC_1101_HU_GET_TEMP:
                    isThermometerServerConnected = false;
                    break;
            }
        }

    }

    public static boolean isFRServerConnected = true;
    public static boolean isThermometerServerConnected = false;

    public void onEventBackgroundThread(BusEvent event){
//        mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
        switch (event.getEventType()) {
            case FRS_SERVER_CONNECT_TRY:
//                connectFRSServer();
                break;
            case APP_CODE_FRS_LOGIN_SUCCESS:
                mLog.d(TAG, " *** APP_CODE_FRS_LOGIN_SUCCESS *** ");
                break;
        }
    }

    FrsWebSocketClient c;
    FrsWebSocketClient c_un;

    private void connectFRSServer () {
        mLog.d(TAG, " * connectFRSServer");
        if (c != null) {
            c.close();
        }
        c = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        try {
//            c = new FrsWebSocketClient( new URI( "ws://" + FRS_WEB_SOCKET_URL + ":80/fcsrecognizedresult" ));
            c = new FrsWebSocketClient( new URI( "ws://" + VFREdgeCache.getInstance().getIpAddress() + ":" + VFREdgeCache.getInstance().getPort() + "/fcsrecognizedresult" ));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        c.connect();

        if (c_un != null) {
            c_un.close();
        }
        c_un = null; // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
        try {
//            c_un = new FrsWebSocketClient( new URI( "ws://" + FRS_WEB_SOCKET_URL + ":80/fcsnonrecognizedresult" ));
            c_un = new FrsWebSocketClient( new URI( "ws://" + VFREdgeCache.getInstance().getIpAddress() + ":" + VFREdgeCache.getInstance().getPort() + "/fcsnonrecognizedresult" ));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        c_un.connect();

//        HashMap<String, String> mMap = new FrsLogin("ichen", "123456");
        mLog.i(TAG, VFREdgeCache.getInstance().getUserAccount());
        mLog.i(TAG, VFREdgeCache.getInstance().getUserPwd());
        HashMap<String, String> mMap = new FrsLogin(VFREdgeCache.getInstance().getUserAccount(), VFREdgeCache.getInstance().getUserPwd());
        try {
            OKHttpAgent.getInstance().postFRSRequest(mMap, OKHttpConstants.FrsRequestCode.APP_CODE_FRS_LOGIN);
//            OKHttpAgent.getInstance().getFRSRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // handler event
    public static final int FRS_SERVER_CONNECT_TRY = 5001;
}
