package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityReceiver extends BroadcastReceiver
{
  private static final String LOGTAG = LogUtil.makeLogTag(ConnectivityReceiver.class);
  private NotificationService notificationService;
  
  public ConnectivityReceiver(NotificationService notificationService) {
    this.notificationService = notificationService;
  }
  
  public void onReceive(Context context, Intent intent) {
    LogUtil.d(LOGTAG, "ConnectivityReceiver.onReceive()...");
    String action = intent.getAction();
    LogUtil.d(LOGTAG, "action=" + action);
    
    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService("connectivity");
    
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    if (networkInfo != null) {
      LogUtil.d(LOGTAG, "Network Type  = " + networkInfo.getTypeName());
      LogUtil.d(LOGTAG, "Network State = " + networkInfo.getState());
      if (networkInfo.isConnected())  {
        LogUtil.i(LOGTAG, "Network connected");
        this.notificationService.connect();
      }
    } else {
      LogUtil.e(LOGTAG, "Network unavailable");
      this.notificationService.disconnect();
    }
  }
}
