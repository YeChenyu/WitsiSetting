package com.witsi.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface WtBtListener {
	
	
	public interface ConnectListerner
	{
		/***开始连接**/
		void onStartConnect();

		/***连接成功**/
		void onDeviceConnected();
		
		/**蓝牙连接设备失败**/
		void onDeviceConnectedFail();	
	}
	
	public interface SearchListerner
	{
		/**查找到设备**/
		void onFindDevice(BluetoothDevice device);
		
		/***查找设备结束**/
		void onFinishFindDevice();		
		
	}
}