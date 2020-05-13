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
import android.os.Build;

import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.api.getFace.GetFace;
import acl.siot.opencvwpc20191007noc.api.listUser.ListUser;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import acl.siot.opencvwpc20191007noc.theme.AppTheme;
import acl.siot.opencvwpc20191007noc.theme.AppThemeManager;
import acl.siot.opencvwpc20191007noc.util.AppStateTracker;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.NullHostNameVerifier;
import acl.siot.opencvwpc20191007noc.util.NullX509TrustManager;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_EVENT_QRCODE_ID_RESET;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_GET_USER_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE_SUCCESS;


/**
 * Application class.
 */
public class App extends Application {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // 手动装载openCV库文件，以保证手机无需安装OpenCV Manager
    static {
        System.loadLibrary("opencv_java3");
    }

    // Http Mechanism
    private OnRequestListener mOnRequestListener = new OnRequestListener();

    @Override
    public void onCreate() {
        super.onCreate();
        mLog.i(TAG, "========== app start ==========");

        trustHost();

        // Task Schedule handle thread
        appThread = new Thread(appRunnable);
        appThread.start();

        setupTheme();
        setupAppStateTracking();

        // HTTP Mechanism
        OKHttpAgent.getInstance().setRequestListener(mOnRequestListener);
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

        private static final long task_minimum_tick_time_msec = 1000; // 1 second

        @Override
        public void run() {
            long tick_count = 0;
            mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));

            while (true) {
                try {
                    long start_time_tick = System.currentTimeMillis();
                    // real-time task

                    if (tick_count % 5 == 3) {
                        mLog.d(TAG, " * heartBeat * ");
                        AppBus.getInstance().post(new BusEvent("back to home page", APP_CODE_EVENT_QRCODE_ID_RESET));
                    }

                    if (tick_count % 60 == 6) {
//                        HashMap<String, String> mMap = new ListUser("test");
//                        OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_LIST_USER);
                    }

                    if (tick_count % 60 == 6) {
//                        HashMap<String, String> mMap = new GetFace("5de8a9b11cce9e1a10b14391");
//                        OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.APP_CODE_GET_FACE);
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
                }
            }
        }
    };



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
                case APP_CODE_GET_USER:
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_GET_USER_SUCCESS));
                    break;
            }
        }

        @Override
        public void onRequestFail(String errorResult, int requestCode) {
            mLog.d(TAG, "onRequestFail(), errorResult= " + errorResult);
            switch (requestCode) {
                case APP_CODE_GET_USER:
                    AppBus.getInstance().post(new BusEvent(errorResult, APP_CODE_GET_USER_FAIL));
                    break;
            }
        }

    }

}
