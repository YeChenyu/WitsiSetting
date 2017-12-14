package com.witsi.setting.hardwaretest;

import java.util.Timer;
import java.util.TimerTask;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class ScreenActivity extends Activity {
	
	private String TAG = "ScreenActivity";
	private Context context = ScreenActivity.this;
	
	private Button backgroundButton;
	private Timer showTimer;
	View main = null;
	private Handler  mHandler = null;
	
	boolean isburning = false;
	boolean screen_test_over = false;
	// 定义周期性显示的颜色
	private int[] screen_color_test = new int[] { R.color.black, R.color.red,
			R.color.green, R.color.blue, R.drawable.screen_gray_level,
			R.drawable.screen_gray_level, R.drawable.screen_color_gradation,
			R.drawable.screen_color_gradation, R.drawable.screen_color_gradation,
			R.drawable.screen_color_gradation, R.drawable.screen_color_gradation,
			R.drawable.screen_color_gradation, R.drawable.screen_color_gradation,
			R.drawable.screen_color_gradation, R.drawable.screen_color_gradation,
			R.drawable.screen_color_gradation, R.drawable.screen_color_gradation,
			R.drawable.screen_color_gradation };
	int i = 0;
	
	private SharedPreferences config;
	

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		config = ConfigSharePaference.getSharedPreferences(context);
		//休眠时隐藏工具条
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_screen_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_screen_activity);
		}
		
		
		showTimer = new Timer();
		config = ConfigSharePaference.getSharedPreferences(context);
		
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		
		backgroundButton = (Button) findViewById(R.id.screen_button);
		
		// SysApplication.getInstance().addActivity(this);
		FyLog.i("坏点测试界面", "进入");
		// 拷机状态下  定义一个计时器，让该计时器周期性地执行指定任务
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// 如果该消息是本程序所发送的
				if (msg.what == 0x1212) {
					// 动态地修改所显示的图片
					backgroundButton
							.setBackgroundResource(screen_color_test[i]);
					i++;
					if (i >= 6) {
						//测试结束
						i = 0;
						isSleepExit = false;
						Intent intent = new Intent(ScreenActivity.this, ScreenCheckActivity.class);
						startActivity(intent);
					}
				}
			}
		};
		//正常cesh8i状态下动作监听
		if (isburning == false) {
			backgroundButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					backgroundButton
							.setBackgroundResource(screen_color_test[i]);
					i++;
					if (i >= 6) {
						isSleepExit = false;
						Intent intent = new Intent(ScreenActivity.this, ScreenCheckActivity.class);
						startActivity(intent);
					}
				}
			});
		}

	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
		if (isburning == true) {
			showTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (screen_test_over == false)
						// 发送空消息
						mHandler.sendEmptyMessage(0x1212);
				}
			}, 0, 2000);
		}
		//1.5秒后将提示屏蔽
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (screen_test_over == false)
					backgroundButton.setText("");
			}
		}, 3000);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		FyLog.i(TAG, "onStop()");
		if(!isSleepExit){
			finish();
		}
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		if(showTimer != null)
		showTimer.cancel();
		main = null;
		screen_test_over = true;
	}
	
	private boolean isSleepExit = true;
	/************************** 事件监听申明区 ***************************/
	@SuppressWarnings("deprecation")
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
}