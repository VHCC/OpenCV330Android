package acl.siot.opencvwpc20191007noc.vms;


import java.util.Date;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.util.LogWriter;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2021/04/26.
 */
public class VmsKioskSync extends HashMap<Object, Object> {

    final String API_KEY_KIOSKUUID = "kioskUUID";

    public VmsKioskSync(String kioskUUID) {
        LogWriter.storeLogToFile(",SYNC-SERVER-REQUEST," + new Date().getTime()/1000);
        super.put(API_KEY_KIOSKUUID, kioskUUID );

        String httpPrefix = VMSEdgeCache.getInstance().getVms_host_is_ssl() ? "https://" : "http://";
        String vmsPort = VMSEdgeCache.getInstance().getVms_host_port() == "" ? VMSEdgeCache.getInstance().getVms_host_is_ssl() ? ":443" : ":80" : ":"+VMSEdgeCache.getInstance().getVms_host_port();
        super.put(APP_KEY_HTTPS_URL, httpPrefix + VMSEdgeCache.getInstance().getVmsHost() + vmsPort + "/api/v2/vmsKioskDevice/deviceSync");
    }


}
