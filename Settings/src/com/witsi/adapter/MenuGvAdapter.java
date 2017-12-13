package com.witsi.adapter;

import com.witsi.setting1.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuGvAdapter extends BaseAdapter{

	private Context context;
	private String label[];
	private int image[];

	public MenuGvAdapter(Context context, String[] label, int[] image) {
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
