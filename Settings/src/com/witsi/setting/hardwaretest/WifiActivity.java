package com.witsi.setting.hardwaretest;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.IntentFilter;

public class WifiActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = WifiActivity.class.getSimpleName();
	private final static boolean D = false;
	private Context context = WifiActivity.this;
	/************************* 申明区 *********************************/
	AlertDialog.Builder mAlertDialogBuilder;
	AlertDialog mAlertDialog;
	View main = null;
	TextView current_wifi;
	Button wifi_choose;
	Button wifi_set;
	ImageView wifi_image;
	private Button button_wifi_false;
	private Button button_wifi_return;
	private Button button_wifi_test_state;
	private Button button_wifi_net_text;
	public TextView text_wifi_state;
	private TextView text_wifi_list;
	private TextView wifi_network_info;
	private TextView wifi_ok;
	private TextView wifi_level;
	
	WifiManager mWifiManager;	
	private WifiAdmin wifiAdmin;
	private ScanResult mScanResult;
	private List<ScanResult> listResult;
	Handler handler;
// Handler timerHandler;
	ClientThread clientThread;
	PingThread mPingThread;
	ArrayList<String> lstWifi;
	
	boolean isburning = false;
	boolean screen_sleep = false;
	boolean just_wifi_flag = false;
	boolean timer_out = false;
	boolean wifi_test_over = false;
	boolean pingok = false;
	
	public int timerdata = 0;
	int k = 1;
	String receivedata;
	String defaultaddr = "192.168.1.1";
	String addr1 = "10.83.50.111";
	String addr2 = "www.baidu.com";
	String addr3 = "192.168.2.1";
	int defaultNum = 4;
	
	public BufferedReader br = null;
	private StringBuffer stringBuffer = new StringBuffer();
	
	
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
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		if (config.getBoolean("alltest", false) == true) {
			if(WifiTest.isWifiTestSucc)
			{
				Intent intent = new Intent(context, BuzzerActivity.class);
				editor.putString("wifi", "ok");
				editor.commit();
				startActivity(intent);
				finish();
				return;
			}
		}

		isburning = config.getBoolean("flag_burn", false);
		if (isburning == true) {
			getLayoutInflater();
			// 假装隐藏……好吧~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_wifi_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_wifi_activity);
		}
	
		/************************ 成员初始化区 *********************************/
		setMobileData(this, false);
		wifiAdmin = new WifiAdmin(context);
		wifi_set = (Button) this.findViewById(R.id.wifi_set);
		View v = findViewById(R.id.ll_tool);
		button_wifi_false = (Button) this.findViewById(R.id.fail);
		button_wifi_return = (Button) this.findViewById(R.id.back);
		button_wifi_test_state = (Button) this.findViewById(R.id.pass);
		button_wifi_net_text = (Button) this.findViewById(R.id.test);

		text_wifi_state = (TextView) this.findViewById(R.id.wifi_state);
		text_wifi_list = (TextView) this.findViewById(R.id.wifi_list);
		wifi_ok = (TextView) this.findViewById(R.id.wifi_ok);
		wifi_network_info = (TextView) this.findViewById(R.id.wifi_network_info);
		wifi_choose = (Button) this.findViewById(R.id.wifi_choose);
		current_wifi = (TextView)this.findViewById(R.id.current_wifi);
		wifi_level = (TextView)this.findViewById(R.id.wifi_level);
		
		/************************ 监听事件初始化区 ******************************/
		wifi_choose.setOnClickListener(this);
		current_wifi.setText("当前连接wifi为\n" + "——" + wifiAdmin.GetSSID());
		wifi_level.setText("信号强度：" + wifiAdmin.GetWifiLevel());		
		wifi_set.setOnClickListener(this);
		button_wifi_net_text.setOnClickListener(this);
		button_wifi_return.setOnClickListener(this);
		button_wifi_false.setOnClickListener(this);
		button_wifi_test_state.setOnClickListener(this);
		wifi_network_info.addTextChangedListener(netTestWatcher);// 非拷机网络数据对接
		/************************* 初始化 **********************************/

		wifi_image = (ImageView) this.findViewById(R.id.wifi_image);
		if (config.getBoolean("light", true) == false) {
			wifi_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		//注册监听wifi状态
		registerReceiver(wifiChangeReceiver, getIntentFilter());
		if (wifiAdmin.Wifistate() == 1)
			text_wifi_state.setText("wifi已开启");
		else{
			text_wifi_state.setText("wifi已关闭");
			wifiAdmin.OpenWifi();
		}

		FyLog.i("Wifi界面", "开启20秒定时器");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (wifi_test_over == false) {
					FyLog.i("Wifi界面", "检测时间到");
					wifi_ok.setText("失败");
					timer_out = true;
					isSleepExit = false;
					if (isburning == true) {
						editor.putString("wifi", "ng");
						editor.putInt("error", 1);
						editor.commit();
					}
				}
			}
		}, 20000);
		//开始测试
		handler = new Handler() // ①
		{
			@Override
			public void handleMessage(Message msg) {
				// 如果消息来自于子线程
				// 将读取的内容追加显示在文本框中
				if (msg.what == 0x123) {
					if (msg.obj.toString() != "") {
						receivedata = msg.obj.toString();
						wifi_network_info.append("\n");
						wifi_network_info.append((msg.obj.toString()));
					}
				}
			}
		};
		if (wifiAdmin.Wifistate() == 0) {
			wifiAdmin.OpenWifi();
		}else{
			//获取wifi列表 显示在左边灰色区域
			getAllNetworkLisr();
		}
		text_wifi_state.setText("wifi已开启");
		if (wifiAdmin.isWifiConnected(context) == true) {
			// if (isburning == false) {
			text_wifi_state.setText("wifi已连接");
			wifi_ok.setText("测试中");
			FyLog.i("wifi", "wifi开启socket线程");
			// 客户端启动ClientThread线程创建网络连接、读取来自服务器的数据
			mPingThread = new PingThread(defaultaddr, 4, handler);
			new Thread(mPingThread).start(); // ①
			FyLog.i("wifi", "wifi的socket线程开启");
			// }
		} else {
			wifi_ok.setText("已断开");
			text_wifi_state.setText("wifi已断开");
			Toast.makeText(context, "请先连接wifi", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/* Called when the application resumes */
	@Override
	protected void onResume() {
		FyLog.i(TAG, "onResume");
		super.onResume();
		getAllNetworkLisr();
	}
	
	public IntentFilter getIntentFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction("android.net.wifi.STATE_CHANGE");
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		//监听wifi信号改变
		filter.addAction("android.net.wifi.RSSI_CHANGED");
		return filter;
	}
	
	boolean ifok = false;
	private TextWatcher netTestWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
			boolean ifover = false;
			String pingdata = wifi_network_info.getText().toString();
			char[] pingdatachar = new char[pingdata.length()];
			
			pingdata.getChars(0, pingdata.length(), pingdatachar, 0);
			for(int a = 0;a<pingdatachar.length;a++){
				
				if((pingdatachar[a] == '率')){
					FyLog.i("pingdata", "pingdata:"+"\n\n\n"+pingdata);
					ifover = true;
					FyLog.i("pingdata",(a+3)+","+(pingdata.length()-2));
					String lostdata = pingdata.subSequence(a+2, pingdata.length()-2).toString();
					FyLog.i("pingdata", "lostdata="+lostdata);
					FyLog.i("pingdata", "lostdata="+" +4 errors, 100");
					
					for(int k =0;k<pingdatachar.length;k++){
						FyLog.i("pingdata", "k="+k+"   "+pingdatachar[k]);
					}
					if(lostdata.equals(" +4 errors, 100")){
						FyLog.i("pingdata","完全丢包");
						wifi_ok.setText("失败");	
						ifok = false;
					}else{
						FyLog.i("pingdata","没有全丢包");
						wifi_ok.setText("通过");
						ifok = true;
					}
					break;
				}
			}
			if(ifover == true){
				wifi_test_over = true;
				if(ifok == true){
					editor.putString("wifi", "ok");
				}else{
					editor.putString("wifi", "ng");
					editor.putInt("error", 1);
				}
				editor.commit();
				isSleepExit = false;
				//拷机选项
				if (config.getBoolean("alltest", false) == true) {
					if(D)FyLog.i("wifi界面", "下个界面选取Buzzer");
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(context);
				}
			}
		}
		@Override
		public void afterTextChanged(Editable arg0) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
	};


	/************************** 成员方法定义区 ***************************/
	private void getAllNetworkLisr() {// 获取wifi列表
		if (wifiAdmin.Wifistate() == 1) {
			// 每次点击扫描之前清空上一次的扫描结果
			if (stringBuffer != null) {
				stringBuffer = new StringBuffer();
			}

			// 开始扫描网络
			wifiAdmin.StartScan();
			listResult = wifiAdmin.GetWifiList();
			stringBuffer = stringBuffer.append("");
			if (listResult != null) {
				for (int i = 0; i < listResult.size(); i++) {
					mScanResult = (ScanResult) listResult.get(i);
					// 得到网络的SSID：the network name
					stringBuffer = stringBuffer
							.append("SSID: " + mScanResult.SSID).append("\n")
							.append("BSSID: " + mScanResult.BSSID).append("\n")
							.append("类型： " + mScanResult.capabilities)
							.append("\n")
							.append("频率： " + mScanResult.frequency)
							.append("\n")
							.append("信号强度：" + mScanResult.level + "dbm")
							.append("\n").append("\n\n");
				}
				text_wifi_list.setText("扫描到的所有Wifi网络：\n"
						+ stringBuffer.toString());
				stringBuffer = stringBuffer.delete(0, stringBuffer.length());
			}
		} else {
			Toast.makeText(context, "请先打开wifi", Toast.LENGTH_SHORT)
					.show();
			stringBuffer = stringBuffer.delete(0, stringBuffer.length());
			text_wifi_list.setText("扫描到的所有Wifi网络：\n" + stringBuffer.toString()
					+ "\n wifi已经关闭");
		}
	}

	private void testNet() {// 下载测试
		if (wifiAdmin.Wifistate() == 1) {
			mPingThread = new PingThread(defaultaddr, defaultNum, handler);
			new Thread(mPingThread).start(); // ①
		} else {
			Toast.makeText(context, "请先打开wifi", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onClick(View wifiClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			wifi_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			wifi_test_over = true;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (wifiClick.getId()) {
			case R.id.pass: {
				if(ifok){
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("wifi", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(context);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("wifi", "ok");
						editor.commit();
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(context);
					}else {
						ActivityManagers.trunToBurnStartActivity(context);
					} 
				}
				break;
			}
			case R.id.fail: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("wifi", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(context);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("wifi", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(context);
				}else {
					ActivityManagers.trunToBurnStartActivity(context);
				} 
				break;
			}
			case R.id.back: {
				ActivityManagers.clearActivity();
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(context);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(context);
				} else {
					ActivityManagers.trunToBurnStartActivity(context);
				} 
				break;
			}
			case R.id.test: {
				if(wifiAdmin.isWifiConnected(context)){
					wifi_ok.setText("测试中");
					timer_out = false;
					wifi_test_over = false;
					text_wifi_list.setText("");
					getAllNetworkLisr();
					//重新测试
					testNet();
				}else{
					wifi_ok.setText("已断开");
					text_wifi_state.setText("wifi已断开");
					text_wifi_state.setBackgroundResource(R.color.red);
				}
				break;
			}
			case R.id.wifi_set: {
				// 装载/res/layout/login.xml界面布局
				final TableLayout loginForm1 = (TableLayout) getLayoutInflater()
						.inflate(R.layout.hardware_login, null);
				new AlertDialog.Builder(this)
				// 设置对话框的标题
					.setTitle("请输入")
					// 设置对话框显示的View对象
					.setView(loginForm1)
					// 为对话框设置一个“确定”按钮
					.setPositiveButton("确定", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							defaultaddr = ((EditText) loginForm1
									.findViewById(R.id.addr)).getText()
									.toString();
							defaultNum = Integer
									.valueOf(((EditText) loginForm1
											.findViewById(R.id.pagnum))
											.getText().toString());
						}
					})
					.setNegativeButton("取消", null)
					.create()
					.show();
				break;
			}
			case R.id.wifi_choose: {
				final RelativeLayout loginForm2 = (RelativeLayout) getLayoutInflater()
						.inflate(R.layout.hardware_login_wifichoose, null);
				lstWifi = new ArrayList<String>();
				Button scan = (Button) loginForm2.findViewById(R.id.scan);
				Button refresh = (Button) loginForm2.findViewById(R.id.refresh);
				ListView lstWifiView = (ListView) loginForm2
						.findViewById(R.id.wifi_list_view);
				final ListAdapter mListAdapter = new ListAdapter(
						context, lstWifi);
				lstWifiView.setAdapter(mListAdapter);
				lstWifiView.setOnItemClickListener(mOnItemClickListener);
				wifiAdmin.StartScan();

				listResult = wifiAdmin.GetWifiList();
				lstWifi.clear();
				mListAdapter.notifyDataSetChanged();
				if (listResult != null) {
					for (int i = 0; i < listResult.size(); i++) {
						FyLog.i("login", "list:" + "\n"
								+ listResult.get(i).toString());
						mScanResult = (ScanResult) listResult.get(i);
						if (mScanResult.capabilities.equals("[ESS]")) {
							lstWifi.add("<传输开放>"+"SSID="+mScanResult.SSID);
							FyLog.i("login", "lstWifi:" + "SSID="
									+ mScanResult.SSID);
						}
						if (mScanResult.capabilities.equals("[WPS][ESS]")) {
							lstWifi.add("<传输加密>"+"SSID="+mScanResult.SSID);
							FyLog.i("login", "lstWifi:" + "SSID="
									+ mScanResult.SSID);
						}
					}
					mListAdapter.notifyDataSetChanged();
				}

				scan.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						//扫描WIFI
						wifiAdmin.StartScan();
						lstWifi.clear();
						mListAdapter.notifyDataSetChanged();
					}
				});
				refresh.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						//刷新WIFI列表
						lstWifi.clear();
						listResult = wifiAdmin.GetWifiList();
						if (listResult != null) {
							for (int i = 0; i < listResult.size(); i++) {
								FyLog.i(TAG, "list:" + "\n"
										+ listResult.get(i).toString());
								mScanResult = (ScanResult) listResult.get(i);
								if (mScanResult.capabilities.equals("[ESS]")) {
									lstWifi.add(0, "<传输开放>"+"SSID="+mScanResult.SSID);
									FyLog.i(TAG, "<传输开放>:" + "SSID="
											+ mScanResult.SSID);
								}else if (mScanResult.capabilities.equals("[WPS][ESS]")) {
									lstWifi.add("<传输加密>"+"SSID="+mScanResult.SSID);
									FyLog.i(TAG, "<传输加密>:" + "SSID="
											+ mScanResult.SSID);
								}
							}
							mListAdapter.notifyDataSetChanged();
						}
					}
				});
				if(lstWifi != null){
					mAlertDialogBuilder = new AlertDialog.Builder(this);
					mAlertDialogBuilder
					// 设置对话框的标题
						.setTitle("请选择开放热点")
						.setView(loginForm2)
						// 设置对话框显示的View对象
						.setView(loginForm2)
						// 为对话框设置一个“取消”按钮
						.setNegativeButton("关闭窗口", null)
						.setPositiveButton("断开连接", new OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								isDeviceDisCon = true;
								wifiAdmin.disconnected();
							}
						})
						.create();
					mAlertDialog = mAlertDialogBuilder.show();
				}else{
					new AlertDialog.Builder(this)
					// 设置对话框的标题
						.setTitle("没有找到开放热点").setView(loginForm2)
						// 设置对话框显示的View对象
						.setView(loginForm2)
						.setNeutralButton("断开连接", new OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								isDeviceDisCon = true;
								wifiAdmin.disconnected();
							}
						})
						// 为对话框设置一个“取消”按钮
						.setNegativeButton("关闭窗口", null)
						// 创建、并显示对话框
						.create().show();
				}
			break;
			}
			default:
				break;
			}
		}
	}

	private String wifiSSID = null;
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
				long arg3) {
			mAlertDialog.dismiss();
			FyLog.i(TAG, "开始连接！！！");
			String wifiSSIDdata = lstWifi.get(position).toString();
			wifiSSID = wifiSSIDdata.substring(11, wifiSSIDdata.length());
			FyLog.i(TAG, "wifiSSID: " + wifiSSID);
			
			if(wifiAdmin.isWifiConnected(context) 
					&& wifiAdmin.GetSSID().equals("\"" + wifiSSID + "\"")){
				Toast.makeText(context, "该wifi已经连接", Toast.LENGTH_SHORT)
						.show();
			}else{
				if(wifiAdmin.isWifiConnected(context)){
					Toast.makeText(context, "wifi正在断开……", Toast.LENGTH_SHORT).show();
					wifiAdmin.disconnected();
				}else{
					wifiAdmin.ContNoPassWordNet(wifiSSID);
					wifi_ok.setText("连接中");
				}
			}
		}
	};
	
	private boolean isDeviceDisCon = false;
	private BroadcastReceiver wifiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			String action = intent.getAction();
			wifiAdmin = new WifiAdmin(context);
			if(action.equals("android.net.wifi.RSSI_CHANGED")){
				wifi_level.setText("信号强度：" + wifiAdmin.GetWifiLevel());
			//WIFI状态改变
			}if(action.equals("android.net.wifi.STATE_CHANGE")){
				
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if(info != null){
					if(info.getState() == NetworkInfo.State.CONNECTED){
						Log.e(TAG, "connect the wifi");
						text_wifi_state.setText("wifi已连接");
						text_wifi_state.setBackgroundResource(R.color.green);
						if(wifi_ok.getText().equals("测试中")
								||wifi_ok.getText().equals("通过")){
						}else{
							wifi_ok.setText("已连接");
						}
						current_wifi.setText("当前连接wifi为\n"+"——"+wifiAdmin.GetSSID());
						wifi_level.setText("信号强度：" + wifiAdmin.GetWifiLevel());
						FyLog.i("wifi", "当前连接wifi为\n"+"——"+wifiAdmin.GetSSID());
					}else if(info.getState() == NetworkInfo.State.CONNECTING){
						
					}else if(info.getState() == NetworkInfo.State.DISCONNECTED){
						Log.e(TAG, "disconnect");
						//提示wifi已断开
						text_wifi_state.setText("wifi已断开");
						text_wifi_state.setBackgroundResource(R.color.red);
						wifi_ok.setText("已断开");
						current_wifi.setText("当前连接wifi为\n" + "——" + "无");
						wifi_level.setText("信号强度：" + wifiAdmin.GetWifiLevel());
						Toast.makeText(context, "WIFI已断开", Toast.LENGTH_SHORT).show();
						//如果只是断开连接则不继续连接wifi
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								wifiAdmin = new WifiAdmin(context);
								wifiAdmin.ContNoPassWordNet(wifiSSID);
								wifi_ok.setText("连接中");
								current_wifi.setText("当前连接wifi为\n" + "——" + wifiAdmin.GetSSID());
								wifi_level.setText("信号强度：" + wifiAdmin.GetWifiLevel());
								FyLog.i(TAG, "当前连接wifi为\n" + "——" + wifiSSID);
							}
						}, 1000);
					}
				}
			}
		}
	};
	/**
	 * 设置手机的移动数据
	 */
	public static void setMobileData(Context pContext, boolean pBoolean) {

		try {

			ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			Class ownerClass = mConnectivityManager.getClass();

			Class[] argsClass = new Class[1];
			argsClass[0] = boolean.class;

			Method method = ownerClass.getMethod("setMobileDataEnabled",
					argsClass);

			method.invoke(mConnectivityManager, pBoolean);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("移动数据设置错误: " + e.toString());
		}
	}

	/**
	 * 返回手机移动数据的状态
	 * 
	 * @param pContext
	 * @param arg
	 *            默认填null
	 * @return true 连接 false 已断开
	 */
	public static boolean getMobileDataState(Context pContext, Object[] arg) {

		try {

			ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			Class ownerClass = mConnectivityManager.getClass();

			Class[] argsClass = null;
			if (arg != null) {
				argsClass = new Class[1];
				argsClass[0] = arg.getClass();
			}

			Method method = ownerClass.getMethod("getMobileDataEnabled",
					argsClass);

			Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

			return isOpen;

		} catch (Exception e) {
			// TODO: handle exception

			System.out.println("得到移动数据状态出错");
			return false;
		}

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		unregisterReceiver(wifiChangeReceiver);
		clientThread = null;
		br = null;
		
		setMobileData(this, true);
		
		stringBuffer = null;
		wifiAdmin = null;
		mScanResult = null;
		
		text_wifi_list.setText("");
		text_wifi_state.setText("wifi已关闭");
		
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "进入Stop状态");
		if(config.getBoolean("alltest", false) == true){
			wifiAdmin.CloseWifi();
		}
		System.gc();
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

	// 以上为点击返回键填出提示窗口

	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// 背光
			switch (event.getAction()) {
			// 触摸屏幕时刻
			case MotionEvent.ACTION_DOWN:
				wifi_image.setBackgroundResource(R.drawable.bg_transport);
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
