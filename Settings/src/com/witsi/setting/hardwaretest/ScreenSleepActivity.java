package com.witsi.setting.hardwaretest;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.witsi.arq.ArqSecurity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenSleepActivity extends Activity implements
		android.view.View.OnClickListener {
	private final String TAG = "SecurityActivity";
	View main = null;
	private ArqSecurity secArq;
	boolean ifCloseActivity = true;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	private Button button_security_return;
	private Button button_security_ok;
	private Button button_security_false;
	private Button button_security_test;
	public int flag_security = -1, datachangeok = 1;
	Intent intent;
	Bundle mybundle;
	boolean screen_sleep = false;
	ImageView security_image;
	boolean isburning = false;
	boolean timer_out = false;
	boolean security_test_ok = false, Security_test_over = false;

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
		ifCloseActivity = true;
		FyLog.i(TAG, "ifCloseActivity="+ifCloseActivity);
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		// ���������Զ�����
		mybundle = new Bundle();
		intent = getIntent();
		mybundle = (Bundle) intent.getExtras();
		mybundle.putString("screen_sleep", "ng");
		if ((Boolean) intent.getSerializableExtra("flag_burn") == true)
			isburning = true;
		else
			isburning = false;
		if (isburning == true) {
			// ��װ���ء����ð�~
			main = getLayoutInflater().from(this).inflate(
					R.layout.hardware_screen_sleep_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_screen_sleep_activity);
		}

		registerReceiver(mReceiver,filterHome);
		// SysApplication.getInstance().addActivity(this);
		FyLog.i("���߽���", "����");
		secArq = new ArqSecurity(this);
		button_security_return = (Button) this
				.findViewById(R.id.securityGetBackToMain);
		button_security_ok = (Button) this
				.findViewById(R.id.security_test_state);
		button_security_false = (Button) this.findViewById(R.id.securityFalse);
		button_security_test = (Button) this.findViewById(R.id.test_security);
		button_security_return.setOnClickListener(ScreenSleepActivity.this);
		button_security_ok.setOnClickListener(ScreenSleepActivity.this);
		button_security_false.setOnClickListener(ScreenSleepActivity.this);
		button_security_test.setOnClickListener(ScreenSleepActivity.this);
		
		security_image = (ImageView) this.findViewById(R.id.security_image);
		if ((Boolean) intent.getSerializableExtra("light") == false) {
			security_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		if ((Boolean) intent.getSerializableExtra("singletest") == false) {
			FyLog.i("Scurity����", "����4�붨ʱ��");
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (Security_test_over == false) {
						if (isburning == true) {
							if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_key")) {
								intent = new Intent(ScreenSleepActivity.this,
										KeyActivity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_ic")) {
								intent = new Intent(ScreenSleepActivity.this,
										IccActivity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_led")) {
								intent = new Intent(ScreenSleepActivity.this,
										LedActivity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_version")) {
								intent = new Intent(ScreenSleepActivity.this,
										VersionActivity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_wifi")) {
								intent = new Intent(ScreenSleepActivity.this,
										WifiActivity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_gprs")) {
								intent = new Intent(ScreenSleepActivity.this,
										GPRS_Activity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_tf")) {
								intent = new Intent(ScreenSleepActivity.this,
										TF_Activity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_serialnumber")) {
								intent = new Intent(ScreenSleepActivity.this,
										SerialNumberActivity.class);
							} else if ((Boolean) intent
									.getSerializableExtra("flag_checkbox_printer")) {
								intent = new Intent(ScreenSleepActivity.this,
										PrinterActivity.class);
							} else {
								intent = new Intent(ScreenSleepActivity.this,
										ResultTableActivity.class);
							}
							intent.putExtras(mybundle);
							FyLog.i("��ȫ״̬����", "�����¸�����");
							startActivity(intent);
						} 
					}
				}
			}, 10);
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
            	finish();  
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
				security_image.setBackgroundResource(R.drawable.lucency);
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
	public void onClick(View securityClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			security_image.setBackgroundResource(R.drawable.lucency);
			screen_sleep = false;
		} else {
			Intent intent;
			Bundle mybundle = new Bundle();
			intent = getIntent();

			mybundle = (Bundle) intent.getExtras();
			isburning = false;
			mybundle.putBoolean("flag_burn", false);
			switch (securityClick.getId()) {
			case R.id.securityGetBackToMain: {
				Security_test_over = true;
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					intent = new Intent(ScreenSleepActivity.this,
							SingleTestActivity.class);
					intent.putExtras(mybundle);
					startActivity(intent);
				} else if ((Boolean) intent.getSerializableExtra("alltest") == true) {
					intent = new Intent(ScreenSleepActivity.this,
							EntryActivity.class);
					intent.putExtras(mybundle);
					startActivity(intent);
				} else {
					intent = new Intent(ScreenSleepActivity.this,
							BurnningActivity.class);
					intent.putExtras(mybundle);
					startActivity(intent);
				}
				break;
			}
			case R.id.security_test_state: {
				flag_security = 1;
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					mybundle.putString("screen_sleep", "ok");
					intent = new Intent(ScreenSleepActivity.this,
					SingleTestActivity.class);
					intent.putExtras(mybundle);
					startActivity(intent);
				} else {
					mybundle.putString("screen_sleep", "ok");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(ScreenSleepActivity.this);
				}
				break;
			}
			case R.id.securityFalse: {
				flag_security = 0;
				if ((Boolean) intent.getSerializableExtra("singletest") ==
				true) {
					mybundle.putString("screen_sleep", "ng");
					intent = new Intent(ScreenSleepActivity.this,
					SingleTestActivity.class);
					intent.putExtras(mybundle);
					startActivity(intent);
				} else {
					mybundle.putString("screen_sleep", "ng");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(ScreenSleepActivity.this);
				}
				break;
			}
			case R.id.test_security: {
				// /* ���²���һ�� */
				getStatusBtnOnClickHandler(securityClick);
			}
			default:
				break;
			}
		}
	}

	public void getStatusBtnOnClickHandler(View source) {
		FyLog.v(TAG, "Screen sleep");
		toggleBrightness(ScreenSleepActivity.this, 0);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				toggleBrightness(ScreenSleepActivity.this, 150);
			}
		}, 1000);
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