package acl.siot.opencvwpc20191007noc.rfid;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import acl.siot.opencvwpc20191007noc.util.MLog;

/**
 * The proxy to access API of serial port.
 * Created by Tm.Shih on 2017/8/15.
 */
public class SerialPortProxy {
    private static SerialPortProxy SPP = null;

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private HandlerThread mRequestHandlerThread;
    private RequestHandler mRequestHandler;
    private boolean mIsSupported = false;

    /**
     * Singleton pattern.
     */
    public static SerialPortProxy getInstance() {
        if (SPP == null) {
            SPP = new SerialPortProxy();
            SPP.mRequestHandlerThread = new HandlerThread(SPP.TAG);
            SPP.mRequestHandlerThread.start();
            SPP.mRequestHandler = new RequestHandler(SPP.mRequestHandlerThread.getLooper());
        }
        return SPP;
    }

    public enum Result {
        NTAG_UID, ZHONG_SHAN_TAG_UID;
    }

    public interface Callback {
        /** Called when the NFC card detected. **/
        void onResponse(@NonNull Result resultType, @Nullable Object result);

        /** Called when the NFC card detecting exception. **/
        void onFailure(@NonNull Result resultType, @Nullable Object result);
    }

    private SerialPortProxy() {
    }

    /**
     * Initialization of NFC module, e.g. /dev/ttyS3.
     */
    public void initSerialPort(@NonNull String serialPortPath) {
        mIsSupported = checkJogtekModule(serialPortPath);
        if (!mIsSupported) {
            mLog.w(TAG, "initSerialPort(): " + serialPortPath + " (Not support!)");
            return;
        }

        mLog.d(TAG, "initSerialPort(): " + serialPortPath);

        Message initMessage = mRequestHandler.obtainMessage(RequestHandler.MSG_OPEN_SERIAL_PORT);
        initMessage.obj = serialPortPath;
        initMessage.sendToTarget();
    }

    /**
     * Checking does the module support or not after {@link #initSerialPort(String)}.
     */
    public boolean isSupported() {
        return mIsSupported;
    }

    /**
     * Start polling of NFC card NTag_UID.
     */
    public void startPollingIdForKaohsiungChangGungPIT() {
        if (!mIsSupported) {
            mLog.w(TAG, "startPollingIdForKaohsiungChangGungPIT(): Cancel!");
            return;
        }

        mLog.d(TAG, "startPollingIdForKaohsiungChangGungPIT(): ");
        mRequestHandler.removeMessages(RequestHandler.MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT);
        mRequestHandler.obtainMessage(RequestHandler.MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT).sendToTarget();
    }

    /**
     * Stop polling of NFC card NTag_UID.
     */
    public void stopPollingIdForKaohsiungChangGungPIT() {
        if (!mIsSupported) {
            mLog.w(TAG, "stopPollingIdForKaohsiungChangGungPIT(): Cancel!");
            return;
        }

        mLog.d(TAG, "stopPollingIdForKaohsiungChangGungPIT(): ");
        mRequestHandler.removeMessages(RequestHandler.MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT);
    }

    /**
     * Start polling of NFC card NTag_UID & TagUID.
     */
    public void startPollingIdForZhongShanPIT() {
        if (!mIsSupported) {
            mLog.w(TAG, "startPollingIdForZhongShanPIT(): Cancel!");
            return;
        }

        mLog.d(TAG, "startPollingIdForZhongShanPIT(): ");
        mRequestHandler.removeMessages(RequestHandler.MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT);
        mRequestHandler.obtainMessage(RequestHandler.MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT).sendToTarget();
        /* Equivalence with following request. */
        //mRequestHandler.obtainMessage(RequestHandler.MSG_GET_NTAG_UID).sendToTarget();
        //mRequestHandler.obtainMessage(RequestHandler.MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT).sendToTarget();
    }

    /**
     * Stop polling of NFC card NTag_UID & TagUID.
     */
    public void stopPollingIdForZhongShanPIT() {
        if (!mIsSupported) {
            mLog.w(TAG, "stopPollingIdForZhongShanPIT(): Cancel!");
            return;
        }

        mLog.d(TAG, "stopPollingIdForZhongShanPIT(): ");
        mRequestHandler.removeMessages(RequestHandler.MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT);
    }

    /**
     * De-initialization of NFC module.
     */
    public void recycleSerialPort() {
        if (!mIsSupported) {
            mLog.w(TAG, "recycleSerialPort(): Cancel!");
            return;
        }

        mLog.d(TAG, "recycleSerialPort(): ");
        mRequestHandler.removeCallbacksAndMessages(null);
        mRequestHandler.obtainMessage(RequestHandler.MSG_CLOSE_SERIAL_PORT).sendToTarget();
    }

    /**
     * Register a callback to be invoked when the detection is responded.
     */
    public void setCallback(Callback callback) {
        mRequestHandler.mResponseHandler.callback = callback;
    }

    private boolean checkJogtekModule(@NonNull String serialPortPath) {
        if (serialPortPath == null || serialPortPath.length() == 0) {
            return false;
        }

        File fileSerialPort = new File(serialPortPath);
        if (fileSerialPort.exists() && !fileSerialPort.isFile() && !fileSerialPort.isDirectory() &&
                fileSerialPort.canRead() && fileSerialPort.canWrite() && !fileSerialPort.canExecute()) {
            // A serial port (COM) means the target path exist, not a file or directory, and readable / writable / non-executable.
            return true;
        }

        return false;
    }

    /**
     * The message queue to setup the serial port.
     */
    private static class RequestHandler extends Handler {
        public static final int MSG_NONE = 0x0001;
        public static final int MSG_OPEN_SERIAL_PORT = 0x1001;
        public static final int MSG_CLOSE_SERIAL_PORT = 0x1002;
        public static final int MSG_READ_NTAG = 0x2000;
        public static final int MSG_GET_NTAG_UID = 0x2001;
        public static final int MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT = 0x2002;
        public static final int MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT = 0x3000;
        public static final int MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT = 0x3001;
        private String TAG = SerialPortProxy.class.getSimpleName() + "#" + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        private ResponseHandler mResponseHandler = new ResponseHandler(Looper.getMainLooper());
        private SerialPortController mSerialPortController;

        public static String getMessageString(int message) {
            switch (message) {
                case MSG_NONE: return "MSG_NONE";
                case MSG_OPEN_SERIAL_PORT: return "MSG_OPEN_SERIAL_PORT";
                case MSG_CLOSE_SERIAL_PORT: return "MSG_CLOSE_SERIAL_PORT";
                case MSG_READ_NTAG: return "MSG_READ_NTAG";
                case MSG_GET_NTAG_UID: return "MSG_GET_NTAG_UID";
                case MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT: return "MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT";
                case MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT: return "MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT";
                case MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT: return "MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT";
                default: return "MSG_UNKNOWN";
            }
        }

        private RequestHandler(Looper looper) {
            super(looper);
        }

        /**
         * Returns a new message from the global message pool, to retrieve block data.
         * @param readBlock is a hex address.
         */
        public final Message obtainMessageReadNTAG(byte readBlock) {
            Message message = obtainMessage(MSG_READ_NTAG, new Byte(readBlock));
            return message;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_OPEN_SERIAL_PORT: {
                    if (mSerialPortController != null) {
                        mSerialPortController.setResponseHandler(null);
                        mSerialPortController.closeSerialPort();
                        mSerialPortController = null;
                    }

                    try {
                        mSerialPortController = new SerialPortController((String) msg.obj, SerialPortController.BOUD_RATE.BR_115200);
                        mSerialPortController.setResponseHandler(mResponseHandler);
                        mSerialPortController.onRequestStart(MSG_OPEN_SERIAL_PORT, null);
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        mLog.w(TAG, e.toString());
                    } catch (InterruptedException e) {
                        mLog.w(TAG, e.toString());
                    }

                    if (mSerialPortController != null) {
                        mSerialPortController.onRequestFinish(MSG_OPEN_SERIAL_PORT, null);
                    }
                } break;

                case MSG_CLOSE_SERIAL_PORT: {
                    if (mSerialPortController == null) {
                        return;
                    }

                    mSerialPortController.onRequestStart(MSG_CLOSE_SERIAL_PORT, null);
                    mSerialPortController.closeSerialPort();
                    mSerialPortController.onRequestFinish(MSG_CLOSE_SERIAL_PORT, null);
                    mSerialPortController.setResponseHandler(null);
                    mSerialPortController = null;
                } break;

                case MSG_READ_NTAG: {
                    if (mSerialPortController == null) {
                        return;
                    }

                    String readBlock = (msg.obj instanceof Byte) ? String.format("%02x", (Byte) msg.obj) : "00";
                    mSerialPortController.onRequestStart(MSG_READ_NTAG, readBlock);
                    mSerialPortController.ReadNtag(readBlock);
                    mSerialPortController.onRequestFinish(MSG_READ_NTAG, readBlock);
                } break;

                case MSG_GET_NTAG_UID: {
                    if (mSerialPortController == null) {
                        return;
                    }

                    mSerialPortController.onRequestStart(MSG_GET_NTAG_UID, null);
                    mSerialPortController.GetNtagUID();
                    mSerialPortController.onRequestFinish(MSG_GET_NTAG_UID, null);
                } break;

                case MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT: {
                    if (mSerialPortController == null) {
                        return;
                    }

                    /* Step 1. Parsing UID. */
                    mSerialPortController.onRequestStart(MSG_GET_NTAG_UID, null);
                    mSerialPortController.GetNtagUID();
                    mSerialPortController.onRequestFinish(MSG_GET_NTAG_UID, null);

                    /* Step 2. Parsing TagUID. */
                    mSerialPortController.onRequestStart(MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT, null);
                    mSerialPortController.ReadNtag("00");
                    mSerialPortController.ReadNtag("04");
                    mSerialPortController.ReadNtag("08");
                    mSerialPortController.ReadNtag("0C");
                    mSerialPortController.ReadNtag("10");
                    mSerialPortController.ReadNtag("14");
                    mSerialPortController.onRequestFinish(MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT, null);

                    Message message = obtainMessage(MSG_POLLING_ID_FOR_ZHONG_SHAN_PIT);
                    sendMessageDelayed(message, 500L);
                } break;

                case MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT: {
                    if (mSerialPortController == null) {
                        return;
                    }

                    mSerialPortController.onRequestStart(MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT, null);
                    mSerialPortController.ReadNtag("00");
                    mSerialPortController.ReadNtag("04");
                    mSerialPortController.ReadNtag("08");
                    mSerialPortController.ReadNtag("0C");
                    mSerialPortController.ReadNtag("10");
                    mSerialPortController.ReadNtag("14");
                    mSerialPortController.onRequestFinish(MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT, null);
                } break;

                case MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT: {
                    if (mSerialPortController == null) {
                        return;
                    }

                    mSerialPortController.onRequestStart(MSG_GET_NTAG_UID, null);
                    mSerialPortController.GetNtagUID();
                    mSerialPortController.onRequestFinish(MSG_GET_NTAG_UID, null);

                    Message message = obtainMessage(MSG_POLLING_ID_FOR_KAOHSIUNG_CHANG_GUNG_PIT);
                    sendMessageDelayed(message, 2000L);
                } break;
            }
        }
    }

    /**
     * Communication channel with serial port.
     */
    private static class SerialPortController extends SerialPortConsole {
        private static final String KEY_FIRMWARE_INFO = "Mode"; // e.g. Mode 15693
        private String TAG = SerialPortProxy.class.getSimpleName() + "#" + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        private String mSerialPortPath = null;
        private ResponseHandler mResponseHandler = null;
        private int mCommand = RequestHandler.MSG_NONE;
        private StringBuffer mStrBuf = new StringBuffer();

        protected SerialPortController(String path, BOUD_RATE baud) throws SecurityException, IOException, InvalidParameterException {
            super(path, baud);
            super.TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
            mSerialPortPath = path;
        }

        protected void onRequestStart(int command, @Nullable Object payload) {
            if (payload != null) mLog.d(TAG, "onRequestStart(): " + RequestHandler.getMessageString(command) + ", " + payload);
            mCommand = command;
            mStrBuf = new StringBuffer();
        }

        protected void onRequestFinish(int command, @Nullable Object payload) {
//            mLog.d(TAG, "onRequestFinish(): " + RequestHandler.getMessageString(command) + ", " + payload + '\n' + mStrBuf);

            mCommand = RequestHandler.MSG_NONE;

            switch (command) {
                case RequestHandler.MSG_GET_NTAG_UID: {
                    String result = handleGetNTagUIDforZhongShanPIT(mStrBuf.toString());
//                    mLog.i(TAG, "onRequestFinish(data): " + result);
                } break;

                case RequestHandler.MSG_READ_TAG_UID_FOR_ZHONG_SHAN_PIT: {
                    String result = handleReadTagUIDforZhongShanPIT(mStrBuf.toString());
                    //mLog.i(TAG, "onRequestFinish(data): " + result);
                } break;
            }
        }

        private String handleGetNTagUIDforZhongShanPIT(final String hexData) {
//            mLog.i(TAG, "handleGetNTagUIDforZhongShanPIT(hexData): " + hexData);
            int indexBlockStart = hexData.lastIndexOf("[");
            int indexBlockEnd = hexData.lastIndexOf("]");
            if (indexBlockStart <= -1 || indexBlockEnd <= -1 || indexBlockStart >= indexBlockEnd) {
//                mLog.w(TAG, "handleGetNTagUIDforZhongShanPIT(): Invalid block token!");
                if (mResponseHandler != null) {
                    //mResponseHandler.obtainMessage(ResponseHandler.ERR_GET_NTAG_UID).sendToTarget();
                }
                return "";
            }

//            mLog.d(TAG, "indexBlockStart:> " + indexBlockStart + ", indexBlockEnd:> " + indexBlockEnd);
            String tagUID = hexData.substring(indexBlockStart + 1, indexBlockEnd);
//            mLog.d(TAG, "tagUID:> " + tagUID);
            if (tagUID.contains(KEY_FIRMWARE_INFO)) {
                // Sometimes, serial port may taking firmware information only.
                return "";
            }
//            mLog.d(TAG, " *** correct, tagUID:> " + tagUID + " *** ");

            if (tagUID.length() == 18) {
                // For Zhong Shan NFC card, it's consist of 9 hex bytes (which byte[3] and byte[8] may be checksum).
                tagUID = tagUID.substring(0, 6) + tagUID.substring(8, 16);
            } else if (tagUID.length() == 10) {
                // For Taiwan easy card, it's consist of 5 hex bytes (which byte[4] may be checksum).
                tagUID = tagUID.substring(0, 8);
            }

            if (mResponseHandler != null) {
                mResponseHandler.obtainMessage(ResponseHandler.MSG_GET_NTAG_UID, tagUID).sendToTarget();
            }

            return tagUID;
        }

        private String handleReadTagUIDforZhongShanPIT(final String hexData) {
            /* Phase I. Concatenating all the content without the identifications. */
            int indexAnchor = 0;
            StringBuffer parseData = new StringBuffer();
            while (indexAnchor < hexData.length()) {
                int indexBlockStart = hexData.indexOf("[", indexAnchor);
                int indexBlockEnd = hexData.indexOf("]", indexAnchor);
                if (indexBlockStart <= -1 || indexBlockEnd <= -1 || indexBlockStart >= indexBlockEnd) {
                    mLog.w(TAG, "handleReadTagUIDforZhongShanPIT(): Invalid block token!");
                    if (mResponseHandler != null) {
                        mResponseHandler.obtainMessage(ResponseHandler.ERR_GET_ZHONG_SHAN_TAG_UID, "Invalid block token!").sendToTarget();
                    }
                    return "";
                }

                // data block may be 16 or 16 + 2 (checksum?) bytes.
                parseData.append(hexData.substring(
                        indexBlockStart + 1,
                        (indexBlockEnd - indexBlockStart - 1 == 36) ? indexBlockEnd - 4 : indexBlockEnd));
                indexAnchor = indexBlockEnd + 1;
            }

            /* Phase II. Taking the Tag UID (in HEX). */
            int indexStart = 50;             // i.e. from byte[25]
            int indexEnd = indexStart + 64;  // i.e. to byte[56]
            String hexUID = "";
            if (parseData.length() > indexEnd) {
                hexUID = parseData.substring(indexStart, indexEnd);
            } else {
                if (parseData.length() > 0) {
                    mLog.w(TAG, "handleReadTagUIDforZhongShanPIT(): Unknown NFC card! " + parseData);
                }
                if (mResponseHandler != null && parseData.length() > 0) {
                    mResponseHandler.obtainMessage(ResponseHandler.ERR_GET_ZHONG_SHAN_TAG_UID, "Unknown NFC card! " + parseData).sendToTarget();
                }
                return "";
            }

            /* Phase III. Convert the HEX string to ASCII. */
            StringBuffer tagUID = new StringBuffer();
            try {
                String hexCH;
                for (int i = 0; i < hexUID.length(); i += 2) {
                    hexCH = hexUID.substring(i, i + 2);
                    tagUID.append((char) Integer.parseInt(hexCH, 16));
                }
            } catch (IndexOutOfBoundsException e) {
                mLog.w(TAG, "handleReadTagUIDforZhongShanPIT(): " + e.toString());
                if (mResponseHandler != null) {
                    mResponseHandler.obtainMessage(ResponseHandler.ERR_GET_ZHONG_SHAN_TAG_UID, "Data index out of bounds!").sendToTarget();
                }
                return "";
            } catch (NumberFormatException e) {
                mLog.w(TAG, "handleReadTagUIDforZhongShanPIT(): " + e.toString());
                if (mResponseHandler != null) {
                    mResponseHandler.obtainMessage(ResponseHandler.ERR_GET_ZHONG_SHAN_TAG_UID, "Data format error!").sendToTarget();
                }
                return "";
            }

            if (mResponseHandler != null) {
                mResponseHandler.obtainMessage(ResponseHandler.MSG_GET_ZHONG_SHAN_TAG_UID, tagUID.toString()).sendToTarget();
            }

            return tagUID.toString();
        }

        @Override
        protected void onDataReceived(byte[] data, int size) {
            if (mCommand != RequestHandler.MSG_NONE) {
                mStrBuf.append(new String(data, 0, size));
            }
        }

        @Override
        protected void onSendData(String data) {
        }

        public void setResponseHandler(ResponseHandler handler) {
            mResponseHandler = handler;
        }
    }

    /**
     * The {@link Handler} to respond feedback in UI thread.
     */
    private static class ResponseHandler extends Handler {
        public static final int MSG_GET_NTAG_UID = 0x11000;
        public static final int ERR_GET_NTAG_UID = 0x11100;
        public static final int MSG_GET_ZHONG_SHAN_TAG_UID = 0x12000;
        public static final int ERR_GET_ZHONG_SHAN_TAG_UID = 0x12100;
        private SerialPortProxy.Callback callback = null;

        private ResponseHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_GET_NTAG_UID: {
                    if (callback != null) {
                        callback.onResponse(Result.NTAG_UID, msg.obj);
                    }
                } break;

                case ERR_GET_NTAG_UID: {
                    if (callback != null) {
                        callback.onFailure(Result.NTAG_UID, "");
                    }
                } break;

                case MSG_GET_ZHONG_SHAN_TAG_UID: {
                    if (callback != null) {
                        callback.onResponse(Result.ZHONG_SHAN_TAG_UID, msg.obj);
                    }
                } break;

                case ERR_GET_ZHONG_SHAN_TAG_UID: {
                    if (callback != null) {
                        callback.onFailure(
                                Result.ZHONG_SHAN_TAG_UID,
                                ((msg.obj instanceof String) ? msg.obj.toString() : ""));
                    }
                } break;
            }
        }
    }
}
