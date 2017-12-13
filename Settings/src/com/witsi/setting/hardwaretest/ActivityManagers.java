package com.witsi.setting.hardwaretest;


import java.util.ArrayList;
import java.util.List;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ActivityManagers {

	private static String TAG = ActivityManagers.class.getSimpleName();
	
	private static int test_activity = 0;
	private static int burning_index = 0;
	private static List<Class<?>> lstBurn = null;
	
	public static void trunToNextActivity(){
		test_activity ++;
	}
	public static void clearActivity(){
		test_activity = 0;
	}
	
//	全部测试
	private static Class<?>[] classes = {
		//手动测试
		CameraActivity.class,
		TouchScreenActivity.class, 
		MagcActivity.class, 
		ScreenActivity.class, 
		KeyActivity.class, 
		VersionActivity.class,  
		LedActivity.class, 
		RtcActivity.class, 
		BuzzerActivity.class, 
		MediaRecordActivity.class,
		BluetoothActivity.class,
		ShakeActivity.class,
		IccActivity.class, 
		SamActivity.class,
		RfActivity.class,
		//自动测试
		WifiActivity.class, 
		SecurityActivity.class, 
		TF_Activity.class, 
		SerialNumberActivity.class, 
		GPRS_Activity.class, 
		PrinterActivity.class, 
		//测试结果列表
		ResultTableActivity.class, 
	};
	
	public static void startNextActivity(Context context){
		Intent intent;
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
			if(classes[test_activity].getSimpleName().equals("MediaRecordActivity"))
				test_activity++;
			if(classes[test_activity].getSimpleName().equals("BluetoothActivity"))
				test_activity++;
			if(classes[test_activity].getSimpleName().equals("ShakeActivity"))
				test_activity++;
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			if(classes[test_activity].getSimpleName().equals("BuzzerActivity"))
				test_activity++;
			if(classes[test_activity].getSimpleName().equals("ShakeActivity"))
				test_activity++;
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			if(classes[test_activity].getSimpleName().equals("BuzzerActivity"))
				test_activity++;
		}
		intent = new Intent(context, classes[test_activity]);
		FyLog.v(TAG, "the activity is: " + classes[test_activity].getSimpleName());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
		context.startActivity(intent);
	}
	
	public static void trunToSingleTestActivity(Context context){
		Intent intent;
		intent = new Intent(context, SingleTestActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}
	public static void trunToEntryActivity(Context context){
		Intent intent;
		intent = new Intent(context, EntryActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}
	public static void trunToBurnStartActivity(Context context){
		Intent intent;
		intent = new Intent(context, BurnningActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}
	
	public static void initBurnningConfig(SharedPreferences config){
		if(lstBurn == null)
			lstBurn = new ArrayList<Class<?>>();
		lstBurn.clear();
		for (int i = 0; i < 11 + 1; i++) {
			if(config.getBoolean("flag_checkbox_screen", false) == true 
					&& !lstBurn.contains(ScreenActivity.class)){
				lstBurn.add(ScreenActivity.class);
				FyLog.d(TAG, "ScreenActivity");
			}else if(config.getBoolean("flag_checkbox_buzzer", false) == true 
					&& !lstBurn.contains(BuzzerActivity.class)){
					lstBurn.add(BuzzerActivity.class);
				FyLog.d(TAG, "BuzzerActivity");
			}else if(config.getBoolean("flag_checkbox_security", false) == true 
					&& !lstBurn.contains(SecurityActivity.class)){
					lstBurn.add(SecurityActivity.class);
				FyLog.d(TAG, "SecurityActivity");
			}else if(config.getBoolean("flag_checkbox_ic", false) == true 
					&& !lstBurn.contains(IccActivity.class)){
					lstBurn.add(IccActivity.class);
				FyLog.d(TAG, "IccActivity");
			}else if(config.getBoolean("flag_checkbox_sam", false) == true 
					&& !lstBurn.contains(SamActivity.class)){
					lstBurn.add(SamActivity.class);
				FyLog.d(TAG, "SamActivity");
			}else if(config.getBoolean("flag_checkbox_rf", false) == true 
					&& !lstBurn.contains(RfActivity.class)){
					lstBurn.add(RfActivity.class);
				FyLog.d(TAG, "RfActivity");
			}else if(config.getBoolean("flag_checkbox_led", false) == true 
					&& !lstBurn.contains(LedActivity.class)){
				lstBurn.add(LedActivity.class);
				FyLog.d(TAG, "LedActivity");
			}else if(config.getBoolean("flag_checkbox_version", false) == true 
					&& !lstBurn.contains(VersionActivity.class)){
				lstBurn.add(VersionActivity.class);
				FyLog.d(TAG, "VersionActivity");
			}else if(config.getBoolean("flag_checkbox_tf", false) == true 
					&& !lstBurn.contains(TF_Activity.class)){
				lstBurn.add(TF_Activity.class);
				FyLog.d(TAG, "TF_Activity");
			}else if(config.getBoolean("flag_checkbox_serialnumber", false) == true 
					&& !lstBurn.contains(SerialNumberActivity.class)){
					lstBurn.add(SerialNumberActivity.class);
				FyLog.d(TAG, "SerialNumberActivity");
			}else if(config.getBoolean("flag_checkbox_shake", false) == true 
					&& !lstBurn.contains(ShakeActivity.class)){
					lstBurn.add(ShakeActivity.class);
				FyLog.d(TAG, "ShakeActivity");
			}else if(!lstBurn.contains(ResultTableActivity.class)){
					lstBurn.add(ResultTableActivity.class);
				FyLog.d(TAG, "ResultTableActivity");
			}
		}
		FyLog.d(TAG, "the size is: " + lstBurn.size());
	}
	
//	拷机测试
	public static  synchronized void toNextBurnTest1(Context context, SharedPreferences config){
		Intent intent;
		FyLog.e(TAG, "the burning activity num is: " + lstBurn.size());
		if(lstBurn.size() < 1)
			initBurnningConfig(config);
		intent = new Intent(context, lstBurn.get(burning_index));
		if(++burning_index >= lstBurn.size())
			burning_index = 0;
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
		context.startActivity(intent);
	}
	
	public static int BURN_SCREEN = 0;
	public static int BURN_BUZZER = 1;
	public static int BURN_SECURITY = 2;
	public static int BURN_IC = 3;
	public static int BURN_SAM = 4;
	public static int BURN_RF = 5;
	public static int BURN_LED = 6;
	public static int BURN_VERSION = 7;
	public static int BURN_TF = 8;
	public static int BURN_SERIALNUMBER = 9;
	public static int BURN_RESULTTABLE = 10;
	
	public static  synchronized void toNextBurnTest(Context context, SharedPreferences config, int index){
		Intent intent = null;
		switch (index) {
		case 0:
			if(config.getBoolean("flag_checkbox_screen", false) == true ){
				intent = new Intent(context, ScreenActivity.class);
				break;
			}
		case 1:
			if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R)
				if(config.getBoolean("flag_checkbox_buzzer", false) == true ){
					intent = new Intent(context, BuzzerActivity.class);
					break;
				}
		case 2:
			if(config.getBoolean("flag_checkbox_security", false) == true ){
				intent = new Intent(context, SecurityActivity.class);
				break;
			}
		case 3:
			if(config.getBoolean("flag_checkbox_ic", false) == true ){
				intent = new Intent(context, IccActivity.class);
			break;
			}
		case 4:
			if(config.getBoolean("flag_checkbox_sam", false) == true ){
				intent = new Intent(context, SamActivity.class);
			break;
			}
		case 5:
			if(config.getBoolean("flag_checkbox_rf", false) == true ){
				intent = new Intent(context, RfActivity.class);
			break;
			}
		case 6:
			if(config.getBoolean("flag_checkbox_led", false) == true ){
				intent = new Intent(context, LedActivity.class);
			break;
			}
		case 7:
			if(config.getBoolean("flag_checkbox_version", false) == true ){
				intent = new Intent(context, VersionActivity.class);
			break;
			}
		case 8:
			if(config.getBoolean("flag_checkbox_tf", false) == true ){
				intent = new Intent(context, TF_Activity.class);
			break;
			}
		case 9:
			if(config.getBoolean("flag_checkbox_serialnumber", false) == true ){
				intent = new Intent(context, SerialNumberActivity.class);
			break;
			}
		case 10:
			intent = new Intent(context, ResultTableActivity.class);
			break;
		default:
			break;
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
		context.startActivity(intent);
	}
	
}
