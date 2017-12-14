package com.witsi.setting.hardwaretest;


import java.io.UnsupportedEncodingException;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

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
import android.witsi.arq.ArqConverts;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.witsi.arq.ArqMisc;

public class VersionActivity extends Activity implements
		android.view.View.OnClickListener {
	private final String TAG = "VersionActivity";
	private Context context = VersionActivity.this;
	
	private Button button_version_return;
	private Button button_version_ok;
	private Button button_version_false;
	private Button button_version_test;
	private Button button_rdp;
	private TextView tvVersion;
	private TextView t1_version_value_hardware;
	private TextView rdp_version_value;
	ImageView version_image;
	private View main;
	
	HexCodec mHexCodec;
	private ArqMisc miscArq;
	private Handler handler = new Handler();
	
	byte[] rdp;
	String rdpdata;
	int isok = 99;
	boolean screen_sleep = false;
	public int flag_version = -1;
	boolean isburning = false;
	boolean version_test_ok = false;
	boolean timer_out = false;
	int datachangeok = 1;

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
		FyLog.i(TAG, "进入版本号测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_version_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_version_activity);
			screen_sleep = false;
		}
		
		mHexCodec = new HexCodec(); 
		miscArq = new ArqMisc(this);
		t1_version_value_hardware = (TextView)this.findViewById(R.id.t1_version_value_hardware);
		tvVersion = (TextView) findViewById(R.id.t1_version_value);
		rdp_version_value = (TextView) findViewById(R.id.rdp_version_value);
		View v = findViewById(R.id.ll_tool);
		button_version_return = (Button) v.findViewById(R.id.back);
		button_version_ok = (Button) v.findViewById(R.id.pass);
		button_version_false = (Button) v.findViewById(R.id.fail);
		button_version_test = (Button) v.findViewById(R.id.test);
		button_rdp = (Button) this.findViewById(R.id.button_rdp);
		button_version_return.setOnClickListener(VersionActivity.this);
		button_version_ok.setOnClickListener(VersionActivity.this);
		button_version_false.setOnClickListener(VersionActivity.this);
		button_version_test.setOnClickListener(VersionActivity.this);
		button_version_test.setText("复位");
		button_rdp.setOnClickListener(VersionActivity.this);
		tvVersion.addTextChangedListener(textWatcher);
		version_image = (ImageView) this.findViewById(R.id.version_image);
		if (screen_sleep == true) {
			version_image.setBackgroundResource(R.drawable.bg_black);
		}
		// 给拷机做自动测试
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
	}

	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		if (isburning == true) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					timer_out = true;
					if (isburning == true) {
						isSleepExit = false;
						if (version_test_ok == true) {
							editor.putString("version", "ok");
						} else {
							editor.putString("version", "ng");
							editor.putInt("error_version", config.getInt("error_version", 0) + 1);
							editor.putInt("error", 1);
						}
						editor.commit();
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_TF);
						finish();
					}
				}
			}, 4000);
		}
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				FyLog.i("版本号界面", "尝试获取版本号");
				int ret;
				byte[] version = new byte[32];

				ret = miscArq.getSystemInfo(0x01, version);
				if (ret <= 0) {
					FyLog.e(TAG, "get security status failed. ret = " + ret);
					tvVersion.setText("get version error.");
				} else {
					byte[] tmp_buf = new byte[ret];
					for (int i = 0; i < ret; i++)
						tmp_buf[i] = version[i];
					tvVersion.setText(ArqConverts.asciiBytesToString(tmp_buf));

				}	
				ret = miscArq.getSystemInfo(0x03, version);
				if (ret <= 0) {
					FyLog.e(TAG, "get security status failed. ret = " + ret);
					t1_version_value_hardware.setText("get version error.");
				} else {
					byte[] tmp_buf = new byte[ret];
					for (int i = 0; i < ret; i++)
						tmp_buf[i] = version[i];
					t1_version_value_hardware.setText(harewardVersionTranslate(tmp_buf));
				}
				//T1系统才有打开
				rdp = new byte[50];
				isok = miscArq.getRdpVer(rdp);
				FyLog.i("isok", "" + isok);
				if (isok >= 0) {
					try {
						rdpdata = new String(rdp, "GBK");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rdp_version_value.setText(rdpdata);
				}
			}
		}, 300);
	}
	
	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (timer_out == false) {
				version_test_ok = true;
			}
			flag_version = 1;
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
	public void onClick(View versionClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			version_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (versionClick.getId()) {
			//T1系统才有打开
			case R.id.button_rdp: {
				rdp = new byte[50];
				isok = miscArq.getRdpVer(rdp);
				FyLog.i("isok", "" + isok);
				if (isok >= 0) {
					try {
						rdpdata = new String(rdp, "GBK");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rdp_version_value.setText(rdpdata);
				}
				break;
			}
			case R.id.back: {
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(VersionActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.clearActivity();
					ActivityManagers.trunToEntryActivity(VersionActivity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(VersionActivity.this);
				}  
				break;
			}
			case R.id.pass: {
				if(flag_version == 1){
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("version", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(VersionActivity.this);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("version", "ok");
						editor.commit();
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(VersionActivity.this);
					}else {
						ActivityManagers.trunToBurnStartActivity(VersionActivity.this);
					} 
				}
				break;
			}
			case R.id.fail: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("version", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(VersionActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("version", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(VersionActivity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(VersionActivity.this);
				} 
				break;
			}
			case R.id.test: {
				// /* 重新测试一次 */
				rdp_version_value.setText("");
				t1_version_value_hardware.setText("");
				tvVersion.setText("");
				 break;
			}
			default:
				break;
			}
		}
	}

	public void getVersionBtnOnClickHandler(View source) {
		int ret;
		byte[] version = new byte[32];

		ret = miscArq.getSystemInfo(0x01, version);
		if (ret <= 0) {
			FyLog.e(TAG, "get security status failed. ret = " + ret);
			tvVersion.setText("get version error.");
		} else {
			byte[] tmp_buf = new byte[ret];
			for (int i = 0; i < ret; i++)
				tmp_buf[i] = version[i];
			tvVersion.setText(ArqConverts.asciiBytesToString(tmp_buf));
		
		}
		ret = miscArq.getSystemInfo(0x03, version);
		FyLog.i("version", "ret:"+ret);
		if (ret <= 0) {
			FyLog.e(TAG, "get security status failed. ret = " + ret);
			t1_version_value_hardware.setText("get version error.");
		} else {
			byte[] tmp_buf = new byte[ret];
			for (int i = 0; i < ret; i++){
				FyLog.i("version", "i:"+i+"________"+tmp_buf[i]);
				tmp_buf[i] = version[i];}
			FyLog.i("version", "version:"+mHexCodec.hexEncode(tmp_buf));
			FyLog.i("version", "version:"+harewardVersionTranslate(tmp_buf));
			t1_version_value_hardware.setText(harewardVersionTranslate(tmp_buf));
		
		}
		
	}
	public String harewardVersionTranslate(byte[] tmp_buf){
		String versionData = "";
		if(tmp_buf[0]!=0)
			versionData = versionData+String.valueOf((int)(tmp_buf[0]&0x0f));
		for(int i = 1;i<tmp_buf.length;i++){
			versionData = versionData+String.valueOf((int)(tmp_buf[i]&0x0f));
		}
		return versionData;
	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
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
				version_image.setBackgroundResource(R.drawable.bg_transport);
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
