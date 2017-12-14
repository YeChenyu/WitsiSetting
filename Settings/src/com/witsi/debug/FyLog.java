package com.witsi.debug;

import android.annotation.SuppressLint;
import android.util.Log;

public class FyLog {

	private static String TAG = FyLog.class.getSimpleName();
	private static final boolean LOG = false;
	private static final boolean D = true;
	private static final boolean V = true;
	private static final boolean E = true;
	private static final boolean W = true;
	private static final boolean I = true;
	
	public static void v(String tag, String msg){
		if(LOG && V){
			Log.v(tag, msg);
		}
	}
	public static void v(String tag, String msg, Throwable tr){
		if(LOG && V){
			Log.v(tag, msg, tr);
		}
	}
	public static void d(String tag, String msg){
		if(LOG && D){
			Log.d(tag, msg);
		}
	}
	public static void d(String tag, String msg, Throwable tr){
		if(LOG && D){
			Log.d(tag, msg, tr);
		}
	}
	public static void i(String tag, String msg){
		if(LOG && I){
			Log.i(tag, msg);
		}
	}
	public static void i(String tag, String msg, Throwable tr){
		if(LOG && I){
			Log.i(tag, msg, tr);
		}
	}
	public static void e(String tag, String msg){
		if(LOG && E){
			Log.e(tag, msg);
		}
	}
	public static void e(String tag, String msg, Throwable tr){
		if(LOG && E){
			Log.e(tag, msg, tr);
		}
	}
	public static void w(String tag, String msg){
		if(LOG && W){
			Log.w(tag, msg);
		}
	}
	public static void w(String tag, String msg, Throwable tr){
		if(LOG && W){
			Log.w(tag, msg, tr);
		}
	}
	public static void d(String tag, String[] msg){
		if(LOG && W){
			for (String string : msg) {
				Log.d(tag, string);
			}
		}
	}
	
	@SuppressLint("NewApi")
	public static void debug(){
		Log.v(TAG, "the version1: " + android.os.Build.BOARD);
		Log.v(TAG, "the version2: " + android.os.Build.BOOTLOADER);
		Log.v(TAG, "the version3: " + android.os.Build.BRAND);
		Log.v(TAG, "the version4: " + android.os.Build.CPU_ABI);
		Log.v(TAG, "the version5: " + android.os.Build.CPU_ABI2);
		Log.v(TAG, "the version6: " + android.os.Build.DEVICE);
		Log.v(TAG, "the version7: " + android.os.Build.DISPLAY);
		Log.v(TAG, "the version8: " + android.os.Build.FINGERPRINT);
		Log.v(TAG, "the version9: " + android.os.Build.HARDWARE);
		Log.v(TAG, "the version10: " + android.os.Build.HOST);
		Log.v(TAG, "the version11: " + android.os.Build.ID);
		Log.v(TAG, "the version12:" + android.os.Build.USER);
		Log.v(TAG, "the version13: " + android.os.Build.MANUFACTURER);
		Log.v(TAG, "the version14: " + android.os.Build.MODEL);
		Log.v(TAG, "the version15: " + android.os.Build.PRODUCT);
		Log.v(TAG, "the version16: " + android.os.Build.RADIO);
		Log.v(TAG, "the version17: " + android.os.Build.SERIAL);
		Log.v(TAG, "the version18: " + android.os.Build.TAGS);
		Log.v(TAG, "the version19: " + android.os.Build.TIME);
		Log.v(TAG, "the version20: " + android.os.Build.TYPE);
		Log.v(TAG, "the version21: " + android.os.Build.UNKNOWN);
		Log.v(TAG, "the version22: " + android.os.Build.VERSION.CODENAME);
		Log.v(TAG, "the version23: " + android.os.Build.VERSION.INCREMENTAL);
		Log.v(TAG, "the version24: " + android.os.Build.VERSION.RELEASE);
		Log.v(TAG, "the version25: " + android.os.Build.VERSION_CODES.BASE);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.BASE_1_1);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.CUPCAKE);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.CUR_DEVELOPMENT);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.DONUT);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.ECLAIR);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.ECLAIR_0_1);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.ECLAIR_MR1);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.FROYO);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.GINGERBREAD);
		Log.v(TAG, "the version26: " + android.os.Build.VERSION_CODES.GINGERBREAD_MR1);
	}
}
