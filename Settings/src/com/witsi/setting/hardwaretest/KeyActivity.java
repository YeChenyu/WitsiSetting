package com.witsi.setting.hardwaretest;

import java.lang.ref.WeakReference;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.witsi.arq.ArqKey;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

public class KeyActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private final String TAG = "KeyActivity";
	private Context context = KeyActivity.this;
	
	private final int KEY_EXPEND_BASE = 128;
	private final int KEY_0 = 0x30;
	private final int KEY_1 = 0x31;
	private final int KEY_2 = 0x32;
	private final int KEY_3 = 0x33;
	private final int KEY_4 = 0x34;
	private final int KEY_5 = 0x35;
	private final int KEY_6 = 0x36;
	private final int KEY_7 = 0x37;
	private final int KEY_8 = 0x38;
	private final int KEY_9 = 0x39;
	private final int KEY_F1 = 0x81;
	private final int KEY_F2 = 0x82;
	private final int KEY_F3 = 0x83;
	private final int KEY_F4 = 0x84;
	private final int KEY_F5 = 0x85;
	private final int KEY_F6 = 0x86;
	private final int KEY_F7 = 0x87;
	private final int KEY_ESC = 0x1B;
	private final int KEY_BS = 0x08;
	private final int KEY_ENTER = 0x0d;

	//2102
	private final int KEY_XIN = '*';
	private final int KEY_JIN = '#';
	//3029
	private final int KEY_00 = 0x92;
	private final int KEY_DOC = '.';
	private final int KEY_DOC1 = KEY_EXPEND_BASE + 12;
	
	private Button btn0;
	private Button btn1;
	private Button btn2;
	private Button btn3;
	private Button btn4;
	private Button btn5;
	private Button btn6;
	private Button btn7;
	private Button btn8;
	private Button btn9;
	private Button btn_f1;
	private Button btn_f2;
	private Button btn_f3;
	private Button btn_f4;
	private Button btn_f5;
	private Button btn_f6;
	private Button btn_f7;
	private Button btn_esc;
	private Button btn_bs;
	private Button btn_enter;

	private Button button_key_return;
	private Button button_key_ok;
	private Button button_key_false;
	private Button button_key_test;
	
	View main = null;
	ImageView key_image;
	TextView tv_tip;
	
	private ArqKey keyArq;

	private Thread thread = null;
	private MyRunnable myrun = new MyRunnable();
	private MyHandler mHandler = new MyHandler(this);
	/* wakelock */
	// int KEY_ESC_flag=0;
	PowerManager pm;
	PowerManager.WakeLock wl;
	/* KEY Value define. */
	
	public int flag_key = -1;
	boolean isburning = false;
	private final int MSG_KEY_PRESS = 1;
	private final int MSG_KEY_RECOVER = 0;
	private final int MSG_START_READ_KEY = 5;
	boolean screen_sleep = false;
	public boolean button0 = false, button1 = false, button2 = false,
			button3 = false, button4 = false, button5 = false, button6 = false,
			button7 = false, button8 = false, button9 = false,
			buttonesc = false, buttonenter = false,
			buttonf1 = false, buttonf2 = false, buttonbs = false;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

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
		setContentView(R.layout.hardware_key_activity);
		
		initViews();
		
		initDatas();
	}

	private void initViews() {
		// SysApplication.getInstance().addActivity(this);
		pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "myflag");
		btn0 = (Button) findViewById(R.id.key0_btn);
		btn1 = (Button) findViewById(R.id.key1_btn);
		btn2 = (Button) findViewById(R.id.key2_btn);
		btn3 = (Button) findViewById(R.id.key3_btn);
		btn4 = (Button) findViewById(R.id.key4_btn);
		btn5 = (Button) findViewById(R.id.key5_btn);
		btn6 = (Button) findViewById(R.id.key6_btn);
		btn7 = (Button) findViewById(R.id.key7_btn);
		btn8 = (Button) findViewById(R.id.key8_btn);
		btn9 = (Button) findViewById(R.id.key9_btn);
		btn_f1 = (Button) findViewById(R.id.keyf1_btn);
		btn_f2 = (Button) findViewById(R.id.keyf2_btn);
		btn_f3 = (Button) findViewById(R.id.keyf3_btn);
		btn_f4 = (Button) findViewById(R.id.keyf4_btn);
		btn_f5 = (Button) findViewById(R.id.keyf5_btn);
		btn_f6 = (Button) findViewById(R.id.keyf6_btn);
		btn_f7 = (Button) findViewById(R.id.keyf7_btn);
		btn_esc = (Button) findViewById(R.id.keyesc_btn);
		btn_bs = (Button) findViewById(R.id.keybs_btn);
		btn_enter = (Button) findViewById(R.id.keyenter_btn);
		tv_tip = (TextView) findViewById(R.id.tv_tip);

		View v = findViewById(R.id.ll_tool);
		button_key_return = (Button) v.findViewById(R.id.back);
		button_key_ok = (Button) v.findViewById(R.id.pass);
		button_key_false = (Button) v.findViewById(R.id.fail);
		button_key_test = (Button) v.findViewById(R.id.test);
		button_key_return.setOnClickListener(KeyActivity.this);
		button_key_ok.setOnClickListener(KeyActivity.this);
		button_key_false.setOnClickListener(KeyActivity.this);
		button_key_test.setOnClickListener(KeyActivity.this);
		button_key_test.setText("重测");
		btn0.setOnClickListener(KeyActivity.this);
		btn1.setOnClickListener(KeyActivity.this);
		btn2.setOnClickListener(KeyActivity.this);
		btn3.setOnClickListener(KeyActivity.this);
		btn4.setOnClickListener(KeyActivity.this);
		btn5.setOnClickListener(KeyActivity.this);
		btn6.setOnClickListener(KeyActivity.this);
		btn7.setOnClickListener(KeyActivity.this);
		btn8.setOnClickListener(KeyActivity.this);
		btn9.setOnClickListener(KeyActivity.this);
		btn_f1.setOnClickListener(KeyActivity.this);
		btn_f2.setOnClickListener(KeyActivity.this);
		btn_esc.setOnClickListener(KeyActivity.this);
		btn_bs.setOnClickListener(KeyActivity.this);
		btn_enter.setOnClickListener(KeyActivity.this);
		key_image = (ImageView) this.findViewById(R.id.key_image);
		
		//根据项目去配置布局
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			btn_esc.setVisibility(View.GONE);
			btn_bs.setVisibility(View.GONE);
			btn_enter.setVisibility(View.GONE);
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102){
			btn_esc.setVisibility(View.INVISIBLE);
		}
	}
	
	private void initDatas() {
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		keyArq = new ArqKey(this);
	}

	protected void onResume() {
		super.onResume();
		FyLog.i(TAG, "onResume()");
		if (myrun.isThreadRunnig() == false) {
			thread = new Thread(myrun);
			thread.start();
		}
		wl.acquire();
	}

	@Override
	public void onClick(View keyClick) {
		isburning = false;
		isSleepExit = false;
		editor.putBoolean("flag_burn", false);
		editor.commit();
		switch (keyClick.getId()) {
		case R.id.back: {
			ActivityManagers.clearActivity();
			if (config.getBoolean("singletest", false) == true) {
				ActivityManagers.trunToSingleTestActivity(KeyActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				ActivityManagers.clearActivity();
				ActivityManagers.trunToEntryActivity(KeyActivity.this);
			}else {
				ActivityManagers.trunToBurnStartActivity(KeyActivity.this);
			}  
			break;
		}
		case R.id.pass: {
			flag_key = 1;
			if (config.getBoolean("singletest", false) == true) {
				editor.putString("key", "ok");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(KeyActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				if (button0 == true && button1 == true && button2 == true
						&& button3 == true && button4 == true
						&& button5 == true && button6 == true
						&& button7 == true && button8 == true
						&& button9 == true 
						&& buttonenter == true
						) {
					if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
						if(buttonesc == true){
							editor.putString("key", "ok");
							editor.commit();
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(KeyActivity.this);
						}
					}else{
						if( buttonf2 == true && buttonf1 == true && buttonbs == true){
							if(buttonesc == true){
								if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
									editor.putString("key", "ok");
									editor.commit();
									ActivityManagers.trunToNextActivity();
									ActivityManagers.startNextActivity(KeyActivity.this);
								}
							}else{
								if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
									editor.putString("key", "ok");
									editor.commit();
									ActivityManagers.trunToNextActivity();
									ActivityManagers.startNextActivity(KeyActivity.this);
								}
							}
						}
					}
				}
			}else {
				ActivityManagers.trunToBurnStartActivity(KeyActivity.this);
			} 
			break;
		}
		case R.id.fail: {
			flag_key = 0;
			if (config.getBoolean("singletest", false) == true) {
				editor.putString("key", "ng");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(KeyActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				editor.putString("key", "ng");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(KeyActivity.this);
			}else {
				ActivityManagers.trunToBurnStartActivity(KeyActivity.this);
			} 
			break;
		}
		case R.id.test: {
			flag_key = 0;
			button0 = false; button1 = false;
			button2 = false; button3 = false;
			button4 = false; button5 = false;
			button6 = false; button7 = false;
			button8 = false; button9 = false;
			buttonesc = false; buttonenter = false;
			buttonbs = false; buttonf1 = false;
			buttonf2 = false;
			btn0.setBackgroundResource(R.drawable.key_round_button);
			btn1.setBackgroundResource(R.drawable.key_round_button);
			btn2.setBackgroundResource(R.drawable.key_round_button);
			btn3.setBackgroundResource(R.drawable.key_round_button);
			btn4.setBackgroundResource(R.drawable.key_round_button);
			btn5.setBackgroundResource(R.drawable.key_round_button);
			btn6.setBackgroundResource(R.drawable.key_round_button);
			btn7.setBackgroundResource(R.drawable.key_round_button);
			btn8.setBackgroundResource(R.drawable.key_round_button);
			btn9.setBackgroundResource(R.drawable.key_round_button);
			btn_bs.setBackgroundResource(R.drawable.key_round_button);
			btn_enter.setBackgroundResource(R.drawable.key_round_button);
			btn_esc.setBackgroundResource(R.drawable.key_round_button);
			btn_f1.setBackgroundResource(R.drawable.key_round_button);
			btn_f2.setBackgroundResource(R.drawable.key_round_button);
			btn0.setText(""); btn1.setText("");
			btn2.setText(""); btn3.setText("");
			btn4.setText(""); btn5.setText("");
			btn6.setText(""); btn7.setText("");
			btn8.setText(""); btn9.setText("");
			btn_bs.setText(""); btn_enter.setText("");
			btn_esc.setText(""); btn_f1.setText("");
			btn_f2.setText("");
			break;
		}
		default:
			break;
		}
	}

	class MyRunnable implements Runnable {
		private boolean threadRunning = false;
		private String strKey = "";
		public void stopThread() {
			threadRunning = false;
		}

		public boolean isThreadRunnig() {
			return threadRunning;
		}

		public void run() {
			FyLog.i(TAG, "KEY scan thread working.");
			int ret, ms;
			byte[] key = new byte[1];

			ms = 4000; // 5s

			threadRunning = true;
			mHandler.sendEmptyMessage(MSG_START_READ_KEY);
			// scan key input.
			while (threadRunning) {
				ret = keyArq.getKey(key, ms);
				if (ret < 0) {
					FyLog.e(TAG, "Get Key failed. ret = " + ret);
					continue;
				}
				switch ((key[0] & 0x00ff)) {
				case KEY_0:
					button0 = true;
					strKey = "0";
					break;
				case KEY_1:
					button1 = true;
					strKey = "1";
					break;
				case KEY_2:
					button2 = true;
					strKey = "2";
					break;
				case KEY_3:
					button3 = true;
					strKey = "3";
					break;
				case KEY_4:
					button4 = true;
					strKey = "4";
					break;
				case KEY_5:
					button5 = true;
					strKey = "5";
					break;
				case KEY_6:
					button6 = true;
					strKey = "6";
					break;
				case KEY_7:
					button7 = true;
					strKey = "7";
					break;
				case KEY_8:
					button8 = true;
					strKey = "8";
					break;
				case KEY_9:
					button9 = true;
					strKey = "9";
					break;
				//    F1
				case KEY_F1:
					buttonf1 = true;
					strKey = "F1";
					break;
				case KEY_XIN:
					buttonf1 = true;
					strKey = "*";
					break;
				case KEY_00:
					buttonf1 = true;
					strKey = "00";
					break;
				//    F2
				case KEY_F2:
					buttonf2 = true;
					strKey = "F2";
					break;
				case KEY_JIN:
					buttonf2 = true;
					strKey = "#";
					break;
				case KEY_DOC:
				case KEY_DOC1:
					buttonf2 = true;
					strKey = ".";
					break;
				//	 功能键
				case KEY_ESC:
					buttonesc = true;
					strKey = "取消";
					break;
				case KEY_BS:
					buttonbs = true;
					strKey = "退格";
					break;
				case KEY_ENTER:
					buttonenter = true;
					strKey = "确认";
					break;
				default:
					break;
				}
				if (config.getBoolean("alltest", false) == true) {
					if (button0 == true && button1 == true 
						&& button2 == true && button3 == true 
						&& button4 == true && button5 == true 
						&& button6 == true && button7 == true 
						&& button8 == true && button9 == true 
						&& buttonenter == true
						){
						isSleepExit = false;
						if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
							if(buttonesc == true){
								editor.putString("key", "ok");
								editor.commit();
								ActivityManagers.trunToNextActivity();
								ActivityManagers.startNextActivity(KeyActivity.this);
							}
						}else{
							if( buttonf2 == true && buttonf1 == true && buttonbs == true){
								if(buttonesc == true){
									if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
										editor.putString("key", "ok");
										editor.commit();
										ActivityManagers.trunToNextActivity();
										ActivityManagers.startNextActivity(KeyActivity.this);
									}
								}else{
									if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
										editor.putString("key", "ok");
										editor.commit();
										ActivityManagers.trunToNextActivity();
										ActivityManagers.startNextActivity(KeyActivity.this);
									}
								}
							}
						}
					}
				}
				Message msg = new Message();
				msg.what = MSG_KEY_PRESS;

				Bundle bundle = new Bundle();
				bundle.putInt("key", (int) key[0] & 0x00ff);
				bundle.putString("keyValue", strKey);
				FyLog.d(TAG, "the key is: " + strKey);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
			}
			threadRunning = false;
		}
	}

	class recoverThread extends Thread {
		private int key = 0;
		private KeyActivity kact;

		public recoverThread(int val, KeyActivity activity) {
			key = val;
			kact = activity;
		}

		public void run() {
			try { // sleep 500ms
				Thread.sleep(500);
			} catch (InterruptedException e) {
				FyLog.d(TAG, "Waiting didnt work!!");
				e.printStackTrace();
			}

			Message msg = new Message();
			msg.what = MSG_KEY_RECOVER;

			Bundle bundle = new Bundle();
			bundle.putInt("key", key);

			msg.setData(bundle);
			kact.mHandler.sendMessage(msg);
		}
	}

	class MyHandler extends Handler {
		// WeakReference to the outer class's instance
		private WeakReference<KeyActivity> mOuter;
		Button btn;

		public MyHandler(KeyActivity activity) {
			mOuter = new WeakReference<KeyActivity>(activity);
		}

		public void handleMessage(Message msg) {
			if(msg.what == MSG_START_READ_KEY){
				if(tv_tip.getVisibility() == View.VISIBLE)
					tv_tip.setVisibility(View.INVISIBLE);
			}else{
				KeyActivity outer = mOuter.get();
				if (outer != null) {
					int key = msg.getData().getInt("key");
					FyLog.d(TAG, "the key value is: " + key);
					switch (key) {
					case KEY_0:
						btn = btn0;

						break;
					case KEY_1:
						btn = btn1;

						break;
					case KEY_2:
						btn = btn2;

						break;
					case KEY_3:
						btn = btn3;

						break;
					case KEY_4:
						btn = btn4;

						break;
					case KEY_5:
						btn = btn5;

						break;
					case KEY_6:
						btn = btn6;

						break;
					case KEY_7:
						btn = btn7;

						break;
					case KEY_8:
						btn = btn8;

						break;
					case KEY_9:
						btn = btn9;

						break;
					case KEY_F1:
					case KEY_XIN:
					case KEY_00:
						btn = btn_f1;
						break;
					case KEY_F2:
					case KEY_JIN:
					case KEY_DOC:
					case KEY_DOC1:
						btn = btn_f2;
						break;
					case KEY_F3:
						btn = btn_f3;
						break;
					case KEY_F4:
						btn = btn_f4;
						break;
					case KEY_F5:
						btn = btn_f5;
						break;
					case KEY_F6:
						btn = btn_f6;
						break;
					case KEY_F7:
						btn = btn_f7;
						break;
					case KEY_ESC:
						btn = btn_esc;
						if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025)
							btn = btn_f1;
						break;
						
					case KEY_BS:
						btn = btn_bs;
						if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025)
							btn = btn_f1;
						break;
						
					case KEY_ENTER:
						btn = btn_enter;
						if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025)
							btn = btn_f2;
						break;
					default:
						FyLog.e(TAG, "unknown key = " + msg.what);
						return;
					}

					if (msg.what == MSG_KEY_PRESS) { // KEY press
						btn.setBackgroundResource(R.drawable.key_round_button_press);
						btn.setTextColor(Color.GREEN);
						btn.setText(msg.getData().getString("keyValue"));
						new recoverThread(key, outer).start();
					}
				}
			}
		}
	}
	protected void onPause() {
		FyLog.i(TAG, "###### onPause called. ######");
		if (myrun.isThreadRunnig() == true) {
			myrun.stopThread();
		}
		super.onPause();
		wl.release();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		if(thread != null){
			thread.interrupt();
			thread = null;
			myrun = null;
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