package acl.siot.opencvwpc20191007noc.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import acl.siot.opencvwpc20191007noc.util.MLog;


/**
 * Created by IChen.Chu on 2020/05/25
 */
public class VFREdgeCache {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Share Preference
     */
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
//    public static final String SHARE_GW_INFO = "share_gw_info";
    public static final String SHARE_EDGE_INFO = "share_edge_info";
//    public static final String SHARE_GW_INFO_DB_VERSION = "share_gw_info_db_version";
    public static final String SHARE_EDGE_INFO_IP_ADDRESS = "share_edge_info_ip_address";
    public static final String SHARE_EDGE_INFO_PORT = "share_edge_info_port";
    public static final String SHARE_EDGE_INFO_USER_ACCOUNT = "share_edge_info_user_account";
    public static final String SHARE_EDGE_INFO_USER_PASSWORD = "share_edge_info_user_password";
    public static final String SHARE_EDGE_INFO_TABLET_ID = "share_edge_info_tablet_id";
    public static final String SHARE_EDGE_INFO_MATCH_SCORE = "share_edge_info_match_score";

    public static final String SHARE_IMAGE_STANDARD_MODE = "share_image_standard_mode";


    private String ipAddress = "";
    private String port = "";
    private String userAccount = "";
    private String userPwd = "";
    private String tabletID = "";
    private String matchScore = "0.85";
    private Boolean isImageStandardMode = true;


    /* Instance */
    private static VFREdgeCache mVFREdgeCache;
    private Context mContext;

    private VFREdgeCache() {
    }

    public void newInstance(Context context) {
        mContext = context;
        sp = context.getSharedPreferences(SHARE_EDGE_INFO, Context.MODE_PRIVATE);
    }

    public static VFREdgeCache getInstance() {
        if (mVFREdgeCache == null) {
            mVFREdgeCache = new VFREdgeCache();
        }
        return mVFREdgeCache;
    }

    public String getIpAddress() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_EDGE_INFO_IP_ADDRESS));
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        editor = sp.edit();
        editor.putString(SHARE_EDGE_INFO_IP_ADDRESS, ipAddress).commit();
    }

    public String getPort() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_EDGE_INFO_PORT));
    }

    public void setPort(String port) {
        this.port = port;
        editor = sp.edit();
        editor.putString(SHARE_EDGE_INFO_PORT, port).commit();
    }

    public String getUserAccount() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_EDGE_INFO_USER_ACCOUNT));
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
        editor = sp.edit();
        editor.putString(SHARE_EDGE_INFO_USER_ACCOUNT, userAccount).commit();
    }

    public String getUserPwd() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_EDGE_INFO_USER_PASSWORD));
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
        editor = sp.edit();
        editor.putString(SHARE_EDGE_INFO_USER_PASSWORD, userPwd).commit();
    }

    public String getTabletID() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_EDGE_INFO_TABLET_ID));
    }

    public void setTabletID(String tabletID) {
        this.tabletID = tabletID;
        editor = sp.edit();
        editor.putString(SHARE_EDGE_INFO_TABLET_ID, tabletID).commit();
    }

    public String getMatchScore() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_EDGE_INFO_MATCH_SCORE)) == null ? "0.85" : ((String) map.get(SHARE_EDGE_INFO_MATCH_SCORE));
    }

    public void setMatchScore(String matchScore) {
        this.matchScore = matchScore;
        editor = sp.edit();
        editor.putString(SHARE_EDGE_INFO_MATCH_SCORE, matchScore).commit();
    }

    public Boolean isImageStandardMode() {
        Map<String, ?> map = sp.getAll();
        return ((Boolean) map.get(SHARE_IMAGE_STANDARD_MODE)) == null ? true : ((Boolean) map.get(SHARE_IMAGE_STANDARD_MODE));
    }

    public void setImageStandardMode(Boolean flag) {
        this.isImageStandardMode = flag;
        editor = sp.edit();
        editor.putBoolean(SHARE_IMAGE_STANDARD_MODE, flag).commit();
    }
}
