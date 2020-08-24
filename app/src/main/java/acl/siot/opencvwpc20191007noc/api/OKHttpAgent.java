package acl.siot.opencvwpc20191007noc.api;


import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.util.MLog;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.App.staticFRSSessionID;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_LOGIN_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.RequestCode.APP_CODE_UPDATE_IMAGE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.URLConstants.FRS_SERVER_URL;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.staticPersonsArray;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.staticPersonsEmployeeNoArray;
import static acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment.isGetStaticPersonsEmployeeNoArray;


public class OKHttpAgent {
    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Singleton
     */
    private static OKHttpAgent mOKHttpAgent;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    /*  */
    private final OkHttpClient mClient = new OkHttpClient();
    private IRequestInterface mIRequestInterface;

    private OKHttpAgent() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(5000, TimeUnit.MILLISECONDS);
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
            try {
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
                mIRequestInterface.onRequestFail(e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage());
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
                                mIRequestInterface.onRequestFail(jsonObj.getString("message"));
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
//            mLog.d(TAG, "PostFRSThread@" + this.hashCode() + ", request= " + json);
            RequestBody body = RequestBody.create(JSON, json);

            Request request = new Request.Builder()
                    .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();
            try {
                Response response = mClient.newCall(request).execute();
                String result = response.body().string();
//                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
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
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            } catch (JSONException e) {
                e.printStackTrace();
                mLog.e(TAG, "e= " + e.getMessage());
//                mIRequestInterface.onRequestFail(e.getMessage());
                mIRequestInterface.onRequestFail(e.getMessage(), postCode);
            }
        }

        private void handleFRSResult(JSONObject jsonObj, int postCode) throws JSONException {
//            System.out.println(jsonObj.toString());
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

    protected class GetThread extends Thread {

        private HashMap mData;

        private int getCode;

        public GetThread(HashMap data, int requestCode) {
            mData = data;
            getCode = requestCode;
        }

        @Override
        public void run() {
            final String mainURL = mData.get(OKHttpConstants.APP_KEY_HTTPS_URL).toString();
            mLog.d(TAG, "get request= " + mainURL);

            Request request = new Request.Builder()
                    .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
//                    .header("Content-Type", "application/json")
                    .get()
                    .build();
            try {
                Response response = mClient.newCall(request).execute();
                String result = response.body().string();
                JSONObject jsonObj = new JSONObject(result);
//                mLog.d(TAG, "PostThread@" + this.hashCode() + ", response.code()= " + response.code());
                switch(response.code()) {
                    case 200: {
                        handleResult(jsonObj, getCode);
                    } break;
                    default: {
                        handleResult(jsonObj, getCode);
                    } break;
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

        private void handleResult(JSONObject jsonObj, int postCode) throws JSONException {
//            System.out.println(jsonObj.toString());
            writeToFile(jsonObj.toString());
//            if (AppSetting.isEngineering()) {
            if (false) {
                mIRequestInterface.onRequestSuccess(jsonObj.get(OKHttpConstants.ResponseKey.DATA).toString(), postCode);
            } else {
                mLog.d(TAG, "GetThread@" + this.hashCode() + ", response= " + jsonObj.toString(4));

                try {
                    mIRequestInterface.onRequestSuccess(jsonObj.toString(), postCode);
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

        @Override
        public void run() {

//            final String mainURL = FRS_SERVER_URL + "/persons?sessionId=" + staticFRSSessionID + "&page_size=1000&skip_pages=0";
            final String mainURL = "http://" + VFREdgeCache.getInstance().getIpAddress() + "/persons?sessionId=" + staticFRSSessionID + "&page_size=1000&skip_pages=0";
            mLog.d(TAG, "get FRS persons request= " + mainURL);
            Request request = new Request.Builder()
                    .url(mainURL)
//                    .header("Authorize", "Bearer 986da599d73c73d49becf33ca6d88c9f0ce23add.d399ad2193c10895916e3b46a8082e94")
//                    .header("Content-signUpEventType", "application/json")
//                    .header("Content-Type", "application/json")
                    .get()
                    .build();
            try {
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
            AppBus.getInstance().post(new BusEvent("login success", APP_CODE_FRS_LOGIN_SUCCESS));
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

    public synchronized void postFRSRequest(HashMap mData, int requestCode) throws IOException {
        PostFRSThread postFRSThread = new PostFRSThread(mData, requestCode);
        postFRSThread.start();
    }

    public synchronized void getFRSRequest() throws IOException {
        GetFRSThread getFRSThread = new GetFRSThread();
        getFRSThread.start();
    }

    public synchronized void getTempRequest(HashMap mData, int requestCode) throws IOException {
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
