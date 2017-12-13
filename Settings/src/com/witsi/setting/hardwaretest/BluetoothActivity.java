package com.witsi.setting.hardwaretest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.witsi.adapter.BtLvAdapter;
import com.witsi.bluetooth.WtBtListener.ConnectListerner;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ClsUtils;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BluetoothActivity extends Activity implements OnClickListener, OnItemClickListener{

	private String TAG = "BluetoothActivity";
	private Context context = BluetoothActivity.this;
	private static final boolean D = true;
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	
	private Button main_return;
	private Button main_ok;
	private Button main_false;
	private Button main_test;
	private ImageView main_image;
	private View main = null;
	private ListView lv_scan;
	private TextView tv_name, tv_mac;
	private CheckBox cb_version;
	private Button btn_scan, btn_connect, btn_bond, btn_send;
	
	private BtLvAdapter adapter;
	private List<BluetoothDevice> list;
	private BluetoothAdapter bt_adapter = null;
	private BluetoothDevice bt_device = null;
	private BluetoothSocket socket = null;
	private BluetoothAdapter.LeScanCallback mLeScanCallback = null;
	
	private boolean isClassicBt = true;
	private SharedPreferences config;
	private Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		setContentView(R.layout.hardware_bluetooth_activity);
		
		initViews();
	}

	private void initViews() {
		// TODO Auto-generated method stub
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		list = new ArrayList<BluetoothDevice>();
		lv_scan = (ListView) findViewById(R.id.lv_scan);
		adapter = new BtLvAdapter(context, list);
		lv_scan.setAdapter(adapter);
		tv_name = (TextView) findViewById(R.id.device_name);
		tv_mac = (TextView) findViewById(R.id.mac);
		btn_bond = (Button) findViewById(R.id.bond);
		btn_scan = (Button) findViewById(R.id.scan);
		btn_connect = (Button) findViewById(R.id.connect);
		btn_send = (Button) findViewById(R.id.send_file);
		cb_version = (CheckBox) findViewById(R.id.version);
		
		View v = findViewById(R.id.ll_tool);
		main_return = (Button) v.findViewById(R.id.back);
		main_ok = (Button) v.findViewById(R.id.pass);
		main_false = (Button) v.findViewById(R.id.fail);
		main_test = (Button) v.findViewById(R.id.test);
		main_return.setOnClickListener(BluetoothActivity.this);
		main_ok.setOnClickListener(BluetoothActivity.this);
		main_false.setOnClickListener(BluetoothActivity.this);
		main_test.setOnClickListener(BluetoothActivity.this);
		
		btn_bond.setOnClickListener(this);
		btn_connect.setOnClickListener(this);
		btn_scan.setOnClickListener(this);
		btn_send.setOnClickListener(this);
		lv_scan.setOnItemClickListener(this);
		cb_version.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@SuppressLint("NewApi")
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(btn_scan.getText().toString().equals("正在搜索...")){
					if(isClassicBt){
						bt_adapter.cancelDiscovery();
					}else{
						bt_adapter.stopLeScan(mLeScanCallback);
					}
					btn_scan.setText("搜索设备");
				}
				isClassicBt = !isChecked;
				bt_adapter = null;
				list.clear();
				adapter.notifyDataSetChanged();
				if(isClassicBt){
					btn_bond.setEnabled(true);
					bt_adapter = BluetoothAdapter.getDefaultAdapter(); 
				}else{
					btn_bond.setEnabled(false);
					// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
			        // BluetoothAdapter through BluetoothManager.
			        final BluetoothManager bluetoothManager =
			                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			        bt_adapter = bluetoothManager.getAdapter();
				}
			}
		});
		
		initBluetoothDevice();
		String filePath = "/mnt/sdcard/log.txt";
		File file = new File(filePath);
		if(!file.exists()){
			copyBigDataToSD(filePath);
		}
	}

	@SuppressLint("NewApi")
	private void initBluetoothDevice() {
		// TODO Auto-generated method stub
		// Initializes a Bluetooth adapter. 
        bt_adapter = BluetoothAdapter.getDefaultAdapter(); 
        // Checks if Bluetooth is supported on the device.
        if (bt_adapter == null) {
        	Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        	mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        	    @Override
        	    public void onLeScan(final BluetoothDevice device,final int rssi, byte[] scanRecord) {
        	        runOnUiThread(new Runnable() {
        	            @Override
        	            public void run() {
        	            	FyLog.v(TAG, "add the device to the list: " + device.getName() + " : " + device.getAddress());
        	                if(!list.contains(device)){
        	                	list.add(device);
        	                	adapter.notifyDataSetChanged();
        	                }
        	            }
        	        });
        	    }
        	};
        }else{
        	cb_version.setEnabled(false);
        }
        
        // Ensures Bluetooth is enabled on the device. 
        // If Bluetooth is not currently enabled
        if (!bt_adapter.isEnabled()) {
            if (!bt_adapter.isEnabled()) {
            	bt_adapter.enable();
            }
        }
        
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerReceiver(mReceiver, getIntentFilter());
	}
	
	private IntentFilter getIntentFilter(){
		IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        return filter;
	}
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		isSleepExit = false;
		switch (v.getId()) {
		case R.id.back: {
			if (config.getBoolean("singletest", false) == true) {
				ActivityManagers.trunToSingleTestActivity(BluetoothActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				ActivityManagers.trunToEntryActivity(BluetoothActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(BluetoothActivity.this);
			} 
			break;
		}
		case R.id.pass: {
			if (config.getBoolean("singletest", false) == true){
				editor.putString("bluetooth", "ok");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(BluetoothActivity.this);
			} else if (config.getBoolean("alltest", false) == true){
				editor.putString("bluetooth", "ok");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(BluetoothActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(BluetoothActivity.this);
			} 
			break;
		}
		case R.id.fail: {
			if (config.getBoolean("singletest", false) == true) {
				editor.putString("bluetooth", "ng");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(BluetoothActivity.this);
			} else if (config.getBoolean("alltest", false) == true){
				editor.putString("bluetooth", "ng");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(BluetoothActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(BluetoothActivity.this);
			} 
			break;
		}
		case R.id.test: {
			// /* 重新测试一次 */
			 break;
		}
		case R.id.scan: {
			if(btn_scan.getText().toString().equals("搜索设备")){
				if(list.size() > 0){
					list.clear();
					adapter.notifyDataSetChanged();
				}
				btn_scan.setText("正在搜索...");
				if(isClassicBt){
					bt_adapter.startDiscovery();
				}else{
					bt_adapter.startLeScan(mLeScanCallback);
				}
			}else{
				btn_scan.setText("搜索设备");
				if(isClassicBt){
					bt_adapter.cancelDiscovery();
				}else{
					bt_adapter.stopLeScan(mLeScanCallback);
				}
			}
			break;
		}
		case R.id.bond: {
			if(bt_device != null){
				btn_scan.setText("搜索设备");
				if(isClassicBt){
					bt_adapter.cancelDiscovery();
				}else{
					bt_adapter.stopLeScan(mLeScanCallback);
				}
				if(btn_bond.getText().toString().equals("配对")){
					BluetoothDevice device = bt_adapter.getRemoteDevice(bt_device.getAddress());
					if(device == null){
						FyLog.v(TAG, "获取远程设备信息失败！");
					}else{
						bt_device = device;
					}
					//用服务号得到socket
					BluetoothSocket tmp_socket = null;
		            try{
		            	tmp_socket = bt_device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
		            	if(tmp_socket != null){
		            		socket = tmp_socket;
		            	}
		            }catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            //BLE 先发起配对，再连接
		            if(isClassicBt){
		            	try {
		    				ClsUtils.createBond(bt_device.getClass(), bt_device);
		    				ClsUtils.BtSetPairingConfirmation(bt_device.getClass(), bt_device);
		    			} catch (Exception e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			}
		            }
				}else{
					try {
						ClsUtils.removeBond(bt_device.getClass(), bt_device);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			break;
		}
		case R.id.connect: {
			break;
		}
		case R.id.send_file: {
			doSendFileByBluetooth();
			break;
		}
		default:
			break;
		}
	}
/**
 * **************** 搜索设备 ****************************************************************************************************************************	
 */
	// The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            FyLog.v(TAG, "the action is: " + action);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //开始搜索蓝牙
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            	
            //查找到蓝牙
            }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                FyLog.v(TAG, "add the device to the list: " + device.getName() + " : " + device.getAddress());
                if(!list.contains(device)){
                	list.add(device);
                	adapter.notifyDataSetChanged();
                }
            //蓝牙查找结束
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            	btn_scan.setText("搜索设备");
            /**
             * ****************** 蓝牙广播通知 ******************************************************************************
             */			
            }else if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
				try {
					FyLog.v(TAG, "自动设置PIN密码");
//            					if(isClassicBt)
					ClsUtils.setPin(bt_device.getClass(), bt_device, "1234");
//            					else
//            						ClsUtils.BtSetPairingConfirmation(bt_device.getClass(), bt_device);
					if(android.os.Build.VERSION.SDK_INT >= 18){
						this.abortBroadcast();
					}
					return;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }else if(device.getBondState() == BluetoothDevice.BOND_BONDED){
        		//BLE下配对成功后再连接
            	adapter.notifyDataSetChanged();
            	btn_bond.setText("移除配对");
        	}else if(device.getBondState() == BluetoothDevice.BOND_NONE){
        		adapter.notifyDataSetChanged();
            	btn_bond.setText("配对");
        	}
        }
    };
    
/**
 * **************** 配对 ****************************************************************************************************************************	
 */
/**
 * **************** 连接 ****************************************************************************************************************************	
 */
    private void doSendFileByBluetooth() {
		String filePath = "/mnt/sdcard/log.txt";
		final File file = new File(filePath);
		if(file.exists()){
			new Handler().post(new Runnable() {
				
				@Override
				public void run() {
					//调用android分享窗口
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(intent);
				}
			});
		}else{
			Toast.makeText(context, "/mnt/sdcard/log.txt不存在", Toast.LENGTH_SHORT).show();
		}
	}
    private void copyBigDataToSD(String strOutFileName){  
        InputStream myInput;  
        OutputStream myOutput;
		try {
			myOutput = new FileOutputStream(strOutFileName);
			myInput = this.getAssets().open("log.txt");  
	        byte[] buffer = new byte[1024];  
	        int length = myInput.read(buffer);
	        while(length > 0)
	        {
	            myOutput.write(buffer, 0, length); 
	            length = myInput.read(buffer);
	        }
	        myOutput.flush();  
	        myInput.close();  
	        myOutput.close();      
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
/**
 * **************** 发送文件 ****************************************************************************************************************************	
 */
	private BluetoothDevice device = null;
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		BluetoothDevice device = list.get(arg2);
		tv_name.setText("设备名：" + device.getName());
		tv_mac.setText("MAC：" + device.getAddress());
		if(device.getBondState() == BluetoothDevice.BOND_BONDED){
			btn_bond.setText("移除配对");
		}else{
			btn_bond.setText("配对");
		}
		bt_device = device;
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "onStop");
		unregisterReceiver(mReceiver);
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
