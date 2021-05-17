package acl.siot.opencvwpc20191007noc.vms;


import com.blankj.utilcode.util.AppUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2021/04/26.
 */
public class VmsKioskConnect extends HashMap<Object, Object> {

    final String API_KEY_CONNECTNUMBER = "connectNumber";
    final String API_KEY_DEVICENAME = "deviceName";
    final String API_KEY_ANDROIDID = "androidID";
    final String API_KEY_APPVERSION = "appVersion";

    public VmsKioskConnect(String paringCode, String kioskName, String androidID) {

        super.put(API_KEY_CONNECTNUMBER, paringCode);
        super.put(API_KEY_DEVICENAME, kioskName);
        super.put(API_KEY_ANDROIDID, androidID);
        super.put(API_KEY_APPVERSION, AppUtils.getAppVersionName());

        String httpPrefix = VMSEdgeCache.getInstance().getVms_host_is_ssl() ? "https://" : "http://";
        String vmsPort = VMSEdgeCache.getInstance().getVms_host_port() == "" ? VMSEdgeCache.getInstance().getVms_host_is_ssl() ? ":443" : ":80" : ":"+VMSEdgeCache.getInstance().getVms_host_port();
        super.put(APP_KEY_HTTPS_URL, httpPrefix + VMSEdgeCache.getInstance().getVmsHost() + vmsPort + "/api/v2/vmsKioskDevice/deviceConnect");
    }


}
