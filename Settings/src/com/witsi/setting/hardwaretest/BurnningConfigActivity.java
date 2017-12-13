package com.witsi.setting.hardwaretest;


import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class BurnningConfigActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = "BurnActivity";
	private Context context = BurnningConfigActivity.this;
	private static final boolean D = true;
	
	CheckBox	
	checkbox_sam, checkbox_rf, 
	checkbox_screen, checkbox_buzzer, 
	checkbox_security, checkbox_ic, 
	checkbox_led, checkbox_version,
	checkbox_tf, checkbox_serialnumber;
	TextView burn_dialog;
	View main = null;
	Button button_burn_return_to_home;
	ImageView burn_image;
	
	boolean isburning = false;
	boolean 
			flag_checkbox_screen = false, flag_checkbox_ic = false,
			flag_checkbox_buzzer = false, flag_checkbox_security = false,
			flag_checkbox_sam = false, flag_checkbox_rf = false,
			flag_checkbox_led = false, flag_checkbox_version = false,
			flag_checkbox_tf = false, flag_checkbox_serialnumber = false;
	boolean screen_sleep = false;
	
	private SharedPreferences config;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		 setContentView(R.layout.hardware_burnning_config_activity);
		 config = ConfigSharePaference.getSharedPreferences(context);
		 editor = config.edit();
		 
		 // SysApplication.getInstance().addActivity(this);
		 if(D)FyLog.i("拷机选择启动界面", "进入");
		/*********************************************/
		burn_image = (ImageView) this.findViewById(R.id.burn_image);
		burn_dialog = (TextView) this.findViewById(R.id.burn_dialog);
		button_burn_return_to_home = (Button) this.findViewById(R.id.burn_return_home);
		button_burn_return_to_home.setOnClickListener(BurnningConfigActivity.this);
		checkbox_screen = (CheckBox) this.findViewById(R.id.burn_screen_check);
		checkbox_sam = (CheckBox) this.findViewById(R.id.burn_sam_check);
		checkbox_rf = (CheckBox) this.findViewById(R.id.burn_rf_check);
		checkbox_buzzer = (CheckBox) this.findViewById(R.id.burn_buzzer_check);
		checkbox_security = (CheckBox) this.findViewById(R.id.burn_security_check);
		checkbox_ic = (CheckBox) this.findViewById(R.id.burn_ic_check);
		checkbox_led = (CheckBox) this.findViewById(R.id.burn_led_check);
		checkbox_version = (CheckBox) this.findViewById(R.id.burn_version_check);
		checkbox_tf = (CheckBox) this.findViewById(R.id.burn_tf_check);
		checkbox_serialnumber = (CheckBox) this.findViewById(R.id.burn_serialnumber_check);
		
		isburning = config.getBoolean("flag_burn", false);
		if(D)FyLog.i(TAG, "the isburning is: " + isburning);
		if (config.getBoolean("light", true) == false) {
			burn_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		
		checkbox_screen.setChecked(config.getBoolean("flag_checkbox_screen", false));
		checkbox_sam.setChecked(config.getBoolean("flag_checkbox_sam", false));
		checkbox_rf.setChecked(config.getBoolean("flag_checkbox_rf", false));
		checkbox_buzzer.setChecked(config.getBoolean("flag_checkbox_buzzer", false));
		checkbox_security.setChecked(config.getBoolean("flag_checkbox_security", false));
		checkbox_ic.setChecked(config.getBoolean("flag_checkbox_ic", false));
		checkbox_led.setChecked(config.getBoolean("flag_checkbox_led", false));
		checkbox_version.setChecked(config.getBoolean("flag_checkbox_version", false));
		checkbox_tf.setChecked(config.getBoolean("flag_checkbox_tf", false));
		checkbox_serialnumber.setChecked(config.getBoolean("flag_checkbox_serialnumber", false));
		
	}
	
	@Override
	public void onClick(View burnclick) {
		if (screen_sleep == true) {
			burn_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			switch (burnclick.getId()) {
			case R.id.burn_return_home: {
				isburning = false;
				isSleepExit = false;
				/* 标记被选中要进行拷机的项目 */
				editor.putBoolean("flag_checkbox_screen", checkbox_screen.isChecked());
				editor.putBoolean("flag_checkbox_sam", checkbox_sam.isChecked());
				editor.putBoolean("flag_checkbox_rf", checkbox_rf.isChecked());
				editor.putBoolean("flag_checkbox_buzzer", checkbox_buzzer.isChecked());
				editor.putBoolean("flag_checkbox_security", checkbox_security.isChecked());
				editor.putBoolean("flag_checkbox_ic", checkbox_ic.isChecked());
				editor.putBoolean("flag_checkbox_led", checkbox_led.isChecked());
				editor.putBoolean("flag_checkbox_version", checkbox_version.isChecked());
				editor.putBoolean("flag_checkbox_tf", checkbox_tf.isChecked());
				editor.putBoolean("flag_checkbox_serialnumber", checkbox_serialnumber.isChecked());
				editor.commit();
//				ActivityManagers.clearBurnningConfig();
				Intent intent = new Intent(BurnningConfigActivity.this, BurnningActivity.class);
				startActivity(intent);
				break;
			}
			
			default:
				break;
			}
		}
	}
	 
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "onStop");
		if(!isSleepExit){
			finish();
		}
	}
	/************************** 事件监听申明区 ***************************/
	@SuppressWarnings("deprecation")
	//点击返回键填出提示窗口
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // 创建退出对话框  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // 设置对话框标题  
            isExit.setTitle("系统提示");  
            // 设置对话框消息  
            isExit.setMessage("确定要退出吗");  
            // 添加选择按钮并注册监听  
            isExit.setButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					ActivityManagers.clearActivity();
					finish();
				}
			});  
            isExit.setButton2("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // 显示对话框  
            isExit.show();  
        }  
        return false;  
    }  
	
	//以上为点击返回键填出提示窗口	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				burn_image.setBackgroundResource(R.drawable.bg_transport);
				screen_sleep = false;
				break;
			// 触摸并移动时刻
			case MotionEvent.ACTION_MOVE:
				break;
			// 终止触摸时刻
			case MotionEvent.ACTION_UP:
				break;
			}
		}
		return false;
	}

	
}
