package acl.siot.opencvwpc20191007noc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.ProcessUtils;

import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.dbHelper.DBAdapter;
import acl.siot.opencvwpc20191007noc.page.subPage.SubPageEmptyFragment;
import acl.siot.opencvwpc20191007noc.page.tranform.FadeInOutBetterTransformer;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.MessageTools;
import acl.siot.opencvwpc20191007noc.vfr.adminSetting.VFRAdminPassword20210429Fragment;
import acl.siot.opencvwpc20191007noc.vfr.adminSetting.VFRAdminSettingFragment;
import acl.siot.opencvwpc20191007noc.vfr.adminSetting.VFRLanguageFragment;
import acl.siot.opencvwpc20191007noc.vfr.adminSetting.VMSAdminSettingFragment;
import acl.siot.opencvwpc20191007noc.vfr.detect.VFRDetect20210303Fragment;
import acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment;
import acl.siot.opencvwpc20191007noc.vfr.webView.VFRWebViewFragment;
import acl.siot.opencvwpc20191007noc.vfr.welcome.VFRWelcome20210308Fragment;
import pub.devrel.easypermissions.EasyPermissions;

import static acl.siot.opencvwpc20191007noc.VMSMainActivity.SectionsPagerAdapter.PAGE_DETECT;
import static acl.siot.opencvwpc20191007noc.VMSMainActivity.SectionsPagerAdapter.PAGE_WELCOME;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_RFID_DETECT_DONE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.DB_CODE_INSERT_DETECT_INFO;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.DB_CODE_INSERT_DETECT_INFO_SUCCESS;

public class VMSMainActivity extends AppCompatActivity {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private final int RC_PERMISSIONS = 9001;

    /**
     * The {@link ViewPager} will host the section contents.
     */
    private ViewPager mViewPager;

    private DBAdapter mDBAdapter;

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link androidx.fragment.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLog.d(TAG, "* onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        AppBus.getInstance().register(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.mainContainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(mSectionsPagerAdapter);
//        mViewPager.setPageTransformer(true, new ScaleInOutTransformer());
//        mViewPager.setPageTransformer(true, new FadeInOutTransformer());
        mViewPager.setPageTransformer(true, new FadeInOutBetterTransformer());
        mViewPager.setOffscreenPageLimit(5);

//        mDBAdapter = DBAdapter.getInstance();
//        mDBAdapter.setDataVersion(String.valueOf(DB_VERSION));
//        AppBus.getInstance().post(new BusEvent("add data", DB_CODE_INSERT_DETECT_INFO));
//        LocaleUtils.updateConfig(this);
        startUSBService();
    }

    private void startUSBService() {
        mLog.i(TAG, "startUSBService...");
        Intent startIntent = new Intent();
        startIntent.setClass(this, USBDetectService.class);
        startIntent.setAction("com.wisepaas.storesenseagent.action.START_USB_SERVICE");
        startService(startIntent);
    }

    private void stopUSBService() {
        mLog.i(TAG, "stopUSBService...");
        Intent startIntent = new Intent();
        startIntent.setClass(this, USBDetectService.class);
        startIntent.setAction("com.wisepaas.storesenseagent.action.STOP_USB_SERVICE");
        startService(startIntent);
    }

    String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        mLog.d(TAG, "* onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSIONS:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startActivity(new Intent(this, DetectActivity.class));
                } else {
                    MessageTools.showToast(this, "權限不足");
                }
        }
    }

    private void checkPermission() {
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.app_name),
                    RC_PERMISSIONS, perms);

        }
    }

    // ************** View Pager *****************
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        // Constants
        static final int PAGE_HOME = 0;
        static final int PAGE_WELCOME = 1;
        //        static final int PAGE_VERIFY = 2;
//        static final int PAGE_DETECT = 3;
        static final int PAGE_DETECT = 3;
        //        static final int PAGE_PWD = 4;
        static final int PAGE_PWD = 2;
        static final int PAGE_SETTING = 4;
        static final int PAGE_LANGUAGE = 6;
        static final int PAGE_WEBVIEW = 20;
        static final int PAGE_SUB_PAGE = 999;

        // Fields
        private final int[] PAGE_GROUP = new int[]{
                PAGE_HOME,
                PAGE_WELCOME,
//                PAGE_VERIFY,
                PAGE_PWD,
                PAGE_DETECT,
                PAGE_SETTING,
                PAGE_LANGUAGE,
                PAGE_WEBVIEW,
                PAGE_SUB_PAGE
        };
        private final String[] PAGE_NAMES = new String[]{
                "PAGE_HOME",
                "PAGE_WELCOME",
//                "PAGE_VERIFY",
                "PAGE_PWD",
                "PAGE_DETECT",
                "PAGE_SETTING",
                "PAGE_LANGUAGE",
                "PAGE_WEBVIEW",
                "PAGE_SUB_PAGE"
        };
        private final Fragment[] fragments = new Fragment[PAGE_GROUP.length];

        private boolean isAppOpened = false;

        // logic fields
        private int lastPosition = PAGE_HOME;

        // Constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case PAGE_HOME: {
                    VFRHomeFragment vfrHomeFragment = VFRHomeFragment.newInstance(2000);
                    vfrHomeFragment.setHomeFragmentListener(new VFRHomeFragment.OnHomeFragmentInteractionListener() {
                        public void onShowEnd() {
                            mLog.d(TAG, "onShowEnd()");
                            if (!isAppOpened) {
                                isAppOpened = true;
//                                switch (SystemPropertiesProxy.get("ro.product.model")) {
//                                    case "usc_130_160":
//                                    case "UTC-115G":
//                                    case "HIT-507":
//                                    case "HIT-512":
////                                        mViewPager.setCurrentItem(PAGE_WEBVIEW);
//                                        break;
//                                }
//                                if (!TRAIL_IS_EXPIRE) {
                                    if (VMSEdgeCache.getInstance().getVms_kiosk_mode() == 1) {
                                        mViewPager.setCurrentItem(PAGE_WELCOME);
                                    } else {
                                        mViewPager.setCurrentItem(PAGE_DETECT);
                                    }
//                                }
                            }
//                            mViewPager.setCurrentItem(PAGE_WELCOME);
                        }
                    });
                    fragment = vfrHomeFragment;
                }
                break;

                case PAGE_WELCOME: {
                    VFRWelcome20210308Fragment vfrWelcomeFragment = VFRWelcome20210308Fragment.newInstance();
                    vfrWelcomeFragment.setOnFragmentInteractionListener(vfrWelcomePageInteractionListener);
                    fragment = vfrWelcomeFragment;
                }
                break;

                case PAGE_DETECT: {
//                    VFRDetectFragment vfrDetectFragment = VFRDetectFragment.newInstance();
//                    vfrDetectFragment.setOnFragmentInteractionListener(vfrDetectPageInteractionListener);
                    VFRDetect20210303Fragment vfrDetectFragment = VFRDetect20210303Fragment.newInstance();
                    vfrDetectFragment.setOnFragmentInteractionListener(vfrDetect20210303PageInteractionListener);
                    fragment = vfrDetectFragment;
                }
                break;

//                case PAGE_VERIFY: {
//                    VFRVerifyFragment vfrVerifyFragment = VFRVerifyFragment.newInstance();
//                    vfrVerifyFragment.setOnFragmentInteractionListener(vfrVerifyPageInteractionListener);
//                    fragment = vfrVerifyFragment;
//                }
//                break;

                case PAGE_PWD: {
//                    VFRAdminPasswordFragment vfrAdminPasswordFragment = VFRAdminPasswordFragment.newInstance();
                    VFRAdminPassword20210429Fragment vfrAdminPassword20210429Fragment = VFRAdminPassword20210429Fragment.newInstance();
                    vfrAdminPassword20210429Fragment.setOnFragmentInteractionListener(vfrAdminPasswordPageInteractionListener);
                    fragment = vfrAdminPassword20210429Fragment;
                }
                break;

                case PAGE_SETTING: {
//                    VFRAdminSettingFragment vfrAdminSettingFragment = VFRAdminSettingFragment.newInstance();
//                    vfrAdminSettingFragment.setOnFragmentInteractionListener(vfrAdminSettingPageInteractionListener);
//                    fragment = vfrAdminSettingFragment;
                    VMSAdminSettingFragment vmsAdminSettingFragment = VMSAdminSettingFragment.newInstance();
                    vmsAdminSettingFragment.setOnFragmentInteractionListener(vmsAdminSettingFragmentListener);
                    fragment = vmsAdminSettingFragment;
                }
                break;

                case PAGE_LANGUAGE: {
                    VFRLanguageFragment vfrLanguageFragment = VFRLanguageFragment.newInstance();
//                    vfrLanguageFragment.setOnFragmentInteractionListener(vfrAdminSettingPageInteractionListener);
                    fragment = vfrLanguageFragment;
                }
                break;

                case PAGE_WEBVIEW: {
                    VFRWebViewFragment vfrWebViewFragment = VFRWebViewFragment.newInstance();
                    fragment = vfrWebViewFragment;
                }
                break;

                case PAGE_SUB_PAGE: {
                    SubPageEmptyFragment subPagesMainFragment = SubPageEmptyFragment.newInstance();
                    fragment = subPagesMainFragment;
                }
                break;
                default:
                    SubPageEmptyFragment subPagesMainFragment = SubPageEmptyFragment.newInstance();
                    fragment = subPagesMainFragment;
                    break;
            }
            mLog.v(TAG, "getItem(): " + fragment.toString());
            fragments[position] = fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return PAGE_GROUP.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "SECTION " + position;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mLog.d(TAG, "onPageSelected(): " + PAGE_NAMES[lastPosition] + " >> " + PAGE_NAMES[position] + ", position= " + position);
            switch (position) {
                case PAGE_WELCOME:
                    if (lastPosition == PAGE_DETECT) {
//                        ((DashboardMainFragment)fragments[PAGE_DASHBOARD]).userLogOutSucceed();
                    }
//                    ((DashboardMainFragment)fragments[PAGE_DASHBOARD]).userLoginSucceed();
                    break;
                case PAGE_DETECT:
//                    ((DashboardMainFragment)fragments[PAGE_DASHBOARD]).userLogOutSucceed();
                    break;
            }
            lastPosition = position;
            mLog.d(TAG, "lastPosition: " + lastPosition);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        int pre_page = PAGE_WELCOME;

        // interactive fragment listener
        // ----------------------------------
        private VFRWelcome20210308Fragment.OnFragmentInteractionListener vfrWelcomePageInteractionListener
                = new VFRWelcome20210308Fragment.OnFragmentInteractionListener() {
            @Override
            public void clickToDetectPage() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT, false);
//                MessageTools.showToast(mContext, "登入成功");
            }

            @Override
            public void onClickAdminSetting() {
                pre_page = PAGE_WELCOME;
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_PWD, false);
            }
        };

        // 20210303
        private VFRDetect20210303Fragment.OnFragmentInteractionListener vfrDetect20210303PageInteractionListener
                = new VFRDetect20210303Fragment.OnFragmentInteractionListener() {
            @Override
            public void onClickConfirmBackToHome() {
                mViewPager.setCurrentItem(PAGE_WELCOME, false);
//                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_UPLOAD);
//                MessageTools.showToast(mContext, "Logout Succeed!");
            }

            @Override
            public void onClickAdminSetting() {
                pre_page = PAGE_DETECT;
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_PWD, false);
            }

        };

        private VFRAdminPassword20210429Fragment.OnFragmentInteractionListener vfrAdminPasswordPageInteractionListener
                = new VFRAdminPassword20210429Fragment.OnFragmentInteractionListener() {
            @Override
            public void clickBackToDetectPage() {
//                switch (pre_page) {
//                    case PAGE_WELCOME:
//                        mViewPager.setCurrentItem(PAGE_WELCOME, false);
//                        break;
//                    case PAGE_DETECT:
//                        mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT, false);
//                        break;
//                }
                switch (VMSEdgeCache.getInstance().getVms_kiosk_mode()) {
                    case 0:
                        mViewPager.setCurrentItem(PAGE_DETECT);
                        break;
                    case 1:
                        mViewPager.setCurrentItem(PAGE_WELCOME);
                        break;
                }
            }

            @Override
            public void clickConfirmPWD() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_SETTING, false);
            }
        };

        private VFRAdminSettingFragment.OnFragmentInteractionListener vfrAdminSettingPageInteractionListener
                = new VFRAdminSettingFragment.OnFragmentInteractionListener() {
            @Override
            public void clickBackToDetectPage() {
//                switch (pre_page) {
//                    case PAGE_WELCOME:
//                        mViewPager.setCurrentItem(PAGE_WELCOME, false);
//                        break;
//                    case PAGE_DETECT:
//                        mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT, false);
//                        break;
//                }
                switch (VMSEdgeCache.getInstance().getVms_kiosk_mode()) {
                    case 0:
                        mViewPager.setCurrentItem(PAGE_DETECT);
                        break;
                    case 1:
                        mViewPager.setCurrentItem(PAGE_WELCOME);
                        break;
                }
            }

            @Override
            public void clickConfirm() {

            }

            @Override
            public void clickLanguagePage() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_LANGUAGE, false);
            }
        };

        private VMSAdminSettingFragment.OnFragmentInteractionListener vmsAdminSettingFragmentListener
                = new VMSAdminSettingFragment.OnFragmentInteractionListener() {
            @Override
            public void clickBackToHomePage() {
//                switch (pre_page) {
//                    case PAGE_WELCOME:
//                        mViewPager.setCurrentItem(PAGE_WELCOME, false);
//                        break;
//                    case PAGE_DETECT:
//                        mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT, false);
//                        break;
//                }
                switch (VMSEdgeCache.getInstance().getVms_kiosk_mode()) {
                    case 0:
                        mViewPager.setCurrentItem(PAGE_DETECT);
                        break;
                    case 1:
                        mViewPager.setCurrentItem(PAGE_WELCOME);
                        break;
                }
            }
        };
    }

    private long firstTime; // 监听两次返回

    //点击两次退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - firstTime < 3000) {
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                return true;
            } else {
                firstTime = System.currentTimeMillis();
                MessageTools.showToast(this, "再點一次退出應用");
                return false;
            }
        }
        return false;
    }

    public void onEventBackgroundThread(BusEvent event) {
//        mLog.i(TAG, " -- Event Bus:> " + event.getEventType());
        switch (event.getEventType()) {
            case DB_CODE_INSERT_DETECT_INFO:
                mDBAdapter.addData();
                mLog.d(TAG, " *** DB_CODE_INSERT_DETECT_INFO *** ");
                break;
            case DB_CODE_INSERT_DETECT_INFO_SUCCESS:
                mLog.d(TAG, " *** DB_CODE_INSERT_DETECT_INFO_SUCCESS *** ");
                mDBAdapter.checkDBAllTables();
                mDBAdapter.uploadData();
                break;
        }
    }

    public void onEventMainThread(BusEvent event) {
        switch (event.getEventType()) {
            case APP_CODE_VMS_KIOSK_RFID_DETECT_DONE:
                mLog.d(TAG, "mViewPager.getCurrentItem():> " + mViewPager.getCurrentItem());
                if (mViewPager.getCurrentItem() == PAGE_WELCOME) {
                    mLog.d(TAG, "RFID:> " + event.getMessage());
                    mViewPager.setCurrentItem(PAGE_DETECT);
                }
                break;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        mLog.d(TAG, " * onDestroy");
        stopUSBService();
        App.threadObject.setRunning(false);
        AppBus.getInstance().unregister(this);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
        super.onDestroy();
    }

}
