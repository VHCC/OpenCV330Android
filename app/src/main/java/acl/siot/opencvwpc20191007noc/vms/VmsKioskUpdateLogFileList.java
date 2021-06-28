package acl.siot.opencvwpc20191007noc.vms;


import java.util.ArrayList;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2021/06/23.
 */
public class VmsKioskUpdateLogFileList extends HashMap<Object, Object> {

    final String API_KEY_UUID = "kioskUUID";
    final String API_KEY_FILENAMELIST = "fileNameList";
    final String API_KEY_FILESIZELIST = "fileSizeList";

    public VmsKioskUpdateLogFileList(String kioskUUID, ArrayList<String> fileNameList, ArrayList<Long> fileSizeList) {

        super.put(API_KEY_UUID, kioskUUID);
        super.put(API_KEY_FILENAMELIST, fileNameList);
        super.put(API_KEY_FILESIZELIST, fileSizeList);

        String httpPrefix = VMSEdgeCache.getInstance().getVms_host_is_ssl() ? "https://" : "http://";
        String vmsPort = VMSEdgeCache.getInstance().getVms_host_port() == "" ? VMSEdgeCache.getInstance().getVms_host_is_ssl() ? ":443" : ":80" : ":"+VMSEdgeCache.getInstance().getVms_host_port();
        super.put(APP_KEY_HTTPS_URL, httpPrefix + VMSEdgeCache.getInstance().getVmsHost() + vmsPort + "/api/v2/vmsKioskDeviceLog/updateDeviceLogFileList");
    }


}
