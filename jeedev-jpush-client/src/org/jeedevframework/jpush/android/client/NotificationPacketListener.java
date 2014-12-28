package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import android.content.Intent;

public class NotificationPacketListener implements PacketListener {
  private static final String LOGTAG = LogUtil.makeLogTag(NotificationPacketListener.class);
  private final XmppManager xmppManager;
  
  public NotificationPacketListener(XmppManager xmppManager) {
    this.xmppManager = xmppManager;
  }
  
  public void processPacket(Packet packet) {
    LogUtil.d(LOGTAG, "NotificationPacketListener.processPacket()...");
    LogUtil.d(LOGTAG, "packet.toXML()=" + packet.toXML());
    if ((packet instanceof NotificationIQ)) {
      NotificationIQ notification = (NotificationIQ)packet;
      if (notification.getChildElementXML().contains("jpush:iq:notification")) {
        String notificationId = notification.getId();
        String notificationApiKey = notification.getApiKey();
        String notificationShowType = notification.getShowType();
        String notificationTitle = notification.getTitle();
        String notificationMessage = notification.getMessage();
        
        String notificationUri = notification.getUri();
        String notificationFrom = notification.getFrom();
        String packetId = notification.getPacketID();
        
        Intent intent = new Intent(JPushInterface.ACTION_SHOW_NOTIFICATION);
        intent.putExtra(JPushInterface.NOTIFICATION_ID, notificationId);
        intent.putExtra(JPushInterface.NOTIFICATION_SHOW_TYPE, notificationShowType);
        intent.putExtra(JPushInterface.NOTIFICATION_API_KEY, notificationApiKey);
        intent.putExtra(JPushInterface.NOTIFICATION_TITLE, notificationTitle);
        intent.putExtra(JPushInterface.NOTIFICATION_MESSAGE, notificationMessage);
        intent.putExtra(JPushInterface.NOTIFICATION_URI, notificationUri);
        intent.putExtra(JPushInterface.NOTIFICATION_FROM, notificationFrom);
        intent.putExtra(JPushInterface.NOTIFICATION_PACKET_ID, packetId);

        IQ result = NotificationIQ.createResultIQ(notification);
        try{
          this.xmppManager.getConnection().sendPacket(result);
        } catch (Exception e){
          e.printStackTrace();
        }
        this.xmppManager.getContext().sendBroadcast(intent);
      }
    }
  }
}
