/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jeedevframework.jpush.server.xmpp.push;

import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.jeedevframework.jpush.server.model.NotificationMO;
import org.jeedevframework.jpush.server.model.User;
import org.jeedevframework.jpush.server.service.NotificationService;
import org.jeedevframework.jpush.server.service.ServiceLocator;
import org.jeedevframework.jpush.server.service.UserService;
import org.jeedevframework.jpush.server.util.CopyMessageUtil;
import org.jeedevframework.jpush.server.xmpp.session.ClientSession;
import org.jeedevframework.jpush.server.xmpp.session.SessionManager;
import org.xmpp.packet.IQ;

/**
 * This class is to manage sending the notifcations to the users.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationManager {

	private static final String NOTIFICATION_NAMESPACE = "jpush:iq:notification";

	private final Log log = LogFactory.getLog(getClass());

	private SessionManager sessionManager;
	
	private NotificationService notificationService;
	
	private UserService userService;

	/**
	 * Constructor.
	 */
	public NotificationManager() {
		sessionManager = SessionManager.getInstance();
		notificationService = ServiceLocator.getNotificationService();
		userService = ServiceLocator.getUserService();
	}
	
	

	/**
	 * Broadcasts a newly created notification message to all connected users.
	 * 
	 * @param apiKey
	 *            the API key
	 * @param title
	 *            the title
	 * @param message
	 *            the message details
	 * @param uri
	 *            the uri
	 */
	public void sendBroadcast(String apiKey,String showType, String title, String message,String uri,Integer timeToLive) {
		log.debug("sendBroadcast()...");
//		List<NotificationMO> notificationMOs = new ArrayList<NotificationMO>();
		IQ notificationIQ = createNotificationIQ(apiKey,"",showType, title, message, uri,timeToLive);

		for (ClientSession session : sessionManager.getSessions()) {
			// 创建通知对象
			NotificationMO notificationMO = new NotificationMO(apiKey,"",showType, title,message, uri,timeToLive);
			try {
				notificationMO.setUsername(session.getUsername());
				notificationMO.setClientIp(session.getHostAddress());
				notificationMO.setResource(session.getAddress().getResource());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 将消息的ID添加到通知对象
			CopyMessageUtil.IQ2Message(notificationIQ, notificationMO);
			notificationMO.setStatus(NotificationMO.STATUS_NOT_SEND);
			//入库
			try{
			notificationService.saveNotification(notificationMO);
			}catch(Exception e){
				log.warn(" notifications insert to database failure!!");
			}
			if (session.getPresence().isAvailable()) {
				
				notificationIQ.setTo(session.getAddress());
				
				session.deliver(notificationIQ);
			} 
			//将每个通知加入集合中
//			notificationMOs.add(notificationMO);
		}
//		try{
//			//批量入库
//			notificationService.createNotifications(notificationMOs);
//		}catch(Exception e){
//			log.warn(" notifications insert to database failure!!");
//		}
		
	}
	
	//给所有用户发送通知
	public void sendAllBroadcast(String apiKey,String showType, String title, String message,
			String uri,Integer timeToLive) {
		//生产推送通知对象，之所以移到这里来，是为了保证同一批次推送的通知ID是一样的。没什么作用
		IQ notificationIQ = createNotificationIQ(apiKey,"",showType, title, message, uri,timeToLive);
		List<User> list = userService.getUsers();
		for (User user : list) {
			this.sendNotifcationToUser(apiKey,"",showType, user.getUsername(), title, message, uri,timeToLive,notificationIQ);
		}
		
	}

	/**
	 * Sends a newly created notification message to the specific user.
	 * 
	 * @param apiKey
	 *            the API key
	 * @param title
	 *            the title
	 * @param message
	 *            the message details
	 * @param uri
	 *            the uri
	 */
	public void sendNotifcationToUser(String apiKey, String username,String title, String message, String uri, IQ notificationIQ) {
		log.debug("sendNotifcationToUser()...");
		ClientSession session = sessionManager.getSession(username);
		// 创建通知对象
		NotificationMO notificationMO = new NotificationMO(apiKey, title,
				message, uri);
		//接收人
		notificationMO.setUsername(username);
		// 将消息的ID添加到通知对象
		CopyMessageUtil.IQ2Message(notificationIQ, notificationMO);
		if (session != null && session.getPresence().isAvailable()) {
			notificationIQ.setTo(session.getAddress());
		
			notificationMO.setStatus(NotificationMO.STATUS_NOT_SEND);
			try {
				notificationMO.setClientIp(session.getHostAddress());
				notificationMO.setResource(session.getAddress().getResource());
					//保存数据库
					notificationService.saveNotification(notificationMO);
			} catch (Exception e) {
				log.warn(" notifications insert to database failure!!");
			}
			//推送消息
			session.deliver(notificationIQ);
		}else{
			//未发送
			notificationMO.setStatus(NotificationMO.STATUS_NOT_SEND);
			try {
					//保存数据库
					notificationService.saveNotification(notificationMO);
			} catch (Exception e) {
				log.warn(" notifications insert to database failure!!");
			}
		}
		
	}
	
	/**
	 * Sends a newly created notification message to the specific user.
	 * 
	 * @param apiKey
	 *            the API key
	 * @param title
	 *            the title
	 * @param message
	 *            the message details
	 * @param uri
	 *            the uri
	 */
	public void sendNotifcationToUser(String apiKey,String appId,String showType, String username,String title, String message, String uri, Integer timeToLive,IQ notificationIQ) {
		log.debug("sendNotifcationToUser()...");
		ClientSession session = sessionManager.getSession(username);
		// 创建通知对象
		NotificationMO notificationMO = new NotificationMO(apiKey,appId,showType, title,message, uri,timeToLive);
		//接收人
		notificationMO.setUsername(username);
		// 将消息的ID添加到通知对象
		CopyMessageUtil.IQ2Message(notificationIQ, notificationMO);
		if (session != null && session.getPresence().isAvailable()) {
			notificationIQ.setTo(session.getAddress());
		
			notificationMO.setStatus(NotificationMO.STATUS_NOT_SEND);
			try {
				notificationMO.setClientIp(session.getHostAddress());
				notificationMO.setResource(session.getAddress().getResource());
					//保存数据库
					notificationService.saveNotification(notificationMO);
			} catch (Exception e) {
				log.warn(" notifications insert to database failure!!");
			}
			//推送消息
			session.deliver(notificationIQ);
		}else{
			//未发送
			notificationMO.setStatus(NotificationMO.STATUS_NOT_SEND);
			try {
					//保存数据库
					notificationService.saveNotification(notificationMO);
			} catch (Exception e) {
				log.warn(" notifications insert to database failure!!");
			}
		}
		
	}

	/**
	 * Creates a new notification IQ and returns it.
	 */
	private IQ createNotificationIQ(String apiKey, String title,String message, String uri) {
		Random random = new Random();
		String id = Integer.toHexString(random.nextInt());
		// String id = String.valueOf(System.currentTimeMillis());

		Element notification = DocumentHelper.createElement(QName.get("notification", NOTIFICATION_NAMESPACE));
		notification.addElement("id").setText(id);
		notification.addElement("apiKey").setText(apiKey);
		notification.addElement("title").setText(title);
		notification.addElement("message").setText(message);
		notification.addElement("uri").setText(uri);

		IQ iq = new IQ();
		iq.setType(IQ.Type.set);
		iq.setChildElement(notification);

		return iq;
	}
	
	/**
	 * Creates a new notification IQ and returns it.
	 */
	private IQ createNotificationIQ(String apiKey,String appId,String showType, String title,String message, String uri,Integer timeToLive) {
		//Random random = new Random();
		//String id = Integer.toHexString(random.nextInt());
		String id = String.valueOf(System.nanoTime());

		Element notification = DocumentHelper.createElement(QName.get("notification", NOTIFICATION_NAMESPACE));
		notification.addElement("id").setText(id);
		notification.addElement("apiKey").setText(apiKey);
		notification.addElement("showType").setText(showType);
		notification.addElement("title").setText(title);
		notification.addElement("message").setText(message);
		notification.addElement("uri").setText(uri);

		IQ iq = new IQ();
		iq.setType(IQ.Type.set);
		iq.setChildElement(notification);

		return iq;
	}
	
	public void sendNotifications(String apiKey, String username,String title, String message, String uri){
		//生产推送通知对象
		IQ notificationIQ = createNotificationIQ(apiKey, title, message, uri);
		//如果是多个用户，根据“;”分隔 ，循环发送
		if(username.indexOf(";")!=-1){
			String[] users = username.split(";");
			for (String user : users) {
				this.sendNotifcationToUser(apiKey, user, title, message, uri,notificationIQ);
			}
		}else{
			this.sendNotifcationToUser(apiKey, username, title, message, uri,notificationIQ);
		}
	}
	
	public void sendNotifications(String apiKey, String appId,String showType,String username,String title, String message, String uri,Integer timeToLive){
		//生产推送通知对象
		IQ notificationIQ = createNotificationIQ(apiKey,appId,showType,title, message, uri,timeToLive);
		//如果是多个用户，根据“;”分隔 ，循环发送
		if(username.indexOf(";")!=-1){
			String[] users = username.split(";");
			for (String user : users) {
				this.sendNotifcationToUser(apiKey,appId,showType,user, title, message, uri,timeToLive,notificationIQ);
			}
		}else{
			this.sendNotifcationToUser(apiKey,appId,showType,username, title, message, uri,timeToLive,notificationIQ);
		}
	}
	
	//发送离线消息
	public void sendOfflineNotification(NotificationMO notificationMO ) {
		log.debug("sendOfflineNotifcation()...");
		IQ notificationIQ = createNotificationIQ(notificationMO.getApiKey(), notificationMO.getTitle(), notificationMO.getMessage(), notificationMO.getUri());
		//将生产通知ID替换成原来的ID
		notificationIQ.setID(notificationMO.getMessageId());
		ClientSession session = sessionManager.getSession(notificationMO.getUsername());
		if (session != null && session.getPresence().isAvailable()) {
			notificationIQ.setTo(session.getAddress());
			session.deliver(notificationIQ);
//			try{
//				//修改通知状态为已发送
//				notificationMO.setStatus(NotificationMO.STATUS_SEND);
//				//IP
//				notificationMO.setClientIp(session.getHostAddress());
//				//来源
//				notificationMO.setResource(session.getAddress().getResource());
//				notificationService.updateNotification(notificationMO);
//			}catch (Exception e) {
//				log.warn(" update notification status failure !");
//			}
		}
	}
}
