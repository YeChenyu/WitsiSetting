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
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		FyLog.i(TAG, "进入序列号测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// 假装隐藏……好吧~
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
		// 获取序列号
		// t1_serial_number_value.setText(android.os.Build.SERIAL);
		miscArq = new ArqMisc(this);
		handler = new Handler(this);
		View v = findViewById(R.id.ll_tool);
		button_serialNumber_return = (Button) v.findViewById(R.id.back);
		button_serialNumber_ok = (Button) v.findViewById(R.id.pass);
		button_serialNumber_false = (Button) v.findViewById(R.id.fail);
		button_serialNumber_test = (Button) v.findViewById(R.id.test);
		t1_serial_number_value = (TextView) this.findViewById(R.id.t1_serial_number_value);
		button_serialNumber_test.setText("复位");
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
		// 给拷机做自动测试
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
		//拷机配置
		if (isburning == true) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					FyLog.i("序列号界面", "检测时间到");
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
			toggleBrightness(this, 200);// 背光
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
		FyLog.i(TAG, "退出序列号测试界面");
		if(thread != null){
			thread.interrupt();
			thread = null;
		}
		if(handler != null)
			handler = null;
			
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
	
	//以上为点击返回键填出提示窗口	
	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				serialnumber_image.setBackgroundResource(R.drawable.bg_transport);
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
