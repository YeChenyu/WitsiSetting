package com.witsi.setting.hardwaretest;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Button;
import android.widget.ImageView;
import android.witsi.arq.ArqMisc;


public class ShakeActivity extends Activity implements
		android.view.View.OnClickListener {
	private String TAG = "BuzzerActivity";
	private Context context = ShakeActivity.this;
	
	private Button button_Buzzer_return;
	private Button button_Buzzer_ok;
	private Button button_Buzzer_false;
	private Button button_Buzzer_test;
	private ImageView buzzer_image;
	
	private ArqMisc miscArq;
	private MyRunnable myrun = new MyRunnable();
	
	public int falg_buzzer = -1;
	private boolean screen_sleep = false;
	private boolean isburning = false;
	
	private SharedPreferences config;
	private Editor editor;
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		setContentView(R.layout.hardware_shake_activity);
		FyLog.i(TAG, "�������Ӳ��Խ���");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		// SysApplication.getInstance().addActivity(this);
		View v = findViewById(R.id.ll_tool);
		button_Buzzer_return = (Button) v.findViewById(R.id.back);
		button_Buzzer_ok = (Button) v.findViewById(R.id.pass);
		button_Buzzer_false = (Button) v.findViewById(R.id.fail);
		button_Buzzer_test = (Button) v.findViewById(R.id.test);
		button_Buzzer_return.setOnClickListener(ShakeActivity.this);
		button_Buzzer_ok.setOnClickListener(ShakeActivity.this);
		button_Buzzer_false.setOnClickListener(ShakeActivity.this);
		button_Buzzer_test.setOnClickListener(ShakeActivity.this);
		miscArq = new ArqMisc(this);
		//��Ļ����
		buzzer_image = (ImageView) this.findViewById(R.id.buzzer_image);
		if (config.getBoolean("light", true) == false) {
			buzzer_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		// ֱ�ӿ�������
		FyLog.i(TAG, "<<<<<<<<< Buzzer on. >>>>>>>>>>>");
		if (myrun.isThreadRunnig() == false) {
			Thread thread = new Thread(myrun);
			thread.start();
		}
		// ��������ѡ����
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		if (isburning == true) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					isSleepExit = false;
					if(falg_buzzer >= 0){
						editor.putString("buzzer", "ok");
					}else{
						editor.putString("buzzer", "ng");
					}
					editor.commit();
					ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SERIALNUMBER);
				}
			}, 3000);
		}
	}

	@Override
	public void onClick(View BuzzerClick) {
		if (screen_sleep == true) {
			buzzer_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
			toggleBrightness(this, 200);// ����
		} else {
			isburning = false;
			isSleepExit = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			switch (BuzzerClick.getId()) {
			case R.id.back: {
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(ShakeActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(ShakeActivity.this);
				}else{
					ActivityManagers.trunToBurnStartActivity(ShakeActivity.this);
				} 
				break;
			}
			case R.id.pass: {
				falg_buzzer = 1;
				if (config.getBoolean("singletest", false) == true){
					editor.putString("buzzer", "ok");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(ShakeActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("buzzer", "ok");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(ShakeActivity.this);
				}else{
					ActivityManagers.trunToBurnStartActivity(ShakeActivity.this);
				} 
				break;
			}
			case R.id.fail: {
				falg_buzzer = -1;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("buzzer", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(ShakeActivity.this);
				} else if (config.getBoolean("alltest", false) == true){
					editor.putString("buzzer", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(ShakeActivity.this);
				}else{
					ActivityManagers.trunToBurnStartActivity(ShakeActivity.this);
				} 
				break;
			}
			case R.id.test: {
				// /* ���²���һ�� */
				if (myrun.isThreadRunnig() == false) {
					Thread thread = new Thread(myrun);
					thread.start();
				}
				 break;
			}
			default:
				break;
			}
		}
	}

	public void buzzerStartBtnOnClickHandler(View source) {
		if (myrun.isThreadRunnig() == false) {
			Thread thread = new Thread(myrun);
			thread.start();
		}
		FyLog.i(TAG, "<<<<<<<<< Buzzer on. >>>>>>>>>>>");
	}

	public void buzzerStopBtnOnClickHandler(View source) {
		if (myrun.isThreadRunnig() == true) {
			myrun.stopThread();
		}
		super.onPause();
		FyLog.i(TAG, "<<<<<<<<< Buzzer off. >>>>>>>>>>>");
	}

	class MyRunnable implements Runnable {
		private boolean threadRunning = false;

		public void stopThread() {
			threadRunning = false;
		}

		public boolean isThreadRunnig() {
			return threadRunning;
		}

		public void run() {
			FyLog.i(TAG, "buzzer thread working.");
			int repeat, on, off;

			repeat = 1;
			on = 2000;
			off = 1000;

			threadRunning = true;

			while (threadRunning) {
				falg_buzzer = miscArq.buzzerCtl(repeat, on, off);
				if (falg_buzzer < 0) {
					FyLog.e(TAG, "buzzer control failed. ret = " + falg_buzzer);
					continue;
				}
				/* wait for 0.5s */
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					FyLog.d(TAG, "Waiting didnt work!!");
					e.printStackTrace();
				}
				FyLog.i(TAG, "buzzer control success.");
			}
			threadRunning = false;
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
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "�˳����Ӳ��Խ���");
	}
	
	protected void onPause() {
		FyLog.i(TAG, "###### onPause called. ######");
		if (myrun.isThreadRunnig() == true) {
			myrun.stopThread();
		}
		if (myrun.isThreadRunnig() == true) {
			myrun.stopThread();
		}
		super.onPause();
		FyLog.i(TAG, "<<<<<<<<< Buzzer off. >>>>>>>>>>>");
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
				public void onClick(DialogInterface arg0, int arg1) {
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
				public void onClick(DialogInterface arg0, int arg1) {
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
				buzzer_image.setBackgroundResource(R.drawable.bg_transport);
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