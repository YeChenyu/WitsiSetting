package com.witsi.setting.hardwaretest;


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
import android.widget.Button;
import android.widget.ImageView;
import android.witsi.arq.ArqMisc;


public class BuzzerActivity extends Activity implements
		android.view.View.OnClickListener {
	private String TAG = "BuzzerActivity";
	private Context context = BuzzerActivity.this;
	
	private Button button_Buzzer_return;
	private Button button_Buzzer_ok;
	private Button button_Buzzer_false;
	private Button button_Buzzer_test;
	private ImageView buzzer_image;
	private View main;
	private ArqMisc miscArq;
	private Thread thread = null;
	private MyRunnable myrun = new MyRunnable();
	private Handler handler = new Handler();
	
	public int falg_buzzer = -1;
	private boolean screen_sleep = false;
	private boolean isburning = false;
	
	private SharedPreferences config;
	private Editor editor;
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		FyLog.i(TAG, "进入蜂鸣器测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_buzzer_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_buzzer_activity);
			screen_sleep = false;
		}
		
		// SysApplication.getInstance().addActivity(this);
		miscArq = new ArqMisc(this);
		View v = findViewById(R.id.ll_tool);
		button_Buzzer_return = (Button) v.findViewById(R.id.back);
		button_Buzzer_ok = (Button) v.findViewById(R.id.pass);
		button_Buzzer_false = (Button) v.findViewById(R.id.fail);
		button_Buzzer_test = (Button) v.findViewById(R.id.test);
		button_Buzzer_return.setOnClickListener(BuzzerActivity.this);
		button_Buzzer_ok.setOnClickListener(BuzzerActivity.this);
		button_Buzzer_false.setOnClickListener(BuzzerActivity.this);
		button_Buzzer_test.setOnClickListener(BuzzerActivity.this);
		//屏幕休眠
		buzzer_image = (ImageView) this.findViewById(R.id.buzzer_image);
		if (config.getBoolean("light", true) == false) {
			buzzer_image.setBackgroundResource(R.drawable.bg_black);
		} 
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
	}

	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		// 直接开启蜂鸣器
		FyLog.i(TAG, "<<<<<<<<< Buzzer on. >>>>>>>>>>>");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (myrun.isThreadRunnig() == false) {
					thread = new Thread(myrun);
					thread.start();
				}
			}
		}, 800);
		
		if (isburning == true) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					myrun.stopThread();
						isSleepExit = false;
						if(falg_buzzer >= 0){
							editor.putString("buzzer", "ok");
							editor.commit();
						}else{
							editor.putString("buzzer", "ng");
							editor.putInt("error_buzzer", config.getInt("error_buzzer", 0) + 1);
							editor.putInt("error", 1);
							editor.commit();
						}
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SECURITY);
						finish();
				}
			}, 3000);
		}
	}
	@Override
	public void onClick(View BuzzerClick) {
		if (screen_sleep == true) {
			buzzer_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
			toggleBrightness(this, 200);// 背光
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (BuzzerClick.getId()) {
			case R.id.back: {
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(BuzzerActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(BuzzerActivity.this);
				}else{
					ActivityManagers.trunToBurnStartActivity(BuzzerActivity.this);
				} 
				break;
			}
			case R.id.pass: {
				falg_buzzer = 1;
				if (config.getBoolean("singletest", false) == true){
					editor.putString("buzzer", "ok");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(BuzzerActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("buzzer", "ok");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(BuzzerActivity.this);
				}else{
					ActivityManagers.trunToBurnStartActivity(BuzzerActivity.this);
				} 
				break;
			}
			case R.id.fail: {
				falg_buzzer = -1;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("buzzer", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(BuzzerActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("buzzer", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(BuzzerActivity.this);
				}else{
					ActivityManagers.trunToBurnStartActivity(BuzzerActivity.this);
				} 
				break;
			}
			case R.id.test: {
				// /* 重新测试一次 */
				if (myrun.isThreadRunnig() == false) {
					Thread thread = new Thread(myrun);
					thread.start();
				}
				 break;
			}
			default:
				break;
			}
		}
	}

	public void buzzerStartBtnOnClickHandler(View source) {
		if (myrun.isThreadRunnig() == false) {
			Thread thread = new Thread(myrun);
			thread.start();
		}
		FyLog.i(TAG, "<<<<<<<<< Buzzer on. >>>>>>>>>>>");
	}

	public void buzzerStopBtnOnClickHandler(View source) {
		if (myrun.isThreadRunnig() == true) {
			myrun.stopThread();
		}
		super.onPause();
		FyLog.i(TAG, "<<<<<<<<< Buzzer off. >>>>>>>>>>>");
	}

	class MyRunnable implements Runnable {
		private boolean threadRunning = false;

		public void stopThread() {
			threadRunning = false;
		}

		public boolean isThreadRunnig() {
			return threadRunning;
		}

		public void run() {
			FyLog.i(TAG, "buzzer thread working.");
			int repeat, on, off;

			repeat = 1;
			on = 200;
			off = 300;

			threadRunning = true;

			while (threadRunning) {
				falg_buzzer = miscArq.buzzerCtl(repeat, on, off);
				if (falg_buzzer < 0) {
					FyLog.e(TAG, "buzzer control failed. ret = " + falg_buzzer);
					continue;
				}
				/* wait for 0.5s */
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					FyLog.d(TAG, "Waiting didnt work!!");
					e.printStackTrace();
				}
				FyLog.i(TAG, "buzzer control success.");
			}
			threadRunning = false;
		}
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "onStop");
		if (myrun.isThreadRunnig() == true) {
			myrun.stopThread();
		}
		if(!isSleepExit){
			finish();
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "退出蜂鸣器测试界面");
		if(thread != null){
			thread.interrupt();
			thread = null;
			myrun = null;
		}
		if(handler != null)
			handler = null;
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
	
	/************************** 事件监听申明区 ***************************/
	//以上为点击返回键填出提示窗口	 
	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				buzzer_image.setBackgroundResource(R.drawable.bg_transport);
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