package com.witsi.setting.hardwaretest;

import java.util.Timer;

import com.witsi.debug.FyLog;
import com.witsi.setting.hardwaretest.PingThread.ThreadListener;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
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

public class GPRS_Activity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = GPRS_Activity.class.getSimpleName();
	private static final boolean D = true;
	private Context context = GPRS_Activity.this;
	
	View main = null;
	private Button button_GPRS_return;
	private Button button_GPRS_ok;
	private Button button_GPRS_false;
	private Button button_GPRS_test;
	private Button button_GPRS_paremeter;
	private TextView gprs_ok, gprs_exist;
	ImageView gprs_image;
	ImageView pictureShow;
	TextView signalStrengthShow, baseStationShow, GPRS_Client_Show;
	
	public TelephonyManager mTelephonyManager;
	MyPhoneStateListener mMyPhoneStateListener;
	private WifiAdmin wifiAdmin;
	// 定义与服务器通信的子线程
	PingThread mPingThread;
	ThreadListener tListener;
	Timer timer = new Timer();
	Handler handler;
	Handler timerHandler;
	
	boolean screen_sleep = false;
	boolean isburning = false;
	boolean isBtnPass = false;
	boolean gprs_test_over = false;
	boolean timer_out = false;
	boolean card_exit = false;
	int i = 0;
	int timers = 0;
	public int flag_GPRS = -1;
	int datachangeok = 1;
	public int mcc, mnc;
	
	private StringBuffer GPRS_ShowListBuffer = new StringBuffer();
	private int test_cnt = 0;
	
	private SharedPreferences config;
	private Editor editor;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		if(D)FyLog.i(TAG, "PGRS界面正常进入");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		wifiAdmin = new WifiAdmin(GPRS_Activity.this);
		//自动测试成功
		if (config.getBoolean("alltest", false) == true) {
			if(GprsTest.isGprsTestSucc){
				editor.putString("gprs", "ok");
				editor.commit();
				flag_GPRS = 1;
			}
		}
		//获取拷机状态
		isburning = config.getBoolean("flag_burn", false);
		if (isburning == true) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.gprs_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.gprs_activity);
		}
		//开启移动网络
		WifiActivity.setMobileData(this, true);
		/*****************************************************************/
		View v = findViewById(R.id.ll_tool);
		button_GPRS_return = (Button) v.findViewById(R.id.back);
		button_GPRS_ok = (Button) v.findViewById(R.id.pass);
		button_GPRS_false = (Button) v.findViewById(R.id.fail);
		button_GPRS_test = (Button) v.findViewById(R.id.test);
		button_GPRS_paremeter = (Button) this.findViewById(R.id.gprs_parameter);
		button_GPRS_return.setOnClickListener(GPRS_Activity.this);
		button_GPRS_ok.setOnClickListener(GPRS_Activity.this);
		button_GPRS_false.setOnClickListener(GPRS_Activity.this);
		button_GPRS_test.setOnClickListener(GPRS_Activity.this);
		button_GPRS_paremeter.setOnClickListener(GPRS_Activity.this);
		signalStrengthShow = (TextView) this.findViewById(R.id.signalstrength_show);
		gprs_ok = (TextView) this.findViewById(R.id.gprs_ok);
		gprs_exist = (TextView) this.findViewById(R.id.gprs_exist);
		baseStationShow = (TextView) this.findViewById(R.id.baseStation_show);
		GPRS_Client_Show = (TextView) this.findViewById(R.id.networkTest_show);
		
		Display dis = getWindowManager().getDefaultDisplay();
		baseStationShow.setWidth(dis.getWidth() / 2);
		baseStationShow.setHeight(dis.getHeight() / 2);
		GPRS_Client_Show.setWidth(dis.getWidth() / 2);
		GPRS_Client_Show.setHeight(dis.getHeight() / 2);
		
		gprs_image = (ImageView) this.findViewById(R.id.gprs_image);
		if (config.getBoolean("light", true) == false) {
			gprs_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		FyLog.i(TAG, "开启20秒定时器");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				FyLog.i(TAG, "检测时间到");
				FyLog.e(TAG, "ping is Success: " + mPingThread.isPingOK);
				if (gprs_test_over == false) {
					timer_out = true;
					FyLog.i(TAG,"GPRS开启socket线程");
					GPRS_Client_Show.append("测试失败：超时");
				}
			}
		}, 30000);

		FyLog.i(TAG, "GPRS进入数据收集阶段");
		tListener = new ThreadListener() {
			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				FyLog.e(TAG, "on Thread Complete");
				// 客户端启动ClientThread线程创建网络连接、读取来自服务器的数据
				if(!PingThread.isPingOK){
					try {
						Thread.sleep(1000);
						FyLog.e(TAG, "第" + test_cnt++ + "测试失败,继续连接");
						mPingThread = new PingThread("www.baidu.com", 1, handler, tListener);
						new Thread(mPingThread).start(); 
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// 如果消息来自于子线程,将读取的内容追加显示在文本框中
				if (msg.what == 0x123) {
					GPRS_Client_Show.append("\n");
					GPRS_Client_Show.append(msg.obj.toString());
					FyLog.e(TAG, "the handler msg is: " + msg.obj.toString());
				}
			}
		};
		
		timerHandler = new Handler() // ①
		{
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0x1111) {
					GPRS_Client_Show.append("\n" + "网络服务器连接：正常" + "\n"
							+ "建立时间：" + "             " + msg.obj.toString()
							+ "ms");
				}
			}
		};
		FyLog.i(TAG, "GPRS准备好socket线程的接收");
		FyLog.i(TAG, "GPRS进入电话数据收集阶段");
		/* 监听是否接收到数据 */
		GPRS_Client_Show.addTextChangedListener(textWatcher);
		/* 用电话管理员来监听电话信号强度 */
		mMyPhoneStateListener = new MyPhoneStateListener();
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mMyPhoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		mTelephonyManager.listen(new TelLocationListener(),
				PhoneStateListener.LISTEN_CELL_LOCATION);
		//获取GPRS参数 点击查看GPRS参数按钮可以看到
		GPRS_ShowListBuffer = GPRS_ShowListBuffer.append("GPRS参数：")
				.append("\n");
		try {
			FyLog.i(TAG, "开始手机数据！！！");
			/** 获得电话管理员 */
			card_exit = true;
			TelephonyManager tm = (TelephonyManager) this
					.getSystemService(TELEPHONY_SERVICE);
			GPRS_ShowListBuffer.append("CCID：").append(tm.getSimSerialNumber())
					.append("\n");
			FyLog.i(TAG, "提示卡已插入");
			gprs_exist.setText("卡已插入");
			GPRS_ShowListBuffer.append("IMSI：").append(tm.getSubscriberId())
					.append("\n");
			GPRS_ShowListBuffer.append("IMEI：").append(tm.getDeviceId())
					.append("\n");
			GPRS_ShowListBuffer.append("本机电话号码：").append(tm.getLine1Number())
					.append("\n");
			GPRS_ShowListBuffer.append("设备的软件版本号IMEI：")
					.append(tm.getDeviceSoftwareVersion()).append("\n");
			GPRS_ShowListBuffer.append("基站信息MCC+MNC：")
					.append(tm.getNetworkOperator()).append("\n");
			String Operator = tm.getNetworkOperator();
			
			mcc = Integer.valueOf(Operator.substring(0, 3));
			mnc = Integer.valueOf(Operator.substring(3, 5));
			switch (mnc) {
			case 00:
				GPRS_ShowListBuffer.append("运营商：中国移动").append("\n");
				break;
			case 01:
				GPRS_ShowListBuffer.append("运营商：中国联通").append("\n");
				break;
			case 10:
				GPRS_ShowListBuffer.append("运营商：中国电信").append("\n");
				break;
			default:
				GPRS_ShowListBuffer.append("运营商：获取失败").append("\n");
				break;
			}
			switch (tm.getPhoneType()) {
			case 0:
				GPRS_ShowListBuffer.append("手机类型：").append("PHONE_TYPE_NONE")
						.append("\n");
				break;
			case 1:
				GPRS_ShowListBuffer.append("手机类型：")
						.append("PHONE_TYPE_GSM GSM").append("\n");
				break;
			case 2:
				GPRS_ShowListBuffer.append("手机类型：")
						.append("PHONE_TYPE_CDMA CDMA").append("\n");
				break;
			default:
				break;
			}
			GPRS_ShowListBuffer.append("ISO国家码：").append(tm.getSimCountryIso())
					.append("\n");
			GPRS_ShowListBuffer.append("MCC+MNC：").append(tm.getSimOperator())
					.append("\n");
			GPRS_ShowListBuffer.append("******************************");
			GPRS_Client_Show.setText(GPRS_ShowListBuffer.toString());
			FyLog.i(TAG, "手机数据收集结束");
		} catch (Exception e) {
			FyLog.e(TAG, "提示无卡插入");
			gprs_exist.setText("无卡插入");
			gprs_ok.setText("失败");
			card_exit = false;
		}

	}

	

	/* Called when the application resumes */
	@Override
	protected void onResume() {
		super.onResume();
		FyLog.i(TAG, "onResume");
		// 这里设置自动开启数据对接
		new Thread(r).start();
	}

	private Runnable r = new Runnable() {
		public void run() {
			//判断wifi状态，直到成功关闭wifi
			while(wifiAdmin.Wifistate() == 1){
				if(wifiAdmin.isWifiConnected(context) == true){
					FyLog.e(TAG, "已连接wifi尝试断开Wifi");
					wifiAdmin.disconnected();
				}else{
					FyLog.e(TAG, "已断开wifi尝试关闭Wifi");
					wifiAdmin.CloseWifi();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			WifiActivity.setMobileData(context, true);
			FyLog.e(TAG, "close the wifi success!");
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					//SIM卡存在
					if (card_exit == true) {
						//单项测试
						gprs_ok.setText("测试中");
						//第一次判断WIFI状态
						if ((wifiAdmin.Wifistate() == 0)) {
							FyLog.i(TAG, "GPRS开启socket线程");
							// 客户端启动ClientThread线程创建网络连接、读取来自服务器的数据
							mPingThread = new PingThread("www.baidu.com",
									1,handler, tListener);
							new Thread(mPingThread).start();
						} 
					}
				}
			}, 1000);
			try {
				mTelephonyManager.listen(mMyPhoneStateListener,
						PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
			} catch (Exception e) {
			}
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		timer.cancel();
	}

	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			FyLog.i(TAG, "文字改变：" + s);
			FyLog.i(TAG, "i=" + i);
			i++;
			
			FyLog.e("gprs", "card_exit=" + card_exit);
			FyLog.e("gprs", "timer_out=" + timer_out);
			FyLog.e("gprs", "s=" + s.toString().contains("丢包率: 0%"));
			if(		card_exit && 
					!timer_out && 
					s.toString().contains("丢包率: 0%")){
				//成功
				gprs_test_over = true;
				test_cnt = 4;
				FyLog.e(TAG, "i=" + i);
				if(i > 9){
					gprs_ok.setText("通过");
					editor.putString("gprs", "ok");
					editor.commit();
					flag_GPRS = 1;
					if (config.getBoolean("alltest", false) == true){
						isSleepExit = false;
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(GPRS_Activity.this);
					}
				}
			}else{
				gprs_test_over = false;
				test_cnt = 0;
				FyLog.e(TAG, "i=" + i);
				if(i > 9){
					if(timer_out){
						gprs_ok.setText("失败");
						editor.putString("gprs", "ng");
						editor.putInt("error", 1);
						editor.commit();
						if (config.getBoolean("alltest", false) == true){
							isSleepExit = false;
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(GPRS_Activity.this);
						}
					}
				}
			}
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
	};

	@Override
	public void onClick(View GPRSClick) {
		if (screen_sleep == true) {
			gprs_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
			toggleBrightness(this, 200);// 背光
		} else {
			isSleepExit = false;
			gprs_test_over = true;
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			switch (GPRSClick.getId()) {
			case R.id.back: {
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(GPRS_Activity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(GPRS_Activity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(GPRS_Activity.this);
				} 
				break;
			}
			case R.id.pass: {
				if(flag_GPRS == 1){
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("gprs", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(GPRS_Activity.this);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("gprs", "ok");
						editor.commit();
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(GPRS_Activity.this);
					}else {
						ActivityManagers.trunToBurnStartActivity(GPRS_Activity.this);
					} 
				}
				break;
			}
			case R.id.fail: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("gprs", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(GPRS_Activity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("gprs", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(GPRS_Activity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(GPRS_Activity.this);
				} 
				break;
			}
			case R.id.test: {
				timer_out = false;
				if(wifiAdmin.Wifistate() == 1)
					wifiAdmin.CloseWifi();
				GPRS_Client_Show.setText("");
				
				if (config.getBoolean("singletest", false) == true) {
					gprs_ok.setText("测试中");
					i = 0;
					if (card_exit == true) {
//						gprs_test_over = false;
						FyLog.i(TAG, "GPRS开启socket线程");
						// 客户端启动PingThread线程创建网络连接、读取来自服务器的数据
						mPingThread = new PingThread("www.baidu.com",
								4, handler, tListener);
						new Thread(mPingThread).start(); 
					} else {
						gprs_ok.setText("失败");
					}
				}
				break;
			}
			case R.id.gprs_parameter: {
				new AlertDialog.Builder(GPRS_Activity.this).setTitle("GPRS参数")
						.setMessage(GPRS_ShowListBuffer.toString())
						.setNegativeButton("取消", null).show();
				break;
			}
			default:
				break;
			}
		}
	}

	/* 重写PhoneStateListener */
	private class MyPhoneStateListener extends PhoneStateListener {
		/*
		 * Get the Signal strength from the provider, each tiome there is an
		 * update 从得到的信号强度,每个tiome供应商有更新
		 */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			signalStrengthShow.setText("信号强度："
					+ String.valueOf(signalStrength.getGsmSignalStrength()));
		}

	};/* End of private Class */

	/*
	 * mLabel.setText(String.format("[%s] mcc=%d mnc=%d cid=%d loc=%d",Operator,
	 * mcc,mnc,loc.getCid(),loc.getLac()));
	 */
	public class TelLocationListener extends PhoneStateListener {
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
			try {
				GsmCellLocation loc = (GsmCellLocation) location;
				StringBuffer baseStationSB;
				baseStationSB = new StringBuffer();
				baseStationSB = baseStationSB.append("基站信息：").append("\n");
				baseStationSB = baseStationSB.append("CELLID：")
						.append(String.valueOf(loc.getCid())).append("\n");
				baseStationSB = baseStationSB.append("LAC：")
						.append(String.valueOf(loc.getLac())).append("\n");
				baseStationSB = baseStationSB.append("MCC：")
						.append(String.valueOf(mcc)).append("\n");
				baseStationSB = baseStationSB.append("MNC：")
						.append(String.valueOf(mnc)).append("\n");

				baseStationSB = baseStationSB
						.append("*************************");
				baseStationShow.setText(baseStationSB);
			} catch (Exception e) {

			}
		}
	}
	/* Called when the application is minimized */
	@Override
	protected void onPause() {
		super.onPause();
		try {
			mTelephonyManager.listen(mMyPhoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		} catch (Exception e) {

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
	/************************** 事件监听申明区 ***************************/
	@SuppressWarnings("deprecation")
	//点击返回键填出提示窗口
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // 创建退出对话框  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // 设置对话框标题  
            isExit.setTitle("系统提示");  
            // 设置对话框消息  
            isExit.setMessage("确定要退出吗");  
            // 添加选择按钮并注册监听  
            isExit.setButton("确定", new DialogInterface.OnClickListener() {
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
            isExit.setButton2("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // 显示对话框  
            isExit.show();  
        }  
        return false;  
    }  
	
	/************************** 事件监听申明区 ***************************/
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				gprs_image.setBackgroundResource(R.drawable.bg_transport);
				screen_sleep = false;
				break;
			// 触摸并移动时刻
			case MotionEvent.ACTION_MOVE:
				break;
			// 终止触摸时刻
			case MotionEvent.ACTION_UP:
				break;
			}
		}
		return false;
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

}
