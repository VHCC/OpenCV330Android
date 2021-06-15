package acl.siot.opencvwpc20191007noc;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Iterator;

import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static acl.siot.opencvwpc20191007noc.App.isBarCodeReaderCanEdit;

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
//            mLog.i(TAG, "===== onReceive =====");
            String action = intent.getAction();
//            mLog.d(TAG, "action:> " + action.toString());
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null) {
                        if (device.getVendorId() == 7851 && device.getProductId() == 7427) {
                            mLog.d(TAG, "DEATTACHED- " + device);
                            mLog.d(TAG, " === BarCode Reader is DisConnected === ");
                            mLog.d(TAG,"getVendorId:> " + device.getVendorId() + ", getProductId:> " + device.getProductId());
                        }
                    }
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
//                        mLog.d(TAG, "ATTACHED- " + device);
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


}
