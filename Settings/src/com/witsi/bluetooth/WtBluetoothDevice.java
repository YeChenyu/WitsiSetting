package com.witsi.bluetooth;

import com.witsi.bluetooth.WtBtDeviceListener.BT_STATE;
import com.witsi.bluetooth.WtBtDeviceListener.BloothConnectListener;
import com.witsi.bluetooth.WtBtListener.ConnectListerner;
import com.witsi.bluetooth.four.Blue4toothChatService;
import com.witsi.bluetooth.three.Blue3toothChatService;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.witsi.arqII.DataTransmit;

public class WtBluetoothDevice {
	
	private static final String TAG = "WtBluetoothDevice";
	protected static final int MSG_CONNECTED = 0;
	protected static final int MSG_DISCONNECTED = 1;
	protected static final int MSG_START_CONNECT = 2;
	protected static final int MSG_CLOSE = 3;
	
	private WtBtDeviceListener mChatService = null;
	private Context mContext;
	private boolean isBLe = false;
	private ConnectListerner connectListener;
	private BluetoothAdapter mBluetoothAdapter;
	public WtBluetoothDevice(Context context)
	{
	
		initDevice(context, false);
	}
	
	public WtBluetoothDevice(Context context, boolean isForceBtClassic)
	{
		initDevice(context, isForceBtClassic);
	}
	
	private void initDevice(Context context, boolean isForceBtClassic)
	{
    	mContext = context;
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if (!mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)
				|| android.os.Build.VERSION.SDK_INT < 18
				||isForceBtClassic) 
		{
    		isBLe = false;
    		mChatService = new Blue3toothChatService( mContext, mBloothConnectListener);
    	}else
    	{
    		isBLe = true;
    		mChatService = new Blue4toothChatService( mContext, mBloothConnectListener);
    	}
	}
	
	public WtBluetoothDevice(Context context,BluetoothDevice device,ConnectListerner listener)
	{
    	mContext = context;
    	if (!mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)
				|| android.os.Build.VERSION.SDK_INT < 18) 
		{
    		isBLe = false;
    		mChatService = new Blue3toothChatService( mContext, mBloothConnectListener);
    	}else
    	{
    		isBLe = true;
    		mChatService = new Blue4toothChatService( mContext, mBloothConnectListener);
    	}
    	
    	conectionDevice( device, listener);
	}

	/***
	 * 获取当前设备的连接状态
	 * 
	 * @return
	 */
	public boolean getConnectionState() {
		if (mChatService != null) {
			if(mChatService.getConnectState())
			{
				return true;
			}
		}
		return false;
	}

	public void close() {
Log.d(TAG, "goto close()");		
		mHandler.sendEmptyMessage(MSG_CLOSE);
	}
	
	
	public DataTransmit getDataTransmit()
	{
		return mDataTransmit;
	}
	BluetoothDevice mBluetoothDevice;
	// 连接设备
	@SuppressLint("NewApi")
	public void conectionDevice(BluetoothDevice device,
			ConnectListerner connectLister) {
		// Attempt to connect to the device
		this.connectListener = connectLister;
		mBluetoothDevice = device;
		mHandler.sendEmptyMessage(MSG_START_CONNECT);
	}
	
	@SuppressLint("NewApi")
	public void conectionDevice(String addr,
			ConnectListerner connectLister) {
		// Attempt to connect to the device
		this.connectListener = connectLister;
		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(addr);;
		mHandler.sendEmptyMessage(MSG_START_CONNECT);

	}

    private String hexByte2HexStr(byte[] bs) 
    {   
    	if(bs == null || bs.length == 0)
    		return null;
    	
        char[] chars = "0123456789ABCDEF".toCharArray();   
        StringBuilder sb = new StringBuilder(""); 
        int bit;   
         
        for (int i = 0; i < bs.length; i++) 
        {   
            bit = (bs[i] & 0x0f0) >> 4;   
            sb.append(chars[bit]);   
            bit = bs[i] & 0x0f;   
            sb.append(chars[bit]); 
            sb.append(' '); 
        }   
        return sb.toString().trim();   
    } 

    private BloothConnectListener mBloothConnectListener = new BloothConnectListener()
    {

		@Override
		public void onStateChange(BT_STATE state) {
			// TODO Auto-generated method stub
			
			Log.i(TAG,"BT_STATE:" +state);
			if(state == BT_STATE.STATE_CONNECTED)
			{
				mHandler.sendEmptyMessage(MSG_CONNECTED);
				
			}else if(state == BT_STATE.STATE_DISCONNECTED)
			{
				mHandler.sendEmptyMessage(MSG_DISCONNECTED);
			}
				
		}
    };
    
    private DataTransmit mDataTransmit = new DataTransmit() {
		@Override
		public byte[] _arq_receive_frame() {
			// TODO Auto-generated method stub
			if (mChatService != null) {

				byte[] recv = mChatService.read();

				if (recv != null && recv.length > 0) {

					Log.i("mDataTransmit", "_arq_receive_frame():"
							+ hexByte2HexStr(recv));
				}
				return recv;
			}else
			{
				Log.e("mDataTransmit", "mChatService == null");
			}
			return null;
		}

		@Override
		public int _arq_send_frame(byte[] arg0) {
			// TODO Auto-generated method stub

			int limitLen = 20;
			
			if (!isBLe) 
			{
				limitLen = 512;
				
	    	}else
	    	{
	    		limitLen = 20;
	    	}
			
			if (mChatService != null) {

				if (arg0 != null && arg0.length > 0) {
					Log.i("mDataTransmit","_arq_send_frame():" + hexByte2HexStr(arg0));
				}
				int len = 0;
				while (len < arg0.length) {

					int tmpLen = arg0.length - len;
					byte[] tmpBuf;

					if (tmpLen > limitLen) {
						tmpBuf = new byte[limitLen];
						System.arraycopy(arg0, len, tmpBuf, 0, limitLen);
						len += limitLen;

					} else {
						tmpBuf = new byte[tmpLen];
						System.arraycopy(arg0, len, tmpBuf, 0, tmpLen);
						len += tmpLen;
					}
					
					mChatService.write(tmpBuf);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return 0;
			}else
			{
				Log.e("mDataTransmit", "mChatService == null");
			}
			return 0;
		}
	};
	
	// 回调函数 处理hander
	private Handler mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
Log.d(TAG, "handleMessage...." + msg.what);
				switch (msg.what) {
				case MSG_CONNECTED:
Log.d(TAG, "connectListener isv" + connectListener);					
					if(connectListener != null){
						connectListener.onDeviceConnected();
					}
					break;
				case MSG_DISCONNECTED:
					if (connectListener != null) {
						connectListener.onDeviceConnectedFail();
					} 
					break;
				case MSG_START_CONNECT:
					
					if (connectListener != null) {
						connectListener.onStartConnect();
					} 
					if (getConnectionState()) 
					{
						if (mChatService != null) {
Log.d(TAG, "mChatService.disconnect()");							
							mChatService.disconnect();
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					if (mChatService.connect(mBluetoothDevice, false) == 1) 
					{
						
					} else 
					{
						mHandler.sendEmptyMessage(MSG_DISCONNECTED);
					}
					break;
				case MSG_CLOSE:
					if (mChatService != null) {
						mChatService.close();
					}
					break;
				default:
					break;
				}
			};
		};

}