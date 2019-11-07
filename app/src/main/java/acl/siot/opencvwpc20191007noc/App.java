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
import android.support.v4.content.ContextCompat;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import acl.siot.opencvwpc20191007noc.theme.AppTheme;
import acl.siot.opencvwpc20191007noc.theme.AppThemeManager;
import acl.siot.opencvwpc20191007noc.util.AppStateTracker;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.NullHostNameVerifier;
import acl.siot.opencvwpc20191007noc.util.NullX509TrustManager;


/**
 * Application class.
 */
public class App extends Application {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());


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

                    if (tick_count % 60 == 5) {
                        mLog.d(TAG, "heartBeat");
                    }

                    if (tick_count % 60 == 6) {
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
