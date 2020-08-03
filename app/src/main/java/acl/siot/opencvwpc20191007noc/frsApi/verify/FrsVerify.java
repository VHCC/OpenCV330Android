package acl.siot.opencvwpc20191007noc.frsApi.verify;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;

import static acl.siot.opencvwpc20191007noc.App.staticFRSSessionID;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2020/05/14.
 */
public class FrsVerify extends HashMap<Object, Object> {

    final String API_KEY_SESSIONID = "sessionId";
    final String API_KEY_TARGET_SCORE = "target_score";
    final String API_KEY_REQUEST_CLIENT = "request_client";
    final String API_KEY_ACTION_ENABLE = "action_enable";
    final String API_KEY_SOURCE_ID = "source_id";
    final String API_KEY_LOCATION = "location";
    final String API_KEY_IMAGE = "image";

    public FrsVerify(String encoded) {

        super.put(API_KEY_SESSIONID, staticFRSSessionID);
        super.put(API_KEY_TARGET_SCORE, Float.valueOf(VFREdgeCache.getInstance().getMatchScore()));
        super.put(API_KEY_REQUEST_CLIENT, "465");
        super.put(API_KEY_ACTION_ENABLE, 0);
        super.put(API_KEY_SOURCE_ID, VFREdgeCache.getInstance().getTabletID());
        super.put(API_KEY_LOCATION, "");
        super.put(API_KEY_IMAGE, encoded);

//        super.put(APP_KEY_HTTPS_URL, URLConstants.FRS_SERVER_URL + "/frs/cgi/verifyface");
        super.put(APP_KEY_HTTPS_URL, "http://" + VFREdgeCache.getInstance().getIpAddress() + "/frs/cgi/verifyface");

    }


}
