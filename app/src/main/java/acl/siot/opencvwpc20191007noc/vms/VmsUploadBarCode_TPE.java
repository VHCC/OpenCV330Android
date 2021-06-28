package acl.siot.opencvwpc20191007noc.vms;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2021/06/18.
 */
public class VmsUploadBarCode_TPE extends HashMap<Object, Object> {

    final String API_KEY_DEVICEUUID = "deviceUuid";
    final String API_KEY_MAPPINGPERSONUUID = "mappingPersonUUID";
    final String API_KEY_UPLOADDEVICENAME = "uploadDeviceName";
    final String API_KEY_AVALO_DEVICE_GROUP = "avalo_device_group";
    final String API_KEY_AVALO_MODE = "avalo_mode";
    final String API_KEY_AVALO_INTERFACE = "avalo_interface";
    final String API_KEY_AVALO_SNAPSHOT = "avalo_snapshot";
    final String API_KEY_AVALO_STATUS = "avalo_status";
    final String API_KEY_AVALO_EXCEPTION = "avalo_exception";
    final String API_KEY_AVALO_SERIAL = "avalo_serial";
    final String API_KEY_AVALO_NAME = "avalo_name";
    final String API_KEY_AVALO_VISITOR = "avalo_visitor";
    final String API_KEY_AVALO_EMAIL = "avalo_email";
    final String API_KEY_AVALO_DEPARTMENT = "avalo_department";
    final String API_KEY_AVALO_TEMPERATURE  = "avalo_temperature";
    final String API_KEY_AVALO_ENABLE_TEMPERATURE = "avalo_enable_temperature";
    final String API_KEY_AVALO_AVALO_TEMPERATURE_THRESHOLD = "avalo_temperature_threshold";
    final String API_KEY_AVALO_TEMPERATURE_ADJUST = "avalo_temperature_adjust";
    final String API_KEY_AVALO_TEMPERATURE_UNIT = "avalo_temperature_unit";
    final String API_KEY_AVALO_ENABLE_MASK = "avalo_enable_mask";
    final String API_KEY_AVALO_MASK = "avalo_mask";
    final String API_KEY_AVALO_UTC_TIMESTAMP = "avalo_utc_timestamp";

    public VmsUploadBarCode_TPE(JSONObject uploadPersonData) throws JSONException {

        NumberFormat formatter = new DecimalFormat("#00.0");
        super.put(API_KEY_DEVICEUUID, VMSEdgeCache.getInstance().getVmsKioskUuid());
        super.put(API_KEY_UPLOADDEVICENAME, VMSEdgeCache.getInstance().getVmsKioskDeviceName());
        super.put(API_KEY_AVALO_DEVICE_GROUP, "");
        super.put(API_KEY_AVALO_MODE,  "RealName".toLowerCase());
        super.put(API_KEY_AVALO_INTERFACE, "barcode".toLowerCase());
//        super.put(API_KEY_AVALO_SNAPSHOT, "data:image/jpeg;base64," + encoded);
        super.put(API_KEY_AVALO_STATUS, "authorized");
        super.put(API_KEY_AVALO_EXCEPTION, "");
        super.put(API_KEY_MAPPINGPERSONUUID, uploadPersonData.getString("vmsPersonUUID"));
        super.put(API_KEY_AVALO_SERIAL, uploadPersonData.getString("vmsPersonSerial"));
        super.put(API_KEY_AVALO_NAME, uploadPersonData.getString("vmsPersonName"));
        super.put(API_KEY_AVALO_VISITOR, false);
        super.put(API_KEY_AVALO_EMAIL, uploadPersonData.getString("vmsPersonEmail"));
        super.put(API_KEY_AVALO_DEPARTMENT, "");
        super.put(API_KEY_AVALO_TEMPERATURE, Float.valueOf(-1));
        super.put(API_KEY_AVALO_ENABLE_TEMPERATURE, VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp());
        super.put(API_KEY_AVALO_AVALO_TEMPERATURE_THRESHOLD, VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp());
        super.put(API_KEY_AVALO_TEMPERATURE_ADJUST, VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation());
        super.put(API_KEY_AVALO_TEMPERATURE_UNIT,  VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_unit());
        super.put(API_KEY_AVALO_ENABLE_MASK, VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask());
//            super.put(API_KEY_AVALO_MASK, isMask);
        super.put(API_KEY_AVALO_UTC_TIMESTAMP, System.currentTimeMillis());

//        String httpPrefix = VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl() ? "https://" : "http://";
//        String vmsPort = VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port() == "" ? VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl() ? ":443" : ":80" : ":"+ VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port();
//        super.put(APP_KEY_HTTPS_URL, httpPrefix + VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_host() + vmsPort +
//                "/api/v2/vmsKioskReports/uploadKioskData");

        super.put(APP_KEY_HTTPS_URL, VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_host());
    }


}
