package acl.siot.opencvwpc20191007noc.api.getFace;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by Ichen on 2019/12/10.
 */
public class GetFace extends HashMap<String, String> {

    final String API_KEY_ID = "id";

    public GetFace(String id) {

        super.put(API_KEY_ID, id);

        super.put(APP_KEY_HTTPS_URL, URLConstants.SERVER_URL + "/api/user/face");

    }


}
