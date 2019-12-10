package acl.siot.opencvwpc20191007noc.api.updateImage;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by Ichen on 2019/12/06.
 */
public class UpdateImage extends HashMap<String, String> {

    final String API_KEY_ID = "id";
    final String API_KEY_IMAGE = "image";

    public UpdateImage(String id, String imageString) {

        super.put(API_KEY_ID, id);
        super.put(API_KEY_IMAGE, imageString);

        super.put(APP_KEY_HTTPS_URL, URLConstants.SERVER_URL + "/api/user/updateImage");

    }


}
