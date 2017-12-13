package com.witsi.setting.hardwaretest;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class BurnConfigActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = "BurnActivity";
	private Context context = BurnConfigActivity.this;
	private static final boolean D = true;
	
	CheckBox	// checkbox_touchscreen, checkbox_magc,checkbox_printer, checkbox_key,
	checkbox_screen,checkbox_buzzer, checkbox_security,
	checkbox_ic, checkbox_led, checkbox_version, checkbox_gprs,
	checkbox_wifi, checkbox_tf, checkbox_serialnumber;
	TextView burn_dialog;
	View main = null;
	Button button_burn_return_to_home;
	ImageView burn_image;
	
	boolean isburning = false;
	boolean ifCloseActivity = true;
	boolean flag_checkbox_touchscreen = false, flag_checkbox_screen = false,
			flag_checkbox_magc = false, flag_checkbox_printer = false,
			flag_checkbox_buzzer = false, flag_checkbox_security = false,
			flag_checkbox_key = false, flag_checkbox_ic = false,
			flag_checkbox_led = false, flag_checkbox_version = false,
			flag_checkbox_gprs = false, flag_checkbox_wifi = false,
			flag_checkbox_tf = false, flag_checkbox_serialnumber = false;
	boolean screen_sleep = false;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	
	private SharedPreferences config;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		 setContentView(R.layout.hardware_burn_config_activity);
		 config = ConfigSharePaference.getSharedPreferences(context);
		 editor = config.edit();
		 
		 registerReceiver(mReceiver,filterHome);
		 // SysApplication.getInstance().addActivity(this);
		 if(D)FyLog.i("拷机选择启动界面", "进入");
		/*********************************************/
		burn_image = (ImageView) this.findViewById(R.id.burn_image);
		burn_dialog = (TextView) this.findViewById(R.id.burn_dialog);
		button_burn_return_to_home = (Button) this.findViewById(R.id.burn_return_home);
		button_burn_return_to_home.setOnClickListener(BurnConfigActivity.this);
//		checkbox_touchscreen = (CheckBox) this.findViewById(R.id.burn_touchscreen_check);
		checkbox_screen = (CheckBox) this.findViewById(R.id.burn_screen_check);
//		checkbox_magc = (CheckBox) this.findViewById(R.id.burn_magc_check);
//		checkbox_printer = (CheckBox) this.findViewById(R.id.burn_printer_check);
		checkbox_buzzer = (CheckBox) this.findViewById(R.id.burn_buzzer_check);
		checkbox_security = (CheckBox) this.findViewById(R.id.burn_security_check);
//		checkbox_key = (CheckBox) this.findViewById(R.id.burn_key_check);
		checkbox_ic = (CheckBox) this.findViewById(R.id.burn_ic_check);
		checkbox_led = (CheckBox) this.findViewById(R.id.burn_led_check);
		checkbox_version = (CheckBox) this.findViewById(R.id.burn_version_check);
		checkbox_gprs = (CheckBox) this.findViewById(R.id.burn_gprs_check);
		checkbox_wifi = (CheckBox) this.findViewById(R.id.burn_wifi_check);
		checkbox_tf = (CheckBox) this.findViewById(R.id.burn_tf_check);
		checkbox_serialnumber = (CheckBox) this.findViewById(R.id.burn_serialnumber_check);
		
		isburning = config.getBoolean("flag_burn", false);
		if(D)FyLog.i(TAG, "the isburning is: " + isburning);
		if (config.getBoolean("light", true) == false) {
			burn_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		
//		checkbox_touchscreen.setChecked(config.getBoolean("flag_checkbox_touchscreen", false));
		checkbox_screen.setChecked(config.getBoolean("flag_checkbox_screen", false));
//		checkbox_magc.setChecked(config.getBoolean("flag_checkbox_magc", false));
//		checkbox_printer.setChecked(config.getBoolean("flag_checkbox_printer", false));
		checkbox_buzzer.setChecked(config.getBoolean("flag_checkbox_buzzer", false));
		checkbox_security.setChecked(config.getBoolean("flag_checkbox_security", false));
//		checkbox_key.setChecked(config.getBoolean("flag_checkbox_key", false));
		checkbox_ic.setChecked(config.getBoolean("flag_checkbox_ic", false));
		checkbox_led.setChecked(config.getBoolean("flag_checkbox_led", false));
		checkbox_version.setChecked(config.getBoolean("flag_checkbox_version", false));
		checkbox_gprs.setChecked(config.getBoolean("flag_checkbox_gprs", false));
		checkbox_wifi.setChecked(config.getBoolean("flag_checkbox_wifi", false));
		checkbox_tf.setChecked(config.getBoolean("flag_checkbox_tf", false));
		checkbox_serialnumber.setChecked(config.getBoolean("flag_checkbox_serialnumber", false));
		
		if (isburning == true) {
			burn_dialog.setText("拷机中…");
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					FyLog.i("拷机选择启动界面", "定时器启动");
					Intent intent;
					if (config.getBoolean("flag_checkbox_screen", false)) {
						FyLog.i("拷机选择启动界面", "选择坏点测试");
						intent = new Intent(BurnConfigActivity.this,
								ScreenActivity.class);
					} else if (config.getBoolean("flag_checkbox_ic", false)) {
						FyLog.i("拷机选择启动界面", "选择IC卡测试");
						intent = new Intent(BurnConfigActivity.this,
								IccActivity.class);
					} else if (config.getBoolean("flag_checkbox_buzzer", false)) {
						FyLog.i("拷机选择启动界面", "选择蜂鸣器测试");
						intent = new Intent(BurnConfigActivity.this,
								BuzzerActivity.class);
					} else if (config.getBoolean("flag_checkbox_security", false)) {
						FyLog.i("拷机选择启动界面", "选择安全状态测试");
						intent = new Intent(BurnConfigActivity.this,
								SecurityActivity.class);
					} else if (config.getBoolean("flag_checkbox_led", false)) {
						FyLog.i("拷机选择启动界面", "选择LED测试");
						intent = new Intent(BurnConfigActivity.this,
								LedActivity.class);
					} else if (config.getBoolean("flag_checkbox_version", false)) {
						FyLog.i("拷机选择启动界面", "选择版本号测试");
						intent = new Intent(BurnConfigActivity.this,
								VersionActivity.class);
					} else if (config.getBoolean("flag_checkbox_wifi", false)) {
						FyLog.i("拷机选择启动界面", "选择WIFI测试");
						intent = new Intent(BurnConfigActivity.this,
								WifiActivity.class);
					} else if (config.getBoolean("flag_checkbox_gprs", false)) {
						FyLog.i("拷机选择启动界面", "选择GPRS测试");
						intent = new Intent(BurnConfigActivity.this,
								GPRS_Activity.class);
					} else if (config.getBoolean("flag_checkbox_tf", false)) {
						FyLog.i("拷机选择启动界面", "选择TF卡测试");
						intent = new Intent(BurnConfigActivity.this,
								TF_Activity.class);
					} else if (config.getBoolean("flag_checkbox_serialnumber", false)) {
						FyLog.i("拷机选择启动界面", "选择序列号测试");
						intent = new Intent(BurnConfigActivity.this,
								SerialNumberActivity.class);
					} else if (config.getBoolean("flag_checkbox_printer", false)) {
						FyLog.i("拷机选择启动界面", "选择打印机测试");
						intent = new Intent(BurnConfigActivity.this,
								PrinterActivity.class);
					} else {
						FyLog.i("拷机选择启动界面", "没有作出选择");
						intent = new Intent(BurnConfigActivity.this,
								ResultTableActivity.class);
					}
					startActivity(intent);
				}
			}, 2000);

		}
	}
	
	@Override
	public void onClick(View burnclick) {
		if (screen_sleep == true) {
			burn_image.setBackgroundResource(R.drawable.lucency);
			screen_sleep = false;
		} else {
			switch (burnclick.getId()) {
			case R.id.burn_return_home: {
				isburning = false;
				// 判断复选框的状态
//				if (checkbox_touchscreen.isChecked())
//					flag_checkbox_touchscreen = true;
//				else
//					flag_checkbox_touchscreen = false;
				if (checkbox_screen.isChecked())
					flag_checkbox_screen = true;
				else
					flag_checkbox_screen = false;
//				if (checkbox_magc.isChecked())
//					flag_checkbox_magc = true;
//				else
//					flag_checkbox_magc = false;
//				if (checkbox_printer.isChecked())
//					flag_checkbox_printer = true;
//				else
//					flag_checkbox_printer = false;
				if (checkbox_buzzer.isChecked())
					flag_checkbox_buzzer = true;
				else
					flag_checkbox_buzzer = false;
				if (checkbox_security.isChecked())
					flag_checkbox_security = true;
				else
					flag_checkbox_security = false;
//				if (checkbox_key.isChecked())
//					flag_checkbox_key = true;
//				else
//					flag_checkbox_key = false;
				if (checkbox_ic.isChecked())
					flag_checkbox_ic = true;
				else
					flag_checkbox_ic = false;
				if (checkbox_led.isChecked())
					flag_checkbox_led = true;
				else
					flag_checkbox_led = false;
				if (checkbox_version.isChecked())
					flag_checkbox_version = true;
				else
					flag_checkbox_version = false;
				if (checkbox_gprs.isChecked())
					flag_checkbox_gprs = true;
				else
					flag_checkbox_gprs = false;
				if (checkbox_wifi.isChecked())
					flag_checkbox_wifi = true;
				else
					flag_checkbox_wifi = false;
				if (checkbox_tf.isChecked())
					flag_checkbox_tf = true;
				else
					flag_checkbox_tf = false;
				if (checkbox_serialnumber.isChecked())
					flag_checkbox_serialnumber = true;
				else
					flag_checkbox_serialnumber = false;
				/* 标记被选中要进行拷机的项目 */
				editor.putBoolean("flag_checkbox_touchscreen", flag_checkbox_touchscreen);
				editor.putBoolean("flag_checkbox_screen", flag_checkbox_screen);
				editor.putBoolean("flag_checkbox_magc", flag_checkbox_magc);
				editor.putBoolean("flag_checkbox_printer", flag_checkbox_printer);
				editor.putBoolean("flag_checkbox_buzzer", flag_checkbox_buzzer);
				editor.putBoolean("flag_checkbox_security", flag_checkbox_security);
				editor.putBoolean("flag_checkbox_key", flag_checkbox_key);
				editor.putBoolean("flag_checkbox_ic", flag_checkbox_ic);
				editor.putBoolean("flag_checkbox_led", flag_checkbox_led);
				editor.putBoolean("flag_checkbox_version", flag_checkbox_version);
				editor.putBoolean("flag_checkbox_gprs", flag_checkbox_gprs);
				editor.putBoolean("flag_checkbox_wifi", flag_checkbox_wifi);
				editor.putBoolean("flag_checkbox_tf", flag_checkbox_tf);
				editor.putBoolean("flag_checkbox_serialnumber", flag_checkbox_serialnumber);
				editor.commit();
				Intent intent = new Intent(BurnConfigActivity.this, BurnningActivity.class);
				startActivity(intent);
				break;
			}
			// case R.id.burn_start: {
			// // 判断复选框的状态
			// if (checkbox_touchscreen.isChecked())
			// flag_checkbox_touchscreen = true;
			// else
			// flag_checkbox_touchscreen = false;
			// if (checkbox_screen.isChecked())
			// flag_checkbox_screen = true;
			// else
			// flag_checkbox_screen = false;
			// if (checkbox_magc.isChecked())
			// flag_checkbox_magc = true;
			// else
			// flag_checkbox_magc = false;
			// if (checkbox_printer.isChecked())
			// flag_checkbox_printer = true;
			// else
			// flag_checkbox_printer = false;
			// if (checkbox_buzzer.isChecked())
			// flag_checkbox_buzzer = true;
			// else
			// flag_checkbox_buzzer = false;
			// if (checkbox_security.isChecked())
			// flag_checkbox_security = true;
			// else
			// flag_checkbox_security = false;
			// if (checkbox_key.isChecked())
			// flag_checkbox_key = true;
			// else
			// flag_checkbox_key = false;
			// if (checkbox_ic.isChecked())
			// flag_checkbox_ic = true;
			// else
			// flag_checkbox_ic = false;
			// if (checkbox_led.isChecked())
			// flag_checkbox_led = true;
			// else
			// flag_checkbox_led = false;
			// if (checkbox_version.isChecked())
			// flag_checkbox_version = true;
			// else
			// flag_checkbox_version = false;
			// if (checkbox_gprs.isChecked())
			// flag_checkbox_gprs = true;
			// else
			// flag_checkbox_gprs = false;
			// if (checkbox_wifi.isChecked())
			// flag_checkbox_wifi = true;
			// else
			// flag_checkbox_wifi = false;
			// if (checkbox_tf.isChecked())
			// flag_checkbox_tf = true;
			// else
			// flag_checkbox_tf = false;
			// /* 标记被选中要进行拷机的项目 */
			// mybundle.putBoolean("flag_checkbox_touchscreen",
			// flag_checkbox_touchscreen);
			// mybundle.putBoolean("flag_checkbox_screen",
			// flag_checkbox_screen);
			// mybundle.putBoolean("flag_checkbox_magc", flag_checkbox_magc);
			// mybundle.putBoolean("flag_checkbox_printer",
			// flag_checkbox_printer);
			// mybundle.putBoolean("flag_checkbox_buzzer",
			// flag_checkbox_buzzer);
			// mybundle.putBoolean("flag_checkbox_security",
			// flag_checkbox_security);
			// mybundle.putBoolean("flag_checkbox_key", flag_checkbox_key);
			// mybundle.putBoolean("flag_checkbox_ic", flag_checkbox_ic);
			// mybundle.putBoolean("flag_checkbox_led", flag_checkbox_led);
			// mybundle.putBoolean("flag_checkbox_version",
			// flag_checkbox_version);
			// mybundle.putBoolean("flag_checkbox_gprs", flag_checkbox_gprs);
			// mybundle.putBoolean("flag_checkbox_wifi", flag_checkbox_wifi);
			// mybundle.putBoolean("flag_checkbox_tf", flag_checkbox_tf);
			// // mybundle.putBoolean("flag_burn", true);
			// if (flag_checkbox_touchscreen) {
			// intent = new Intent(BurnActivity.this,
			// TouchScreenActivity.class);
			// } else if (flag_checkbox_screen) {
			// intent = new Intent(BurnActivity.this, ScreenActivity.class);
			// } else if (flag_checkbox_magc) {
			// intent = new Intent(BurnActivity.this, MagcActivity.class);
			// } else if (flag_checkbox_printer) {
			// intent = new Intent(BurnActivity.this, PrinterActivity.class);
			// } else if (flag_checkbox_buzzer) {
			// intent = new Intent(BurnActivity.this, BuzzerActivity.class);
			// } else if (flag_checkbox_security) {
			// intent = new Intent(BurnActivity.this, SecurityActivity.class);
			// } else if (flag_checkbox_key) {
			// intent = new Intent(BurnActivity.this, KeyActivity.class);
			// } else if (flag_checkbox_ic) {
			// intent = new Intent(BurnActivity.this, IccActivity.class);
			// } else if (flag_checkbox_led) {
			// intent = new Intent(BurnActivity.this, LedActivity.class);
			// } else if (flag_checkbox_version) {
			// intent = new Intent(BurnActivity.this, VersionActivity.class);
			// } else if (flag_checkbox_gprs) {
			// intent = new Intent(BurnActivity.this, GPRS_Activity.class);
			// } else if (flag_checkbox_wifi) {
			// intent = new Intent(BurnActivity.this, WifiActivity.class);
			// } else if (flag_checkbox_tf) {
			// intent = new Intent(BurnActivity.this, TF_Activity.class);
			// }
			// intent.putExtras(mybundle);
			// startActivity(intent);
			// break;
			// }
			default:
				break;
			}
		}
	}
	 
	@Override
	protected void onStop() {
		super.onStop();
		FyLog.i(TAG, "进入Stop状态");
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
		if (ifCloseActivity == true) {
			FyLog.i(TAG, "尝试进行finish");
			this.finish();
			FyLog.i(TAG, "已执行finish");
		}
	}
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		ifCloseActivity = true;
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
	}
	
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
            isExit.setButton("确定", listener);  
            isExit.setButton2("取消", listener);  
            // 显示对话框  
            isExit.show();  
  
        }  
          
        return false;  
          
    }  
    /**监听对话框里面的button点击事件*/  
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
    {  
        public void onClick(DialogInterface dialog, int which)  
        {  
            switch (which)  
            {  
            case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序  
                finish();  
                break;  
            case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框  
                break;  
            default:  
                break;  
            }  
        }  
    };    
	//以上为点击返回键填出提示窗口	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				burn_image.setBackgroundResource(R.drawable.lucency);
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

	
	//用于接收
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				ifCloseActivity =false;
				FyLog.i(TAG, "home键按下");
			}

		}

	};
}
