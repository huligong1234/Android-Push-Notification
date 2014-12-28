package org.jeedevframework.jpush.android.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

public final class JPushManager
{
  private Context mContext;
  private static JPushManager instance;
  private ServiceManager serviceManager;
  private String version = "0.0.1";
  private String xmppHost = "192.168.1.105";
  private String xmppPort = "5222";
  private SharedPreferences sharedPrefs;
  
  public JPushManager initialize(Context context) {
    this.mContext = context;
    
    this.serviceManager = new ServiceManager(context, this.xmppHost, this.xmppPort, this.version);
    this.serviceManager.startService();
    
    this.sharedPrefs = context.getSharedPreferences(JPushInterface.SHARED_PREFERENCE_NAME, 0);
    
    JPushInterface.initDeviceStatus(context);
    return instance;
  }
  
  private ServiceManager getServiceManager() {
    if (this.serviceManager == null) {
      initialize(this.mContext);
    }
    return this.serviceManager;
  }
  
  public static synchronized JPushManager getInstance() {
    if (instance == null) {
    	synchronized (JPushManager.class) {
    		instance = new JPushManager();
		}
    }
    return instance;
  }
  
  public JPushManager setCallBackHandler(Handler callBackHandler) {
    getServiceManager().setCallBackHandler(callBackHandler);
    return instance;
  }
  
  public JPushManager turnOnPush(){
    getServiceManager().setNotification(Boolean.valueOf(true));
    return instance;
  }
  
  public JPushManager turnOffPush() {
    getServiceManager().setNotification(Boolean.valueOf(false));
    getServiceManager().stopService();
    return instance;
  }
  
  public JPushManager setDebugMode(Boolean enable) {
    getServiceManager().setDebugMode(enable);
    org.jeedevframework.jpush.android.utils.LogUtil.isDebugMode = enable;
    return instance;
  }
  
  public JPushManager setNotificationIcon(int iconId) {
    getServiceManager().setNotificationIcon(iconId);
    return instance;
  }
  
  public JPushManager setNotificationHandler(Boolean enable) {
    getServiceManager().setNotificationHandler(enable);
    return instance;
  }
  
  public JPushManager setNotificationSystemBar(Boolean enable) {
    getServiceManager().setNotificationSystemBar(enable);
    return instance;
  }
  
  public JPushManager setNotificationSound(Boolean enable) {
    getServiceManager().setNotificationSound(enable);
    return instance;
  }
  
  public JPushManager setNotificationVibrate(Boolean enable) {
    getServiceManager().setNotificationVibrate(enable);
    return instance;
  }
  
  public JPushManager setNotificationToast(Boolean enable) {
    getServiceManager().setNotificationToast(enable);
    return instance;
  }
  
  public String getVersion() {
    return this.version;
  }
  
  public String getClientID() {
    return this.sharedPrefs.getString("DEVICE_ID", "");
  }
}
