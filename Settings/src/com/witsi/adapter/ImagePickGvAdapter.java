package com.witsi.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.witsi.setting1.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImagePickGvAdapter<T> extends BaseAdapter{

	private Context context;
	private List<T> list;

	public ImagePickGvAdapter(Context context, List<T> list) {
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
		if(arg1 == null){
			vh = new ViewHolder();
			arg1 = LayoutInflater.from(context)
					.inflate(R.layout.manager_imagepick_item, null);
			iv = (ImageView) arg1.findViewById(R.id.iv);
			
			vh.setIv(iv);
			
			arg1.setTag(vh);
		}else{
			vh = (ViewHolder) arg1.getTag();
			iv = vh.getIv();
		}
		String str = (String) list.get(arg0);
		InputStream is;
		try {
			is = context.getResources().getAssets().open(str);
			Bitmap bit = BitmapFactory.decodeStream(is);
			iv.setImageBitmap(bit);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return arg1;
	}
	
	class ViewHolder{
		
		private ImageView iv;
		
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

		
	}

}
