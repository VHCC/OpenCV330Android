package acl.siot.opencvwpc20191007noc.vms;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2021/07/08.
 */
public class VmsKioskAuthTimeCheck extends HashMap<Object, Object> {

    final String API_KEY_ENDTIMESTAMP = "endTimestamp";

    public VmsKioskAuthTimeCheck(String endTimestamp) {

        super.put(API_KEY_ENDTIMESTAMP, endTimestamp);

        String httpPrefix = VMSEdgeCache.getInstance().getVms_host_is_ssl() ? "https://" : "http://";
        String vmsPort = VMSEdgeCache.getInstance().getVms_host_port() == "" ? VMSEdgeCache.getInstance().getVms_host_is_ssl() ? ":443" : ":80" : ":"+VMSEdgeCache.getInstance().getVms_host_port();
        super.put(APP_KEY_HTTPS_URL, httpPrefix + VMSEdgeCache.getInstance().getVmsHost() + vmsPort + "/api/v2/vmsKioskDevice/authorizeTimeCheck");
    }


}
