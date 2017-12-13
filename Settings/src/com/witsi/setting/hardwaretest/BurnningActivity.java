package com.witsi.setting.hardwaretest;



import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class BurnningActivity extends Activity implements
		android.view.View.OnClickListener {
	private String TAG = "BurnStartActivity";
	private static final boolean D = true;
	private Context context = BurnningActivity.this;
	
	private Button button_burnstart_return_to_home, button_burnstart_choose,
		button_burnstart_recode, button_burnstart_0_5h,
		button_burnstart_1h, button_burnstart_2h, button_burnstart_4h,
		button_burnstart_24h, button_burnstart_48h;
	
	private FileHelper helper;
	private boolean isburning = false;
	private int year, month, date, hour, minute, second, burntime;
	
	private SharedPreferences config;
	private Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.hardware_burnning_activity);
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		// SysApplication.getInstance().addActivity(this);
		toggleBrightness(this, 200);
		helper = new FileHelper(getApplicationContext());
		button_burnstart_return_to_home = (Button) this.findViewById(R.id.burn_startGetBackToMain);
		button_burnstart_choose = (Button) this.findViewById(R.id.test_burn_start_choose);
		button_burnstart_recode = (Button) this.findViewById(R.id.burn_Record);
		button_burnstart_0_5h = (Button) this.findViewById(R.id.burn_0_5h);
		button_burnstart_1h = (Button) this.findViewById(R.id.burn_1h);
		button_burnstart_2h = (Button) this.findViewById(R.id.burn_2h);
		button_burnstart_4h = (Button) this.findViewById(R.id.burn_4h);
		button_burnstart_24h = (Button) this.findViewById(R.id.burn_24h);
		button_burnstart_48h = (Button) this.findViewById(R.id.burn_48h);
		button_burnstart_0_5h.setOnClickListener(BurnningActivity.this);
		button_burnstart_1h.setOnClickListener(BurnningActivity.this);
		button_burnstart_2h.setOnClickListener(BurnningActivity.this);
		button_burnstart_4h.setOnClickListener(BurnningActivity.this);
		button_burnstart_24h.setOnClickListener(BurnningActivity.this);
		button_burnstart_48h.setOnClickListener(BurnningActivity.this);
		button_burnstart_return_to_home.setOnClickListener(BurnningActivity.this);
		button_burnstart_choose.setOnClickListener(BurnningActivity.this);
		button_burnstart_recode.setOnClickListener(BurnningActivity.this);
		if(D)FyLog.i(TAG, "进入");
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		
		ActivityManagers.initBurnningConfig(config);
		final WifiAdmin wifi = new WifiAdmin(BurnningActivity.this);
		wifi.CloseWifi();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(wifi.Wifistate() == 1){
					FyLog.e(TAG, "close the wifi for second:");
					wifi.CloseWifi();
				}
			}
		}, 2000);
		
	}

	@Override
	public void onClick(View v) {
		
		Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
		t.setToNow(); // 取得系统时间。
		year = t.year;
		month = t.month + 1;
		date = t.monthDay;
		hour = t.hour;
		minute = t.minute;
		second = t.second;
		editor.putInt("error_tf", 0);
		editor.putInt("year", year);
		editor.putInt("month", month);
		editor.putInt("date", date);
		editor.putInt("hour", hour);
		editor.putInt("minute", minute);
		editor.putInt("second", second);
		editor.putBoolean("light", true);
		editor.commit();
		
		editor.putString("touchscreen", "无");
		editor.putString("screen", "无");
		editor.putString("magc", "无");
		editor.putString("printer", "无");
		editor.putString("buzzer", "无");
		editor.putString("security", "无");
		editor.putString("key", "无");
		editor.putString("ic", "无");
		editor.putString("led", "无");
		editor.putString("version", "无");
		editor.putString("gprs", "无");
		editor.putString("wifi", "无");
		editor.putString("tf", "无");
		editor.putString("serialnumber", "无");
		editor.commit();
		
		isSleepExit = false;
		switch (v.getId()) {
		case R.id.burn_startGetBackToMain: {
			ActivityManagers.trunToEntryActivity(context);
			break;
		}
		case R.id.test_burn_start_choose: {
			Intent intent = new Intent(BurnningActivity.this, BurnningConfigActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.burn_Record: {
			String title;
			if(config.getBoolean("burn_success", false))
				title = "成功";
			else 
				title = "失败";
			if(config.getBoolean("burn_stop", false) == true)
				title = "手动停止";
			FyLog.d(TAG, "the burn stop is: " + config.getBoolean("burn_stop", false));
			String tmp = helper.readNativeFile("test.txt");
			if(tmp == null){
				tmp = "暂无拷机记录";
				title = "无";
			}
			new AlertDialog.Builder(BurnningActivity.this).setTitle("上次拷机记录：" + title)
					.setMessage(tmp)
					.setPositiveButton("取消", null).show();
			break;
		}
		case R.id.burn_0_5h: {
			burntime = date * 24 * 60 + hour * 60 + minute + 30;
			editor.putBoolean("flag_burn", true);
			editor.putInt("burntime", burntime);
			editor.putBoolean("burn_stop", false);
			editor.commit();
			ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SCREEN);
			break;
		}
		case R.id.burn_1h: {
			burntime = date * 24 * 60 + hour * 60 + minute + 60;
			editor.putBoolean("flag_burn", true);
			editor.putBoolean("burn_stop", false);
			editor.putInt("burntime", burntime);
			editor.commit();
			ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SCREEN);
			break;
		}
		case R.id.burn_2h: {
			burntime = date * 24 * 60 + hour * 60 + minute + 120;
			editor.putBoolean("flag_burn", true);
			editor.putBoolean("burn_stop", false);
			editor.putInt("burntime", burntime);
			editor.commit();
			ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SCREEN);
			break;
		}
		case R.id.burn_4h: {
			burntime = date * 24 * 60 + hour * 60 + minute + 240;
			editor.putBoolean("flag_burn", true);
			editor.putBoolean("burn_stop", false);
			editor.putInt("burntime", burntime);
			editor.commit();
			ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SCREEN);
			break;
		}
		case R.id.burn_24h: {
			burntime = date * 24 * 60 + hour * 60 + minute + 1440;
			editor.putBoolean("flag_burn", true);
			editor.putBoolean("burn_stop", false);
			editor.putInt("burntime", burntime);
			editor.commit();
			ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SCREEN);
			break;
		}
		case R.id.burn_48h: {
			burntime = date * 24 * 60 + hour * 60 + minute + 2880;
			editor.putBoolean("flag_burn", true);
			editor.putBoolean("burn_stop", false);
			editor.putInt("burntime", burntime);
			editor.commit();
			ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SCREEN);
			break;
		}

		default:
			break;
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
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
	
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
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
