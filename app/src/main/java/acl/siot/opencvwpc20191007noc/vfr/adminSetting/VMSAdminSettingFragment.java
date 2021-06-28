package acl.siot.opencvwpc20191007noc.vfr.adminSetting;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.DeviceUtils;
import com.chaos.view.PinView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import acl.siot.opencvwpc20191007noc.AppBus;
import acl.siot.opencvwpc20191007noc.BusEvent;
import acl.siot.opencvwpc20191007noc.R;
import acl.siot.opencvwpc20191007noc.api.OKHttpAgent;
import acl.siot.opencvwpc20191007noc.cache.VMSEdgeCache;
import acl.siot.opencvwpc20191007noc.util.LogWriter;
import acl.siot.opencvwpc20191007noc.util.MLog;
import acl.siot.opencvwpc20191007noc.util.MessageTools;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskConnect;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskRemove;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskSync;
import acl.siot.opencvwpc20191007noc.vms.VmsKioskTryConnect;
import cn.pedant.SweetAlert.SweetAlertDialog;
import devlight.io.library.ntb.NavigationTabBar;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

import static acl.siot.opencvwpc20191007noc.App.TIME_TICK;
import static acl.siot.opencvwpc20191007noc.App.isBarCodeReaderCanEdit;
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
import static acl.siot.opencvwpc20191007noc.api.OKHttpConstants.FrsRequestCode.APP_CODE_VMS_KIOSK_DEVICE_UPDATE_FILE_LOG_LIST;
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
    Boolean isBarcodeReaderCheckedTemp;
    CheckBox barcodeReaderCheckBox;

    // TAB DETECT
    EditText avalo_host;
    TextView avaloAlertTemp;
    TextView avaloAlertTempMinus;
    TextView avaloAlertTempAdd;
    TextView avaloComTemp;
    TextView avaloComTempMinus;
    TextView avaloComTempAdd;

    Switch isTempOn;
    Switch isMaskOn;
    EditText visitorTemplate;
    EditText nonVisitorTemplate;

    // TAB OTHER
    EditText screenTimeout;
    Button changePWDBtn;

    // TAB LOG
    Spinner logFileSpinner;
    ArrayAdapter adapter_log;
    TextView logView;

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
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_host(trd_hostname.getText().toString());
//                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_port(trd_port.getText().toString());
//                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_enable_ssl(trd_is_ssl.isChecked());
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_account(trd_account.getText().toString());
                        VMSEdgeCache.getInstance().setVms_kiosk_third_event_party_password(trd_password.getText().toString());
                        break;
                    case TAB_DEVICE:
                        // Server Setting
                        VMSEdgeCache.getInstance().setVmsKioskDeviceName(deviceName.getText().toString());
//                        VMSEdgeCache.getInstance().setVms_host_port(server_port.getText().toString());
//                        VMSEdgeCache.getInstance().setVms_host_is_ssl(server_is_ssl.isChecked());
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

                        VMSEdgeCache.getInstance().setVms_kiosk_device_input_bar_code_scanner(isBarcodeReaderCheckedTemp);

                        VMSEdgeCache.getInstance().setVms_kiosk_video_type(optionOptical.isChecked() ? 0 : 1);
                        break;
                    case TAB_DETECT:
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_device_host(avalo_host.getText().toString());
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_alert_temp(Float.valueOf(avaloAlertTemp.getText().toString()));
                        VMSEdgeCache.getInstance().setVms_kiosk_avalo_temp_compensation(Float.valueOf(avaloComTemp.getText().toString()));

                        VMSEdgeCache.getInstance().setVms_kiosk_is_enable_temp(isTempOn.isChecked());
                        VMSEdgeCache.getInstance().setVms_kiosk_is_enable_mask(isMaskOn.isChecked());

                        break;
                    case TAB_OTHER:
//                        mLog.d(TAG, "screenTimeout:> " + screenTimeout.getText().toString());
//                        mLog.d(TAG, " > 180? " + (Integer.valueOf(screenTimeout.getText().toString()) > 180));
//                        mLog.d(TAG, " < 30 ? " + (Integer.valueOf(screenTimeout.getText().toString()) < 10));
                        if ((Integer.valueOf(screenTimeout.getText().toString()) < 10) || (Integer.valueOf(screenTimeout.getText().toString()) > 180)) {
                            MessageTools.showLongToast(getContext(), "screenTimeOut out of limit.");
                            return;
                        }
                        VMSEdgeCache.getInstance().setVms_kiosk_screen_timeout(Integer.valueOf(screenTimeout.getText().toString()));
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
//                        trd_port = view.findViewById(R.id.trd_port);
//                        trd_is_ssl = view.findViewById(R.id.trd_is_ssl);
                        trd_account = view.findViewById(R.id.trd_account);
                        trd_password = view.findViewById(R.id.trd_password);

                        server_host_name.setText(VMSEdgeCache.getInstance().getVmsHost());
                        server_port.setText(VMSEdgeCache.getInstance().getVms_host_port());
                        server_is_ssl.setChecked(VMSEdgeCache.getInstance().getVms_host_is_ssl());

                        trd_hostname.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_host());
//                        trd_port.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port());
//                        trd_is_ssl.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl());
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
                        optionThermal.setEnabled(isThermometerServerConnected);
                        optionThermal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                optionOptical.setChecked(false);
                                optionThermal.setChecked(true);
                            }
                        });

                        ArrayAdapter<CharSequence> adapter =
                                ArrayAdapter.createFromResource(getContext(),    //對應的Context
                                        R.array.setting_device_mode,             //資料選項內容
                                        R.layout.vms_spinner_item);
                        adapter.setDropDownViewResource(R.layout.vms_spinner_drpodown_item);
                        deviceModeSpinner.setAdapter(adapter);
                        deviceModeSpinner.setSelection(VMSEdgeCache.getInstance().getVms_kiosk_mode());
                        deviceModeSpinner.setEnabled(isVmsConnected);

                        cardReaderCheckBox = view.findViewById(R.id.cardReaderCheckBox);

                        isBarcodeReaderCheckedTemp = VMSEdgeCache.getInstance().getVms_kiosk_device_input_bar_code_scanner();
                        barcodeReaderCheckBox = view.findViewById(R.id.barcodeReaderCheckBox);
                        barcodeReaderCheckBox.setEnabled(isBarCodeReaderCanEdit);
                        if (isBarCodeReaderCanEdit) {
                            barcodeReaderCheckBox.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_device_input_bar_code_scanner());
                        } else {
                            barcodeReaderCheckBox.setChecked(false);
                        }
                        barcodeReaderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                mLog.d(TAG, "isChecked:> " + b);
                                isBarcodeReaderCheckedTemp = b;
                            }
                        });

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

                        isTempOn = view.findViewById(R.id.isTempOn);
                        isMaskOn = view.findViewById(R.id.isMaskOn);
                        visitorTemplate = view.findViewById(R.id.visitorTemplate);
                        nonVisitorTemplate = view.findViewById(R.id.nonVisitorTemplate);

                        isTempOn.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp());
                        isMaskOn.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask());
                        visitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid() == "" ? "None" : VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid());
                        nonVisitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid() == "" ? "None" : VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid());

                        avalo_host.setText(VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host());
                        avaloAlertTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()));
                        avaloComTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation()));

                        avaloAlertTempMinus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (Float.valueOf(avaloAlertTemp.getText().toString()) == 35.0f) {
                                    return;
                                }
                                avaloAlertTemp.setText(String.valueOf(formatter.format(Float.valueOf(avaloAlertTemp.getText().toString()) - 0.1f)));
                            }
                        });

                        avaloAlertTempAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (Float.valueOf(avaloAlertTemp.getText().toString()) == 40.0f) {
                                    return;
                                }
                                avaloAlertTemp.setText(String.valueOf(formatter.format(Float.valueOf(avaloAlertTemp.getText().toString()) + 0.1f)));
                            }
                        });

                        avaloComTempMinus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (Float.valueOf(avaloComTemp.getText().toString()) == -5.0f) {
                                    return;
                                }
                                avaloComTemp.setText(String.valueOf(formatterCom.format(Float.valueOf(avaloComTemp.getText().toString()) - 0.1f)));
                            }
                        });

                        avaloComTempAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (Float.valueOf(avaloComTemp.getText().toString()) == 5.0f) {
                                    return;
                                }
                                avaloComTemp.setText(String.valueOf(formatterCom.format(Float.valueOf(avaloComTemp.getText().toString()) + 0.1f)));
                            }
                        });

                        ArrayAdapter<CharSequence> adapterTempUnit =
                                ArrayAdapter.createFromResource(getContext(),    //對應的Context
                                        R.array.setting_temp_unit,                             //資料選項內容
                                        R.layout.vms_spinner_item);
                        adapterTempUnit.setDropDownViewResource(R.layout.vms_spinner_drpodown_item);
                        tempUnitSpinner.setAdapter(adapterTempUnit);
                        tempUnitSpinner.setEnabled(false);

                        break;
                    case TAB_OTHER:
                        view = LayoutInflater.from(getContext()).inflate(R.layout.tab_other, null, false);

                        screenTimeout = view.findViewById(R.id.screenTimeout);
                        changePWDBtn = view.findViewById(R.id.changePWDBtn);

                        screenTimeout.setText(VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout().toString());
                        changePWDBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AppBus.getInstance().post(new BusEvent("change pwd", SHOW_DIALOG_CHANGE_PWD));
                            }
                        });
                        break;
                    case TAB_LOG:
                        view = LayoutInflater.from(getContext()).inflate(R.layout.tab_log, null, false);

                        ArrayList<String> log_files_array = new ArrayList<>();
                        File logDir = new File(LogWriter.showLogFileFolder());
                        walkDir(logDir, log_files_array);

                        logView = view.findViewById(R.id.logView);

                        logFileSpinner = (Spinner) view.findViewById(R.id.log_file_spinner);
                        adapter_log = new ArrayAdapter<>(view.getContext(), R.layout.vms_spinner_item, log_files_array);
                        adapter_log.setDropDownViewResource(R.layout.vms_spinner_drpodown_item);
                        logFileSpinner.setAdapter(adapter_log);

                        logFileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                String selectedItem = adapterView.getItemAtPosition(i).toString();
                                mLog.d(TAG, ":> " + selectedItem);
                                readFile(selectedItem);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                        readFile(adapter_log.getItem(0).toString());
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
//                        trd_port.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port());
//                        trd_is_ssl.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl());
                        trd_account.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_account());
                        trd_password.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_password());
                        break;
                    case TAB_DEVICE:
                        uuid.setText(VMSEdgeCache.getInstance().getVmsKioskUuid());
                        deviceName.setText(VMSEdgeCache.getInstance().getVmsKioskDeviceName());
                        deviceModeSpinner.setSelection(VMSEdgeCache.getInstance().getVms_kiosk_mode());
                        deviceModeSpinner.setEnabled(isVmsConnected);
                        optionOptical.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? true : false);
                        optionThermal.setEnabled(isThermometerServerConnected);
                        optionThermal.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? false : true);

                        barcodeReaderCheckBox.setEnabled(isBarCodeReaderCanEdit);
                        if (isBarCodeReaderCanEdit) {
                            barcodeReaderCheckBox.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_device_input_bar_code_scanner());
                        } else {
                            barcodeReaderCheckBox.setChecked(false);
                        }
                        break;
                    case TAB_DETECT:
                        avalo_host.setText(VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host());
                        avaloAlertTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()));
                        avaloComTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation()));

                        isTempOn.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp());
                        isMaskOn.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask());
                        visitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid() == "" ? "None" : VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid());
                        nonVisitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid() == "" ? "None" : VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid());
                        break;
                    case TAB_OTHER:
                        screenTimeout.setText(VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout().toString());
                        break;
                    case TAB_LOG:
                        logFileSpinner.setSelection(0);
                        readFile(adapter_log.getItem(0).toString());
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

    private SweetAlertDialog saDialog_connect_server;

    private AlertDialog atDialog_connect_server_connect;
    private AlertDialog atDialog_connect_server_disconnect;
    private AlertDialog atDialog_change_pwd;
    AlertDialog.Builder builder;
    View myView_alertDialog;

    Button cancelBtn;
    Button summitBtn;
    Button confirmBtn;


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onEventMainThread(BusEvent event) {
        switch (event.getEventType()) {
            case SHOW_DIALOG_CHANGE_PWD:

                LayoutInflater inflater_change_pwd = getLayoutInflater();
                myView_alertDialog = inflater_change_pwd.inflate(R.layout.dialog_change_pwd_content, null);


                final TextFieldBoxes text_field_boxes_old = myView_alertDialog.findViewById(R.id.text_field_boxes_old);
                final ExtendedEditText extended_edit_text_old = myView_alertDialog.findViewById(R.id.extended_edit_text_old);

                text_field_boxes_old.getEndIconImageButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLog.d(TAG, "onClick:> " + extended_edit_text_old.getInputType());
                        text_field_boxes_old.setEndIcon(extended_edit_text_old.getInputType() == 1 ? R.drawable.icon_eye_open :  R.drawable.icon_eye_close);
                        extended_edit_text_old.setInputType(extended_edit_text_old.getInputType() == 1 ?
                                65665 :
                                InputType.TYPE_CLASS_TEXT);
                    }
                });

                final TextFieldBoxes text_field_boxes_new = myView_alertDialog.findViewById(R.id.text_field_boxes_new);
                final ExtendedEditText extended_edit_text_new = myView_alertDialog.findViewById(R.id.extended_edit_text_new);

                text_field_boxes_new.getEndIconImageButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLog.d(TAG, "onClick:> " + extended_edit_text_new.getInputType());
                        text_field_boxes_new.setEndIcon(extended_edit_text_new.getInputType() == 1 ? R.drawable.icon_eye_open :  R.drawable.icon_eye_close);
                        extended_edit_text_new.setInputType(extended_edit_text_new.getInputType() == 1 ?
                                65665 :
                                InputType.TYPE_CLASS_TEXT);
                    }
                });

                final TextFieldBoxes text_field_boxes_confirm = myView_alertDialog.findViewById(R.id.text_field_boxes_confirm);
                final ExtendedEditText extended_edit_text_confirm = myView_alertDialog.findViewById(R.id.extended_edit_text_confirm);

                text_field_boxes_confirm.getEndIconImageButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLog.d(TAG, "onClick:> " + extended_edit_text_confirm.getInputType());
                        text_field_boxes_confirm.setEndIcon(extended_edit_text_confirm.getInputType() == 1 ? R.drawable.icon_eye_open :  R.drawable.icon_eye_close);
                        extended_edit_text_confirm.setInputType(extended_edit_text_confirm.getInputType() == 1 ?
                                65665 :
                                InputType.TYPE_CLASS_TEXT);
                    }
                });

                cancelBtn = myView_alertDialog.findViewById(R.id.cancelBtn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        atDialog_change_pwd.dismiss();
                    }
                });

                summitBtn = myView_alertDialog.findViewById(R.id.summitBtn);
                summitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO check PWD
                        mLog.d(TAG, ":> " + extended_edit_text_old.getText().toString());
                        mLog.d(TAG, "default:> " + VMSEdgeCache.getInstance().getVms_kiosk_settingPassword());
                        if (!extended_edit_text_old.getText().toString().equals(VMSEdgeCache.getInstance().getVms_kiosk_settingPassword())) {
//                            MessageTools.showToast(getContext(), "old password wrong");
                            text_field_boxes_old.setError("The password is incorrect, please try again.", true);
                            return;
                        }
                        if (!extended_edit_text_new.getText().toString().equals(extended_edit_text_confirm.getText().toString())) {
//                            MessageTools.showToast(getContext(), "please check confirm password and new password are the same.");
                            text_field_boxes_confirm.setError("The password do not match, please try again.", true);
                            return;
                        }

                        VMSEdgeCache.getInstance().setVms_kiosk_settingPassword(extended_edit_text_new.getText().toString());

                        AppBus.getInstance().post(new BusEvent("vms apply update", APP_CODE_VMS_KIOSK_DEVICE_APPLY_UPDATE));
                        atDialog_change_pwd.dismiss();
                    }
                });

                builder = new AlertDialog.Builder(getContext());
                builder.setView(myView_alertDialog);
                atDialog_change_pwd = builder.create();
                atDialog_change_pwd.show();
                break;
            case SHOW_DIALOG_CONNECT:

                LayoutInflater inflater_change_connect = getLayoutInflater();
                myView_alertDialog = inflater_change_connect.inflate(R.layout.dialog_connect_content, null);

//                final CodeInput cInput = (CodeInput) myView_alertDialog.findViewById(R.id.codeInput);
                final PinView cInput = myView_alertDialog.findViewById(R.id.codeInput);

                cancelBtn = myView_alertDialog.findViewById(R.id.cancelBtn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        atDialog_connect_server_connect.dismiss();
                    }
                });

                summitBtn = myView_alertDialog.findViewById(R.id.summitBtn);
                summitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        String results = "";

//                        for (int i = 0; i < cInput.getCode().length; i++) {
//                            results += String.valueOf(cInput.getCode()[i]);
//                        }

//                        mLog.d(TAG, "paring code:> " + results);
                        mLog.d(TAG, "paring code:> " +  cInput.getText().toString());
                        String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        VmsKioskConnect mMap = new VmsKioskConnect(cInput.getText().toString(), VMSEdgeCache.getInstance().getVmsKioskDeviceName(), android_id);
                        try {
                            OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_CONNECT);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                builder = new AlertDialog.Builder(getContext());
                builder.setView(myView_alertDialog);
                atDialog_connect_server_connect = builder.create();
                atDialog_connect_server_connect.show();

                LayoutInflater inflater = LayoutInflater.from(getContext());
                LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.connect_pin_code, null);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

//                final CodeInput cInput_2 = (CodeInput) linearLayout.findViewById(R.id.pairing);
//
//                saDialog_connect_server = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);
//                saDialog_connect_server.setTitleText("Please Enter Paring Code")
//                        .setCustomView(linearLayout)
//                        .setConfirmButton("Connect", new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                saDialog_connect_server.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
//                                saDialog_connect_server.setCancelable(false);
//                                String results = "";
//                                for (int i = 0; i < cInput.getCode().length; i++) {
//                                    results += String.valueOf(cInput.getCode()[i]);
//                                }
//                                mLog.d(TAG, "paring code:> " + results);
//                                String android_id = Settings.Secure.getString(getContext().getContentResolver(),
//                                        Settings.Secure.ANDROID_ID);
//                                VmsKioskConnect mMap = new VmsKioskConnect(results, VMSEdgeCache.getInstance().getVmsKioskDeviceName(), android_id);
//                                try {
//                                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_CONNECT);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        })
//                        .show();

                break;
            case SHOW_DIALOG_DISCONNECT:

                LayoutInflater inflater_change_disconnect = getLayoutInflater();
                myView_alertDialog = inflater_change_disconnect.inflate(R.layout.dialog_disconnect_content, null);

                cancelBtn = myView_alertDialog.findViewById(R.id.cancelBtn);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        atDialog_connect_server_disconnect.dismiss();
                    }
                });

                confirmBtn = myView_alertDialog.findViewById(R.id.confirmBtn);
                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        VmsKioskRemove mMap = new VmsKioskRemove(VMSEdgeCache.getInstance().getVmsKioskUuid());
                        try {
                            OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_REMOVE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


                builder = new AlertDialog.Builder(getContext());
                builder.setView(myView_alertDialog);
                atDialog_connect_server_disconnect = builder.create();
                atDialog_connect_server_disconnect.show();



//                saDialog_connect_server = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE);
//                saDialog_connect_server.setTitleText("Disconnect")
//                        .setContentText("Please confirm that you would like to disconnect from the server. Select \" Confirm \" to disconnect from the server, or \" Cancel \" to abort the operation.")
//                        .setConfirmButton("Confirm", new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                saDialog_connect_server.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
//                                saDialog_connect_server.setCancelable(false);
//                                VmsKioskRemove mMap = new VmsKioskRemove(VMSEdgeCache.getInstance().getVmsKioskUuid());
//                                try {
//                                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_REMOVE);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        })
//                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                saDialog_connect_server.dismiss();
//                            }
//                        })
//                        .show();
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_REMOVE_SUCCESS:
                if (atDialog_connect_server_disconnect != null && atDialog_connect_server_disconnect.isShowing()) {
                    atDialog_connect_server_disconnect.dismiss();
                }

//                saDialog_connect_server.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
//                saDialog_connect_server.getButton(BUTTON_CONFIRM).setEnabled(false);
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_CONNECT_SUCCESS:
//                connectVmsBtn_server.setText("DISCONNECT");
//                connectVmsBtn_server.setBackground(getResources().getDrawable(R.drawable.ic_btn_disconnect));
//                saDialog_connect_server.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
//                saDialog_connect_server.setCancelable(true);
//                if (saDialog_connect_server != null && saDialog_connect_server.isShowing()) {
//                    saDialog_connect_server.setTitle("Connect SUCCEED");
//                }
                mLog.d(TAG, "kiosk UUID:> " + VMSEdgeCache.getInstance().getVmsKioskUuid());
//                saDialog_connect_server.getButton(BUTTON_CONFIRM).setEnabled(false);
                AppBus.getInstance().post(new BusEvent("sync vms Data", APP_CODE_VMS_KIOSK_DEVICE_SYNC));
                VmsKioskSync mMap = new VmsKioskSync(VMSEdgeCache.getInstance().getVmsKioskUuid());
                try {
                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_SYNC);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_CONNECT_FAIL:
//                saDialog_connect_server.changeAlertType(SweetAlertDialog.ERROR_TYPE);
//                saDialog_connect_server.setCancelable(true);
//                mLog.d(TAG, event.getMessage());
//                if (saDialog_connect_server != null && saDialog_connect_server.isShowing()) {
//                    saDialog_connect_server.setTitle(event.getMessage());
//                }
                MessageTools.showLongToast(getContext(), event.getMessage());
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

                barcodeReaderCheckBox.setEnabled(isBarCodeReaderCanEdit);
                if (!isBarCodeReaderCanEdit) {
                    barcodeReaderCheckBox.setChecked(false);
                }

                break;
            case APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_SUCCESS:
//                saDialog_connect_server.getButton(BUTTON_CONFIRM).setEnabled(true);
//                AppBus.getInstance().post(new BusEvent("sync vms Data", APP_CODE_VMS_KIOSK_DEVICE_SYNC));
//                mMap = new VmsKioskSync(VMSEdgeCache.getInstance().getVmsKioskUuid());
//                try {
//                    OKHttpAgent.getInstance().postRequest(mMap, APP_CODE_VMS_KIOSK_DEVICE_SYNC);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
            case APP_CODE_VMS_KIOSK_DEVICE_TRY_CONNECT_VMS_FAIL:
//                saDialog_connect_server.changeAlertType(SweetAlertDialog.ERROR_TYPE);
//                saDialog_connect_server.setTitleText("VMS Server Host is not Exist");
//                saDialog_connect_server.getButton(BUTTON_CONFIRM).setEnabled(false);
                mLog.d(TAG, event.getMessage());
                MessageTools.showLongToast(getContext(), event.getMessage());
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
//                        trd_port.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_port());
//                        trd_is_ssl.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_enable_ssl());
                        trd_account.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_account());
                        trd_password.setText(VMSEdgeCache.getInstance().getVms_kiosk_third_event_party_password());
                        break;
                    case TAB_DEVICE:
                        uuid.setText(VMSEdgeCache.getInstance().getVmsKioskUuid());
                        deviceName.setText(VMSEdgeCache.getInstance().getVmsKioskDeviceName());
                        deviceModeSpinner.setSelection(VMSEdgeCache.getInstance().getVms_kiosk_mode());
                        deviceModeSpinner.setEnabled(isVmsConnected);
                        optionOptical.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? true : false);
                        optionThermal.setEnabled(isThermometerServerConnected);
                        optionThermal.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_video_type() == 0 ? false : true);

                        barcodeReaderCheckBox.setEnabled(isBarCodeReaderCanEdit);
                        if (isBarCodeReaderCanEdit) {
                            barcodeReaderCheckBox.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_device_input_bar_code_scanner());
                        } else {
                            barcodeReaderCheckBox.setChecked(false);
                        }
                        break;
                    case TAB_DETECT:
                        avalo_host.setText(VMSEdgeCache.getInstance().getVms_kiosk_avalo_device_host());
                        avaloAlertTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_alert_temp()));
                        avaloComTemp.setText(String.valueOf(VMSEdgeCache.getInstance().getVms_kiosk_avalo_temp_compensation()));

                        isTempOn.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_is_enable_temp());
                        isMaskOn.setChecked(VMSEdgeCache.getInstance().getVms_kiosk_is_enable_mask());
                        visitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid() == "" ? "None" : VMSEdgeCache.getInstance().getVmsKioskVisitorTemplateUuid());
                        nonVisitorTemplate.setText(VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid() == "" ? "None" : VMSEdgeCache.getInstance().getVmsKioskDefaultTemplateUuid());
                        break;
                    case TAB_OTHER:
                        screenTimeout.setText(VMSEdgeCache.getInstance().getVms_kiosk_screen_timeout().toString());
                        break;
                }
                if (atDialog_connect_server_connect != null && atDialog_connect_server_connect.isShowing()) {
                    atDialog_connect_server_connect.dismiss();
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

    final int SHOW_DIALOG_CHANGE_PWD = 10025;

    // -------------------------------------------
    public interface OnFragmentInteractionListener {
        void clickBackToHomePage();
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        onFragmentInteractionListener = listener;
    }

    private void walkDir(File dir, ArrayList log_array) {
        String txtPattern = ".txt";

        File listFile[] = dir.listFiles();

        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    walkDir(listFile[i], log_array);
                } else {
                    if (listFile[i].getName().endsWith(txtPattern) && listFile[i].getName().contains(DeviceUtils.getAndroidID())){
                        log_array.add(listFile[i].getName());
                        mLog.d(TAG, "name:> " + listFile[i].getName() + ", file Size:> " + listFile[i].length());
                        //Do what ever u want
                    }
                }
            }
        }
    }

    private void readFile(String logFile) {
        logView.setText(" *** " + logFile + " *** ");
        InputStream fis = null;
        try {
            // open the file for reading
            fis = new FileInputStream(LogWriter.showLogFileFolder() + File.separator + logFile);
            // if file the available for reading
            if (fis != null) {
                // prepare the file for reading
                InputStreamReader chapterReader = new InputStreamReader(fis);
                BufferedReader buffreader = new BufferedReader(chapterReader);

                String line;
                // read every line of the file into the line-variable, on line at the time
                do {
                    line = buffreader.readLine();
                    // do something with the line
//                    mLog.d(TAG, line);
                    logView.append("\n\n" + line);
                } while (line != null);
            }
        } catch (Exception e) {
            // print stack trace.
        } finally {
            // close the file.
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
