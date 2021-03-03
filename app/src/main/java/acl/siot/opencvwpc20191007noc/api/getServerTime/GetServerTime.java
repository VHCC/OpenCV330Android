package acl.siot.opencvwpc20191007noc.api.getServerTime;

import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;

public class GetServerTime extends HashMap<String, String> {

    public GetServerTime() {

        super.put(APP_KEY_HTTPS_URL, URLConstants.ICHEN_SERVER_URL + "/serverTime");

    }
}
