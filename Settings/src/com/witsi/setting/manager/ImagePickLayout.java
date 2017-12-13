package com.witsi.setting.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.witsi.adapter.ImagePickGvAdapter;
import com.witsi.setting1.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class ImagePickLayout implements OnItemClickListener{

	private Context context;
	private View v;
	private GridView gv;
	private List<String> list ;
	private String imageSrc;
	private Bitmap bitMap;
	
	private OnImagePickItemClick listener;
	
	public ImagePickLayout(Context context, OnImagePickItemClick listener) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.listener = listener;
		v = LayoutInflater.from(context).inflate(R.layout.manager_imagepick_layout, null);
		gv = (GridView) v.findViewById(R.id.gv);
		list = new ArrayList<String>();
		list.add("camera.png");
		list.add("childish_camera.png");
		list.add("childish_credit_cards.png");
		list.add("childish_folder.png");
		list.add("childish_game_pad.png");
		list.add("childish_gears.png");
		list.add("childish_globe.png");
		list.add("game_control.png");
		list.add("gear.png");
		list.add("setting.png");
		ImagePickGvAdapter<String> adapter = new ImagePickGvAdapter<String>(context, list);
		gv.setAdapter(adapter);
		
		gv.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		imageSrc = list.get(arg2);
		listener.onItemClick(imageSrc);
	}
	
	public interface OnImagePickItemClick{
		public void onItemClick(String bitmap);
	}
	public View getView() {
		return v;
	}
	
	public Bitmap getBitMap() {
		return bitMap;
	}
}
