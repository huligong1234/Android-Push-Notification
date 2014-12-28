JeeDev JPush Server
============
消息推送支持两种显示类型，一种为NOTIFICATION和PAYLOAD,通过showType参数传递到客户端。
NOTIFICATION表示正常消息类型，而PAYLOAD表示自定义消息类型，即消息内容格式及显示方式自定义，仅支持通过Handler方式接收处理。


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