package org.jeedevframework.jpush.android.client;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class NotificationIQProvider implements IQProvider {
  public IQ parseIQ(XmlPullParser parser) throws Exception {
    NotificationIQ notification = new NotificationIQ();
    for (boolean done = false; !done;) {
      int eventType = parser.next();
      if (eventType == 2) {
        if ("id".equals(parser.getName())) {
          notification.setId(parser.nextText());
        } else if ("apiKey".equals(parser.getName())) {
          notification.setApiKey(parser.nextText());
        } else if ("showType".equals(parser.getName())) {
          notification.setShowType(parser.nextText());
        } else if ("title".equals(parser.getName())) {
          notification.setTitle(parser.nextText());
        } else if ("message".equals(parser.getName())) {
          notification.setMessage(parser.nextText());
        } else if ("uri".equals(parser.getName())) {
          notification.setUri(parser.nextText());
        }
      } else if ((eventType == 3) && ("notification".equals(parser.getName()))) {
        done = true;
      }
    }
    return notification;
  }
}
