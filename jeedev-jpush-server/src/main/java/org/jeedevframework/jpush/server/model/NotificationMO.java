/**
 * 
 */
package org.jeedevframework.jpush.server.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author chengqiang.liu
 *	通知内容实体
 */
@Entity
@Table(name = "apn_notification")
public class NotificationMO implements Serializable {
	
	private static final long serialVersionUID = 1845362556725768545L;
	
	public static final String STATUS_NOT_SEND = "0";
	public static final String STATUS_SEND = "1";
	public static final String STATUS_RECEIVE = "2";
	public static final String STATUS_READ = "3";
	
	public NotificationMO(){
	}
	
	public NotificationMO(String apiKey,String title,String message,String uri){
		this.apiKey = apiKey;
		this.title = title;
		this.message = message;
		this.uri = uri;
		this.timeToLive = 0;
		this.createTime = new Timestamp(System.currentTimeMillis());
		this.updateTime = new Timestamp(System.currentTimeMillis());
	}
	
	public NotificationMO(String apiKey,String appId,String showType,String title,String message,String uri,Integer timeToLive){
		this.apiKey = apiKey;
		this.appId = appId;
		this.showType = showType;
		this.title = title;
		this.message = message;
		this.uri = uri;
		this.timeToLive = timeToLive;
		this.createTime = new Timestamp(System.currentTimeMillis());
		this.updateTime = new Timestamp(System.currentTimeMillis());
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "username", length = 64)
    private String username;
    
    @Column(name = "client_ip", length = 64)
    private String clientIp;
    
    @Column(name = "resource", length = 64)
    private String resource;

    @Column(name = "message_id", length = 64)
    private String messageId;
    
    @Column(name = "api_key", length = 64)
    private String apiKey;
    
    @Column(name = "app_id", length = 64)
    private String appId;
    
    @Column(name = "app_key", length = 512)
    private String appKey;

    @Column(name = "show_type", length = 32,columnDefinition="varchar(32) comment 'NOTIFICATION:点击打开应用;PAYLOAD:透传'")
    private String showType;
    
    @Column(name = "time_to_live", length = 11)
    private Integer timeToLive;
    
    @Column(name = "title", length = 512)
    private String title;

    @Column(name = "message", length = 1024)
    private String message;
    
    @Column(name = "uri", length = 512)
    private String uri;
    
    @Column(name = "status",columnDefinition="varchar(10) comment '0: 未发送 1：已发送 2：已接收 3：已查看'")
    private String status;
    
    @Column(name = "created_time",updatable = false)
    private Timestamp createTime ;
    
    @Column(name = "updateTime")
    private Timestamp updateTime ;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getShowType() {
		return showType;
	}

	public void setShowType(String showType) {
		this.showType = showType;
	}

	public Integer getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(Integer timeToLive) {
		this.timeToLive = timeToLive;
	}

}
