package org.jeedevframework.jpush.android.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtil
{
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
  
  public static byte[] parseHexString(String str)
  {
    if (str == null) {
      return new byte[0];
    }
    StringBuffer sb = new StringBuffer(str);
    byte[] result = new byte[str.length() / 2];
    for (int i = 0; i < result.length; i++) {
      result[i] = ((byte)Integer.parseInt(sb.substring(i * 2, i * 2 + 2), 
        16));
    }
    return result;
  }
  
  public static String toHexString(byte[] bytes)
  {
    return toHexString(bytes, 0, bytes.length, 16, "");
  }
  
  public static String toHexString(byte[] bytes, int offset, int length)
  {
    return toHexString(bytes, offset, length, 16, " ");
  }
  
  public static String toHexString(byte[] bytes, int offset, int length, int lineWidth, String separator)
  {
    StringBuffer sb = new StringBuffer();
    if (offset < 0) {
      offset = 0;
    }
    if ((length < 0) || (length > bytes.length)) {
      length = bytes.length;
    }
    for (int i = offset; i < length; i++)
    {
      if ((lineWidth > 0) && (i % lineWidth == 0) && (sb.length() > 0)) {
        sb.append("\n");
      }
      if ((bytes[i] & 0xFF) < 16) {
        sb.append("0");
      }
      sb.append(Integer.toHexString(bytes[i] & 0xFF).toUpperCase());
      sb.append(separator);
    }
    return sb.toString();
  }
  
  public static String formatDate(Date date)
  {
    return dateFormatter.format(date);
  }
  
  public static String formatDateTime(Date date)
  {
    return dateTimeFormatter.format(date);
  }
  
  public static String formatTime(Date date)
  {
    return timeFormatter.format(date);
  }
  
  public static String getURLEncode(String s)
  {
    if (s == null) {
      return null;
    }
    return URLEncoder.encode(s);
  }
  
  public static String getURLDecode(String s)
  {
    if (s == null) {
      return null;
    }
    URLDecoder decoder = new URLDecoder();
    try
    {
      return URLDecoder.decode(s);
    }
    catch (Exception e) {}
    return null;
  }
  
  public static String getCurrentFormat(double toBeFormat, String pattern)
  {
    DecimalFormat df = new DecimalFormat(pattern);
    return df.format(toBeFormat);
  }
}
