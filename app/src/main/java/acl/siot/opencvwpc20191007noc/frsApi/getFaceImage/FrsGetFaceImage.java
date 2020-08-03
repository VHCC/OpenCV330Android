package acl.siot.opencvwpc20191007noc.frsApi.getFaceImage;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;

import static acl.siot.opencvwpc20191007noc.App.staticFRSSessionID;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2020/05/14.
 */
public class FrsGetFaceImage extends HashMap<String, String> {

    final String API_KEY_SESSIONID = "sessionId";
    final String API_KEY_FACE_ID_NUMBER = "face_id_number";

    public FrsGetFaceImage(String face_id) {

        super.put(API_KEY_SESSIONID, staticFRSSessionID);
        super.put(API_KEY_FACE_ID_NUMBER, face_id);

//        super.put(APP_KEY_HTTPS_URL, URLConstants.FRS_SERVER_URL + "/persons/faceimage");
        super.put(APP_KEY_HTTPS_URL, "http://" + VFREdgeCache.getInstance().getIpAddress() + "/persons/faceimage");

    }


}
