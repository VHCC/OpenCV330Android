package acl.siot.opencvwpc20191007noc.frsApi.login;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by IChen on 2020/05/14.
 */
public class FrsLogin extends HashMap<String, String> {

    final String API_KEY_USER_NAME = "username";
    final String API_KEY_PWD = "password";

    public FrsLogin(String userName, String pwd) {

        super.put(API_KEY_USER_NAME, userName);
        super.put(API_KEY_PWD, pwd);

//        super.put(APP_KEY_HTTPS_URL, URLConstants.FRS_SERVER_URL + "/users/login");
        super.put(APP_KEY_HTTPS_URL, "http://" + VFREdgeCache.getInstance().getIpAddress() + "/users/login");

    }


}
