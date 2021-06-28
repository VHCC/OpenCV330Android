package acl.siot.opencvwpc20191007noc.api;


import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
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

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
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
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.staticPersonsArray;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.staticPersonsEmployeeNoArray;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.isGetStaticPersonsEmployeeNoArray;


public class OKHttpAgent {
    private static final MLog mLog = new MLog(true);
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
    private final OkHttpClient mClient;
    private OkHttpClient mClient_TPE;
    private IRequestInterface mIRequestInterface;
    OkHttpClient.Builder mBuilder;
    SSLContext ssLContext = null;

    private OKHttpAgent() {
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
//            mIRequestInterface.showLoading();
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
                JSONObject jsonObj = new JSONObject(result);
//                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200: {
                        handleResult(jsonObj, postCode);
                    } break;
                    default: {
                        handleResult(jsonObj, postCode);
                    } break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            } catch (Exception e){
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            }
        }

        private void handleResult(JSONObject jsonObj, int postCode) throws JSONException {
//            System.out.println(jsonObj.toString());
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
                        handleResult(jsonObj, postCodeTPE);
                        break;
                    }
                    default: {
                        handleResult(jsonObj, postCodeTPE);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCodeTPE);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCodeTPE);
            } catch (Exception e){
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCodeTPE);
            }
        }

        private void handleResult(JSONObject jsonObj, int postCode) throws JSONException {
//            System.out.println(jsonObj.toString());
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
                        handleResult(jsonObj, postCode);
                    } break;
                    default: {
                        handleResult(jsonObj, postCode);
                    } break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            }
        }

        private void handleResult(JSONObject jsonObj, int postCode) throws JSONException {
//            System.out.println(jsonObj.toString());
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

    protected class PostFRSThread extends Thread {
        private HashMap mData;

        private int postCode;

        public PostFRSThread(HashMap data, int requestCode) {
            mData = data;
            postCode = requestCode;
        }

        @Override
        public void run() {
//            mIRequestInterface.showLoading();
            final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
            mData.remove(OKHttpConstants.APP_KEY_HTTPS_URL);
            JSONObject jsonObjRaw = new JSONObject(mData);
            String json = null;
            try {
                json = jsonObjRaw.toString(4);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mLog.d(TAG, "PostFRSThread@" + this.hashCode() + ", request= " + json);
            RequestBody body = RequestBody.create(JSON, json);
            try {
            Request request = new Request.Builder()
                    .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();
                Response response = mClient.newCall(request).execute();
                String result = response.body().string();
                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200: {
                        JSONObject jsonObj = new JSONObject(result);
                        handleFRSResult(jsonObj, postCode);
                        break;
                    }
                    default: {
                        mIRequestInterface.onRequestFail(result, postCode);
//                        handleFRSResult(jsonObj, postCode);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                if (mIRequestInterface != null) {
                    mIRequestInterface.onRequestFail(e.getMessage(), postCode);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
//                mIRequestInterface.onRequestFail(e.getMessage());
                if (mIRequestInterface != null) {
                    mIRequestInterface.onRequestFail(e.getMessage(), postCode);
                }
            }
        }

        private void handleFRSResult(JSONObject jsonObj, int postCode) throws JSONException {
            writeToFile(jsonObj.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(jsonObj.get(OKHttpConstants.ResponseKey.DATA).toString(), postCode);
            } else {
                mLog.d(TAG, "PostFRSThread@" + this.hashCode() + ", response= " + jsonObj.toString(4));
                try {
                    mIRequestInterface.onRequestSuccess(jsonObj.toString(), postCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class PostAvaloThread extends Thread {
        private boolean switchFlag;

        public PostAvaloThread(boolean flag) {
            switchFlag = flag;
        }

        @Override
        public void run() {
//            final String mainURL = "http://192.168.4.1/websocket.json";
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
                String result = response.body().string();
                mLog.d(TAG, "PostAvaloThread@" + this.hashCode() + ", response.code()= " + response.code());
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
            }
        }
    }

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
            final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
            mLog.d(TAG, "get request= " + mainURL);
            try {
            Request request = new Request.Builder()
                    .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
//                    .header("Content-Type", "application/json")
                    .get()
                    .build();
                Response response = mClient.newCall(request).execute();
                String result = response.body().string();
                JSONObject jsonObj = new JSONObject(result);
//                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200:
                        handleResult(jsonObj, getCode);
                        break;
                    default:
                        handleResult(jsonObj, getCode);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                isThermometerServerConnected = false;
                mLog.e(TAG, "e= " + e.getMessage());
//                mIRequestInterface.onRequestFail(e.getMessage(), getCode);
            } catch (JSONException e) {
                e.printStackTrace();
                isThermometerServerConnected = false;
                mLog.e(TAG, "e= " + e.getMessage());
//                mIRequestInterface.onRequestFail(e.getMessage(), getCode);
            }
        }

        private void handleResult(JSONObject jsonObj, int getCode) throws JSONException {
//            System.out.println(jsonObj.toString());
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

    // Get info Thread
    protected class GetFRSThread extends Thread {
        public GetFRSThread() {
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
//            final String mainURL = FRS_SERVER_URL + "/persons?sessionId=" + staticFRSSessionID + "&page_size=1000&skip_pages=0";
//            final String mainURL = "http://" + VFREdgeCache.getInstance().getIpAddress() + "/persons?sessionId=" + staticFRSSessionID + "&page_size=1000&skip_pages=0";
            final String mainURL = "http://" + VFREdgeCache.getInstance().getIpAddress() + "/persons?sessionId=" + "staticFRSSessionID" + "&page_size=1000&skip_pages=0";
            mLog.d(TAG, "get FRS persons request= " + mainURL);
            try {
            Request request = new Request.Builder()
                    .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
//                    .header("Content-Type", "application/json")
                    .get()
                    .build();
                Response response = mClient.newCall(request).execute();
                String result = response.body().string();
                JSONObject jsonObj = new JSONObject(result);
                switch(response.code()) {
                    case 200: {
                        handleGetResult(jsonObj);
                        break;
                    }
                    default:
                        handleGetResult(jsonObj);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                mLog.e(TAG, "IOException= " + e.getMessage());
//                RxBus.getInstance().send(Login.RXBUS_EVENT.DB_UPDATE_REQUEST_TIMEOUT);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "JSONException= " + e.getMessage());
//                RxBus.getInstance().send(Login.RXBUS_EVENT.DB_UPDATE_REQUEST_TIMEOUT);
            }
        }

        private void handleGetResult(JSONObject jsonObj) throws JSONException {
//            mLog.w(TAG, "response= " + jsonObj);
            JSONObject personLists = (JSONObject) jsonObj.get("person_list");
            mLog.w(TAG, "total= " + personLists.get("total"));
            mLog.w(TAG, "total_pages= " + personLists.get("total_pages"));
            mLog.w(TAG, "page_index= " + personLists.get("page_index"));
            mLog.w(TAG, "page_size= " + personLists.get("page_size"));
            mLog.w(TAG, "size= " + personLists.get("size"));
            staticPersonsArray = (JSONArray) personLists.get("persons");
//            mLog.w(TAG, "personsArray= " + staticPersonsArray);
            for (int index=0; index < staticPersonsArray.length(); index ++) {
                JSONObject person = (JSONObject) staticPersonsArray.get(index);
                JSONObject personInfo = (JSONObject) person.get("person_info");
                staticPersonsEmployeeNoArray.add(personInfo.getString("employeeno"));
            }
            isGetStaticPersonsEmployeeNoArray = true;
//            AppBus.getInstance().post(new BusEvent("login success", APP_CODE_FRS_LOGIN_SUCCESS));
//            mLog.i(TAG, "staticPersonsEmployeeNoArray length= " + staticPersonsEmployeeNoArray.size());
            try {
//                mIRequestInterface.onRequestDBUpdateSuccess(jsonObj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void postRequest(HashMap mData, int requestCode) throws IOException {
//        mLog.d(TAG, "request post= " + mData.toString());
        PostThread postThread = new PostThread(mData, requestCode);
        postThread.start();
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

    public synchronized void postUploadFileRequest(HashMap mData, int requestCode) throws IOException {
//        mLog.d(TAG, "request post= " + mData.toString());
        PostUploadFileThread postUploadFileThread = new PostUploadFileThread(mData, requestCode);
        postUploadFileThread.start();
    }

    public synchronized void postFRSRequest(HashMap mData, int requestCode) throws IOException {
        PostFRSThread postFRSThread = new PostFRSThread(mData, requestCode);
        postFRSThread.start();
    }

    public synchronized void postAvaloWebsocket(boolean flag) throws IOException {
        PostAvaloThread avaloThread = new PostAvaloThread(flag);
        avaloThread.start();
    }

    public synchronized void getFRSRequest() throws IOException {
        GetFRSThread getFRSThread = new GetFRSThread();
        getFRSThread.start();
    }

    public synchronized void getRequest(HashMap mData, int requestCode) throws IOException {
        GetThread getThread = new GetThread(mData, requestCode);
        getThread.start();
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
