package org.jeedevframework.jpush.android.client;

import java.util.List;
import java.util.Properties;

import org.jeedevframework.jpush.android.utils.LogUtil;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

public class ServiceManager
{
  private static final String LOGTAG = LogUtil.makeLogTag(ServiceManager.class);
  private Context context;
  private SharedPreferences sharedPrefs;
  private Properties props;
  private String callbackActivityPackageName;
  private String callbackActivityClassName;
  private String apiKey = "1234567890";
  private static Handler callBackHandler;
  
  public ServiceManager(Context context, String xmppHost, String xmppPort, String version) {
    this.context = context;
    
    if ((context instanceof Activity)) {
      LogUtil.i(LOGTAG, "Callback Activity...");
      Activity callbackActivity = (Activity)context;
      this.callbackActivityPackageName = callbackActivity.getPackageName();
      this.callbackActivityClassName = callbackActivity.getClass().getName();
    }
    this.apiKey = getMetaDataValue("JPUSH_APP_ID");
    

    LogUtil.i(LOGTAG, "apiKey=" + this.apiKey);
    LogUtil.i(LOGTAG, "xmppHost=" + xmppHost);
    LogUtil.i(LOGTAG, "xmppPort=" + xmppPort);
    
    this.sharedPrefs = context.getSharedPreferences(JPushInterface.SHARED_PREFERENCE_NAME, 0);
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putString("API_KEY", this.apiKey);
    editor.putString("VERSION", version);
    editor.putString("XMPP_HOST", xmppHost);
    editor.putInt("XMPP_PORT", Integer.parseInt(xmppPort));
    editor.putString("CALLBACK_ACTIVITY_PACKAGE_NAME", this.callbackActivityPackageName);
    editor.putString("CALLBACK_ACTIVITY_CLASS_NAME", this.callbackActivityClassName);
    editor.commit();
  }
  
  public void startService() {
    Thread serviceThread = new Thread(new Runnable() {
      public void run() {
        Intent intent = NotificationService.getIntent();
        ServiceManager.this.context.startService(intent);
      }
    });
    serviceThread.start();
  }
  
  public void stopService() {
    Intent intent = NotificationService.getIntent();
    this.context.stopService(intent);
  }
  
  public static Handler getCallBackHandler() {
    return callBackHandler;
  }
  
  public void setCallBackHandler(Handler p_callBackHandler){
    callBackHandler = p_callBackHandler;
  }
  
  private String getMetaDataValue(String name, String def) {
    String value = getMetaDataValue(name);
    return value == null ? def : value;
  }
  
  private String getMetaDataValue(String name) {
    Object value = null;
    PackageManager packageManager = this.context.getPackageManager();
    try {
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.context.getPackageName(), 128);
      if ((applicationInfo != null) && (applicationInfo.metaData != null)) {
        value = applicationInfo.metaData.get(name);
      }
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException("Could not read the name in the manifest file.", e);
    }
    ApplicationInfo applicationInfo;
    if (value == null) {
      throw new RuntimeException("The name '" + name + "' is not defined in the manifest file's meta data.");
    }
    return value.toString();
  }
  
  private Properties loadProperties() {
    Properties props = new Properties();
    try {
      int id = this.context.getResources().getIdentifier("jpush", "raw", this.context.getPackageName());
      props.load(this.context.getResources().openRawResource(id));
    }
    catch (Exception e) {
      LogUtil.e(LOGTAG, "Could not find the properties file.", e);
    }
    return props;
  }
  
  public static boolean isServiceRunning(Context mContext, String className)  {
    boolean isRunning = false;
    ActivityManager activityManager = (ActivityManager)mContext.getSystemService("activity");
    List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
    if (serviceList.size() <= 0) {
      return false;
    }
    for (int i = 0; i < serviceList.size(); i++) {
      if (((ActivityManager.RunningServiceInfo)serviceList.get(i)).service.getClassName().equals(className))
      {
        isRunning = true;
        break;
      }
    }
    return isRunning;
  }
  
  public void setDebugMode(Boolean enable) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putBoolean("SETTINGS_DEBUG_ENABLED", enable.booleanValue());
    editor.commit();
  }
  
  public void setNotificationIcon(int iconId) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putInt("NOTIFICATION_ICON", iconId);
    editor.commit();
  }
  
  public void setNotificationHandler(Boolean enable) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putBoolean("SETTINGS_HANDLER_ENABLED", enable.booleanValue());
    editor.commit();
  }
  
  public void setNotificationSystemBar(Boolean enable) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putBoolean("SETTINGS_SYSTEM_BAR_ENABLED", enable.booleanValue());
    editor.commit();
  }
  
  public void setNotificationSound(Boolean enable) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putBoolean("SETTINGS_SOUND_ENABLED", enable.booleanValue());
    editor.commit();
  }
  
  public void setNotificationVibrate(Boolean enable) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putBoolean("SETTINGS_VIBRATE_ENABLED", enable.booleanValue());
    editor.commit();
  }
  
  public void setNotificationToast(Boolean enable) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putBoolean("SETTINGS_TOAST_ENABLED", enable.booleanValue());
    editor.commit();
  }
  
  public void setNotification(Boolean enable) {
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putBoolean("SETTINGS_NOTIFICATION_ENABLED", enable.booleanValue());
    editor.commit();
  }
}
