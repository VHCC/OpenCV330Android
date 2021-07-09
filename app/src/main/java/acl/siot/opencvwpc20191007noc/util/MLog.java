package acl.siot.opencvwpc20191007noc.util;

import android.util.Log;

import static acl.siot.opencvwpc20191007noc.vfr.adminSetting.VFRAdminPassword20210429Fragment.isDebugRecordMode;

public class MLog {

    // General Field
    static final boolean ENABLE_GLOBAL_LOG = true;
    static final String LOG_PREFIX = "Adventeh-WPC, ";

    // Local Field
    private final boolean enableLocalLog;

    public MLog(boolean enableLocalLog) {
        this.enableLocalLog = enableLocalLog;
    }

    private static String getClassName(Object obj) {
        return obj.getClass().getSimpleName();
    }

    public void v(String tag, String msg) {
        if (!ENABLE_GLOBAL_LOG) { return;}
        if (enableLocalLog) { Log.v(LOG_PREFIX + tag, msg);}
    }

    public void d(String tag, String msg) {

        if (!ENABLE_GLOBAL_LOG) { return;}
        if (enableLocalLog) {
            if (isDebugRecordMode) LogWriter.storeLogToDebugFile("DEBUG, ["+tag + "], " + msg);
            Log.d(LOG_PREFIX +tag, msg);
        }
    }

    public void i(String tag, String msg) {
        if (!ENABLE_GLOBAL_LOG) { return;}
        if (enableLocalLog) { Log.i(LOG_PREFIX +tag, msg);}
    }

    public void w(String tag, String msg) {
        if (!ENABLE_GLOBAL_LOG) { return;}
        if (enableLocalLog) { Log.w(LOG_PREFIX +tag, msg);}
    }

    public void e(String tag, String msg) {
        if (!ENABLE_GLOBAL_LOG) { return;}
        if (enableLocalLog) {
            if (isDebugRecordMode) LogWriter.storeLogToDebugFile("ERROR, ["+tag + "], " + msg);
            Log.e(LOG_PREFIX +tag, msg);
        }
    }

    public void e(String tag, String msg, Throwable tr) {
        if (!ENABLE_GLOBAL_LOG) { return;}
        if (enableLocalLog) { Log.e(LOG_PREFIX +tag, msg, tr);}
    }

}
