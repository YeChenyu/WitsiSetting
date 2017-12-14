package com.witsi.setting.hardwaretest;

import java.lang.reflect.Method;

import com.witsi.setting1.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class PasswordActivity extends Activity implements
		android.view.View.OnClickListener {
	String TAG = "PasswordActivity";
	Button num0, num1, num2, num3, num4, num5, num6, num7, num8, num9, pass,
			del, finishactivity;
	Button password1, password2, password3, password4, password5, password6;
	TextView password_ok;
	CheckBox ispasswordshow;
	WifiAdmin mWifiAdmin;
	boolean if_start_wifi_open = true;
	boolean ifCloseActivity = true;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	char[] password = { ' ', ' ', ' ', ' ', ' ', ' ', ' ' };
	char[] keys = { ' ', '3', '0', '2', '3', '0', '2' };
	int i = 1;
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "进入Stop状态");
		Log.i(TAG, "ifCloseActivity="+ifCloseActivity);
		if (ifCloseActivity == true) {
			Log.i(TAG, "尝试进行finish");
			this.finish();
			Log.i(TAG, "已执行finish");
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
		String TAG = "PasswordActivity";
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		setContentView(R.layout.hardware_password_activity);
		
		registerReceiver(mReceiver,filterHome);
//		if(getMobileDataState(this,null)){
//			setMobileData(this,true);
//		}
	
		mWifiAdmin = new WifiAdmin(this);
		if(mWifiAdmin.Wifistate() == 0){
			if_start_wifi_open = false;
		}else{
			if_start_wifi_open = true;
		}
		mWifiAdmin.OpenWifi();
		if(if_start_wifi_open == false){
			new Handler().postDelayed(new Runnable(){	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Log.i("wifi","wifi一开始未打开");
					Log.i("wifi","开始扫描连接");
					mWifiAdmin.StartScan();
					mWifiAdmin.getAndContNoPassWordNet();
				}	
			}, 4500);
		}else{
			Log.i("wifi","wifi一开始已经打开");
			Log.i("wifi", "isWifiConnected="+isWifiConnected(this));
			if(isWifiConnected(this) == false ){
				Log.i("wifi","开始扫描连接");
				mWifiAdmin.StartScan();
				mWifiAdmin.getAndContNoPassWordNet();
			}
		}
		Log.i("wifi", "getMobileDataState="+getMobileDataState(this,null));
		toggleBrightness(this, 200);// 背光
		ispasswordshow = (CheckBox) this.findViewById(R.id.ispasswordshow);
		num0 = (Button) this.findViewById(R.id.num0);
		num1 = (Button) this.findViewById(R.id.num1);
		num2 = (Button) this.findViewById(R.id.num2);
		num3 = (Button) this.findViewById(R.id.num3);
		num4 = (Button) this.findViewById(R.id.num4);
		num5 = (Button) this.findViewById(R.id.num5);
		num6 = (Button) this.findViewById(R.id.num6);
		num7 = (Button) this.findViewById(R.id.num7);
		num8 = (Button) this.findViewById(R.id.num8);
		num9 = (Button) this.findViewById(R.id.num9);
		password1 = (Button) this.findViewById(R.id.password1);
		password2 = (Button) this.findViewById(R.id.password2);
		password3 = (Button) this.findViewById(R.id.password3);
		password4 = (Button) this.findViewById(R.id.password4);
		password5 = (Button) this.findViewById(R.id.password5);
		password6 = (Button) this.findViewById(R.id.password6);
		del = (Button) this.findViewById(R.id.del);
		pass = (Button) this.findViewById(R.id.pass);
		finishactivity = (Button) this.findViewById(R.id.finishactivity);
		password_ok = (TextView) this.findViewById(R.id.password_ok);
		num0.setOnClickListener(PasswordActivity.this);
		num1.setOnClickListener(PasswordActivity.this);
		num2.setOnClickListener(PasswordActivity.this);
		num3.setOnClickListener(PasswordActivity.this);
		num4.setOnClickListener(PasswordActivity.this);
		num5.setOnClickListener(PasswordActivity.this);
		num6.setOnClickListener(PasswordActivity.this);
		num7.setOnClickListener(PasswordActivity.this);
		num8.setOnClickListener(PasswordActivity.this);
		num9.setOnClickListener(PasswordActivity.this);
		password1.setOnClickListener(PasswordActivity.this);
		password2.setOnClickListener(PasswordActivity.this);
		password3.setOnClickListener(PasswordActivity.this);
		password4.setOnClickListener(PasswordActivity.this);
		password5.setOnClickListener(PasswordActivity.this);
		password6.setOnClickListener(PasswordActivity.this);
		del.setOnClickListener(PasswordActivity.this);
		pass.setOnClickListener(PasswordActivity.this);
		ispasswordshow.setOnClickListener(PasswordActivity.this);
		finishactivity.setOnClickListener(PasswordActivity.this);
	}

	@Override
	public void onClick(View passClick) {

		switch (passClick.getId()) {
		case R.id.ispasswordshow: {
			if (ispasswordshow.isChecked() == false) {
				// 不显示密码
				if (i > 1)
					password1.setText("*");
				else
					password1.setText(" ");
				if (i > 2)
					password2.setText("*");
				else
					password2.setText(" ");
				if (i > 3)
					password3.setText("*");
				else
					password3.setText(" ");
				if (i > 4)
					password4.setText("*");
				else
					password4.setText(" ");
				if (i > 5)
					password5.setText("*");
				else
					password5.setText(" ");
				if (i > 6)
					password6.setText("*");
				else
					password6.setText(" ");
			} else {
				// 显示密码
				password1.setText(String.valueOf(password[1]));
				password2.setText(String.valueOf(password[2]));
				password3.setText(String.valueOf(password[3]));
				password4.setText(String.valueOf(password[4]));
				password5.setText(String.valueOf(password[5]));
				password6.setText(String.valueOf(password[6]));
			}
			break;
		}
		case R.id.num0: {
			getpassword('0');
			break;
		}
		case R.id.num1: {
			getpassword('1');
			break;
		}
		case R.id.num2: {
			getpassword('2');
			break;
		}
		case R.id.num3: {
			getpassword('3');
			break;
		}
		case R.id.num4: {
			getpassword('4');
			break;
		}
		case R.id.num5: {
			getpassword('5');
			break;
		}
		case R.id.num6: {
			getpassword('6');
			break;
		}
		case R.id.num7: {
			getpassword('7');
			break;
		}
		case R.id.num8: {
			getpassword('8');
			break;
		}
		case R.id.num9: {
			getpassword('9');
			break;
		}
		case R.id.pass: {
			boolean passok = false;
			for (int j = 1; j <= 6; j++) {
				if (password[j] == keys[j]) {
					passok = true;
				} else {
					passok = false;
					break;
				}
				password_ok.setText("正确");
//				Log.i(TAG, "password:" + password[j] + "," + "keys:" + keys[j]);
//				Log.i(TAG, "j:" + j + "," + "passok:" + passok);
			}
			if (passok == false) {
				password_ok.setText("错误");
				i = 1;
				for (int j = 1; j <= 6; j++) {
					password[j] = ' ';
				}
				password1.setText(String.valueOf(password[1]));
				password2.setText(String.valueOf(password[2]));
				password3.setText(String.valueOf(password[3]));
				password4.setText(String.valueOf(password[4]));
				password5.setText(String.valueOf(password[5]));
				password6.setText(String.valueOf(password[6]));
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						password_ok.setText("");
					}
				}, 1000);
				break;

			} else {
				if(if_start_wifi_open == false){
				new Handler().postDelayed(new Runnable(){	
					@Override
					public void run() {
						Intent intent = new Intent(PasswordActivity.this,
								EntryActivity.class);
						startActivity(intent);
					}	
				}, 2000);}else{
					Intent intent = new Intent(PasswordActivity.this,
							EntryActivity.class);
					startActivity(intent);
				}
				
			}
			break;
		}
		case R.id.del: {
			if (i > 1) {
				i--;
				password[i] = ' ';
			}
			break;
		}
		case R.id.finishactivity: {
			finish();
		}
		default:
			break;
		}
		if (ispasswordshow.isChecked() == false) {
			// 不显示密码
			if (i > 1)
				password1.setText("*");
			else
				password1.setText(" ");
			if (i > 2)
				password2.setText("*");
			else
				password2.setText(" ");
			if (i > 3)
				password3.setText("*");
			else
				password3.setText(" ");
			if (i > 4)
				password4.setText("*");
			else
				password4.setText(" ");
			if (i > 5)
				password5.setText("*");
			else
				password5.setText(" ");
			if (i > 6)
				password6.setText("*");
			else
				password6.setText(" ");
		} else {
			// 显示密码
			password1.setText(String.valueOf(password[1]));
			password2.setText(String.valueOf(password[2]));
			password3.setText(String.valueOf(password[3]));
			password4.setText(String.valueOf(password[4]));
			password5.setText(String.valueOf(password[5]));
			password6.setText(String.valueOf(password[6]));
		}
	}

	public void getpassword(char num) {
		if (i < 7) {
			password[i] = num;
			i++;
			int j = i;
			for (; j <= 6; j++) {
				password[j] = ' ';
			}
		}
	}

	/**
	 * 更改背光亮度
	 * 
	 * @param activity
	 */
	public void toggleBrightness(Activity activity, int light) {
		// 获取亮度值
		int brightness = getScreenBrightness(activity);
		// 是否亮度自动调节，如果是则关闭自动调节
		boolean isAutoBrightness = isAutoBrightness(getContentResolver());
		if (isAutoBrightness) {
			stopAutoBrightness(activity);
		}
		// brightness += 50;// 按自己的需求设置
		// 设置亮度
		setBrightness(activity, light);

		if (brightness > 255) {
			// 亮度超过最大值后设置为自动调节
			startAutoBrightness(activity);
			brightness = 50;// 按自己的需求设置
		}
		// 保存设置状态
		saveBrightness(getContentResolver(), brightness);
	}

	/**
	 * 判断是否开启了自动亮度调节
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
	 * 获取屏幕的亮度
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
	 * 设置亮度
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
	 * 停止自动亮度调节
	 * 
	 * @param activity
	 */
	public void stopAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	/**
	 * 开启亮度自动调节
	 * 
	 * @param activity
	 */
	public void startAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	/**
	 * 保存亮度设置状态
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
	//用于接收
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				ifCloseActivity =false;
				Log.i(TAG, "home键按下");
			}

		}

	};

	public boolean isWifiConnected(Context context) {  
	      if (context != null) {  
	          ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
	                  .getSystemService(Context.CONNECTIVITY_SERVICE);  
	          NetworkInfo mWiFiNetworkInfo = mConnectivityManager  
	                  .getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
	          if (mWiFiNetworkInfo != null) {  
	        	  Log.i("wifi", "mWiFiNetworkInfo="+mWiFiNetworkInfo.toString());
	              return mWiFiNetworkInfo.isConnected();  
	          }  
	     }  
	     return false;  
	 }
	/**
	* 设置手机的移动数据
	*/
	public static void setMobileData(Context pContext, boolean pBoolean) {
	
		try {
	
			ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
	
			Class ownerClass = mConnectivityManager.getClass();
	
			Class[] argsClass = new Class[1];
			argsClass[0] = boolean.class;
	
			Method method = ownerClass.getMethod("setMobileDataEnabled",
					argsClass);
	
			method.invoke(mConnectivityManager, pBoolean);
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("移动数据设置错误: " + e.toString());
		}
	}
	
	/**
	* 返回手机移动数据的状态
	* 
	* @param pContext
	* @param arg
	*            默认填null
	* @return true 连接 false 未连接
	*/
	public static boolean getMobileDataState(Context pContext, Object[] arg) {
	
		try {
	
			ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
	
			Class ownerClass = mConnectivityManager.getClass();
	
			Class[] argsClass = null;
			if (arg != null) {
				argsClass = new Class[1];
				argsClass[0] = arg.getClass();
			}
	
			Method method = ownerClass.getMethod("getMobileDataEnabled",
					argsClass);
	
			Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);
	
			return isOpen;
	
		} catch (Exception e) {
			// TODO: handle exception
	
			System.out.println("得到移动数据状态出错");
			return false;
		}
	
	}
}
