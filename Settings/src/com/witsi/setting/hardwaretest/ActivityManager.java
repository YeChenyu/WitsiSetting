package com.witsi.setting.hardwaretest;

import com.witsi.debug.FyLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityManager {

	private static String TAG = ActivityManager.class.getSimpleName();
//	private static final int ENTRY_ACTIVITY = -2;
//	private static final int SINGLE_TEST_ACTIVITY = -1;
//	private static final int CAMERA_ACTIVITY = 0;
//	private static final int MAGC_ACTIVITY = 1;
//	private static final int TOUCH_SCREEN_ACTIVITY = 2;
//	private static final int SCREEN_CHECK_ACTIVITY = 3;
//	private static final int KEY_ACTIVITY = 4;
//	private static final int WIFI_ACTIVITY = 5;
//	private static final int BUZZER_ACTIVITY = 6;
//	private static final int ICC_ACTIVITY = 7;
//	private static final int GPRS_ACTIVITY = 8;
//	private static final int LED_ACTIVITY = 9;
//	private static final int VERSION_ACTIVITY = 10;
//	private static final int TF_ACTIVITY = 11;
//	private static final int SERIAL_NUMBER_ACTIVITY = 12;
//	private static final int PRINTER_ACTIVITY = 13;
//	private static final int RESULT_TABLE_ACTIVITY = 14;
//	private static final int BURN_ACTIVITY = 15;
	
	private static int test_activity = 0;
	
	public static void trunToNextActivity(){
		test_activity ++;
	}
	public static void clearActivity(){
		test_activity = 0;
	}
	private static Class<?>[] classes = {
			TouchScreenActivity.class, ScreenActivity.class, 
			MagcActivity.class, IccActivity.class, 
			CameraActivity.class, KeyActivity.class, 
			LedActivity.class, //BuzzerActivity.class,
			WifiActivity.class, GPRS_Activity.class, 
			SecurityActivity.class, VersionActivity.class,  
			SerialNumberActivity.class, 
			RtcActivity.class, TF_Activity.class, 
			//ScreenSleepActivity.class, 
			/*DwnLoadActivity.class,*/ PrinterActivity.class, 
			ResultTableActivity.class, BurnConfigActivity.class
			};
	public static void startNextActivity(Context context){
		Intent intent;
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
}
