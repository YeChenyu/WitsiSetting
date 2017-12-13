package com.witsi.setting.hardwaretest.tradetest.dialog;

import com.witsi.setting.hardwaretest.tradetest.dialog.PayPasswordView.ActionEnum;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * 提示Dialog
 * 
 * @author LanYan
 *
 */
public class KeyBoardDialog extends Dialog {
	Activity activity;
	private View view;
	private boolean isOutSideTouch = true;
	PayPasswordView object;
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public boolean isOutSideTouch() {
		return isOutSideTouch;
	}

	public void setOutSideTouch(boolean isOutSideTouch) {
		this.isOutSideTouch = isOutSideTouch;
	}

	public KeyBoardDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public KeyBoardDialog(Context context) {
		this(context, 0);
		// TODO Auto-generated constructor stub
	}

	public KeyBoardDialog(Activity activity, PayPasswordView view) {
		super(activity, android.R.style.Animation_Dialog);
		this.activity = activity;
		object = view;
		this.view = view.getView();
	}

	public KeyBoardDialog(Activity activity, View view, int theme) {
		super(activity, theme);
		this.activity = activity;
		this.view = view;
	}

	public KeyBoardDialog(Activity activity, View view, int theme, boolean isOutSide) {
		super(activity, theme);
		this.activity = activity;
		this.view = view;
		this.isOutSideTouch = isOutSide;
	}
	
  
	public void KeyChange(int num){
		
		object.parseActionType(ActionEnum.change, num);

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(view);
		setCanceledOnTouchOutside(isOutSideTouch);

		DisplayMetrics dm = new DisplayMetrics();

		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);


		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
		layoutParams.width = screenWidth;
		layoutParams.height = screenHeight - 60;
		this.getWindow().setAttributes(layoutParams);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {  
        	
        	object.parseActionType(ActionEnum.cancel, -1);
        }  
        return false;  
	}
}
