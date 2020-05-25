package acl.siot.opencvwpc20191007noc.cache;

/**
 * Created by IChen.Chu on 2019/1/18
 */
public class APITokenCache {

    // Field
    private String tokenID;
    private String key;

    private static APITokenCache mApiTokenCache;

    // Key
    public static String KEY_TOKEN_ID = "token_id";
    public static String KEY_KEY = "key";

    public APITokenCache(String responseTokenID, String responseKey) {
        this.tokenID = responseTokenID;
        this.key = responseKey;
    }

    public static void createNewToken(String responseTokenID, String responseKey) {
        mApiTokenCache = new APITokenCache(responseTokenID, responseKey);
    }

    public static APITokenCache getInstance() {
        return mApiTokenCache;
    }

    // -------------------------------------
    public String getTokenID() {
        return tokenID;
    }

    public String getKey() {
        return key;
    }
}
