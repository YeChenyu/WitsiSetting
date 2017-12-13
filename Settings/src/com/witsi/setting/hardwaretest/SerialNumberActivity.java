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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.witsi.arq.ArqMisc;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.witsi.arq.ArqConverts;


public class SerialNumberActivity extends Activity implements
		android.view.View.OnClickListener, Callback {
	private final String TAG = "SerialNumberActivity";
	private Context context = SerialNumberActivity.this;
	
	private Button button_serialNumber_return;
	private Button button_serialNumber_ok;
	private Button button_serialNumber_false;
	private Button button_serialNumber_test;
	ImageView serialnumber_image;
	TextView t1_serial_number_value;
	View main = null;
	
	private Handler handler = null;
	private ArqMisc miscArq;
	private Thread thread = null;

	boolean screen_sleep = false;
	boolean isBtnPass = false;
	boolean isAutoPass = false;
	boolean isburning = false;
	boolean serialnumber_test_ok = false;
	boolean timer_out = false;
	int flag_serialnumber = -1;
	
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
		FyLog.i(TAG, "�������кŲ��Խ���");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//����ʱ���ع�����
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// ��װ���ء����ð�~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_serial_number_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_serial_number_activity);
			screen_sleep = false;
		}
		// SysApplication.getInstance().addActivity(this);
		// ��ȡ���к�
		// t1_serial_number_value.setText(android.os.Build.SERIAL);
		miscArq = new ArqMisc(this);
		handler = new Handler(this);
		View v = findViewById(R.id.ll_tool);
		button_serialNumber_return = (Button) v.findViewById(R.id.back);
		button_serialNumber_ok = (Button) v.findViewById(R.id.pass);
		button_serialNumber_false = (Button) v.findViewById(R.id.fail);
		button_serialNumber_test = (Button) v.findViewById(R.id.test);
		t1_serial_number_value = (TextView) this.findViewById(R.id.t1_serial_number_value);
		button_serialNumber_test.setText("��λ");
		Display dis = getWindowManager().getDefaultDisplay();
		t1_serial_number_value.setWidth(dis.getWidth() / 5);
		t1_serial_number_value.setHeight(dis.getHeight() / 5);
		findViewById(R.id.get_serial_number_btn).setOnClickListener(this);
		findViewById(R.id.test).setOnClickListener(this);
		button_serialNumber_return.setOnClickListener(this);
		button_serialNumber_ok.setOnClickListener(this);
		button_serialNumber_false.setOnClickListener(this);
		t1_serial_number_value.addTextChangedListener(textWatcher);
		serialnumber_image = (ImageView) this.findViewById(R.id.serial_number_image);
		if (screen_sleep == true) {
			serialnumber_image.setBackgroundResource(R.drawable.bg_black);
		}
		// ���������Զ�����
		isburning = config.getBoolean("flag_burn", false);
		FyLog.i(TAG, "isburning: " + isburning);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				thread = new Thread(new ReadSNThread());
				thread.start();
			}
		}, 300);
		//��������
		if (isburning == true) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					FyLog.i("���кŽ���", "���ʱ�䵽");
					timer_out = true;
					isSleepExit = false;
					if(serialnumber_test_ok)
						editor.putString("serialnumber", "ok");
					else{
						editor.putString("serialnumber", "ng");
						editor.putInt("error_serialnumber", config.getInt("error_serialnumber", 0) + 1);
						editor.putInt("error", 1);
					}
					editor.commit();
					ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_RESULTTABLE);
					finish();
				}
			}, 3000);
		}
	}

	class ReadSNThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int ret;
			byte[] serialnumber = new byte[32];
			ret = miscArq.getSystemInfo(0x02, serialnumber);
			if (ret <= 0) {
				FyLog.e(TAG, "get security status failed. ret = " + ret);
				t1_serial_number_value.setText("get serialnumber error.");
			} else {
				FyLog.i(TAG, ArqConverts.bytesToHexString(serialnumber) + ":" + ret);
				Message msg = handler.obtainMessage();
				byte[] tmp_buf = new byte[ret];
				for (int i = 0; i < ret; i++)
					tmp_buf[i] = serialnumber[i];
				if("0000000000000000000000000000000000000000000000000000000000000000"
						.equals(ArqConverts.bytesToHexString(serialnumber)))
					msg.obj = "0000000000000000000000000000000000000000000000000000000000000000";
				else
					msg.obj = ArqConverts.asciiBytesToString(serialnumber);
				handler.sendMessage(msg);
				serialnumber = null;
				tmp_buf = null;
			}
		}
	}
	@Override
	public boolean handleMessage(Message arg0) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "the msg is: " + (String)arg0.obj);
		t1_serial_number_value.setText((String)arg0.obj);
		return false;
	}
	
	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (timer_out == false) {
				FyLog.v(TAG, "the serialnumber is:" + s.toString() + ":");
				if(s.toString().length() != 0){
					editor.putString("serialnumber", "ok");
					editor.commit();
					isSleepExit = false;
					serialnumber_test_ok = true;
					if (config.getBoolean("alltest", false) == true){
						if(!isBtnPass){
							isAutoPass = true;
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(SerialNumberActivity.this);
						}
					}
				}
			}
			flag_serialnumber = 1;
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
	public void onClick(View serialnumberClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			serialnumber_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			isSleepExit = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			switch (serialnumberClick.getId()) {
			case R.id.back: {
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(SerialNumberActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(SerialNumberActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(SerialNumberActivity.this);
				}  
				break;
			}
			case R.id.pass: {
				if(flag_serialnumber == 1){
					isBtnPass = true;
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("serialnumber", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(SerialNumberActivity.this);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("serialnumber", "ok");
						editor.commit();
						if(!isAutoPass){
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(SerialNumberActivity.this);
						}
					}else {
						ActivityManagers.trunToBurnStartActivity(SerialNumberActivity.this);
					}  
				}
				break;
			}
			case R.id.fail: {
				isBtnPass = true;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("serialnumber", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(SerialNumberActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("serialnumber", "ng");
					editor.commit();
					if(!isAutoPass){
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(SerialNumberActivity.this);
					}
				}else {
					ActivityManagers.trunToBurnStartActivity(SerialNumberActivity.this);
				}  
				break;
			}
			case R.id.test:{
				t1_serial_number_value.setText("FFFFFFFF");
			}break;
			case R.id.get_serial_number_btn:
				t1_serial_number_value.setText("FFFFFFFF");
				new Thread(new ReadSNThread()).start();
				break;
			default:
				break;
			}
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
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "�˳����кŲ��Խ���");
		if(thread != null){
			thread.interrupt();
			thread = null;
		}
		if(handler != null)
			handler = null;
			
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
				serialnumber_image.setBackgroundResource(R.drawable.bg_transport);
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
