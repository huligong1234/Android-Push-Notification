package org.jeedevframework.jpush.android.client;

import org.jeedevframework.jpush.android.utils.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationDetailsActivity
  extends Activity
{
  private static final String LOGTAG = LogUtil.makeLogTag(NotificationDetailsActivity.class);
  private String callbackActivityPackageName;
  private String callbackActivityClassName;
  private String notificationFrom;
  private String packetId;
  
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    
    SharedPreferences sharedPrefs = getSharedPreferences(JPushInterface.SHARED_PREFERENCE_NAME, 0);
    this.callbackActivityPackageName = sharedPrefs.getString("CALLBACK_ACTIVITY_PACKAGE_NAME", "");
    this.callbackActivityClassName = sharedPrefs.getString("CALLBACK_ACTIVITY_CLASS_NAME", "");
    
    Intent intent = getIntent();
    String notificationId = intent.getStringExtra(JPushInterface.NOTIFICATION_ID);
    String notificationApiKey = intent.getStringExtra(JPushInterface.NOTIFICATION_API_KEY);
    String notificationTitle = intent.getStringExtra(JPushInterface.NOTIFICATION_TITLE);
    String notificationMessage = intent.getStringExtra(JPushInterface.NOTIFICATION_MESSAGE);
    String notificationUri = intent.getStringExtra(JPushInterface.NOTIFICATION_URI);
    
    this.notificationFrom = intent.getStringExtra(JPushInterface.NOTIFICATION_FROM);
    this.packetId = intent.getStringExtra(JPushInterface.NOTIFICATION_PACKET_ID);
    
    LogUtil.d(LOGTAG, "notificationId=" + notificationId);
    LogUtil.d(LOGTAG, "notificationApiKey=" + notificationApiKey);
    LogUtil.d(LOGTAG, "notificationTitle=" + notificationTitle);
    LogUtil.d(LOGTAG, "notificationMessage=" + notificationMessage);
    LogUtil.d(LOGTAG, "notificationUri=" + notificationUri);
    LogUtil.d(LOGTAG, "notificationFrom=" + this.notificationFrom);
    LogUtil.d(LOGTAG, "notificationPacketId=" + this.packetId);
    
    JPushInterface.reportStatus(this.packetId, this.notificationFrom);
    

    View rootView = createView(notificationTitle, notificationMessage, notificationUri);
    setContentView(rootView);
  }
  
  private View createView(String title, String message, final String uri)
  {
    LinearLayout linearLayout = new LinearLayout(this);
    linearLayout.setBackgroundColor(0xffeeeeee);
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    linearLayout.setPadding(5, 5, 5, 5);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
    linearLayout.setLayoutParams(layoutParams);
    
    TextView textTitle = new TextView(this);
    textTitle.setText(title);
    textTitle.setTextSize(18.0F);
    
    // textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    textTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    textTitle.setTextColor(0xff000000);
    textTitle.setGravity(Gravity.CENTER);
    
    layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    layoutParams.setMargins(30, 30, 30, 0);
    textTitle.setLayoutParams(layoutParams);
    linearLayout.addView(textTitle);
    
    TextView textDetails = new TextView(this);
    textDetails.setText(message);
    textDetails.setTextSize(14);
    // textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
    textDetails.setTextColor(0xff333333);
    textDetails.setGravity(Gravity.CENTER);

    layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    layoutParams.setMargins(30, 10, 30, 20);
    textDetails.setLayoutParams(layoutParams);
    linearLayout.addView(textDetails);
    
    Button okButton = new Button(this);
    okButton.setText("确定");
    okButton.setWidth(220);
    
    okButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View view)
      {
        Intent intent;
        if ((uri != null) && 
          (uri.length() > 0) && (
          (uri.startsWith("http:")) || (uri.startsWith("https:")) || 
          (uri.startsWith("tel:")) || 
          (uri.startsWith("geo:"))))
        {
          intent = new Intent("android.intent.action.VIEW", Uri.parse(uri));
        }
        else
        {
          intent = new Intent().setClassName(NotificationDetailsActivity.this.callbackActivityPackageName, NotificationDetailsActivity.this.callbackActivityClassName);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          // intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
          // intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        NotificationDetailsActivity.this.startActivity(intent);
        NotificationDetailsActivity.this.finish();
      }
    });
    LinearLayout innerLayout = new LinearLayout(this);
    innerLayout.setGravity(Gravity.CENTER);
    innerLayout.addView(okButton);
    
    linearLayout.addView(innerLayout);
    
    return linearLayout;
  }
}
