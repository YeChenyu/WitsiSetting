package com.witsi.setting.hardwaretest;

import com.witsi.setting1.R;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.witsi.arq.ArqConverts;
import android.witsi.arq.ArqMessage;
import android.witsi.arq.ArqReceiverListener;
import android.witsi.arq.ArqTextOpt;
import android.witsi.arq.ArqUpdate;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

public class DwnLoadActivity extends Activity implements OnClickListener{
	private static final String TAG = "DwnLoadActivity";
	
	private Button button_down_return;
	private Button button_down_ok;
	private Button button_down_false;
	// 系统文件路径 (OTA升级)
	private CheckBox cbSys;
	private EditText edSys;

	// 应用文件路径
	private CheckBox cbApp;
	private EditText edApp;
	
	// 普通文件路径
	private CheckBox cbText;
	private EditText edText;	
	
	// 显示下载状态
	private TextView tvSta;
	
	// 显示进度
	private ProgressBar bar;	
	
	private Handler mHandler = new MyHandler(this);

	// 指示下载进程是否运行
	private boolean downLoadThreadRunning = false;

	// 烧写指示符
	private boolean downLoadSysNeeded = false;
	private boolean downLoadAppNeeded = false;
	private boolean downLoadTextNeeded = false;
	
	// 下载程序进程
	private DownLoadThread downLoadThread;
	OTADownLoadCallable downLoadSysCallable = new OTADownLoadCallable(ArqUpdate.FILE_TYPE_SYSTEM);
	DownLoadCallableClass downLoadAppCallable = new DownLoadCallableClass(ArqUpdate.FILE_TYPE_APPLICATION);
	DownLoadCallableClass downLoadTextCallable = new DownLoadCallableClass(ArqUpdate.FILE_TYPE_NORMAL);

	// 程序升级结果
	private int updateResult = -1;
	
	private static final int UPDATE_RESULT_INIT = -1;
	private static final int UPDATE_RESULT_DONE = 0;
	private static final int UPDATE_RESULT_PARTITION_NOT_FOUND = 1;
	private static final int UPDATE_RESULT_WRONG_APPLICATION_TYPE = 2;
	private static final int UPDATE_RESULT_APPLICATION_NOT_EXIST = 3;
	private static final int UPDATE_RESULT_INVALID_PACKAGE = 4;	
	private static final int UPDATE_RESULT_UNKNOWN_ERROR = 5;
	private static final int MAX_FRAME_DATA_LEN = 1000;
	private SparseArray<String> typeNames = new SparseArray<String>();
	private SparseArray<EditText> edts = new SparseArray<EditText>();
	private SparseArray<String> updateMessages = new SparseArray<String>();
	
	// RDP接口对象
	ArqUpdate arq;
	// OTA下载升级
	ArqTextOpt arqOTA = null;
	
	public int flag_download = -1;
	Intent intent;
	ImageView led_image;
	View main = null;
	Bundle mybundle;
	boolean screen_sleep = false;
	boolean isburning = false;
	private Handler handler;
	boolean ifCloseActivity = true;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	
	/**********************************************************************************************/
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		// 给拷机做选择项
		mybundle = new Bundle();
		intent = getIntent();
		mybundle = (Bundle) intent.getExtras();
		if ((Boolean) intent.getSerializableExtra("flag_burn") == true)
			isburning = true;
		else
			isburning = false;
		if (isburning == true) {
			// 假装隐藏……好吧~
			main = getLayoutInflater().from(this).inflate(
					R.layout.hardware_led_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_download_activity);
		}
		registerReceiver(mReceiver,filterHome);
		
		button_down_return = (Button) this.findViewById(R.id.downGetBackToMain);
		button_down_ok = (Button) this.findViewById(R.id.down_test_state);
		button_down_false = (Button) this.findViewById(R.id.downFalse);
		button_down_return.setOnClickListener(DwnLoadActivity.this);
		button_down_ok.setOnClickListener(DwnLoadActivity.this);
		button_down_false.setOnClickListener(DwnLoadActivity.this);
		// 各个控件
		cbSys = (CheckBox) findViewById(R.id.sys_checkbox);
		edSys = (EditText) findViewById(R.id.sys_file_path);	
	
		cbApp = (CheckBox) findViewById(R.id.app_checkbox);
		edApp = (EditText) findViewById(R.id.app_file_path);
			
		cbText = (CheckBox) findViewById(R.id.text_checkbox);
		edText = (EditText) findViewById(R.id.text_file_path);

		tvSta = (TextView) findViewById(R.id.downlaod_status);		
		bar = (ProgressBar) findViewById(R.id.procbar);
		
		edSys.setText(R.string.download_system_file);
		edApp.setText(R.string.download_application_file);
		
		typeNames.put(ArqUpdate.FILE_TYPE_SYSTEM, "OTA升级文件");
		typeNames.put(ArqUpdate.FILE_TYPE_APPLICATION, "应用程序");
		typeNames.put(ArqUpdate.FILE_TYPE_NORMAL, "普通文件");
		
		edts.put(ArqUpdate.FILE_TYPE_SYSTEM, edSys);
		edts.put(ArqUpdate.FILE_TYPE_APPLICATION, edApp);
		edts.put(ArqUpdate.FILE_TYPE_NORMAL, edText);
		
		updateMessages.put(UPDATE_RESULT_PARTITION_NOT_FOUND, "分区未找到");
		updateMessages.put(UPDATE_RESULT_WRONG_APPLICATION_TYPE, "错误的应用类型");
		updateMessages.put(UPDATE_RESULT_APPLICATION_NOT_EXIST, "应用不存在");
		updateMessages.put(UPDATE_RESULT_INVALID_PACKAGE, "收到无效的进度帧");
		updateMessages.put(UPDATE_RESULT_UNKNOWN_ERROR, "未知的升级帧错误");		
		
		arq = new ArqUpdate(this);
		arqOTA = new ArqTextOpt(this);
		
		led_image = (ImageView) this.findViewById(R.id.led_image);
		if ((Boolean) intent.getSerializableExtra("light") == false) {
			led_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isburning == true) {
					if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_version")) {
						intent = new Intent(DwnLoadActivity.this,
								VersionActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_wifi")) {
						intent = new Intent(DwnLoadActivity.this,
								WifiActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_gprs")) {
						intent = new Intent(DwnLoadActivity.this,
								GPRS_Activity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_tf")) {
						intent = new Intent(DwnLoadActivity.this, TF_Activity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_serialnumber")) {
						intent = new Intent(DwnLoadActivity.this,
								SerialNumberActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_printer")) {
						intent = new Intent(DwnLoadActivity.this,
								PrinterActivity.class);
					} else {
						intent = new Intent(DwnLoadActivity.this,
								ResultTableActivity.class);
					}
					mybundle.putString("download", "ok");
					intent.putExtras(mybundle);
					startActivity(intent);
				}
			}
		}, 3000);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
	}
	protected void onPause() {  
        Log.i(TAG, "###### onPause called. ######");  
        this.finish();
        super.onPause();  
    }	
	@Override
	public void onClick(View ledClick) {
		if (screen_sleep == true) {
			led_image.setBackgroundResource(R.drawable.lucency);
			screen_sleep = false;
			toggleBrightness(this, 200);// 背光
		} else {
			Intent intent;
			Bundle mybundle = new Bundle();
			intent = getIntent();
			mybundle = (Bundle) intent.getExtras();
			isburning = false;
			mybundle.putBoolean("flag_burn", false);
			switch (ledClick.getId()) {
			case R.id.downGetBackToMain: {
				ActivityManager.clearActivity();
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					ActivityManager.trunToSingleTestActivity(DwnLoadActivity.this);
				} else if ((Boolean) intent.getSerializableExtra("alltest") == true) {
					ActivityManager.trunToEntryActivity(DwnLoadActivity.this);
				} else {
					ActivityManager.trunToBurnStartActivity(DwnLoadActivity.this);
				}
				break;
			}
			case R.id.down_test_state: {
				flag_download = 1;
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					mybundle.putString("download", "ok");
					ActivityManager.trunToSingleTestActivity(DwnLoadActivity.this);
				} else {
					mybundle.putString("download", "ok");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(DwnLoadActivity.this);
				}
				break;
			}
			case R.id.downFalse: {
				flag_download = 0;
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					mybundle.putString("download", "ng");
					ActivityManager.trunToSingleTestActivity(DwnLoadActivity.this);
				} else {
					mybundle.putString("download", "ng");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(DwnLoadActivity.this);
				}
				break;
			}
			default:
				break;
			}
		}

	}
	
	// 设置开始下载按钮监听器
	public void downLoadBtnOnClickHandler(View v) {
		/* 是否已经有文件在烧写了, 有则返回 */
		if(downLoadThreadRunning == true) { 
			Log.i(TAG, "Download thread already running. just return.");
			return;
		}
		
		downLoadSysNeeded = cbSys.isChecked();
		downLoadAppNeeded = cbApp.isChecked();
		downLoadTextNeeded = cbText.isChecked();
		
		// Clear display
		tvSta.setText("");
		//是否需要下载OTA文件
		// 是否需要烧写文件
		if(downLoadSysNeeded || downLoadAppNeeded || downLoadTextNeeded) {
			downLoadThread = new DownLoadThread();
			downLoadThread.start();
		}	
	}
	
	// 导入系统文件按钮监听器（ OTA 升级）
	public void importSysFileOnClickHandler(View v) 
	{
		Intent intent = new Intent(DwnLoadActivity.this, FileExplorerActivity.class);
		//启动需要监听返回值的Activity，并设置请求码：requestCode = 0
		startActivityForResult(intent, 0);
	}
	
	// 导入应用文件按钮监听器
	public void importAppFileOnClickHandler(View v) 
	{
		Intent intent = new Intent(DwnLoadActivity.this, FileExplorerActivity.class);
		//启动需要监听返回值的Activity，并设置请求码：requestCode = 1
		startActivityForResult(intent, 1);
	}
	
	// 导入普通文件按钮监听器
	public void importTextFileOnClickHandler(View v) 
	{
		Intent intent = new Intent(DwnLoadActivity.this, FileExplorerActivity.class);
		//启动需要监听返回值的Activity，并设置请求码：requestCode = 2
		startActivityForResult(intent, 2);
	}

	// 获取回传的值
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
		Log.i(TAG, "requestCode = " + requestCode + "; resultCode = " + resultCode);
		
		if(resultCode != RESULT_OK) {
			Log.e(TAG, "onActivityResult with resultCode != RESULT_OK");
			return;
		}
		
		String filePath = data.getStringExtra("path");
		Log.i(TAG, "filePath = " + filePath);
		switch(requestCode) { 
			case 0:  // 返回选取的系统文件（OTA文件）            
	            edSys.setText(filePath);
	            break;  
			case 1: // 返回选取的应用文件名 
	            edApp.setText(filePath);
	            break;  
			case 2: // 返回选取的普通文件名 
	            edText.setText(filePath);
	            break;  
			default:  
	            break; 
		}
	}

	/******************************** 烧写进程总入口,用于控制系统,应用,普通文件的升级 ********************************/
	private class DownLoadThread extends Thread {
		int ret = -1;
		
		public void run() {		
			// Wait download to complete
			sendStageMessageToMain("等待下载...");
			while(threadTestAndSet()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
	                Log.d(TAG, "Waiting didnt work!!");
	                e.printStackTrace();
				}
			}
			
			/* <<<<<<<<< DownLoad System bin file >>>>>>>>> */
			if(downLoadSysNeeded) {
				FutureTask<Integer> future = new FutureTask<Integer>(downLoadSysCallable);
				new Thread(future).start();
				try {
					ret = future.get().intValue();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(ret != 0) {
					arq.releaseUpdateLock(); // unLOCK UART
					downLoadThreadRunning = false;
					return;
				}
			}
			
			//Lock UART
//			arq.getUpdateLock(); 
			
			/* <<<<<<<<< DownLoad application bin file >>>>>>>>> */
			if(downLoadAppNeeded) {
				FutureTask<Integer> future = new FutureTask<Integer>(downLoadAppCallable);
				new Thread(future).start();
				try {
					ret = future.get().intValue();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(ret != 0) {
					arq.releaseUpdateLock(); // unLOCK UART
					downLoadThreadRunning = false;
					return;	
				}
			}
			
			/* <<<<<<<<< DownLoad Text file >>>>>>>>> */
			if(downLoadTextNeeded) {
				FutureTask<Integer> future = new FutureTask<Integer>(downLoadTextCallable);
				new Thread(future).start();
				try {
					ret = future.get().intValue();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(ret != 0) {
					arq.releaseUpdateLock(); // unLOCK UART
					downLoadThreadRunning = false;
					return;					
				}
			}
			
//			arq.releaseUpdateLock(); // unLOCK UART
			downLoadThreadRunning = false;
		}
	}
	/******************************************** 辅助函数 ******************************************/
	/*
	 * 将摘要信息装换为32字节的sha256值
	 * param: 
	 * 		data --- 摘要信息数据
	 * return:
	 * 		!= null: 编码后的32字节sha256数据
	 * 		= null: 失败
	 */
	private static byte[] Sha256Encode(byte[] data) {
		MessageDigest md;
		byte[] retBytes = null;
		
		if(data == null) {
			Log.e(TAG, "Bad Argument.");
			return null;
		}
		
		try {
			md = MessageDigest.getInstance("SHA-256");
			retBytes = md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return retBytes;
	}

	// 用于2个进程同步,返回之前状态值:
	// true --- 已经有线程在运行了,等待; 
	// false --- 没有线程运行,当前可以运行
	private synchronized boolean threadTestAndSet() 
	{
		if(downLoadThreadRunning) {
			return true;
		} else {
			downLoadThreadRunning = true;
			return false;
		}
	} 
	/******************************** OTA 下载线程 ********************************/
	class OTADownLoadCallable implements Callable<Integer> {
		
		private int downLoadType ;
		public OTADownLoadCallable(int type) {
			this.downLoadType = type;
		}
		
		@Override
		public Integer call() throws Exception {
			// TODO Auto-generated method stub
			int ret, fileLen, remail, sendLen;
			int frame_cnt = 0;
			int rec_length = 0;
			byte[] frame_id = new byte[2];
			
			try
			{
				//如果手机插入了SD卡，而且应用程序具有访问SD的权限
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					//用于存储文件数据
					byte[] buf = new byte[MAX_FRAME_DATA_LEN];
					
					// 查看是否已经选择了要烧写的系统文件
					String filePath = edts.get(downLoadType).getText().toString();
					if("".equals(filePath)){
						sendDialogMessageToMain("请指定要下载的" + typeNames.get(downLoadType));
						return Integer.valueOf(-1);
					}
					
					// 创建文件对象
					File dwnFile = new File(filePath);				
					// 检查文件是否存在
					if(!dwnFile.exists()) {
						sendDialogMessageToMain(filePath + " 不存在");
						return Integer.valueOf(-2);
					}
					/* STEP1: <<<<<<<<<<<<< OTA请求下载, 并等待应答  >>>>>>>>>>>>*/
					ret = arqOTA.OTADownload();
					Log.v(TAG, "the OTADownload ret is: " + ret);
					sendStageMessageToMain("ota请求下载" + typeNames.get(downLoadType) + "...");
					if(ret != 0){
						Log.v(TAG, "请求下载失败  返回值: " + ret);
						sendStageMessageToMain("ota请求下载" + typeNames.get(downLoadType) + "失败！");
						return Integer.valueOf(-3);
					}
					/* STEP2: <<<<<<<<<<<<<<<< 发送文件数据, 并且接收返回数据确认  >>>>>>>>>>>>>>>>>> */
					// 创建随机访问文件对象
					RandomAccessFile raf = new RandomAccessFile(dwnFile, "r");					
					sendStageMessageToMain("开始下载" + typeNames.get(downLoadType) + "...");
					// 获取文件长度
					fileLen = (int)raf.length();
					sendLen = 0;      //已发送长度
					frame_cnt = 0;	  //帧序号
					remail = fileLen; //文件长度
					raf.seek(0); // set to file start
					
					while(true){
						try {
							if((rec_length = raf.read(buf)) > 0){
								Log.v(TAG, "length is: " + rec_length + " the tmp is: " + " : " +
											ArqConverts.bytesToHexString(buf));
								ret = arqOTA.OTAWrite(++frame_cnt, buf, frame_id);
								Log.v(TAG, "the OTAWrite ret is: " + ret);
								if(ret != 0) {
									Log.e(TAG, "Send data failed. ret = " + ret + "; frameId = " + ArqConverts.bytesToHexString(frame_id));
									sendDialogMessageToMain("发送" + typeNames.get(downLoadType) +"数据失败, framdId = " 
											+ ArqConverts.bytesToHexString(frame_id));
									return Integer.valueOf(-4);
								}
								sendLen += rec_length;
								/**************** 发送下载进度 ***************/
								sendProgressMessageToMain((float)sendLen / (fileLen));
								if(sendLen == remail)
									break;
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					/* Step3: <<<<<<<<<<<<<<<< OTA 文件下载结束  >>>>>>>>>>>>>>>>>> */
					ret = arqOTA.OTADownloadEnd(frame_cnt);
					if(ret != 0) {
						Log.e(TAG, "Send OTADownloadFinish command failed. ret = " + ret);
						sendDialogMessageToMain("发送" + typeNames.get(downLoadType) + "下载请求命令失败");
						return Integer.valueOf(-5);
					}
					Log.v(TAG, "the OTADownloadFinish ret is: " + ret);
					sendStageMessageToMain("下载" + typeNames.get(downLoadType) + "成功");
					raf.close();
					return Integer.valueOf(ret);
				} else { /* SD卡未插入 */
					sendDialogMessageToMain("SD卡未插入,请检查！");
					return Integer.valueOf(-6);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Integer.valueOf(-7);				
			}						
		}
	}
	
	class DownLoadCallableClass implements Callable<Integer> {
		private int downLoadType = 0;

		public DownLoadCallableClass(int type) {
			this.downLoadType = type;
		}

		@Override
		public Integer call() throws Exception {
			// TODO Auto-generated method stub
			int ret, fileLen, len, remail, sendLen;
			int frameId = 0, i;
			
			final int checkPeriod = 100; // 100ms
			final int checkTimes = 200; // (200 * 100)ms = 20s	
				
			try
			{
				//如果手机插入了SD卡，而且应用程序具有访问SD的权限
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					//用于存储文件数据
					byte[] buf = new byte[ArqUpdate.MAX_FRAME_DATA_LEN];
					
					// 查看是否已经选择了要烧写的系统文件
					String filePath = edts.get(downLoadType).getText().toString();
					if("".equals(filePath)){
						sendDialogMessageToMain("请指定要下载的" + typeNames.get(downLoadType));
						return Integer.valueOf(-1);
					}
					
					// 创建文件对象
					File dwnFile = new File(filePath);				
					// 检查文件是否存在
					if(!dwnFile.exists()) {
						sendDialogMessageToMain(filePath + " 不存在");
						return Integer.valueOf(-2);
					}
					
					// 创建随机访问文件对象
					RandomAccessFile raf = new RandomAccessFile(dwnFile, "r");					
					/* STEP1: <<<<<<<<<<<<< 发送文件摘要信息, 并等待应答  >>>>>>>>>>>>*/
					sendStageMessageToMain("开始下载" + typeNames.get(downLoadType) + "...");
					// 获取文件长度
					fileLen = (int)raf.length();
					// 将文件内容读取到临时缓存区中
					byte[] tmpBuf = new byte[fileLen];
					// 重定位到文件头	
					raf.seek(0); 			
					len = raf.read(tmpBuf, 0, fileLen);
					if(len != fileLen) {
						Log.e(TAG, "Read file: " + dwnFile.getName() + " failed. len = " + len);
						sendDialogMessageToMain("读文件:　" + dwnFile.getName() + " 失败");
						return Integer.valueOf(-3);
					}
					// 计算文件sha256值
					byte[] sha256 = Sha256Encode(tmpBuf);					
					ret = arq.sendTag(downLoadType, dwnFile.getName(), sha256);
					if(ret != 0) {
						Log.e(TAG, "Send down load file tag failed. ret = " + ret);
						sendDialogMessageToMain("发送" + typeNames.get(downLoadType) + "TAG失败");
						return Integer.valueOf(-4);
					}
					
					/* STEP2: <<<<<<<<<<<<<<<< 发送文件数据, 并且接收返回数据确认  >>>>>>>>>>>>>>>>>> */
					sendLen = 0;
					frameId = 0;
					remail = fileLen;
					raf.seek(0); // set to file start
			
					while(remail > 0) {
						len = (remail > ArqUpdate.MAX_FRAME_DATA_LEN) ? ArqUpdate.MAX_FRAME_DATA_LEN : remail;
						
						ret = raf.read(buf, 0, len);
						if(ret != len) {
							Log.e(TAG, "Read file: " + dwnFile.getName() + " failed. ret = " + ret + "; len = " + len);
							sendDialogMessageToMain("读" + typeNames.get(downLoadType) + "出错");
							return Integer.valueOf(-5);
						}
						
						// is it the last frame ?
						if(remail - len == 0) {
							frameId = 0xffff;
						}
						
						ret = arq.sendData(frameId, buf, len);
						if(ret != frameId) {
							Log.e(TAG, "Send data failed. ret = " + ret + "; frameId = " + frameId);
							sendDialogMessageToMain("发送" + typeNames.get(downLoadType) +"数据失败, framdId = " + frameId);
							return Integer.valueOf(-6);
						}
						
						remail -= len;
						sendLen += len;
						frameId++;
						/**************** 发送下载进度 ***************/
						sendProgressMessageToMain((float)sendLen / (fileLen));
					}
					
					sendStageMessageToMain("下载" + typeNames.get(downLoadType) + "成功");
					
					if(downLoadType == ArqUpdate.FILE_TYPE_NORMAL) {
						return Integer.valueOf(0); // download normal file success
					}
					
					/* 注册T1升级监听器 */
					arq.registerProgressListener((android.witsi.arq.ArqReceiverListener) mListener);
					
					/* Step3: <<<<<<<<<<<<<<<< 开始系统文件更新  >>>>>>>>>>>>>>>>>> */
					sendStageMessageToMain("开始升级" + typeNames.get(downLoadType) + "...");
					ret = arq.startUpdate((downLoadType == ArqUpdate.FILE_TYPE_SYSTEM) ? 
							ArqUpdate.START_UPDATE_SYSTEM : ArqUpdate.START_UPDATE_APPLICATION);
					if(ret != 0) {
						Log.e(TAG, "Send start update command failed. ret = " + ret);
						sendDialogMessageToMain("发送" + typeNames.get(downLoadType) + "升级命令失败");
						arq.unregisterProgressListener();
						return Integer.valueOf(-7);
					}
					
					updateResult = UPDATE_RESULT_INIT; // 初始化状态
					// 等待20S超时,一般T1更新不会超过20S
					for(i = 0; i < checkTimes; i++) {
						// delay 100ms first					
						try { 
							Thread.sleep(checkPeriod);
						} catch (InterruptedException e) {
			                Log.d(TAG, "Waiting didnt work!!");
			                e.printStackTrace();
						}
											
						if(updateResult != UPDATE_RESULT_INIT){ 
							break;
						}
					}
					
					if((i == checkTimes) && (updateResult == UPDATE_RESULT_INIT)) {
						Log.e(TAG, "received T1 update progress result. Timeout > 20s");
						sendDialogMessageToMain("接收" + typeNames.get(downLoadType) + "升级进度超时");
						sendStageMessageToMain("升级" + typeNames.get(downLoadType) + "失败");
						ret = -8; // 超时
					} else if(updateResult == UPDATE_RESULT_DONE){
						sendStageMessageToMain("升级" + typeNames.get(downLoadType) + "成功");
						ret = 0;
					} else { // 升级错误提示信息
						sendDialogMessageToMain(typeNames.get(downLoadType) + updateMessages.get(updateResult));
						ret = -9;
					}
					
					// unregister Listener
					arq.unregisterProgressListener();
					raf.close();
					
					return Integer.valueOf(ret);
				} else { /* SD卡未插入 */
					sendDialogMessageToMain("SD卡未插入,请检查！");
					return Integer.valueOf(-10);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Integer.valueOf(-11);				
			}						
		}
		
		// progress frame Listener
		ArqReceiverListener.Stub mListener = new ArqReceiverListener.Stub() {
			@Override
			public void actionPerformed(ArqMessage msg) throws RemoteException {
				// TODO Auto-generated method stub				
				int ret;						
				byte[] result = new byte[3];
				
				ret = msg.getCmdFrameData(result, 0, 3); // <2-Bytes respond> <1-Bytes Progress>
				if(ret != 3) {
					updateResult = UPDATE_RESULT_INVALID_PACKAGE;
					return;
				}
				
				// 解析返回结果
				ret = arq.getUpdateResult(result);
				if(ret >= 0) {
					if(ret == 100) {
						updateResult = UPDATE_RESULT_DONE;
					}
					sendProgressMessageToMain((float) ret / 100); // 返回T1烧写进度
				} else {
					if(ret == ArqUpdate.RESULT_PARTITION_NOT_FOUND) {
						// Partition not found
						updateResult = UPDATE_RESULT_PARTITION_NOT_FOUND;	
					} else if(ret == ArqUpdate.RESULT_WRONG_APPLICATION_TYPE) {
						// wrong application type
						updateResult = UPDATE_RESULT_WRONG_APPLICATION_TYPE;
					} else if(ret == ArqUpdate.RESULT_APPLICATION_NOT_EXIST) {
						// application not exist
						updateResult = UPDATE_RESULT_APPLICATION_NOT_EXIST;
					} else {
						// error not defined
						updateResult = UPDATE_RESULT_UNKNOWN_ERROR;
					}
				}
			}
		};		
	}
	
	private void sendDialogMessageToMain(String text) {
    	Message msg = new Message();
    	msg.what = 0x000;
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("dialog", text);

    	/* send message */
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
	}
	
	private void sendStageMessageToMain(String text) {
    	Message msg = new Message();
    	msg.what = 0x100;
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("stage", text);

    	/* send message */
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
	}	
	
	private void sendProgressMessageToMain(float progress) {
    	Message msg = new Message();
    	msg.what = 0x200;
    	
    	Bundle bundle = new Bundle();
    	bundle.putFloat("progress", progress);

    	/* send message */
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
	}
	
	//ui主线程中运行该方法,用于: 显示下载进度, 显示弹窗信息, 下载阶段信息 
	private static class MyHandler extends Handler {
		private final WeakReference<DwnLoadActivity> mActivity;
		
		public MyHandler(DwnLoadActivity activity) {
			mActivity = new WeakReference<DwnLoadActivity> (activity);
		}

		public void handleMessage(Message msg) {
			DwnLoadActivity outer = mActivity.get();
			
			if(outer != null) {					
				if(msg.what == 0x000) { // 显示弹窗信息
					String dlgMsg = msg.getData().getString("dialog");
					
		            new AlertDialog.Builder(outer).setTitle("错误信息")  
		            .setMessage(dlgMsg)  
		            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {  
		                @Override  
		                public void onClick(DialogInterface dialog, int which) {  
		                      
		                }  
		            }).show();  	            
					return;
				}
			
				if(msg.what == 0x100) { // 显示文件烧写阶段
					String staMsg = msg.getData().getString("stage");
					String oldText = outer.tvSta.getText().toString();
					outer.tvSta.setText(oldText + staMsg + "\n");
					return;
				}
				
				if(msg.what == 0x200) { // display proccess bar
					float progressFloat = msg.getData().getFloat("progress");
					int progressInt = (int)(progressFloat * 100);
					outer.bar.setProgress(progressInt);
				}
			}
		}
	}
	
	
	/************************** 事件监听申明区 ***************************/
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
            isExit.setButton("确定", listener);  
            isExit.setButton2("取消", listener);  
            // 显示对话框  
            isExit.show();  
  
        }  
          
        return false;  
          
    }  
    /**监听对话框里面的button点击事件*/  
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
    {  
        public void onClick(DialogInterface dialog, int which)  
        {  
            switch (which)  
            {  
            case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序  
            	ActivityManager.clearActivity();
            	DwnLoadActivity.this.finish();  
                break;  
            case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框  
                break;  
            default:  
                break;  
            }  
        }  
    };    
	//以上为点击返回键填出提示窗口	
	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				led_image.setBackgroundResource(R.drawable.lucency);
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

	//用于接收
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				ifCloseActivity =false;
				Log.i(TAG, "home键按下");
			}

		}

	};
}