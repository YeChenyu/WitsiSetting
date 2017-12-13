package com.witsi.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface WtBtDeviceListener {

	public enum BT_STATE{
		STATE_NONE,
		STATE_LISTEN,
		STATE_CONNECTING,
		STATE_CONNECTED,
		STATE_DISCONNECTED
	}

  
 // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 0x4000;
    public static final int MESSAGE_READ = 0x4001;
    public static final int MESSAGE_WRITE = 0x4002;
    
    public static interface BloothConnectListener{
    	
		void onStateChange(BT_STATE state);
		
    }

	int connect(BluetoothDevice device, boolean secure);
	

	void close();

	byte[] read();

	void write(byte[] arg0);

	boolean getConnectState();

	void disconnect();
}
