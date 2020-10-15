package acl.siot.opencvwpc20191007noc.wbSocket;

import android.media.FaceDetector;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.cache.VFREdgeCache;
import acl.siot.opencvwpc20191007noc.util.MLog;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY_UN_RECOGNIZED;
import static acl.siot.opencvwpc20191007noc.vfr.upload.VFRVerifyFragment.staticVerifySwitch;
import static acl.siot.opencvwpc20191007noc.App.isFRServerConnected;

/**
 * Created by IChen.Chu on 2020/5/14
 */
public class FrsWebSocketClient  extends WebSocketClient {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());



    public FrsWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public FrsWebSocketClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public FrsWebSocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    public FrsWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders);
    }

    public FrsWebSocketClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        mLog.i(TAG, " + opened connection, " + handshakedata.getHttpStatusMessage() );
        isFRServerConnected = true;
    }

    public static JSONObject staticPersonRecognitionResult;
    public static JSONObject staticPersonInfo;
    public static String staticPersonID;

    @Override
    public void onMessage(String message) {

        try {
            JSONObject tempResult = new JSONObject(message);

            int type = tempResult.getInt("type");
            String channel = tempResult.getString("channel");


//            mLog.i(TAG, " *** type: " + type );
//            mLog.i(TAG, " *** channel: " + channel );
            if (VFREdgeCache.getInstance().getTabletID().equals(channel)) {
                mLog.i(TAG, " *** type: " + type );
                mLog.i(TAG, " *** channel: " + channel );
//                mLog.d(TAG, "received: " + message );
                staticPersonRecognitionResult = tempResult;

                switch (type) {
                    case 0: // unRecognized
                        if (staticVerifySwitch) {
                            AppBus.getInstance().post(new BusEvent("verify unRecognized", APP_CODE_FRS_VERIFY_UN_RECOGNIZED));
                        }
                        staticVerifySwitch = false;
//                    AppBus.getInstance().post(new BusEvent("face detect done", 2001));
                        break;
                    case 1: // success
                        JSONObject person_info = tempResult.getJSONObject("person_info");
                        mLog.i(TAG, " *** person_info: " + person_info.toString() );
                        staticPersonID = staticPersonRecognitionResult.getString("person_id");
                        if (staticVerifySwitch) {
                            staticPersonInfo = staticPersonRecognitionResult.getJSONObject("person_info");
                            AppBus.getInstance().post(new BusEvent("verify successfully", APP_CODE_FRS_VERIFY_SUCCESS));
                        }
                        staticVerifySwitch = false;
//                    AppBus.getInstance().post(new BusEvent("face detect done", 2001));
                        break;
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mLog.i(TAG, " + Connection closed by " + ( remote ? "remote peer" : "us" ) + ", Code: " + code + ", Reason: " + reason  );
        isFRServerConnected = false;
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        mLog.i(TAG, " + error= " + ex);
    }
}
