package org.jeedevframework.jpush.android.client;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jeedevframework.jpush.android.utils.LogUtil;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class NotificationService extends Service
{
  private static final String LOGTAG = LogUtil.makeLogTag(NotificationService.class);
  public static final String SERVICE_NAME = NotificationService.class.getName();
  private TelephonyManager telephonyManager;
  private BroadcastReceiver notificationReceiver;
  private BroadcastReceiver connectivityReceiver;
  private PhoneStateListener phoneStateListener;
  private ExecutorService executorService;
  private TaskSubmitter taskSubmitter;
  private TaskTracker taskTracker;
  private XmppManager xmppManager;
  private SharedPreferences sharedPrefs;
  private String deviceId;
  private WifiManager wifiManager;
  private ConnectivityManager connectivityManager;
  private PowerManager.WakeLock mWakeLock;
  
  public NotificationService() {
    this.notificationReceiver = new NotificationReceiver();
    this.connectivityReceiver = new ConnectivityReceiver(this);
    this.phoneStateListener = new PhoneStateChangeListener(this);
    this.executorService = Executors.newSingleThreadExecutor();
    this.taskSubmitter = new TaskSubmitter(this);
    this.taskTracker = new TaskTracker(this);
  }
  
  public void onCreate() {
    LogUtil.d(LOGTAG, "onCreate()...");
    this.telephonyManager = ((TelephonyManager)getSystemService("phone"));
    this.wifiManager = ((WifiManager)getSystemService("wifi"));
    this.connectivityManager = ((ConnectivityManager)getSystemService("connectivity"));
    
    this.sharedPrefs = getSharedPreferences(JPushInterface.SHARED_PREFERENCE_NAME, 0);
    

    this.deviceId = this.telephonyManager.getDeviceId();
    
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.putString("DEVICE_ID", this.deviceId);
    editor.commit();
    if ((this.deviceId == null) || (this.deviceId.trim().length() == 0) || (this.deviceId.matches("0+"))) {
      if (this.sharedPrefs.contains("EMULATOR_DEVICE_ID")) {
        this.deviceId = this.sharedPrefs.getString("EMULATOR_DEVICE_ID", "");
      } else {
        this.deviceId = ("EMU" + new Random(System.currentTimeMillis()).nextLong()).toLowerCase(Locale.getDefault());
        editor.putString("EMULATOR_DEVICE_ID", this.deviceId);
        editor.commit();
      }
    }
    LogUtil.d(LOGTAG, "deviceId=" + this.deviceId);
    
    this.xmppManager = new XmppManager(this);
    JPushInterface.xmppManager = this.xmppManager;
    
    this.taskSubmitter.submit(new Runnable(){
      public void run(){
        NotificationService.this.start();
      }
    });
    avoidBeKilled();
  }
  
  private void avoidBeKilled() {
    int ID = 1;
    Notification notification = new Notification();
    startForeground(1, notification);
  }
  
  public void onStart(Intent intent, int startId) {
    LogUtil.d(LOGTAG, "onStart()...");
  }
  
  public void onDestroy() {
    LogUtil.d(LOGTAG, "onDestroy()...");
    stop();
  }
  
  public IBinder onBind(Intent intent) {
    LogUtil.d(LOGTAG, "onBind()...");
    return null;
  }
  
  public void onRebind(Intent intent) {
    LogUtil.d(LOGTAG, "onRebind()...");
  }
  
  public boolean onUnbind(Intent intent) {
    LogUtil.d(LOGTAG, "onUnbind()...");
    return true;
  }
  
  private final void acquireWakeLock() {
    if (this.mWakeLock == null) {
      PowerManager pm = (PowerManager)getSystemService("power");
      this.mWakeLock = pm.newWakeLock(1, NotificationService.class.getName());
    }
    if (this.mWakeLock != null) {
      this.mWakeLock.acquire();
    }
  }
  
  private final void releaseWakeLock()
  {
    if (this.mWakeLock != null)
    {
      this.mWakeLock.release();
      this.mWakeLock = null;
    }
  }
  
  public static Intent getIntent()
  {
    return new Intent(NotificationService.class.getName());
  }
  
  public ExecutorService getExecutorService()
  {
    return this.executorService;
  }
  
  public TaskSubmitter getTaskSubmitter()
  {
    return this.taskSubmitter;
  }
  
  public TaskTracker getTaskTracker()
  {
    return this.taskTracker;
  }
  
  public XmppManager getXmppManager()
  {
    return this.xmppManager;
  }
  
  public SharedPreferences getSharedPreferences()
  {
    return this.sharedPrefs;
  }
  
  public String getDeviceId()
  {
    return this.deviceId;
  }
  
  public void connect()
  {
    LogUtil.d(LOGTAG, "connect()...");
    this.taskSubmitter.submit(new Runnable()
    {
      public void run()
      {
        NotificationService.this.getXmppManager().connect();
      }
    });
  }
  
  public void disconnect()
  {
    LogUtil.d(LOGTAG, "disconnect()...");
    this.taskSubmitter.submit(new Runnable()
    {
      public void run()
      {
        NotificationService.this.getXmppManager().disconnect();
      }
    });
  }
  
  private void registerNotificationReceiver()
  {
    IntentFilter filter = new IntentFilter();
    filter.addAction(JPushInterface.ACTION_SHOW_NOTIFICATION);
    filter.addAction(JPushInterface.ACTION_NOTIFICATION_CLICKED);
    filter.addAction(JPushInterface.ACTION_NOTIFICATION_CLEARED);
    registerReceiver(this.notificationReceiver, filter);
  }
  
  private void unregisterNotificationReceiver()
  {
    unregisterReceiver(this.notificationReceiver);
  }
  
  private void registerConnectivityReceiver()
  {
    LogUtil.d(LOGTAG, "registerConnectivityReceiver()...");
    this.telephonyManager.listen(this.phoneStateListener, 64);
    IntentFilter filter = new IntentFilter();
    
    filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
    registerReceiver(this.connectivityReceiver, filter);
  }
  
  private void unregisterConnectivityReceiver()
  {
    LogUtil.d(LOGTAG, "unregisterConnectivityReceiver()...");
    this.telephonyManager.listen(this.phoneStateListener, 0);
    unregisterReceiver(this.connectivityReceiver);
  }
  
  private void start()
  {
    LogUtil.d(LOGTAG, "start()...");
    registerNotificationReceiver();
    registerConnectivityReceiver();
    

    this.xmppManager.connect();
  }
  
  private void stop()
  {
    LogUtil.d(LOGTAG, "stop()...");
    unregisterNotificationReceiver();
    unregisterConnectivityReceiver();
    this.xmppManager.disconnect();
    this.executorService.shutdown();
  }
  
  public class TaskSubmitter
  {
    final NotificationService notificationService;
    
    public TaskSubmitter(NotificationService notificationService)
    {
      this.notificationService = notificationService;
    }
    
    public Future submit(Runnable task)
    {
      Future result = null;
      if ((!this.notificationService.getExecutorService().isTerminated()) && 
        (!this.notificationService.getExecutorService().isShutdown()) && 
        (task != null)) {
        result = this.notificationService.getExecutorService().submit(task);
      }
      return result;
    }
  }
  
  public class TaskTracker
  {
    final NotificationService notificationService;
    public int count;
    
    public TaskTracker(NotificationService notificationService)
    {
      this.notificationService = notificationService;
      this.count = 0;
    }
    
    public void increase()
    {
      synchronized (this.notificationService.getTaskTracker())
      {
        this.notificationService.getTaskTracker().count += 1;
        LogUtil.d(NotificationService.LOGTAG, "Incremented task count to " + this.count);
      }
    }
    
    public void decrease()
    {
      synchronized (this.notificationService.getTaskTracker())
      {
        this.notificationService.getTaskTracker().count -= 1;
        LogUtil.d(NotificationService.LOGTAG, "Decremented task count to " + this.count);
      }
    }
  }
}
