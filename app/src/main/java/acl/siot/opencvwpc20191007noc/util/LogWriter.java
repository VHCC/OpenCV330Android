package acl.siot.opencvwpc20191007noc.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogWriter {

    private static final MLog mLog = new MLog(false);
    private static final String TAG = "LogWriter";

    static public void storeLogToFile(String writeData) {
        try {

            mLog.i(TAG, String.format("PID[%d]:  %s", android.os.Process.myPid(), writeData));

//            String logFilePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + toDayLogFileName() + "_Log.txt";
//            String backupLogFilePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + toDayLogFileName() + "_Log_backup.txt";

            String logFilePath = getLogFilePath();

            File logfile = new File(logFilePath);

            mLog.i(TAG, "logfile exists= " + logfile.exists());
            mLog.i(TAG, "logfile url= " + logfile.getAbsolutePath());

            FileWriter fw;
            //logfile over 4 MB will copy to backup file (4 MB = 4194304  Byte)
//            if (logfile.length() > 5 * 1024 * 1024) {
//                fw = new FileWriter(logFilePath);
//            } else {
                fw = new FileWriter(logFilePath, true);
//            }

            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("[" + getCurrentTimeStamp() + "] " + writeData);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            mLog.e(TAG, String.format("PID[%d]:  %s", android.os.Process.myPid(), e.getMessage()));
        }
    }

    static private String getLogFilePath() throws IOException {
        int index_log = 0;
        String logFilePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + toDayLogFileName() + "_Log_" + String.valueOf(index_log) + ".txt";
        File logfile = new File(logFilePath);
        if (!logfile.exists()) {
            logfile.createNewFile();
        } else {
            while (logfile.exists() && logfile.length() >= 5 * 1024 * 1024) {
                index_log += 1;
                logFilePath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                        + toDayLogFileName() + "_Log_" + String.valueOf(index_log) + ".txt";
                logfile = new File(logFilePath);
            }
            if (!logfile.exists()) {
                logfile.createNewFile();
            }
        }

        return logFilePath;
    }

    static private void copyFile(File src, File dst) {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (IOException e) {
            mLog.e(TAG, String.format("PID[%d]:  %s", android.os.Process.myPid(), e.getMessage()));
        }
    }

    static private String toDayLogFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    static private String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
}
