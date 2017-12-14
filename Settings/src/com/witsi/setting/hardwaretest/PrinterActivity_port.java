package com.witsi.setting.hardwaretest;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.witsi.arq.ArqMisc;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.witsi.arq.ArqConverts;
//import android.witsi.misc.Printer;

public class PrinterActivity_port extends Activity implements
		android.view.View.OnClickListener {
	private final String TAG = "PrinterActivity";
	private static final boolean D = true;
	private Context context = PrinterActivity_port.this;
	
	private TextView prn_status, prn_content;
	private Button button_Printer_return;
	private Button button_Printer_ok;
	private Button button_Printer_false;
	private Button button_Printer_test;
	ImageView printer_image;
	View main = null;
	
	private MyRunnable myrun = new MyRunnable();
	private MyHandler mHandler = new MyHandler(this);
	private ArqMisc miscArq;
	
	byte[] printerpicture = null;
	String burn_last_data;
	String printerData;
	private String msg = null;
	private boolean printThreadRunning = false;
	public int flag_printer = -1;
	boolean screen_sleep = false;
	boolean isburning = false;
	int year, month, date, hour, minute, second, burntime;
	boolean ifCloseActivity = true;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

	private SharedPreferences config;
	private Editor editor;

	// 用于2个进程同步,返回之前状态值:
	// true --- 已经有线程在运行了,等待;
	// false --- 没有线程运行,当前可以运行
	private synchronized boolean threadTestAndSet() {
		if (printThreadRunning) {
			return true;
		} else {
			printThreadRunning = true;
			return false;
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		ifCloseActivity = true;
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		if (isburning == true) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_printer_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_printer_activity);
		}
		registerReceiver(mReceiver,filterHome);
		// SysApplication.getInstance().addActivity(this);
		
		prn_status = (TextView) findViewById(R.id.prnStatus);
		prn_content = (TextView) findViewById(R.id.prnContent);
		// display the content
		miscArq = new ArqMisc(this);
		button_Printer_return = (Button) this
				.findViewById(R.id.getbacktomain);
		button_Printer_ok = (Button) this.findViewById(R.id.pass);
		button_Printer_false = (Button) this.findViewById(R.id.fail);
		button_Printer_test = (Button) this.findViewById(R.id.test);
		button_Printer_return.setOnClickListener(PrinterActivity_port.this);
		button_Printer_ok.setOnClickListener(PrinterActivity_port.this);
		button_Printer_false.setOnClickListener(PrinterActivity_port.this);
		button_Printer_test.setOnClickListener(PrinterActivity_port.this);

		// // 给拷机做选择项
		// mybundle = new Bundle();
		// intent = getIntent();
		// mybundle = (Bundle) intent.getExtras();
		// if ((Boolean) intent.getSerializableExtra("flag_burn") == true)
		// isburning = true;
		// else
		// isburning = false;

		printer_image = (ImageView) this.findViewById(R.id.printer_image);
		if (config.getBoolean("light", true) == false) {
			printer_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		burn_last_data = "";
		burn_last_data = burn_last_data + "\n\r";
		printerData = "";
		printerData = printerData + "\n\r";
		if (config.getString("camera", "").equals("ng")) {
			burn_last_data = burn_last_data + "照相机测试：一一一N" + "\n\r";
			printerData = printerData + "照相机测试：一一一N" + "\n\r";
		} else if (config.getString("camera", "").equals("ok")){
			burn_last_data = burn_last_data + "照相机测试：一一一Y" + "\n\r";
			printerData = printerData + "照相机测试：一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "照相机测试：一一一无" + "\n\r";
			printerData = printerData + "照相机测试：一一一无" + "\n\r";
		}
		/*---------------*/
		if (config.getString("touchscreen", "").equals("ng")) {
			burn_last_data = burn_last_data + "触摸屏测试：一一一N" + "\n\r";
			printerData = printerData + "触摸屏测试：一一一N" + "\n\r";
		} else if (config.getString("touchscreen", "").equals("ok")){
			burn_last_data = burn_last_data + "触摸屏测试：一一一Y" + "\n\r";
			printerData = printerData + "触摸屏测试：一一一Y" + "\n\r";
		}else{
			burn_last_data = burn_last_data + "触摸屏测试：一一一无" + "\n\r";
			printerData = printerData + "触摸屏测试：一一一无" + "\n\r";
		}
		/*---------------*/
		if (config.getString("screen", "").equals("ng")) {
			burn_last_data = burn_last_data + "坏点测试：一一一一N" + "\n\r";
			printerData = printerData + "坏点测试：一一一一N" + "\n\r";
		} else if (config.getString("screen", "").equals("ok")) {
			burn_last_data = burn_last_data + "坏点测试：一一一一Y" + "\n\r";
			printerData = printerData + "坏点测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "坏点测试：一一一一无" + "\n\r";
			printerData = printerData + "坏点测试：一一一一无" + "\n\r";
		}
		/*---------------*/
		if (config.getString("magc", "").equals("ng"))  {
			burn_last_data = burn_last_data + "磁卡测试：一一一一N" + "\n\r";
			printerData = printerData + "磁卡测试：一一一一N" + "\n\r";
		} else  if (config.getString("magc", "").equals("ok")){
			burn_last_data = burn_last_data + "磁卡测试：一一一一Y" + "\n\r";
			printerData = printerData + "磁卡测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "磁卡测试：一一一一无" + "\n\r";
			printerData = printerData + "磁卡测试：一一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("buzzer", "").equals("ng")) {
			burn_last_data = burn_last_data + "蜂鸣器测试：一一一N" + "\n\r";
			printerData = printerData + "蜂鸣器测试：一一一N" + "\n\r";
		} else if (config.getString("buzzer", "").equals("ok")){
			burn_last_data = burn_last_data + "蜂鸣器测试：一一一Y" + "\n\r";
			printerData = printerData + "蜂鸣器测试：一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "蜂鸣器测试：一一一无" + "\n\r";
			printerData = printerData + "蜂鸣器测试：一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("security", "").equals("ng")) {
			burn_last_data = burn_last_data + "安全状态测试：一一N" + "\n\r";
			printerData = printerData + "安全状态测试：一一N" + "\n\r";
		} else if (config.getString("security", "").equals("ok")){
			burn_last_data = burn_last_data + "安全状态测试：一一Y" + "\n\r";
			printerData = printerData + "安全状态测试：一一Y" + "\n\r";
		}else{
			burn_last_data = burn_last_data + "安全状态测试：一一无" + "\n\r";
			printerData = printerData + "安全状态测试：一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("key", "").equals("ng")) {
			burn_last_data = burn_last_data + "按键测试：一一一一N" + "\n\r";
			printerData = printerData + "按键测试：一一一一N" + "\n\r";
		} else if (config.getString("key", "").equals("ok")){
			burn_last_data = burn_last_data + "按键测试：一一一一Y" + "\n\r";
			printerData = printerData + "按键测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "按键测试：一一一一无" + "\n\r";
			printerData = printerData + "按键测试：一一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("ic", "").equals("ng")) {
			burn_last_data = burn_last_data + "IC卡测试：一一一一N" + "\n\r";
			printerData = printerData + "IC卡测试：一一一一N" + "\n\r";
		} else if (config.getString("ic", "").equals("ok")){
			burn_last_data = burn_last_data + "IC卡测试：一一一一Y" + "\n\r";
			printerData = printerData + "IC卡测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "IC卡测试：一一一一无" + "\n\r";
			printerData = printerData + "IC卡测试：一一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("led", "").equals("ng")) {
			burn_last_data = burn_last_data + "LED测试： 一一一一N" + "\n\r";
			printerData = printerData + "LED测试： 一一一一N" + "\n\r";
		} else if (config.getString("led", "").equals("ok")){
			burn_last_data = burn_last_data + "LED测试： 一一一一Y" + "\n\r";
			printerData = printerData + "LED测试： 一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "LED测试： 一一一一无" + "\n\r";
			printerData = printerData + "LED测试： 一一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("version", "").equals("ng")) {
			burn_last_data = burn_last_data + "版本号测试：一一一N" + "\n\r";
			printerData = printerData + "版本号测试：一一一N" + "\n\r";
		} else if (config.getString("version", "").equals("ok")) {
			burn_last_data = burn_last_data + "版本号测试：一一一Y" + "\n\r";
			printerData = printerData + "版本号测试：一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "版本号测试：一一一无" + "\n\r";
			printerData = printerData + "版本号测试：一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("wifi", "").equals("ng")) {
			burn_last_data = burn_last_data + "Wifi测试： 一一一一N" + "\n\r";
			printerData = printerData + "Wifi测试：一一一一N" + "\n\r";

		} else if (config.getString("wifi", "").equals("ok")) {
			burn_last_data = burn_last_data + "Wifi测试： 一一一一Y" + "\n\r";
			printerData = printerData + "Wifi测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "Wifi测试： 一一一一无" + "\n\r";
			printerData = printerData + "Wifi测试：一一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("gprs", "").equals("ng"))  {
			burn_last_data = burn_last_data + "GPRS测试：  一一一N" + "\n\r";
			printerData = printerData + "GPRS测试：一一一一N" + "\n\r";
		} else if (config.getString("gprs", "").equals("ok"))  {
			burn_last_data = burn_last_data + "GPRS测试：  一一一Y" + "\n\r";
			printerData = printerData + "GPRS测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "GPRS测试：  一一一无" + "\n\r";
			printerData = printerData + "GPRS测试：一一一一无" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("tf", "").equals("ng"))  {
			burn_last_data = burn_last_data + "T F卡测试：   一一一N" + "\n\r";
			printerData = printerData + "TF卡测试：一一一一N" + "\n\r";
		} else if (config.getString("tf", "").equals("ok"))  {
			burn_last_data = burn_last_data + "T F卡测试：   一一一Y" + "\n\r";
			printerData = printerData + "TF卡测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "T F卡测试：   一一一无" + "\n\r";
			printerData = printerData + "TF卡测试：一一一一无" + "\n\r";
		}
		/*-----------------------*/

//		burn_last_data = burn_last_data + "设备序列号：" + getsysteminfo(0x02)
//				+ "\n\r";
//		burn_last_data = burn_last_data + "硬件版本号：" + getsysteminfo(0x03)
//				+ "\n\r";
//		burn_last_data = burn_last_data + "软件版本号：" + getsysteminfo(0x01)
//				+ "\n\r";
//		printerData = printerData + "设备序列号：" + getsysteminfo(0x02)
//				+ "\n\r";
//		printerData = printerData + "硬件版本号：" + getsysteminfo(0x03)
//				+ "\n\r";
//		printerData = printerData + "软件版本号：" + getsysteminfo(0x01)
//				+ "\n\r";
		
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		burn_last_data = burn_last_data + "IMEI:" + tm.getDeviceId() + "\n\r";
		printerData = printerData + "IMEI:" + tm.getDeviceId() + "\n\r";
		Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
		t.setToNow(); // 取得系统时间。
		year = t.year;
		month = t.month + 1;
		date = t.monthDay;
		hour = t.hour;
		minute = t.minute;
		second = t.second;
		burn_last_data = burn_last_data + "时间：" + year + "年" + month + "月"
				+ date + "日" + hour + "时" + minute + "分" + "\n\r";
		printerData = printerData + "时间：" + year + "年" + month + "月"
				+ date + "日" + hour + "时" + minute + "分" + "\n\r";
		/*-----------------------*/

		/*-----------------------*/
		burn_last_data = burn_last_data 
				+ "*******测试结束********"+ "\n\r" + "\n\r";
		printerData= printerData 
				+ "*******测试结束********"+ "\n\r" + "\n\r";
		msg = printerData;
		prn_content.setText(burn_last_data);
		/*-----------------------*/

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isburning == true) {
					Intent intent = new Intent(PrinterActivity_port.this,
							ResultTableActivity.class);
					editor.putString("printer", "ok");
					editor.commit();
					startActivity(intent);
				}
			}
		}, 2000);
	}

	@Override
	public void onClick(View PrinterClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			printer_image.setBackgroundResource(R.drawable.lucency);
			screen_sleep = false;
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			switch (PrinterClick.getId()) {
			case R.id.getbacktomain: {
				ActivityManager.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManager.trunToSingleTestActivity(PrinterActivity_port.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManager.trunToEntryActivity(PrinterActivity_port.this);
				}else {
					ActivityManager.trunToBurnStartActivity(PrinterActivity_port.this);
				}   
				break;
			}
			case R.id.pass: {
				flag_printer = 1;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("printer", "ok");
					ActivityManager.trunToSingleTestActivity(PrinterActivity_port.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("printer", "ok");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(PrinterActivity_port.this);
				}else {
					ActivityManager.trunToBurnStartActivity(PrinterActivity_port.this);
				} 
				editor.commit();
				break;
			}
			case R.id.fail: {
				flag_printer = 0;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("printer", "ng");
					ActivityManager.trunToSingleTestActivity(PrinterActivity_port.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("printer", "ng");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(PrinterActivity_port.this);
				}else {
					ActivityManager.trunToBurnStartActivity(PrinterActivity_port.this);
				} 
				editor.commit();
				break;
			}
			case R.id.test: {
			}
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(D)FyLog.i(TAG, "进入Stop状态");
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
		if (ifCloseActivity == true) {
			FyLog.i(TAG, "尝试进行finish");
			this.finish();
			FyLog.i(TAG, "已执行finish");
		}
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
	}

	// drawable=>Bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	// Bitmap=>byte[]
	private byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// byte[]=>Bitmap
	private Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	public void startPrintBtnOnClickHandler(View v) {
		/* 是否前一次打印还未结束 */
		FyLog.i("Printer", "按键按下");
		if (printThreadRunning == true) {
			FyLog.i(TAG, "printer is busy. just return.");
			return;
		}
		FyLog.i("Printer", "调用线程");
		Thread thread = new Thread(myrun);
		FyLog.i("Printer", "启动线程");
		thread.start();
		FyLog.i("Printer", "启动线程Y");
	}

	class MyRunnable implements Runnable {
		public void run() {
			int ret;
			FyLog.i("Printer", "进入Runnable");
			// Wait printer to ready
//			while (threadTestAndSet()) {
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					Log.d(TAG, "Waiting didnt work!!");
//					e.printStackTrace();
//				}
//			}
//			/* init printer */
//			ret = prn.init();
//			if (ret != 0) { /* success. */
//				Log.e(TAG, "打印机初始化N!!!");
//			}
//			byte[] printerpicture = new byte[12288];
//			for (int i = 0; i < 12288; i++) {
//				printerpicture[i] = (byte) 0xff;
//			}
//			FyLog.i(TAG, HexCodec.hexEncode(printerpicture));
//			ret = prn.printImage(384, 32, 0, printerpicture);
//			ret = prn.printString(msg);
//			FyLog.i(TAG, "" + ret);
//			if (ret != 0) {
//				Log.e(TAG, "打印字符串N!!!");
//				sendMessageToMain("打印字符串N!!!");
//				printThreadRunning = false;
//				return;
//			}
//			// }
//
//			/* print the string */
//			ret = prn.start();
//			FyLog.i(TAG, "start_ret:" + ret);
//			if (ret != 0) {
//				Log.e(TAG, "打印机开启N!!!");
//				printThreadRunning = false;
//			}
//			int status ;
//			while (true) {
//				/* wait 500ms */
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					Log.d(TAG, "Waiting didnt work!!");
//					e.printStackTrace();
//				}
//
//				status = prn.getStatus();
//				if(ret < 0){
//					Log.e(TAG, "获取打印机状态失败！");
//				}
//				if (status == 0) {
//					Log.e(TAG, "打印Y,结束打印。");
//					sendMessageToMain("打印成功,结束打印。");
//					break;
//				}
//				if (prn.noPaper()) {
//					Log.e(TAG, "打印机缺纸,结束打印。");
//					sendMessageToMain("打印机缺纸,结束打印。");
//					break;
//				}
//				if (prn.overHeat()) {
//					Log.e(TAG, "打印机过热,结束打印。");
//					sendMessageToMain("打印机过热,结束打印。");
//					break;
//				}
//				if (prn.isBusy()) {
//					Log.e(TAG, "打印机打印中。。。");
//				}
//			}

			/* close printer */
//			prn.exit();
//			printThreadRunning = false;
		}

		public void sendMessageToMain(String statusMsg) {
			Message msg = new Message();
			msg.what = 1;

			Bundle bundle = new Bundle();
			bundle.putString("status", statusMsg);

			/* send message */
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
	}

	class MyHandler extends Handler {
		// WeakReference to the outer class's instance
		private WeakReference<PrinterActivity_port> mOuter;

		public MyHandler(PrinterActivity_port activity) {
			mOuter = new WeakReference<PrinterActivity_port>(activity);
		}

		public void handleMessage(Message msg) {
			PrinterActivity_port outer = mOuter.get();
			Toast.makeText(PrinterActivity_port.this, msg.getData().getString("status"),
					Toast.LENGTH_SHORT).show();
			if (outer != null) {
				if (msg.what != 1) {
					FyLog.e(TAG, "Unknown message received.");
					return;
				}
				outer.prn_status.setText(msg.getData().getString("status"));
			}
		}
	}

	public String getsysteminfo(int need) {
		miscArq = new ArqMisc(this);
		int ret;
		byte[] serialnumber = new byte[32];
		ret = miscArq.getSystemInfo(need, serialnumber);
		if (ret <= 0) {
			FyLog.e(TAG, "get security status failed. ret = " + ret);
			return null;
		} else {
			byte[] tmp_buf = new byte[ret];
			for (int i = 0; i < ret; i++)
				tmp_buf[i] = serialnumber[i];
			FyLog.i(TAG, ArqConverts.asciiBytesToString(tmp_buf) + ret);

			String sysinfo = ArqConverts.asciiBytesToString(tmp_buf);
			serialnumber = null;
			tmp_buf = null;
			return (sysinfo);
		}
	}

	/************************** 事件监听申明区 ***************************/
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
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				printer_image.setBackgroundResource(R.drawable.lucency);
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