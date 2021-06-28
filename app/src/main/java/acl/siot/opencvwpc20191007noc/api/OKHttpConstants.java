package acl.siot.opencvwpc20191007noc.api;

public class OKHttpConstants {

    public static final String APP_KEY_HTTPS_URL = "WPC_REQUEST_URL";

    public static class ResponseKey {
        public static final String CODE = "Code";
        public static final String DATA = "Data";
        public static final String ERROR_CODE = "error_code";
        public static final String ERROR_MESSAGE = "error_message";
    }

    /**
     * To specify status of the https task.
     */
    public static class RequestCode {
        public static final int APP_CODE_LIST_USER = 1005;

        public static final int APP_CODE_UPDATE_IMAGE = 1006;
        public static final int APP_CODE_UPDATE_IMAGE_SUCCESS = 10061;

        public static final int APP_CODE_GET_FACE = 1007;

        public static final int AVALO_TEST = 89761;
    }

    public static class FrsRequestCode {

        public static final int APP_CODE_FRS_VERIFY = 10002;
        public static final int APP_CODE_FRS_VERIFY_SUCCESS = 100021;
        public static final int APP_CODE_FRS_VERIFY_UN_RECOGNIZED = 100022;

        public static final int APP_CODE_FRS_GET_FACE_ORIGINAL = 10003;
        public static final int APP_CODE_FRS_GET_FACE_ORIGINAL_SUCCESS = 100031;

        public static final int APP_CODE_THC_1101_HU_GET_TEMP = 11000;
        public static final int APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS = 110001;

        public static final int APP_CODE_FRS_MODIFY_PERSON_INFO = 10004;
        public static final int APP_CODE_FRS_MODIFY_PERSON_INFO_SUCCESS = 100041;

        public static final int DB_CODE_INSERT_DETECT_INFO = 50001;
        public static final int DB_CODE_INSERT_DETECT_INFO_SUCCESS = 500011;

        // VMS
        public static final int APP_CODE_VMS_SERVER_UPLOAD = 20000;
        public static final int APP_CODE_VMS_SERVER_UPLOAD_SUCCESS = 200001;

        public static final int APP_CODE_VMS_SERVER_UPLOAD_TPE = 20008;
        public static final int APP_CODE_VMS_SERVER_UPLOAD_TPE_SUCCESS = 200081;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_CONNECT = 20001;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_CONNECT_SUCCESS = 200011;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_CONNECT_FAIL = 200019;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_HB = 20002;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_HB_SUCCESS = 200021;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_SYNC = 20003;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_SYNC_SUCCESS = 200031;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL = 200039;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS = 20004;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_SUCCESS = 200041;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_FAIL = 200049;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_REMOVE = 20005;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_REMOVE_SUCCESS = 200051;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE = 20006;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE_SUCCESS = 200061;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL = 20007;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_CHECK_PERSON_SERIAL_SUCCESS = 200071;

        public static final int APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST = 20010;
        public static final int APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST_SUCCESS = 200101;

        public static final int APP_CODE_VMS_KIOSK_RFID_DETECT_DONE = 30001;

        public static final int APP_CODE_LOG_UPLOAD = 40001;
        public static final int APP_CODE_LOG_UPLOAD_SUCCESS = 400011;


    }

}
