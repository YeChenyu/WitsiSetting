package com.witsi.adapter;

import java.util.List;

import com.witsi.activitys.WifiAdmin;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WifiLvAdapter extends BaseAdapter{

	private String TAG = WifiLvAdapter.class.getSimpleName();
	private Context context;
	private List<ScanResult> list;
	private WifiAdmin admin;
	private String connectingSsid = "$$$$$";

	public WifiLvAdapter(Context context, WifiAdmin admin, List<ScanResult> list) {
		super();
		this.context = context;
		this.admin = admin;
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

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder vh = null;
		ImageView iv1, iv2, iv3;
		TextView tv1, tv2, tv3;
		
		if(arg1 == null){
			vh = new ViewHolder();
			arg1 = LayoutInflater.from(context)
					.inflate(R.layout.wifi_lv_item, null);
			iv1 = (ImageView) arg1.findViewById(R.id.iv1);
			tv1 = (TextView) arg1.findViewById(R.id.tv1);
			tv2 = (TextView) arg1.findViewById(R.id.tv2);
			tv3 = (TextView) arg1.findViewById(R.id.tv3);
			iv2 = (ImageView) arg1.findViewById(R.id.iv2);
			iv3 = (ImageView) arg1.findViewById(R.id.iv3);
			
			vh.setIv1(iv1);
			vh.setTv1(tv1);
			vh.setTv2(tv2);
			vh.setTv3(tv3);
			vh.setIv2(iv2);
			vh.setIv3(iv3);
			
			arg1.setTag(vh);
		}else{
			vh = (ViewHolder) arg1.getTag();
			iv1 = vh.getIv1();
			tv1 = vh.getTv1();
			tv2 = vh.getTv2();
			tv3 = vh.getTv3();
			iv2 = vh.getIv2();
			iv3 = vh.getIv3();
		}
		
		ScanResult scan = list.get(arg0);
		//信号大小
		int level = Math.abs(scan.level);
		FyLog.i(TAG, "the level is: " + level);
		if(level > 100){
			iv2.setImageResource(R.drawable.wifi_a1);
		}else if(level > 80){
			iv2.setImageResource(R.drawable.wifi_a2);
		}else if(level > 70){
			iv2.setImageResource(R.drawable.wifi_a3);
		}else if(level > 62){
			iv2.setImageResource(R.drawable.wifi_a4);
		}else if(level > 55){
			iv2.setImageResource(R.drawable.wifi_a5);
		}else if(level > 50){
			iv2.setImageResource(R.drawable.wifi_a6);
		}else{
			iv2.setImageResource(R.drawable.wifi_a6);
		}
		//是否受保护
		if(scan.capabilities.equals("[WPS][ESS]")){
			iv3.setImageResource(R.drawable.un_lock);
		}else{
			iv3.setImageResource(R.drawable.lock);
		}
		if(scan.SSID == null)
			tv1.setText("未知网络");
		else
			tv1.setText(scan.SSID);
		
		List<WifiConfiguration> lst = admin.GetConfiguration();
		for (WifiConfiguration wifi : lst) {
			FyLog.i(TAG, "the config wifi.ssid is: " + wifi.SSID );
			if(wifi.SSID.equals("\"" + scan.SSID + "\"")
					&& wifi.preSharedKey != null && wifi.preSharedKey.equals("*")){
				if(admin.GetSSID() != null && admin.GetSSID().equals("\""+scan.SSID+"\"") 
						&& admin.isWifiConnected(context))
					break;
				FyLog.i(TAG, "the connected config wifi is: " + wifi.SSID + "the ssid is: " + scan.SSID + "the preSharedKey is: " + wifi.preSharedKey);
				tv3.setText("已保存");
				tv3.setTextColor(Color.BLACK);
		 	}
		}
		if(admin.GetSSID() != null && admin.GetSSID().equalsIgnoreCase("\""+scan.SSID+"\"") 
				&& admin.isWifiConnected(context)){
//			Log.d("wifi", "已连接 " + admin.GetSSID() + " : " + scan.SSID + " : " + connectingSsid+ "connected="+ admin.isWifiConnected(context));
			tv3.setText("已连接");
			connectingSsid = null;
			tv3.setTextColor(Color.rgb(26, 195, 134));
		}else {
			//当正在连接wifi的时候显示：正在连接中...
			if(connectingSsid != null && scan.SSID.equals(connectingSsid)){
				Log.d("wifi", "连接中 " + admin.GetSSID() + " : " + scan.SSID + " : " + connectingSsid);
				tv3.setText("连接中...");
				tv3.setTextColor(Color.CYAN);
			}else{
				tv3.setText("未连接");
				tv3.setTextColor(Color.GRAY);
			}
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
	
	public void setConnectingSsid(String connectingSsid) {
		this.connectingSsid = connectingSsid;
		this.notifyDataSetChanged();
	}
	class ViewHolder{
		
		private ImageView iv1;
		private TextView tv1;
		private TextView tv2;
		private TextView tv3;
		private ImageView iv2;
		private ImageView iv3;
		
		public ViewHolder() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ImageView getIv1() {
			return iv1;
		}

		public void setIv1(ImageView iv1) {
			this.iv1 = iv1;
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

		public ImageView getIv2() {
			return iv2;
		}

		public void setIv3(ImageView iv3) {
			this.iv3 = iv3;
		}
		public ImageView getIv3() {
			return iv3;
		}
		
		public void setIv2(ImageView iv2) {
			this.iv2 = iv2;
		}

		public TextView getTv3() {
			return tv3;
		}

		public void setTv3(TextView tv3) {
			this.tv3 = tv3;
		}

	}

}
