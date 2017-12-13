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
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		FyLog.i(TAG, "进入安全状态测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// 假装隐藏……好吧~
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
		button_security_test.setText("复位");
		tvStatus.addTextChangedListener(textWatcher);
		//屏幕休眠
		security_image = (ImageView) this.findViewById(R.id.security_image);
		if (screen_sleep == true) {
			security_image.setBackgroundResource(R.drawable.bg_black);
		} 
		// 给拷机做自动测试
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
				FyLog.i("安全状态界面", "尝试获取安全状态");
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
			toggleBrightness(this, 200);// 背光
			security_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			//取消拷机状态
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
				// /* 重新测试一次 */
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
		FyLog.i(TAG, "退出安全状态测试界面");
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
				security_image.setBackgroundResource(R.drawable.bg_transport);
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