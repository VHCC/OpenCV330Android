package acl.siot.opencvwpc20191007noc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.ArrayList;

import acl.siot.opencvwpc20191007noc.page.subPage.SubPageEmptyFragment;
import acl.siot.opencvwpc20191007noc.page.tranform.FadeInOutBetterTransformer;
import acl.siot.opencvwpc20191007noc.page.tranform.FadeInOutTransformer;
import acl.siot.opencvwpc20191007noc.page.tranform.ScaleInOutTransformer;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.vfr.adminSetting.VFRAdminPasswordFragment;
import acl.siot.opencvwpc20191007noc.vfr.adminSetting.VFRAdminSettingFragment;
import acl.siot.opencvwpc20191007noc.vfr.detect.VFRDetectFragment;
import acl.siot.opencvwpc20191007noc.vfr.home.VFRHomeFragment;
import acl.siot.opencvwpc20191007noc.vfr.upload.VFRVerifyFragment;
import acl.siot.opencvwpc20191007noc.vfr.welcome.VFRWelcomeFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import pub.devrel.easypermissions.EasyPermissions;

public class VFRMainActivity extends AppCompatActivity {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private final int RC_PERMISSIONS = 9001;

    /**
     * The {@link ViewPager} will host the section contents.
     */
    private ViewPager mViewPager;

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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSIONS:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startActivity(new Intent(this, DetectActivity.class));
                } else {
                    Toast.makeText(this, "權限不足", Toast.LENGTH_LONG).show();
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
        static final int PAGE_DETECT = 3;
        static final int PAGE_PWD = 4;
        static final int PAGE_SETTING = 5;
        static final int PAGE_VERIFY = 2;
        static final int PAGE_SUB_PAGE = 999;

        // Fields
        private final int[] PAGE_GROUP = new int[]{
                PAGE_HOME,
                PAGE_WELCOME,
                PAGE_DETECT,
                PAGE_VERIFY,
                PAGE_PWD,
                PAGE_SETTING,
                PAGE_SUB_PAGE
        };
        private final String[] PAGE_NAMES = new String[]{
                "PAGE_HOME",
                "PAGE_WELCOME",
                "PAGE_DETECT",
                "PAGE_VERIFY",
                "PAGE_PWD",
                "PAGE_SETTING",
                "PAGE_SUB_PAGE"
        };
        private final Fragment[] fragments = new Fragment[PAGE_GROUP.length];

        // logic fields
        private int lastPosition = PAGE_HOME;

        // Constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position) {
                case PAGE_HOME: {
                    VFRHomeFragment vfrHomeFragment = VFRHomeFragment.newInstance(2000);
                    vfrHomeFragment.setHomeFragmentListener(new VFRHomeFragment.OnHomeFragmentInteractionListener() {
                        public void onShowEnd() {
                            mLog.d(TAG, "onShowEnd()");
                            mViewPager.setCurrentItem(PAGE_WELCOME);
                        }
                    });
                    fragment = vfrHomeFragment;
                }
                break;

                case PAGE_WELCOME: {
                    VFRWelcomeFragment vfrWelcomeFragment = VFRWelcomeFragment.newInstance();
                    vfrWelcomeFragment.setOnFragmentInteractionListener(vfrWelcomePageInteractionListener);
                    fragment = vfrWelcomeFragment;
                }
                break;

                case PAGE_DETECT: {
                    VFRDetectFragment vfrDetectFragment = VFRDetectFragment.newInstance();
                    vfrDetectFragment.setOnFragmentInteractionListener(vfrDetectPageInteractionListener);
                    fragment = vfrDetectFragment;
                }
                break;

                case PAGE_VERIFY: {
                    VFRVerifyFragment vfrVerifyFragment = VFRVerifyFragment.newInstance();
                    vfrVerifyFragment.setOnFragmentInteractionListener(vfrVerifyPageInteractionListener);
                    fragment = vfrVerifyFragment;
                }
                break;

                case PAGE_PWD: {
                    VFRAdminPasswordFragment vfrAdminPasswordFragment = VFRAdminPasswordFragment.newInstance();
                    vfrAdminPasswordFragment.setOnFragmentInteractionListener(vfrAdminPasswordPageInteractionListener);
                    fragment = vfrAdminPasswordFragment;
                }
                break;

                case PAGE_SETTING: {
                    VFRAdminSettingFragment vfrAdminSettingFragment = VFRAdminSettingFragment.newInstance();
                    vfrAdminSettingFragment.setOnFragmentInteractionListener(vfrAdminSettingPageInteractionListener);
                    fragment = vfrAdminSettingFragment;
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
            }
            lastPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        // interactive fragment listener
        // ----------------------------------
        private VFRWelcomeFragment.OnFragmentInteractionListener vfrWelcomePageInteractionListener
                = new VFRWelcomeFragment.OnFragmentInteractionListener() {
            @Override
            public void clickToDetectPage() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT);
//                MessageTools.showToast(mContext, "登入成功");
            }
        };

        private VFRDetectFragment.OnFragmentInteractionListener vfrDetectPageInteractionListener
                = new VFRDetectFragment.OnFragmentInteractionListener() {
            @Override
            public void onClickCancelDetect() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_WELCOME);
//                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_UPLOAD);
//                MessageTools.showToast(mContext, "Logout Succeed!");
            }

            @Override
            public void onClickAdminSetting() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_PWD);
            }

            @Override
            public void onDetectThreeFaces() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_VERIFY);
            }
        };

        private VFRVerifyFragment.OnFragmentInteractionListener vfrVerifyPageInteractionListener
                = new VFRVerifyFragment.OnFragmentInteractionListener() {
            @Override
            public void clickRetry() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT);
            }

            @Override
            public void uploadImageFinish() {
//                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_RESULT);
            }
        };

        private VFRAdminPasswordFragment.OnFragmentInteractionListener vfrAdminPasswordPageInteractionListener
                = new VFRAdminPasswordFragment.OnFragmentInteractionListener() {
            @Override
            public void clickBackToDetectPage() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT);
            }

            @Override
            public void clickConfirmPWD() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_SETTING);
            }
        };

        private VFRAdminSettingFragment.OnFragmentInteractionListener vfrAdminSettingPageInteractionListener
                = new VFRAdminSettingFragment.OnFragmentInteractionListener() {
            @Override
            public void clickBackToDetectPage() {
                mViewPager.setCurrentItem(SectionsPagerAdapter.PAGE_DETECT);
            }

            @Override
            public void clickConfirm() {

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
                return true;
            } else {
                firstTime = System.currentTimeMillis();
                Toast.makeText(this, "再點一次退出應用", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return false;
    }

}
