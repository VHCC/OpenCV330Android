package acl.siot.opencvwpc20191007noc.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import acl.siot.opencvwpc20191007noc.util.MLog;

/**
 * Feature to cache the account information.
 * Created by Ichen on 2017/11/15.
 */
public class UserAccountCache {
    private static final MLog Log = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Share Preference
     */
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    public static final String SHARE_ACCOUNT_USER = "share_account_user";
    public static final String SHARE_ACCOUNT_USER_EMAIL_KEY = "account_user_email_key";
    public static final String SHARE_ACCOUNT_USER_PASSWORD_KEY = "account_user_password_key";
    public static final String SHARE_ACCOUNT_USER_CLOUD_DID_KEY = "account_user_cloud_did_key";

    /* Instance */
    private static UserAccountCache mInstance;
    private Context mContext;

    /**
     * Cache Data
     */
    private String userName;
    private String userEmail;
    private String passWord;

    private String CloudUserID;

    private boolean isUserLogin;

    /**
     * Constructor
     */
    private UserAccountCache() {
    }

    /**
     * The FirstTime MainApplication start the App.
     * @param context
     */
    public void newInstance(Context context) {
        mContext = context;
        sp = context.getSharedPreferences(SHARE_ACCOUNT_USER, Context.MODE_PRIVATE);
        initUserInfo();
    }

    public static UserAccountCache getInstance() {
        if (mInstance == null) {
            mInstance = new UserAccountCache();
        }
        return mInstance;
    }

    public boolean isUserCache() {
        if (userEmail == null || userEmail.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /* Logic */
    private void initUserInfo() {
        Map<String, ?> map = sp.getAll();
        String userEmail = ((String) map.get(SHARE_ACCOUNT_USER_EMAIL_KEY));
        getInstance().setUserEmail(userEmail);
        String userPassword = ((String) map.get(SHARE_ACCOUNT_USER_PASSWORD_KEY));
        getInstance().setPassWord(userPassword);
        String userCloudDid = ((String) map.get(SHARE_ACCOUNT_USER_CLOUD_DID_KEY));
        getInstance().setCloudUserID(userCloudDid);
    }

    /*----------------- Attributes ---------------------*/
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;

    }

    public String getUserEmail() {
        return userEmail;
    }

    public boolean checkUserLogin() {
        return isUserLogin;
    }

    public void setUserLogin(boolean status) {
        isUserLogin = status;
    }



    /**
     * Save into SharePreference
     * @param userEmail
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        editor = sp.edit();
        editor.putString(SHARE_ACCOUNT_USER_EMAIL_KEY, userEmail).commit();
    }

    public String getPassWord() {
        return passWord;
    }

    /**
     * Save into SharePreference
     * @param passWord
     */
    public void setPassWord(String passWord) {
        this.passWord = passWord;
        editor = sp.edit();
        editor.putString(SHARE_ACCOUNT_USER_PASSWORD_KEY, passWord).commit();
    }

    public String getCloudUserID() {
        return CloudUserID;
    }

    public void setCloudUserID(String cloudUserID) {
        this.CloudUserID = cloudUserID;
        editor = sp.edit();
        editor.putString(SHARE_ACCOUNT_USER_CLOUD_DID_KEY, cloudUserID).commit();
    }


}
