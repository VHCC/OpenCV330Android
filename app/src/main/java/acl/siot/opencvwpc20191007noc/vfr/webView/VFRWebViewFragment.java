package acl.siot.opencvwpc20191007noc.vfr.webView;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.just.agentweb.AbsAgentWebSettings;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebConfig;
import com.just.agentweb.DefaultDownloadImpl;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.IAgentWebSettings;
import com.just.agentweb.WebListenerManager;

import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.cache.VFRThermometerCache;
import acl.siot.opencvwpc20191007noc.util.MLog;
import im.delight.android.webview.AdvancedWebView;

/**
 * Created by IChen.Chu on 2020/12/18
 * A fragment to show WebView page.
 */
public class VFRWebViewFragment extends Fragment implements AdvancedWebView.Listener  {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    // Handler
    private Handler mHandler = new MainHandler();
    private Runnable mFragmentRunnable = new FragmentRunnable();

    // Listener
    private OnHomeFragmentInteractionListener mHomeFragmentListener;

    // View
    TextView appVersion;
    WebView webview;

    public VFRWebViewFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of WebViewFragment.
     */
    public static VFRWebViewFragment newInstance() {
        VFRWebViewFragment fragment = new VFRWebViewFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    LinearLayout linearLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vfr_fragment_web_view, container, false);
        appVersion = rootView.findViewById(R.id.appVersion);
        linearLayout = rootView.findViewById(R.id.linearLayout);
//        webview = rootView.findViewById(R.id.webview);
        initViewsFeature();

        mWebView = (AdvancedWebView) rootView.findViewById(R.id.webview);
        mWebView.setListener(getActivity(), this);
        mWebView.setMixedContentAllowed(false);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.loadUrl("http://" + VFRThermometerCache.getInstance().getIpAddress() + "/temperature.html#english");

//        mAgentWeb = AgentWeb.with(this)
//                .setAgentWebParent((LinearLayout) linearLayout, new LinearLayout.LayoutParams(-1, -1))
//                .useDefaultIndicator()
//                .createAgentWeb()
//                .ready()
//                .go("http://192.168.4.1/temperature.html#english");

//        AgentWebConfig.debug();
        return rootView;
    }

    protected AgentWeb mAgentWeb;
    private AdvancedWebView mWebView;

    private void initViewsFeature() {
//        appVersion.setText("WebView");

//        WebSettings webSettings = webview.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//
//        webview.setWebViewClient(new WebViewClient());
//        webview.loadUrl("http://192.168.4.1/temperature.html");



        // AgentWeb 没有把WebView的功能全面覆盖 ，所以某些设置 AgentWeb 没有提供 ， 请从WebView方面入手设置。
//        mAgentWeb.getWebCreator().getWebView().setOverScrollMode(WebView.OVER_SCROLL_NEVER);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(String url) {

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }

    // -------------------------------------------
    public interface OnHomeFragmentInteractionListener {
        void onShowEnd();
    }

    public void setHomeFragmentListener(OnHomeFragmentInteractionListener listener) {
        mHomeFragmentListener = listener;
    }

    // -------------------------------------------
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private class FragmentRunnable implements Runnable {

        @Override
        public void run() {
            if (mHomeFragmentListener != null) {
                mHomeFragmentListener.onShowEnd();
            }
        }
    }

}
