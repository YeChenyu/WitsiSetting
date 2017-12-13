package com.witsi.setting.hardwaretest;


import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenCheckActivity extends Activity implements
		android.view.View.OnClickListener {
	
	String TAG = "ScreenCheckActivity";
	private Context context = ScreenCheckActivity.this;
	
	private Button button_ScreenCheck_return;
	private Button button_ScreenCheck_ok;
	private Button button_ScreenCheck_false;
	private Button button_ScreenCheck_test;
	View main = null;
	ImageView screencheck_image;
	public TextView flag_screen_burn_state;
	
	boolean screen_sleep = false;
	boolean isburning = false;
	
	private SharedPreferences config;
	private Editor editor;
	
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		FyLog.i(TAG, "������Ļ������Խ���");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//����ʱ���ع�����
		if (config.getBoolean("light", true) == false) {
			// ��װ���ء����ð�~
			main = LayoutInflater.from(context).inflate(
					R.layout.hardware_screencheck_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_screencheck_activity);
			screen_sleep = false;
		}
		/************ UI **********************/
		flag_screen_burn_state = (TextView) this.findViewById(R.id.flag_screen_burn_state);
		button_ScreenCheck_return = (Button) this.findViewById(R.id.back);
		button_ScreenCheck_ok = (Button) this.findViewById(R.id.pass);
		button_ScreenCheck_false = (Button) this.findViewById(R.id.fail);
		button_ScreenCheck_test = (Button) this.findViewById(R.id.test);
		button_ScreenCheck_return.setOnClickListener(ScreenCheckActivity.this);
		button_ScreenCheck_ok.setOnClickListener(ScreenCheckActivity.this);
		button_ScreenCheck_false.setOnClickListener(ScreenCheckActivity.this);
		button_ScreenCheck_test.setOnClickListener(ScreenCheckActivity.this);
		//�����Ǳ�����ɫ
		screencheck_image = (ImageView) this.findViewById(R.id.screencheck_image);
		if (screen_sleep == true) {
			screencheck_image.setBackgroundResource(R.drawable.bg_black);
		} 
		// ��������ѡ����
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		if (isburning == true) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					//��ʼ��������
					isSleepExit = false;
					editor.putString("screen", "ok");
					editor.commit();
					ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_BUZZER);
					finish();
				}
			}, 3000);
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}

	@Override
	public void onClick(View screencheckClick) {
		isSleepExit = false;
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			screencheck_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			switch (screencheckClick.getId()) {
			case R.id.back: {
				//����������
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(ScreenCheckActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.clearActivity();
					ActivityManagers.trunToEntryActivity(ScreenCheckActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(ScreenCheckActivity.this);
				} 
				break;
			}
			//����ͨ��
			case R.id.pass: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("screen", "ok");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(ScreenCheckActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("screen", "ok");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(ScreenCheckActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(ScreenCheckActivity.this);
				} 
				break;
			}
			//����ʧ��
			case R.id.fail: {
				if(config.getBoolean("singletest", false) == true) {
					editor.putString("screen", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(ScreenCheckActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("screen", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(ScreenCheckActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(ScreenCheckActivity.this);
				} 
				break;
			}
			//���²���
			case R.id.test: {
				/* ���²���һ�� */
				Intent intent = new Intent(ScreenCheckActivity.this, ScreenActivity.class);
				startActivity(intent);
				break;
			}
			default:
				break;
			}
		}
	}

	
	private boolean isSleepExit = true;
	@Override
	public void onStop() {
		super.onStop();
		FyLog.i(TAG, "onStop()");
		if(!isSleepExit){
			finish();
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
	}
	/************************** �¼����������� ***************************/
	@SuppressWarnings("deprecation")
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
					editor.putBoolean("burn", false);
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
				screencheck_image.setBackgroundResource(R.drawable.bg_transport);
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
/* ------------------------------------------------------------------------------------------------------------*/
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
