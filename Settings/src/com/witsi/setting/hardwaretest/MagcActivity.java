package com.witsi.setting.hardwaretest;



import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.witsi.arq.ArqConverts;
import android.witsi.arq.ArqMagc;
import android.witsi.arq.MagcCard;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class MagcActivity extends Activity implements
					android.view.View.OnClickListener, Callback {
	private final String TAG = "MagcActivity";
	private Context context = MagcActivity.this;
	
	private TextView track1_cout;
	private TextView track2_cout;
	private TextView track3_cout;
	private TextView track1_value;
	private TextView track2_value;
	private TextView track3_value;
	private TextView total_cout;
	private Button button_Magc_return;
	private Button button_Magc_ok;
	private Button button_Magc_false;
	private Button button_Magc_test;
	ImageView magc_image;
	
	private ArqMagc magcArq;
	private MagcCard magc = new MagcCard();
	private Handler handler = null;
	private CardCheckThread thread = null;
	
	boolean screen_sleep = false;
	boolean isburning = false;
	private int tr1_count = 0, tr2_count = 0, tr3_count = 0, total = 0;

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
		setContentView(R.layout.hardware_magc_activity);
		FyLog.d(TAG, "进入屏幕磁卡测试界面");

		initViews();
		
		initDatas();
	}

	private void initViews() {
		track1_cout = (TextView) findViewById(R.id.track1_count);
		track2_cout = (TextView) findViewById(R.id.track2_count);
		track3_cout = (TextView) findViewById(R.id.track3_count);

		track1_value = (TextView) findViewById(R.id.track1_value);
		track2_value = (TextView) findViewById(R.id.track2_value);
		track3_value = (TextView) findViewById(R.id.track3_value);

		total_cout = (TextView) findViewById(R.id.total_count);
		
		View v = (View)findViewById(R.id.ll_tool);
		button_Magc_return = (Button) v.findViewById(R.id.back);
		button_Magc_ok = (Button) v.findViewById(R.id.pass);
		button_Magc_false = (Button) v.findViewById(R.id.fail);
		button_Magc_test = (Button) v.findViewById(R.id.test);
		button_Magc_return.setOnClickListener(MagcActivity.this);
		button_Magc_ok.setOnClickListener(MagcActivity.this);
		button_Magc_false.setOnClickListener(MagcActivity.this);
		button_Magc_test.setOnClickListener(MagcActivity.this);
		magc_image = (ImageView) this.findViewById(R.id.magc_image);
		button_Magc_test.setText("");
	}
	
	private void initDatas() {
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		// SysApplication.getInstance().addActivity(this);
		magcArq = new ArqMagc(context);
		handler = new Handler(this);
	}

	protected void onResume() {
		super.onResume();
		FyLog.d(TAG, "onResume()");
		if(thread == null){
			thread = new CardCheckThread();
			thread.start();
		}
	}

	@Override
	public void onClick(View magcClick) {
		//结束拷机
		isburning = false;
		editor.putBoolean("flag_burn", false);
		editor.commit();
		isSleepExit = false;
		switch (magcClick.getId()) {
		case R.id.back: {
			ActivityManagers.clearActivity();
			if (config.getBoolean("singletest", false) == true) {
				ActivityManagers.trunToSingleTestActivity(MagcActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				ActivityManagers.clearActivity();
				ActivityManagers.trunToEntryActivity(MagcActivity.this);
			}  else {
				ActivityManagers.trunToBurnStartActivity(MagcActivity.this);
			} 
			break;
		}
		case R.id.pass: {
			if (config.getBoolean("singletest", false) == true) {
				editor.putString("magc", "ok");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(MagcActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				editor.putString("magc", "ok");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(MagcActivity.this);
			} else {
				ActivityManagers.trunToBurnStartActivity(MagcActivity.this);
			} 
			break;
		}
		case R.id.fail: {
			if (config.getBoolean("singletest", false) == true) {
				editor.putString("magc", "ng");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(MagcActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				editor.putString("magc", "ng");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(MagcActivity.this);
			} else {
				ActivityManagers.trunToBurnStartActivity(MagcActivity.this);
			} 
			break;
		}
		default:
			break;
		}
	}

	class CardCheckThread extends Thread {
		
		private boolean threadRunning = false;

		public CardCheckThread() {
			// TODO Auto-generated constructor stub
			magc.setTimeout(6000);
			magc.setAlgorithmId(0xff);
			threadRunning = true;
		}
		public void run() {
			FyLog.i(TAG, "magc card thread working.");
			int ret;
			while (threadRunning) {
				/* read magc card track */
				ret = magcArq.magcRead(magc);
				if (ret < 0) {
					FyLog.e(TAG, "Magc card read failed. ret = " + ret);
					continue;
				}
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}
		}
		
		public void stopThread() {
			threadRunning = false;
		}

		public boolean isThreadRunnig() {
			return threadRunning;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		if (msg.what != 1) {
			FyLog.e(TAG, "Unknown message received.");
			return false;
		}

		byte[] track1 = magc.getTrack1Data();
		byte[] track2 = magc.getTrack2Data();
		byte[] track3 = magc.getTrack3Data();

		/* if track1 exist, read out the track data. */
		if (track1 != null) {
			tr1_count++;
			track1_cout.setText(Integer.toString(tr1_count)); // track1
																// exist
																// and
																// decode
																// success.
			track1_value.setText(ArqConverts.asciiBytesToString(track1));
			FyLog.d(TAG, "track1: " + ArqConverts.asciiBytesToString(track1));
		}

		/* if track2 exist, read out the track data. */
		if (track2 != null) {
			tr2_count++;
			track2_cout.setText(Integer.toString(tr2_count)); // track2 track2: 6221503910003171772=49121201740600000
																// exist
																// and
																// decode
																// success.
			track2_value.setText(ArqConverts.asciiBytesToString(track2));
			FyLog.d(TAG, "track2: " + ArqConverts.asciiBytesToString(track2));
		}

		/* if track3 exist, read out the track data. */
		if (track3 != null) {
			tr3_count++;
			track3_cout.setText(Integer.toString(tr3_count)); // track3track3: 996221503910003171772=1561560000000000000003000000114000049121=000000000000=000000000000=000000017406000
																// exist
																// and
																// decode
																// success.
			track3_value.setText(ArqConverts.asciiBytesToString(track3));
			FyLog.d(TAG, "track3: " + ArqConverts.asciiBytesToString(track3));
		}

		total++;
		total_cout.setText(Integer.toString(total));
		return false;
	}

	public void returnBtnOnClickHandler(View source) {
		/* Clear data */
		tr1_count = 0;
		tr2_count = 0;
		tr3_count = 0;
		total = 0;

		/* Clear display */
		track1_cout.setText("0");
		track2_cout.setText("0");
		track3_cout.setText("0");
		total_cout.setText("0");

		track1_value.setText("000000000000000000");
		track2_value.setText("000000000000000000");
		track3_value.setText("000000000000000000");
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		FyLog.d(TAG, "onPause");
		if(thread != null){
			thread.stopThread();
			thread.interrupt();
			thread = null;
		}
	}
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.d(TAG, "onStop");
		if(!isSleepExit){
			finish();
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.d(TAG, "onDestroy");
		if(magcArq != null)
			magcArq = null;
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