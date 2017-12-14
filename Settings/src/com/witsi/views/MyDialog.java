package com.witsi.views;

import javax.crypto.spec.PSource;

import com.witsi.setting1.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

public class MyDialog{

	private Context context = null;
	
	private TextView title;
	private LinearLayout view;
	private Button positive, negetive, multi;
	
	private Dialog dialog = null;
	private OnDialogClickListener listener;
	private boolean pos = false, neg = false, mul = false;
	
	public MyDialog(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		dialog = new Dialog(context, R.style.transparentFrameWindowStyle);
		View layout = LayoutInflater.from(context).inflate(R.layout.my_dialog_layout, null);
		title = (TextView) layout.findViewById(R.id.title);
		view = (LinearLayout) layout.findViewById(R.id.view);
		positive = (Button) layout.findViewById(R.id.positive);
		negetive = (Button) layout.findViewById(R.id.negetive);
		multi = (Button) layout.findViewById(R.id.multi);
		
		dialog.setContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
	}
	
	public void setTitle(String str){
		if(str != null){
			title.setText(str);
		}
	}
	
	public void setView(View layout) {
		if(dialog != null){
			view.addView(layout);
		}
	}
	
	public interface OnDialogClickListener{
		public void onClick();
	}
	public void setPositiveButton(String str, final OnDialogClickListener listener){
		if(str != null){
			positive.setText(str);
		}
		positive.setVisibility(View.VISIBLE);
		pos = true;
		positive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(listener != null){
					listener.onClick();
				}
				dialog.dismiss();
			}
		});
	}
	
	public void setNegetiveButton(String str, final OnDialogClickListener listener){
		if(str != null){
			negetive.setText(str);
		}
		negetive.setVisibility(View.VISIBLE);
		neg = true;
		negetive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(listener != null){
					listener.onClick();
				}
				dialog.dismiss();
			}
		});
	}
	public void setMultiButton(String str, final OnDialogClickListener listener){
		if(str != null){
			multi.setText(str);
		}
		mul = true;
		multi.setVisibility(View.VISIBLE);
		multi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(listener != null){
					listener.onClick();
				}
				dialog.dismiss();
			}
		});
	}
	
	
	
	
	
	public void show(int location){
		if(dialog != null){
			Window window = dialog.getWindow();
			// 设置显示动画
			window.setWindowAnimations(R.style.main_menu_animstyle);
			WindowManager.LayoutParams wl = window.getAttributes();
			wl.x = 0;
			wl.y = location;
			// 以下这两句是为了保证按钮可以水平满屏
			wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
			wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

			// 设置显示位置
			dialog.onWindowAttributesChanged(wl);
			// 设置点击外围解散
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
			dialog.show();
		}
	}
}
