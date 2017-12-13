package com.witsi.setting.hardwaretest;


import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenCheckActivity extends Activity implements
		android.view.View.OnClickListener {
	
	String TAG = "ScreenCheckActivity";
	private Context context = ScreenCheckActivity.this;
	
	private Button button_ScreenCheck_return;
	private Button button_ScreenCheck_ok;
	private Button button_ScreenCheck_false;
	private Button button_ScreenCheck_test;
	View main = null;
	ImageView screencheck_image;
	public TextView flag_screen_burn_state;
	
	boolean screen_sleep = false;
	boolean isburning = false;
	
	private SharedPreferences config;
	private Editor editor;
	
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		FyLog.i(TAG, "进入屏幕坏点测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			// 假装隐藏……好吧~
			main = LayoutInflater.from(context).inflate(
					R.layout.hardware_screencheck_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_screencheck_activity);
			screen_sleep = false;
		}
		/************ UI **********************/
		flag_screen_burn_state = (TextView) this.findViewById(R.id.flag_screen_burn_state);
		button_ScreenCheck_return = (Button) this.findViewById(R.id.back);
		button_ScreenCheck_ok = (Button) this.findViewById(R.id.pass);
		button_ScreenCheck_false = (Button) this.findViewById(R.id.fail);
		button_ScreenCheck_test = (Button) this.findViewById(R.id.test);
		button_ScreenCheck_return.setOnClickListener(ScreenCheckActivity.this);
		button_ScreenCheck_ok.setOnClickListener(ScreenCheckActivity.this);
		button_ScreenCheck_false.setOnClickListener(ScreenCheckActivity.this);
		button_ScreenCheck_test.setOnClickListener(ScreenCheckActivity.this);
		//休眠是背景黑色
		screencheck_image = (ImageView) this.findViewById(R.id.screencheck_image);
		if (screen_sleep == true) {
			screencheck_image.setBackgroundResource(R.drawable.bg_black);
		} 
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		if (isburning == true) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					//开始拷机测试
					isSleepExit = false;
					editor.putString("screen", "ok");
					editor.commit();
					ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_BUZZER);
					finish();
				}
			}, 3000);
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}

	@Override
	public void onClick(View screencheckClick) {
		isSleepExit = false;
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			screencheck_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			switch (screencheckClick.getId()) {
			case R.id.back: {
				//返回主界面
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(ScreenCheckActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.clearActivity();
					ActivityManagers.trunToEntryActivity(ScreenCheckActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(ScreenCheckActivity.this);
				} 
				break;
			}
			//测试通过
			case R.id.pass: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("screen", "ok");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(ScreenCheckActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("screen", "ok");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(ScreenCheckActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(ScreenCheckActivity.this);
				} 
				break;
			}
			//测试失败
			case R.id.fail: {
				if(config.getBoolean("singletest", false) == true) {
					editor.putString("screen", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(ScreenCheckActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("screen", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(ScreenCheckActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(ScreenCheckActivity.this);
				} 
				break;
			}
			//重新测试
			case R.id.test: {
				/* 重新测试一次 */
				Intent intent = new Intent(ScreenCheckActivity.this, ScreenActivity.class);
				startActivity(intent);
				break;
			}
			default:
				break;
			}
		}
	}

	
	private boolean isSleepExit = true;
	@Override
	public void onStop() {
		super.onStop();
		FyLog.i(TAG, "onStop()");
		if(!isSleepExit){
			finish();
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
	}
	/************************** 事件监听申明区 ***************************/
	@SuppressWarnings("deprecation")
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
				screencheck_image.setBackgroundResource(R.drawable.bg_transport);
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
/* ------------------------------------------------------------------------------------------------------------*/
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
