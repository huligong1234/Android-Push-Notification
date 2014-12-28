package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
  private static final String LOGTAG = LogUtil.makeLogTag(AlarmReceiver.class);
  
  public void onReceive(Context context, Intent intent) {
    final Context contextf = context;
    if (!ServiceManager.isServiceRunning(context, NotificationService.class.getName())) {
      Thread serviceThread = new Thread(new Runnable() {
        public void run() {
          Intent intent = new Intent(contextf, NotificationService.class);
          contextf.startService(intent);
        }
      });
      serviceThread.start();
    } else {
      try {
        JPushInterface.xmppManager.getConnection().startKeepAliveThread(JPushInterface.xmppManager);
      }
      catch (Exception e) {
        LogUtil.d(LOGTAG, e.getMessage());
      }
    }
  }
}
