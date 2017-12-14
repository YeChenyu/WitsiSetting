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
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
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
		if(D)FyLog.i(TAG, "����");
		// ��������ѡ����
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
		
		Time t = new Time(); // or Time t=new Time("GMT+8"); ����Time Zone���ϡ�
		t.setToNow(); // ȡ��ϵͳʱ�䡣
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
		
		editor.putString("touchscreen", "��");
		editor.putString("screen", "��");
		editor.putString("magc", "��");
		editor.putString("printer", "��");
		editor.putString("buzzer", "��");
		editor.putString("security", "��");
		editor.putString("key", "��");
		editor.putString("ic", "��");
		editor.putString("led", "��");
		editor.putString("version", "��");
		editor.putString("gprs", "��");
		editor.putString("wifi", "��");
		editor.putString("tf", "��");
		editor.putString("serialnumber", "��");
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
				title = "�ɹ�";
			else 
				title = "ʧ��";
			if(config.getBoolean("burn_stop", false) == true)
				title = "�ֶ�ֹͣ";
			FyLog.d(TAG, "the burn stop is: " + config.getBoolean("burn_stop", false));
			String tmp = helper.readNativeFile("test.txt");
			if(tmp == null){
				tmp = "���޿�����¼";
				title = "��";
			}
			new AlertDialog.Builder(BurnningActivity.this).setTitle("�ϴο�����¼��" + title)
					.setMessage(tmp)
					.setPositiveButton("ȡ��", null).show();
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
