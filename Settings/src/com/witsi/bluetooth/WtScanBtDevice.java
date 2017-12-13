package com.witsi.bluetooth;

import com.witsi.bluetooth.WtBtListener.SearchListerner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



public class WtScanBtDevice {
	
	protected static final int MSG_FIND_DEVICE = 0;

	protected static final int MSG_BLOOTH_FINISH = 1;
	
	private Context mContext;
	private SearchListerner searchListener;
	private BluetoothAdapter mBtAdapter;
	private static boolean mIsDiscovering = false;
	 @SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback;
	private boolean isBLe = false;
	
	public WtScanBtDevice(Context context)
	{
	
		initDevice(context, false);
	}
	
	public WtScanBtDevice(Context context, boolean isForceBtClassic)
	{
		initDevice(context, isForceBtClassic);
	}
	
	@SuppressLint("NewApi")
	private void initDevice(Context context, boolean isForceBtClassic)
    {
		Log.d("WtScanBtDevice", "00000000000000000000000000");
    	mContext = context;
    	
    	if (!mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)
				|| android.os.Build.VERSION.SDK_INT < 18 
				|| isForceBtClassic) {
    		
    		Log.d("WtScanBtDevice", "111111111111111111111111111");
    		isBLe = false;
    		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    		
    	}else
    	{
    		Log.d("WtScanBtDevice", "222222222222222222222222222");
    		isBLe = true;
    		
			final BluetoothManager bluetoothManager = (BluetoothManager) mContext
					.getSystemService(Context.BLUETOOTH_SERVICE);
	    	mBtAdapter = bluetoothManager.getAdapter();
	    	mLeScanCallback =
	                new BluetoothAdapter.LeScanCallback() {

	            @Override
	            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
	    		    Message msg = new Message();
	    			msg.what = MSG_FIND_DEVICE;
	    			msg.obj = device;
	    			mHandler.sendMessage(msg);
	            }
	        };
    	}
    }
    
	
	@SuppressLint("NewApi")
	public void stopDiscovering()
	{
		mIsDiscovering = false;
		if (!isBLe) {
			
			mBtAdapter.cancelDiscovery();
			if(isRegister){
				mContext.unregisterReceiver(mReceiver);
				isRegister = false;
			}
			
		} else {
			
			mBtAdapter.stopLeScan(mLeScanCallback);
		}
		
	}
	
	boolean isRegister = false;
	private void startScanI()
	{

//		if (mBtAdapter.isDiscovering()) 
//		{
//			 stopDiscovering();
//        }
		
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);
        isRegister = true;
		mBtAdapter.startDiscovery();
	}
	
	

    
    private static final long SCAN_PERIOD = 10000;
	@SuppressLint("NewApi")
	private void startScanII()
	{

		mHandler.postDelayed(new Runnable() {
	                @SuppressLint("NewApi")
					@Override
	                public void run() {         
	                	 mHandler.postDelayed(new Runnable() {
	     	                @SuppressLint("NewApi")
	     					@Override
	     	                public void run() {         

	     	                	mHandler.sendEmptyMessage(MSG_BLOOTH_FINISH);
	     	                	
	     	                }
	     	            }, SCAN_PERIOD);             
	                }
	      }, SCAN_PERIOD);
		mBtAdapter.startLeScan(mLeScanCallback);
	}
/***
	 * 开始查找设备
	 * 
	 * @return 1.表示 正在搜索设备 -1.表示 设备不支持蓝牙
	 */
	public void startScanDevice(SearchListerner listener) {
		this.searchListener = listener;
		
		if (!mBtAdapter.isEnabled()) {
			mBtAdapter.enable();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(!mIsDiscovering)
		{
			mIsDiscovering = true;
			if (!isBLe) {
	
				startScanI();
				
			} else if (mContext.getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_BLUETOOTH_LE)) {
				
				startScanII();
			}
		}

	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				Message msg = new Message();
				msg.what = MSG_FIND_DEVICE;
				msg.obj = device;
				mHandler.sendMessage(msg);
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				mHandler.sendEmptyMessage(MSG_BLOOTH_FINISH);
			}
		}
	};
	
	// 回调函数 处理hander
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			Log.d("handleMessage.....", "handleMessage...." + msg.what);
			switch (msg.what) {

			case MSG_FIND_DEVICE:
				Log.i("MSG_FIND_DEVICE..", "MSG_FIND_DEVICE....");
				if (searchListener != null) {
					searchListener.onFindDevice((BluetoothDevice) msg.obj);
				}
				break;

			case MSG_BLOOTH_FINISH:
				if (searchListener != null) {
					stopDiscovering();
					searchListener.onFinishFindDevice();
				}
				break;

			default:
				break;
			}
		};
	};


}