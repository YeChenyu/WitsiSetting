package com.witsi.setting.hardwaretest;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.witsi.arq.ArqMisc;

public class CmdActivity extends Activity {
	
	private String TAG = "CmdActivity";
	private Context context = CmdActivity.this;
	
	View main;
	public TextView cmd_show;
	private TextView tv_msg;
	Handler handler;
	Handler mHandler;
	ArqMisc arqMisc;
	
	boolean mRunning = true;
	int i = 3;
	@Override
	protected void onResume() {
		super.onResume();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("CMD启动界面", "已销毁");
		mRunning = false;
		mHandler.removeCallbacks(mCmdRunnable);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//假装隐藏……好吧~	
		main = getLayoutInflater().from(this).inflate(
				R.layout.hardware_cmd_activity, null);
		main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		setContentView(main);
		Log.i("CMD启动界面", "进入");
		arqMisc = new ArqMisc(this);
		cmd_show = (TextView) this.findViewById(R.id.cmd_show);
		tv_msg = (TextView) findViewById(R.id.textView1);
		/**  根据不同的项目有不同的提示*/
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102
			||	ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			tv_msg.setText("按退格键退出命令模式！！");
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			tv_msg.setText("按取消键退出命令模式！！");
		}
		
		HandlerThread thread_write = new HandlerThread("mCmdRunnable");
		thread_write.start();
		handler = new Handler() // ①
		{
			@Override
			public void handleMessage(Message msg) {
				Log.i("CMD", "接受到msg");
				if (msg.what == 0x333) {
					if (msg.obj.toString().equals("0")) {
						cmd_show.setText("已停止命令模式");
						isSleepExit = false;
					} else {
						cmd_show.setText("异常");
					}
				}
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(CmdActivity.this,
								EntryActivity.class);
						startActivity(intent);
					}
				}, 500);
			}
		};
		mHandler = new Handler(thread_write.getLooper());// 使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
		mHandler.post(mCmdRunnable);// 将线程post到Handler中

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
	
	Runnable mCmdRunnable = new Runnable() {
		@Override
		public void run() {
			while (mRunning == true) {
				Log.i("CMD", "Runnbanle开启");
				// ----------模拟耗时的操作，开始---------------
				try {
					i = arqMisc.enterCmdMode();
				} catch (Exception e) {
					Log.i("CMD", e.toString());
				}
				Log.i("CMD", "enterCmdMode开启");
				Log.i("CMD", "i=" + i);
				mRunning = false;
				Message msg = new Message();
				msg.what = 0x333;
				msg.obj = i;
				handler.sendMessage(msg);
			}
			Log.i("CMD", "Runnbanle结束");
		}
	};
	
	
	//点击返回键填出提示窗口
		@SuppressWarnings("deprecation")
		@Override  
		public boolean onKeyDown(int keyCode, KeyEvent event)  
		{  
//	        if (keyCode == KeyEvent.KEYCODE_BACK )  
//	        {  
//	            // 创建退出对话框  
//	            final AlertDialog isExit = new AlertDialog.Builder(this).create();  
//	            // 设置对话框标题  
//	            isExit.setTitle("系统提示");  
//	            // 设置对话框消息  
//	            isExit.setMessage("确定要退出吗");  
//	            // 添加选择按钮并注册监听  
//	            isExit.setButton("确定", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//						ActivityManagers.clearActivity();
//						isSleepExit = false;
//		                finish();  
//					}
//				});  
//	            isExit.setButton2("取消", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//					}
//				});  
//	            // 显示对话框  
//	            if(i != 0){
//					Toast.makeText(context, "未退出命令模式，请先退出命令模式！", Toast.LENGTH_SHORT).show();
//				}else{
//					isExit.show();  
//				}
//	        }  
	          
	        return true;  
	          
	    }  
		//以上为点击返回键填出提示窗口	
}
