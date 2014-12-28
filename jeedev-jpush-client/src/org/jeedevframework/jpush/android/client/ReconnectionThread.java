package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;

public class ReconnectionThread extends Thread {
  private static final String LOGTAG = LogUtil.makeLogTag(ReconnectionThread.class);
  private final XmppManager xmppManager;
  private int waiting;
  
  ReconnectionThread(XmppManager xmppManager) {
    this.xmppManager = xmppManager;
    this.waiting = 0;
  }
  
  public void run() {
      try {
          while (!isInterrupted()) {
              LogUtil.d(LOGTAG, "Trying to reconnect in " + waiting()
                      + " seconds");
              Thread.sleep((long) waiting() * 1000L);
              xmppManager.connect();
              waiting++;
          }
      } catch (final InterruptedException e) {
          xmppManager.getHandler().post(new Runnable() {
              public void run() {
                  xmppManager.getConnectionListener().reconnectionFailed(e);
              }
          });
      }
  }
  
  private int waiting() {
    if (this.waiting < 5) {
      return 5;
    }
    if (this.waiting < 10) {
      return 10;
    }
    if (this.waiting < 30) {
      return 30;
    }
    return this.waiting < 50 ? 300 : 600;
  }
  
  public void resetWaiting(){
    this.waiting = 0;
  }
}
