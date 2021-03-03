package acl.siot.opencvwpc20191007noc.api.detectInfo;

import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;

public class DetectInfo extends HashMap<String, Object> {

    final String API_KEY_RFID = "avalo_rfid";
    final String API_KEY_FACE_BASE_64 = "avalo_face_base_64";
    final String API_KEY_PEOPLE_TEMP = "avalo_people_temp";
    final String API_KEY_MASK_STATUS = "avalo_mask_status";
    final String API_KEY_DETECT_TIMESTAMP = "avalo_detect_timestamp";

    public DetectInfo(String rfid,
                      String face64,
                      Float temp,
                      boolean maskStatus,
                      double timestamp) {

        super.put(API_KEY_RFID, rfid);
        super.put(API_KEY_FACE_BASE_64, face64);
        super.put(API_KEY_PEOPLE_TEMP, temp);
        super.put(API_KEY_MASK_STATUS, maskStatus);
        super.put(API_KEY_DETECT_TIMESTAMP, timestamp);

        super.put(APP_KEY_HTTPS_URL, URLConstants.ICHEN_SERVER_URL + "api/v1/avalo/uploadData");

    }
}
