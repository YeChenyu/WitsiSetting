package com.witsi.setting.hardwaretest;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.witsi.arq.ArqConverts;
import android.witsi.misc.Printer;
//import android.device.misc.Printer;

public class PrinterActivity extends Activity implements
		android.view.View.OnClickListener {
	private final String TAG = "PrinterActivity";
	private Context context = PrinterActivity.this;
	
	private TextView prn_status, prn_content;
	private Button button_Printer_return;
	private Button button_Printer_ok;
	private Button button_Printer_false;
	private Button button_Printer_test;
	ImageView printer_image;
	View main = null;
	
	private MyRunnable myrun = new MyRunnable();
	private Thread thread = null;
	private MyHandler mHandler = new MyHandler(this);
	private ArqMisc miscArq;
	private Printer prn;
	private SeekBar sk;
	private TextView tv_grey;
	
	byte[] printerpicture = null;
	String burn_last_data;
	String printerData;
	private String msg = null;
	private boolean printThreadRunning = false;
	public int flag_printer = -1;
	boolean screen_sleep = false;
	boolean isburning = false;
	int year, month, date, hour, minute, second, burntime;

	private SharedPreferences config;
	private Editor editor;

	// ����2������ͬ��,����֮ǰ״ֵ̬:
	// true --- �Ѿ����߳���������,�ȴ�;
	// false --- û���߳�����,��ǰ��������
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
	}

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
		prn = new Printer();
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
		// SysApplication.getInstance().addActivity(this);
		
		prn_status = (TextView) findViewById(R.id.prnStatus);
		prn_content = (TextView) findViewById(R.id.prnContent);
		// display the content
		miscArq = new ArqMisc(this);
		button_Printer_return = (Button) this
				.findViewById(R.id.back);
		button_Printer_ok = (Button) this.findViewById(R.id.pass);
		button_Printer_false = (Button) this.findViewById(R.id.fail);
		button_Printer_test = (Button) this.findViewById(R.id.test);
		button_Printer_return.setOnClickListener(PrinterActivity.this);
		button_Printer_ok.setOnClickListener(PrinterActivity.this);
		button_Printer_false.setOnClickListener(PrinterActivity.this);
		button_Printer_test.setOnClickListener(PrinterActivity.this);
		button_Printer_test.setText("��ӡ");
		sk = (SeekBar) findViewById(R.id.sk_grey);
		tv_grey = (TextView) findViewById(R.id.tv_grey);
		sk.setProgress(10);
		sk.setMax(20);
		tv_grey.setText("10");
		sk.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				tv_grey.setText("" + arg1);
			}
		});

		printer_image = (ImageView) this.findViewById(R.id.printer_image);
		if (config.getBoolean("light", true) == false) {
			printer_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		burn_last_data = "";
		burn_last_data = burn_last_data + "\r";
		printerData = "";
		printerData = printerData + "\r";
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
		if (config.getString("sam", "").equals("ng")) {
			burn_last_data = burn_last_data + "SAM�����ԣ� һһһһN" + "\n\r";
			printerData = printerData + "SAM�����ԣ� һһһһN" + "\n\r";
		} else if (config.getString("sam", "").equals("ok")){
			burn_last_data = burn_last_data + "SAM�����ԣ� һһһһY" + "\n\r";
			printerData = printerData + "SAM�����ԣ� һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "SAM�����ԣ� һһһһ��" + "\n\r";
			printerData = printerData + "SAM�����ԣ� һһһһ��" + "\n\r";
		}
		/*-----------------------*/
		if (config.getString("rf", "").equals("ng")) {
			burn_last_data = burn_last_data + "�ǽӿ����ԣ� һһһһN" + "\n\r";
			printerData = printerData + "�ǽӿ����ԣ� һһһһN" + "\n\r";
		} else if (config.getString("rf", "").equals("ok")){
			burn_last_data = burn_last_data + "�ǽӿ����ԣ� һһһһY" + "\n\r";
			printerData = printerData + "�ǽӿ����ԣ� һһһһY" + "\n\r";
		} else{
			burn_last_data = burn_last_data + "�ǽӿ����ԣ� һһһһ��" + "\n\r";
			printerData = printerData + "�ǽӿ����ԣ� һһһһ��" + "\n\r";
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
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			/*-----------------------*/
			if (config.getString("media", "").equals("ng"))  {
				burn_last_data = burn_last_data + "��Ƶ���ԣ�   һһһN" + "\n\r";
				printerData = printerData + "��Ƶ���ԣ�һһһһN" + "\n\r";
			} else if (config.getString("media", "").equals("ok"))  {
				burn_last_data = burn_last_data + "��Ƶ���ԣ�   һһһY" + "\n\r";
				printerData = printerData + "��Ƶ���ԣ�һһһһY" + "\n\r";
			} else{
				burn_last_data = burn_last_data + "��Ƶ���ԣ�   һһһ��" + "\n\r";
				printerData = printerData + "��Ƶ���ԣ�һһһһ��" + "\n\r";
			}
			/*-----------------------*/
			if (config.getString("bluetooth", "").equals("ng"))  {
				burn_last_data = burn_last_data + "�������ԣ�   һһһN" + "\n\r";
				printerData = printerData + "�������ԣ�һһһһN" + "\n\r";
			} else if (config.getString("bluetooth", "").equals("ok"))  {
				burn_last_data = burn_last_data + "�������ԣ�   һһһY" + "\n\r";
				printerData = printerData + "�������ԣ�һһһһY" + "\n\r";
			} else{
				burn_last_data = burn_last_data + "�������ԣ�   һһһ��" + "\n\r";
				printerData = printerData + "�������ԣ�һһһһ��" + "\n\r";
			}
		}
		/*-----------------------*/
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			/*-----------------------*/
			if (config.getString("shake", "").equals("ng"))  {
				burn_last_data = burn_last_data + "���Ӳ��ԣ�   һһһN" + "\n\r";
				printerData = printerData + "���Ӳ��ԣ�һһһһN" + "\n\r";
			} else if (config.getString("shake", "").equals("ok"))  {
				burn_last_data = burn_last_data + "���Ӳ��ԣ�   һһһY" + "\n\r";
				printerData = printerData + "���Ӳ��ԣ�һһһһY" + "\n\r";
			} else{
				burn_last_data = burn_last_data + "���Ӳ��ԣ�   һһһ��" + "\n\r";
				printerData = printerData + "���Ӳ��ԣ�һһһһ��" + "\n\r";
			}
		}
//		burn_last_data = burn_last_data + "�豸���кţ�" + getsysteminfo(0x02)
//				+ "\n\r";
//		burn_last_data = burn_last_data + "Ӳ���汾�ţ�" + getsysteminfo(0x03)
//				+ "\n\r";
//		burn_last_data = burn_last_data + "����汾�ţ�" + getsysteminfo(0x01)
//				+ "\n\r";
//		printerData = printerData + "�豸���кţ�" + getsysteminfo(0x02)
//				+ "\n\r";
//		printerData = printerData + "Ӳ���汾�ţ�" + getsysteminfo(0x03)
//				+ "\n\r";
//		printerData = printerData + "����汾�ţ�" + getsysteminfo(0x01)
//				+ "\n\r";
		
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
				+ "*******���Խ���********"+ "\n\r" + "\n\r";
		printerData= printerData 
				+ "*******���Խ���********"+ "\n\r" + "\n\r";
		msg = "\n\n\n" + printerData;
		prn_content.setText(burn_last_data);
		/*-----------------------*/

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isburning == true) {
					Intent intent = new Intent(PrinterActivity.this,
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
			toggleBrightness(this, 200);// ����
			printer_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (PrinterClick.getId()) {
			case R.id.back: {
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(PrinterActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(PrinterActivity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(PrinterActivity.this);
				}   
				break;
			}
			case R.id.pass: {
				flag_printer = 1;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("printer", "ok");
					ActivityManagers.trunToSingleTestActivity(PrinterActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("printer", "ok");
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(PrinterActivity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(PrinterActivity.this);
				} 
				editor.commit();
				break;
			}
			case R.id.fail: {
				flag_printer = 0;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("printer", "ng");
					ActivityManagers.trunToSingleTestActivity(PrinterActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("printer", "ng");
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(PrinterActivity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(PrinterActivity.this);
				} 
				editor.commit();
				break;
			}
			case R.id.test: {
				//��ʼ��ӡ
				startPrintBtnOnClickHandler(button_Printer_test);
			}
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		if(thread != null){
			thread.interrupt();
			thread = null;
			myrun = null;
		}
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
		/* �Ƿ�ǰһ�δ�ӡ��δ���� */
		FyLog.i("Printer", "��������");
		if (printThreadRunning == true) {
			FyLog.i(TAG, "printer is busy. just return.");
			return;
		}
		FyLog.i("Printer", "�����߳�");
		thread = new Thread(myrun);
		FyLog.i("Printer", "�����߳�");
		thread.start();
		FyLog.i("Printer", "�����߳�Y");
	}

	class MyRunnable implements Runnable {
		public void run() {
			int ret;
			FyLog.i("Printer", "����Runnable");
			// Wait printer to ready
			while (threadTestAndSet()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					FyLog.d(TAG, "Waiting didnt work!!");
					e.printStackTrace();
				}
			}
			/* init printer */
			ret = prn.init();
			if (ret != 0) { /* success. */
				FyLog.e(TAG, "��ӡ����ʼ��N!!!");
			}
			byte[] printerpicture = new byte[12288];
			for (int i = 0; i < 12288; i++) {
				printerpicture[i] = (byte) 0xff;
			}
			FyLog.i(TAG, HexCodec.hexEncode(printerpicture));
			prn.setGrey(sk.getProgress());
			ret = prn.printImage(384, 32, 0, printerpicture);
			ret = prn.printString(msg);
			FyLog.i(TAG, "" + ret);
			if (ret != 0) {
				FyLog.e(TAG, "��ӡ�ַ���N!!!");
				sendMessageToMain("��ӡ�ַ���N!!!");
				printThreadRunning = false;
				return;
			}
			// }

			/* print the string */
			ret = prn.start();
			FyLog.i(TAG, "start_ret:" + ret);
			if (ret != 0) {
				FyLog.e(TAG, "��ӡ������N!!!");
				printThreadRunning = false;
			}
			int status ;
			while (true) {
				/* wait 500ms */
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					FyLog.d(TAG, "Waiting didnt work!!");
					e.printStackTrace();
				}

				status = prn.getStatus();
				if(ret < 0){
					FyLog.e(TAG, "��ȡ��ӡ��״̬ʧ�ܣ�");
				}
				if (status == 0) {
					FyLog.e(TAG, "��ӡY,������ӡ��");
					sendMessageToMain("��ӡ�ɹ�,������ӡ��");
					break;
				}
				if (prn.noPaper()) {
					FyLog.e(TAG, "��ӡ��ȱֽ,������ӡ��");
					sendMessageToMain("��ӡ��ȱֽ,������ӡ��");
					break;
				}
				if (prn.overHeat()) {
					FyLog.e(TAG, "��ӡ������,������ӡ��");
					sendMessageToMain("��ӡ������,������ӡ��");
					break;
				}
				if (prn.isBusy()) {
					FyLog.e(TAG, "��ӡ����ӡ�С�����");
				}
			}

			/* close printer */
			prn.exit();
			printThreadRunning = false;
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
		private WeakReference<PrinterActivity> mOuter;

		public MyHandler(PrinterActivity activity) {
			mOuter = new WeakReference<PrinterActivity>(activity);
		}

		public void handleMessage(Message msg) {
			PrinterActivity outer = mOuter.get();
			Toast.makeText(PrinterActivity.this, msg.getData().getString("status"),
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
	
	/************************** �¼����������� ***************************/
	@SuppressWarnings("deprecation")
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
            isExit.setButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					ActivityManagers.clearActivity();
					finish();
				}
			});  
            isExit.setButton2("ȡ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // ��ʾ�Ի���  
            isExit.show();  
        }  
        return false;  
    }  
	
	//����Ϊ������ؼ������ʾ����	
	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			switch (event.getAction()) {
			// ������Ļʱ��
			case MotionEvent.ACTION_DOWN:
				printer_image.setBackgroundResource(R.drawable.bg_transport);
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

}