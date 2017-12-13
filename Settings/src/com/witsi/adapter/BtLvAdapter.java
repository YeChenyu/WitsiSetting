package com.witsi.adapter;

import java.util.List;

import com.witsi.setting1.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BtLvAdapter extends BaseAdapter{

	private Context context;
	private List<BluetoothDevice> list;

	public BtLvAdapter(Context context, List<BluetoothDevice> list) {
		super();
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder vh = null;
		ImageView iv;
		TextView tv1, tv2, tv3;
		
		if(arg1 == null){
			vh = new ViewHolder();
			arg1 = LayoutInflater.from(context)
					.inflate(R.layout.hardware_bluetooth_unbond_lv_item, null);
			iv = (ImageView) arg1.findViewById(R.id.iv);
			tv1 = (TextView) arg1.findViewById(R.id.tv1);
			tv2 = (TextView) arg1.findViewById(R.id.tv2);
			tv3 = (TextView) arg1.findViewById(R.id.tv3);
			
			vh.setIv(iv);
			vh.setTv1(tv1);
			vh.setTv2(tv2);
			vh.setTv3(tv3);
			
			arg1.setTag(vh);
		}else{
			vh = (ViewHolder) arg1.getTag();
			iv = vh.getIv();
			tv1 = vh.getTv1();
			tv2 = vh.getTv2();
			tv3 = vh.getTv3();
		}
		
		BluetoothDevice device = list.get(arg0);
		if(device.getName() == null)
			tv1.setText("未知设备");
		else
			tv1.setText(device.getName());
		tv2.setText(device.getAddress());
		if(device.getBondState() == BluetoothDevice.BOND_BONDED){
			tv3.setText("设备名：    (已配对)");
		}else{
			tv3.setText("设备名：");
		}
		return arg1;
	}
	
	
	/**
	 * 将指定索引的元素显示在第一行
	 * @param position
	 */
	public void upToTheFirst(int position){
		if(list.size() > 1){
			list.add(0, list.get(position));
			list.remove(position + 1);
			this.notifyDataSetChanged();
		}
	}
	
	class ViewHolder{
		
		private ImageView iv;
		private TextView tv1;
		private TextView tv2, tv3;
		
		public ViewHolder() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ImageView getIv() {
			return iv;
		}

		public void setIv(ImageView iv) {
			this.iv = iv;
		}

		public TextView getTv1() {
			return tv1;
		}

		public void setTv1(TextView tv1) {
			this.tv1 = tv1;
		}

		public TextView getTv2() {
			return tv2;
		}

		public void setTv2(TextView tv2) {
			this.tv2 = tv2;
		}
		public TextView getTv3() {
			return tv3;
		}
		
		public void setTv3(TextView tv3) {
			this.tv3 = tv3;
		}

	}

}
