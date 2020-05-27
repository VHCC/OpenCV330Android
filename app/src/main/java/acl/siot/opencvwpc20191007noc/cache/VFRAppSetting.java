package acl.siot.opencvwpc20191007noc.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import acl.siot.opencvwpc20191007noc.util.MLog;


/**
 * Created by IChen.Chu on 2020/05/25
 */
public class VFRAppSetting {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Share Preference
     */
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    public static final String SHARE_APP_SETTING = "share_app_setting";
    public static final String SHARE_APP_SETTING_PWD = "share_app_setting_pwd";


    private String pwd = "";


    /* Instance */
    private static VFRAppSetting mVfrThermometerCache;
    private Context mContext;

    private VFRAppSetting() {
    }

    public void newInstance(Context context) {
        mContext = context;
        sp = context.getSharedPreferences(SHARE_APP_SETTING, Context.MODE_PRIVATE);
    }

    public static VFRAppSetting getInstance() {
        if (mVfrThermometerCache == null) {
            mVfrThermometerCache = new VFRAppSetting();
        }
        return mVfrThermometerCache;
    }

    public String getPwd() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_APP_SETTING_PWD));
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
        editor = sp.edit();
        editor.putString(SHARE_APP_SETTING_PWD, pwd).commit();
    }
}
