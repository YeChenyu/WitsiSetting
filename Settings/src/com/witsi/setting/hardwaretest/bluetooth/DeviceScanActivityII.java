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

package com.witsi.setting.hardwaretest.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.witsi.bluetooth.WtBtListener.SearchListerner;
import com.witsi.bluetooth.WtScanBtDevice;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;


/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivityII extends Activity implements OnClickListener,
									OnItemClickListener{
	
	private static final String TAG = "DeviceScanActivityII";
	private static final boolean D = true;
	
	private ListView lv_scan;
	private Button btn_menu;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.

    WtScanBtDevice mWtScanBtDevice;

    private static final long SCAN_PERIOD = 10000;

    private Handler mHandler;
    
    public static boolean isForceBtClassic = false;
	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hardware_scan_device_activity);
        mHandler = new Handler();
        
        lv_scan = (ListView) findViewById(R.id.lv_device);
        btn_menu = (Button) findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(this);
        lv_scan.setOnItemClickListener(this);
        
        mWtScanBtDevice = new WtScanBtDevice(this, isForceBtClassic);
        
        scanLeDevice(true);
        
        FyLog.d(TAG, "2222222222222222222222222222222222222222");	
    }

	SearchListerner mSearchListerner = new SearchListerner()
	{

		@Override
		public void onFindDevice(BluetoothDevice device) {
			// TODO Auto-generated method stub
			mLeDeviceListAdapter.addDevice(device);
            mLeDeviceListAdapter.notifyDataSetChanged();
		}

		@Override
		public void onFinishFindDevice() {
			// TODO Auto-generated method stub
			 mScanning = false;
			 FyLog.v(TAG, "finish find device");
			 btn_menu.setText("开始扫描蓝牙设备");
		}                                                            
		
		
	};
	
	

	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("NewApi")
				@Override
                public void run() {
                    mScanning = false;
                    mWtScanBtDevice.stopDiscovering();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mWtScanBtDevice.startScanDevice(mSearchListerner);
        } else {
            mScanning = false;
            mWtScanBtDevice.stopDiscovering();
        }
        invalidateOptionsMenu();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (mScanning) {
        	btn_menu.setText("停止扫描蓝牙设备");
        } else {
        	btn_menu.setText("开始扫描蓝牙设备");
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        lv_scan.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
//            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (!mScanning) {
			mLeDeviceListAdapter.clear();
            scanLeDevice(true);
        } else {
        	scanLeDevice(false);
        }
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		final BluetoothDevice device = mLeDeviceListAdapter.getDevice(arg2);
        
        if (device == null) return;
        
        Bundle bundle = new Bundle();
//        bundle.putInt(DeviceControl.EXTRAS_DEVICE_TYPE, device.getType());  
//		bundle.putString(DeviceControl.EXTRAS_DEVICE_NAME, device.getName());
//		bundle.putString(DeviceControl.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        
		bundle.putParcelable("BLUEDEVICE", device);
		Intent intent = new Intent();
		intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        FyLog.d(TAG, "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");	
        finish();
	}
	

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivityII.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.hardware_listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}