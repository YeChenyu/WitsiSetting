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
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

   private BlueToothChangeListener listener;
   
   
   
   static  interface BlueToothChangeListener{
    	void onConnected();
    	void onDisConnected();
    	void onServicesDisCovered();
    	void onDataAvailable();
    };
    
    
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    
    private boolean isOnCharacteristicChanged = false;
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    @SuppressLint("NewApi")
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @SuppressLint("NewApi")
		@Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {         
            if (newState == BluetoothProfile.STATE_CONNECTED) {
            	if(listener!=null){
            		listener.onConnected();
            	}
            	
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                boolean result= mBluetoothGatt.discoverServices();
                Log.i(TAG, "Attempting to start service discovery:" +result);
               
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            	if(listener!=null){
            		listener.onDisConnected();
            	} 
            	Log.i(TAG, "onDisConnected.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        	
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(listener!=null){
            		listener.onServicesDisCovered();
            	} 
            }
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
        	
        	Log.i(TAG,"44444444444444444444444444444444");        
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	Log.i(TAG,"5555555555555555555555555555555:");            	
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

			try {
				mByteArrayOutputStream.write(characteristic.getValue());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			}
			setOnCharacteristicChanged(true);
        }
    };
    
    ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
    public ByteArrayOutputStream getByteArrayOutputStream()
    {
    	return mByteArrayOutputStream;
    }
    
    
    public void setOnCharacteristicChanged(boolean enabled)
    {
    	isOnCharacteristicChanged = enabled;
    }
    
    public boolean getOnCharacteristicChanged()
    {
    	return isOnCharacteristicChanged;
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    @SuppressLint("NewApi")
	public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
            	Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
        	Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    private String mBluetoothDeviceAddress;
    @SuppressLint("NewApi")
	public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
        	Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        
//        if(mBluetoothGatt!=null){
//        	 mBluetoothGatt.close();
//        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }
        
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
        	Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        Log.d(TAG, "Trying to create a new connection.");       
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
        	Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */							   
    @SuppressLint("NewApi")
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
        	Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    
    @SuppressLint("NewApi")
	public byte[] readCharacteristicII(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
        	Log.w(TAG, "BluetoothAdapter not initialized");
            return null;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
        byte[] data = characteristic.getValue();
        
        return data;

    }
    
    @SuppressLint("NewApi")
	public void writeCharacteristic(BluetoothGattCharacteristic characteristic,byte[] value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
        	Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        characteristic.setValue(value);
    	boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
    	Log.v(TAG,"Write Status: " + status);
    }  
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    @SuppressLint("NewApi")
	public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
        	Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    @SuppressLint("NewApi")
	public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


	public void setListener(BlueToothChangeListener listener) {
		this.listener = listener;
	}   
}
