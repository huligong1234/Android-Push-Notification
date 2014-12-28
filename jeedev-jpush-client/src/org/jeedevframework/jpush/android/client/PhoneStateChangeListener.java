package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;

import android.telephony.PhoneStateListener;

public class PhoneStateChangeListener extends PhoneStateListener {
  private static final String LOGTAG = LogUtil.makeLogTag(PhoneStateChangeListener.class);
  private final NotificationService notificationService;
  
  public PhoneStateChangeListener(NotificationService notificationService) {
    this.notificationService = notificationService;
  }
  
  public void onDataConnectionStateChanged(int state) {
    super.onDataConnectionStateChanged(state);
    LogUtil.d(LOGTAG, "onDataConnectionStateChanged()...");
    LogUtil.d(LOGTAG, "Data Connection State = " + getState(state));
    if (state == 2) {
      this.notificationService.connect();
    }
  }
  
  private String getState(int state) {
    switch (state) {
    case 0: 
      return "DATA_DISCONNECTED";
    case 1: 
      return "DATA_CONNECTING";
    case 2: 
      return "DATA_CONNECTED";
    case 3: 
      return "DATA_SUSPENDED";
    }
    return "DATA_<UNKNOWN>";
  }
}
