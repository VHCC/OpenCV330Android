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

        public static final int APP_CODE_GET_USER = 1009;
        public static final int APP_CODE_GET_USER_SUCCESS = 10091;
        public static final int APP_CODE_GET_USER_FAIL = 10092;

        public static final int APP_CODE_EVENT_QRCODE_ID_RESET = 9009;

        public static final int APP_CODE_THC_1101_HU_GET_TEMP = 11000;
        public static final int APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS = 110001;


    }

}
