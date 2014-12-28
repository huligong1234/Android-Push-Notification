package org.jeedevframework.jpush.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util
{
  protected static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', 
    '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  protected static MessageDigest messagedigest = null;
  
  static
  {
    try
    {
      messagedigest = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException nsaex)
    {
      System.err.println(MD5Util.class.getName() + 
        "初始化失败，MessageDigest不支持MD5Util。");
      nsaex.printStackTrace();
    }
  }
  
  public static String getMD5String(String s)
  {
    return getMD5String(s.getBytes());
  }
  
  public static boolean checkPassword(String password, String md5PwdStr)
  {
    String s = getMD5String(password);
    return s.equals(md5PwdStr);
  }
  
  public static String getFileMD5String(File file)
    throws IOException
  {
    InputStream fis = new FileInputStream(file);
    byte[] buffer = new byte[1024];
    int numRead = 0;
    while ((numRead = fis.read(buffer)) > 0) {
      messagedigest.update(buffer, 0, numRead);
    }
    fis.close();
    return bufferToHex(messagedigest.digest());
  }
  
  public static String getFileMD5String_old(File file)
    throws IOException
  {
    FileInputStream in = new FileInputStream(file);
    FileChannel ch = in.getChannel();
    MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0L, 
      file.length());
    messagedigest.update(byteBuffer);
    return bufferToHex(messagedigest.digest());
  }
  
  public static String getMD5String(byte[] bytes)
  {
    messagedigest.update(bytes);
    return bufferToHex(messagedigest.digest());
  }
  
  private static String bufferToHex(byte[] bytes)
  {
    return bufferToHex(bytes, 0, bytes.length);
  }
  
  private static String bufferToHex(byte[] bytes, int m, int n)
  {
    StringBuffer stringbuffer = new StringBuffer(2 * n);
    int k = m + n;
    for (int l = m; l < k; l++) {
      appendHexPair(bytes[l], stringbuffer);
    }
    return stringbuffer.toString();
  }
  
  private static void appendHexPair(byte bt, StringBuffer stringbuffer)
  {
    char c0 = hexDigits[((bt & 0xF0) >> 4)];
    char c1 = hexDigits[(bt & 0xF)];
    stringbuffer.append(c0);
    stringbuffer.append(c1);
  }
  
  public static void main(String[] args)
  {
    System.out.println(getMD5String("123456"));
  }
}