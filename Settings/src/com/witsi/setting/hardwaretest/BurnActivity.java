package com.witsi.setting.hardwaretest;


import com.witsi.setting1.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class BurnActivity extends Activity implements
		android.view.View.OnClickListener {
	Intent intent;
	Bundle mybundle;
	View main = null;
	Button button_burn_return_to_home;
	ImageView burn_image;
	boolean isburning = false;
	TextView burn_dialog;
	String TAG = "BurnActivity";
	boolean ifCloseActivity = true;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

	CheckBox	// checkbox_touchscreen, checkbox_magc,checkbox_printer, checkbox_key,
			checkbox_screen,checkbox_buzzer, checkbox_security,
			checkbox_ic, checkbox_led, checkbox_version, checkbox_gprs,
			checkbox_wifi, checkbox_tf, checkbox_serialnumber;
	boolean flag_checkbox_touchscreen = false, flag_checkbox_screen = false,
			flag_checkbox_magc = false, flag_checkbox_printer = false,
			flag_checkbox_buzzer = false, flag_checkbox_security = false,
			flag_checkbox_key = false, flag_checkbox_ic = false,
			flag_checkbox_led = false, flag_checkbox_version = false,
			flag_checkbox_gprs = false, flag_checkbox_wifi = false,
			flag_checkbox_tf = false, flag_checkbox_serialnumber = false;
	boolean screen_sleep = false;


@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "����Stop״̬");
		Log.i(TAG, "ifCloseActivity="+ifCloseActivity);
		if (ifCloseActivity == true) {
			Log.i(TAG, "���Խ���finish");
			this.finish();
			Log.i(TAG, "��ִ��finish");
		}
	}
	@Override
	protected void onResume(){
		super.onResume();
		Log.i(TAG, "onResume");
		ifCloseActivity = true;
		Log.i(TAG, "ifCloseActivity="+ifCloseActivity);
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// getLayoutInflater();
//		// ��װ���ء����ð�~
//		main = LayoutInflater.from(this).inflate(R.layout.activity_burn, null);
//		main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//		setContentView(main);
		 setContentView(R.layout.hardware_burn_config_activity);

registerReceiver(mReceiver,filterHome);
		// SysApplication.getInstance().addActivity(this);
		Log.i("����ѡ����������", "����");
		mybundle = new Bundle();
		intent = getIntent();
		mybundle = (Bundle) intent.getExtras();
		/*********************************************/
		burn_image = (ImageView) this.findViewById(R.id.burn_image);
		burn_dialog = (TextView) this.findViewById(R.id.burn_dialog);
		button_burn_return_to_home = (Button) this
				.findViewById(R.id.burn_return_home);
		button_burn_return_to_home.setOnClickListener(BurnActivity.this);
//		checkbox_touchscreen = (CheckBox) this
//				.findViewById(R.id.burn_touchscreen_check);
		checkbox_screen = (CheckBox) this.findViewById(R.id.burn_screen_check);
//		checkbox_magc = (CheckBox) this.findViewById(R.id.burn_magc_check);
//		checkbox_printer = (CheckBox) this
//				.findViewById(R.id.burn_printer_check);
		checkbox_buzzer = (CheckBox) this.findViewById(R.id.burn_buzzer_check);
		checkbox_security = (CheckBox) this
				.findViewById(R.id.burn_security_check);
//		checkbox_key = (CheckBox) this.findViewById(R.id.burn_key_check);
		checkbox_ic = (CheckBox) this.findViewById(R.id.burn_ic_check);
		checkbox_led = (CheckBox) this.findViewById(R.id.burn_led_check);
		checkbox_version = (CheckBox) this
				.findViewById(R.id.burn_version_check);
		checkbox_gprs = (CheckBox) this.findViewById(R.id.burn_gprs_check);
		checkbox_wifi = (CheckBox) this.findViewById(R.id.burn_wifi_check);
		checkbox_tf = (CheckBox) this.findViewById(R.id.burn_tf_check);
		checkbox_serialnumber = (CheckBox) this
				.findViewById(R.id.burn_serialnumber_check);
		if ((Boolean) intent.getSerializableExtra("flag_burn") == true)
			isburning = true;
		else
			isburning = false;
		if ((Boolean) intent.getSerializableExtra("light") == false) {
			burn_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;
//		checkbox_touchscreen.setChecked((Boolean) intent
//				.getSerializableExtra("flag_checkbox_touchscreen"));
		checkbox_screen.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_screen"));
//		checkbox_magc.setChecked((Boolean) intent
//				.getSerializableExtra("flag_checkbox_magc"));
//		checkbox_printer.setChecked((Boolean) intent
//				.getSerializableExtra("flag_checkbox_printer"));
		checkbox_buzzer.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_buzzer"));
		checkbox_security.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_security"));
//		checkbox_key.setChecked((Boolean) intent
//				.getSerializableExtra("flag_checkbox_key"));
		checkbox_ic.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_ic"));
		checkbox_led.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_led"));
		checkbox_version.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_version"));
		checkbox_gprs.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_gprs"));
		checkbox_wifi.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_wifi"));
		checkbox_tf.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_tf"));
		checkbox_serialnumber.setChecked((Boolean) intent
				.getSerializableExtra("flag_checkbox_serialnumber"));
		if (isburning == true) {
			burn_dialog.setText("�����С�");
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Log.i("����ѡ����������", "��ʱ������");
					if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_magc")) {
						Log.i("����ѡ����������", "ѡ��ſ�����");
						intent = new Intent(BurnActivity.this,
								MagcActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_touchscreen")) {
						Log.i("����ѡ����������", "ѡ����������");
						intent = new Intent(BurnActivity.this,
								TouchScreenActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_screen")) {
						Log.i("����ѡ����������", "ѡ�񻵵����");
						intent = new Intent(BurnActivity.this,
								ScreenActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_printer")) {
						Log.i("����ѡ����������", "ѡ���ӡ������");
						intent = new Intent(BurnActivity.this,
								PrinterActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_buzzer")) {
						Log.i("����ѡ����������", "ѡ�����������");
						intent = new Intent(BurnActivity.this,
								BuzzerActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_security")) {
						Log.i("����ѡ����������", "ѡ��ȫ״̬����");
						intent = new Intent(BurnActivity.this,
								SecurityActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_key")) {
						Log.i("����ѡ����������", "ѡ�񰴼�����");
						intent = new Intent(BurnActivity.this,
								KeyActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_ic")) {
						Log.i("����ѡ����������", "ѡ��IC������");
						intent = new Intent(BurnActivity.this,
								IccActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_led")) {
						Log.i("����ѡ����������", "ѡ��LED����");
						intent = new Intent(BurnActivity.this,
								LedActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_version")) {
						Log.i("����ѡ����������", "ѡ��汾�Ų���");
						intent = new Intent(BurnActivity.this,
								VersionActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_wifi")) {
						Log.i("����ѡ����������", "ѡ��WIFI����");
						intent = new Intent(BurnActivity.this,
								WifiActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_gprs")) {
						Log.i("����ѡ����������", "ѡ��GPRS����");
						intent = new Intent(BurnActivity.this,
								GPRS_Activity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_tf")) {
						intent = new Intent(BurnActivity.this,
								TF_Activity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_serialnumber")) {
						Log.i("����ѡ����������", "ѡ�����кŲ���");
						intent = new Intent(BurnActivity.this,
								SerialNumberActivity.class);
					} else {
						Log.i("����ѡ����������", "û������ѡ��");
						intent = new Intent(BurnActivity.this,
								ResultTableActivity.class);
					}
					intent.putExtras(mybundle);
					startActivity(intent);
				}
			}, 2000);

		}
	}
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
			switch (event.getAction()) {
			// ������Ļʱ��
			case MotionEvent.ACTION_DOWN:
				burn_image.setBackgroundResource(R.drawable.lucency);
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
	public void onClick(View burnclick) {
		if (screen_sleep == true) {
			burn_image.setBackgroundResource(R.drawable.lucency);
			screen_sleep = false;
		} else {
			switch (burnclick.getId()) {
			case R.id.burn_return_home: {
				isburning = false;
				// �жϸ�ѡ���״̬
//				if (checkbox_touchscreen.isChecked())
//					flag_checkbox_touchscreen = true;
//				else
//					flag_checkbox_touchscreen = false;
				if (checkbox_screen.isChecked())
					flag_checkbox_screen = true;
				else
					flag_checkbox_screen = false;
//				if (checkbox_magc.isChecked())
//					flag_checkbox_magc = true;
//				else
//					flag_checkbox_magc = false;
//				if (checkbox_printer.isChecked())
//					flag_checkbox_printer = true;
//				else
//					flag_checkbox_printer = false;
				if (checkbox_buzzer.isChecked())
					flag_checkbox_buzzer = true;
				else
					flag_checkbox_buzzer = false;
				if (checkbox_security.isChecked())
					flag_checkbox_security = true;
				else
					flag_checkbox_security = false;
//				if (checkbox_key.isChecked())
//					flag_checkbox_key = true;
//				else
//					flag_checkbox_key = false;
				if (checkbox_ic.isChecked())
					flag_checkbox_ic = true;
				else
					flag_checkbox_ic = false;
				if (checkbox_led.isChecked())
					flag_checkbox_led = true;
				else
					flag_checkbox_led = false;
				if (checkbox_version.isChecked())
					flag_checkbox_version = true;
				else
					flag_checkbox_version = false;
				if (checkbox_gprs.isChecked())
					flag_checkbox_gprs = true;
				else
					flag_checkbox_gprs = false;
				if (checkbox_wifi.isChecked())
					flag_checkbox_wifi = true;
				else
					flag_checkbox_wifi = false;
				if (checkbox_tf.isChecked())
					flag_checkbox_tf = true;
				else
					flag_checkbox_tf = false;
				if (checkbox_serialnumber.isChecked())
					flag_checkbox_serialnumber = true;
				else
					flag_checkbox_serialnumber = false;
				/* ��Ǳ�ѡ��Ҫ���п�������Ŀ */
				mybundle.putBoolean("flag_checkbox_touchscreen",
						flag_checkbox_touchscreen);
				mybundle.putBoolean("flag_checkbox_screen",
						flag_checkbox_screen);
				mybundle.putBoolean("flag_checkbox_magc", flag_checkbox_magc);
				mybundle.putBoolean("flag_checkbox_printer",
						flag_checkbox_printer);
				mybundle.putBoolean("flag_checkbox_buzzer",
						flag_checkbox_buzzer);
				mybundle.putBoolean("flag_checkbox_security",
						flag_checkbox_security);
				mybundle.putBoolean("flag_checkbox_key", flag_checkbox_key);
				mybundle.putBoolean("flag_checkbox_ic", flag_checkbox_ic);
				mybundle.putBoolean("flag_checkbox_led", flag_checkbox_led);
				mybundle.putBoolean("flag_checkbox_version",
						flag_checkbox_version);
				mybundle.putBoolean("flag_checkbox_gprs", flag_checkbox_gprs);
				mybundle.putBoolean("flag_checkbox_wifi", flag_checkbox_wifi);
				mybundle.putBoolean("flag_checkbox_tf", flag_checkbox_tf);
				mybundle.putBoolean("flag_checkbox_serialnumber",
						flag_checkbox_serialnumber);
				intent = new Intent(BurnActivity.this, BurnningActivity.class);
				intent.putExtras(mybundle);
				startActivity(intent);
				break;
			}
			// case R.id.burn_start: {
			// // �жϸ�ѡ���״̬
			// if (checkbox_touchscreen.isChecked())
			// flag_checkbox_touchscreen = true;
			// else
			// flag_checkbox_touchscreen = false;
			// if (checkbox_screen.isChecked())
			// flag_checkbox_screen = true;
			// else
			// flag_checkbox_screen = false;
			// if (checkbox_magc.isChecked())
			// flag_checkbox_magc = true;
			// else
			// flag_checkbox_magc = false;
			// if (checkbox_printer.isChecked())
			// flag_checkbox_printer = true;
			// else
			// flag_checkbox_printer = false;
			// if (checkbox_buzzer.isChecked())
			// flag_checkbox_buzzer = true;
			// else
			// flag_checkbox_buzzer = false;
			// if (checkbox_security.isChecked())
			// flag_checkbox_security = true;
			// else
			// flag_checkbox_security = false;
			// if (checkbox_key.isChecked())
			// flag_checkbox_key = true;
			// else
			// flag_checkbox_key = false;
			// if (checkbox_ic.isChecked())
			// flag_checkbox_ic = true;
			// else
			// flag_checkbox_ic = false;
			// if (checkbox_led.isChecked())
			// flag_checkbox_led = true;
			// else
			// flag_checkbox_led = false;
			// if (checkbox_version.isChecked())
			// flag_checkbox_version = true;
			// else
			// flag_checkbox_version = false;
			// if (checkbox_gprs.isChecked())
			// flag_checkbox_gprs = true;
			// else
			// flag_checkbox_gprs = false;
			// if (checkbox_wifi.isChecked())
			// flag_checkbox_wifi = true;
			// else
			// flag_checkbox_wifi = false;
			// if (checkbox_tf.isChecked())
			// flag_checkbox_tf = true;
			// else
			// flag_checkbox_tf = false;
			// /* ��Ǳ�ѡ��Ҫ���п�������Ŀ */
			// mybundle.putBoolean("flag_checkbox_touchscreen",
			// flag_checkbox_touchscreen);
			// mybundle.putBoolean("flag_checkbox_screen",
			// flag_checkbox_screen);
			// mybundle.putBoolean("flag_checkbox_magc", flag_checkbox_magc);
			// mybundle.putBoolean("flag_checkbox_printer",
			// flag_checkbox_printer);
			// mybundle.putBoolean("flag_checkbox_buzzer",
			// flag_checkbox_buzzer);
			// mybundle.putBoolean("flag_checkbox_security",
			// flag_checkbox_security);
			// mybundle.putBoolean("flag_checkbox_key", flag_checkbox_key);
			// mybundle.putBoolean("flag_checkbox_ic", flag_checkbox_ic);
			// mybundle.putBoolean("flag_checkbox_led", flag_checkbox_led);
			// mybundle.putBoolean("flag_checkbox_version",
			// flag_checkbox_version);
			// mybundle.putBoolean("flag_checkbox_gprs", flag_checkbox_gprs);
			// mybundle.putBoolean("flag_checkbox_wifi", flag_checkbox_wifi);
			// mybundle.putBoolean("flag_checkbox_tf", flag_checkbox_tf);
			// // mybundle.putBoolean("flag_burn", true);
			// if (flag_checkbox_touchscreen) {
			// intent = new Intent(BurnActivity.this,
			// TouchScreenActivity.class);
			// } else if (flag_checkbox_screen) {
			// intent = new Intent(BurnActivity.this, ScreenActivity.class);
			// } else if (flag_checkbox_magc) {
			// intent = new Intent(BurnActivity.this, MagcActivity.class);
			// } else if (flag_checkbox_printer) {
			// intent = new Intent(BurnActivity.this, PrinterActivity.class);
			// } else if (flag_checkbox_buzzer) {
			// intent = new Intent(BurnActivity.this, BuzzerActivity.class);
			// } else if (flag_checkbox_security) {
			// intent = new Intent(BurnActivity.this, SecurityActivity.class);
			// } else if (flag_checkbox_key) {
			// intent = new Intent(BurnActivity.this, KeyActivity.class);
			// } else if (flag_checkbox_ic) {
			// intent = new Intent(BurnActivity.this, IccActivity.class);
			// } else if (flag_checkbox_led) {
			// intent = new Intent(BurnActivity.this, LedActivity.class);
			// } else if (flag_checkbox_version) {
			// intent = new Intent(BurnActivity.this, VersionActivity.class);
			// } else if (flag_checkbox_gprs) {
			// intent = new Intent(BurnActivity.this, GPRS_Activity.class);
			// } else if (flag_checkbox_wifi) {
			// intent = new Intent(BurnActivity.this, WifiActivity.class);
			// } else if (flag_checkbox_tf) {
			// intent = new Intent(BurnActivity.this, TF_Activity.class);
			// }
			// intent.putExtras(mybundle);
			// startActivity(intent);
			// break;
			// }
			default:
				break;
			}
		}
	}
	//���ڽ���
		private BroadcastReceiver mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				
				String action = intent.getAction();
				if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
					ifCloseActivity =false;
					Log.i(TAG, "home������");
				}

			}

		};
}
