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
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.view.InputDevice;
import android.widget.Toast;

import com.blankj.utilcode.util.DeviceUtils;

import acl.siot.opencvwpc20191007noc.thc11001huApi.PostConfig.PostConfig;
import acl.siot.opencvwpc20191007noc.thc11001huApi.firmware14181.Firmware14181Temp;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskUpdateLogFileList;
import acl.siot.opencvwpc20191007noc.vms.VmsLogUploadFile;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.cache.VFRAppSetting;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;
import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.rfid.SerialPortProxy;
import acl.siot.opencvwpc20191007noc.thc11001huApi.getTemp.GetTemp;
import acl.siot.opencvwpc20191007noc.theme.AppTheme;
import acl.siot.opencvwpc20191007noc.theme.AppThemeManager;
import acl.siot.opencvwpc20191007noc.util.AppStateTracker;
import acl.siot.opencvwpc20191007noc.util.LogWriter;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.MessageTools;
import acl.siot.opencvwpc20191007noc.util.NullHostNameVerifier;
import acl.siot.opencvwpc20191007noc.util.NullX509TrustManager;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskApplyUpdate;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskHB;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskSync;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_AVALO_THERMAL_POST_CONFIG;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_AVALO_THERMAL_POST_TEMP;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_AVALO_THERMAL_POST_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_GET_FACE_ORIGINAL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_GET_FACE_ORIGINAL_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_MODIFY_PERSON_INFO;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_MODIFY_PERSON_INFO_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_LOG_UPLOAD;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_AUTH_TIME_CHECK;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_AUTH_TIME_CHECK_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_AUTH_TIME_CHECK_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CONNECT;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CONNECT_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CONNECT_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_HB;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_REMOVE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_REMOVE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_RFID_DETECT_DONE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_STATUS_INACTIVE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_STATUS_INACTIVE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_SERVER_UPLOAD;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_SERVER_UPLOAD_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE_SUCCESS;

/**
 * Application class.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
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

    public static boolean TRAIL_IS_EXPIRE = false;
    SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    String toRFC3339(Date d) {
        return rfc3339.format(d).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        super.onCreate();

        mLog.i(TAG, "========== app start ==========");
//        LogWriter.storeLogToFile("========== app start ==========");
        LogWriter.storeLogToFile(",APP-START," + new Date().getTime() / 1000);

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
        VMSEdgeCache.getInstance().newInstance(this);
        VFRThermometerCache.getInstance().newInstance(this);
        VFRAppSetting.getInstance().newInstance(this);

        trustHost();

        // Task Schedule handle thread
        appThread = new Thread(appRunnable);
        appThread.start();

        setupTheme();
        setupAppStateTracking();

        SerialPortProxy.getInstance().initSerialPort("/dev/ttyUSB2");
        SerialPortProxy.getInstance().startPollingIdForZhongShanPIT();
        SerialPortProxy.getInstance().setCallback(new RFIDCallback());

//        Date d = new Date("2055/04/01");
//        mLog.d(TAG, "2021/04/01 d:> " + d.getTime() / 1000);
//        TRAIL_IS_EXPIRE = System.currentTimeMillis() / 1000 > d.getTime() / 1000;
//        mLog.d(TAG, "System.currentTimeMillis():> " + System.currentTimeMillis());
//        mLog.d(TAG, "isExpire:> " + TRAIL_IS_EXPIRE);
//        if (TRAIL_IS_EXPIRE)
//            AppBus.getInstance().post(new BusEvent("face detect done", DEVICE_NOT_SUPPORT));
        threadObject.setRunning(true);
    }

    // vms backend connect Status
    public static boolean isVmsConnected = false;

    // Card Reader FEATURE
    public static boolean isRFIDFunctionOn = false;

    // Barcode FEATURE
    public static boolean isBarCodeFunctionOn = false;
    public static boolean isBarCodeReaderCanEdit = false;

    public static boolean isFRServerConnected = true; //Deprecated 2021/01/15
    public static boolean isThermometerServerConnected = false;
    public static boolean isAvaloFirmwareOver14181 = false;

//    public static String detectSerialNumber = "00000000";

    private class RFIDCallback implements SerialPortProxy.Callback {
        @Override
        public void onResponse(@NonNull SerialPortProxy.Result resultType, @Nullable Object result) {
            mLog.d(TAG, "onResponse:> " + result.toString() + ", resultType:> " + resultType);
            if (isRFIDFunctionOn) {
//                detectSerialNumber = result.toString();
//                mLog.d(TAG, "detectSerialNumber:> " + detectSerialNumber);
                if (App.vmsPersonSyncMapSerial.containsKey(result.toString().trim())) {
                    MessageTools.showToast(getApplicationContext(), result.toString());
                    uploadPersonData = App.vmsPersonSyncMapSerial.get(result.toString().trim());
                    mLog.d(TAG, "rfid Person:> " + uploadPersonData);
                    AppBus.getInstance().post(new BusEvent(result.toString(), APP_CODE_VMS_KIOSK_RFID_DETECT_DONE));
                } else {
                    MessageTools.showToast(getApplicationContext(), result.toString() + " 此卡號無效");
                }
            }
        }

        @Override
        public void onFailure(@NonNull SerialPortProxy.Result resultType, @Nullable Object result) {
            mLog.e(TAG, "RFID, onFailure:> " + result.toString());
        }
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
        mLog.i(TAG, "========== app onTerminate ==========");
        if (appThread.isAlive()) {
            appThread.interrupt();
        }
        AppBus.getInstance().unregister(this);
        super.onTerminate();
    }

    /**
     * Trust all the https host.
     * //TODO it might be dangerous.
     */
    private void trustHost() {
        mLog.i(TAG, "trustHost()");
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
        SSLContext ssLContext = null;
        try {
            ssLContext = SSLContext.getInstance("TLS");
            ssLContext.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(ssLContext.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void checkUsbDeviceStatus() {
        boolean isBarCodeReaderConnectedCheck = false;
        InputManager inputManager = (InputManager) getSystemService(Context.INPUT_SERVICE);
        mLog.d(TAG, inputManager.getInputDeviceIds().length + " inputManager device(s) founded");
        for (int i = 0; i < inputManager.getInputDeviceIds().length; i++) {
            InputDevice inputDevice = inputManager.getInputDevice(inputManager.getInputDeviceIds()[i]);
            if (inputDevice.getVendorId() == 7851 && inputDevice.getProductId() == 7427) {
                mLog.d(TAG, " === BarCode Reader is Connected === ");
//                mLog.d(TAG,"inputDevice:> " + inputDevice);
//                mLog.d(TAG,"getVendorId:> " + inputDevice.getVendorId() + ", getProductId:> " + inputDevice.getProductId());
                isBarCodeReaderConnectedCheck = true;
                isBarCodeReaderCanEdit = true;
            }
        }
        if (!isBarCodeReaderConnectedCheck) {
            mLog.d(TAG, " $$$ BarCode Reader is DisConnected $$$ ");
            VMSEdgeCache.getInstance().setVms_kiosk_device_input_bar_code_scanner(false);
            isBarCodeReaderCanEdit = false;
        }
    }

    static final ThreadObject threadObject = new ThreadObject();

    static class ThreadObject extends Object {
        boolean isRunning = true;

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }
    }

    // Thread
    private Thread appThread;
    private Runnable appRunnable = new Runnable() {

        private static final long task_minimum_tick_time_msec = 800; // 0.8 second

        @Override
        public void run() {
            long tick_count = 0;
            mLog.d(TAG, "task_minimum_tick_time_msec= " + (task_minimum_tick_time_msec));

            while (threadObject.isRunning()) {
                try {
                    long start_time_tick = System.currentTimeMillis();

                    if (tick_count % 30 == 5) {
                        AppBus.getInstance().post(new BusEvent("VFR heart beats", VFR_HEART_BEATS));
                    }

                    if (tick_count % 1 == 0) {
                        if (isThermometerServerConnected) {
                            if (isAvaloFirmwareOver14181) {
                                HashMap<String, String> mMap = new Firmware14181Temp();
                                OKHttpAgent.getInstance().postAvaloTempRequest(mMap, APP_CODE_AVALO_THERMAL_POST_TEMP);
                            } else {
                                HashMap<String, String> mMap = new GetTemp();
                                OKHttpAgent.getInstance().getRequest(mMap, APP_CODE_THC_1101_HU_GET_TEMP);
                            }
                        }
                    }

                    if (tick_count % 20 == 2) {
                        if (!isAvaloFirmwareOver14181) {
                            HashMap<String, String> mMap = new PostConfig();
                            OKHttpAgent.getInstance().postAvaloConfigRequest(mMap, APP_CODE_AVALO_THERMAL_POST_CONFIG);
                        }
                    }

                    if (tick_count % 5 == 2) {
                        if (isAvaloFirmwareOver14181) {
                            HashMap<String, String> mMap = new GetTemp();
                            OKHttpAgent.getInstance().getRequest(mMap, APP_CODE_THC_1101_HU_GET_TEMP);
                        }
                    }

                    if (tick_count % 20 == 10) {
                        AppBus.getInstance().post(new BusEvent("vms HB", APP_CODE_VMS_KIOSK_DEVICE_HB));
                    }

                    if (tick_count % 5 == 0) {
                        AppBus.getInstance().post(new BusEvent("time tick", TIME_TICK));
                    }

                    if (tick_count % 10 == 0) {
                        checkUsbDeviceStatus();
                    }

//                    if (tick_count % 10 == 0) {
//                        mLog.d(TAG, getDeviceModel());
//                        // Check Device Model
//                        switch (getDeviceModel()) {
//                            case "usc_130_160":
//                            case "UTC-115G":
//                            case "HIT-507":
//                            case "HIT-512":
//                                break;
//                            default:
//                                AppBus.getInstance().post(new BusEvent("face detect done", DEVICE_NOT_SUPPORT));
//                                break;
//                        }
//                    }

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

    public static String getDeviceModel() {
        return Build.MODEL;
    }


    private DecimalFormat df = new DecimalFormat("0.00");

    public static HashMap<String, JSONObject> vmsPersonSyncMapUUID = new HashMap<>();
    public static HashMap<String, JSONObject> vmsPersonSyncMapSerial = new HashMap<>();
    public static JSONObject uploadPersonData;
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
                case APP_CODE_FRS_GET_FACE_ORIGINAL:
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_FRS_GET_FACE_ORIGINAL_SUCCESS));
                    break;
                case APP_CODE_THC_1101_HU_GET_TEMP:
                    isThermometerServerConnected = true;
                    if (!isAvaloFirmwareOver14181) {
                        AppBus.getInstance().post(new BusEvent(response, APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS));
                    }
                    break;
                case APP_CODE_FRS_MODIFY_PERSON_INFO:
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_FRS_MODIFY_PERSON_INFO_SUCCESS));
                    break;
                case APP_CODE_VMS_SERVER_UPLOAD:
                    mLog.d(TAG, "APP_CODE_VMS_SERVER_UPLOAD");
                    try {
                        JSONObject kioskUploadResponse = new JSONObject(response);
//                        mLog.d(TAG, "kioskUploadResponse:> " + kioskUploadResponse.toString());
                        String url = kioskUploadResponse.getString("url");
                        mLog.d(TAG, "url:> " + url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_VMS_SERVER_UPLOAD_SUCCESS));
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_CONNECT:
                    mLog.d(TAG, "APP_CODE_VMS_KIOSK_DEVICE_CONNECT");
                    try {
                        JSONObject kioskConnectResponse = new JSONObject(response);
                        int type = kioskConnectResponse.getInt("type");
                        JSONObject kioskDevice = kioskConnectResponse.getJSONObject("kioskDevice");
                        String kioskUUID = kioskDevice.getString("uuid"); // GET Kiosk UUID
                        VMSEdgeCache.getInstance().setVmsKioskUuid(kioskUUID);

                        mLog.d(TAG, "type:> " + type + ", kioskUUID:> " + kioskUUID);

                        switch(type) {
                            case 0:
                                AppBus.getInstance().post(new BusEvent("vms apply update", APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE)); // 一般連線，以本地端為主資料上傳
                                break;
                            case 1: // MAPPED Device
                                AppBus.getInstance().post(new BusEvent(response, APP_CODE_VMS_KIOSK_DEVICE_CONNECT_SUCCESS)); // 換機連線，以雲端為主資料下載
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_SYNC:
                    try {
                        isVmsConnected = true;
                        JSONObject kioskDeviceInfoResp = new JSONObject(response);
                        mLog.d(TAG, kioskDeviceInfoResp.toString());
                        JSONObject kioskDevice = kioskDeviceInfoResp.getJSONObject("kioskDeviceInfo");
                        JSONObject visitorTemplate = kioskDeviceInfoResp.getJSONObject("visitorTemplate");
                        JSONObject nonVisitorTemplate = kioskDeviceInfoResp.getJSONObject("nonVisitorTemplate");
                        JSONArray allVmsPersons = kioskDeviceInfoResp.getJSONArray("allVmsPerson");
                        if (null != allVmsPersons) {
                            mLog.d(TAG, "allVmsPersons len:> " + allVmsPersons.length());
                            for (int index = 0; index < allVmsPersons.length(); index++) {
                                JSONObject vmsPersonItem = (JSONObject) allVmsPersons.get(index);
                                vmsPersonSyncMapUUID.put(vmsPersonItem.get("vmsPersonUUID").toString(), vmsPersonItem);
                                vmsPersonSyncMapSerial.put(vmsPersonItem.get("vmsPersonSerial").toString(), vmsPersonItem);
                            }
                        }

                        // sync to device
                        VMSEdgeCache.getInstance().setVmsKioskUuid(kioskDevice.getString("uuid"));
                        VMSEdgeCache.getInstance().setVmsKioskDeviceName(kioskDevice.getString("deviceName"));

                        VMSEdgeCache.getInstance().setVms_kiosk_mode(kioskDevice.getInt("mode"));
                        VMSEdgeCache.getInstance().setVms_kiosk_video_type(kioskDevice.getInt("videoType"));
                        VMSEdgeCache.getInstance().setVms_kiosk_device_memo(kioskDevice.getString("memo"));

                        VMSEdgeCache.getInstance().setVms_kiosk_screen_timeout(kioskDevice.getInt("screenTimeout"));
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_device_host(kioskDevice.getString("avaloDeviceHost"));
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_alert_temp((float) kioskDevice.getDouble("avaloAlertTemp"));
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_temp_compensation((float) kioskDevice.getDouble("avaloTempCompensation"));
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_temp_unit(kioskDevice.getString("avaloTempUnit"));

                        VMSEdgeCache.getInstance().setVms_kiosk_is_enable_temp(kioskDevice.getBoolean("isEnableTemp"));
                        VMSEdgeCache.getInstance().setVms_kiosk_is_enable_mask(kioskDevice.getBoolean("isEnableMask"));

                        VMSEdgeCache.getInstance().setVmsKioskVisitorTemplateUuid(visitorTemplate.getString("templateName"));
                        VMSEdgeCache.getInstance().setVmsKioskDefaultTemplateUuid(nonVisitorTemplate.getString("templateName"));

                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_enable(kioskDevice.getBoolean("tEPEnable"));
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_host(kioskDevice.getString("tEPHost"));
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_port(kioskDevice.getString("tEPPort"));
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_enable_ssl(kioskDevice.getBoolean("tEPEnableSSL"));
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_account(kioskDevice.getString("tEPAccount"));
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_password(kioskDevice.getString("tEPPassword"));

                        VMSEdgeCache.getInstance().setVms_kiosk_settingPassword(kioskDevice.getString("settingPassword"));

                        int status = kioskDevice.getInt("status");
//                        mLog.d(TAG, "status:> " + status);
                        if (status == 2) {
                            AppBus.getInstance().post(new BusEvent(response, APP_CODE_VMS_KIOSK_STATUS_INACTIVE)); // 設備停用
                            return;
                        }

                        LogWriter.storeLogToFile(",SYNC-SERVER-SUCCESS," + new Date().getTime() / 1000);
                        AppBus.getInstance().post(new BusEvent(response, APP_CODE_VMS_KIOSK_DEVICE_SYNC_SUCCESS));
                    } catch (JSONException e) {
                        mLog.e(TAG, "e:> " + e.toString());
                        e.printStackTrace();
                    }
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_HB:
                    isVmsConnected = true;
                    try {
                        JSONObject kioskDeviceHBResp = new JSONObject(response);
                        JSONObject kioskDeviceHB = kioskDeviceHBResp.getJSONObject("vmsKioskDeviceHB");
                        Boolean isNeedSync = kioskDeviceHB.getBoolean("isNeedSync");
                        Boolean isNeedCheckLogFile = kioskDeviceHB.getBoolean("isNeedCheckLogFile");
                        if (isNeedSync) {
                            AppBus.getInstance().post(new BusEvent("sync vms Data", APP_CODE_VMS_KIOSK_DEVICE_SYNC));
                            VmsKioskSync mMap = new VmsKioskSync(VMSEdgeCache.getInstance().getVmsKioskUuid());
                            try {
                                OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_SYNC);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (isNeedCheckLogFile) {
                            File logDir = new File(LogWriter.showLogFileFolder());
                            walkDir(logDir);
                            VmsKioskUpdateLogFileList mMap = new VmsKioskUpdateLogFileList(VMSEdgeCache.getInstance().getVmsKioskUuid(), fileNameList, fileSizeList);
                            try {
                                OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE:
                    AppBus.getInstance().post(new BusEvent("", APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE_SUCCESS));
                    VmsKioskSync mMap = new VmsKioskSync(VMSEdgeCache.getInstance().getVmsKioskUuid());
                    try {
                        OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_SYNC);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS:
                    AppBus.getInstance().post(new BusEvent("", APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_SUCCESS));
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_REMOVE:
                    isVmsConnected = false;
                    AppBus.getInstance().post(new BusEvent("", APP_CODE_VMS_KIOSK_DEVICE_REMOVE_SUCCESS));
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL:
                    JSONObject vmsPerson = null;
                    try {
                        vmsPerson = new JSONObject(response);
                        uploadPersonData = vmsPerson.getJSONObject("vmsPerson");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppBus.getInstance().post(new BusEvent("", APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL_SUCCESS));
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST:
                    try {
                        JSONObject responseLogFile = new JSONObject(response);
                        JSONArray needUploadFiles = responseLogFile.getJSONArray("needUploadFileList");
                        mLog.d(TAG, needUploadFiles.toString());
                        mLog.d(TAG, "array size:> " + needUploadFiles.length());

                        for (int index = 0; index < needUploadFiles.length(); index ++) {
                            File logfile = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + needUploadFiles.get(index));

                            HashMap<Object, Object> mMapUpload = new VmsLogUploadFile(logfile);
                            try {
                                OKHttpAgent.getInstance().postUploadFileRequest(mMapUpload, APP_CODE_LOG_UPLOAD);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST_SUCCESS));
                    break;
                case APP_CODE_VMS_AUTH_TIME_CHECK:
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_VMS_AUTH_TIME_CHECK_SUCCESS));
                    break;
                case APP_CODE_AVALO_THERMAL_POST_CONFIG:
                    isThermometerServerConnected = true;
                    if (response != null) {
                        try {
                            JSONObject avaloConfig = new JSONObject(response);
                            String firmwareVersion = avaloConfig.getString("firmware_version");
                            mLog.d(TAG, " [Avalo] firmwareVersion:> " + firmwareVersion);
                            switch (firmwareVersion) {
                                case "ADV_1.4.18.1":
                                    isAvaloFirmwareOver14181 = true;
                                    break;
                                default:
//                                    isAvaloFirmwareOver14181 = false;
                                    break;
                            }
                        } catch (JSONException e) {
                            mLog.e(TAG, "JSONException, err:> " + e);
                            e.printStackTrace();
                        }
                    }
                    break;
                case APP_CODE_AVALO_THERMAL_POST_TEMP:
                    mLog.d(TAG, "APP_CODE_AVALO_THERMAL_POST_TEMP:> " + response);
//                    isThermometerServerConnected = true;
                    AppBus.getInstance().post(new BusEvent(response, APP_CODE_AVALO_THERMAL_POST_TEMP_SUCCESS));
                    break;
            }
        }

        @Override
        public void onRequestFail(String errorResult) {
//            mLog.d(TAG, "onRequestFail(), errorResult= " + errorResult);
        }

        @Override
        public void onRequestFail(String errorResult, int requestCode) {
            mLog.d(TAG, "onRequestFail(), errorResult= " + errorResult + ", requestCode= " + requestCode);
            switch (requestCode) {
                case APP_CODE_THC_1101_HU_GET_TEMP:
                    isThermometerServerConnected = false;
//                    isAvaloFirmwareOver14181 = false;
                    VMSEdgeCache.getInstance().setVms_kiosk_video_type(0);
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_SYNC:
                    AppBus.getInstance().post(new BusEvent("APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL", APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL));
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_CONNECT:
                    AppBus.getInstance().post(new BusEvent(errorResult, APP_CODE_VMS_KIOSK_DEVICE_CONNECT_FAIL));
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS:
                    AppBus.getInstance().post(new BusEvent(errorResult, APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_FAIL));
                    break;
                case APP_CODE_VMS_KIOSK_DEVICE_HB:
                    if (errorResult == null ) {
                        isVmsConnected = false;
                    } else if (errorResult.equals("DEVICE_INACTIVE")) {
                        AppBus.getInstance().post(new BusEvent(errorResult, APP_CODE_VMS_KIOSK_STATUS_INACTIVE));
                    } else {
                        isVmsConnected = false;
                    }
                    break;
                case APP_CODE_VMS_AUTH_TIME_CHECK:
                    AppBus.getInstance().post(new BusEvent("out of admission time", APP_CODE_VMS_AUTH_TIME_CHECK_FAIL));
                    break;
                case APP_CODE_AVALO_THERMAL_POST_CONFIG:
                    if (null != errorResult && errorResult.contains("to connect")) {
                        isThermometerServerConnected = false;
//                        isAvaloFirmwareOver14181 = false;
                        VMSEdgeCache.getInstance().setVms_kiosk_video_type(0);
                    }
                    break;

            }

        }
    }

    public void onEventBackgroundThread(BusEvent event) {
        switch (event.getEventType()) {
            case APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE_SUCCESS:
                mLog.d(TAG, "APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE_SUCCESS");
                break;
            case APP_CODE_VMS_KIOSK_STATUS_INACTIVE:
                AppBus.getInstance().post(new BusEvent("", APP_CODE_VMS_KIOSK_STATUS_INACTIVE_SUCCESS));
//                MessageTools.showLongToast(getBaseContext(), "此設備已經停用");
                break;
        }
    }

    public void onEventMainThread(BusEvent event) {
        switch (event.getEventType()) {
            case DEVICE_NOT_SUPPORT:
                Toast.makeText(getApplicationContext(),
                        "this device is not supported.", Toast.LENGTH_LONG).show();
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_HB:
                VmsKioskHB mMap = new VmsKioskHB(VMSEdgeCache.getInstance().getVmsKioskUuid());
                try {
                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_HB);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE:
                AppBus.getInstance().post(new BusEvent("sync vms Data", APP_CODE_VMS_KIOSK_DEVICE_SYNC));
                VmsKioskApplyUpdate mMap_apply = new VmsKioskApplyUpdate(VMSEdgeCache.getInstance().getVmsKioskUuid());
                try {
                    OKHttpAgent.getInstance().postRequest(mMap_apply, APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    // handler event
    public static final int VFR_HEART_BEATS = 6001;
    public static final int TIME_TICK = 7001;

    private ArrayList<String> fileNameList;
    private ArrayList<Long> fileSizeList;
    private void walkDir(File dir) {
        fileNameList = new ArrayList<>();
        fileSizeList = new ArrayList<>();

        String txtPattern = ".txt";

        File listFile[] = dir.listFiles();

        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    walkDir(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(txtPattern) && listFile[i].getName().contains(DeviceUtils.getAndroidID())){
                        fileNameList.add(listFile[i].getName());
                        fileSizeList.add(listFile[i].length());
                        mLog.d(TAG, "name:> " + listFile[i].getName() + ", file Size:> " + listFile[i].length());
                        //Do what ever u want
                    }
                }
            }
        }
    }

}
