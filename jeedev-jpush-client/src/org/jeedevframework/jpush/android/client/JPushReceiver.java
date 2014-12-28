package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;
import org.jeedevframework.jpush.android.utils.StringUtils;
import org.jivesoftware.smack.packet.IQ;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.SystemClock;

public class JPushReceiver extends BroadcastReceiver {
  private static final String LOGTAG = LogUtil.makeLogTag(JPushReceiver.class);
  private static boolean alarmReceiverFlag = true;
  private String callbackActivityPackageName;
  private String callbackActivityClassName;
  
  public void onReceive(Context context, Intent intent) {
    SharedPreferences sharedPrefs = context.getSharedPreferences(JPushInterface.SHARED_PREFERENCE_NAME, 0);
    this.callbackActivityPackageName = sharedPrefs.getString("CALLBACK_ACTIVITY_PACKAGE_NAME", "");
    this.callbackActivityClassName = sharedPrefs.getString("CALLBACK_ACTIVITY_CLASS_NAME", "");
    if (JPushInterface.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())){
    	String notificationId = intent.getStringExtra(JPushInterface.NOTIFICATION_ID);
        String notificationApiKey = intent.getStringExtra(JPushInterface.NOTIFICATION_API_KEY);
        String notificationShowType = intent.getStringExtra(JPushInterface.NOTIFICATION_SHOW_TYPE);
        String notificationTitle = intent.getStringExtra(JPushInterface.NOTIFICATION_TITLE);
        String notificationMessage = intent.getStringExtra(JPushInterface.NOTIFICATION_MESSAGE);
        String notificationUri = intent.getStringExtra(JPushInterface.NOTIFICATION_URI);
        String notificationFrom = intent.getStringExtra(JPushInterface.NOTIFICATION_FROM);
        String packetId = intent.getStringExtra(JPushInterface.NOTIFICATION_PACKET_ID);
      
		LogUtil.d(LOGTAG, "notificationId=" + notificationId);
		LogUtil.d(LOGTAG, "notificationApiKey=" + notificationApiKey);
		LogUtil.d(LOGTAG, "notificationShowType=" + notificationShowType);
		LogUtil.d(LOGTAG, "notificationTitle=" + notificationTitle);
		LogUtil.d(LOGTAG, "notificationMessage=" + notificationMessage);
		LogUtil.d(LOGTAG, "notificationUri=" + notificationUri);
		LogUtil.d(LOGTAG, "notificationFrom=" + notificationFrom);
		LogUtil.d(LOGTAG, "notificationPacketId=" + packetId);

      IQ result = new IQ() {
        public String getChildElementXML(){
          return null;
        }
      };
      result.setType(IQ.Type.RESULT);
      result.setPacketID(packetId);
      result.setTo(notificationFrom);
      try{
        JPushInterface.xmppManager.getConnection().sendPacket(result);
      }catch (Exception e){
        LogUtil.e(LOGTAG, "sendPacket is error", e);
      }
      
      
      if (!StringUtils.isEmpty(notificationUri)){
        if (notificationUri.toLowerCase().contains(".apk")){
          //
        }else{
          Intent it = new Intent("android.intent.action.VIEW", Uri.parse(notificationUri));
          it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(it);
        }
      }else if ((!StringUtils.isEmpty(this.callbackActivityPackageName)) && (!StringUtils.isEmpty(this.callbackActivityClassName))){
        intent = new Intent().setClassName(this.callbackActivityPackageName, this.callbackActivityClassName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        // intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        context.startActivity(intent);
      }
    }
    else if (alarmReceiverFlag) {
      alarmReceiverFlag = false;
      Intent intenti = new Intent(context, AlarmReceiver.class);
      intenti.setAction(AlarmReceiver.class.getName());
      PendingIntent sender = PendingIntent.getBroadcast(context, 0, intenti, 0);
      long firstime = SystemClock.elapsedRealtime();
      AlarmManager am = (AlarmManager)context.getSystemService("alarm");
      
      am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 30000L, sender);
    }
  }
}
