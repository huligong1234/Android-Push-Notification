package org.jeedevframework.jpush.android.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import org.jeedevframework.jpush.android.utils.LogUtil;
import org.jeedevframework.jpush.android.utils.MD5Util;
import org.jeedevframework.jpush.android.utils.StringUtils;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;

public class JPushInterface
{
  private static final String LOGTAG = LogUtil.makeLogTag(JPushInterface.class);
  public static final String SHARED_PREFERENCE_NAME = "jpush_preferences";
  
  
  public static final String CALLBACK_ACTIVITY_PACKAGE_NAME = "CALLBACK_ACTIVITY_PACKAGE_NAME";
  public static final String CALLBACK_ACTIVITY_CLASS_NAME = "CALLBACK_ACTIVITY_CLASS_NAME";
  
  
  public static final String API_KEY = "API_KEY";
  public static final String VERSION = "VERSION";
  
  
  public static final String XMPP_HOST = "XMPP_HOST";
  public static final String XMPP_PORT = "XMPP_PORT";
  public static final String XMPP_USERNAME = "XMPP_USERNAME";
  public static final String XMPP_PASSWORD = "XMPP_PASSWORD";
  
  
  public static final String DEVICE_ID = "DEVICE_ID";
  public static final String CLIENT_ID = "CLIENT_ID";
  public static final String EMULATOR_DEVICE_ID = "EMULATOR_DEVICE_ID";
  public static final String NOTIFICATION_ICON = "NOTIFICATION_ICON";
  
  
  public static final String SETTINGS_DEBUG_ENABLED = "SETTINGS_DEBUG_ENABLED";
  public static final String SETTINGS_NOTIFICATION_ENABLED = "SETTINGS_NOTIFICATION_ENABLED";
  public static final String SETTINGS_SOUND_ENABLED = "SETTINGS_SOUND_ENABLED";
  public static final String SETTINGS_VIBRATE_ENABLED = "SETTINGS_VIBRATE_ENABLED";
  public static final String SETTINGS_TOAST_ENABLED = "SETTINGS_TOAST_ENABLED";
  public static final String SETTINGS_HANDLER_ENABLED = "SETTINGS_HANDLER_ENABLED";
  public static final String SETTINGS_SYSTEM_BAR_ENABLED = "SETTINGS_SYSTEM_BAR_ENABLED";
  
  
  public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
  public static final String NOTIFICATION_SHOW_TYPE = "NOTIFICATION_SHOW_TYPE";
  public static final String NOTIFICATION_API_KEY = "NOTIFICATION_API_KEY";
  public static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";
  public static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
  public static final String NOTIFICATION_URI = "NOTIFICATION_URI";
  public static final String NOTIFICATION_PACKET_ID = "PACKET_ID";
  public static final String NOTIFICATION_FROM = "NOTIFICATION_FROM";
  
  public static final String SHOW_TYPE_NOTIFICATION = "NOTIFICATION";
  public static final String SHOW_TYPE_PAYLOAD = "PAYLOAD";
  
  
  public static final String ACTION_SHOW_NOTIFICATION = "org.jeedevframework.jpush.android.intent.SHOW_NOTIFICATION";
  public static final String ACTION_NOTIFICATION_CLICKED = "org.jeedevframework.jpush.android.intent.NOTIFICATION_CLICKED";
  public static final String ACTION_NOTIFICATION_CLEARED = "org.jeedevframework.jpush.android.intent.NOTIFICATION_CLEARED";
  public static final String ACTION_NOTIFICATION_RECEIVED_PROXY = "org.jeedevframework.jpush.android.intent.NOTIFICATION_RECEIVED_PROXY";

  public static XmppManager xmppManager = null;
  public static HashMap<String, String> DEVICE_STATUS_MAP = new HashMap<String, String>();
  
  public static HashMap<String, String> initDeviceStatus(Context context)
  {
    try
    {
      TelephonyManager tm = (TelephonyManager)context.getSystemService("phone");
      String model = Build.MODEL;
      
      String os = "android" + Build.VERSION.RELEASE;
      String deviceid = tm.getDeviceId();
      String tel = tm.getLine1Number();
      String imei = tm.getSimSerialNumber();
      String imsi = tm.getSubscriberId();
      
      DEVICE_STATUS_MAP.put("sdk_int", String.valueOf(Build.VERSION.SDK_INT));
      DEVICE_STATUS_MAP.put("manufacturer", Build.MANUFACTURER);
      DEVICE_STATUS_MAP.put("brand", Build.BRAND);
      
      DEVICE_STATUS_MAP.put("model", model);
      DEVICE_STATUS_MAP.put("os", os);
      DEVICE_STATUS_MAP.put("tel", tel);
      if (!StringUtils.isEmpty(deviceid)) {
        DEVICE_STATUS_MAP.put("deviceId", deviceid);
      }
      if (!StringUtils.isEmpty(imei)) {
        DEVICE_STATUS_MAP.put("imei", imei);
      }
      if (!StringUtils.isEmpty(imsi)) {
        DEVICE_STATUS_MAP.put("imsi", imsi);
      }
      PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      DEVICE_STATUS_MAP.put("packageName", info.applicationInfo.packageName);
      DEVICE_STATUS_MAP.put("versionCode", String.valueOf(info.versionCode));
      DEVICE_STATUS_MAP.put("versionName", info.versionName);
      
      WifiManager wifi = (WifiManager)context.getSystemService("wifi");
      WifiInfo wifiInfo = wifi.getConnectionInfo();
      DEVICE_STATUS_MAP.put("macAddress", wifiInfo.getMacAddress());
      try
      {
        int cid = 0;
        int lac = 0;
        String mcc = "";
        String mnc = "";
        int type = tm.getNetworkType();
        if (type == TelephonyManager.NETWORK_TYPE_GPRS // GSM网
				|| type == TelephonyManager.NETWORK_TYPE_EDGE
				|| type == TelephonyManager.NETWORK_TYPE_HSDPA) {
          GsmCellLocation gsm = (GsmCellLocation)tm.getCellLocation();
          if (gsm == null){
            Log.e("GsmCellLocation", "GsmCellLocation is null!!!");
            return null;
          }
          lac = gsm.getLac();
          mcc = tm.getNetworkOperator().substring(0, 3);
          mnc = tm.getNetworkOperator().substring(3, 5);
          cid = gsm.getCid();
        }else if (type == TelephonyManager.NETWORK_TYPE_CDMA // 电信cdma网
				|| type == TelephonyManager.NETWORK_TYPE_1xRTT
				|| type == TelephonyManager.NETWORK_TYPE_EVDO_0
				|| type == TelephonyManager.NETWORK_TYPE_EVDO_A) {
          CdmaCellLocation cdma = (CdmaCellLocation)tm.getCellLocation();
          if (cdma == null){
            Log.e("CdmaCellLocation", "CdmaCellLocation is null!!!");
            return null;
          }
          lac = cdma.getNetworkId();
          mcc = tm.getNetworkOperator().substring(0, 3);
          mnc = String.valueOf(cdma.getSystemId());
          cid = cdma.getBaseStationId();
        }
        DEVICE_STATUS_MAP.put("mcc", mcc);
        DEVICE_STATUS_MAP.put("mnc", mnc);
        DEVICE_STATUS_MAP.put("cid", String.valueOf(cid));
        DEVICE_STATUS_MAP.put("lac", String.valueOf(lac));
      }catch (Exception e){
        LogUtil.e(LOGTAG, e.getMessage(), e);
      }
    }catch (PackageManager.NameNotFoundException e){
      LogUtil.e(LOGTAG, e.getMessage(), e);
    }
    return DEVICE_STATUS_MAP;
  }
  
  public static void reportStatus(String packetId, String notificationFrom)
  {
    IQ result = new IQ()
    {
      public String getChildElementXML()
      {
        return null;
      }
    };
    result.setType(IQ.Type.RESULT);
    result.setPacketID(packetId);
    result.setTo(notificationFrom);
    try
    {
      if (xmppManager != null) {
        xmppManager.getConnection().sendPacket(result);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static String getClientId(Context context)
  {
    String clientId = "";
    String deviceId = "";
    String macAddress = "";
    String imei = "";
    String imsi = "";
    if (context != null) {
      try{
        TelephonyManager tm = (TelephonyManager)context.getSystemService("phone");
        deviceId = StringUtils.trimToEmpty(tm.getDeviceId());
        imei = StringUtils.trimToEmpty(tm.getSimSerialNumber());
        imsi = StringUtils.trimToEmpty(tm.getSubscriberId());
        
        if ((StringUtils.isEmpty(deviceId)) 
        		|| (deviceId.length() < 10) 
          || (!isNormal(deviceId))) {
          imei = "";
        }
        
        if ((StringUtils.isEmpty(imei)) 
        		|| (imei.length() < 10) 
        		|| (!StringUtils.isNumeric(imei)) 
        		|| (!isNormal(imei))) {
          imei = "";
        }
        
        if ((StringUtils.isEmpty(imsi)) 
        		|| (imsi.length() != 15) 
        		|| (!StringUtils.isNumeric(imsi)) 
        		|| (!isNormal(imsi))) {
          imsi = "";
        }
        
        WifiManager wifi = (WifiManager)context.getSystemService("wifi");
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        macAddress = StringUtils.trimToEmpty(wifiInfo.getMacAddress());
        if ((StringUtils.isEmpty(macAddress)) || 
          ("00:00:00:00:00:00".equals(macAddress)) || 
          (!macAddress.contains(":")) || 
          (!isNormal(macAddress))) {
          macAddress = "";
        }
      }catch (Exception e){
        e.printStackTrace();
      }
    }
    SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME, 0);
    SharedPreferences.Editor editor = sharedPrefs.edit();
    
    String str = deviceId + macAddress + imei + imsi;
    if (StringUtils.isEmpty(str)){
      if (sharedPrefs.contains("EMULATOR_DEVICE_ID")){
        clientId = sharedPrefs.getString("EMULATOR_DEVICE_ID", "");
      }else{
        clientId = ("EMU" + new Random(System.currentTimeMillis()).nextLong()).toLowerCase(Locale.getDefault());
        editor.putString("EMULATOR_DEVICE_ID", deviceId);
        editor.commit();
      }
    }
    else {
      clientId = MD5Util.getMD5String(str);
    }
    editor.putString("CLIENT_ID", clientId);
    editor.commit();
    
    return clientId;
  }
  
  private static boolean isNormal(String param){
    if ((param.contains("00000000")) || 
      (param.contains("11111111")) || 
      (param.contains("12345678")) || 
      (StringUtils.isOrder(param)) || 
      (StringUtils.isSame(param))) {
      return false;
    }
    return true;
  }
}
