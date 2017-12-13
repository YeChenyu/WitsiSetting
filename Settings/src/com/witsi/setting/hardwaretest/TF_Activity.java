package com.witsi.setting.hardwaretest;

import java.io.File;
import java.io.IOException;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Formatter;
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

public class TF_Activity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = "TF_Activity";
	private Context context = TF_Activity.this;
	
	TextView TF_show;
	ImageView tf_image;
	Button button_TF_Return, button_TF_test, button_TF_false, button_TF_ok;
	View main = null;
	
	private FileHelper helper;
	private Handler handler = null;
	
	boolean SDexist = false;
	boolean isburning = false;
	boolean isBtnPass = false;
	boolean isAutoPass = false;
	boolean TF_test_over = false;
	boolean screen_sleep = false;
	int flag_serialnumber = -1;
	private StringBuffer TF_show_SB = new StringBuffer();

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
		FyLog.i(TAG, "onCreate()");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_tf_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_tf_activity);
			screen_sleep = false;
		}
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);

		TF_show = (TextView) this.findViewById(R.id.tf_show);
		Display dis = getWindowManager().getDefaultDisplay();
		TF_show.setWidth(dis.getWidth() / 2);
		TF_show.setHeight(dis.getHeight() / 2);
		handler = new Handler();
		View v = findViewById(R.id.ll_tool);
		button_TF_Return = (Button) v.findViewById(R.id.back);
		button_TF_test = (Button) v.findViewById(R.id.test);
		button_TF_false = (Button) v.findViewById(R.id.fail);
		button_TF_ok = (Button) v.findViewById(R.id.pass);
		button_TF_Return.setOnClickListener(TF_Activity.this);
		button_TF_test.setOnClickListener(TF_Activity.this);
		button_TF_false.setOnClickListener(TF_Activity.this);
		button_TF_ok.setOnClickListener(TF_Activity.this);
		helper = new FileHelper(getApplicationContext());

		tf_image = (ImageView) this.findViewById(R.id.tf_image);
		if (screen_sleep == true) {
			tf_image.setBackgroundResource(R.drawable.bg_black);
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				FyLog.i("TF界面", "检测时间到");
				if (TF_test_over == false) {
					isSleepExit = false;
					editor.putString("tf", "ng");
					editor.putInt("error_tf", config.getInt("error_tf", 0) + 1);
					editor.putInt("error", 1);
					editor.commit();
					if (isburning == true) {
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SERIALNUMBER);
					} else if (config.getBoolean("alltest", false) == true) {
						if(!isBtnPass){
							isAutoPass = true;
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(TF_Activity.this);
						}
					}

				}
			}
		}, 3000);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				isSleepExit = false;
				TF_show_SB = TF_show_SB.append("TF卡参数：").append("\n\r");
				try {
					TF_show_SB.append("创建文件："
							+ helper.createSDFile("test.txt").getAbsolutePath()
							+ "\n\r");
					helper.witeSDFile("test.txt", "ok");
					SDexist = true;
					TF_show_SB.append("SD卡是否存在：  存在" + "\n\r");
				} catch (IOException e) {
					e.printStackTrace();
					SDexist = false;
					TF_show_SB.append("SD卡是否存在：  不存在" + "\n\r");
				}
				if (SDexist) {
					editor.putString("tf", "ok");
					editor.commit();
					FyLog.i("TF界面", "获取TF卡路径");
					TF_show_SB.append("TF卡路径：").append(helper.getSDPATH())
							.append("\n\r");
					TF_show_SB.append("读取文件:" + helper.readSDFile("test.txt")
							+ "\n\r");
					TF_show_SB.append("删除文件是否成功:"
							+ helper.deleteSDFile("test.txt") + "\n\r");
				} else {
					editor.putString("tf", "ng");
					editor.putInt("error", 1);
					editor.commit();
				}
				TF_show.setText(TF_show_SB.toString());
				isSleepExit = false;
				if (isburning == true) {
					if (TF_test_over == false) {
						TF_test_over = true;
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SERIALNUMBER);
					}
				} else if (config.getBoolean("alltest", false) == true) {
					if (TF_test_over == false) {
						TF_test_over = true;
						if(!isBtnPass){
							isAutoPass = true;
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(TF_Activity.this);
						}
					}
				}
				flag_serialnumber = 1;
			}
		}, 800);
	}
	
	/**
	 * 获得SD卡总大小
	 */
	private String getSDTotalSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* totalBlocks);
	}

	/**
	 * 获得sd卡剩余容量，即可用大小
	 */
	private String getSDAvailableSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* availableBlocks);
	}

	/**
	 * 获得机身内存总大小
	 */
	private String getRomTotalSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* totalBlocks);
	}

	/**
	 * 获得机身可用内存
	 */
	private String getRomAvailableSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* availableBlocks);
	}

	/************************** 事件监听申明区 ***************************/
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				tf_image.setBackgroundResource(R.drawable.bg_transport);
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

	@Override
	public void onClick(View ledClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			tf_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (ledClick.getId()) {
			case R.id.back: {
				TF_test_over = true;
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(TF_Activity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(TF_Activity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(TF_Activity.this);
				}  
				break;
			}
			case R.id.test: {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						TF_show_SB = TF_show_SB.append("TF卡参数：").append("\n\r");
						try {
							TF_show_SB.append("创建文件："
									+ helper.createSDFile("test.txt").getAbsolutePath()
									+ "\n\r");
							helper.witeSDFile("test.txt", "ok");
							SDexist = true;
							TF_show_SB.append("SD卡是否存在：  存在" + "\n\r");
						} catch (IOException e) {
							e.printStackTrace();
							SDexist = false;
							TF_show_SB.append("SD卡是否存在：  不存在" + "\n\r");
						}
						if (SDexist) {
							editor.putString("tf", "ok");
							FyLog.i("TF界面", "获取TF卡路径");
							TF_show_SB.append("TF卡路径：").append(helper.getSDPATH())
									.append("\n\r");
							TF_show_SB.append("读取文件:" + helper.readSDFile("test.txt")
									+ "\n\r");
							TF_show_SB.append("删除文件是否成功:"
									+ helper.deleteSDFile("test.txt") + "\n\r");
						} else {
							editor.putString("tf", "ng");
							editor.putInt("error", 1);
						}
						editor.commit();
						isSleepExit = false;
						TF_show.setText(TF_show_SB.toString());
						if (isburning == true) {
							if (TF_test_over == false) {
								TF_test_over = true;
								ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SERIALNUMBER);
							}
						} else if (config.getBoolean("alltest", false) == true) {
							if (TF_test_over == false) {
								TF_test_over = true;
								editor.putString("tf", "ok");
								editor.commit();
								if(!isBtnPass){
									ActivityManagers.trunToNextActivity();
									ActivityManagers.startNextActivity(TF_Activity.this);
								}
							}
						}
					}
				}, 0);
				break;
			}
			case R.id.pass: {
				if(flag_serialnumber == 1){
					isBtnPass = true;
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("tf", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(TF_Activity.this);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("tf", "ok");
						editor.commit();
						if(!isAutoPass){
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(TF_Activity.this);
						}
					}else {
						ActivityManagers.trunToBurnStartActivity(TF_Activity.this);
					} 
				}
				break;
			}
			case R.id.fail: {
				isBtnPass = true;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("tf", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(TF_Activity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("tf", "ng");
					editor.commit();
					if(!isAutoPass){
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(TF_Activity.this);
					}
				}else {
					ActivityManagers.trunToBurnStartActivity(TF_Activity.this);
				} 
				break;
			}
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
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