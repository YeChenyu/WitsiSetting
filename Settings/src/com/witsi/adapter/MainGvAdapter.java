package com.witsi.adapter;

import com.witsi.setting1.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainGvAdapter extends BaseAdapter{

	private Context context;
	private String label[];
	private int image[];

	public MainGvAdapter(Context context, String[] label, int[] image) {
		super();
		this.context = context;
		this.label = label;
		this.image = image;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return label.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return label[arg0];
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
		TextView tv;
		if(arg1 == null){
			vh = new ViewHolder();
			arg1 = LayoutInflater.from(context)
					.inflate(R.layout.main_gv_item, null);
			iv = (ImageView) arg1.findViewById(R.id.iv);
			tv = (TextView) arg1.findViewById(R.id.tv);
			
			vh.setIv(iv);
			vh.setTv(tv);
			
			arg1.setTag(vh);
		}else{
			vh = (ViewHolder) arg1.getTag();
			iv = vh.getIv();
			tv = vh.getTv();
		}
		
		tv.setText(label[arg0]);
		iv.setImageResource(image[arg0]);
		if(arg0 == 0){
			arg1.setBackgroundColor(Color.rgb(175, 0, 188));
		}else if(arg0 == 1){
			arg1.setBackgroundColor(Color.rgb(26, 195, 134));
		}else if(arg0 == 2){
			arg1.setBackgroundColor(Color.rgb(252, 140, 8));
		}else if(arg0 == 4){
			arg1.setBackgroundColor(Color.rgb(253, 79, 6));
		}else if(arg0 == 3){
			arg1.setBackgroundColor(Color.rgb(250, 77, 193));
		}else if(arg0 == 5){
			arg1.setBackgroundColor(Color.rgb(26, 181, 235));
		}else if(arg0 == 6){
			arg1.setBackgroundColor(Color.rgb(40, 65, 234));
		}else{
			arg1.setBackgroundColor(Color.rgb(253, 79, 6));
		}
		return arg1;
	}
	
	class ViewHolder{
		
		private TextView tv;
		private ImageView iv;
		
		public ViewHolder() {
			super();
			// TODO Auto-generated constructor stub
		}

		public TextView getTv() {
			return tv;
		}

		public void setTv(TextView tv) {
			this.tv = tv;
		}

		public ImageView getIv() {
			return iv;
		}

		public void setIv(ImageView iv) {
			this.iv = iv;
		}
	}

}
