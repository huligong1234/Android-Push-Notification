package com.example.jpush_example;

import java.util.Map;

import org.jeedevframework.jpush.android.client.JPushInterface;
import org.jeedevframework.jpush.android.client.JPushManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.example.jpush_example.R;

public class MainActivity extends Activity {
	
	private TextView textView01;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textView01 = (TextView)findViewById(R.id.textView01);
		String clientId = JPushInterface.getClientId(this);
		textView01.setText("clientId:"+clientId);
        Handler callBackHandler = new Handler() {
			public void handleMessage(Message msg) {
				Map<String, String> resultInfo = (Map<String, String>) msg.obj;
				//String title = resultInfo.get(WPushInterface.NOTIFICATION_TITLE);
				String message = resultInfo.get(JPushInterface.NOTIFICATION_MESSAGE); //
				
				 new AlertDialog.Builder(MainActivity.this)
					.setTitle("消息通知")
					.setMessage(message)
					.setPositiveButton("确定", null).show();
				 
				 String packetId = resultInfo.get(JPushInterface.NOTIFICATION_PACKET_ID);
				 String from = resultInfo.get(JPushInterface.NOTIFICATION_FROM); 
				 JPushInterface.reportStatus(packetId, from);
			
			}
		};

			JPushManager.getInstance().initialize(this)
			.setNotificationIcon(R.drawable.notification)
			.setNotificationSystemBar(true)
			.setNotificationHandler(true)
			.setCallBackHandler(callBackHandler)
			.setDebugMode(true)
			.turnOnPush();
			
			//Toast.makeText(this, clientId, Toast.LENGTH_LONG).show();
	}
}
