package org.jeedevframework.jpush.android.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

import org.jeedevframework.jpush.android.utils.LogUtil;
import org.jeedevframework.jpush.android.utils.StringUtils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.ProviderManager;

public class XmppManager
{
  private static final String LOGTAG = LogUtil.makeLogTag(XmppManager.class);
  private static final String XMPP_RESOURCE_NAME = "JPush";
  private Context context;
  private NotificationService.TaskSubmitter taskSubmitter;
  private NotificationService.TaskTracker taskTracker;
  private SharedPreferences sharedPrefs;
  private String xmppHost;
  private int xmppPort;
  private XMPPConnection connection;
  private String username;
  private String password;
  private ConnectionListener connectionListener;
  private PacketListener notificationPacketListener;
  private Handler handler;
  private List<Runnable> taskList;
  private boolean running = false;
  private Future<?> futureTask;
  private Thread reconnection;
  private String deviceId;
  private String clientId;
  
  public XmppManager(NotificationService notificationService) {
    this.context = notificationService;
    this.deviceId = notificationService.getDeviceId();
    this.clientId = JPushInterface.getClientId(this.context);
    this.taskSubmitter = notificationService.getTaskSubmitter();
    this.taskTracker = notificationService.getTaskTracker();
    this.sharedPrefs = notificationService.getSharedPreferences();
    
    this.xmppHost = this.sharedPrefs.getString("XMPP_HOST", "localhost");
    this.xmppPort = this.sharedPrefs.getInt("XMPP_PORT", 5222);
    this.username = this.sharedPrefs.getString("XMPP_USERNAME", "");
    this.password = this.sharedPrefs.getString("XMPP_PASSWORD", "");
    
    this.connectionListener = new PersistentConnectionListener(this);
    this.notificationPacketListener = new NotificationPacketListener(this);
    
    this.handler = new Handler();
    this.taskList = new ArrayList();
    this.reconnection = new ReconnectionThread(this);
  }
  
  public Context getContext() {
    return this.context;
  }
  
  public void connect() {
    LogUtil.d(LOGTAG, "connect()...");
    submitLoginTask();
  }
  
  public void disconnect() {
    LogUtil.d(LOGTAG, "disconnect()...");
    terminatePersistentConnection();
  }
  
  public void terminatePersistentConnection(){
    LogUtil.d(LOGTAG, "terminatePersistentConnection()...");
    Runnable runnable = new Runnable() {
      final XmppManager xmppManager = XmppManager.this;
      
      public void run() {
        if (this.xmppManager.isConnected()) {
          LogUtil.d(XmppManager.LOGTAG, "terminatePersistentConnection()... run()");
          this.xmppManager.getConnection().removePacketListener(
            this.xmppManager.getNotificationPacketListener());
          this.xmppManager.getConnection().disconnect();
        }
        this.xmppManager.runTask();
      }
    };
    addTask(runnable);
  }
  
  public XMPPConnection getConnection() {
    return this.connection;
  }
  
  public void setConnection(XMPPConnection connection){
    this.connection = connection;
  }
  
  public String getUsername() {
    return this.username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public String getPassword() {
    return this.password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public ConnectionListener getConnectionListener(){
    return this.connectionListener;
  }
  
  public PacketListener getNotificationPacketListener() {
    return this.notificationPacketListener;
  }
  
  public void startReconnectionThread() {
    synchronized (this.reconnection) {
      if (!this.reconnection.isAlive()) {
        this.reconnection.setName("Xmpp Reconnection Thread");
        this.reconnection.start();
      }
    }
  }
  
  public Handler getHandler() {
    return this.handler;
  }
  
  public void reregisterAccount() {
    removeAccount();
    submitLoginTask();
    runTask();
  }
  
  public List<Runnable> getTaskList() {
    return this.taskList;
  }
  
  public Future<?> getFutureTask() {
    return this.futureTask;
  }
  
  public void runTask(){
    LogUtil.d(LOGTAG, "runTask()...");
    synchronized (this.taskList){
      this.running = false;
      this.futureTask = null;
      if (!this.taskList.isEmpty()) {
        Runnable runnable = (Runnable)this.taskList.get(0);
        this.taskList.remove(0);
        this.running = true;
        this.futureTask = this.taskSubmitter.submit(runnable);
        if (this.futureTask == null) {
          this.taskTracker.decrease();
        }
      }
    }
    this.taskTracker.decrease();
    LogUtil.d(LOGTAG, "runTask()...done");
  }
  
  private String newRandomUUID(){
    String uuidRaw = UUID.randomUUID().toString();
    return uuidRaw.replaceAll("-", "");
  }
  
  private synchronized String getUUID(){
    if (StringUtils.isEmpty(this.clientId)) {
      this.clientId = JPushInterface.getClientId(this.context);
    }
    return this.clientId;
  }
  
  private boolean isConnected(){
    return (this.connection != null) && (this.connection.isConnected());
  }
  
  private boolean isAuthenticated(){
    return (this.connection != null) && (this.connection.isConnected()) && (this.connection.isAuthenticated());
  }
  
  private boolean isRegistered(){
    return (this.sharedPrefs.contains("XMPP_USERNAME")) && (this.sharedPrefs.contains("XMPP_PASSWORD"));
  }
  
  private void submitConnectTask() {
      LogUtil.d(LOGTAG, "submitConnectTask()...");
      addTask(new ConnectTask());
  }

  private void submitRegisterTask() {
	  LogUtil.d(LOGTAG, "submitRegisterTask()...");
      submitConnectTask();
      addTask(new RegisterTask());
  }

  private void submitLoginTask() {
	  LogUtil.d(LOGTAG, "submitLoginTask()...");
      submitRegisterTask();
      addTask(new LoginTask());
  }
  
  private void addTask(Runnable runnable) {
    LogUtil.d(LOGTAG, "addTask(runnable)...");
    this.taskTracker.increase();
    synchronized (this.taskList) {
      if ((this.taskList.isEmpty()) && (!this.running)) {
        this.running = true;
        this.futureTask = this.taskSubmitter.submit(runnable);
        if (this.futureTask == null) {
          this.taskTracker.decrease();
        }
      }else{
        runTask();
        this.taskList.add(runnable);
      }
    }
    LogUtil.d(LOGTAG, "addTask(runnable)... done");
  }
  
  private void removeAccount(){
    SharedPreferences.Editor editor = this.sharedPrefs.edit();
    editor.remove("XMPP_USERNAME");
    editor.remove("XMPP_PASSWORD");
    editor.commit();
  }
  
  private class ConnectTask implements Runnable {
    final XmppManager xmppManager;
    
    private ConnectTask(){
      this.xmppManager = XmppManager.this;
    }
    
    public void run(){
      LogUtil.i(XmppManager.LOGTAG, "ConnectTask.run()...");
      if (!this.xmppManager.isConnected()){
        ConnectionConfiguration connConfig = new ConnectionConfiguration(XmppManager.this.xmppHost, XmppManager.this.xmppPort);
        
        connConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
        connConfig.setSASLAuthenticationEnabled(false);
        connConfig.setCompressionEnabled(false);
        
        XMPPConnection connection = new XMPPConnection(connConfig);
        this.xmppManager.setConnection(connection);
        try{
          connection.connect();
          LogUtil.i(XmppManager.LOGTAG, "XMPP connected successfully");
          

          ProviderManager.getInstance().addIQProvider("notification", "jpush:iq:notification", new NotificationIQProvider());
        } catch (XMPPException e) {
          LogUtil.e(XmppManager.LOGTAG, "XMPP connection failed", e);
        }
        this.xmppManager.runTask();
      } else{
        LogUtil.i(XmppManager.LOGTAG, "XMPP connected already");
        this.xmppManager.runTask();
      }
    }
  }
  
  private class RegisterTask implements Runnable {
    final XmppManager xmppManager;
    
    private RegisterTask(){
      this.xmppManager = XmppManager.this;
    }
    
    public void run(){
      LogUtil.i(XmppManager.LOGTAG, "RegisterTask.run()...");
      if (!this.xmppManager.isRegistered()) {
        final String newUsername = XmppManager.this.getUUID();
        final String newPassword = XmppManager.this.getUUID();
        
        Registration registration = new Registration();
        
        PacketFilter packetFilter = new AndFilter(new PacketFilter[] { new PacketIDFilter(
          registration.getPacketID()), new PacketTypeFilter(
          IQ.class) });
        
        PacketListener packetListener = new PacketListener() {
          public void processPacket(Packet packet) {
            LogUtil.d("RegisterTask.PacketListener", "processPacket().....");
            LogUtil.d("RegisterTask.PacketListener", "packet=" + packet.toXML());
            if ((packet instanceof IQ)) {
              IQ response = (IQ)packet;
              if (response.getType() == IQ.Type.ERROR) {
                if (!response.getError().toString().contains("409")) {
                  LogUtil.e(XmppManager.LOGTAG, "Unknown error while registering XMPP account! " + response.getError().getCondition());
                }
              } else if (response.getType() == IQ.Type.RESULT){
                XmppManager.RegisterTask.this.xmppManager.setUsername(newUsername);
                XmppManager.RegisterTask.this.xmppManager.setPassword(newPassword);
                LogUtil.d(XmppManager.LOGTAG, "username=" + newUsername);
                LogUtil.d(XmppManager.LOGTAG, "password=" + newPassword);
                
                SharedPreferences.Editor editor = XmppManager.this.sharedPrefs.edit();
                editor.putString("XMPP_USERNAME", newUsername);
                editor.putString("XMPP_PASSWORD", newPassword);
                editor.commit();
                LogUtil.i(XmppManager.LOGTAG, "Account registered successfully");
                XmppManager.RegisterTask.this.xmppManager.runTask();
              }
            }
          }
        };
        XmppManager.this.connection.addPacketListener(packetListener, packetFilter);
        
        registration.setType(IQ.Type.SET);
        


        Object[] keys = JPushInterface.DEVICE_STATUS_MAP.keySet().toArray();
        for (Object key : keys) {
          if (JPushInterface.DEVICE_STATUS_MAP.get(key) != null) {
            registration.addAttribute(key.toString(), ((String)JPushInterface.DEVICE_STATUS_MAP.get(key)).toString());
          }
        }
        registration.addAttribute("username", newUsername);
        registration.addAttribute("password", newPassword);
        XmppManager.this.connection.sendPacket(registration);
      }
      else
      {
        LogUtil.i(XmppManager.LOGTAG, "Account registered already");
        this.xmppManager.runTask();
      }
    }
  }
  
  private class LoginTask implements Runnable {
    final XmppManager xmppManager;
    
    private LoginTask(){
      this.xmppManager = XmppManager.this;
    }
    
    public void run(){
      LogUtil.i(XmppManager.LOGTAG, "LoginTask.run()...");
      if (!this.xmppManager.isAuthenticated()){
        LogUtil.d(XmppManager.LOGTAG, "1-username=" + XmppManager.this.username);
        LogUtil.d(XmppManager.LOGTAG, "1-password=" + XmppManager.this.password);
       
        if ((StringUtils.isEmpty(XmppManager.this.username)) || (StringUtils.isEmpty(XmppManager.this.password))){
          XmppManager.this.username = XmppManager.this.getUUID();
          XmppManager.this.password = XmppManager.this.username;
        }
        
        LogUtil.d(XmppManager.LOGTAG, "2-username=" + XmppManager.this.username);
        LogUtil.d(XmppManager.LOGTAG, "2-password=" + XmppManager.this.password);
        
        try{
          this.xmppManager.getConnection().login(this.xmppManager.getUsername(), this.xmppManager.getPassword(), XMPP_RESOURCE_NAME);
          LogUtil.d(XmppManager.LOGTAG, "Loggedn in successfully");
          if (this.xmppManager.getConnectionListener() != null) {
            this.xmppManager.getConnection().addConnectionListener(this.xmppManager.getConnectionListener());
          }
          PacketFilter packetFilter = new PacketTypeFilter(NotificationIQ.class);
          
          PacketListener packetListener = this.xmppManager.getNotificationPacketListener();
          XmppManager.this.connection.addPacketListener(packetListener, packetFilter);
          

          XmppManager.this.getConnection().startKeepAliveThread(this.xmppManager);
        }catch (XMPPException e){
          LogUtil.e(XmppManager.LOGTAG, "LoginTask.run()... xmpp error");
          LogUtil.e(XmppManager.LOGTAG, "Failed to login to xmpp server. Caused by: " + 
            e.getMessage());
          String INVALID_CREDENTIALS_ERROR_CODE = "401";
          String errorMessage = e.getMessage();
          if (errorMessage != null) {
            if (errorMessage.contains(INVALID_CREDENTIALS_ERROR_CODE)){
              this.xmppManager.reregisterAccount();
              return;
            }
          }
          this.xmppManager.startReconnectionThread();
        }catch (Exception e){
          LogUtil.e(XmppManager.LOGTAG, "LoginTask.run()... other error");
          LogUtil.e(XmppManager.LOGTAG, "Failed to login to xmpp server. Caused by: " + 
            e.getMessage());
          this.xmppManager.startReconnectionThread();
        }
        this.xmppManager.runTask();
      }
      else{
        LogUtil.i(XmppManager.LOGTAG, "Logged in already");
        this.xmppManager.runTask();
      }
      ((ReconnectionThread)XmppManager.this.reconnection).resetWaiting();
    }
  }
}
