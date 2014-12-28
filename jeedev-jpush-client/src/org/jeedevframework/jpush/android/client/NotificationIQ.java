package org.jeedevframework.jpush.android.client;

import org.jivesoftware.smack.packet.IQ;

public class NotificationIQ
  extends IQ
{
  private String id;
  private String apiKey;
  private String showType;
  private String title;
  private String message;
  private String uri;
  
  public String getChildElementXML()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("<").append("notification").append(" xmlns=\"").append(
      "jpush:iq:notification").append("\">");
    if (this.id != null) {
      buf.append("<id>").append(this.id).append("</id>");
    }
    buf.append("</").append("notification").append("> ");
    return buf.toString();
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public void setId(String id)
  {
    this.id = id;
  }
  
  public String getApiKey()
  {
    return this.apiKey;
  }
  
  public void setApiKey(String apiKey)
  {
    this.apiKey = apiKey;
  }
  
  public String getTitle()
  {
    return this.title;
  }
  
  public void setTitle(String title)
  {
    this.title = title;
  }
  
  public String getMessage()
  {
    return this.message;
  }
  
  public void setMessage(String message)
  {
    this.message = message;
  }
  
  public String getUri()
  {
    return this.uri;
  }
  
  public void setUri(String url)
  {
    this.uri = url;
  }
  
  public String getShowType()
  {
    return this.showType;
  }
  
  public void setShowType(String showType)
  {
    this.showType = showType;
  }
}
