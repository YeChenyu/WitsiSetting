package com.witsi.setting.hardwaretest;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.setting.hardwaretest.bluetooth.DeviceScanActivityII;
import com.witsi.setting.hardwaretest.bluetooth.FxService;
import com.witsi.setting.hardwaretest.bluetooth.FxService.ServiceBinder;
import com.witsi.setting.hardwaretest.bluetooth.FxServiceOnClink;
import com.witsi.tools.ConfigSharePaference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.witsi.bluetooth.WtBluetoothDevice;
import com.witsi.bluetooth.WtBtListener.ConnectListerner;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.witsi.arqII.ArqMisc;
import android.witsi.arqII.ArqService;
import android.witsi.arqII.DataTransmit;
import android.witsi.arqII.WtPrint;
import android.witsi.arqII.WtPrintStringSet;
import android.witsi.arqII.onPrintListener;

public class PrinterActivity_BT extends Activity implements
		android.view.View.OnClickListener {
	
	private final String TAG = "PrinterActivity";
	private Context context = PrinterActivity_BT.this;
	private static final boolean D = true;
	
	private TextView prn_status, prn_content;
	private Button button_Printer_return;
	private Button button_Printer_ok;
	private Button button_Printer_false;
	private Button button_Printer_test;
	View main = null;
	ImageView printer_image;
	
	private ArqMisc miscArq;
	
	byte[] printerpicture = null;
	String burn_last_data;
	String printerData;
	private boolean printThreadRunning = false;
	private String msg = null;
	public int flag_printer = -1;
	private boolean isPrinterOk = false;
	boolean screen_sleep = false;
	boolean isburning = false;
	int year, month, date, hour, minute, second, burntime;
	boolean ifCloseActivity = false;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

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
		button_Printer_return = (Button) this
				.findViewById(R.id.getbacktomain);
		button_Printer_ok = (Button) this.findViewById(R.id.pass);
		button_Printer_false = (Button) this.findViewById(R.id.fail);
		button_Printer_test = (Button) this.findViewById(R.id.test);
		button_Printer_return.setOnClickListener(PrinterActivity_BT.this);
		button_Printer_ok.setOnClickListener(PrinterActivity_BT.this);
		button_Printer_false.setOnClickListener(PrinterActivity_BT.this);
		button_Printer_test.setOnClickListener(PrinterActivity_BT.this);

		initBluetooth();
		
		printer_image = (ImageView) this.findViewById(R.id.printer_image);
		if (config.getBoolean("light", true) == false) {
			printer_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		//打印数据
		burn_last_data = "";
		burn_last_data = burn_last_data + "";
		printerData = "";
		printerData = printerData + "";
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
		if (config.getString("serialnumber", "").equals("ng")) {
			burn_last_data = burn_last_data + "序列号测试：一一一N" + "\n\r";
			printerData = printerData + "序列号测试：一一一N" + "\n\r";
		} else if (config.getString("serialnumber", "").equals("ok")) {
			burn_last_data = burn_last_data + "序列号测试：一一一Y" + "\n\r";
			printerData = printerData + "序列号测试：一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "序列号测试：一一一无" + "\n\r";
			printerData = printerData + "序列号测试：一一一无" + "\n\r";
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
		if (config.getString("rtc", "").equals("ng"))  {
			burn_last_data = burn_last_data + "RTC测试：   一一一N" + "\n\r";
			printerData = printerData + "RTC测试：一一一一N" + "\n\r";
		} else if (config.getString("rtc", "").equals("ok"))  {
			burn_last_data = burn_last_data + "RTC测试：   一一一Y" + "\n\r";
			printerData = printerData + "RTC测试：一一一一Y" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "RTC测试：   一一一无" + "\n\r";
			printerData = printerData + "RTC测试：一一一一无" + "\n\r";
		}
		/*-----------------------*/
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
				+ "*******测试结束********"+ "\n\r" + "\n";
		printerData= printerData 
				+ "*******测试结束********"+ "\n\r" + 
				"\n\r   \n\r   \n\r   \n\r   \n\r" +
				"\n\r   \n\r   \n\r   \n\r   \n\r";
		msg = printerData;
		prn_content.setText(burn_last_data);
		/*-----------------------*/
		if(isburning)
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if(isPrinterOk)
						editor.putString("printer", "ok");
					else
						editor.putString("printer", "ng");
					editor.commit();
					Intent intent = new Intent(PrinterActivity_BT.this,
							ResultTableActivity.class);
					startActivity(intent);
				}
			}, 10000);
	}

	private ServiceBinder serviceBinder;
	private static final int REQUEST_BLE_SCAN = 2;
	ServiceConnection serviceConnection;
	private WtBluetoothDevice mWtBluetoothDevice;
	
	private BluetoothDevice mBluetoothDevice;
	ArqService mBluetoothArqService = null;
	DataTransmit dataTransmit;
	private boolean isScanBtEnable = true;
	//蓝牙设备初始化，蓝牙浮动状态显示窗口初始化并绑定服务
	private void initBluetooth()
	{
		DeviceScanActivityII.isForceBtClassic = true;
		mWtBluetoothDevice = new WtBluetoothDevice( this, DeviceScanActivityII.isForceBtClassic);
		dataTransmit = mWtBluetoothDevice.getDataTransmit();
		
		mBluetoothArqService = new ArqService(dataTransmit,null);

		this.bindService(new Intent(PrinterActivity_BT.this, FxService.class),
				serviceConnection = new ServiceConnection() {
			
	        public void onServiceConnected(ComponentName name, IBinder service) {
	
				 Toast.makeText(getApplicationContext(),"serviceConnection",
	                        Toast.LENGTH_SHORT).show();
	        	serviceBinder = (ServiceBinder)service;
//	        	mFloatView = (Button)serviceBinder.getView();
	        	serviceBinder.addOnClink(new FxServiceOnClink()
	        	{
	        		@Override
	        		//蓝牙选择列表
	        		public int OnClink(View arg0) {
	        			// TODO Auto-generated method stub
	        			if(isScanBtEnable)
	        			{
	        				isScanBtEnable = false;
		        	 		Intent intent = new Intent( PrinterActivity_BT.this, DeviceScanActivityII.class);
		        	 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	 		startActivityForResult( intent, REQUEST_BLE_SCAN);	
	        			}
	        			return 0;
	        		}
	        	});
	        }
	        public void onServiceDisconnected(ComponentName name) {
	        	serviceBinder = null;
//	        	mFloatView = null;
	        }
	
		}, BIND_AUTO_CREATE);
		
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		FyLog.d(TAG, "OnActivityResult requestCode = " + requestCode);	 
			Toast.makeText( PrinterActivity_BT.this,"OnActivityResult requestCode = " + requestCode,
									Toast.LENGTH_SHORT).show();   		
		switch(requestCode)
		{

	       case REQUEST_BLE_SCAN:
				isScanBtEnable = true;
	    	   //蓝牙请求连接
	    	   if(resultCode==Activity.RESULT_OK)
	    	   {
	    		   mBluetoothDevice = ((BluetoothDevice)data.getParcelableExtra("BLUEDEVICE"));
	    		   
	    		   mWtBluetoothDevice.conectionDevice( mBluetoothDevice, new ConnectListerner(){

	    				@Override
	    				public void onStartConnect() {
	    					// TODO Auto-generated method stub
	    			    	 broadcastUpdate(FxService.ACTION_GATT_CONNECTED, "正在连接");

	    				}

	    				@Override
	    				public void onDeviceConnected() {
	    					// TODO Auto-generated method stub
	    		       	 	broadcastUpdate(FxService.ACTION_GATT_CONNECTED, "已连接");


	    				}

	    				@Override
	    				public void onDeviceConnectedFail() {
	    					// TODO Auto-generated method stub
	    			    	 broadcastUpdate(FxService.ACTION_GATT_CONNECTED, "未连接");

	    				}
	    			   
	    		   });

	    	   }
	    	   break;
		}

	}
	//浮动窗口状态显示更新
	private void broadcastUpdate(final String action,final String title) {
        final Intent intent = new Intent(action);
        intent.putExtra(FxService.CONNECTED_STATE, title);
        this.sendBroadcast(intent);
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
				ifCloseActivity = true;
				ActivityManager.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManager.trunToSingleTestActivity(PrinterActivity_BT.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManager.trunToEntryActivity(PrinterActivity_BT.this);
				} else {
					ActivityManager.trunToBurnStartActivity(PrinterActivity_BT.this);
				}  
				break;
			}
			case R.id.pass: {
				flag_printer = 1;
				ifCloseActivity = true;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("printer", "ok");
					editor.commit();
					ActivityManager.trunToSingleTestActivity(PrinterActivity_BT.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("printer", "ok");
					editor.commit();
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(PrinterActivity_BT.this);
					PrinterActivity_BT.this.finish();
				}else {
					ActivityManager.trunToBurnStartActivity(PrinterActivity_BT.this);
				}  
				break;
			}
			case R.id.fail: {
				flag_printer = 0;
				ifCloseActivity = true;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("printer", "ng");
					editor.commit();
					ActivityManager.trunToSingleTestActivity(PrinterActivity_BT.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("printer", "ng");
					editor.commit();
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(PrinterActivity_BT.this);
					PrinterActivity_BT.this.finish();
				}else {
					ActivityManager.trunToBurnStartActivity(PrinterActivity_BT.this);
				}  
				break;
			}
			case R.id.test: {
				print(mBluetoothArqService);
			}
			default:
				break;
			}
		}
	}

	public void startPrintBtnOnClickHandler(View v) {
		/* 是否前一次打印还未结束 */
		print(mBluetoothArqService);
	}
	
	ArqMisc arqMisc;
	public boolean isLink(ArqService mArqService)
	{
		
		
		arqMisc = new ArqMisc(PrinterActivity_BT.this, mArqService);
		
		if(arqMisc!=null)
		{
			
			int ret = arqMisc.echoWithOutPermission(500);
			FyLog.d(TAG, "ret =" + ret);	 			
			if(ret==0)
			{
				return true;
			}

		}
		Toast.makeText( PrinterActivity_BT.this, "未连接,打印失败",
                Toast.LENGTH_SHORT).show(); 
		return false;
	}
	
	WtPrint mWtPrint;
	private void print(ArqService mArqService)
	{
		if(!isLink(mArqService))
			return;
		

		mWtPrint = new WtPrint(this, mArqService);
		
		
//		String msg = "\n\n\n<LOGO>#XX特约商户签购单\n\n商户名: XX质保部测试商户12345123456aaabbb12345\n商户号: 818331053980069\n终端号: 60157853\n操作员号: fuiou\n卡号: 622500******0220\n交易类型: 消费\n凭证号(VOUCHER NO.): 107636\n交易时间: 2015-01-05 14:43:40\n交易参考号(REFNO.): 051443831619\n交易金额: 0.66元\n备注: (交易成功)\nARQC:3FC099F13F08B069\nTVR:0000048010\nAID:A000000333010102\nTSI:FC00\nATC:0F3C\nAPP LABEL:PBOC CREDIT\nAPPLICATION:PBOC CREDIT\n\n\n本人确认以上交易,同意将其记入本卡账户.如有需要,请致电400-6677-333,www.fuiou.com\n\n..........客户存根..........\n\n\n\n\n\n\n\n\n\n";

		Toast.makeText( PrinterActivity_BT.this, "开始打印",
                Toast.LENGTH_SHORT).show();   	
		List<WtPrintStringSet> list = new ArrayList();
		WtPrintStringSet tmp1 = new WtPrintStringSet();
		tmp1.setData(msg);
		list.add(tmp1);
		mWtPrint.printString(60, list, new onPrintListener(){

			@Override
			public void onComplete(PRINT_STATE arg0, int arg1) {
				// TODO Auto-generated method stub
				FyLog.v(TAG, "the status is: " + arg0);
				if(arg0 == PRINT_STATE.STATE_GET_STATUS_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "获取状态失败",
			                Toast.LENGTH_SHORT).show();   
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_PRINT_SUCC)
				{
					Toast.makeText( PrinterActivity_BT.this, "打印成功",
			                Toast.LENGTH_SHORT).show();  
					isPrinterOk = true;
				}else if(arg0 == PRINT_STATE.STATE_NO_PAPER)
				{
					Toast.makeText( PrinterActivity_BT.this, "缺纸",
			                Toast.LENGTH_SHORT).show();  
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_OVER_HEAT)
				{
					Toast.makeText( PrinterActivity_BT.this, "过热",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_VOLTAGE_ANOMALY)
				{
					Toast.makeText( PrinterActivity_BT.this, "电压异常",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_GREY_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "设置灰度错误",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_WRITE_IMAGE_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "打印图片错误",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_FONT_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "设置字体错误",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_UNDERLINE_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "设置下划线错误",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_SPACING_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "设置边距错误",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_WRITE_STRING_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "设置打印数据失败",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}
			}
			
		} );
			

	}
	@Override
	protected void onStop() {
		super.onStop();
		FyLog.i(TAG, "进入Stop状态");
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
		if (ifCloseActivity == true) {
			FyLog.i(TAG, "尝试进行finish");
			this.finish();
			FyLog.i(TAG, "已执行finish");
		}
	}
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		ifCloseActivity = false;
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		ActivityManager.clearActivity();
		FyLog.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
		if(mWtBluetoothDevice != null)
		{
			mWtBluetoothDevice.close();
		}
		if(serviceConnection!=null)this.unbindService(serviceConnection);
		
		if(mBluetoothArqService != null)
		{
			mBluetoothArqService.OnFinishService();
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
            	ActivityManager.clearActivity();
            	PrinterActivity_BT.this.finish();  
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