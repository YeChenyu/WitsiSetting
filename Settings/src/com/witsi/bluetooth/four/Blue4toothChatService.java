/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.witsi.bluetooth.four;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import com.witsi.bluetooth.WtBtDeviceListener;
import com.witsi.bluetooth.WtBtDeviceListener.BT_STATE;
import com.witsi.bluetooth.four.BluetoothLeService.BlueToothChangeListener;


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public  class Blue4toothChatService implements WtBtDeviceListener{
	
    private final static String TAG = Blue4toothChatService.class.getSimpleName();

    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    
    private boolean mConnected = false;

    private BluetoothGattCharacteristic mReadCharacteristic = null;
    private BluetoothGattCharacteristic mWriteCharacteristic = null;
    private Context mContext;
    
    private final String SERVICE_CONFIG = "0000ffff-0000-1000-8000-00805f9b34fb";
    private final String READ_CHARACTERISTIC_CONFIG = "0000ff01-0000-1000-8000-00805f9b34fb";
    private final String WRITE_CHARACTERISTIC_CONFIG = "0000ff02-0000-1000-8000-00805f9b34fb";

    private final String SERVICE_CONFIG1 = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    private final String READ_CHARACTERISTIC_CONFIG1 = "49535343-1e4d-4bd9-ba61-23c647249616";
    private final String WRITE_CHARACTERISTIC_CONFIG1 = "49535343-8841-43f4-a8d4-ecbe34729bb3";
    
    private BloothConnectListener connectListener;
   
    
    private BluetoothDevice mDevice;
    private int mCount = 0;
    private boolean isAutoDisconnected = true;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	Log.i(TAG, "onServiceConnected() " );
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            mBluetoothLeService.setListener(changeListener);
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override		
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    
    BlueToothChangeListener changeListener=new BlueToothChangeListener() {
		
		@Override
		public void onServicesDisCovered() {
			initBluetoothGattCharacteristic(mBluetoothLeService.getSupportedGattServices());
		}
		
		@Override
		public void onDisConnected() {
			
			setConnectState(false);
			Log.i(TAG, "onDisConnected............ " );
        	
        
		}
		
		@Override
		public void onDataAvailable() {
			
		}
		
		@Override
		public void onConnected() {
			
			Log.i(TAG, "onConnected............");
        	
		}
	};

    public Blue4toothChatService(Context context,BloothConnectListener listener)
    {	
    	Log.i(TAG, "Blue4toothChatService............");
    	mContext = context;
    	connectListener = listener;
    }
   
    @SuppressLint("NewApi")
	private void initBluetoothGattCharacteristic(List<BluetoothGattService> gattServices)
    {
    	
    	Log.w(TAG, "initBluetoothGattCharacteristic");      	
        for (BluetoothGattService gattService : gattServices) 
        {
            
            if(!gattService.getUuid().equals(UUID.fromString(SERVICE_CONFIG))
            		&&!gattService.getUuid().equals(UUID.fromString(SERVICE_CONFIG1)))
            {
            	continue;
            }
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
            	 
            	Log.w(TAG, gattCharacteristic.getUuid().toString());     
            	
                if(gattCharacteristic.getUuid().equals(UUID.fromString(READ_CHARACTERISTIC_CONFIG1)))
                {  

                	mReadCharacteristic = gattCharacteristic;
                	setReadCharacteristic(mReadCharacteristic);
                }else if(gattCharacteristic.getUuid().equals(UUID.fromString(WRITE_CHARACTERISTIC_CONFIG1)))
                {
                	mWriteCharacteristic = gattCharacteristic;
                }
            	
                if(gattCharacteristic.getUuid().equals(UUID.fromString(READ_CHARACTERISTIC_CONFIG)))
                {
                	mReadCharacteristic = gattCharacteristic;
                	setReadCharacteristic(mReadCharacteristic);
                }else if(gattCharacteristic.getUuid().equals(UUID.fromString(WRITE_CHARACTERISTIC_CONFIG)))
                {
                	mWriteCharacteristic = gattCharacteristic;
                }     
            }
            if(gattServices!=null&&gattServices.size()>0){
            	Log.d("STATE_CONNECTED", "STATE_CONNECTED......");
            	setConnectState(true);
            }
            break;
        }
    }
    
	@Override
	public int connect(BluetoothDevice device, boolean secure) {
		
		mCount = 0;
		mDevice = device;
		 
		Intent gattServiceIntent = new Intent( mContext, BluetoothLeService.class);
	    mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	    
	    
	    Log.i(TAG, "mBluetoothLeService " + device.getName());
	    mDeviceAddress=device.getAddress();
	    
	    isAutoDisconnected = true;

		if(mBluetoothLeService!=null){
			
			Log.i(TAG, "1");
			mBluetoothLeService.close();
			Log.i(TAG, "2");
			if(mBluetoothLeService.connect(mDeviceAddress))
        	{
				return 1;
        	}
		}	
		return 0;
	}
    

    private  void setConnectState(boolean state) {
    	mConnected = state;
    	if(mConnected)
    	{
    		connectListener.onStateChange(BT_STATE.STATE_CONNECTED);
    	}else
    	{
    		connectListener.onStateChange(BT_STATE.STATE_DISCONNECTED);
    	}
    }  
    
    public  void close() {
    	if(mBluetoothLeService!=null)
    	{
    		mContext.unbindService(mServiceConnection);
    		mBluetoothLeService.close();
            mBluetoothLeService = null;
            mReadCharacteristic = null;
            mWriteCharacteristic = null;
    	}
    	setConnectState(false);
    }  
    
    public  boolean getConnectState() {
    	return mConnected;
    }  
    
    
    @SuppressLint("NewApi")
	private void setReadCharacteristic(BluetoothGattCharacteristic characteristic)
    {
    	
    	Log.w(TAG, "setReadCharacteristic : " + mReadCharacteristic.getUuid().toString());
        mBluetoothLeService.setCharacteristicNotification(
        		mReadCharacteristic, true);
    	mBluetoothLeService.readCharacteristic(mReadCharacteristic);
    }
    
    @SuppressLint("NewApi")
	public  void write(byte[] value) {
        if (mWriteCharacteristic == null ) {
        	//LogUtil.w(TAG, "BluetoothGattCharacteristic is null");
            return ;
        }
        if(!mConnected){
        	Log.w(TAG, "Bluetooth hasn't Connected");
            return;
        }
        mBluetoothLeService.writeCharacteristic(mWriteCharacteristic,value);
        return;
    }  
    
    @SuppressLint("NewApi")
	public  byte[] read() {
    	
        if (mReadCharacteristic == null ) {
        	//LogUtil.w(TAG, "BluetoothGattCharacteristic is null");
            return null;
        }
        
        if(!mConnected){
        	//LogUtil.w(TAG, "Bluetooth hasn't Connected");
            return null;
        }

        if(mBluetoothLeService.getOnCharacteristicChanged())
        {
        	mBluetoothLeService.setOnCharacteristicChanged(false);
        	
        	ByteArrayOutputStream out = mBluetoothLeService.getByteArrayOutputStream();
        	byte[] buf = out.toByteArray();
        	byte[] tmp = new byte[buf.length];
        	System.arraycopy(buf, 0, tmp, 0, buf.length);
        	out.reset();
        	buf = null;
        	return tmp;
        }
        return null;
        
    }

	@Override
	public void disconnect() {
		
		isAutoDisconnected = false;
		setConnectState(false);
		mBluetoothLeService.disconnect();
	}  
    
}
