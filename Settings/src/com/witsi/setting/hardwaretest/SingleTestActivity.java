package com.witsi.setting.hardwaretest;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.witsi.arq.ArqMisc;

public class SingleTestActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = SingleTestActivity.class.getSimpleName();
	private Context context = SingleTestActivity.this;
	/************************* 申明区 *********************************/
	private Button button_main_touchscreen;
	private Button button_main_screen;
	private Button button_main_magc;
	private Button button_main_icc;
	private Button button_main_sam;
	private Button button_main_rf;
	private Button button_main_key;
	private Button button_main_camera; 
	private Button button_main_security;
	private Button button_main_version;
	private Button button_main_wifi;
	private Button button_main_gprs;
	private Button button_main_TF; 
	private Button button_main_serial_number;
	private Button button_main_rtc; 
	private Button button_main_led;
	private Button button_main_media;
	private Button button_main_buzzer;
	private Button button_main_bluetooth;
	private Button button_main_printer;
	
	private Button button_main_return_home;
	
	private Button btn_buzzer;
	private Button btn_printer;
	private boolean isBluetoothEnable = true;
	
	private WifiAdmin mywifiAdmin;
	/* —————————————————————————————————————————————————————————————————— */
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//关闭休眠
		setContentView(R.layout.hardware_single_test_activity);
	//	SysApplication.getInstance().addActivity(this);
		/************************ 成员初始化区 ********************************/
		toggleBrightness(this,200);//背光
		button_main_wifi = (Button) this.findViewById(R.id.main_wifi);
		button_main_printer = (Button) this.findViewById(R.id.main_printer);
		button_main_buzzer = (Button) this.findViewById(R.id.main_buzzer);
		button_main_led = (Button) this.findViewById(R.id.main_led);
		button_main_version = (Button) this.findViewById(R.id.main_versions);
		button_main_key = (Button) this.findViewById(R.id.main_key);
		button_main_magc = (Button) this.findViewById(R.id.main_magc);
		button_main_icc = (Button) this.findViewById(R.id.main_icc);
		button_main_sam = (Button) this.findViewById(R.id.main_sam);
		button_main_rf = (Button) this.findViewById(R.id.main_rf);
		button_main_security = (Button) this.findViewById(R.id.main_security);
		button_main_screen = (Button) this.findViewById(R.id.main_screen);
		button_main_touchscreen = (Button) this.findViewById(R.id.main_touchscreen);
		button_main_gprs = (Button) this.findViewById(R.id.main_GPRS);
		button_main_TF = (Button) this.findViewById(R.id.main_TF);
		button_main_serial_number = (Button) this.findViewById(R.id.main_serial_number);
		button_main_return_home = (Button) this.findViewById(R.id.main_return_home);
		button_main_camera = (Button) this.findViewById(R.id.main_camera);
		button_main_rtc = (Button) this.findViewById(R.id.main_rtc);
		button_main_media = (Button) this.findViewById(R.id.main_media);
		button_main_bluetooth = (Button) this.findViewById(R.id.main_bluetooth);
		//根据设备支不支持蓝牙进行配置
		BluetoothAdapter bt_classic, bt_ble = null;
		bt_classic = BluetoothAdapter.getDefaultAdapter();
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			final BluetoothManager bluetoothManager =
	                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			bt_ble = bluetoothManager.getAdapter();
        }
		if(bt_classic == null && bt_ble == null){
			isBluetoothEnable = false;
		}
		//根据不同设备差异进行配置
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102
				|| ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			if(isBluetoothEnable){
				button_main_buzzer.setText("蓝牙测试");
				button_main_bluetooth.setText("打印机测试");
				button_main_printer.setText("返回主页");
				button_main_printer.setTextColor(Color.RED);
				button_main_return_home.setVisibility(View.GONE);
			}else{
				button_main_buzzer.setText("打印机测试");
				button_main_bluetooth.setText("返回主页");
				button_main_bluetooth.setTextColor(Color.RED);
				button_main_printer.setVisibility(View.GONE);
				button_main_return_home.setVisibility(View.GONE);
			}
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			button_main_buzzer.setText("振子测试");
		}
		/* —————————————————————————————————————————————————————————————————— */
		/************************ 监听事件初始化区 ****************************/
		button_main_buzzer.setOnClickListener(SingleTestActivity.this);
		button_main_wifi.setOnClickListener(SingleTestActivity.this);
		button_main_printer.setOnClickListener(SingleTestActivity.this);
		button_main_led.setOnClickListener(SingleTestActivity.this);
		button_main_version.setOnClickListener(SingleTestActivity.this);
		button_main_key.setOnClickListener(SingleTestActivity.this);
		button_main_magc.setOnClickListener(SingleTestActivity.this);
		button_main_icc.setOnClickListener(SingleTestActivity.this);
		button_main_sam.setOnClickListener(SingleTestActivity.this);
		button_main_rf.setOnClickListener(SingleTestActivity.this);
		button_main_security.setOnClickListener(SingleTestActivity.this);
		button_main_screen.setOnClickListener(SingleTestActivity.this);
		button_main_touchscreen.setOnClickListener(SingleTestActivity.this);
		button_main_gprs.setOnClickListener(SingleTestActivity.this);
		button_main_TF.setOnClickListener(SingleTestActivity.this);
		button_main_return_home.setOnClickListener(SingleTestActivity.this);
		button_main_serial_number.setOnClickListener(SingleTestActivity.this);
		button_main_camera.setOnClickListener(SingleTestActivity.this);
		button_main_rtc.setOnClickListener(SingleTestActivity.this);
		button_main_media.setOnClickListener(SingleTestActivity.this);
		button_main_bluetooth.setOnClickListener(SingleTestActivity.this);
		mywifiAdmin = new WifiAdmin(SingleTestActivity.this);
		
	}

	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}
	
	@Override
	public void onClick(View mainClick) {
		Intent intent;
		Bundle mybundle = new Bundle();
		intent = new Intent();
//运行状态
		mybundle.putInt("error", 0);
		mybundle.putInt("burntimes", 0);
		mybundle.putBoolean("flag_burn", false);
		mybundle.putBoolean("light", true);
//显示的测试结果	
		mybundle.putString("touchscreen", "无");
		mybundle.putString("screen", "无");
		mybundle.putString("magc", "无");
		mybundle.putString("printer", "无");
		mybundle.putString("buzzer", "无");
		mybundle.putString("security", "无");
		mybundle.putString("key", "无");
		mybundle.putString("ic", "无");
		mybundle.putString("led", "无");
		mybundle.putString("version", "无");
		mybundle.putString("gprs", "无");
		mybundle.putString("wifi", "无");
		mybundle.putString("tf", "无");
		mybundle.putString("serialnumber", "无");
				
//错误次数记录
		mybundle.putInt("error_touchscreen", 0);
		mybundle.putInt("error_screen", 0);
		mybundle.putInt("error_magc", 0);
		mybundle.putInt("error_printer", 0);
		mybundle.putInt("error_buzzer", 0);
		mybundle.putInt("error_security", 0);
		mybundle.putInt("error_key", 0);
		mybundle.putInt("error_ic", 0);
		mybundle.putInt("error_led", 0);
		mybundle.putInt("error_version", 0);
		mybundle.putInt("error_gprs", 0);
		mybundle.putInt("error_wifi", 0);
		mybundle.putInt("error_tf", 0);
		mybundle.putInt("error_serialnumber", 0);
//时间记录
		mybundle.putInt("year", 0);
		mybundle.putInt("month", 0);
		mybundle.putInt("date", 0);
		mybundle.putInt("hour", 0);
		mybundle.putInt("minute", 0);
		mybundle.putInt("second", 0);
		mybundle.putInt("burntime", 0);

/* 标记被选中要进行拷机的项目 */
		mybundle.putBoolean("flag_checkbox_touchscreen", false);
		mybundle.putBoolean("flag_checkbox_screen", true);
		mybundle.putBoolean("flag_checkbox_magc", false);
		mybundle.putBoolean("flag_checkbox_printer", false);
		mybundle.putBoolean("flag_checkbox_buzzer", true);
		mybundle.putBoolean("flag_checkbox_security", true);
		mybundle.putBoolean("flag_checkbox_key", false);
		mybundle.putBoolean("flag_checkbox_ic", true);
		mybundle.putBoolean("flag_checkbox_led", false);
		mybundle.putBoolean("flag_checkbox_version", true);
		mybundle.putBoolean("flag_checkbox_gprs", false);
		mybundle.putBoolean("flag_checkbox_wifi", true);
		mybundle.putBoolean("flag_checkbox_tf", false);
		mybundle.putBoolean("flag_checkbox_serialnumber", false);
		mybundle.putBoolean("singletest", true);
		mybundle.putBoolean("alltest", false);
		
		isSleepExit = false;
		switch (mainClick.getId()) {
		case R.id.main_touchscreen: {
			intent = new Intent(SingleTestActivity.this,
					TouchScreenActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_screen: {
			intent = new Intent(SingleTestActivity.this, ScreenActivity.class);
			intent.putExtras(mybundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;
		}
		case R.id.main_magc: {
			intent = new Intent(SingleTestActivity.this, MagcActivity.class);
			intent.putExtras(mybundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;
		}
		case R.id.main_icc: {
			intent = new Intent(SingleTestActivity.this, IccActivity.class);
			intent.putExtras(mybundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;
		}
		case R.id.main_sam: {
			intent = new Intent(SingleTestActivity.this, SamActivity.class);
			intent.putExtras(mybundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;
		}
		case R.id.main_rf: {
			intent = new Intent(SingleTestActivity.this, RfActivity.class);
			intent.putExtras(mybundle);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			break;
		}
		case R.id.main_key: {
			intent = new Intent(SingleTestActivity.this, KeyActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_camera: {
			intent = new Intent(SingleTestActivity.this,
					CameraActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_security: {
			intent = new Intent(SingleTestActivity.this, SecurityActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_versions: {
			intent = new Intent(SingleTestActivity.this, VersionActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_wifi: {
//			/* —————————————————————————————————————————————————————————————————— */
			mywifiAdmin.OpenWifi();
			intent = new Intent(SingleTestActivity.this, WifiActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_GPRS: {
			WifiActivity.setMobileData(SingleTestActivity.this, true);
			intent = new Intent(SingleTestActivity.this, GPRS_Activity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_TF: {
			intent = new Intent(SingleTestActivity.this,
					TF_Activity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_serial_number: {
			intent = new Intent(SingleTestActivity.this, SerialNumberActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_rtc: {
			intent = new Intent(SingleTestActivity.this,
					RtcActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_led: {
			intent = new Intent(SingleTestActivity.this, LedActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		case R.id.main_media: {
			intent = new Intent(SingleTestActivity.this, MediaRecordActivity.class);
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		
		case R.id.main_buzzer: {
			if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102
					|| ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
				if(isBluetoothEnable)
					intent = new Intent(SingleTestActivity.this, BluetoothActivity.class);
				else
					intent = new Intent(SingleTestActivity.this, PrinterActivity.class);
				
			}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
				intent = new Intent(SingleTestActivity.this, ShakeActivity.class);
			}else{
				intent = new Intent(SingleTestActivity.this, BuzzerActivity.class);
			}
			intent.putExtras(mybundle);
			startActivity(intent);
			break;
		}
		
		case R.id.main_bluetooth:{
			if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102
					|| ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
				if(isBluetoothEnable){
					intent = new Intent(SingleTestActivity.this, PrinterActivity.class);
					intent.putExtras(mybundle);
					startActivity(intent);
				}else
					ActivityManagers.trunToEntryActivity(SingleTestActivity.this);
				
			}else{
				intent = new Intent(SingleTestActivity.this, BluetoothActivity.class);
				intent.putExtras(mybundle);
				startActivity(intent);
			}
			break;
		}
			
		case R.id.main_printer: {
			if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102
					|| ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
				ActivityManagers.trunToEntryActivity(SingleTestActivity.this);
			}else{
				intent = new Intent(SingleTestActivity.this, PrinterActivity.class);
				intent.putExtras(mybundle);
				startActivity(intent);
			}
			break;
		}
		
		case R.id.main_return_home: {
			ActivityManagers.trunToEntryActivity(SingleTestActivity.this);
			break;
		}
		
		default:
			break;
		}
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
