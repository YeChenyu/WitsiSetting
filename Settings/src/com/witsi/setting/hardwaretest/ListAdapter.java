package com.witsi.setting.hardwaretest;

import java.util.ArrayList;
import java.util.List;

import com.witsi.setting1.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<String> list;
	Context context;

	ListAdapter(Context context, ArrayList<String> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		mInflater = LayoutInflater.from(context);
		convertView = mInflater.inflate(R.layout.hardware_list_item, null);
		viewHolder = new ViewHolder(
				(View) convertView.findViewById(R.id.list_child),
				(TextView) convertView.findViewById(R.id.chat_msg));
		convertView.setTag(viewHolder);
		viewHolder.msg.setText(list.get(position).toString());//对话数据
		return convertView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		// return list.get(position);
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	class ViewHolder {
		protected View child;
		protected TextView msg;

		public ViewHolder(View child, TextView msg) {
			this.child = child;
			this.msg = msg;

		}
	}

}
