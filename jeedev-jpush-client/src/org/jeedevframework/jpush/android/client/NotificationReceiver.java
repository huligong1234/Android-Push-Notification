package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class NotificationReceiver extends BroadcastReceiver {
  private static final String LOGTAG = LogUtil.makeLogTag(NotificationReceiver.class);
  
  public void onReceive(Context context, Intent intent) {
    LogUtil.d(LOGTAG, "NotificationReceiver.onReceive()...");
    String action = intent.getAction();
    LogUtil.d(LOGTAG, "action=" + action);
    if (JPushInterface.ACTION_SHOW_NOTIFICATION.equals(action)) {
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
      
      Notifier notifier = new Notifier(context);
      notifier.notify(notificationId, notificationApiKey, notificationShowType, notificationTitle, notificationMessage, notificationUri, notificationFrom, packetId);
    }
  }
}
