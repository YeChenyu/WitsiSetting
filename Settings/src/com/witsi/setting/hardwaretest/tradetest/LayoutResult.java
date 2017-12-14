package com.witsi.setting.hardwaretest.tradetest;



import com.witsi.setting1.R;

import android.app.Activity;
import android.content.Context;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LayoutResult
{
	private Button btn;
	public LayoutResult(Context context, String title, String txt,int gravity, OnClickListener cancleListener)
	{
		((Activity) context).setContentView(R.layout.hardware_tradetest_result);

		((TextView)((Activity) context).findViewById(R.id.result_title_text)).setText(title);
		TextView msg = ((TextView)((Activity) context).findViewById(R.id.result_lable));
		msg.setGravity(gravity);
		msg.setText(txt);
		btn = ((Button)((Activity) context).findViewById(R.id.result_confirm_button));
		btn.setOnClickListener(cancleListener);
	}
	
	public LayoutResult(Context context, String title, String btnText, String txt, int gravity, OnClickListener cancleListener)
	{
		((Activity) context).setContentView(R.layout.hardware_tradetest_result);

		((TextView)((Activity) context).findViewById(R.id.result_title_text)).setText(title);
		TextView msg = ((TextView)((Activity) context).findViewById(R.id.result_lable));
		msg.setGravity(gravity);
		txt = txt + "\n\r";
		msg.setText(txt);
		btn = ((Button)((Activity) context).findViewById(R.id.result_confirm_button));
		btn.setOnClickListener(cancleListener);
		btn.setText(btnText);
	}

}

