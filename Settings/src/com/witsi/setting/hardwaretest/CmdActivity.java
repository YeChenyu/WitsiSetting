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
		Log.i("CMD��������", "������");
		mRunning = false;
		mHandler.removeCallbacks(mCmdRunnable);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//��װ���ء����ð�~	
		main = getLayoutInflater().from(this).inflate(
				R.layout.hardware_cmd_activity, null);
		main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		setContentView(main);
		Log.i("CMD��������", "����");
		arqMisc = new ArqMisc(this);
		cmd_show = (TextView) this.findViewById(R.id.cmd_show);
		tv_msg = (TextView) findViewById(R.id.textView1);
		/**  ���ݲ�ͬ����Ŀ�в�ͬ����ʾ*/
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102
			||	ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			tv_msg.setText("���˸���˳�����ģʽ����");
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			tv_msg.setText("��ȡ�����˳�����ģʽ����");
		}
		
		HandlerThread thread_write = new HandlerThread("mCmdRunnable");
		thread_write.start();
		handler = new Handler() // ��
		{
			@Override
			public void handleMessage(Message msg) {
				Log.i("CMD", "���ܵ�msg");
				if (msg.what == 0x333) {
					if (msg.obj.toString().equals("0")) {
						cmd_show.setText("��ֹͣ����ģʽ");
						isSleepExit = false;
					} else {
						cmd_show.setText("�쳣");
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
		mHandler = new Handler(thread_write.getLooper());// ʹ��HandlerThread��looper���󴴽�Handler�����ʹ��Ĭ�ϵĹ��췽�������п�������UI�߳�
		mHandler.post(mCmdRunnable);// ���߳�post��Handler��

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
				Log.i("CMD", "Runnbanle����");
				// ----------ģ���ʱ�Ĳ�������ʼ---------------
				try {
					i = arqMisc.enterCmdMode();
				} catch (Exception e) {
					Log.i("CMD", e.toString());
				}
				Log.i("CMD", "enterCmdMode����");
				Log.i("CMD", "i=" + i);
				mRunning = false;
				Message msg = new Message();
				msg.what = 0x333;
				msg.obj = i;
				handler.sendMessage(msg);
			}
			Log.i("CMD", "Runnbanle����");
		}
	};
	
	
	//������ؼ������ʾ����
		@SuppressWarnings("deprecation")
		@Override  
		public boolean onKeyDown(int keyCode, KeyEvent event)  
		{  
//	        if (keyCode == KeyEvent.KEYCODE_BACK )  
//	        {  
//	            // �����˳��Ի���  
//	            final AlertDialog isExit = new AlertDialog.Builder(this).create();  
//	            // ���öԻ������  
//	            isExit.setTitle("ϵͳ��ʾ");  
//	            // ���öԻ�����Ϣ  
//	            isExit.setMessage("ȷ��Ҫ�˳���");  
//	            // ���ѡ��ť��ע�����  
//	            isExit.setButton("ȷ��", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//						ActivityManagers.clearActivity();
//						isSleepExit = false;
//		                finish();  
//					}
//				});  
//	            isExit.setButton2("ȡ��", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//					}
//				});  
//	            // ��ʾ�Ի���  
//	            if(i != 0){
//					Toast.makeText(context, "δ�˳�����ģʽ�������˳�����ģʽ��", Toast.LENGTH_SHORT).show();
//				}else{
//					isExit.show();  
//				}
//	        }  
	          
	        return true;  
	          
	    }  
		//����Ϊ������ؼ������ʾ����	
}
