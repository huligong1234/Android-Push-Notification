package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;
import org.jivesoftware.smack.ConnectionListener;

import android.util.Log;

public class PersistentConnectionListener implements ConnectionListener {
  private static final String LOGTAG = LogUtil.makeLogTag(PersistentConnectionListener.class);
  private final XmppManager xmppManager;
  
  public PersistentConnectionListener(XmppManager xmppManager) {
    this.xmppManager = xmppManager;
  }
  
  public void connectionClosed() {
    Log.d(LOGTAG, "connectionClosed()...");
    if ((this.xmppManager.getConnection() != null) && 
      (this.xmppManager.getConnection().isConnected())) {
      this.xmppManager.getConnection().disconnect();
    }
    this.xmppManager.startReconnectionThread();
  }
  
  public void connectionClosedOnError(Exception e) {
    Log.d(LOGTAG, "connectionClosedOnError()...");
    if ((this.xmppManager.getConnection() != null) && 
      (this.xmppManager.getConnection().isConnected())) {
      this.xmppManager.getConnection().disconnect();
    }
    this.xmppManager.startReconnectionThread();
  }
  
  public void reconnectingIn(int seconds) {
    Log.d(LOGTAG, "reconnectingIn()...");
  }
  
  public void reconnectionFailed(Exception e) {
    Log.d(LOGTAG, "reconnectionFailed()...");
  }
  
  public void reconnectionSuccessful() {
    Log.d(LOGTAG, "reconnectionSuccessful()...");
  }
}
