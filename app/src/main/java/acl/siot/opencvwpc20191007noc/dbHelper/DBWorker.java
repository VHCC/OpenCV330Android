package acl.siot.opencvwpc20191007noc.dbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.api.OKHttpConstants;
import acl.siot.opencvwpc20191007noc.api.detectInfo.DetectInfo;
import acl.siot.opencvwpc20191007noc.api.updateImage.UpdateImage;
import acl.siot.opencvwpc20191007noc.util.MLog;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.DB_CODE_INSERT_DETECT_INFO_SUCCESS;
import static acl.siot.opencvwpc20191007noc.dbHelper.DBConstants.DB_VERSION;

/**
 * Created by IChen.Chu on 2019/1/31
 */
public class DBWorker extends SQLiteOpenHelper implements DBAdapter.WorkerTask {
    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());


    // Constants
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private final static String DB_NAME = "vfr.db";// Database name

    private final Context mContext;
    private SQLiteDatabase mDataBase;

    public DBWorker(Context context) {
        super(context, DB_NAME, null, DB_VERSION);// 1? Its database Version
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        mLog.d(TAG, "DB_PATH:> " + DB_PATH + ", DB_VERSION:> " + DB_VERSION);
        this.mContext = context;
    }

    public SQLiteDatabase getDBInstance() {
        return mDataBase;
    }

    public void initDB() throws IOException, SQLException {
        mLog.d(TAG, "initDB... ...");
        //If the database does not exist, copy it from the assets.
        if (!isFileExist()) {
            this.getWritableDatabase();
            this.close();
//            try {
//                //Copy the database from assets
////                copyDataBase();
//                mLog.w(TAG, "createDatabase database created, copy the database from assets.");
//            } catch (IOException mIOException) {
//                mLog.e(TAG, "mIOException= " + mIOException.getMessage());
//                throw new Error("ErrorCopyingDataBase");
//            }
        }
        openDataBase();
        mLog.d(TAG, "initDB... done");
    }

    public void initialDB() throws IOException {
        mLog.d(TAG, "initialDB... ...");
//        boolean dbExist = checkDataBase();
//        if (!isFileExist()) {
//            this.getReadableDatabase();
//            this.close();
//        }
        if (checkDataBase()) {
            // By calling this method here onUpgrade will be called on a
            // writable database, but only if the version number has been increased
            this.getWritableDatabase();
        } else {
            //By calling this method an empty database will be created into the                     default system path
            //of the application so we will be able to overwrite that database with our database.
            this.getWritableDatabase();
            this.close();
            checkDataBase();
//            try {
//                copyDataBase();
//            } catch (IOException e) {
//                throw new Error("Error copying database");
//            }
        }
        mLog.d(TAG, "initialDB... done");
    }

    private boolean checkDataBase() {
        mLog.d(TAG, "checkDataBase...");
        mDataBase = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            mLog.d(TAG, " *** db version:> " + mDataBase.getVersion());
        } catch (SQLiteException e) {
            mLog.d(TAG, e.toString());
        }
//        if (mDataBase != null) {
//            mDataBase.close();
//        }
//        mLog.d(TAG, "checkDB:> " + (mDataBase != null));
        return mDataBase != null ? true : false;
    }


    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean isFileExist() throws IOException {
        File dbFile = new File(DB_PATH + DB_NAME);
        mLog.v(TAG, "dbFile.exists= " + dbFile.exists() + ", dbFile= " + dbFile);
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException {
        String outFileName = DB_PATH + DB_NAME;
        mLog.d(TAG, "copyDataBase... " + outFileName);
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //Open the database, so we can query it
    private void openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        mLog.v(TAG, "openDataBase= " + mPath);
//        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, 0);
        mLog.d(TAG, " *** db version:> " + mDataBase.getVersion());
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    public void importDB(String DbFileName, String dbVersion) {
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        String sdCardDBName = DbFileName;
        String internalDBPath = "/data/" + AppUtils.getAppPackageName() + "/databases/" + DB_NAME;

        File sdPath = new File(sdDir);
        File dataDir = Environment.getDataDirectory();
        mLog.d(TAG, "importDB from [" + sdDir + sdCardDBName + "] to [" + dataDir + internalDBPath + "]");
        FileChannel source = null;
        FileChannel destination = null;
        File sdCardDBFile = new File(sdPath, sdCardDBName);
        mLog.d(TAG, "is sdCardDBFile exist= [" + sdCardDBFile.exists() + "]");
        File internalDBFile = new File(dataDir + internalDBPath);
        mLog.d(TAG, "is internalDBFile exist= [" + internalDBFile.exists() + "]");
        try {
            source = new FileInputStream(sdCardDBFile).getChannel();
            destination = new FileOutputStream(internalDBFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            openDataBase();
            mLog.w(TAG, "importDB completely, dbVersion= [" + dbVersion + "]");
            sdCardDBFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
//            mLog.e(TAG, "DBAdapter.getInstance().isTableExists(\"dbversion\")= " + DBAdapter.getInstance().isTableExists("dbversion"));

            String SQL_DROP = "DROP TABLE IF EXISTS " + DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION;
            mDataBase.execSQL(SQL_DROP);

//            mLog.e(TAG, "DBAdapter.getInstance().isTableExists(\"dbversion\")= " + DBAdapter.getInstance().isTableExists("dbversion"));

            String SQL = "CREATE TABLE IF NOT EXISTS " + DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION + " ( " +
                    "_id INTEGER, " +
                    "_version VARCHAR(10)" +
                    ");";
//        mLog.d(TAG, "SQL= " + SQL);
//        mLog.d(TAG, DB_PATH + DB_NAME + " isOpen= " + mDataBase.isOpen());
            mDataBase.execSQL(SQL);

//            mLog.e(TAG, "DBAdapter.getInstance().isTableExists(\"dbversion\")= " + DBAdapter.getInstance().isTableExists("dbversion"));
        }

//        DBAdapter.getInstance().setDBVersion(dbVersion);

//        ActivityUtils.getTopActivity().stopService((new Intent(ActivityUtils.getTopActivity(), CCMService.class)));
//        ActivityUtils.getTopActivity().stopService((new Intent(ActivityUtils.getTopActivity(), UpdateDBFileService.class)));

        mDataBase.close();
        mainHandler.sendEmptyMessage(0);
    }

    private MainHandler mainHandler = new MainHandler();

    @Override
    public void insertData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap2 = BitmapFactory.decodeResource(ActivityUtils.getTopActivity().getResources(), R.drawable.vfr_mask_ok);
//        Bitmap bitmap = ((BitmapDrawable) ActivityUtils.getTopActivity().getApplicationContext().getResources().getDrawable(R.drawable.sssaaa)).getBitmap();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        bitmap2.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] photoByteArray = baos.toByteArray();

        String encoded = Base64.encodeToString(photoByteArray, Base64.NO_WRAP);

        ContentValues cv = new ContentValues(6);
        cv.put(DBConstants.TableDetectInfo.KEY_RFID_PRIMARY, "67891234");
//        cv.put(DBConstants.TableDetectInfo.KEY_FACE_BASE64, photoByteArray);
        cv.put(DBConstants.TableDetectInfo.KEY_FACE_BASE64, encoded);
        cv.put(DBConstants.TableDetectInfo.KEY_PEOPLE_TEMPERATURE, 36.4f);
        cv.put(DBConstants.TableDetectInfo.KEY_MASK_STATUS, true);
        cv.put(DBConstants.TableDetectInfo.KEY_DETECT_TIMESTAMP, System.currentTimeMillis() / 1000);
        cv.put(DBConstants.TableDetectInfo.KEY_IS_SEND, false);

        mDataBase.insert(DBConstants.TableDetectInfo.TABLE_NAME_DETECT_INFO, null, cv);

//        AppBus.getInstance().post(new BusEvent("add data", DB_CODE_INSERT_DETECT_INFO_SUCCESS));

        HashMap<String, Object> mMap = new DetectInfo("67891234", encoded, 36.4f, true, System.currentTimeMillis() / 1000);
        try {
            OKHttpAgent.getInstance().postRequest(mMap, OKHttpConstants.RequestCode.AVALO_TEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reportData() {
        String SQL = "SELECT * FROM " + DBConstants.TableDetectInfo.TABLE_NAME_DETECT_INFO +
                " WHERE " + DBConstants.TableDetectInfo.KEY_IS_SEND +
                " == " + 0 + " ";
        Cursor device_cursor = mDataBase.rawQuery(SQL, null);
        mLog.d(TAG, "SQL:> " + SQL);
        mLog.d(TAG, "need reports:> " + device_cursor.getCount());
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            RxBus.getInstance().send(Login.RXBUS_EVENT.DB_IMPORT_COMPLETE);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        mLog.d(TAG, " *** onOpen");
    }


    @Override
    public void onCreate(SQLiteDatabase sqDB) {
        mLog.d(TAG, " *** onCreate, sqLiteDatabase= " + sqDB);
        initTable(sqDB);
    }

    private void initTable(SQLiteDatabase sqDB) {
        mLog.d(TAG, "initTable ...");
        String SQL = "";

        SQL = "CREATE TABLE IF NOT EXISTS " + DBConstants.TableDataVersion.TABLE_NAME_DATA_VERSION + " ( " +
                DBConstants.TableDataVersion.KEY_ID_PRIMARY + " INTEGER PRIMARY KEY NOT NULL, " +
                DBConstants.TableDataVersion.KEY_DATA_VERSION + " VARCHAR(10)" +
                ");";
        sqDB.execSQL(SQL);
        mLog.d(TAG, "SQL:> " + SQL);

        SQL = "CREATE TABLE IF NOT EXISTS " + DBConstants.TableDetectInfo.TABLE_NAME_DETECT_INFO + " ( " +
                DBConstants.TableDetectInfo.KEY_RFID_PRIMARY + " VARCHAR(16) NOT NULL, " +
                DBConstants.TableDetectInfo.KEY_FACE_BASE64 + " BLOB, " +
                DBConstants.TableDetectInfo.KEY_PEOPLE_TEMPERATURE + " REAL, " +
                DBConstants.TableDetectInfo.KEY_MASK_STATUS + " BOOLEAN, " +
                DBConstants.TableDetectInfo.KEY_DETECT_TIMESTAMP + " DATETIME, " +
                DBConstants.TableDetectInfo.KEY_IS_SEND + " BOOLEAN" +
                ");";
        sqDB.execSQL(SQL);
        mLog.d(TAG, "SQL:> " + SQL);


        mLog.d(TAG, "initTable ... done");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        mLog.d(TAG, " *** onUpgrade, oldVersion:> " + oldVersion + ", newVersion:> " + newVersion);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DB_VERSION);
        onCreate(sqLiteDatabase);
    }


}
