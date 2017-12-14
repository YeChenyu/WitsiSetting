package com.witsi.activitys;


import com.witsi.adapter.MainGvAdapter;
import com.witsi.adapter.MainLvAdapter;
import com.witsi.setting1.R;
import com.witsi.setting.hardwaretest.EntryActivity;
import com.witsi.setting.manager.ManagerActivity;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener
									, OnClickListener{

	private String TAG = MainActivity.class.getSimpleName();
	private Context context = MainActivity.this;
	
	private View wifi, bluetooth, selftest, device, location, display, manager;
	private ListView lv_main;
	private MainLvAdapter adapter;
	private String[] label = {
			"WI-FI",
			"蓝牙",
//			"设备自检",
			"网络定位",
			"声音显示",
//			"用户管理",
			"关于设备",
			
	};
	private int[] image = {
			R.drawable.main_wifi,
			R.drawable.main_bluetooth,
//			R.drawable.main_selftest,
			R.drawable.main_location,
			R.drawable.main_display,
//			R.drawable.main_manager
			R.drawable.main_about,
			};
	private LinearLayout ll_back;
	
	private boolean isBluetoothEnable = true;
	
	@SuppressLint("NewApi")
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		lv_main = (ListView) findViewById(R.id.main_lv);
		adapter = new MainLvAdapter(context, label, image);
		lv_main.setAdapter(adapter);
		lv_main.setOnItemClickListener(this);
		ll_back.setOnClickListener(this);
		
		BluetoothAdapter bt_adapter;
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			bt_adapter = BluetoothAdapter.getDefaultAdapter();
        }else{
        	final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        	bt_adapter = bluetoothManager.getAdapter();
        }
		if(bt_adapter == null){
			isBluetoothEnable = false;
		}
	}

	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		switch (arg2) {
		case 0:
			intent.setClass(context, WifiActivity.class);
			context.startActivity(intent);
			break;
		case 1:
			if (isBluetoothEnable) {
				intent.setClass(context, BluetoothActivity.class);
				context.startActivity(intent);
			}else{
				Toast.makeText(context, "本设备无蓝牙功能！", Toast.LENGTH_SHORT).show();
			}
			break;
		case 2:
//			intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
//			startActivity(intent);
//			intent.setClassName("com.android.settings", "com.android.settings.LocationSettings");
			
			intent.setClass(context, NetworkActivity.class);
			context.startActivity(intent);
			
			break;
		case 3:
			intent.setClass(context, DisplayActivity.class);
			context.startActivity(intent);
			break;

		case 4:  
			intent.setClass(context, AboutDeviceActivity.class);
			context.startActivity(intent);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		finish();
	}

}
