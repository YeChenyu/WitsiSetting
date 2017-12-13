package com.witsi.adapter;

import java.util.List;

import com.witsi.setting1.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ManagerGvAdapter<T> extends BaseAdapter{

	private Context context;
	private List<T> list;

	public ManagerGvAdapter(Context context, List<T> list) {
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
		ImageView iv, iv1;
		TextView tv;
		if(arg1 == null){
			vh = new ViewHolder();
			arg1 = LayoutInflater.from(context)
					.inflate(R.layout.manager_gv_item, null);
			iv = (ImageView) arg1.findViewById(R.id.iv);
			iv1 = (ImageView) arg1.findViewById(R.id.iv1);
			tv = (TextView) arg1.findViewById(R.id.tv);
			
			vh.setIv(iv);
			vh.setIv1(iv1);
			vh.setTv(tv);
			
			arg1.setTag(vh);
		}else{
			vh = (ViewHolder) arg1.getTag();
			iv = vh.getIv();
			iv1 = vh.getIv1();
			tv = vh.getTv();
		}
		ResolveInfo info = (ResolveInfo) list.get(arg0);
		PackageManager mypm = context.getPackageManager();
		iv.setImageDrawable(info.loadIcon(mypm));
		tv.setText(info.loadLabel(mypm));
//		if(info.isConfig)
//			iv1.setVisibility(View.VISIBLE);
//		else
//			iv1.setVisibility(View.GONE);
		
		return arg1;
	}
	
	class ViewHolder{
		
		private TextView tv;
		private ImageView iv, iv1;
		
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

		public ImageView getIv1() {
			return iv1;
		}

		public void setIv1(ImageView iv1) {
			this.iv1 = iv1;
		}
		
		
	}

}
