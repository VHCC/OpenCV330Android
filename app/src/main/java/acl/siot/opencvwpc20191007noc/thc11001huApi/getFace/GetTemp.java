package acl.siot.opencvwpc20191007noc.thc11001huApi.getFace;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen.Chu on 2020/04/22.
 */
public class GetTemp extends HashMap<String, String> {

    public GetTemp() {

        super.put(APP_KEY_HTTPS_URL, URLConstants.THC_1101_HU_URL + "/gettemp");

    }


}
