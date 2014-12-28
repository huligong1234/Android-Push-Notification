package org.jeedevframework.jpush.android.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jeedevframework.jpush.android.utils.LogUtil;

public class Notifier
{
  private static final String LOGTAG = LogUtil.makeLogTag(Notifier.class);
  private static final Random random = new Random(System.currentTimeMillis());
  private Context context;
  private SharedPreferences sharedPrefs;
  private NotificationManager notificationManager;
  
  public Notifier(Context context)
  {
    this.context = context;
    this.sharedPrefs = context.getSharedPreferences(JPushInterface.SHARED_PREFERENCE_NAME, 0);
    this.notificationManager = 
      ((NotificationManager)context.getSystemService("notification"));
  }
  
  public void notify(String notificationId, String apiKey, String showType, String title, String message, String uri, String from, String packetId)
  {
    LogUtil.d(LOGTAG, "notify()...");
    
    LogUtil.d(LOGTAG, "notificationId=" + notificationId);
    LogUtil.d(LOGTAG, "notificationApiKey=" + apiKey);
    LogUtil.d(LOGTAG, "notificationShowType=" + showType);
    LogUtil.d(LOGTAG, "notificationTitle=" + title);
    LogUtil.d(LOGTAG, "notificationMessage=" + message);
    LogUtil.d(LOGTAG, "notificationUri=" + uri);
    LogUtil.d(LOGTAG, "notificationFrom=" + from);
    LogUtil.d(LOGTAG, "notificationPacketId=" + packetId);
    if (isNotificationEnabled())
    {
      if (isNotificationToastEnabled()) {
        Toast.makeText(this.context, message, 1).show();
      }
      if (("PAYLOAD".equals(showType)) && (isNotificationHandlerEnabled()))
      {
        Handler callback = ServiceManager.getCallBackHandler();
        if (callback != null)
        {
          Map<String, String> objMap = new HashMap();
          objMap.put("NOTIFICATION_ID", notificationId);
          objMap.put("NOTIFICATION_API_KEY", apiKey);
          objMap.put("NOTIFICATION_SHOW_TYPE", showType);
          objMap.put("NOTIFICATION_TITLE", title);
          objMap.put("NOTIFICATION_MESSAGE", message);
          objMap.put("NOTIFICATION_URI", uri);
          objMap.put("NOTIFICATION_FROM", from);
          objMap.put("PACKET_ID", packetId);
          callback.sendMessage(Message.obtain(callback, 0, objMap));
        }
      }
      if (("NOTIFICATION".equals(showType)) && (isNotificationSystemBarEnabled()))
      {
        Notification notification = new Notification();
        notification.icon = getNotificationIcon();
        notification.defaults = 4;
        if (isNotificationSoundEnabled()) {
          notification.defaults |= 0x1;
        }
        if (isNotificationVibrateEnabled()) {
          notification.defaults |= 0x2;
        }
        notification.flags |= 0x10;
        notification.when = System.currentTimeMillis();
        notification.tickerText = message;
        

        Intent intent = new Intent(JPushReceiver.class.getName());
        
        intent.putExtra("NOTIFICATION_ID", notificationId);
        intent.putExtra("NOTIFICATION_API_KEY", apiKey);
        intent.putExtra("NOTIFICATION_TITLE", title);
        intent.putExtra("NOTIFICATION_MESSAGE", message);
        intent.putExtra("NOTIFICATION_URI", uri);
        intent.putExtra("NOTIFICATION_FROM", from);
        intent.putExtra("PACKET_ID", packetId);
        
        intent.setFlags(268435456);
        intent.setFlags(8388608);
        intent.setFlags(1073741824);
        intent.setFlags(536870912);
        intent.setFlags(67108864);
        
        PendingIntent contentIntent = PendingIntent.getActivity(this.context, random.nextInt(), 
          intent, 134217728);
        
        notification.setLatestEventInfo(this.context, title, message, contentIntent);
        this.notificationManager.notify(random.nextInt(), notification);
      }
    }
    else
    {
      LogUtil.w(LOGTAG, "Notificaitons disabled.");
    }
  }
  
  private int getNotificationIcon()
  {
    return this.sharedPrefs.getInt("NOTIFICATION_ICON", 0);
  }
  
  private boolean isNotificationEnabled()
  {
    return this.sharedPrefs.getBoolean("SETTINGS_NOTIFICATION_ENABLED", false);
  }
  
  private boolean isNotificationSoundEnabled()
  {
    return this.sharedPrefs.getBoolean("SETTINGS_SOUND_ENABLED", true);
  }
  
  private boolean isNotificationVibrateEnabled()
  {
    return this.sharedPrefs.getBoolean("SETTINGS_VIBRATE_ENABLED", true);
  }
  
  private boolean isNotificationToastEnabled()
  {
    return this.sharedPrefs.getBoolean("SETTINGS_TOAST_ENABLED", false);
  }
  
  private boolean isNotificationHandlerEnabled()
  {
    return this.sharedPrefs.getBoolean("SETTINGS_HANDLER_ENABLED", false);
  }
  
  private boolean isNotificationSystemBarEnabled()
  {
    return this.sharedPrefs.getBoolean("SETTINGS_SYSTEM_BAR_ENABLED", true);
  }
}
