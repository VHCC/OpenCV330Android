package acl.siot.opencvwpc20191007noc.frsApi.modifyPersonInfo;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.cache.APITokenCache;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;

import static acl.siot.opencvwpc20191007noc.App.staticFRSSessionID;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.APP_KEY_HTTPS_URL;
import static acl.siot.opencvwpc20191007noc.wbSocket.FrsWebSocketClient.staticPersonInfo;


/**
 * Created by IChen on 2020/06/23.
 */
public class FrsModifyPersonInfo extends HashMap<String, Object> {

    final String API_KEY_SESSIONID = "session_id";
    final String API_KEY_PERSON_ID = "person_id";

    final String API_KEY_PERSON_INFO = "person_info";
    final String API_KEY_PERSON_INFO_FULL_NAME = "fullname";
    final String API_KEY_PERSON_INFO_EMPLOYEENO = "employeeno";
    final String API_KEY_PERSON_INFO_EMAIL_ADDRESS = "email_address";
    final String API_KEY_PERSON_INFO_CARDNO = "cardno";
    final String API_KEY_PERSON_INFO_EXPIRATION_DATE = "expiration_date";
    final String API_KEY_PERSON_INFO_ORGANIZATION = "organization";

    final String API_KEY_PERSON_INFO_GROUP_LIST = "group_list";

    final String API_KEY_PERSON_INFO_DEPARTMENT_LIST = "department_list";

    public FrsModifyPersonInfo(String personID, JSONObject personInfo) {



        HashMap<String, Object> personInfoMap = new HashMap<>();
        try {
            personInfoMap.put(API_KEY_PERSON_INFO_FULL_NAME, personInfo.getString(API_KEY_PERSON_INFO_FULL_NAME));
            personInfoMap.put(API_KEY_PERSON_INFO_EMPLOYEENO, personInfo.getString(API_KEY_PERSON_INFO_EMPLOYEENO));
            personInfoMap.put(API_KEY_PERSON_INFO_EMAIL_ADDRESS, personInfo.getString(API_KEY_PERSON_INFO_EMAIL_ADDRESS));
            personInfoMap.put(API_KEY_PERSON_INFO_CARDNO, personInfo.getString(API_KEY_PERSON_INFO_CARDNO));
            personInfoMap.put(API_KEY_PERSON_INFO_EXPIRATION_DATE, personInfo.getString(API_KEY_PERSON_INFO_EXPIRATION_DATE));
            personInfoMap.put(API_KEY_PERSON_INFO_ORGANIZATION, personInfo.getString(API_KEY_PERSON_INFO_ORGANIZATION));

            JSONArray groupList = staticPersonInfo.getJSONArray("group_list");
            ArrayList<Object> groupArrayList = new ArrayList();
            for (int groupIndex = 0; groupIndex < groupList.length(); groupIndex++) {
                HashMap<String, Object> dataGroupHashMap = new HashMap<>();
                dataGroupHashMap.put("id", ((JSONObject)groupList.get(groupIndex)).getString("id"));
                dataGroupHashMap.put("groupname", ((JSONObject)groupList.get(groupIndex)).getString("groupname"));
                groupArrayList.add(dataGroupHashMap);
            }
            personInfoMap.put(API_KEY_PERSON_INFO_GROUP_LIST, groupArrayList);

            ArrayList<Object> departmentsArrayList = new ArrayList();
            JSONArray department_list = staticPersonInfo.getJSONArray("department_list");
            for (int departmentIndex = 0; departmentIndex < department_list.length(); departmentIndex++) {
                HashMap<String, Object> dataDepartmentHashMap = new HashMap<>();
                dataDepartmentHashMap.put("objectId", ((JSONObject)department_list.get(departmentIndex)).getString("objectId"));
                dataDepartmentHashMap.put("code", ((JSONObject)department_list.get(departmentIndex)).getString("code"));
                dataDepartmentHashMap.put("name", ((JSONObject)department_list.get(departmentIndex)).getString("name"));
                dataDepartmentHashMap.put("description", ((JSONObject)department_list.get(departmentIndex)).getString("description"));
                departmentsArrayList.add(dataDepartmentHashMap);
            }

            personInfoMap.put(API_KEY_PERSON_INFO_DEPARTMENT_LIST, departmentsArrayList);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        super.put(API_KEY_PERSON_INFO, personInfoMap);
        super.put(API_KEY_SESSIONID, staticFRSSessionID);
        super.put(API_KEY_PERSON_ID, personID);
        super.put(APP_KEY_HTTPS_URL, "http://" + VFREdgeCache.getInstance().getIpAddress() + "/frs/cgi/modifypersoninfo");

    }


}
