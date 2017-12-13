package com.witsi.views;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class BtVisibleTimeOutLayout{

	private Context context;
	private RadioGroup group;
	private RadioButton minute2, minute5, hours1, never, bond;
	
	public View layout;
	private OnVisibleTimeOutListener listener;
	
	public interface OnVisibleTimeOutListener{
		public void setTimeOut(int timeout);
	}
	
	public BtVisibleTimeOutLayout(Context context, BluetoothAdapter adapter) {
		// TODO Auto-generated constructor stub
		this.context = context;
		layout = LayoutInflater.from(context).inflate(R.layout.bluetooth_visible_time_out, null);
		
		group = (RadioGroup) layout.findViewById(R.id.gruop);
		minute2 = (RadioButton) layout.findViewById(R.id.minute2);
		minute5 = (RadioButton) layout.findViewById(R.id.minute5);
		hours1 = (RadioButton) layout.findViewById(R.id.hours);
		never = (RadioButton) layout.findViewById(R.id.never);
		bond = (RadioButton) layout.findViewById(R.id.bond);
		
		if(adapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE){
			bond.setChecked(true);
		}
		
		minute2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(minute2.isChecked())
					listener.setTimeOut(60);
			}
		});
		group.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedId) {
				FyLog.e("RaidoGroup", "check change");
				if(listener != null){
					switch (checkedId) {
					case R.id.minute2:
						listener.setTimeOut(60);
						FyLog.d("RaidoGroup", "" + 60);
						break;
					case R.id.minute5:
						listener.setTimeOut(300);
						FyLog.d("RaidoGroup", "" + 300);
						break;
					case R.id.hours:
						listener.setTimeOut(3600);
						FyLog.d("RaidoGroup", "" + 3600);
						break;
					case R.id.never:
						listener.setTimeOut(-1);
						FyLog.d("RaidoGroup", "" + -160);
						break;
					case R.id.bond:
						listener.setTimeOut(0);
						FyLog.d("RaidoGroup", "" + 0);
						break;
					default:
						listener.setTimeOut(-2);
						FyLog.d("RaidoGroup", "" + -2);
						break;
					}
				}
			}
        });
	}
	
	public View getLayout(){
		return layout;
	}
	
	public void setOnVisibleTimeOutListener(OnVisibleTimeOutListener listener){
		this.listener = listener;
	}
	
}
