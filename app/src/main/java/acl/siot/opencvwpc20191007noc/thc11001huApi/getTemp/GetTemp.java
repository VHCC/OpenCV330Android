package acl.siot.opencvwpc20191007noc.thc11001huApi.getTemp;



import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen.Chu on 2020/05/14.
 */
public class GetTemp extends HashMap<String, String> {

    public GetTemp() {

//        super.put(APP_KEY_HTTPS_URL, URLConstants.THC_1101_HU_URL + "/gettemp");
        super.put(APP_KEY_HTTPS_URL, "http://" + VFRThermometerCache.getInstance().getIpAddress() + "/gettemp");

    }


}

