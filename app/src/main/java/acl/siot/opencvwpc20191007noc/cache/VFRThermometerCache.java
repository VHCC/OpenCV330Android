package acl.siot.opencvwpc20191007noc.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import acl.siot.opencvwpc20191007noc.util.MLog;


/**
 * Created by IChen.Chu on 2020/05/25
 */
public class VFRThermometerCache {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Share Preference
     */
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    public static final String SHARE_THERMO_INFO = "share_thermo_info";
    public static final String SHARE_THERMO_INFO_IP_ADDRESS = "share_thermo_info_ip_address";
    public static final String SHARE_THERMO_INFO_ALERT_TEMP = "share_thermo_info_alert_temp";


    private String ipAddress = "";
    private float alertTemp = 0.0f;


    /* Instance */
    private static VFRThermometerCache mVfrThermometerCache;
    private Context mContext;

    private VFRThermometerCache() {
    }

    public void newInstance(Context context) {
        mContext = context;
        sp = context.getSharedPreferences(SHARE_THERMO_INFO, Context.MODE_PRIVATE);
    }

    public static VFRThermometerCache getInstance() {
        if (mVfrThermometerCache == null) {
            mVfrThermometerCache = new VFRThermometerCache();
        }
        return mVfrThermometerCache;
    }

    public String getIpAddress() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_THERMO_INFO_IP_ADDRESS));
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        editor = sp.edit();
        editor.putString(SHARE_THERMO_INFO_IP_ADDRESS, ipAddress).commit();
    }

    public float getAlertTemp() {
        Map<String, ?> map = sp.getAll();
        return (((Float) map.get(SHARE_THERMO_INFO_ALERT_TEMP)) == null) ? 0.0f : ((Float) map.get(SHARE_THERMO_INFO_ALERT_TEMP));
    }

    public void setAlertTemp(float alertTemp) {
        this.alertTemp = alertTemp;
        editor = sp.edit();
        editor.putFloat(SHARE_THERMO_INFO_ALERT_TEMP, alertTemp).commit();
    }
}
