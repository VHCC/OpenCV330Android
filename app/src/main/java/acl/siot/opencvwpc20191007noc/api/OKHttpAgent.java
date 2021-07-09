package acl.siot.opencvwpc20191007noc.api;


import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.NullHostNameVerifier;
import acl.siot.opencvwpc20191007noc.util.NullX509TrustManager;
import acl.siot.opencvwpc20191007noc.vms.VmsLogUploadFile;
import androidx.annotation.RequiresApi;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;


public class OKHttpAgent {
    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Singleton
     */
    private static OKHttpAgent mOKHttpAgent;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static final MediaType URL_ENCODED
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    /*  */
//    private final OkHttpClient mClient = new OkHttpClient();
    private OkHttpClient mClient;
    private OkHttpClient mClient_getTemp;
    private OkHttpClient mClient_postTemp;
    private OkHttpClient mClient_postConfig;
    private OkHttpClient mClient_TPE;
    private IRequestInterface mIRequestInterface;
    OkHttpClient.Builder mBuilder;
    SSLContext ssLContext = null;

    private OKHttpAgent() {
        mLog.d(TAG, " *** OKHttpAgent *** ");
        mBuilder = new OkHttpClient.Builder();
        try {
            ssLContext = SSLContext.getInstance("TLS");
            ssLContext.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        mBuilder.sslSocketFactory(ssLContext.getSocketFactory());
        mBuilder.hostnameVerifier(new NullHostNameVerifier());
        mBuilder.connectTimeout(5000, TimeUnit.MILLISECONDS);
        mClient = mBuilder.build();
        mClient_getTemp = mBuilder.build();
        mClient_postTemp = mBuilder.build();
        mClient_postConfig = mBuilder.build();
    }

    public static OKHttpAgent getInstance() {
        if (mOKHttpAgent == null) {
            mOKHttpAgent = new OKHttpAgent();
        }
        return mOKHttpAgent;
    }


    protected class PostThread extends Thread {
        private HashMap mData;
        private int postCode;

        public PostThread(HashMap data, int requestCode) {
            mData = data;
            postCode = requestCode;
        }

        @Override
        public void run() {
            try {
                final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
                mData.remove(OKHttpConstants.APP_KEY_HTTPS_URL);
                JSONObject jsonObjRaw = new JSONObject(mData);
                String json = null;
                try {
                    json = jsonObjRaw.toString(4);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mLog.d(TAG, "PostThread@" + this.hashCode() + ", request= " + json);
                RequestBody body = RequestBody.create(JSON, json);

                Request request = new Request.Builder()
                    .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();
                Response response = mClient.newCall(request).execute();
                String result = response.body().string();
                mLog.d(TAG, "[POST] result:> " + result);
                JSONObject jsonObj = new JSONObject(result);
                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200: {
                        response.close();
                        handlePostResult(jsonObj, postCode);
                    } break;
                    default: {
                        handlePostResult(jsonObj, postCode);
                    } break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "IOException, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "JSONException, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            } catch (Exception e){
                e.printStackTrace();
                mLog.e(TAG, "Exception, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            }
        }

        private void handlePostResult(JSONObject jsonObj, int postCode) throws JSONException {
            writeToFile(jsonObj.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(jsonObj.get(OKHttpConstants.ResponseKey.DATA).toString(), postCode);
            } else {
                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response= " + jsonObj.toString(4));

                try {
                    if (jsonObj.toString().contains("code")) {
                        int errorCode = jsonObj.getInt("code");
//                        mLog.w(TAG, "errorCode= " + errorCode);
                        switch (errorCode) {
                            case 0:
                                mIRequestInterface.onRequestSuccess(jsonObj.toString(), postCode);
                                break;
                            default:
                                mIRequestInterface.onRequestFail(jsonObj.getString("message"), postCode);
                                break;
                        }
                    }
                    // TODO
                    // watch out the situations whose errorCode are not 0.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class PostTPEThread extends Thread {
        private HashMap mData;
        private int postCodeTPE;

        public PostTPEThread(HashMap data, int requestCode) {
            mData = data;
            postCodeTPE = requestCode;
        }

        @Override
        public void run() {
            try {
                final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
                mData.remove(OKHttpConstants.APP_KEY_HTTPS_URL);
                JSONObject jsonObjRaw = new JSONObject(mData);
                String json = null;
                try {
                    json = jsonObjRaw.toString(4);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mLog.d(TAG, "PostTPEThread@" + this.hashCode() + ", request= " + json);
                RequestBody body = RequestBody.create(JSON, json);

                Request request = new Request.Builder()
                        .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
                        .header("Content-Type", "application/json")
                        .post(body)
                        .build();
                Response response = mClient_TPE.newCall(request).execute();
                String result = response.body().string();
                JSONObject jsonObj = new JSONObject(result);
//                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200: {
                        response.close();
                        handlePostTPEResult(jsonObj, postCodeTPE);
                        break;
                    }
                    default: {
                        response.close();
                        handlePostTPEResult(jsonObj, postCodeTPE);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "IOException, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCodeTPE);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "JSONException, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCodeTPE);
            } catch (Exception e){
                e.printStackTrace();
                mLog.e(TAG, "Exception, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCodeTPE);
            }
        }

        private void handlePostTPEResult(JSONObject jsonObj, int postCode) throws JSONException {
            writeToFile(jsonObj.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(jsonObj.get(OKHttpConstants.ResponseKey.DATA).toString(), postCode);
            } else {
                mLog.d(TAG, "PostTPEThread@" + this.hashCode() + ", response= " + jsonObj.toString(4));

                try {
                    if (jsonObj.toString().contains("code")) {
                        int errorCode = jsonObj.getInt("code");
                        mLog.w(TAG, "errorCode= " + errorCode);
                        switch (errorCode) {
                            case 0:
                                mIRequestInterface.onRequestSuccess(jsonObj.toString(), postCode);
                                break;
                            default:
                                mIRequestInterface.onRequestFail(jsonObj.getString("message"), postCode);
                                break;
                        }
                    }
                    // watch out the situations whose errorCode are not 0.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class PostUploadFileThread extends Thread {
        private HashMap mData;
        private int postCode;

        public PostUploadFileThread(HashMap data, int requestCode) {
            mData = data;
            postCode = requestCode;
        }

        @Override
        public void run() {
            final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
            mLog.d(TAG, "PostUploadFileThread, mainURL:> " +mainURL);
            mData.remove(OKHttpConstants.APP_KEY_HTTPS_URL);
            File file = (File) mData.get(VmsLogUploadFile.API_KEY_UPLOAD_LOG_FILE);

            try {
                RequestBody formBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("uploadFile", file.getName(),
                                RequestBody.create(MediaType.parse("text/plain"), file))
                        .addFormDataPart("kioskUUID", VMSEdgeCache.getInstance().getVmsKioskUuid())
                        .addFormDataPart("fileSize", String.valueOf(file.length()))
                        .build();
                Request request = new Request.Builder().url(mainURL).post(formBody).build();
                Response response = mClient.newCall(request).execute();

                String result = response.body().string();
                JSONObject jsonObj = new JSONObject(result);
//                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200: {
                        response.close();
                        handleResult(jsonObj, postCode);
                    } break;
                    default: {
                        response.close();
                        handleResult(jsonObj, postCode);
                    } break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "IOException, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "JSONException, e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            }
        }

        private void handleResult(JSONObject jsonObj, int postCode) throws JSONException {
            writeToFile(jsonObj.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(jsonObj.get(OKHttpConstants.ResponseKey.DATA).toString(), postCode);
            } else {
                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response= " + jsonObj.toString(4));

                try {
                    if (jsonObj.toString().contains("code")) {
                        int errorCode = jsonObj.getInt("code");
                        mLog.w(TAG, "errorCode= " + errorCode);
                        switch (errorCode) {
                            case 0:
                                mIRequestInterface.onRequestSuccess(jsonObj.toString(), postCode);
                                break;
                            default:
                                mIRequestInterface.onRequestFail(jsonObj.getString("message"), postCode);
                                break;
                        }
                    }
                    // TODO
                    // watch out the situations whose errorCode are not 0.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final Object mSyncObject_AvaloSocket = new Object();

    protected class PostAvaloWebSocketThread extends Thread {
        private boolean switchFlag;

        public PostAvaloWebSocketThread(boolean flag) {
            switchFlag = flag;
        }

        @Override
        public void run() {
            synchronized (mSyncObject_AvaloSocket){
                final String mainURL = "http://" + VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host() + "/websocket.json";
                mLog.d(TAG, "PostAvaloThread@" + this.hashCode());
                String bodyString = switchFlag ? "websocket,1" : "websocket,0";
                RequestBody body = RequestBody.create(URL_ENCODED, bodyString);
                try {
                    Request request = new Request.Builder()
                            .url(mainURL)
                            .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                            .post(body)
                            .build();
                    Response response = mClient.newCall(request).execute();
                    mLog.d(TAG, "PostAvaloWebSocketThread@" + this.hashCode() + ", response.code()= " + response.code());
                } catch (IOException e) {
                    e.printStackTrace();
                    mLog.e(TAG, "PostAvaloThread@" + this.hashCode() + ", IOException, e= " + e.getMessage());
                }
            }
        }
    }

    private final Object mSyncObject_GetTemp = new Object();

    protected class GetThread extends Thread {
        private HashMap mData;

        private int getCode;

        public GetThread(HashMap data, int requestCode) {
            mData = data;
            getCode = requestCode;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            synchronized (mSyncObject_GetTemp){
                final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
                mLog.d(TAG, "request [GET]:> " + mainURL);
                try {
                    Request request = new Request.Builder()
                            .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
//                    .header("Content-Type", "application/json")
                            .get()
                            .build();
                    Response response = mClient_getTemp.newCall(request).execute();
                    String result = response.body().string();
                    mLog.d(TAG, " - result:> " + result);
                    JSONObject jsonObj = new JSONObject(result);
//                mLog.d(TAG, "GetThread@" + this.hashCode() + ", response.code()= " + response.code());
                    switch(response.code()) {
                        case 200:
                            response.close();
                            getHandleResult(jsonObj, getCode);
                            break;
                        default:
                            response.close();
                            getHandleResult(jsonObj, getCode);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (null != e && e.toString().contains("to connect")) {
                        isThermometerServerConnected = false;
                    }
                    VMSEdgeCache.getInstance().setVms_kiosk_video_type(0);
                    mLog.e(TAG, "GetThread@" + this.hashCode() + ", IOException, e= " + e.getMessage() + ", isThermometerServerConnected:> " + isThermometerServerConnected);
//                mIRequestInterface.onRequestFail(e.getMessage(), getCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (null != e && e.toString().contains("to connect")) {
                        isThermometerServerConnected = false;
                    }
                    VMSEdgeCache.getInstance().setVms_kiosk_video_type(0);
                    mLog.e(TAG, "GetThread@" + this.hashCode() + ", JSONException, e= " + e.getMessage() + ", isThermometerServerConnected:> " + isThermometerServerConnected);
//                mIRequestInterface.onRequestFail(e.getMessage(), getCode);
                }
            }
        }

        private void getHandleResult(JSONObject jsonObj, int getCode) throws JSONException {
            writeToFile(jsonObj.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(jsonObj.get(OKHttpConstants.ResponseKey.DATA).toString(), getCode);
            } else {
                mLog.d(TAG, "GetThread@" + this.hashCode() + ", response= " + jsonObj.toString(4) + ", getCode= " + getCode);
                try {
                    mIRequestInterface.onRequestSuccess(jsonObj.toString(), getCode);
                    // TODO
                    // watch out the situations whose errorCode are not 0.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class PostAvaloConfigThread extends Thread {
        private HashMap mData;
        private int postCode;

        public PostAvaloConfigThread(HashMap data, int requestCode) {
            mData = data;
            postCode = requestCode;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            try {
                final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
                mData.remove(OKHttpConstants.APP_KEY_HTTPS_URL);
                JSONObject jsonObjRaw = new JSONObject(mData);
                String json = jsonObjRaw.toString(0);
                mLog.d(TAG, "PostAvaloConfigThread@" + this.hashCode() + ", request= " + json);
                RequestBody body = RequestBody.create(JSON, "");

                Request request = new Request.Builder()
                        .url(mainURL)
                        .header("Content-Type", "application/json")
                        .post(body)
                        .build();
                Response response = mClient_postConfig.newCall(request).execute();
                String result = response.body().string();
                mLog.d(TAG, "[POST] result:> " + result);
                JSONObject jsonObj = new JSONObject(result);
                mLog.d(TAG, "PostAvaloConfigThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200: {
                        response.close();
                        handleAvaloPostResult(jsonObj, postCode);
                        break;
                    }
                    default: {
                        mIRequestInterface.onRequestFail(result, postCode);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "PostAvaloConfigThread@" + this.hashCode() + ", IOException, e= " + e.getMessage() + ", isThermometerServerConnected:> " + isThermometerServerConnected);
                if (mIRequestInterface != null) {
                    mIRequestInterface.onRequestFail(e.getMessage(), postCode);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "PostAvaloConfigThread@" + this.hashCode() + ", JSONException, e= " + e.getMessage() + ", isThermometerServerConnected:> " + isThermometerServerConnected);
                if (mIRequestInterface != null) {
                    mIRequestInterface.onRequestFail(e.getMessage(), postCode);
                }
            }
        }

        private void handleAvaloPostResult(JSONObject jsonObj, int postCode) throws JSONException {
            writeToFile(jsonObj.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(jsonObj.get(OKHttpConstants.ResponseKey.DATA).toString(), postCode);
            } else {
                mLog.d(TAG, "PostAvaloConfigThread@" + this.hashCode() + ", response= " + jsonObj.toString(4));
                try {
                    mIRequestInterface.onRequestSuccess(jsonObj.toString(), postCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final Object mSyncObject_PostAvaloTempThread = new Object();
    private static boolean canPostTemp = true;

    protected class PostAvaloTempThread extends Thread {
        private HashMap mData;
        private int postCode;


        public PostAvaloTempThread(HashMap data, int requestCode) {
            mData = data;
            postCode = requestCode;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            synchronized (mSyncObject_PostAvaloTempThread){
                canPostTemp = false;
                try {
                    final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
//                    mData.remove(OKHttpConstants.APP_KEY_HTTPS_URL);
                    JSONObject jsonObjRaw = new JSONObject(mData);
                    String json = jsonObjRaw.toString(0);
//                mLog.d(TAG, "PostAvaloTempThread@" + this.hashCode() + ", request= " + json);
                    RequestBody body = RequestBody.create(JSON, "");

                    Request request = new Request.Builder()
                            .url(mainURL)
                            .header("Content-Type", "application/json")
                            .post(body)
                            .build();
                    Response response = mClient_postTemp.newCall(request).execute();
                    String result = response.body().string();
                mLog.d(TAG, "[POST] result:> " + result);
//                mLog.d(TAG, "PostAvaloTempThread@" + this.hashCode() + ", response.code()= " + response.code());
                    if (null == result) {
                        return;
                    }
                    if (result.trim().equals("")) {
                        return;
                    }
                    if (result.length() > 5) {
                        return;
                    }
                    switch(response.code()) {
                        case 200: {
                            response.close();
                            handleAvaloPostTempResult(result, postCode);
                            break;
                        }
                        default: {
                            mIRequestInterface.onRequestFail(result, postCode);
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mLog.e(TAG, "PostAvaloConfigThread@" + this.hashCode() + ", IOException, e= " + e.getMessage() + ", isThermometerServerConnected:> " + isThermometerServerConnected);
                    if (mIRequestInterface != null) {
                        mIRequestInterface.onRequestFail(e.getMessage(), postCode);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mLog.e(TAG, "PostAvaloConfigThread@" + this.hashCode()  + ", JSONException, e= " + e.getMessage() + ", isThermometerServerConnected:> " + isThermometerServerConnected);
                    if (mIRequestInterface != null) {
                        mIRequestInterface.onRequestFail(e.getMessage(), postCode);
                    }
                } finally {
                    canPostTemp = true;

                }
            }
        }

        private void handleAvaloPostTempResult(String result, int postCode) throws JSONException {
            writeToFile(result.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(result, postCode);
            } else {
                mLog.d(TAG, "PostAvaloTempThread@" + this.hashCode() + ", response= " + result);
                try {
                    mIRequestInterface.onRequestSuccess(result.toString(), postCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // =============== Request Interface ============

    public synchronized void postRequest(HashMap mData, int requestCode) throws IOException {
        mLog.d(TAG, "request [POST]:> " + mData.toString());
        PostThread postThread = new PostThread(mData, requestCode);
        postThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized void postAvaloTempRequest(HashMap mData, int requestCode) throws IOException {
        PostAvaloTempThread postAvaloTempThread = new PostAvaloTempThread(mData, requestCode);
        postAvaloTempThread.start();
    }
    public synchronized void postAvaloConfigRequest(HashMap mData, int requestCode) throws IOException {
        if (true) {
            mLog.d(TAG, "request [POST]:> " + mData.toString());
            PostAvaloConfigThread postAvaloConfigThread = new PostAvaloConfigThread(mData, requestCode);
            postAvaloConfigThread.start();
        }
    }

    public synchronized void postUploadFileRequest(HashMap mData, int requestCode) throws IOException {
        PostUploadFileThread postUploadFileThread = new PostUploadFileThread(mData, requestCode);
        postUploadFileThread.start();
    }

    public synchronized void postAvaloWebsocket(boolean flag) throws IOException {
        PostAvaloWebSocketThread avaloThread = new PostAvaloWebSocketThread(flag);
        avaloThread.start();
    }

    public synchronized void getRequest(HashMap mData, int requestCode) throws IOException {
        GetThread getThread = new GetThread(mData, requestCode);
        getThread.start();
    }

    public synchronized void postTPERequest(HashMap mData, int requestCode) throws IOException {
        if (mClient_TPE == null && VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable()) {
            OkHttpClient.Builder mBuilder_TPE = new OkHttpClient.Builder();
            mBuilder_TPE.authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    mLog.d(TAG, " *** authenticate *** ");
                    String credential = Credentials.basic(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_account(), VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_password());
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
            });
            try {
                ssLContext = SSLContext.getInstance("TLS");
                ssLContext.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            mBuilder.sslSocketFactory(ssLContext.getSocketFactory());
            mBuilder.hostnameVerifier(new NullHostNameVerifier());
            mBuilder.connectTimeout(5000, TimeUnit.MILLISECONDS);
            mClient_TPE = mBuilder.build();
        }
        PostTPEThread postTPEThread = new PostTPEThread(mData, requestCode);
        postTPEThread.start();
    }

    public interface IRequestInterface {
        void onRequestSuccess(String result, int requestCode);

        void onRequestFail(String errorResult);

        void onRequestFail(String errorResult, int requestCode);

    }

    public void setRequestListener(IRequestInterface listener) {
        mIRequestInterface = listener;
    }

    public void writeToFile(String data) {
        // Get the directory for the user's public pictures directory.
        final File path = Environment.getExternalStoragePublicDirectory(
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DOWNLOADS);
        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "log.txt");
        // Save your stream, don't forget to flush() it before closing it.

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
