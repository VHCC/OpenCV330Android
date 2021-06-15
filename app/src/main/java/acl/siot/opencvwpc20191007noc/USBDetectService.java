package acl.siot.opencvwpc20191007noc;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.view.InputDevice;

import java.util.HashMap;
import java.util.Iterator;

import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static acl.siot.opencvwpc20191007noc.App.isBarCodeReaderConnected;

/**
 * Created by IChen.Chu on 2018/11/20
 */
public class USBDetectService extends Service {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static final String ACTION_USB_PERMISSION = "advantech.ccm.USB_PERMISSION";

    private UsbManager mUsbManager = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mLog.d(TAG, "onBind");
        //-----------start-----------
        return null;
    }

    IntentFilter filterAttached_and_Detached = null;

    @Override
    public void onCreate() {
        mLog.d(TAG, "onCreate");

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        filterAttached_and_Detached = new IntentFilter();
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.EXTRA_PERMISSION_GRANTED);
        filterAttached_and_Detached.addAction("android.hardware.usb.action.USB_STATE");
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filterAttached_and_Detached);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        mLog.d(TAG, deviceList.size() + " USB device(s) founded");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            mLog.d(TAG, "" + device);
        }

//        InputManager inputManager = (InputManager) getSystemService(Context.INPUT_SERVICE);
//        mLog.d(TAG, inputManager.getInputDeviceIds().length + " inputManager device(s) founded");
//        for (int i = 0; i < inputManager.getInputDeviceIds().length; i++) {
//            InputDevice inputDevice = inputManager.getInputDevice(inputManager.getInputDeviceIds()[i]);
//            mLog.d(TAG, "inputDevice:> " + inputDevice);
//            mLog.d(TAG, "getVendorId:> " + inputDevice.getVendorId());
//            mLog.d(TAG, "getProductId:> " + inputDevice.getProductId());
//        }


        super.onCreate();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onReceive(Context context, Intent intent) {
            mLog.i(TAG, "===== onReceive =====");
            String action = intent.getAction();
            mLog.d(TAG, "action:> " + action.toString());
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null) {
                        mLog.d(TAG, "DEATTACHED- " + device);
                        isBarCodeReaderConnected = false;
                    }
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        mLog.d(TAG, "ATTACHED- " + device);
                    }
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    } else {
                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(USBDetectService.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        mUsbManager.requestPermission(device, mPermissionIntent);
                    }
                }
            }
//
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {

                            mLog.w(TAG, "getProductId= " + device.getProductId());
                            mLog.w(TAG, "getVendorId= " + device.getVendorId());
                            mLog.d(TAG, "PERMISSION- " + device);
//                            copyLogFileFrom530ToUSB();
//                            copyTREKConfigFileToDevice();
//                            startToConfig530();
                        }
                    }
                }
            }
        }
    };

//    private void copyLogFileFrom530ToUSB() {
//        mLog.w(TAG,"copyLogFileFrom530ToUSB()");
//        LogWriter.storeLogToFile("copyLogFileFrom530ToUSB()");
//        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(getApplicationContext()/* Context or Activity */);
//        for(UsbMassStorageDevice deviceUnit: devices) {
//
//            // before interacting with a device you need to call init()!
//            try {
//                deviceUnit.init();
//            } catch (IOException e) {
//                mLog.w(TAG, "deviceUnit.init()");
//                LogWriter.storeLogToFile("deviceUnit.init(), e= " + e.getMessage());
//                e.printStackTrace();
//            }
//
//            FileSystem currentFs = deviceUnit.getPartitions().get(0).getFileSystem();
//            UsbFile root = currentFs.getRootDirectory();
//
//            //Find the directory for the SD Card using the API
//            //*Don't* hardcode "/sdcard"
//            File sdcard = Environment.getExternalStorageDirectory();
//
//            //Get the text file
//            File loraLogIn530 = new File(sdcard,"loralog.txt");
//
//            if (loraLogIn530.exists()) {
//
//                try {
//                    mLog.w(TAG, "copy file name= " + loraLogIn530.getName());
//                    if (root.search(loraLogIn530.getName()) != null) {
//                        mLog.w(TAG, "delete old log file!");
//                        root.search(loraLogIn530.getName()).delete();
//                    }
//                    UsbFile usbFile = root.createFile(loraLogIn530.getName());
//                    long size = loraLogIn530.length();
//                    usbFile.setLength(loraLogIn530.length());
//                    InputStream inputStream = null;
//                    Uri uri = Uri.fromFile(loraLogIn530);
//                    inputStream = getContentResolver().openInputStream(uri);
//
//                    OutputStream outputStream = UsbFileStreamFactory.createBufferedOutputStream(usbFile, currentFs);
//
//                    byte[] bytes = new byte[1337];
//                    int count;
//                    long total = 0;
//
//                    while ((count = inputStream.read(bytes)) != -1){
//                        outputStream.write(bytes, 0, count);
//                        if (size > 0) {
//                            total += count;
//                            int progress = (int) total;
//                            if(loraLogIn530.length() > Integer.MAX_VALUE) {
//                                progress = (int) (total / 1024);
//                            }
//                        }
//                    }
//
//                    outputStream.close();
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    mLog.e(TAG, "error copying!");
//                    LogWriter.storeLogToFile("error copying! e= " + e.getMessage());
//                }
//            }
//
//            //Get the text file CFCCM530WIn530
//            File CFCCM530WIn530 = new File(sdcard,"CFCCM530W_Log.txt");
//
//            if (CFCCM530WIn530.exists()) {
//
//                try {
//                    mLog.w(TAG, "copy file name= " + CFCCM530WIn530.getName());
//                    if (root.search(CFCCM530WIn530.getName()) != null) {
//                        mLog.w(TAG, "delete old log file!");
//                        root.search(CFCCM530WIn530.getName()).delete();
//                    }
//                    UsbFile usbFile = root.createFile(CFCCM530WIn530.getName());
//                    long size = CFCCM530WIn530.length();
//                    usbFile.setLength(CFCCM530WIn530.length());
//                    InputStream inputStream = null;
//                    Uri uri = Uri.fromFile(CFCCM530WIn530);
//                    inputStream = getContentResolver().openInputStream(uri);
//
//                    OutputStream outputStream = UsbFileStreamFactory.createBufferedOutputStream(usbFile, currentFs);
//
//                    byte[] bytes = new byte[1337];
//                    int count;
//                    long total = 0;
//
//                    while ((count = inputStream.read(bytes)) != -1){
//                        outputStream.write(bytes, 0, count);
//                        if (size > 0) {
//                            total += count;
//                            int progress = (int) total;
//                            if(CFCCM530WIn530.length() > Integer.MAX_VALUE) {
//                                progress = (int) (total / 1024);
//                            }
//                        }
//                    }
//
//                    outputStream.close();
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    mLog.e(TAG, "error copying!");
//                    LogWriter.storeLogToFile("error copying! e= " + e.getMessage());
//                }
//            }
//
//            //Get the text file CFCCM530W_ReportIn530
//            File CFCCM530W_ReportIn530 = new File(sdcard,"CFCCM530W_Reports_Log.txt");
//
//            if (CFCCM530W_ReportIn530.exists()) {
//
//                try {
//                    mLog.w(TAG, "copy file name= " + CFCCM530W_ReportIn530.getName());
//                    LogWriter.storeLogToFile("copy file name= " + CFCCM530W_ReportIn530.getName());
//                    if (root.search(CFCCM530W_ReportIn530.getName()) != null) {
//                        mLog.w(TAG, "delete old log file!");
//                        root.search(CFCCM530W_ReportIn530.getName()).delete();
//                    }
//                    UsbFile usbFile = root.createFile(CFCCM530W_ReportIn530.getName());
//                    long size = CFCCM530W_ReportIn530.length();
//                    usbFile.setLength(CFCCM530W_ReportIn530.length());
//                    InputStream inputStream = null;
//                    Uri uri = Uri.fromFile(CFCCM530W_ReportIn530);
//                    inputStream = getContentResolver().openInputStream(uri);
//
//                    OutputStream outputStream = UsbFileStreamFactory.createBufferedOutputStream(usbFile, currentFs);
//
//                    byte[] bytes = new byte[1337];
//                    int count;
//                    long total = 0;
//
//                    while ((count = inputStream.read(bytes)) != -1){
//                        outputStream.write(bytes, 0, count);
//                        if (size > 0) {
//                            total += count;
//                            int progress = (int) total;
//                            if(CFCCM530W_ReportIn530.length() > Integer.MAX_VALUE) {
//                                progress = (int) (total / 1024);
//                            }
//                        }
//                    }
//
//                    outputStream.close();
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    mLog.e(TAG, "error copying!");
//                    LogWriter.storeLogToFile("error copying! e= " + e.getMessage());
//                }
//            }
//        }
//    }

//    private void copyTREKConfigFileToDevice() {
//        mLog.w(TAG,"copyTREKConfigFileToDevice()");
//        LogWriter.storeLogToFile("copyTREKConfigFileToDevice()");
//        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(getApplicationContext()/* Context or Activity */);
//
//        for(UsbMassStorageDevice deviceUnit: devices) {
//
//            // before interacting with a device you need to call init()!
//            try {
//                deviceUnit.init();
//            } catch (IOException e) {
//                mLog.w(TAG, "deviceUnit.init()");
//                LogWriter.storeLogToFile("deviceUnit.init(), e= " + e.getMessage());
//                e.printStackTrace();
//            }
//
//            FileSystem currentFs = deviceUnit.getPartitions().get(0).getFileSystem();
//            UsbFile root = currentFs.getRootDirectory();
//
//            //*Don't* hardcode "/sdcard"
//            File sdcard = Environment.getExternalStorageDirectory();
//
//            //Get the text file
//            File configFile = new File(sdcard,"TREK_530_CONFIG_SETTING.csv");
//
//            if (configFile.exists()) {
////                configFile.delete();
//                mLog.w(TAG, "TREK_530_CONFIG_SETTING.csv exist in TREK-530 device!");
//                LogWriter.storeLogToFile("TREK_530_CONFIG_SETTING.csv exist in TREK-530 device!");
//            }
//
//            try {
//                mLog.w(TAG, "config file name= " + configFile.getName());
//                LogWriter.storeLogToFile("config file name= " + configFile.getName());
//                if (root.search(configFile.getName()) != null) {
//
//                    UsbFile usbFile = root.search(configFile.getName());
//                    long size = configFile.length();
//                    OutputStream os = null;
//
//                    Uri uri = Uri.fromFile(configFile);
//                    os = getContentResolver().openOutputStream(uri);
//
//                    InputStream inputStream = UsbFileStreamFactory.createBufferedInputStream(usbFile, currentFs);
//
//                    byte[] bytes = new byte[1337];
//                    int count;
//                    long total = 0;
//
//                    while ((count = inputStream.read(bytes)) != -1){
//                        os.write(bytes, 0, count);
//                        if (size > 0) {
//                            total += count;
//                            int progress = (int) total;
//                            if(configFile.length() > Integer.MAX_VALUE) {
//                                progress = (int) (total / 1024);
//                            }
//                        }
//                    }
//
//                    inputStream.close();
//                    os.close();
//                    mLog.w(TAG, "TREK_530_CONFIG_SETTING.csv had copied to Device done!");
//                    LogWriter.storeLogToFile("TREK_530_CONFIG_SETTING.csv had copied to Device done!");
//                } else {
//                    mLog.w(TAG, "there's no TREK_530_CONFIG_SETTING.csv in USB device!");
//                    LogWriter.storeLogToFile("there's no TREK_530_CONFIG_SETTING.csv in USB device!");
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                mLog.e(TAG, "config 530 failed!");
//                LogWriter.storeLogToFile("config 530 failed! e= " + e.getMessage());
//            }
//        }
//    }

//    private void startToConfig530() {
//        mLog.w(TAG,"startToConfig530()");
//        LogWriter.storeLogToFile("startToConfig530()");
//
//        new CountDownTimer(2000, 1000) {
//
//            @Override
//            public void onTick(long millisUntilFinished) {
//            }
//
//            @Override
//            public void onFinish() {
//                //*Don't* hardcode "/sdcard"
//                File sdcard = Environment.getExternalStorageDirectory();
//                //Get the text file
//                File deviceConfigFile = new File(sdcard,"TREK_530_CONFIG_SETTING.csv");
//                loadTREK530ConfigFile(deviceConfigFile);
//            }
//        }.start();
//    }

    /**
     * Load External file to set configuration.
     */
//    private void loadTREK530ConfigFile(File filePath) {
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "Big5"));
//            String line;
//            String branchDID = "";
//            String GWDID = EdgeInfo.getInstance().getEdgeID();
//            mLog.w(TAG, "default GWDID= " + GWDID);
//            LogWriter.storeLogToFile( "default GWDID= " + GWDID);
//            LoraConfGenerator loraConfGenerator = new LoraConfGenerator();
//            HashMap<String, String> hashMap = new HashMap<>();
//            while ((line = reader.readLine()) != null) {
////                mLog.w(TAG, "line= " + line);
//
//                if (line.contains(ConfigConstants.LoadKey.SubKey_AddToCloud.BRANCH_ID)) {
//                    branchDID = line.split(",")[1];
//                    mLog.d(TAG, "branchDID= " + branchDID);
//                    LogWriter.storeLogToFile("branchDID= " + branchDID);
//                }
//
//                if (line.contains(ConfigConstants.LoadKey.SubKey_AddToCloud.GATEWAY_ID)) {
//                    GWDID = line.split(",")[1];
//                    mLog.d(TAG, "file GWDID= " + GWDID);
//                    LogWriter.storeLogToFile("file GWDID= " + GWDID);
//                }
//
//                if (line.contains(ConfigConstants.LoadKey.SubKey_AddToCloud.IPADDR)) {
//                    String ipaddr = line.split(",")[1];
//                    mLog.d(TAG, "ipaddr= " + ipaddr);
//                    LogWriter.storeLogToFile("ipaddr= " + ipaddr);
//                    hashMap.put(ConfigConstants.LoadKey.SubKey_AddToCloud.IPADDR, ipaddr);
//                }
//                hashMap.put(ConfigConstants.LoadKey.SubKey_AddToCloud.IPADDR, InternetHelper.getIPAddress(true));
//
//                if (line.contains(ConfigConstants.LoadKey.SubKey_AddToCloud.MODEL_NAME)) {
//                    String modelName = line.split(",")[1];
//                    mLog.d(TAG, "modelName= " + modelName);
//                    LogWriter.storeLogToFile("modelName= " + modelName);
//                    hashMap.put(ConfigConstants.LoadKey.SubKey_AddToCloud.MODEL_NAME, modelName);
//                }
//                hashMap.put(ConfigConstants.LoadKey.SubKey_AddToCloud.MODEL_NAME, "eis-land-530");
//
//                if (line.contains(ConfigConstants.LoadKey.SubKey_AddToCloud.NAME)) {
//                    String name = line.split(",")[1];
//                    mLog.d(TAG, "name= " + name);
//                    LogWriter.storeLogToFile("name= " + name);
//                    hashMap.put(ConfigConstants.LoadKey.SubKey_AddToCloud.NAME, name);
//                }
//
//                if (line.contains(ConfigConstants.LoadKey.SubKey_AddToCloud.DESCRIPTION)) {
//                    String desc = line.split(",")[1];
//                    mLog.d(TAG, "desc= " + desc);
//                    LogWriter.storeLogToFile("desc= " + desc);
//                    hashMap.put(ConfigConstants.LoadKey.SubKey_AddToCloud.DESCRIPTION, desc);
//                }
//
//                if (line.contains(ConfigConstants.LoadKey.SubKey_LoraMonitorLogConfig.LOG)) {
//                    String log = line.split(",")[1];
//                    loraConfGenerator.setLOG(log);
//                    mLog.d(TAG, "log= " + log);
//                    LogWriter.storeLogToFile("log= " + log);
//                }
//                if (line.contains(ConfigConstants.LoadKey.SubKey_LoraMonitorLogConfig.FW_VER)) {
//                    String fw_ver = line.split(",")[1];
//                    loraConfGenerator.setFW_VER(fw_ver);
//                    mLog.w(TAG, "fw_ver= " + fw_ver);
//                    LogWriter.storeLogToFile("fw_ver= " + fw_ver);
//                }
//                if (line.contains(ConfigConstants.LoadKey.SubKey_LoraMonitorLogConfig.CH)) {
//                    String ch = line.split(",")[1];
//                    loraConfGenerator.setCH(ch);
//                    mLog.w(TAG, "ch= " + ch);
//                    LogWriter.storeLogToFile("ch= " + ch);
//                }
//                if (line.contains(ConfigConstants.LoadKey.SubKey_LoraMonitorLogConfig.BR)) {
//                    String br = line.split(",")[1];
//                    loraConfGenerator.setBR(br);
//                    mLog.w(TAG, "br= " + br);
//                    LogWriter.storeLogToFile("br= " + br);
//                }
//            }
//            reader.close();
//            setupLoraLogFile(loraConfGenerator);
//            hashMap.put(ConfigConstants.LoadKey.SubKey_AddToCloud.VERSION, AppUtils.getAppVersionName());
//            String json = new JSONObject(hashMap).toString();
//            mLog.w(TAG, "json= " + json);
//            LogWriter.storeLogToFile("json= " + json);
//
//            try {
//                mLog.w(TAG, "AEC encrypt= " + new String(AESCBCUtil.AES_cbc_encrypt(json.getBytes())));
//            } catch (Exception e) {
//                mLog.e(TAG, "error= " + e.getMessage());
//                LogWriter.storeLogToFile("error= " + e.getMessage());
//                e.printStackTrace();
//            }
//
////            try {
////                HashMap<String, Object> mMap = new GWAdd(branchDID, GWDID, new String(AESCBCUtil.AES_cbc_encrypt(json.getBytes())));
////                OKHttpAgent.getInstance().postRequest(mMap);
////            } catch (IOException e) {
////                e.printStackTrace();
////                mLog.e(TAG, "GWAdd");
////                LogWriter.storeLogToFile("GWAdd, e= " + e.getMessage());
////            }
//
//        } catch (Exception e) {
//            mLog.e(TAG, "loadTREK530ConfigFile = " + e.getMessage());
//            LogWriter.storeLogToFile("loadTREK530ConfigFile Exception= " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

//    private void setupLoraLogFile(LoraConfGenerator loraConfGenerator) {
//        try {
//            mLog.w(TAG, "loraConfGenerator= " + loraConfGenerator.toString());
//            String configFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "loralog.txt";
//
//            File configFile = new File(configFilePath);
//
//            FileWriter fw;
//            //logfile over 4 MB will copy to backup file (4 MB = 4194304  Byte)
//            fw = new FileWriter(configFilePath);
//            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write(loraConfGenerator.toString());
//            bw.newLine();
//            bw.close();
//
//        } catch (IOException e) {
//            mLog.e(TAG, String.format("PID[%d]:  %s", android.os.Process.myPid(), e.getMessage()));
//            LogWriter.storeLogToFile(String.format("PID[%d]:  %s", android.os.Process.myPid(), e.getMessage()));
//        }
//    }

}
