 package com.witsi.setting.hardwaretest;


import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.witsi.arq.ArqMisc;

public class LedActivity extends Activity implements
		android.view.View.OnClickListener{
	
	private final String TAG = "LedActivity";
	private Context context = LedActivity.this;
	
	private Button button_led_return;
	private Button button_led_ok;
	private Button button_led_false;
	private TextView tv_tip;
	Button test;
	private Button button_led_test;
	private ImageView led_image;
	private RadioButton swLed1, swLed2, swLed3, swLed4;
	private View main;
	
	private ArqMisc arqMisc;
	private Handler handler;
	
	boolean screen_sleep = false;
	boolean isburning = false;
	int flag_led = -1;
	
	private SharedPreferences config;
	private Editor editor;
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		FyLog.i(TAG, "进入Led测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_led_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_led_activity);
			screen_sleep = false;
		}
		
		arqMisc = new ArqMisc(LedActivity.this);
		handler = new Handler();
		View v = findViewById(R.id.ll_tool);
		button_led_return = (Button) v.findViewById(R.id.back);
		button_led_ok = (Button) v.findViewById(R.id.pass);
		button_led_false = (Button) v.findViewById(R.id.fail);
		button_led_test = (Button) v.findViewById(R.id.test);
		tv_tip = (TextView) findViewById(R.id.tv_tip);
		button_led_return.setOnClickListener(LedActivity.this);
		button_led_ok.setOnClickListener(LedActivity.this);
		button_led_false.setOnClickListener(LedActivity.this);
		button_led_test.setOnClickListener(LedActivity.this);
//		button_led_test.setText("");
		 swLed1 = (RadioButton)findViewById(R.id.led1);
		 swLed2 = (RadioButton)findViewById(R.id.led2);
		 swLed3 = (RadioButton)findViewById(R.id.led3);
		 swLed4 = (RadioButton)findViewById(R.id.led4);

		 if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
				swLed2.setVisibility(View.INVISIBLE);
				swLed3.setVisibility(View.INVISIBLE);
				swLed4.setVisibility(View.INVISIBLE);
		 }
		swLed1.setChecked(false);
		swLed2.setChecked(false);
		swLed3.setChecked(false);
		swLed4.setChecked(false);
		//屏幕休眠
		led_image = (ImageView) this.findViewById(R.id.led_image);
		if (screen_sleep == true) {
			led_image.setBackgroundResource(R.drawable.bg_black);
		}
		
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		//开启led
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				led_test_method(0);
			}
		}, 200);
		if(isburning == true)
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isSleepExit = false;
					editor.putString("led", "ok");
					editor.commit();
					ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_VERSION);
					finish();
				}
			}, 3000);
	}
	private boolean isTestOver = false;
	@Override
	public void onClick(View ledClick) {
		if (screen_sleep == true) {
			led_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
			toggleBrightness(this, 200);// 背光
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (ledClick.getId()) {
			case R.id.back: {
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(LedActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(LedActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(LedActivity.this);
				} 
				break;
			}
			case R.id.pass: {
				flag_led = 1;
				if(flag_led > 0){
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("led", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(LedActivity.this);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("led", "ok");
						editor.commit();
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(LedActivity.this);
					} else {
						ActivityManagers.trunToBurnStartActivity(LedActivity.this);
					} 
				}
				break;
			}
			case R.id.fail: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("led", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(LedActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("led", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(LedActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(LedActivity.this);
				} 
				break;
			}
			case R.id.test:
				led_test_method(200);
			break;
			default:
				break;
			}
		}

	}
	
	public void led_test_method(int timer){
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				int ret1 = arqMisc.led(0x01, 0x01);
				int ret2 = arqMisc.led(0x02, 0x01);
				int ret3 = arqMisc.led(0x03, 0x01);
				int ret4 = arqMisc.led(0x04, 0x01);
				swLed1.setChecked(true);
				swLed2.setChecked(true);
				swLed3.setChecked(true);
				swLed4.setChecked(true);
				if(ret1 > 0 && ret2 > 0 && ret3 > 0 && ret4 > 0)
					flag_led = 3;
			}
		}, timer);
	}
	@SuppressLint("NewApi")
	public void led_test_method_shake(int timer){
		arqMisc.led(0x04, 0x01);
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				int ret = arqMisc.led(0x04, 0x00);
				swLed4.setChecked(false);
				ret = arqMisc.led(0x03, 0x01);
				swLed3.setChecked(true);
				
			}
		}, timer);
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				int ret = arqMisc.led(0x03, 0x00);
				swLed3.setChecked(false);
				ret = arqMisc.led(0x02, 0x01);
				swLed2.setChecked(true);
			}
			
		}, timer*2);
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				int ret = arqMisc.led(0x02, 0x00);
				swLed2.setChecked(false);
				ret = arqMisc.led(0x01, 0x01);
				swLed1.setChecked(true);
			}
			
		}, timer*3);
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				int ret = arqMisc.led(0x01, 0x00);
				swLed1.setChecked(false);
			}
			
		}, timer*4);
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				int ret = arqMisc.led(0x01, 0x01);
				ret = arqMisc.led(0x02, 0x01);
				ret = arqMisc.led(0x03, 0x01);
				ret = arqMisc.led(0x04, 0x01);
				swLed1.setChecked(true);
				swLed2.setChecked(true);
				swLed3.setChecked(true);
				swLed4.setChecked(true);
				if(ret >= 0){
					flag_led++;
					if(flag_led > 0){
						isTestOver = true;
						if(isburning == true)
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									isSleepExit = false;
									editor.putString("led", "ok");
									editor.commit();
									ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_VERSION);
									finish();
								}
							}, 2000);
					}else{
						led_test_method(300);
					}
				}
			}
			
		}, 2500);
	}
	@SuppressLint("NewApi")
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int ret = -1;
				ret= arqMisc.led(1, 0);
				ret = arqMisc.led(2, 0);
				ret = arqMisc.led(3, 0);
				ret = arqMisc.led(4, 1);
		    	if(ret < 0) {
		    		Log.e(TAG, "turn off failed.");
		    	} else {
		    		FyLog.i(TAG, "turn off success.");
		    	} 
			}
		}, 1);
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "onStop");
		swLed1.setChecked(false);
		swLed2.setChecked(false);
		swLed3.setChecked(false);
		swLed4.setChecked(true);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int ret = -1;
				ret= arqMisc.led(1, 0);
				ret = arqMisc.led(2, 0);
				ret = arqMisc.led(3, 0);
				ret = arqMisc.led(4, 1);
		    	if(ret < 0) {
		    		Log.e(TAG, "turn off failed.");
		    	} else {
		    		FyLog.i(TAG, "turn off success.");
		    	} 
			}
		}, 1);
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
					isburning = false;
					editor.putBoolean("burn", false);
					editor.commit();
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
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				led_image.setBackgroundResource(R.drawable.bg_transport);
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

	/**
	 * 更改背光亮度
	 * 
	 * @param activity
	 */
	public void toggleBrightness(Activity activity, int light) {
		// 获取亮度值
		int brightness = getScreenBrightness(activity);
		// 是否亮度自动调节，如果是则关闭自动调节
		boolean isAutoBrightness = isAutoBrightness(getContentResolver());
		if (isAutoBrightness) {
			stopAutoBrightness(activity);
		}
		// brightness += 50;// 按自己的需求设置
		// 设置亮度
		setBrightness(activity, light);

		if (brightness > 255) {
			// 亮度超过最大值后设置为自动调节
			startAutoBrightness(activity);
			brightness = 50;// 按自己的需求设置
		}
		// 保存设置状态
		saveBrightness(getContentResolver(), brightness);
	}

	/**
	 * 判断是否开启了自动亮度调节
	 * 
	 * @param aContext
	 * @return
	 */
	public boolean isAutoBrightness(ContentResolver aContentResolver) {
		boolean automicBrightness = false;
		try {
			automicBrightness = Settings.System.getInt(aContentResolver,
					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}

	/**
	 * 获取屏幕的亮度
	 * 
	 * @param activity
	 * @return
	 */
	public int getScreenBrightness(Activity activity) {
		int nowBrightnessValue = 0;
		ContentResolver resolver = activity.getContentResolver();
		try {
			nowBrightnessValue = android.provider.Settings.System.getInt(
					resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}

	/**
	 * 设置亮度
	 * 
	 * @param activity
	 * @param brightness
	 */
	public void setBrightness(Activity activity, int brightness) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
		activity.getWindow().setAttributes(lp);

	}

	/**
	 * 停止自动亮度调节
	 * 
	 * @param activity
	 */
	public void stopAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	/**
	 * 开启亮度自动调节
	 * 
	 * @param activity
	 */
	public void startAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	/**
	 * 保存亮度设置状态
	 * 
	 * @param resolver
	 * @param brightness
	 */
	public void saveBrightness(ContentResolver resolver, int brightness) {
		Uri uri = android.provider.Settings.System
				.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
		android.provider.Settings.System.putInt(resolver,
				Settings.System.SCREEN_BRIGHTNESS, brightness);
		resolver.notifyChange(uri, null);
	}
}
