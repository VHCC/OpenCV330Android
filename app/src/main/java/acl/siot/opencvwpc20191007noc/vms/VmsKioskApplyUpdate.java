package acl.siot.opencvwpc20191007noc.vms;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2021/04/26.
 */
public class VmsKioskApplyUpdate extends HashMap<Object, Object> {

    final String API_KEY_UUID = "uuid";
    final String API_KEY_DEVICENAME = "deviceName";
    final String API_KEY_MODE = "mode";
    final String API_KEY_VIDEOTYPE = "videoType";
    final String API_KEY_MEMO = "memo";
    final String API_KEY_SCREENTIMEOUT = "screenTimeout";
    final String API_KEY_AVALODEVICEHOST = "avaloDeviceHost";
    final String API_KEY_AVALOALERTTEMP = "avaloAlertTemp";
    final String API_KEY_AVALOTEMPCOMPENSATION = "avaloTempCompensation";

    final String API_KEY_TEPENABLE = "tEPEnable";
    final String API_KEY_TEPHOST = "tEPHost";
    final String API_KEY_TEPPORT = "tEPPort";
    final String API_KEY_TEPENABLESSL = "tEPEnableSSL";
    final String API_KEY_TEPACCOUNT = "tEPAccount";
    final String API_KEY_TEPPASSWORD = "tEPPassword";

    public VmsKioskApplyUpdate(String kioskUUID) {

        super.put(API_KEY_UUID, kioskUUID);
        super.put(API_KEY_DEVICENAME, VMSEdgeCache.getInstance().getVmsKioskDeviceName());
        super.put(API_KEY_MODE, VMSEdgeCache.getInstance().getVms_kiosk_mode());
        super.put(API_KEY_VIDEOTYPE, VMSEdgeCache.getInstance().getVms_kiosk_video_type());
//        super.put(API_KEY_MEMO, kioskUUID);
        super.put(API_KEY_SCREENTIMEOUT, VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout());
        super.put(API_KEY_AVALODEVICEHOST, VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host());
        super.put(API_KEY_AVALOALERTTEMP, VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp());
        super.put(API_KEY_AVALOTEMPCOMPENSATION, VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation());

        super.put(API_KEY_TEPENABLE, VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable());
        super.put(API_KEY_TEPHOST, VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_host());
        super.put(API_KEY_TEPPORT, VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port());
        super.put(API_KEY_TEPENABLESSL, VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl());
        super.put(API_KEY_TEPACCOUNT, VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_account());
        super.put(API_KEY_TEPPASSWORD, VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_password());

        String httpPrefix = VMSEdgeCache.getInstance().getVms_host_is_ssl() ? "https://" : "http://";
        String vmsPort = VMSEdgeCache.getInstance().getVms_host_port() == "" ? VMSEdgeCache.getInstance().getVms_host_is_ssl() ? ":443" : ":80" : ":"+VMSEdgeCache.getInstance().getVms_host_port();
        super.put(APP_KEY_HTTPS_URL, httpPrefix + VMSEdgeCache.getInstance().getVmsHost() + vmsPort + "/api/v2/vmsKioskDevice/deviceApplyUpdate");
    }


}
