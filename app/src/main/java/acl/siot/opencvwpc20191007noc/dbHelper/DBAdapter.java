package acl.siot.opencvwpc20191007noc.dbHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

import acl.siot.opencvwpc20191007noc.util.MLog;

/**
 * Created by IChen.Chu on 2019/1/31
 */
public class DBAdapter {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private DBWorker mDbWorker;
    private static DBAdapter mDbAdapter = null;

    public static DBAdapter getInstance() {
        if (mDbAdapter == null) {
            mDbAdapter = new DBAdapter();
        }
        return mDbAdapter;
    }

    public DBAdapter() {
        mDbWorker = new DBWorker(ActivityUtils.getTopActivity());
        try {
//            mDbWorker.initDB();
            mDbWorker.initialDB();
        } catch (IOException e) {
            mLog.e(TAG, e.toString());
            e.printStackTrace();
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
        }
    }

    public interface WorkerTask {
        void insertData();

        void reportData();
    }

    public void addData() {
        mDbWorker.insertData();
    }

    public void uploadData() {
        mDbWorker.reportData();
    }

    // base manipulate.
    public void createDatabase(String DBFileName, String dbVersion) throws SQLException {
        mDbWorker.importDB(DBFileName, dbVersion);
    }

    public void close() {
        mDbWorker.close();
    }


    // ---------------------------------------------------------------
    public void checkDBAllTables() {
        mLog.d(TAG, "======== Check DB Tables ========");

        Cursor c = mDbWorker.getDBInstance().rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
//                mLog.d(TAG, "table name= " + c.getString(0));
                mLog.d(TAG, "table:> [" + c.getString(0) + "], isExist= [" + isTableExists(c.getString(0)) + "]");
                if (!c.getString(0).equals("android_metadata") && isTableExists(c.getString(0))) {
                    checkAllDataFromTable(c.getString(0));
                }
//                mLog.d(TAG, "Supplier isExist= [" + isTableExists("Supplier") + "]");
                c.moveToNext();
            }
        }
        mLog.d(TAG, "======== Check DB Tables Done ========");
    }

    private void checkAllDataFromTable(String tableName) {
        Cursor c = mDbWorker.getDBInstance().rawQuery("SELECT * FROM " + tableName, null);
        String displayTitle = "";
        int countIndex = 1;
        if (c.moveToFirst()) {
            for (int index = 0; index < c.getColumnCount(); index++) {
                displayTitle += c.getColumnName(index) + "= %s, ";
            }
            while (!c.isAfterLast()) {
                String displayString[] = new String[c.getColumnCount()];
                for (int index = 0; index < c.getColumnCount(); index++) {
                    if (c.getColumnName(index).equals(DBConstants.TableDetectInfo.KEY_FACE_BASE64)) {
                        displayString[index] = "Face_base64";
//                        mLog.d(TAG, "face:> " + c.getBlob(index));
//                        mLog.d(TAG, "face:> " + c.getString(index));
//                        String encoded = Base64.encodeToString(c.getBlob(index), Base64.NO_WRAP);
//                        mLog.d(TAG, "face base64:> " + encoded);

//                        final File path = Environment.getExternalStoragePublicDirectory(
//                                //Environment.DIRECTORY_PICTURES
//                                Environment.DIRECTORY_DOWNLOADS);
//
//                        // Make sure the path directory exists.
//                        if (!path.exists()) {
//                            // Make it, if it doesn't exit
//                            path.mkdirs();
//                        }
//
//                        final File file = new File(path, "face_" + index + ".txt");
//                        mLog.d(TAG, "path:> " +path);
//
//                        // Save your stream, don't forget to flush() it before closing it.
//
//                        try {
//                            file.createNewFile();
//                            FileOutputStream fOut = new FileOutputStream(file);
//                            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//                            myOutWriter.append(c.getString(index));
//
//                            myOutWriter.close();
//
//                            fOut.flush();
//                            fOut.close();
//                        } catch (IOException e) {
//                            Log.e("Exception", "File write failed: " + e.toString());
//                        }

                    } else {
                        displayString[index] = c.getString(index);
                    }
                }
                mLog.d(TAG, " - " + countIndex + ", " + String.format(displayTitle, displayString));
                countIndex++;
                c.moveToNext();
            }
        }
    }

    public String getDataVersion() throws IOException {
        if (isTableExists(DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION)) {
            Cursor cursor = mDbWorker.getDBInstance().rawQuery("SELECT * FROM " + DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION, null);
            if (cursor.getCount() == 0) {
                // device not exist in database
                cursor.close();
                return "empty!";
            } else {
                cursor.moveToNext();
                String dbVersion = cursor.getString(cursor.getColumnIndex(DBConstants.TableDataVersion.KEY_DATA_VERSION));
                cursor.close();
                return dbVersion;
            }
        } else {
            return "empty!";
        }
    }

    public void setDataVersion(String data_version) {
        mLog.d(TAG, "setDataVersion, " + data_version);
        try {
            String SQL_setdb_version = "SELECT * FROM " + DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION;
            Cursor cursor = mDbWorker.getDBInstance().rawQuery(SQL_setdb_version, null);
            if (cursor.getCount() == 0) {
                // device not exist in database
                ContentValues insert_db_version_cv = new ContentValues(2);
                insert_db_version_cv.put(DBConstants.TableDataVersion.KEY_ID_PRIMARY, DBConstants.TableDataVersion.DATA_VERSION_ID);
                insert_db_version_cv.put(DBConstants.TableDataVersion.KEY_DATA_VERSION, data_version);
                mLog.d(TAG, " - insert, " + DBConstants.TableDataVersion.KEY_ID_PRIMARY + ":> " + DBConstants.TableDataVersion.DATA_VERSION_ID + ", " + DBConstants.TableDataVersion.KEY_DATA_VERSION + ":> " + data_version);
//            mLog.d(TAG, "mDb isOpen= " + mDb.isOpen());
                mDbWorker.getDBInstance().insert(DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION, null, insert_db_version_cv);
                cursor.close();
            } else {
                cursor.moveToNext();
                mLog.d(TAG, " - update:> " + DBConstants.TableDataVersion.KEY_ID_PRIMARY + ":> " + cursor.getString(0) +
                        ", " + DBConstants.TableDataVersion.KEY_DATA_VERSION + ":> " + cursor.getString(1));
                ContentValues update_db_version_cv = new ContentValues(1);
                update_db_version_cv.put(DBConstants.TableDataVersion.KEY_DATA_VERSION, data_version);
                mDbWorker.getDBInstance().update(DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION, update_db_version_cv,
                        DBConstants.TableDataVersion.KEY_ID_PRIMARY + "=" + cursor.getString(0), null);
                cursor.close();
            }
            mLog.d(TAG, "data version:> " + getDataVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTableExists(String tableName) {
        if (tableName == null || mDbWorker.getDBInstance() == null || !mDbWorker.getDBInstance().isOpen()) {
            return false;
        }
        Cursor cursor = mDbWorker.getDBInstance().rawQuery("SELECT 1 FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
//        mLog.w(TAG, " - isTableExists, [" + tableName + "], " + (count > 0));
        return count > 0;
    }

    public Cursor getTableData(String tableName) {
        Cursor c = mDbWorker.getDBInstance().rawQuery("SELECT * FROM " + tableName, null);
        return c;
    }
}
