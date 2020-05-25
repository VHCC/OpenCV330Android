package acl.siot.opencvwpc20191007noc.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import acl.siot.opencvwpc20191007noc.util.MLog;


/**
 * Created by IChen.Chu on 2019/1/24
 */
public class GatewayInfoCache {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Share Preference
     */
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    public static final String SHARE_GW_INFO = "share_gw_info";
    public static final String SHARE_GW_INFO_DB_VERSION = "share_gw_info_db_version";


    private String name = "";
    private String branch_id = "";
    private String status = "";

    private String db_version = "";

    private boolean isRegistered = false;

    /* Instance */
    private static GatewayInfoCache mGatewayInfoCache;
    private Context mContext;

    private GatewayInfoCache() {
    }

    public void newInstance(Context context) {
        mContext = context;
        sp = context.getSharedPreferences(SHARE_GW_INFO, Context.MODE_PRIVATE);
    }

    public static GatewayInfoCache getInstance() {
        if (mGatewayInfoCache == null) {
            mGatewayInfoCache = new GatewayInfoCache();
        }
        return mGatewayInfoCache;
    }

    public void initData(String name, String branchDID, String status) {
        this.name = name;
        this.branch_id = branchDID;
        this.status = status;
        Map<String, ?> map = sp.getAll();
        String dbVersion = ((String) map.get(SHARE_GW_INFO_DB_VERSION));
        setDbVersion(dbVersion);
    }

    public String getName() {
        return name;
    }

    public String getBranch_id() {
        return branch_id;
    }


    public String getStatus() {
        return status;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public void setDbVersion(String db_version) {
        this.db_version = db_version;
        editor = sp.edit();
        editor.putString(SHARE_GW_INFO_DB_VERSION, db_version).commit();
    }

    public String getDbVersion() {
        return db_version;
    }

    @Override
    public String toString() {
        return "name= " + name
                + ", branchID= " + branch_id
                + ", isRegistered = " + isRegistered;
    }
}
