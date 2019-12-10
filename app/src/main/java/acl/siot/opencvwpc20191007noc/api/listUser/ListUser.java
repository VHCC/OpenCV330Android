package acl.siot.opencvwpc20191007noc.api.listUser;


import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.api.URLConstants;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;


/**
 * Created by Ichen on 2019/12/06.
 */
public class ListUser extends HashMap<String, String> {

    final String API_KEY_KEYWORD = "keyword";

    public ListUser(String keyword) {

        super.put(API_KEY_KEYWORD, keyword);

        super.put(APP_KEY_HTTPS_URL, URLConstants.SERVER_URL + "/api/user/list");

    }


}
