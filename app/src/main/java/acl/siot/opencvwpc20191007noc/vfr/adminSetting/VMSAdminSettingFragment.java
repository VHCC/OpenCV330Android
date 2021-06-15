package acl.siot.opencvwpc20191007noc.vfr.adminSetting;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.glomadrian.codeinputlib.CodeInput;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskConnect;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskRemove;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskSync;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskTryConnect;
import cn.pedant.SweetAlert.SweetAlertDialog;
import devlight.io.library.ntb.NavigationTabBar;

import static acl.siot.opencvwpc20191007noc.App.TIME_TICK;
import static acl.siot.opencvwpc20191007noc.App.isBarCodeReaderConnected;
import static acl.siot.opencvwpc20191007noc.App.isThermometerServerConnected;
import static acl.siot.opencvwpc20191007noc.App.isVmsConnected;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CONNECT;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CONNECT_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_CONNECT_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_REMOVE;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_REMOVE_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_SYNC_SUCCESS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_FAIL;
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_SUCCESS;
import static cn.pedant.SweetAlert.SweetAlertDialog.BUTTON_CONFIRM;

/**
 * Created by IChen.Chu on 2021/04/22
 * A fragment to show admin setting page.
 */
public class VMSAdminSettingFragment extends Fragment {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());


    private Button adminHomeBtn;
    private Button applyBtn;
    private TextView time_left;
    private TextView time_right;

    private ImageView thermoConnectStatus;

    private NavigationTabBar navigationTabBar;
    private ViewPager viewPager;

    private NumberFormat formatter = new DecimalFormat("#00.0");
    private NumberFormat formatterCom = new DecimalFormat("#0.0");

    // Listener
    private OnFragmentInteractionListener onFragmentInteractionListener;

    // Constructor
    public VMSAdminSettingFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new fragment instance of VMSAdminSettingFragment.
     */
    public static VMSAdminSettingFragment newInstance() {
        VMSAdminSettingFragment fragment = new VMSAdminSettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        AppBus.getInstance().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreateView");
        View rootView = inflater.inflate(R.layout.vms_fragment_admin_setting, container, false);

        initViewIDs(rootView);
        initViewsFeature();

        return rootView;
    }

    private void initViewIDs(View rootView) {
//        navigationTabBar = (NavigationTabBar) rootView.findViewById(R.id.ntb_vertical);
        navigationTabBar = (NavigationTabBar) rootView.findViewById(R.id.ntb_vertical);
        viewPager = (ViewPager) rootView.findViewById(R.id.vp_vertical_ntb);

        adminHomeBtn = rootView.findViewById(R.id.adminHomeBtn);
        applyBtn = rootView.findViewById(R.id.applyBtn);
        time_left = rootView.findViewById(R.id.time_left);
        time_right = rootView.findViewById(R.id.time_right);

        thermoConnectStatus = rootView.findViewById(R.id.thermoConnectStatus);
    }

    final int TAB_SERVER = 0;
    final int TAB_DEVICE = 1;
    final int TAB_DETECT = 2;
    final int TAB_OTHER = 3;
    final int TAB_LOG = 4;

    // TAB SERVER
    EditText server_host_name;
    EditText server_port;
    CheckBox server_is_ssl;

    Button connectVmsBtn_server;

    EditText trd_hostname;
    EditText trd_port;
    CheckBox trd_is_ssl;
    EditText trd_account;
    EditText trd_password;

    // TAB DEVICE
    EditText uuid;
    EditText deviceName;
    Spinner deviceModeSpinner;

    RadioButton optionOptical;
    RadioButton optionThermal;

    CheckBox cardReaderCheckBox;
    CheckBox barcodeReaderCheckBox;

    // TAB DETECT
    EditText avalo_host;
    TextView avaloAlertTemp;
    TextView avaloAlertTempMinus;
    TextView avaloAlertTempAdd;
    TextView avaloComTemp;
    TextView avaloComTempMinus;
    TextView avaloComTempAdd;
    EditText visitorTemplate;
    EditText nonVisitorTemplate;

    private void initViewsFeature() {

        adminHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentInteractionListener.clickBackToHomePage();
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.getCurrentItem();
                switch (viewPager.getCurrentItem()) {
                    case TAB_SERVER:
//                        mLog.d(TAG, "server_host_name.getText().toString():> " + server_host_name.getText().toString());
//                        VMSEdgeCache.getInstance().setVmsHost(server_host_name.getText().toString());
//                        VMSEdgeCache.getInstance().setVms_host_port(server_port.getText().toString());
//                        VMSEdgeCache.getInstance().setVms_host_is_ssl(server_is_ssl.isChecked());
                        break;
                    case TAB_DEVICE:
                        // Server Setting
                        VMSEdgeCache.getInstance().setVmsKioskDeviceName(deviceName.getText().toString());
                        VMSEdgeCache.getInstance().setVms_host_port(server_port.getText().toString());
                        VMSEdgeCache.getInstance().setVms_host_is_ssl(server_is_ssl.isChecked());
                        VMSEdgeCache.getInstance().setVms_kiosk_video_type(optionOptical.isChecked() ? 0 : 1);

                        mLog.d(TAG, "deviceModeSpinner.getSelectedItem():> " + deviceModeSpinner.getSelectedItem());
                        int mode = 0;
                        switch ((String)deviceModeSpinner.getSelectedItem()) {
                            case "Normal":
                                mode = 0;
                                break;
                            case "Advanced":
                                mode = 1;
                                break;
                        }
                        VMSEdgeCache.getInstance().setVms_kiosk_mode(mode);
                        break;
                    case TAB_DETECT:
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_device_host(avalo_host.getText().toString());
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_alert_temp(Float.valueOf(avaloAlertTemp.getText().toString()));
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_temp_compensation(Float.valueOf(avaloComTemp.getText().toString()));
                        break;
                    case TAB_OTHER:
                        break;
                    case TAB_LOG:
                        break;
                }
                AppBus.getInstance().post(new BusEvent("vms apply update", APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE));
            }
        });

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
//                mLog.d(TAG, "isViewFromObject");
                return view.equals(object);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                ((ViewPager) container).removeView((View) object);
            }


            @RequiresApi(api = Build.VERSION_CODES.N)
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_vp, null, false);
                TextView txtPage = (TextView) view.findViewById(R.id.txt_vp_item_page);
                txtPage.setText(String.format("Page #%d", position));
                mLog.d(TAG, "position:> " + position);
                switch (position) {
                    case TAB_SERVER:
                        view = LayoutInflater.from(getContext()).inflate(R.layout.tab_server, null, false);
                        connectVmsBtn_server = view.findViewById(R.id.btn_connect);
                        server_host_name = view.findViewById(R.id.server_host_name);
                        server_port = view.findViewById(R.id.server_port);
                        server_is_ssl = view.findViewById(R.id.server_isssl);

                        trd_hostname = view.findViewById(R.id.trd_hostname);
                        trd_port = view.findViewById(R.id.trd_port);
                        trd_is_ssl = view.findViewById(R.id.trd_is_ssl);
                        trd_account = view.findViewById(R.id.trd_account);
                        trd_password = view.findViewById(R.id.trd_password);

                        server_host_name.setText(VMSEdgeCache.getInstance().getVmsHost());
                        server_port.setText(VMSEdgeCache.getInstance().getVms_host_port());
                        server_is_ssl.setChecked(VMSEdgeCache.getInstance().getVms_host_is_ssl());

                        trd_hostname.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_host());
                        trd_port.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port());
                        trd_is_ssl.setEnabled(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl());
                        trd_account.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_account());
                        trd_password.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_password());

                        if (isVmsConnected) {
                            connectVmsBtn_server.setText("DISCONNECT");
                            connectVmsBtn_server.setBackground(getResources().getDrawable(R.drawable.ic_btn_disconnect));
                        } else {
                            connectVmsBtn_server.setText("CONNECT");
                            connectVmsBtn_server.setBackground(getResources().getDrawable(R.drawable.ic_btn_connect));
                        }

                        connectVmsBtn_server.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                mLog.d(TAG, "server_host_name.getText().toString():> " + server_host_name.getText().toString());
                                VMSEdgeCache.getInstance().setVmsHost(server_host_name.getText().toString());
                                VMSEdgeCache.getInstance().setVms_host_port(server_port.getText().toString());
                                VMSEdgeCache.getInstance().setVms_host_is_ssl(server_is_ssl.isChecked());

                                VmsKioskTryConnect mMap = new VmsKioskTryConnect(VMSEdgeCache.getInstance().getVmsHost());
                                try {
                                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (isVmsConnected) {
                                    AppBus.getInstance().post(new BusEvent("btn_connect", SHOW_DIALOG_DISCONNECT));
                                } else {
                                    AppBus.getInstance().post(new BusEvent("btn_connect", SHOW_DIALOG_CONNECT));
                                }
                            }
                        });
                        break;
                    case TAB_DEVICE:
                        view = LayoutInflater.from(getContext()).inflate(R.layout.tab_device, null, false);
                        deviceModeSpinner = (Spinner) view.findViewById(R.id.device_mode_spinner);
                        uuid = view.findViewById(R.id.uuid);
                        uuid.setText(VMSEdgeCache.getInstance().getVmsKioskUuid());
                        deviceName = view.findViewById(R.id.deviceName);
                        deviceName.setText(VMSEdgeCache.getInstance().getVmsKioskDeviceName());

                        optionOptical = view.findViewById(R.id.optionOptical);
                        optionOptical.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? true : false);
                        optionOptical.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                optionOptical.setChecked(true);
                                optionThermal.setChecked(false);
                            }
                        });
                        optionThermal = view.findViewById(R.id.optionThermal);
                        optionThermal.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? false : true);
                        optionThermal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                optionOptical.setChecked(false);
                                optionThermal.setChecked(true);
                            }
                        });

                        ArrayAdapter<CharSequence> adapter =
                                ArrayAdapter.createFromResource(getContext(),    //對應的Context
                                        R.array.setting_device_mode,                             //資料選項內容
                                        R.layout.vms_spinner_item);
                        adapter.setDropDownViewResource(R.layout.vms_spinner_drpodown_item);
                        deviceModeSpinner.setAdapter(adapter);
                        deviceModeSpinner.setSelection(VMSEdgeCache.getInstance().getVms_kiosk_mode());

                        cardReaderCheckBox = view.findViewById(R.id.cardReaderCheckBox);
                        barcodeReaderCheckBox = view.findViewById(R.id.barcodeReaderCheckBox);
                        barcodeReaderCheckBox.setChecked(isBarCodeReaderConnected);

                        break;
                    case TAB_DETECT:
                        view = LayoutInflater.from(getContext()).inflate(R.layout.tab_detect, null, false);
                        Spinner tempUnitSpinner = (Spinner) view.findViewById(R.id.temp_unit_spinner);
                        avalo_host = view.findViewById(R.id.avalo_host);
                        avaloAlertTemp = view.findViewById(R.id.avaloAlertTemp);
                        avaloAlertTempMinus = view.findViewById(R.id.avaloAlertTempMinus);
                        avaloAlertTempAdd = view.findViewById(R.id.avaloAlertTempAdd);
                        avaloComTemp = view.findViewById(R.id.avaloComTemp);
                        avaloComTempMinus = view.findViewById(R.id.avaloComTempMinus);
                        avaloComTempAdd = view.findViewById(R.id.avaloComTempAdd);
                        visitorTemplate = view.findViewById(R.id.visitorTemplate);
                        nonVisitorTemplate = view.findViewById(R.id.nonVisitorTemplate);
                        visitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid());
                        nonVisitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid());
                        avalo_host.setText(VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host());
                        avaloAlertTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()));
                        avaloComTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation()));

                        avaloAlertTempMinus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                avaloAlertTemp.setText(String.valueOf(formatter.format(Float.valueOf(avaloAlertTemp.getText().toString()) - 0.1f)));
                            }
                        });

                        avaloAlertTempAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                avaloAlertTemp.setText(String.valueOf(formatter.format(Float.valueOf(avaloAlertTemp.getText().toString()) + 0.1f)));
                            }
                        });

                        avaloComTempMinus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                avaloComTemp.setText(String.valueOf(formatterCom.format(Float.valueOf(avaloComTemp.getText().toString()) - 0.1f)));
                            }
                        });

                        avaloComTempAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                avaloComTemp.setText(String.valueOf(formatterCom.format(Float.valueOf(avaloComTemp.getText().toString()) + 0.1f)));
                            }
                        });

                        ArrayAdapter<CharSequence> adapterTempUnit =
                                ArrayAdapter.createFromResource(getContext(),    //對應的Context
                                        R.array.setting_temp_unit,                             //資料選項內容
                                        R.layout.vms_spinner_item);
                        adapterTempUnit.setDropDownViewResource(R.layout.vms_spinner_drpodown_item);
                        tempUnitSpinner.setAdapter(adapterTempUnit);

                        break;
                    case TAB_OTHER:
                        view = LayoutInflater.from(getContext()).inflate(R.layout.tab_other, null, false);
                        break;
                    case TAB_LOG:
                        view = LayoutInflater.from(getContext()).inflate(R.layout.tab_a, null, false);
                        Button connectVmsBtn = view.findViewById(R.id.connectVmsBtn);
                        connectVmsBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AppBus.getInstance().post(new BusEvent("connect vms", SHOW_DIALOG_CONNECT));
                            }
                        });
                        break;
                }

                container.addView(view);
                return view;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                mLog.d(TAG, "onPageScrolled, position:> " + position);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPageSelected(int position) {
                mLog.d(TAG, "onPageSelected, position:> " + position);
                switch(position) {
                    case TAB_SERVER:
                        server_host_name.setText(VMSEdgeCache.getInstance().getVmsHost());
                        server_port.setText(VMSEdgeCache.getInstance().getVms_host_port());
                        server_is_ssl.setChecked(VMSEdgeCache.getInstance().getVms_host_is_ssl());

                        trd_hostname.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_host());
                        trd_port.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port());
                        trd_is_ssl.setEnabled(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl());
                        trd_account.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_account());
                        trd_password.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_password());
                        break;
                    case TAB_DEVICE:
                        uuid.setText(VMSEdgeCache.getInstance().getVmsKioskUuid());
                        deviceName.setText(VMSEdgeCache.getInstance().getVmsKioskDeviceName());
                        deviceModeSpinner.setSelection(VMSEdgeCache.getInstance().getVms_kiosk_mode());
                        optionOptical.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? true : false);
                        optionThermal.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? false : true);

                        barcodeReaderCheckBox.setChecked(isBarCodeReaderConnected);
                        break;
                    case TAB_DETECT:
                        avalo_host.setText(VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host());
                        avaloAlertTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()));
                        avaloComTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation()));
                        visitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid());
                        nonVisitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid());
                        break;
                    case TAB_OTHER:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        final String[] colors = getResources().getStringArray(R.array.vms_setting);

        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
                        .title("Server")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[0]))
                        .title("Device")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_third),
                        Color.parseColor(colors[0]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Detect")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fourth),
                        Color.parseColor(colors[0]))
                        .title("Other")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fourth),
                        Color.parseColor(colors[0]))
                        .title("Log")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
    }

    private SweetAlertDialog saDialog_avatar;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onEventMainThread(BusEvent event) {
        switch (event.getEventType()) {
            case SHOW_DIALOG_CONNECT:
                LayoutInflater inflater = LayoutInflater.from(getContext());
                LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.connect_pin_code, null);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                final CodeInput cInput = (CodeInput) linearLayout.findViewById(R.id.pairing);

                saDialog_avatar = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);
                saDialog_avatar.setTitleText("Please Enter Paring Code")
                        .setCustomView(linearLayout)
                        .setConfirmButton("Connect", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                saDialog_avatar.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                saDialog_avatar.setCancelable(false);
                                String results = "";
                                for (int i = 0; i < cInput.getCode().length; i++) {
                                    results += String.valueOf(cInput.getCode()[i]);
                                }
                                mLog.d(TAG, "paring code:> " + results);
                                String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                                        Settings.Secure.ANDROID_ID);
                                VmsKioskConnect mMap = new VmsKioskConnect(results, VMSEdgeCache.getInstance().getVmsKioskDeviceName(), android_id);
                                try {
                                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_CONNECT);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();

                break;
            case SHOW_DIALOG_DISCONNECT:
                saDialog_avatar = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);
                saDialog_avatar.setTitleText("Disconnect")
                        .setContentText("Please confirm that you would like to disconnect from the server. Select \" Confirm \" to disconnect from the server, or \" Cancel \" to abort the operation.")
                        .setConfirmButton("Confirm", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                saDialog_avatar.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                saDialog_avatar.setCancelable(false);
                                VmsKioskRemove mMap = new VmsKioskRemove(VMSEdgeCache.getInstance().getVmsKioskUuid());
                                try {
                                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_REMOVE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                saDialog_avatar.dismiss();
                            }
                        })
                        .show();
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_REMOVE_SUCCESS:
                saDialog_avatar.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                saDialog_avatar.getButton(BUTTON_CONFIRM).setEnabled(false);
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_CONNECT_SUCCESS:
                connectVmsBtn_server.setText("DISCONNECT");
                connectVmsBtn_server.setBackground(getResources().getDrawable(R.drawable.ic_btn_disconnect));
                saDialog_avatar.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                saDialog_avatar.setCancelable(true);
                if (saDialog_avatar.isShowing()) {
                    saDialog_avatar.setTitle("Connect SUCCEED");
                }
                mLog.d(TAG, "kiosk UUID:> " + VMSEdgeCache.getInstance().getVmsKioskUuid());
                saDialog_avatar.getButton(BUTTON_CONFIRM).setEnabled(false);
                AppBus.getInstance().post(new BusEvent("sync vms Data", APP_CODE_VMS_KIOSK_DEVICE_SYNC));
                String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                VmsKioskSync mMap = new VmsKioskSync(android_id);
                try {
                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_SYNC);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_CONNECT_FAIL:
                saDialog_avatar.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                saDialog_avatar.setCancelable(true);
                mLog.d(TAG, event.getMessage());
                if (saDialog_avatar.isShowing()) {
                    saDialog_avatar.setTitle(event.getMessage());
                }
                break;
            case TIME_TICK:
                Date d = new Date();
                CharSequence s_1 = android.text.format.DateFormat.format("yyyy-MM-dd", d.getTime());
                CharSequence s_2 = android.text.format.DateFormat.format("hh:mm", d.getTime());
                time_left.setText(s_1);
                time_right.setText(s_2);

                if (isVmsConnected) {
                    connectVmsBtn_server.setText("DISCONNECT");
                    connectVmsBtn_server.setBackground(getResources().getDrawable(R.drawable.ic_btn_disconnect));
                } else {
                    connectVmsBtn_server.setText("CONNECT");
                    connectVmsBtn_server.setBackground(getResources().getDrawable(R.drawable.ic_btn_connect));
                }

                break;
            case APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_SUCCESS:
                saDialog_avatar.getButton(BUTTON_CONFIRM).setEnabled(true);
                AppBus.getInstance().post(new BusEvent("sync vms Data", APP_CODE_VMS_KIOSK_DEVICE_SYNC));
                android_id = Settings.Secure.getString(getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                mMap = new VmsKioskSync(android_id);
                try {
                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_SYNC);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_FAIL:
                saDialog_avatar.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                saDialog_avatar.setTitleText("VMS Server Host is not Exist");
                saDialog_avatar.getButton(BUTTON_CONFIRM).setEnabled(false);
                mLog.d(TAG, event.getMessage());
                break;
            case APP_CODE_THC_1101_HU_GET_TEMP_SUCCESS:
//                thermoConnectStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_SYNC_SUCCESS:
                switch(viewPager.getCurrentItem()) {
                    case TAB_SERVER:
                        server_host_name.setText(VMSEdgeCache.getInstance().getVmsHost());
                        server_port.setText(VMSEdgeCache.getInstance().getVms_host_port());
                        server_is_ssl.setChecked(VMSEdgeCache.getInstance().getVms_host_is_ssl());

                        trd_hostname.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_host());
                        trd_port.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port());
                        trd_is_ssl.setEnabled(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl());
                        trd_account.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_account());
                        trd_password.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_password());
                        break;
                    case TAB_DEVICE:
                        uuid.setText(VMSEdgeCache.getInstance().getVmsKioskUuid());
                        deviceName.setText(VMSEdgeCache.getInstance().getVmsKioskDeviceName());
                        deviceModeSpinner.setSelection(VMSEdgeCache.getInstance().getVms_kiosk_mode());
                        optionOptical.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? true : false);
                        optionThermal.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? false : true);

                        barcodeReaderCheckBox.setChecked(isBarCodeReaderConnected);
                        break;
                    case TAB_DETECT:
                        avalo_host.setText(VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host());
                        avaloAlertTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()));
                        avaloComTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation()));
                        visitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid());
                        nonVisitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid());
                        break;
                    case TAB_OTHER:
                        break;
                }
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_SYNC_FAIL:
                break;
        }
        if (null != thermoConnectStatus) {
            thermoConnectStatus.setImageDrawable(isThermometerServerConnected ? getContext().getDrawable(R.drawable.ic_connect_20210303) : getContext().getDrawable(R.drawable.ic_disconnect_20210303));
        }
    }

    final int SHOW_DIALOG_CONNECT = 10015;
    final int SHOW_DIALOG_DISCONNECT = 10016;

    // -------------------------------------------
    public interface OnFragmentInteractionListener {
        void clickBackToHomePage();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

}
