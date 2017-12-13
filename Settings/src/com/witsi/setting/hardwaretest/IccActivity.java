package com.witsi.setting.hardwaretest;

import java.util.Timer;
import java.util.TimerTask;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.witsi.arq.ArqConverts;
import android.witsi.arq.ArqIcc;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class IccActivity extends Activity implements OnClickListener, Callback{
	
	private final String TAG = "IccActivity";
	private Context context = IccActivity.this;
	
	private Button button_icc_return;
	private Button button_icc_ok;
	private Button button_icc_false;
	private Button button_icc_test;
	private TextView tv_tip;
	private ImageView icc_image;
	private TextView tvStatus, tvAtr, tvRandom;
	private View main = null;
	
	private Timer showTimer;
	private ArqIcc iccArq;
	private Handler handler;
	private CardCheckThread thread = null;
	
	private boolean screen_sleep = false;
	public int flag_icc = -1;
	private boolean isburning = false;
	private int ItemId;
	private final byte[] GET_RANDOM_CMD = new byte[] { 0x00, (byte) 0x84, 0x00, 0x00, 0x08 };
	
	private SharedPreferences config;
	private Editor editor;
	private int CARD_CHECK_TIME_OUT = 10000;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		FyLog.d(TAG, "进入ICC测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		// 给拷机做选择项
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);
		if (isburning == true) {
			getLayoutInflater();
			// 假装隐藏⋯⋯好吧~
			main = LayoutInflater.from(context).inflate(
					R.layout.hardware_icc_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_icc_activity);
		}

		initViews();
		
		initDatas();
	}

	private void initViews() {
		tvStatus = (TextView) findViewById(R.id.status_value);
		tvAtr = (TextView) findViewById(R.id.atr_value);
		tvRandom = (TextView) findViewById(R.id.random_value);
		tv_tip = (TextView) findViewById(R.id.tv_tip);
		button_icc_return = (Button) this.findViewById(R.id.back);
		button_icc_ok = (Button) this.findViewById(R.id.pass);
		button_icc_false = (Button) this.findViewById(R.id.fail);
		button_icc_test = (Button) this.findViewById(R.id.test);
		button_icc_return.setOnClickListener(IccActivity.this);
		button_icc_ok.setOnClickListener(IccActivity.this);
		button_icc_false.setOnClickListener(IccActivity.this);
		button_icc_test.setOnClickListener(IccActivity.this);
		tv_tip.setText("");
	}

	private void initDatas(){

		handler  = new Handler(this);
		iccArq = new ArqIcc(context);
		//屏幕休眠
		icc_image = (ImageView) this.findViewById(R.id.icc_image);
		if (config.getBoolean("light", true) == false) {
			icc_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		if(ProjectConfig.getTheMechineType() != ProjectConfig.PROG_3027R)
			CARD_CHECK_TIME_OUT = 10000;
		//开始寻卡
		thread = new CardCheckThread(0);
		thread.start();
		if (config.getBoolean("singletest", false) == false)
			setCardCheckTimeOut();
	}

	private void setCardCheckTimeOut() {
		// 定义一个计时器，让该计时器周期性地执行指定任务
		showTimer = new Timer();
		showTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				isThreadRunning = false;
				handler.sendEmptyMessage(CARD_TIMEOUT);
				FyLog.e(TAG, "card check time out");
				isSleepExit = false;
				if(flag_icc != 0){
					if(isburning == true){
						exitThread();
						editor.putString("ic", "ng");
						editor.putInt("error_ic", config.getInt("error_ic", 0) + 1);
						editor.putInt("error", 1);
						editor.commit();
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SAM);
						finish();
					}else if (config.getBoolean("alltest", false) == true) {
						editor.putString("ic", "ng");
						editor.commit();
					}
				}
			}
		}, CARD_CHECK_TIME_OUT);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}
	
	
	private boolean isThreadRunning = true;
	private boolean isExitThread = false;
	class CardCheckThread extends Thread{
		
		private int slot = -1;
		
		public CardCheckThread(int slot) {
			// TODO Auto-generated constructor stub
			this.slot = slot;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!isExitThread)
			while(isThreadRunning){
				cardCheck(slot);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	private void stopTheThread(){
		isThreadRunning = false;
	}
	private void exitThread(){
		isThreadRunning = false;
		isExitThread = true;
	}
	
	private synchronized void cardCheck(int slot){
		/* check if card exist */
		byte[] rbuf = new byte[1024];
		int ret = -1, flag = -1;
		//卡槽类型
		handler.sendEmptyMessage(CARD_TYPE);
		//检测芯片卡是否存在
		ret = iccArq.iccPresent(rbuf);
		if (ret < 0) {
			FyLog.e(TAG, "icc present check failed ret = " + ret);
			return;
		}
		flag = (rbuf[slot] & 0x00ff);
		if (flag == 0x00) {
			/* 没有卡存在 */
			FyLog.d(TAG, "slot-" + slot + " has no card exist!");
			handler.sendEmptyMessage(CARD_NOT_EXIST);
			return;
		} else if (flag == 0x01) {
			/* 卡存在但是没上电 */
			handler.sendEmptyMessage(CARD_EXIST);
			FyLog.d(TAG, "slot-" + slot + " has card exist!");
		} else if (flag == 0x03) {
			FyLog.d(TAG, "card exist and already power up!");
			handler.sendEmptyMessage(CARD_EXIST_POWER_ON);
		} else {
			FyLog.e(TAG, "Wrong return value: flag = " + flag);
			return;
		}
		//芯片卡存在，但是未上电
		if (flag != 0x03) {
			ret = iccArq.iccPowerUp(slot, rbuf);
			if (ret < 0) {
				FyLog.e(TAG, "icc power up failed.");
				return;
			}
		}
		//上电成功，开始读取IC卡数据
		/* get ATR value. */
		byte[] atr_buf = new byte[ret];
		for (int i = 0; i < ret; i++) {
			atr_buf[i] = rbuf[i];
		}
		Message msg = handler.obtainMessage();
		msg.what = CARD_ATR;
		if(ret > 0){
			msg.obj = ArqConverts.bytesToHexString(atr_buf, '-');
		}else {
			msg.obj = "无";
		}
		handler.sendMessage(msg);
		
		/* get random */
		ret = iccArq.iccApdu(slot, GET_RANDOM_CMD,
				GET_RANDOM_CMD.length, rbuf); // 3s
		if (ret < 0) {
			FyLog.e(TAG, "send apdu failed.");
			return;
		}
		byte[] rdm_buf = new byte[ret];
		for (int i = 0; i < ret; i++) {
			rdm_buf[i] = rbuf[i];
		}
		msg = handler.obtainMessage();
		msg.what = CARD_RANDOM;
		if(ret > 0)
			msg.obj = ArqConverts.bytesToHexString(rdm_buf, '-');
		else 
			msg.obj = "无";
		handler.sendMessage(msg);
		/* power down */
		ret = iccArq.iccPowerDown(slot);
		if (ret < 0) {
			FyLog.e(TAG, "icc power down failed.");
		} else {
			FyLog.i(TAG, "icc power down success.");
			handler.sendEmptyMessage(CARD_POWER_DOWN);
		}
	}

	private boolean isDisplay = false;
	private static final int CARD_TYPE = 2;
	private static final int CARD_EXIST = 0;
	private static final int CARD_NOT_EXIST = 1;
	private static final int CARD_EXIST_POWER_ON = 3;
	private static final int CARD_ATR = 4;
	private static final int CARD_RANDOM = 5;
	private static final int CARD_TIMEOUT = 6;
	private static final int CARD_POWER_DOWN = 7;
	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case CARD_TYPE:
//			tv_tip.setText("正在寻卡...");
			break;
		case CARD_EXIST:
			tvStatus.setText("CARD EXIST");
			break;
		case CARD_NOT_EXIST:
			tvStatus.setText("NO CARD EXIST");
			tvAtr.setText("00000000");
			tvRandom.setText("00000000");
			tv_tip.setText("");
			break;
		case CARD_EXIST_POWER_ON:
			tvStatus.setText("CARD EXIST & Power Up");
			break;
		case CARD_ATR:
			tvAtr.setText((String) msg.obj);
			break;
		case CARD_RANDOM:
			tvRandom.setText((String) msg.obj);
			break;
		case CARD_POWER_DOWN:
//			isThreadRunning = false;
			break;
		case CARD_TIMEOUT:
			tv_tip.setText("失败");
			break;
		default:
			break;
		}
		if (!tvStatus.getText().toString().equals("NO CARD EXIST")
				&& !tvAtr.getText().toString().equals("00000000")
				&& !tvRandom.getText().toString().equals("00000000"
						)) {
			//单项测试不需要设置超时时间
			if (config.getBoolean("singletest", false) == false){
				showTimer.purge();
				showTimer.cancel();
			}
			flag_icc = 1;
			tv_tip.setText("成功");
			//拷机
			if (isburning == true && !isDisplay){ 
				isDisplay = true;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						exitThread();
						isSleepExit = false;
						editor.putString("ic", "ok");
						editor.commit();
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SAM);
						finish();
					}
				}, 2500);
			}else if (config.getBoolean("alltest", false) == true && !isDisplay) {
				isDisplay = true;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						exitThread();
						isSleepExit = false;
						if(flag_icc != 0){
							editor.putString("ic", "ok");
							editor.commit();
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(IccActivity.this);
						}
					}
				}, 800);
			} 
		}
		else if(tvStatus.getText().toString().equals("CARD EXIST")){
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		if (screen_sleep == true) {
			icc_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			ItemId = v.getId();
			isburning = false;
			isSleepExit = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			switch (ItemId) {
			case R.id.back: {
				flag_icc = 0;
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(IccActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.clearActivity();
					ActivityManagers.trunToEntryActivity(IccActivity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(IccActivity.this);
				} 
				break;
			}
			case R.id.pass: {
				if (config.getBoolean("singletest", false) == false) 
					showTimer.cancel();
				if(flag_icc == 1){
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("ic", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(IccActivity.this);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("ic", "ok");
						editor.commit();
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(IccActivity.this);
					} else {
						ActivityManagers.trunToBurnStartActivity(IccActivity.this);
					}
				}
				break;
			}
			case R.id.fail: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("ic", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(IccActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("ic", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(IccActivity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(IccActivity.this);
				}
				break;
			}
			case R.id.test: {
				tvStatus.setText("NO CARD EXIST");
				tvAtr.setText("00000000");
				tvRandom.setText("00000000");
//				tv_tip.setText("开始测试");
				isThreadRunning = true;
				if (config.getBoolean("singletest", false) == false) 
					setCardCheckTimeOut();
				break;
			}
			default: {
				FyLog.e(TAG, "Error!");
				return;
			}
			}
		}
	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		if(thread != null){
			thread.interrupt();
			thread = null;
		}
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "进入Stop状态");
		if (config.getBoolean("singletest", false) == false) 
			showTimer.cancel();
		stopTheThread();
		exitThread();
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
	 * 拷机时点击屏幕，休眠结束，显示正常界面
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				icc_image.setBackgroundResource(R.drawable.bg_transport);
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

}