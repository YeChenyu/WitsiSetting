package com.witsi.setting.hardwaretest;


import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class EntryActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = "EntryActivity";
	private Context context = EntryActivity.this;
	public static final String LOCK = "factory_lock";
	public static final String LOCK_KEY = "lock_key";
	
	private Button button_single_test, button_all_test, button_burn_test, button_cmd,
			button_exit, button_gpio, button_trade;
	private TextView text_tool_version;
	
	private WifiAdmin mWifiAdmin;
	private boolean ifCloseActivity = true;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	private SharedPreferences config;
	private Editor editor;
	
	@Override
	protected void onResume(){
		super.onResume();
		
		FyLog.i(TAG, "onResume");
		ifCloseActivity = true;
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		setContentView(R.layout.hardware_entry_activity);
		
		// SysApplication.getInstance().addActivity(this);
		int lightnum = getScreenBrightness(this);
		FyLog.i("light", ""+lightnum);
		toggleBrightness(this,lightnum);//����
		//startAutoBrightness(this);
		initViews();
		
		initDatas();
		
	}
	private void initViews() {
		
		findViewById(R.id.action_back).findViewById(R.id.sw).setVisibility(View.GONE);
		findViewById(R.id.action_back).findViewById(R.id.ll_back).setVisibility(View.GONE);
		button_single_test = (Button) this.findViewById(R.id.entry_single);
		button_all_test = (Button) this.findViewById(R.id.entry_all);
		button_burn_test = (Button) this.findViewById(R.id.entry_burn);
		button_trade = (Button) this.findViewById(R.id.entry_trans);
		button_exit = (Button) this.findViewById(R.id.entry_exit);
		button_cmd = (Button) this.findViewById(R.id.entry_cmd);
		button_gpio = (Button) this.findViewById(R.id.entry_ti_gpio);
		button_single_test.setOnClickListener(EntryActivity.this);
		button_all_test.setOnClickListener(EntryActivity.this);
		button_burn_test.setOnClickListener(EntryActivity.this);
		button_trade.setOnClickListener(EntryActivity.this);
		button_exit.setOnClickListener(EntryActivity.this);
		button_cmd.setOnClickListener(EntryActivity.this);
		button_gpio.setOnClickListener(EntryActivity.this);
		text_tool_version = (TextView)this.findViewById(R.id.text_tool_version);
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
			button_trade.setVisibility(View.GONE);
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			
		}
	}
	
	private void initDatas() {
		
		Display dis = getWindowManager().getDefaultDisplay();
		FyLog.e(TAG, "the width is: " +dis.getWidth() + " the height: " + dis.getHeight() );
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		mWifiAdmin = new WifiAdmin(this);
		try {
			text_tool_version.setText("���Թ��߰汾="+ getLocalVersionCode(this));
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		��ʼ����������ѡ��
//		ActivityManagers.initBurnningConfig(config);
		new WifiAdmin(EntryActivity.this).OpenWifi();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					if(mWifiAdmin.isWifiConnected(EntryActivity.this) == false){
						mWifiAdmin.StartScan();
						mWifiAdmin.getAndContNoPassWordNet();
					}else{
						FyLog.v(TAG, "wifi connect");
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
		
	@Override
	public void onClick(View v) {
		Intent intent;
//����״̬
		editor.putInt("error", 0);
		editor.putInt("burntimes", 0);
		editor.putBoolean("singletest", false);
		editor.putBoolean("alltest", false);
		editor.putBoolean("flag_burn", false);
		editor.putBoolean("light", true);
		editor.commit();
//��ʾ�Ĳ��Խ��	
		
			editor.putInt("burn_nums", 0);
			editor.putString("touchscreen", "��");
			editor.putString("screen", "��");
			editor.putString("magc", "��");
			editor.putString("printer", "��");
			editor.putString("buzzer", "��");
			editor.putString("security", "��");
			editor.putString("key", "��");
			editor.putString("ic", "��");
			editor.putString("sam", "��");
			editor.putString("rf", "��");
			editor.putString("led", "��");
			editor.putString("version", "��");
			editor.putString("gprs", "��");
			editor.putString("wifi", "��");
			editor.putString("tf", "��");
			editor.putString("serialnumber", "��");
			editor.putString("camera", "��");
			editor.putString("rtc", "��");
			editor.putString("lock_screen", "��");
			editor.putString("shake", "��");
			editor.putString("media", "��");
			editor.putString("bluetooth", "��");
			editor.commit();
			//ʱ���¼
			editor.putInt("year", 0);
			editor.putInt("month", 0);
			editor.putInt("date", 0);
			editor.putInt("hour", 0);
			editor.putInt("minute", 0);
			editor.putInt("second", 0);
			editor.putInt("burntime", 0);
			editor.commit();
	
//���������¼
//		editor.putInt("error_touchscreen", 0);
//		editor.putInt("error_screen", 0);
//		editor.putInt("error_magc", 0);
//		editor.putInt("error_printer", 0);
//		editor.putInt("error_buzzer", 0);
//		editor.putInt("error_security", 0);
//		editor.putInt("error_key", 0);
//		editor.putInt("error_ic", 0);
//		editor.putInt("error_sam", 0);
//		editor.putInt("error_rf", 0);
//		editor.putInt("error_led", 0);
//		editor.putInt("error_version", 0);
//		editor.putInt("error_gprs", 0);
//		editor.putInt("error_wifi", 0);
//		editor.putInt("error_tf", 0);
//		editor.putInt("error_serialnumber", 0);
//		editor.putInt("error_camera", 0);
//		editor.putInt("error_rtc", 0);
//		editor.putInt("error_lock_screen", 0);
//		editor.putInt("error_shake", 0);
//		editor.putInt("error_media", 0);
//		editor.putInt("error_bluetooth", 0);
//		editor.commit();

/* ��Ǳ�ѡ��Ҫ���п�������Ŀ */
		editor.putBoolean("flag_checkbox_screen", true);
		editor.putBoolean("flag_checkbox_buzzer", false);
		editor.putBoolean("flag_checkbox_security", true);
		editor.putBoolean("flag_checkbox_ic", true);
		editor.putBoolean("flag_checkbox_sam", true);
		editor.putBoolean("flag_checkbox_rf", false);
		editor.putBoolean("flag_checkbox_led", true);
		editor.putBoolean("flag_checkbox_version", true);
		editor.putBoolean("flag_checkbox_tf", false);
		editor.putBoolean("flag_checkbox_serialnumber", true);
		editor.putBoolean("flag_checkbox_shake", false);
		editor.commit();

		isSleepExit = false;
		switch (v.getId()) {
		case R.id.entry_single: {
			
			editor.putBoolean("singletest", true);
			editor.putBoolean("alltest", false);
			editor.commit();
			ActivityManagers.trunToSingleTestActivity(EntryActivity.this);
			break;
		}
		case R.id.entry_all: {
			
			editor.putBoolean("singletest", false);
			editor.putBoolean("alltest", true);
			editor.commit();
			ActivityManagers.startNextActivity(EntryActivity.this);
			break;
		}
		case R.id.entry_burn: {
			intent = new Intent(EntryActivity.this, BurnningActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.entry_trans: {
			intent = new Intent(EntryActivity.this, TradeActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.entry_cmd: {
			intent = new Intent(EntryActivity.this, CmdActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.entry_exit: {
			this.finish();
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
	    
	public static int getLocalVersionCode(Context context)  
	        throws NameNotFoundException {  
	    PackageManager packageManager = context.getPackageManager();  
	    PackageInfo packageInfo = packageManager.getPackageInfo(  
	            context.getPackageName(), 0);  
	    return packageInfo.versionCode;  
	} 
	
/* ******************************************************************************************** */		
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
