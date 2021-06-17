package acl.siot.opencvwpc20191007noc.vms;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2021/06/16.
 */
public class CheckVmsPersonSerial extends HashMap<Object, Object> {

    final String API_KEY_VMS_PERSON_SERIAL = "vmsPersonSerial";

    public CheckVmsPersonSerial(String vmsPersonSerial) {

        super.put(API_KEY_VMS_PERSON_SERIAL, vmsPersonSerial);

        String httpPrefix = VMSEdgeCache.getInstance().getVms_host_is_ssl() ? "https://" : "http://";
        String vmsPort = VMSEdgeCache.getInstance().getVms_host_port() == "" ? VMSEdgeCache.getInstance().getVms_host_is_ssl() ? ":443" : ":80" : ":"+VMSEdgeCache.getInstance().getVms_host_port();
        super.put(APP_KEY_HTTPS_URL, httpPrefix + VMSEdgeCache.getInstance().getVmsHost() + vmsPort + "/api/v2/vmsPerson/checkVmsPersonSerialByDevice");
    }


}
