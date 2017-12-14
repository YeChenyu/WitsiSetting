package com.witsi.setting.hardwaretest;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

public class BurnConfigActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = "BurnActivity";
	private Context context = BurnConfigActivity.this;
	private static final boolean D = true;
	
	CheckBox	// checkbox_touchscreen, checkbox_magc,checkbox_printer, checkbox_key,
	checkbox_screen,checkbox_buzzer, checkbox_security,
	checkbox_ic, checkbox_led, checkbox_version, checkbox_gprs,
	checkbox_wifi, checkbox_tf, checkbox_serialnumber;
	TextView burn_dialog;
	View main = null;
	Button button_burn_return_to_home;
	ImageView burn_image;
	
	boolean isburning = false;
	boolean ifCloseActivity = true;
	boolean flag_checkbox_touchscreen = false, flag_checkbox_screen = false,
			flag_checkbox_magc = false, flag_checkbox_printer = false,
			flag_checkbox_buzzer = false, flag_checkbox_security = false,
			flag_checkbox_key = false, flag_checkbox_ic = false,
			flag_checkbox_led = false, flag_checkbox_version = false,
			flag_checkbox_gprs = false, flag_checkbox_wifi = false,
			flag_checkbox_tf = false, flag_checkbox_serialnumber = false;
	boolean screen_sleep = false;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	
	private SharedPreferences config;
	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		 setContentView(R.layout.hardware_burn_config_activity);
		 config = ConfigSharePaference.getSharedPreferences(context);
		 editor = config.edit();
		 
		 registerReceiver(mReceiver,filterHome);
		 // SysApplication.getInstance().addActivity(this);
		 if(D)FyLog.i("����ѡ����������", "����");
		/*********************************************/
		burn_image = (ImageView) this.findViewById(R.id.burn_image);
		burn_dialog = (TextView) this.findViewById(R.id.burn_dialog);
		button_burn_return_to_home = (Button) this.findViewById(R.id.burn_return_home);
		button_burn_return_to_home.setOnClickListener(BurnConfigActivity.this);
//		checkbox_touchscreen = (CheckBox) this.findViewById(R.id.burn_touchscreen_check);
		checkbox_screen = (CheckBox) this.findViewById(R.id.burn_screen_check);
//		checkbox_magc = (CheckBox) this.findViewById(R.id.burn_magc_check);
//		checkbox_printer = (CheckBox) this.findViewById(R.id.burn_printer_check);
		checkbox_buzzer = (CheckBox) this.findViewById(R.id.burn_buzzer_check);
		checkbox_security = (CheckBox) this.findViewById(R.id.burn_security_check);
//		checkbox_key = (CheckBox) this.findViewById(R.id.burn_key_check);
		checkbox_ic = (CheckBox) this.findViewById(R.id.burn_ic_check);
		checkbox_led = (CheckBox) this.findViewById(R.id.burn_led_check);
		checkbox_version = (CheckBox) this.findViewById(R.id.burn_version_check);
		checkbox_gprs = (CheckBox) this.findViewById(R.id.burn_gprs_check);
		checkbox_wifi = (CheckBox) this.findViewById(R.id.burn_wifi_check);
		checkbox_tf = (CheckBox) this.findViewById(R.id.burn_tf_check);
		checkbox_serialnumber = (CheckBox) this.findViewById(R.id.burn_serialnumber_check);
		
		isburning = config.getBoolean("flag_burn", false);
		if(D)FyLog.i(TAG, "the isburning is: " + isburning);
		if (config.getBoolean("light", true) == false) {
			burn_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		
//		checkbox_touchscreen.setChecked(config.getBoolean("flag_checkbox_touchscreen", false));
		checkbox_screen.setChecked(config.getBoolean("flag_checkbox_screen", false));
//		checkbox_magc.setChecked(config.getBoolean("flag_checkbox_magc", false));
//		checkbox_printer.setChecked(config.getBoolean("flag_checkbox_printer", false));
		checkbox_buzzer.setChecked(config.getBoolean("flag_checkbox_buzzer", false));
		checkbox_security.setChecked(config.getBoolean("flag_checkbox_security", false));
//		checkbox_key.setChecked(config.getBoolean("flag_checkbox_key", false));
		checkbox_ic.setChecked(config.getBoolean("flag_checkbox_ic", false));
		checkbox_led.setChecked(config.getBoolean("flag_checkbox_led", false));
		checkbox_version.setChecked(config.getBoolean("flag_checkbox_version", false));
		checkbox_gprs.setChecked(config.getBoolean("flag_checkbox_gprs", false));
		checkbox_wifi.setChecked(config.getBoolean("flag_checkbox_wifi", false));
		checkbox_tf.setChecked(config.getBoolean("flag_checkbox_tf", false));
		checkbox_serialnumber.setChecked(config.getBoolean("flag_checkbox_serialnumber", false));
		
		if (isburning == true) {
			burn_dialog.setText("�����С�");
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					FyLog.i("����ѡ����������", "��ʱ������");
					Intent intent;
					if (config.getBoolean("flag_checkbox_screen", false)) {
						FyLog.i("����ѡ����������", "ѡ�񻵵����");
						intent = new Intent(BurnConfigActivity.this,
								ScreenActivity.class);
					} else if (config.getBoolean("flag_checkbox_ic", false)) {
						FyLog.i("����ѡ����������", "ѡ��IC������");
						intent = new Intent(BurnConfigActivity.this,
								IccActivity.class);
					} else if (config.getBoolean("flag_checkbox_buzzer", false)) {
						FyLog.i("����ѡ����������", "ѡ�����������");
						intent = new Intent(BurnConfigActivity.this,
								BuzzerActivity.class);
					} else if (config.getBoolean("flag_checkbox_security", false)) {
						FyLog.i("����ѡ����������", "ѡ��ȫ״̬����");
						intent = new Intent(BurnConfigActivity.this,
								SecurityActivity.class);
					} else if (config.getBoolean("flag_checkbox_led", false)) {
						FyLog.i("����ѡ����������", "ѡ��LED����");
						intent = new Intent(BurnConfigActivity.this,
								LedActivity.class);
					} else if (config.getBoolean("flag_checkbox_version", false)) {
						FyLog.i("����ѡ����������", "ѡ��汾�Ų���");
						intent = new Intent(BurnConfigActivity.this,
								VersionActivity.class);
					} else if (config.getBoolean("flag_checkbox_wifi", false)) {
						FyLog.i("����ѡ����������", "ѡ��WIFI����");
						intent = new Intent(BurnConfigActivity.this,
								WifiActivity.class);
					} else if (config.getBoolean("flag_checkbox_gprs", false)) {
						FyLog.i("����ѡ����������", "ѡ��GPRS����");
						intent = new Intent(BurnConfigActivity.this,
								GPRS_Activity.class);
					} else if (config.getBoolean("flag_checkbox_tf", false)) {
						FyLog.i("����ѡ����������", "ѡ��TF������");
						intent = new Intent(BurnConfigActivity.this,
								TF_Activity.class);
					} else if (config.getBoolean("flag_checkbox_serialnumber", false)) {
						FyLog.i("����ѡ����������", "ѡ�����кŲ���");
						intent = new Intent(BurnConfigActivity.this,
								SerialNumberActivity.class);
					} else if (config.getBoolean("flag_checkbox_printer", false)) {
						FyLog.i("����ѡ����������", "ѡ���ӡ������");
						intent = new Intent(BurnConfigActivity.this,
								PrinterActivity.class);
					} else {
						FyLog.i("����ѡ����������", "û������ѡ��");
						intent = new Intent(BurnConfigActivity.this,
								ResultTableActivity.class);
					}
					startActivity(intent);
				}
			}, 2000);

		}
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
				editor.putBoolean("flag_checkbox_touchscreen", flag_checkbox_touchscreen);
				editor.putBoolean("flag_checkbox_screen", flag_checkbox_screen);
				editor.putBoolean("flag_checkbox_magc", flag_checkbox_magc);
				editor.putBoolean("flag_checkbox_printer", flag_checkbox_printer);
				editor.putBoolean("flag_checkbox_buzzer", flag_checkbox_buzzer);
				editor.putBoolean("flag_checkbox_security", flag_checkbox_security);
				editor.putBoolean("flag_checkbox_key", flag_checkbox_key);
				editor.putBoolean("flag_checkbox_ic", flag_checkbox_ic);
				editor.putBoolean("flag_checkbox_led", flag_checkbox_led);
				editor.putBoolean("flag_checkbox_version", flag_checkbox_version);
				editor.putBoolean("flag_checkbox_gprs", flag_checkbox_gprs);
				editor.putBoolean("flag_checkbox_wifi", flag_checkbox_wifi);
				editor.putBoolean("flag_checkbox_tf", flag_checkbox_tf);
				editor.putBoolean("flag_checkbox_serialnumber", flag_checkbox_serialnumber);
				editor.commit();
				Intent intent = new Intent(BurnConfigActivity.this, BurnningActivity.class);
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
