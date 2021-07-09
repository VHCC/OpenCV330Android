package acl.siot.opencvwpc20191007noc.thc11001huApi.PostConfig;



import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen.Chu on 2021/07/09.
 */
public class PostConfig extends HashMap<String, String> {

    public PostConfig() {
        super.put(APP_KEY_HTTPS_URL, "http://" + VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host() + "/json/setting.json");

    }

}

