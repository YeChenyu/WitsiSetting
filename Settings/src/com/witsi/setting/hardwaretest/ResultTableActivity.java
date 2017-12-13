package com.witsi.setting.hardwaretest;

import java.io.IOException;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.witsi.arq.ArqMisc;

public class ResultTableActivity extends Activity implements
		android.view.View.OnClickListener {
	private String TAG = "ResultTableActivity";
	private Context context = ResultTableActivity.this;
	
	TextView textView_Result_TouchScreen;
	TextView textView_Result_Screen;
	TextView textView_Result_MAGC;
	TextView textView_Result_Printer;
	TextView textView_Result_Buzzer;
	TextView textView_Result_Security;
	TextView textView_Result_Key;
	TextView textView_Result_IC;
	TextView textView_Result_SAM;
	TextView textView_Result_RF;
	TextView textView_Result_LED;
	TextView textView_Result_Version;
	TextView textView_Result_GPRS;
	TextView textView_Result_WIFI;
	TextView textView_Result_TF;
	TextView textView_Result_SerialNumber;
	TextView textView_Result_Camera;
	TextView textView_Result_RTC;
	TextView textView_Result_MEDIA;
	TextView textView_Result_BLUETOOTH;
	TextView textView_Result_SHAKE;
	
	Button Button_Result_Return_home;
	Button Button_Stop_Burn;
	ImageView result_image;
	View main = null;
	
	FileHelper helper;
	
	int nowyear;
	int nowmonth;
	int nowdate;
	int nowhour;
	int nowminute;
	int nowsecond;
	int startyear;
	int startmonth;
	int startdate;
	int starthour;
	int startminute;
	int startsecond;
	int burntimes = 0;
	int burntime = 0, nowtime = 0;
	boolean screen_sleep = false;
	// SQLiteDatabase db;
	boolean isburning = false;
	boolean result_over = false;
	private String burn_last_data;
	
	private SharedPreferences config;
	private Editor editor;
	private ArqMisc arqMisc = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		FyLog.i(TAG, "������������");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		// ��������ѡ����
		isburning = config.getBoolean("flag_burn", false);
		if (isburning == true) {
			getLayoutInflater();
			// ��װ���ء����ð�~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_result_table_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			// FyLog.i("����", "�ر�");
			 toggleBrightness(this, 0);
		} else {
			setContentView(R.layout.hardware_result_table_activity);
			FyLog.i("����", "����");
			toggleBrightness(this, 200);
		}
		System.gc();
		FyLog.i("ResultTable", "System.gc()");
		/***************************************/
		initViews();

		initDatas();
	}

	private void initViews() {
		result_image = (ImageView) this.findViewById(R.id.result_image);
		textView_Result_TouchScreen = (TextView) this.findViewById(R.id.result_touchScreen);
		textView_Result_Screen = (TextView) this.findViewById(R.id.result_screen);
		textView_Result_MAGC = (TextView) this.findViewById(R.id.result_magc);
		textView_Result_Printer = (TextView) this.findViewById(R.id.result_printer);
		textView_Result_Buzzer = (TextView) this.findViewById(R.id.result_buzzer);
		textView_Result_Security = (TextView) this.findViewById(R.id.result_security);
		textView_Result_Key = (TextView) this.findViewById(R.id.result_key);
		textView_Result_IC = (TextView) this.findViewById(R.id.result_ic);
		textView_Result_SAM = (TextView) this.findViewById(R.id.result_sam);
		textView_Result_RF = (TextView) this.findViewById(R.id.result_rf);
		textView_Result_LED = (TextView) this.findViewById(R.id.result_led);
		textView_Result_Version = (TextView) this.findViewById(R.id.result_version);
		textView_Result_GPRS = (TextView) this.findViewById(R.id.result_gprs);
		textView_Result_WIFI = (TextView) this.findViewById(R.id.result_wifi);
		textView_Result_TF = (TextView) this.findViewById(R.id.result_tf);
		textView_Result_SerialNumber = (TextView) this.findViewById(R.id.result_serialnumber);
		textView_Result_Camera = (TextView) this.findViewById(R.id.result_camera);
		textView_Result_RTC = (TextView) this.findViewById(R.id.result_rtc);
		textView_Result_MEDIA = (TextView) this.findViewById(R.id.result_media);
		textView_Result_BLUETOOTH = (TextView) this.findViewById(R.id.result_bt);
		textView_Result_SHAKE = (TextView) this.findViewById(R.id.result_shake);
		Button_Result_Return_home = (Button) this.findViewById(R.id.result_table_return_home);
		Button_Stop_Burn = (Button) this.findViewById(R.id.stop_burn);

		Button_Result_Return_home.setOnClickListener(ResultTableActivity.this);
		Button_Stop_Burn.setOnClickListener(ResultTableActivity.this);
		
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			textView_Result_MEDIA.setText(config.getString("media", ""));
			textView_Result_BLUETOOTH.setText(config.getString("bluetooth", ""));
			textView_Result_SHAKE.setText(config.getString("shake", ""));
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
			textView_Result_MEDIA.setVisibility(View.GONE);
			textView_Result_BLUETOOTH.setVisibility(View.GONE);
			textView_Result_SHAKE.setVisibility(View.GONE);
		}

		textView_Result_TouchScreen.setText(config.getString("touchscreen", ""));
		textView_Result_Screen.setText(config.getString("screen", ""));
		textView_Result_MAGC.setText(config.getString("magc", ""));
		textView_Result_Printer.setText(config.getString("printer", ""));
		textView_Result_Buzzer.setText(config.getString("buzzer", ""));
		textView_Result_Security.setText(config.getString("security", ""));
		textView_Result_Key.setText(config.getString("key", ""));
		textView_Result_IC.setText(config.getString("ic", ""));
		textView_Result_SAM.setText(config.getString("sam", ""));
		textView_Result_RF.setText(config.getString("rf", ""));
		textView_Result_LED.setText(config.getString("led", ""));
		textView_Result_Camera.setText(config.getString("camera", ""));
		textView_Result_Version.setText(config.getString("version", ""));
		textView_Result_GPRS.setText(config.getString("gprs", ""));
		textView_Result_WIFI.setText(config.getString("wifi", ""));
		textView_Result_TF.setText(config.getString("tf", ""));
		textView_Result_RTC.setText(config.getString("rtc", ""));
		textView_Result_SerialNumber.setText(config.getString("serialnumber", ""));
		
		startyear = (Integer) config.getInt("year", 0);
		startmonth = (Integer) config.getInt("month", 0);
		startdate = (Integer) config.getInt("date", 0);
		starthour = (Integer) config.getInt("hour", 0);
		startminute = (Integer) config.getInt("minute", 0);
		startsecond = (Integer) config.getInt("second", 0);
	}
	
	private void initDatas() {
		arqMisc = new ArqMisc(context);
		Time t = new Time(); // or Time t=new Time("GMT+8"); ����Time Zone���ϡ�
		t.setToNow(); // ȡ��ϵͳʱ�䡣
		nowyear = t.year;
		nowmonth = t.month + 1;
		nowdate = t.monthDay;
		nowhour = t.hour;
		nowminute = t.minute;
		nowsecond = t.second;

		if (config.getBoolean("light", true) == false) {
			result_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		FyLog.i("!!!!!!!!!!", "eeeeeeee");
		if (isburning == true) {
			burn_last_data = " ������ʼʱ�䣺" + "\n\r" + "		" + startyear + "��"
					+ startmonth + "��" + startdate + "��" + starthour + "ʱ"
					+ startminute + "��" + "\n\r";
			burn_last_data = burn_last_data + "��������ʱ�䣺" + "\n\r" + "		"
					+ nowyear + "��" + nowmonth + "��" + nowdate + "��" + nowhour
					+ "ʱ" + nowminute + "��" + "\n\r";
			
			helper = new FileHelper(getApplicationContext());
			if (config.getString("camera", "").equals("ng")) {
				burn_last_data = burn_last_data + "��������ԣ�            ʧ��"
						+ "\n\r";
			} else if (config.getString("camera", "").equals("ok")){
				burn_last_data = burn_last_data + "��������ԣ�            �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("touchscreen", "").equals("ng")) {
				burn_last_data = burn_last_data + "���������ԣ�            ʧ��"
						+ "\n\r";
			} else if (config.getString("touchscreen", "").equals("ok")){
				burn_last_data = burn_last_data + "���������ԣ�            �ɹ�"
						+ "\n\r";
			}
			/*-----------------------*/
			if (config.getString("screen", "").equals("ng")){
				burn_last_data = burn_last_data + "������ԣ�                ʧ��"
						+ "\n\r";
			} else if (config.getString("screen", "").equals("ok")){
				burn_last_data = burn_last_data + "������ԣ�                �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("nagc", "").equals("ng")){
				burn_last_data = burn_last_data + "�ſ����ԣ�                ʧ��"
						+ "\n\r";
			} else if (config.getString("magc", "").equals("ok")){
				burn_last_data = burn_last_data + "�ſ����ԣ�                �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("printer", "").equals("ng")) {
				burn_last_data = burn_last_data + "��ӡ�����ԣ�            ʧ��"
						+ "\n\r";
			} else if (config.getString("printer", "").equals("ok")){
				burn_last_data = burn_last_data + "��ӡ�����ԣ�            �ɹ�"
						+ "\n\r";
			}
			/*-----------------------*/
			if (config.getString("buzzer", "").equals("ng")) {
				burn_last_data = burn_last_data + "���������ԣ�            ʧ��"
						+ "\n\r";
			} else if (config.getString("buzzer", "").equals("ok")) {
				burn_last_data = burn_last_data + "���������ԣ�            �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("touchscreen", "").equals("ng")) {
				burn_last_data = burn_last_data + "��ȫ״̬���ԣ�        ʧ��" + "\n\r";
			} else  if (config.getString("security", "").equals("ok")){
				burn_last_data = burn_last_data + "��ȫ״̬���ԣ�        �ɹ�" + "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("key", "").equals("ng")){
				burn_last_data = burn_last_data + "�������ԣ�               ʧ��"
						+ "\n\r";
			} else  if (config.getString("key", "").equals("ok")){
				burn_last_data = burn_last_data + "�������ԣ�               �ɹ�"
						+ "\n\r";
			}
			/*-----------------------*/
			if (config.getString("ic", "").equals("ng")){
				burn_last_data = burn_last_data + "IC�����ԣ�                ʧ��"
						+ "\n\r";
			} else if (config.getString("ic", "").equals("ok")) {
				burn_last_data = burn_last_data + "IC�����ԣ�                �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("sam", "").equals("ng")){
				burn_last_data = burn_last_data + "SAM�����ԣ�           ʧ��"
						+ "\n\r";
			} else if (config.getString("sam", "").equals("ok")) {
				burn_last_data = burn_last_data + "SAM�����ԣ�           �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("rf", "").equals("ng")){
				burn_last_data = burn_last_data + "RF�����ԣ�              ʧ��"
						+ "\n\r";
			} else if (config.getString("rf", "").equals("ok")) {
				burn_last_data = burn_last_data + "RF�����ԣ�              �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("led", "").equals("ng")){
				burn_last_data = burn_last_data + "LED���ԣ�                ʧ��"
						+ "\n\r";
			} else if (config.getString("led", "").equals("ok")){
				burn_last_data = burn_last_data + "LED���ԣ�                �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("version", "").equals("ng")) {
				burn_last_data = burn_last_data + "�汾�Ų��ԣ�           ʧ��"
						+ "\n\r";
			} else if (config.getString("version", "").equals("ok")){
				burn_last_data = burn_last_data + "�汾�Ų��ԣ�           �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("wifi", "").equals("ng")){
				burn_last_data = burn_last_data
						+ "Wifi���ԣ�                ʧ��" + "\n\r";
			} else if (config.getString("wifi", "").equals("ok")){
				burn_last_data = burn_last_data
						+ "Wifi���ԣ�                �ɹ�" + "\n\r";
			}
			/*-----------------------*/
			if (config.getString("gprs", "").equals("ng")){
				burn_last_data = burn_last_data + "GPRS���ԣ�             ʧ��"
						+ "\n\r";
			} else if (config.getString("gprs", "").equals("ok")){
				burn_last_data = burn_last_data + "GPRS���ԣ�             �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("tf", "").equals("ng")){
				burn_last_data = burn_last_data + "TF�����ԣ�              ʧ��"
						+ "\n\r";
			} else if (config.getString("tf", "").equals("ok")){
				burn_last_data = burn_last_data + "TF�����ԣ�              �ɹ�"
						+ "\n\r";
			} 
			/*-----------------------*/
			if (config.getString("serialnumber", "").equals("ng")){
				burn_last_data = burn_last_data + "���кŲ��ԣ�           ʧ��"
						+ "\n\r";
			} else if (config.getString("serialnumber", "").equals("ok")) {
				burn_last_data = burn_last_data + "���кŲ��ԣ�           �ɹ�"
						+ "\n\r";
			}
			burntimes = (Integer) config.getInt("burntimes", 0);
			burntimes++;
			burn_last_data = burn_last_data + "����������                    "
					+ burntimes + "\n\r";
			editor.putInt("burntimes", burntimes);
			editor.commit();
			
			try {
				helper.deleteNativeFile("test.txt");
				helper.createNativeFile("test.txt");
				helper.witeNativeFile("test.txt", burn_last_data);
				FyLog.i("RsultTale", "�ļ������ɹ�");
			} catch (IOException e) {
				e.printStackTrace();
				FyLog.e("RsultTale", "�ļ�����ʧ��");
			}
			//����ʧ��
			if ((Integer) config.getInt("error", 0) == 1) {
				isburning = false;
				screen_sleep = false;
				toggleBrightness(this, 200);
				result_image.setBackgroundResource(R.drawable.bg_transport);
				Button_Stop_Burn.setText("�����쳣����");
				editor.putBoolean("burn_success", false);
				editor.commit();
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						arqMisc.buzzerCtl(2, 300, 300);
					}
				}, 1500);
			}
		}
		FyLog.i("RsultTable!!!!!!!!!!!!!", "��ʼ����ʱ��");
		if (isburning == true) {
			burntime = (Integer) config.getInt("burntime", 0);
			nowtime = nowdate * 24 * 60 + nowhour * 60 + nowminute;
			FyLog.i(TAG, "burntime:" + burntime);
			FyLog.i(TAG, "nowtime:" + nowtime);
			if (nowtime >= burntime) {
				isburning = false;
				screen_sleep = false;
				editor.putBoolean("burn_success", true);
				editor.commit();
				Button_Stop_Burn.setText("������������");
				toggleBrightness(this, 200);
				result_image.setBackgroundResource(R.drawable.bg_transport);
				//����������
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						arqMisc.buzzerCtl(1, 2000, 500);
					}
				}, 1500);
			}else{
				//�����ڶ��ֿ���ʱ��Ļ����
				if(ProjectConfig.burn_config){
					if (screen_sleep == false) {
						editor.putBoolean("light", false);
						editor.commit();
					}
				}
				//���еڶ��ֿ���
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if(isburning == true)
							ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SCREEN);
					}
				}, 5000);
			}
		}
		//ȫ�����Բ���ʵֹͣ����
		if (config.getBoolean("alltest", false) == true) {
			Button_Stop_Burn.setText("");
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}

	@Override
	public void onClick(View mainClick) {
		result_over = true;
		isSleepExit = false;
		editor.putBoolean("light", true);
		isburning = false;
		FyLog.i("����", "����");
		toggleBrightness(this, 200);
		startAutoBrightness(this);
		switch (mainClick.getId()) {
		case R.id.result_table_return_home: {
			ActivityManagers.clearActivity();
			ActivityManagers.trunToEntryActivity(ResultTableActivity.this);
			break;
		}
		case R.id.stop_burn: {
			editor.putBoolean("burn_stop", true);
			editor.commit();
			isburning = false;
			Button_Stop_Burn.setText("��ֹͣ����");
			break;
		}
		default:
			break;
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
					isburning = false;
					editor.putBoolean("flag_burn", false);
					editor.putBoolean("burn_stop", true);
					editor.commit();
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
				result_image.setBackgroundResource(R.drawable.bg_transport);
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
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
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
