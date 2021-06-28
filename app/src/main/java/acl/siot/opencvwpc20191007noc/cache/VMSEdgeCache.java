package acl.siot.opencvwpc20191007noc.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import acl.siot.opencvwpc20191007noc.util.MLog;


/**
 * Created by IChen.Chu on 2021/04/22
 */
public class VMSEdgeCache {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    /**
     * Share Preference
     */
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public static final String SHARE_VMS_EDGE_INFO = "share_vms_edge_info";


    private String vms_host = "localhost";
    public static final String SHARE_VMS_EDGE_INFO_VMS_HOST =
            "share_vms_edge_info_vms_host";

    private String vms_host_port = "80";
    public static final String SHARE_VMS_EDGE_INFO_VMS_HOST_PORT =
            "share_vms_edge_info_vms_host_port";

    private Boolean vms_host_is_ssl = false;
    public static final String SHARE_VMS_EDGE_INFO_VMS_HOST_IS_SSL =
            "share_vms_edge_info_vms_host_is_ssl";

    private String vms_kiosk_uuid = "kioskUUIDNULL"; // vms_web can not edit
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_UUID =
            "share_vms_edge_info_kiosk_uuid";


    // VMS CANT EDIT PROPERTIES
    private Boolean vms_kiosk_device_input_bar_code_scanner = false;
    public static final String SHARE_VMS_EDGE_INPUT_BAR_CODE_SCANNER =
            "share_vms_edge_input_bar_code_scanner";

    private Boolean vms_kiosk_device_input_card_reader = false;
    public static final String SHARE_VMS_EDGE_INPUT_CARD_READER =
            "share_vms_edge_input_card_reader";



    private String vms_kiosk_device_name = "Avalo Kiosk";
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_NAME =
            "share_vms_edge_info_kiosk_name";

    private Integer vms_kiosk_mode = 0; // 0 Normal, 1 Advanced
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_MODE =
            "share_vms_edge_info_vms_kiosk_mode";

    private Integer vms_kiosk_video_type = 0; // 0 Optical, 1 Thermal
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_VIDEO_TYPE =
            "share_vms_edge_info_vms_kiosk_video_type";

    private String vms_kiosk_device_memo = "";
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_MEMO =
            "share_vms_edge_info_kiosk_memo";

    private String vms_kiosk_settingPassword = "123456";
    public static final String SHARE_VMS_EDGE_SETTING_PWD =
            "share_vms_edge_setting_pwd";

    private Integer vms_kiosk_screen_timeout = 30; // 1 seconds per one tick, default: 30s
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_SCREEN_TIMEOUT =
            "share_vms_edge_info_vms_kiosk_screen_timeout";

    private String vms_kiosk_avalo_device_host = "192.168.4.1"; // default: 192.168.4.1
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_DEVICE_HOST =
            "share_vms_edge_info_vms_kiosk_avalo_device_host";

    private Float vms_kiosk_avalo_alert_temp = 37.5f; // default: 37.5f
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_ALERT_TEMP =
            "share_vms_edge_info_vms_kiosk_avalo_alert_temp";

    private Float vms_kiosk_avalo_temp_compensation = 0.0f; // default: 0.0
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_COMPENSATION =
            "share_vms_edge_info_vms_kiosk_avalo_temp_compensation";

    private String vms_kiosk_avalo_temp_unit = "C"; // default: C
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_UNIT =
            "share_vms_edge_info_vms_kiosk_avalo_temp_unit";

    private Boolean vms_kiosk_is_enable_temp = true; // default: true
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_TEMP =
            "share_vms_edge_info_vms_kiosk_is_enable_temp";

    private Boolean vms_kiosk_is_enable_mask = true; // default: true
    public static final String SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_MASK =
            "share_vms_edge_info_vms_kiosk_is_enable_mask";

    private String vms_kiosk_visitor_template_uuid = "none";
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_VISITOR_TEMPLATE_UUID =
            "share_vms_edge_info_kiosk_visitor_template_uuid";

    private String vms_kiosk_default_template_uuid = "none";
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_DEFAULT_TEMPLATE_UUID =
            "share_vms_edge_info_kiosk_default_template_uuid";

    private Boolean vms_kiosk_third_event_party_enable = true; // default false
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE =
            "share_vms_edge_info_kiosk_third_event_party_enable";

    private String vms_kiosk_third_event_party_host = ""; // default empty
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_HOST =
            "share_vms_edge_info_kiosk_third_event_party_host";

    private String vms_kiosk_third_event_party_port = ""; // default empty
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_PORT =
            "share_vms_edge_info_kiosk_third_event_party_port";

    private Boolean vms_kiosk_third_event_party_enable_ssl = false; // default false
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE_SSL =
            "share_vms_edge_info_kiosk_third_event_party_enable_ssl";

    private String vms_kiosk_third_event_party_account = ""; // default empty
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ACCOUNT =
            "share_vms_edge_info_kiosk_third_event_party_account";

    private String vms_kiosk_third_event_party_password = ""; // default empty
    public static final String SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_PASSWORD =
            "share_vms_edge_info_kiosk_third_event_party_password";

    /* Instance */
    private static VMSEdgeCache mVMSEdgeCache;
    private Context mContext;

    private VMSEdgeCache() {
    }

    public void newInstance(Context context) {
        mContext = context;
        sp = context.getSharedPreferences(SHARE_VMS_EDGE_INFO, Context.MODE_PRIVATE);
    }

    public static VMSEdgeCache getInstance() {
        if (mVMSEdgeCache == null) {
            mVMSEdgeCache = new VMSEdgeCache();
        }
        return mVMSEdgeCache;
    }

    //    ------------------------------------ sp---------------------

    public String getVmsHost() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_VMS_HOST) == null ? vms_host.trim() : ((String) map.get(SHARE_VMS_EDGE_INFO_VMS_HOST)).toString().trim());
    }

    public void setVmsHost(String vms_host) {
        this.vms_host = vms_host;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_VMS_HOST, vms_host).commit();
    }

    public String getVms_host_port() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_VMS_HOST_PORT) == null ? vms_host_port.trim() : ((String) map.get(SHARE_VMS_EDGE_INFO_VMS_HOST_PORT)).toString().trim());
    }

    public void setVms_host_port(String vms_host_port) {
        this.vms_host = vms_host;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_VMS_HOST_PORT, vms_host_port).commit();
    }

    public Boolean getVms_host_is_ssl() {
        Map<String, ?> map = sp.getAll();
        return (Boolean) map.get(SHARE_VMS_EDGE_INFO_VMS_HOST_IS_SSL) == null ? false : (Boolean) map.get(SHARE_VMS_EDGE_INFO_VMS_HOST_IS_SSL);
    }

    public void setVms_host_is_ssl(Boolean vms_host_is_ssl) {
        this.vms_host_is_ssl = vms_host_is_ssl;
        editor = sp.edit();
        editor.putBoolean(SHARE_VMS_EDGE_INFO_VMS_HOST_IS_SSL, vms_host_is_ssl).commit();
    }

    public String getVmsKioskUuid() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_UUID) == null ? vms_kiosk_uuid : (String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_UUID));
    }

    public void setVmsKioskUuid(String uuid) {
        this.vms_kiosk_uuid = uuid;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_UUID, uuid).commit();
    }

    public String getVmsKioskDeviceName() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_NAME) == null ? vms_kiosk_device_name : (String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_NAME));
    }

    public void setVmsKioskDeviceName(String vms_kiosk_device_name) {
        this.vms_kiosk_device_name = vms_kiosk_device_name;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_NAME, vms_kiosk_device_name).commit();
    }

    public Integer getVms_kiosk_mode() {
        Map<String, ?> map = sp.getAll();
        return ((Integer) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_MODE) == null ? vms_kiosk_mode : (Integer) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_MODE));
    }

    public void setVms_kiosk_mode(Integer vms_kiosk_mode) {
        this.vms_kiosk_mode = vms_kiosk_mode;
        editor = sp.edit();
        editor.putInt(SHARE_VMS_EDGE_INFO_VMS_KIOSK_MODE, vms_kiosk_mode).commit();
    }

    public Integer getVms_kiosk_video_type() {
        Map<String, ?> map = sp.getAll();
        return ((Integer) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_VIDEO_TYPE) == null ? vms_kiosk_video_type : (Integer) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_VIDEO_TYPE));
    }

    public void setVms_kiosk_video_type(Integer vms_kiosk_video_type) {
        this.vms_kiosk_video_type = vms_kiosk_video_type;
        editor = sp.edit();
        editor.putInt(SHARE_VMS_EDGE_INFO_VMS_KIOSK_VIDEO_TYPE, vms_kiosk_video_type).commit();
    }

    public String getVms_kiosk_device_memo() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_MEMO));
    }

    public void setVms_kiosk_device_memo(String vms_kiosk_device_memo) {
        this.vms_kiosk_device_memo = vms_kiosk_device_memo;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_MEMO, vms_kiosk_device_memo).commit();
    }

    public Integer getVms_kiosk_screen_timeout() {
        Map<String, ?> map = sp.getAll();
        return ((Integer) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_SCREEN_TIMEOUT) == null ? vms_kiosk_screen_timeout : (Integer) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_SCREEN_TIMEOUT));
    }

    public void setVms_kiosk_screen_timeout(Integer vms_kiosk_screen_timeout) {
        this.vms_kiosk_screen_timeout = vms_kiosk_screen_timeout;
        editor = sp.edit();
        editor.putInt(SHARE_VMS_EDGE_INFO_VMS_KIOSK_SCREEN_TIMEOUT, vms_kiosk_screen_timeout).commit();
    }

    public String getVms_kiosk_avalo_device_host() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_DEVICE_HOST) == null ? vms_kiosk_avalo_device_host : (String) ((String) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_DEVICE_HOST)).toString().trim());
    }

    public void setVms_kiosk_avalo_device_host(String vms_kiosk_avalo_device_host) {
        this.vms_kiosk_avalo_device_host = vms_kiosk_avalo_device_host;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_DEVICE_HOST, vms_kiosk_avalo_device_host).commit();
    }

    public Float getVms_kiosk_avalo_alert_temp() {
        Map<String, ?> map = sp.getAll();
        return ((Float) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_ALERT_TEMP) == null ? vms_kiosk_avalo_alert_temp : (Float) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_ALERT_TEMP));
    }

    public void setVms_kiosk_avalo_alert_temp(Float vms_kiosk_avalo_alert_temp) {
        this.vms_kiosk_avalo_alert_temp = vms_kiosk_avalo_alert_temp;
        editor = sp.edit();
        editor.putFloat(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_ALERT_TEMP, vms_kiosk_avalo_alert_temp).commit();
    }

    public Float getVms_kiosk_avalo_temp_compensation() {
        Map<String, ?> map = sp.getAll();
        return ((Float) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_COMPENSATION)  == null ? vms_kiosk_avalo_temp_compensation : (Float) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_COMPENSATION));
    }

    public void setVms_kiosk_avalo_temp_compensation(Float vms_kiosk_avalo_temp_compensation) {
        this.vms_kiosk_avalo_temp_compensation = vms_kiosk_avalo_temp_compensation;
        editor = sp.edit();
        editor.putFloat(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_COMPENSATION, vms_kiosk_avalo_temp_compensation).commit();
    }

    public String getVms_kiosk_avalo_temp_unit() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_UNIT) == null ? "C" : (String) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_UNIT));
    }

    public void setVms_kiosk_avalo_temp_unit(String vms_kiosk_avalo_temp_unit) {
        this.vms_kiosk_avalo_temp_unit = vms_kiosk_avalo_temp_unit;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_VMS_KIOSK_AVALO_TEMP_UNIT, vms_kiosk_avalo_temp_unit).commit();
    }

    public Boolean getVms_kiosk_is_enable_temp() {
        Map<String, ?> map = sp.getAll();
        return ((Boolean) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_TEMP) == null ? true : (Boolean) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_TEMP));
    }

    public void setVms_kiosk_is_enable_temp(Boolean vms_kiosk_is_enable_temp) {
        this.vms_kiosk_is_enable_temp = vms_kiosk_is_enable_temp;
        editor = sp.edit();
        editor.putBoolean(SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_TEMP, vms_kiosk_is_enable_temp).commit();
    }

    public Boolean getVms_kiosk_is_enable_mask() {
        Map<String, ?> map = sp.getAll();
        return ((Boolean) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_MASK) == null ? true : (Boolean) map.get(SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_MASK));
    }

    public void setVms_kiosk_is_enable_mask(Boolean vms_kiosk_is_enable_mask) {
        this.vms_kiosk_is_enable_mask = vms_kiosk_is_enable_mask;
        editor = sp.edit();
        editor.putBoolean(SHARE_VMS_EDGE_INFO_VMS_KIOSK_IS_ENABLE_MASK, vms_kiosk_is_enable_mask).commit();
    }


    public String getVmsKioskVisitorTemplateUuid() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_VISITOR_TEMPLATE_UUID) == null ? vms_kiosk_visitor_template_uuid : (String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_VISITOR_TEMPLATE_UUID));
    }

    public void setVmsKioskVisitorTemplateUuid(String vms_kiosk_visitor_template_uuid) {
        this.vms_kiosk_visitor_template_uuid = vms_kiosk_visitor_template_uuid;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_VISITOR_TEMPLATE_UUID, vms_kiosk_visitor_template_uuid).commit();
    }

    public String getVmsKioskDefaultTemplateUuid() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_DEFAULT_TEMPLATE_UUID)  == null ? vms_kiosk_default_template_uuid : (String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_DEFAULT_TEMPLATE_UUID));
    }

    public void setVmsKioskDefaultTemplateUuid(String vms_kiosk_default_template_uuid) {
        this.vms_kiosk_default_template_uuid = vms_kiosk_default_template_uuid;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_DEFAULT_TEMPLATE_UUID, vms_kiosk_default_template_uuid).commit();
    }

    public Boolean getVms_kiosk_third_event_party_enable() {
        Map<String, ?> map = sp.getAll();
        return ((Boolean) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE) == null ? true : (Boolean) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE));
    }

    public void setVms_kiosk_third_event_party_enable(Boolean vms_kiosk_third_event_party_enable) {
        this.vms_kiosk_third_event_party_enable = vms_kiosk_third_event_party_enable;
        editor = sp.edit();
        editor.putBoolean(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE, vms_kiosk_third_event_party_enable).commit();
    }

    public String getVms_kiosk_third_event_party_host() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_HOST));
    }

    public void setVms_kiosk_third_event_party_host(String vms_kiosk_third_event_party_host) {
        this.vms_kiosk_third_event_party_host = vms_kiosk_third_event_party_host;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_HOST, vms_kiosk_third_event_party_host).commit();
    }

    public String getVms_kiosk_third_event_party_port() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_PORT));
    }

    public void setVms_kiosk_third_event_party_port(String vms_kiosk_third_event_party_port) {
        this.vms_kiosk_third_event_party_port = vms_kiosk_third_event_party_port;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_PORT, vms_kiosk_third_event_party_port).commit();
    }

    public Boolean getVms_kiosk_third_event_party_enable_ssl() {
        Map<String, ?> map = sp.getAll();
        return ((Boolean) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE_SSL) == null ? false : (Boolean) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE_SSL));
    }

    public void setVms_kiosk_third_event_party_enable_ssl(Boolean vms_kiosk_third_event_party_enable_ssl) {
        this.vms_kiosk_third_event_party_enable_ssl = vms_kiosk_third_event_party_enable_ssl;
        editor = sp.edit();
        editor.putBoolean(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ENABLE_SSL, vms_kiosk_third_event_party_enable_ssl).commit();
    }

    public String getVms_kiosk_third_event_party_account() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ACCOUNT));
    }

    public void setVms_kiosk_third_event_party_account(String vms_kiosk_third_event_party_account) {
        this.vms_kiosk_third_event_party_account = vms_kiosk_third_event_party_account;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_ACCOUNT, vms_kiosk_third_event_party_account).commit();
    }

    public String getVms_kiosk_third_event_party_password() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_PASSWORD));
    }

    public void setVms_kiosk_third_event_party_password(String vms_kiosk_third_event_party_password) {
        this.vms_kiosk_third_event_party_password = vms_kiosk_third_event_party_password;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_INFO_KIOSK_THIRD_EVENT_PARTY_PASSWORD, vms_kiosk_third_event_party_password).commit();
    }

    public Boolean getVms_kiosk_device_input_bar_code_scanner() {
        Map<String, ?> map = sp.getAll();
        return ((Boolean) map.get(SHARE_VMS_EDGE_INPUT_BAR_CODE_SCANNER) == null ? false : (Boolean) map.get(SHARE_VMS_EDGE_INPUT_BAR_CODE_SCANNER));
    }

    public void setVms_kiosk_device_input_bar_code_scanner(Boolean vms_kiosk_device_input_bar_code_scanner) {
        this.vms_kiosk_device_input_bar_code_scanner = vms_kiosk_device_input_bar_code_scanner;
        editor = sp.edit();
        editor.putBoolean(SHARE_VMS_EDGE_INPUT_BAR_CODE_SCANNER, vms_kiosk_device_input_bar_code_scanner).commit();
    }

    public Boolean getVms_kiosk_device_input_card_reader() {
        Map<String, ?> map = sp.getAll();
        return ((Boolean) map.get(SHARE_VMS_EDGE_INPUT_CARD_READER) == null ? false : (Boolean) map.get(SHARE_VMS_EDGE_INPUT_CARD_READER));
    }

    public void setVms_kiosk_device_input_card_reader(Boolean vms_kiosk_device_input_card_reader) {
        this.vms_kiosk_device_input_card_reader = vms_kiosk_device_input_card_reader;
        editor = sp.edit();
        editor.putBoolean(SHARE_VMS_EDGE_INPUT_CARD_READER, vms_kiosk_device_input_card_reader).commit();
    }

    public String getVms_kiosk_settingPassword() {
        Map<String, ?> map = sp.getAll();
        return ((String) map.get(SHARE_VMS_EDGE_SETTING_PWD)   == null ? vms_kiosk_settingPassword : (String) map.get(SHARE_VMS_EDGE_SETTING_PWD));
    }

    public void setVms_kiosk_settingPassword(String vms_kiosk_settingPassword) {
        this.vms_kiosk_settingPassword = vms_kiosk_settingPassword;
        editor = sp.edit();
        editor.putString(SHARE_VMS_EDGE_SETTING_PWD, vms_kiosk_settingPassword).commit();
    }

    public String showInfoAll() {

        String results = "";
        results += "vms_host:> " + getVmsHost() + ", ";
        results += "vms_kiosk_device_name:> " + getVmsKioskDeviceName() + ", ";
        results += "vms_kiosk_mode:> " + getVms_kiosk_mode() + ", ";
        results += "vms_kiosk_video_type:> " + getVms_kiosk_video_type() + ", ";
        results += "vms_kiosk_device_memo:> " + getVms_kiosk_device_memo() + ", ";
        results += "vms_kiosk_screen_timeout:> " + getVms_kiosk_screen_timeout() + ", ";
        results += "vms_kiosk_avalo_device_host:> " + getVms_kiosk_avalo_device_host() + ", ";
        results += "vms_kiosk_avalo_alert_temp:> " + getVms_kiosk_avalo_alert_temp() + ", ";
        results += "vms_kiosk_avalo_temp_compensation:> " + getVms_kiosk_avalo_temp_compensation() + ", ";
        results += "vms_kiosk_avalo_temp_unit:> " + getVms_kiosk_avalo_temp_unit()+ ", ";
        results += "vms_kiosk_is_enable_temp:> " + getVms_kiosk_is_enable_temp() + ", ";
        results += "vms_kiosk_visitor_template_uuid:> " + getVmsKioskVisitorTemplateUuid() + ", ";
        results += "vms_kiosk_default_template_uuid:> " + getVmsKioskDefaultTemplateUuid() + ", ";
        results += "vms_kiosk_third_event_party_enable:> " + getVms_kiosk_third_event_party_enable() + ", ";
        results += "vms_kiosk_third_event_party_host:> " + getVms_kiosk_third_event_party_host() + ", ";
        results += "vms_kiosk_third_event_party_port:> " + getVms_kiosk_third_event_party_port() + ", ";
        results += "vms_kiosk_third_event_party_enable_ssl:> " + getVms_kiosk_third_event_party_enable_ssl() + ", ";
        results += "vms_kiosk_third_event_party_account:> " + getVms_kiosk_third_event_party_account() + ", ";
        results += "vms_kiosk_third_event_party_password:> " + getVms_kiosk_third_event_party_password() + ", ";
        return results;
    }
}
