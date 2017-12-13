package com.witsi.setting.hardwaretest;


import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.witsi.arq.ArqSecurity;

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

public class SecurityActivity extends Activity implements
		android.view.View.OnClickListener {
	private final String TAG = "SecurityActivity";
	private Context context = SecurityActivity.this;
	
	private TextView tvStatus;
	private Button button_security_return;
	private Button button_security_ok;
	private Button button_security_false;
	private Button button_security_test;
	private ImageView security_image;
	private View main;
	
	private ArqSecurity secArq;
	
	public int flag_security = -1, datachangeok = 1;
	boolean screen_sleep = false;
	boolean isburning = false;
	boolean isBtnPass = false;
	boolean isAutoPass = false;
	boolean security_test_ok = false, Security_test_over = false;
	private String securitydata;
	private Handler handler = new Handler();
	
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
		FyLog.i(TAG, "���밲ȫ״̬���Խ���");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//����ʱ���ع�����
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// ��װ���ء����ð�~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_security_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_security_activity);
			screen_sleep = false;
		}
		// SysApplication.getInstance().addActivity(this);
		secArq = new ArqSecurity(this);
		tvStatus = (TextView) findViewById(R.id.security_status_value);
		View v = findViewById(R.id.ll_tool);
		button_security_return = (Button) v.findViewById(R.id.back);
		button_security_ok = (Button) v.findViewById(R.id.pass);
		button_security_false = (Button) v.findViewById(R.id.fail);
		button_security_test = (Button) v.findViewById(R.id.test);
		button_security_return.setOnClickListener(SecurityActivity.this);
		button_security_ok.setOnClickListener(SecurityActivity.this);
		button_security_false.setOnClickListener(SecurityActivity.this);
		button_security_test.setOnClickListener(SecurityActivity.this);
		button_security_test.setText("��λ");
		tvStatus.addTextChangedListener(textWatcher);
		//��Ļ����
		security_image = (ImageView) this.findViewById(R.id.security_image);
		if (screen_sleep == true) {
			security_image.setBackgroundResource(R.drawable.bg_black);
		} 
		// ���������Զ�����
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
	}

	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				FyLog.i("��ȫ״̬����", "���Ի�ȡ��ȫ״̬");
				int ret;
				String[] status = new String[1];
				ret = secArq.getStatus(status);
				if (ret < 0) {
					FyLog.e(TAG, "get security status failed. ret = " + ret);
					tvStatus.setText("get status error.");
				} else {
					tvStatus.setText(status[0]);
				}
			}
		}, 1000);
	}
	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			securitydata = tvStatus.getText().toString();
			FyLog.i("Security", securitydata);
			if (securitydata.equals("00000000")) {
				security_test_ok = true;
				editor.putString("security", "ok");
				editor.commit();
			} else {
				security_test_ok = false;
				editor.putString("security", "ng");
				editor.putInt("error_security", config.getInt("error_security", 0) + 1);
				editor.putInt("error", 1);
				editor.commit();
			}
			isSleepExit = false;
			if(isburning == true){
				ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_IC);
				finish();
			}else if (config.getBoolean("alltest", false) == true) {
				if(!isBtnPass){
					isAutoPass = true;
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(SecurityActivity.this);
				}
			}
			flag_security = 1;
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
		}
	};
	
	@Override
	public void onClick(View securityClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			security_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			//ȡ������״̬
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (securityClick.getId()) {
			case R.id.back: {
				Security_test_over = true;
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(context);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.clearActivity();
					ActivityManagers.trunToEntryActivity(context);
				}  else {
					ActivityManagers.trunToBurnStartActivity(SecurityActivity.this);
				} 
				break;
			}
			case R.id.pass: {
				if(flag_security == 1){
					isBtnPass = true;
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("security", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(context);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("security", "ok");
						editor.commit();
						if(!isAutoPass){
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(SecurityActivity.this);
						}
					} else {
						ActivityManagers.trunToBurnStartActivity(SecurityActivity.this);
					} 
				}
				break;
			}
			case R.id.fail: {
				flag_security = 0;
				isBtnPass = true;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("security", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(context);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("security", "ng");
					editor.commit();
					if(!isAutoPass){
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(SecurityActivity.this);
					}
				} else {
					ActivityManagers.trunToBurnStartActivity(SecurityActivity.this);
				} 
				break;
			}
			case R.id.test: {
				// /* ���²���һ�� */
				tvStatus.setText("FFFFFFFF");
			}
			default:
				break;
			}
		}
	}

	public void getStatusBtnOnClickHandler(View source) {
		int ret;
		String[] status = new String[1];

		ret = secArq.getStatus(status);
		if (ret < 0) {
			FyLog.e(TAG, "get security status failed. ret = " + ret);
			tvStatus.setText("get status error.");
		} else {
			tvStatus.setText(status[0]);
		}
	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "�˳���ȫ״̬���Խ���");
		if(handler != null)
			handler = null;
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
				security_image.setBackgroundResource(R.drawable.bg_transport);
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