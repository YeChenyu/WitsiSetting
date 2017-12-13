package com.witsi.setting.hardwaretest;

import java.util.Timer;
import java.util.TimerTask;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class TouchScreenActivity extends Activity implements Callback, OnTouchListener{

	private String TAG = "TouchScreenActivity";
	private Context context = TouchScreenActivity.this;
	
	private AlertDialog dialog = null;
	private DrawView touchScreen;
	
	private Timer showTimer;
	private Handler handler = null;
	private boolean isburning = false;
	private boolean touch_test_over = false;  //触摸超时
	
	private SharedPreferences config;
	private Editor editor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		setContentView(R.layout.hardware_touchscreen_activity);
		// SysApplication.getInstance().addActivity(this);
		FyLog.i(TAG, "进入触摸屏测试界面");
		
		initViews();
		
		initDatas();
	}
	
	private void initViews() {
		// TODO Auto-generated method stub
		touchScreen = (DrawView) findViewById(R.id.touch_screen);
		touchScreen.setOnTouchListener(this);
	}
	private void initDatas() {
		// TODO Auto-generated method stub
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//判断是否进行拷机
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		/***************************************************************************/
		handler = new Handler(this);
		showTimer = new Timer();
		if (isburning == false) {
			//在非拷机状态下，两秒后提示结束框
			showTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (touch_test_over == false){
						// 发送空消息
						handler.sendEmptyMessage(0x1212);
					}
				}
			}, 5000, 2000);
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}
	
	private int down = 0;
	private int distance = 0;
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:
			down = (int) arg1.getX();
			break;
		case MotionEvent.ACTION_UP:
			distance = Math.abs(distance - down);
			if(distance > 50 )
				// 发送空消息
				handler.sendEmptyMessage(0x1212);
			break;

		default:
			break;
		}
		return false;
	}
	
	
	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		// 如果该消息是本程序所发送的
		if (msg.what == 0x1212) {
			if (isburning == false) {
				FyLog.v(TAG, "测试结束！");
				if(dialog == null)
					dialog = new AlertDialog.Builder(TouchScreenActivity.this)
					.setTitle("测试结果")
					.setMessage("请选择操作")
					.setPositiveButton("通过", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0,
								int arg1) {
							isSleepExit = false;
							// 按键触发事件进入下一项测试或返回主界面
							if (config.getBoolean("singletest", false) == true) {
								//单项测试
								editor.putString("touchscreen", "ok");
								editor.commit();
								ActivityManagers.trunToSingleTestActivity(TouchScreenActivity.this);
							} else if(config.getBoolean("alltest", false) == true){
								//全部测试
								editor.putString("touchscreen", "ok");
								editor.commit();
								ActivityManagers.trunToNextActivity();
								ActivityManagers.startNextActivity(TouchScreenActivity.this);
							}else {
								ActivityManagers.trunToBurnStartActivity(TouchScreenActivity.this);
							} 
						}
					})
					.setNegativeButton("返回上级",
						new OnClickListener() {
							@Override
							public void onClick(
									DialogInterface arg0,
									int arg1) {
								isSleepExit = false;
								// 按键触发事件 根据不通的测试选择返回的主界面
								ActivityManagers.clearActivity();
								if (config.getBoolean("singletest", false) == true) {
									ActivityManagers.trunToSingleTestActivity(TouchScreenActivity.this);
								} else if(config.getBoolean("alltest", false) == true) {
									ActivityManagers.clearActivity();
									ActivityManagers.trunToEntryActivity(TouchScreenActivity.this);
								}else {
									ActivityManagers.trunToBurnStartActivity(TouchScreenActivity.this);
								}  
								
							}
						})
					.setNeutralButton("失败", new OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0,
								int arg1) {
							isSleepExit = false;
							if (config.getBoolean("singletest", false) == true) {
								editor.putString("touchscreen", "ng");
								editor.commit();
								ActivityManagers.trunToSingleTestActivity(TouchScreenActivity.this);
							} else if(config.getBoolean("alltest", false) == true){
								editor.putString("touchscreen", "ng");
								editor.commit();
								ActivityManagers.trunToNextActivity();
								ActivityManagers.startNextActivity(TouchScreenActivity.this);
							}else {
								ActivityManagers.trunToBurnStartActivity(TouchScreenActivity.this);
							} 
						}
					}).create();
				if(dialog != null && !dialog.isShowing()){
					dialog.show();
				}
			}
		}
		return false;
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
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		if(showTimer != null)
			showTimer.cancel();
	}
	
	//点击返回键填出提示窗口
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
        	if(dialog != null){
        		dialog.show();
        	}
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
            	isburning = false;
				editor.putBoolean("burn", false);
				editor.commit();
				ActivityManagers.clearActivity();
                finish();  
                break;  
            case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框  
                break;  
            default:  
                break;  
            }  
        }  
    };    
    
/*  -----------------------------------------------------------------------------------------------------------*/
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
