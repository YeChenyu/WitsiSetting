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
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		// ��������ѡ����
		isburning = config.getBoolean("flag_burn", false);
		if (isburning == true) {
			getLayoutInflater();
			// ��װ���ء����ð�~
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
		//��ӡ����
		burn_last_data = "";
		burn_last_data = burn_last_data + "";
		printerData = "";
		printerData = printerData + "";
		if (config.getString("camera", "").equals("ng")) {
			burn_last_data = burn_last_data + "��������ԣ�һһһN" + "\n\r";
			printerData = printerData + "��������ԣ�һһһN" + "\n\r";
		} else if (config.getString("camera", "").equals("ok")){
			burn_last_data = burn_last_data + "��������ԣ�һһһY" + "\n\r";
			printerData = printerData + "��������ԣ�һһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "��������ԣ�һһһ��" + "\n\r";
			printerData = printerData + "��������ԣ�һһһ��" + "\n\r";
		}
		/*---------------*/
		if (config.getString("touchscreen", "").equals("ng")) {
			burn_last_data = burn_last_data + "���������ԣ�һһһN" + "\n\r";
			printerData = printerData + "���������ԣ�һһһN" + "\n\r";
		} else if (config.getString("touchscreen", "").equals("ok")){
			burn_last_data = burn_last_data + "���������ԣ�һһһY" + "\n\r";
			printerData = printerData + "���������ԣ�һһһY" + "\n\r";
		}else{
			burn_last_data = burn_last_data + "���������ԣ�һһһ��" + "\n\r";
			printerData = printerData + "���������ԣ�һһһ��" + "\n\r";
		}
		/*---------------*/
		if (config.getString("screen", "").equals("ng")) {
			burn_last_data = burn_last_data + "������ԣ�һһһһN" + "\n\r";
			printerData = printerData + "������ԣ�һһһһN" + "\n\r";
		} else if (config.getString("screen", "").equals("ok")) {
			burn_last_data = burn_last_data + "������ԣ�һһһһY" + "\n\r";
			printerData = printerData + "������ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "������ԣ�һһһһ��" + "\n\r";
			printerData = printerData + "������ԣ�һһһһ��" + "\n\r";
		}
		/*---------------*/
		if (config.getString("magc", "").equals("ng"))  {
			burn_last_data = burn_last_data + "�ſ����ԣ�һһһһN" + "\n\r";
			printerData = printerData + "�ſ����ԣ�һһһһN" + "\n\r";
		} else  if (config.getString("magc", "").equals("ok")){
			burn_last_data = burn_last_data + "�ſ����ԣ�һһһһY" + "\n\r";
			printerData = printerData + "�ſ����ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "�ſ����ԣ�һһһһ��" + "\n\r";
			printerData = printerData + "�ſ����ԣ�һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("buzzer", "").equals("ng")) {
			burn_last_data = burn_last_data + "���������ԣ�һһһN" + "\n\r";
			printerData = printerData + "���������ԣ�һһһN" + "\n\r";
		} else if (config.getString("buzzer", "").equals("ok")){
			burn_last_data = burn_last_data + "���������ԣ�һһһY" + "\n\r";
			printerData = printerData + "���������ԣ�һһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "���������ԣ�һһһ��" + "\n\r";
			printerData = printerData + "���������ԣ�һһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("security", "").equals("ng")) {
			burn_last_data = burn_last_data + "��ȫ״̬���ԣ�һһN" + "\n\r";
			printerData = printerData + "��ȫ״̬���ԣ�һһN" + "\n\r";
		} else if (config.getString("security", "").equals("ok")){
			burn_last_data = burn_last_data + "��ȫ״̬���ԣ�һһY" + "\n\r";
			printerData = printerData + "��ȫ״̬���ԣ�һһY" + "\n\r";
		}else{
			burn_last_data = burn_last_data + "��ȫ״̬���ԣ�һһ��" + "\n\r";
			printerData = printerData + "��ȫ״̬���ԣ�һһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("key", "").equals("ng")) {
			burn_last_data = burn_last_data + "�������ԣ�һһһһN" + "\n\r";
			printerData = printerData + "�������ԣ�һһһһN" + "\n\r";
		} else if (config.getString("key", "").equals("ok")){
			burn_last_data = burn_last_data + "�������ԣ�һһһһY" + "\n\r";
			printerData = printerData + "�������ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "�������ԣ�һһһһ��" + "\n\r";
			printerData = printerData + "�������ԣ�һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("ic", "").equals("ng")) {
			burn_last_data = burn_last_data + "IC�����ԣ�һһһһN" + "\n\r";
			printerData = printerData + "IC�����ԣ�һһһһN" + "\n\r";
		} else if (config.getString("ic", "").equals("ok")){
			burn_last_data = burn_last_data + "IC�����ԣ�һһһһY" + "\n\r";
			printerData = printerData + "IC�����ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "IC�����ԣ�һһһһ��" + "\n\r";
			printerData = printerData + "IC�����ԣ�һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("led", "").equals("ng")) {
			burn_last_data = burn_last_data + "LED���ԣ� һһһһN" + "\n\r";
			printerData = printerData + "LED���ԣ� һһһһN" + "\n\r";
		} else if (config.getString("led", "").equals("ok")){
			burn_last_data = burn_last_data + "LED���ԣ� һһһһY" + "\n\r";
			printerData = printerData + "LED���ԣ� һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "LED���ԣ� һһһһ��" + "\n\r";
			printerData = printerData + "LED���ԣ� һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("version", "").equals("ng")) {
			burn_last_data = burn_last_data + "�汾�Ų��ԣ�һһһN" + "\n\r";
			printerData = printerData + "�汾�Ų��ԣ�һһһN" + "\n\r";
		} else if (config.getString("version", "").equals("ok")) {
			burn_last_data = burn_last_data + "�汾�Ų��ԣ�һһһY" + "\n\r";
			printerData = printerData + "�汾�Ų��ԣ�һһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "�汾�Ų��ԣ�һһһ��" + "\n\r";
			printerData = printerData + "�汾�Ų��ԣ�һһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("serialnumber", "").equals("ng")) {
			burn_last_data = burn_last_data + "���кŲ��ԣ�һһһN" + "\n\r";
			printerData = printerData + "���кŲ��ԣ�һһһN" + "\n\r";
		} else if (config.getString("serialnumber", "").equals("ok")) {
			burn_last_data = burn_last_data + "���кŲ��ԣ�һһһY" + "\n\r";
			printerData = printerData + "���кŲ��ԣ�һһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "���кŲ��ԣ�һһһ��" + "\n\r";
			printerData = printerData + "���кŲ��ԣ�һһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("wifi", "").equals("ng")) {
			burn_last_data = burn_last_data + "Wifi���ԣ� һһһһN" + "\n\r";
			printerData = printerData + "Wifi���ԣ�һһһһN" + "\n\r";

		} else if (config.getString("wifi", "").equals("ok")) {
			burn_last_data = burn_last_data + "Wifi���ԣ� һһһһY" + "\n\r";
			printerData = printerData + "Wifi���ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "Wifi���ԣ� һһһһ��" + "\n\r";
			printerData = printerData + "Wifi���ԣ�һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("gprs", "").equals("ng"))  {
			burn_last_data = burn_last_data + "GPRS���ԣ�  һһһN" + "\n\r";
			printerData = printerData + "GPRS���ԣ�һһһһN" + "\n\r";
		} else if (config.getString("gprs", "").equals("ok"))  {
			burn_last_data = burn_last_data + "GPRS���ԣ�  һһһY" + "\n\r";
			printerData = printerData + "GPRS���ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "GPRS���ԣ�  һһһ��" + "\n\r";
			printerData = printerData + "GPRS���ԣ�һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("tf", "").equals("ng"))  {
			burn_last_data = burn_last_data + "T F�����ԣ�   һһһN" + "\n\r";
			printerData = printerData + "TF�����ԣ�һһһһN" + "\n\r";
		} else if (config.getString("tf", "").equals("ok"))  {
			burn_last_data = burn_last_data + "T F�����ԣ�   һһһY" + "\n\r";
			printerData = printerData + "TF�����ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "T F�����ԣ�   һһһ��" + "\n\r";
			printerData = printerData + "TF�����ԣ�һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("rtc", "").equals("ng"))  {
			burn_last_data = burn_last_data + "RTC���ԣ�   һһһN" + "\n\r";
			printerData = printerData + "RTC���ԣ�һһһһN" + "\n\r";
		} else if (config.getString("rtc", "").equals("ok"))  {
			burn_last_data = burn_last_data + "RTC���ԣ�   һһһY" + "\n\r";
			printerData = printerData + "RTC���ԣ�һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "RTC���ԣ�   һһһ��" + "\n\r";
			printerData = printerData + "RTC���ԣ�һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		burn_last_data = burn_last_data + "IMEI:" + tm.getDeviceId() + "\n\r";
		printerData = printerData + "IMEI:" + tm.getDeviceId() + "\n\r";
		Time t = new Time(); // or Time t=new Time("GMT+8"); ����Time Zone���ϡ�
		t.setToNow(); // ȡ��ϵͳʱ�䡣
		year = t.year;
		month = t.month + 1;
		date = t.monthDay;
		hour = t.hour;
		minute = t.minute;
		second = t.second;
		burn_last_data = burn_last_data + "ʱ�䣺" + year + "��" + month + "��"
				+ date + "��" + hour + "ʱ" + minute + "��" + "\n\r";
		printerData = printerData + "ʱ�䣺" + year + "��" + month + "��"
				+ date + "��" + hour + "ʱ" + minute + "��" + "\n\r";
		/*-----------------------*/

		/*-----------------------*/
		burn_last_data = burn_last_data 
				+ "*******���Խ���********"+ "\n\r" + "\n";
		printerData= printerData 
				+ "*******���Խ���********"+ "\n\r" + 
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
	//�����豸��ʼ������������״̬��ʾ���ڳ�ʼ�����󶨷���
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
	        		//����ѡ���б�
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
	    	   //������������
	    	   if(resultCode==Activity.RESULT_OK)
	    	   {
	    		   mBluetoothDevice = ((BluetoothDevice)data.getParcelableExtra("BLUEDEVICE"));
	    		   
	    		   mWtBluetoothDevice.conectionDevice( mBluetoothDevice, new ConnectListerner(){

	    				@Override
	    				public void onStartConnect() {
	    					// TODO Auto-generated method stub
	    			    	 broadcastUpdate(FxService.ACTION_GATT_CONNECTED, "��������");

	    				}

	    				@Override
	    				public void onDeviceConnected() {
	    					// TODO Auto-generated method stub
	    		       	 	broadcastUpdate(FxService.ACTION_GATT_CONNECTED, "������");


	    				}

	    				@Override
	    				public void onDeviceConnectedFail() {
	    					// TODO Auto-generated method stub
	    			    	 broadcastUpdate(FxService.ACTION_GATT_CONNECTED, "δ����");

	    				}
	    			   
	    		   });

	    	   }
	    	   break;
		}

	}
	//��������״̬��ʾ����
	private void broadcastUpdate(final String action,final String title) {
        final Intent intent = new Intent(action);
        intent.putExtra(FxService.CONNECTED_STATE, title);
        this.sendBroadcast(intent);
    }
	@Override
	public void onClick(View PrinterClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
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
		/* �Ƿ�ǰһ�δ�ӡ��δ���� */
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
		Toast.makeText( PrinterActivity_BT.this, "δ����,��ӡʧ��",
                Toast.LENGTH_SHORT).show(); 
		return false;
	}
	
	WtPrint mWtPrint;
	private void print(ArqService mArqService)
	{
		if(!isLink(mArqService))
			return;
		

		mWtPrint = new WtPrint(this, mArqService);
		
		
//		String msg = "\n\n\n<LOGO>#XX��Լ�̻�ǩ����\n\n�̻���: XX�ʱ��������̻�12345123456aaabbb12345\n�̻���: 818331053980069\n�ն˺�: 60157853\n����Ա��: fuiou\n����: 622500******0220\n��������: ����\nƾ֤��(VOUCHER NO.): 107636\n����ʱ��: 2015-01-05 14:43:40\n���ײο���(REFNO.): 051443831619\n���׽��: 0.66Ԫ\n��ע: (���׳ɹ�)\nARQC:3FC099F13F08B069\nTVR:0000048010\nAID:A000000333010102\nTSI:FC00\nATC:0F3C\nAPP LABEL:PBOC CREDIT\nAPPLICATION:PBOC CREDIT\n\n\n����ȷ�����Ͻ���,ͬ�⽫����뱾���˻�.������Ҫ,���µ�400-6677-333,www.fuiou.com\n\n..........�ͻ����..........\n\n\n\n\n\n\n\n\n\n";

		Toast.makeText( PrinterActivity_BT.this, "��ʼ��ӡ",
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
					Toast.makeText( PrinterActivity_BT.this, "��ȡ״̬ʧ��",
			                Toast.LENGTH_SHORT).show();   
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_PRINT_SUCC)
				{
					Toast.makeText( PrinterActivity_BT.this, "��ӡ�ɹ�",
			                Toast.LENGTH_SHORT).show();  
					isPrinterOk = true;
				}else if(arg0 == PRINT_STATE.STATE_NO_PAPER)
				{
					Toast.makeText( PrinterActivity_BT.this, "ȱֽ",
			                Toast.LENGTH_SHORT).show();  
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_OVER_HEAT)
				{
					Toast.makeText( PrinterActivity_BT.this, "����",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_VOLTAGE_ANOMALY)
				{
					Toast.makeText( PrinterActivity_BT.this, "��ѹ�쳣",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_GREY_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "���ûҶȴ���",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_WRITE_IMAGE_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "��ӡͼƬ����",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_FONT_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "�����������",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_UNDERLINE_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "�����»��ߴ���",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_SET_SPACING_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "���ñ߾����",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}else if(arg0 == PRINT_STATE.STATE_WRITE_STRING_ERR)
				{
					Toast.makeText( PrinterActivity_BT.this, "���ô�ӡ����ʧ��",
			                Toast.LENGTH_SHORT).show(); 
					isPrinterOk = false;
				}
			}
			
		} );
			

	}
	@Override
	protected void onStop() {
		super.onStop();
		FyLog.i(TAG, "����Stop״̬");
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
		if (ifCloseActivity == true) {
			FyLog.i(TAG, "���Խ���finish");
			this.finish();
			FyLog.i(TAG, "��ִ��finish");
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
	/************************** �¼����������� ***************************/
	//������ؼ������ʾ����
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // �����˳��Ի���  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // ���öԻ������  
            isExit.setTitle("ϵͳ��ʾ");  
            // ���öԻ�����Ϣ  
            isExit.setMessage("ȷ��Ҫ�˳���");  
            // ���ѡ��ť��ע�����  
            isExit.setButton("ȷ��", listener);  
            isExit.setButton2("ȡ��", listener);  
            // ��ʾ�Ի���  
            isExit.show();  
  
        }  
          
        return false;  
          
    }  
    /**�����Ի��������button����¼�*/  
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
    {  
        public void onClick(DialogInterface dialog, int which)  
        {  
            switch (which)  
            {  
            case AlertDialog.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����  
            	ActivityManager.clearActivity();
            	PrinterActivity_BT.this.finish();  
                break;  
            case AlertDialog.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���  
                break;  
            default:  
                break;  
            }  
        }  
    };    
	//����Ϊ������ؼ������ʾ����	
	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			switch (event.getAction()) {
			// ������Ļʱ��
			case MotionEvent.ACTION_DOWN:
				printer_image.setBackgroundResource(R.drawable.lucency);
				screen_sleep = false;
				break;
			// �������ƶ�ʱ��
			case MotionEvent.ACTION_MOVE:
				break;
			// ��ֹ����ʱ��
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
	 * ���ı�������
	 * 
	 * @param activity
	 */
	public void toggleBrightness(Activity activity, int light) {
		// ��ȡ����ֵ
		int brightness = getScreenBrightness(activity);
		// �Ƿ������Զ����ڣ��������ر��Զ�����
		boolean isAutoBrightness = isAutoBrightness(getContentResolver());
		if (isAutoBrightness) {
			stopAutoBrightness(activity);
		}
		// brightness += 50;// ���Լ�����������
		// ��������
		setBrightness(activity, light);

		if (brightness > 255) {
			// ���ȳ������ֵ������Ϊ�Զ�����
			startAutoBrightness(activity);
			brightness = 50;// ���Լ�����������
		}
		// ��������״̬
		saveBrightness(getContentResolver(), brightness);
	}

	/**
	 * �ж��Ƿ������Զ����ȵ���
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
	 * ��ȡ��Ļ������
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
	 * ��������
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
	 * ֹͣ�Զ����ȵ���
	 * 
	 * @param activity
	 */
	public void stopAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	/**
	 * ���������Զ�����
	 * 
	 * @param activity
	 */
	public void startAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	/**
	 * ������������״̬
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

	//���ڽ���
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				ifCloseActivity =false;
				FyLog.i(TAG, "home������");
			}

		}

	};
}