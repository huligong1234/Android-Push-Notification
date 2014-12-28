package org.jeedevframework.jpush.android.utils;

import android.util.Log;

public class LogUtil
{
  static String tag = "JPush_debug!";
  public static Boolean isDebugMode = Boolean.valueOf(false);
  
  public static String makeLogTag(Class cls)
  {
    return "JPush_" + cls.getSimpleName();
  }
  
  public static void i(String tag, String info) {}
  
  public static void e(String tag, String info)
  {
    Log.e(tag, info);
  }
  
  public static void e(String tag, String info, Throwable tr)
  {
    Log.e(tag, info, tr);
  }
  
  public static void w(String tag, String info)
  {
    Log.w(tag, info);
  }
  
  public static void d(String tag, String info)
  {
    if (isDebugMode.booleanValue()) {
      Log.d(tag, info);
    }
  }
  
  public static void d(String info)
  {
    if (isDebugMode.booleanValue()) {
      Log.d(tag, info);
    }
  }
}
