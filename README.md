#  Android Push Notification 介绍 
基于AndroidPN 项目实现的的消息推送
## 特点 ##
* 客户端服务器交互初始化
* 用户登录认证
* 客户端发起在线心跳通知
* 发送消息时若用户在线则直接发送，否则保存到数据库
* 服务器从数据库中获取未接收过的消息列表，循环发送同时更新发送状态为已发送，接收通知更新状态为已接收，再次收到通知则更新为已读
* 消息状态变化过程 未发送(0)->已发送(1)->已接收(2)->已读(3)

-----
消息推送支持两种显示类型，一种为NOTIFICATION和PAYLOAD,通过showType参数传递到客户端。
NOTIFICATION表示正常消息类型，而PAYLOAD表示自定义消息类型，即消息内容格式及显示方式自定义，仅支持通过Handler方式接收处理。




=====
Android客户端使用PAYLOAD自定义消息代码示例：

```java

Handler callBackHandler = new Handler() {
    public void handleMessage(Message msg) {
		Map<String, String> resultInfo = (Map<String, String>) msg.obj;
		//String title = resultInfo.get(WPushInterface.NOTIFICATION_TITLE);
		String message = resultInfo.get(JPushInterface.NOTIFICATION_MESSAGE); //handler方式，自定义消息内容通过message方式传递
		
		 new AlertDialog.Builder(getApplicationContext())
			.setTitle("消息通知")
			.setMessage(message)
			.setPositiveButton("确定", null).show();
		 
		 String packetId = resultInfo.get(JPushInterface.NOTIFICATION_PACKET_ID);
		 String from = resultInfo.get(JPushInterface.NOTIFICATION_FROM); 
		 JPushInterface.reportStatus(packetId, from);
	
	}
};

JPushManager.getInstance().initialize(this) //初始化
.setNotificationIcon(R.drawable.notification) 
.setNotificationSystemBar(false) //关闭通知栏消息
.setNotificationHandler(true) //使用Handler处理消息
.setCallBackHandler(callBackHandler)
.setDebugMode(true) //Debug模式显示调试信息
.turnOnPush(); //开启消息推送接收

```