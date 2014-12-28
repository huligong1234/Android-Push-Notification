package org.jeedevframework.jpush.android.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils
{
  public static final String EMPTY = "";
  
  public static boolean isBlank(String str)
  {
    return (str == null) || (str.trim().length() == 0);
  }
  
  public static String trimToEmpty(String str)
  {
    return str == null ? "" : str.trim();
  }
  
  public static boolean isEmpty(String str)
  {
    return (str == null) || (str.length() == 0);
  }
  
  public static boolean isEquals(String actual, String expected)
  {
    return ObjectUtils.isEquals(actual, expected);
  }
  
  public static String nullStrToEmpty(String str)
  {
    return str == null ? "" : str;
  }
  
  public static boolean isNumeric(String str)
  {
    if (str == null) {
      return false;
    }
    int sz = str.length();
    for (int i = 0; i < sz; i++) {
      if (!Character.isDigit(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  
  public static String capitalizeFirstLetter(String str)
  {
    if (isEmpty(str)) {
      return str;
    }
    char c = str.charAt(0);
    return str.length() + 
      Character.toUpperCase(c) + str.substring(1);
  }
  
  public static String utf8Encode(String str)
  {
    if ((!isEmpty(str)) && (str.getBytes().length != str.length())) {
      try
      {
        return URLEncoder.encode(str, "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
        throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
      }
    }
    return str;
  }
  
  public static String utf8Encode(String str, String defultReturn)
  {
    if ((!isEmpty(str)) && (str.getBytes().length != str.length())) {
      try
      {
        return URLEncoder.encode(str, "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
        return defultReturn;
      }
    }
    return str;
  }
  
  public static String getHrefInnerHtml(String href)
  {
    if (isEmpty(href)) {
      return "";
    }
    String hrefReg = ".*<[\\s]*a[\\s]*.*>(.+?)<[\\s]*/a[\\s]*>.*";
    Pattern hrefPattern = Pattern.compile(hrefReg, 2);
    Matcher hrefMatcher = hrefPattern.matcher(href);
    if (hrefMatcher.matches()) {
      return hrefMatcher.group(1);
    }
    return href;
  }
  
  public static String htmlEscapeCharsToString(String source)
  {
    return isEmpty(source) ? source : source.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
      .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
  }
  
  public static String fullWidthToHalfWidth(String s)
  {
    if (isEmpty(s)) {
      return s;
    }
    char[] source = s.toCharArray();
    for (int i = 0; i < source.length; i++) {
      if (source[i] == '　') {
        source[i] = ' ';
      } else if ((source[i] >= 65281) && (source[i] <= 65374)) {
        source[i] = ((char)(source[i] - 65248));
      } else {
        source[i] = source[i];
      }
    }
    return new String(source);
  }
  
  public static String halfWidthToFullWidth(String s)
  {
    if (isEmpty(s)) {
      return s;
    }
    char[] source = s.toCharArray();
    for (int i = 0; i < source.length; i++) {
      if (source[i] == ' ') {
        source[i] = '　';
      } else if ((source[i] >= '!') && (source[i] <= '~')) {
        source[i] = ((char)(source[i] + 65248));
      } else {
        source[i] = source[i];
      }
    }
    return new String(source);
  }
  
  static String orderStr = "";
  
  static
  {
    for (int i = 33; i < 127; i++) {
      orderStr += Character.toChars(i)[0];
    }
  }
  
  public static boolean isOrder(String str)
  {
    if (!str.matches("((\\d)|([a-z])|([A-Z]))+")) {
      return false;
    }
    return orderStr.contains(str);
  }
  
  public static boolean isSame(String str)
  {
    String regex = str.substring(0, 1) + "{" + str.length() + "}";
    return str.matches(regex);
  }
}
