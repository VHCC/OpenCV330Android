package acl.siot.opencvwpc20191007noc.wbSocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Map;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.util.MLog;

import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_FRS_VERIFY_UN_RECOGNIZED;
import static acl.siot.opencvwpc20191007noc.vfr.upload.VFRVerifyFragment.staticVerifySwitch;

/**
 * Created by IChen.Chu on 2020/5/14
 */
public class FrsWebSocketClient  extends WebSocketClient {

    private static final MLog mLog = new MLog(false);
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
        mLog.i(TAG, "opened connection" );
    }

    public static JSONObject staticPersonRecognitionResult;
    public static JSONObject staticPersonInfo;

    @Override
    public void onMessage(String message) {
        mLog.i(TAG, "received: " + message );
        try {
            staticPersonRecognitionResult = new JSONObject(message);
            int type = staticPersonRecognitionResult.getInt("type");
            switch (type) {
                case 0: // unRecognized
                    if (staticVerifySwitch) {
                        AppBus.getInstance().post(new BusEvent("verify unRecognized", APP_CODE_FRS_VERIFY_UN_RECOGNIZED));
                    }
                    staticVerifySwitch = false;
//                    AppBus.getInstance().post(new BusEvent("face detect done", 2001));
                    break;
                case 1: // success
                    if (staticVerifySwitch) {
                        staticPersonInfo = staticPersonRecognitionResult.getJSONObject("person_info");
                        AppBus.getInstance().post(new BusEvent("verify successfully", APP_CODE_FRS_VERIFY_SUCCESS));
                    }
                    staticVerifySwitch = false;
//                    AppBus.getInstance().post(new BusEvent("face detect done", 2001));
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mLog.i(TAG, "Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason  );
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        mLog.i(TAG, "error= " + ex);
    }
}
