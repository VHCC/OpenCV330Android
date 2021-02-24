package acl.siot.opencvwpc20191007noc.dbHelper;

public class DBConstants {

    public static final int DB_VERSION = 3;



    public class TableDataVersion {
        public final static String TABLE_NAME_DATA_VERSION = "dataversion";

        public final static int DATA_VERSION_ID = 12345;
        public final static String KEY_ID_PRIMARY = "_id";
        public final static String KEY_DATA_VERSION = "_data_version";
    }

    public class TableDetectInfo {
        public final static String TABLE_NAME_DETECT_INFO = "detectinfo";

        public final static String KEY_RFID_PRIMARY = "_rfid";
        public final static String KEY_FACE_BASE64 = "_face_base64";
        public final static String KEY_PEOPLE_TEMPERATURE = "_people_temperature";
        public final static String KEY_MASK_STATUS = "_mask_status";
        public final static String KEY_DETECT_TIMESTAMP = "_detect_timestamp";
        public final static String KEY_IS_SEND = "_is_send";
    }

}
