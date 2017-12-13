package com.witsi.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface WtBtListener {
	
	
	public interface ConnectListerner
	{
		/***��ʼ����**/
		void onStartConnect();

		/***���ӳɹ�**/
		void onDeviceConnected();
		
		/**���������豸ʧ��**/
		void onDeviceConnectedFail();	
	}
	
	public interface SearchListerner
	{
		/**���ҵ��豸**/
		void onFindDevice(BluetoothDevice device);
		
		/***�����豸����**/
		void onFinishFindDevice();		
		
	}
}