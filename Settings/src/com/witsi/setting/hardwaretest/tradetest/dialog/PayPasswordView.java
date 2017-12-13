package com.witsi.setting.hardwaretest.tradetest.dialog;

import com.witsi.setting1.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Dialog 支付密码键盘
 * 
 * @author LanYan
 *
 */
@SuppressLint("InflateParams")
public class PayPasswordView implements OnClickListener {


	private LinearLayout del;
	private LinearLayout cancel;
	private LinearLayout sure;
	
	private ImageView box1;
	private ImageView box2;
	private ImageView box3;
	private ImageView box4;
	private ImageView box5;
	private ImageView box6;
	private TextView content;
	private TextView title;
	
	private View mView;
	private OnPayListener listener;
	@SuppressWarnings("unused")
	private Context mContext;

	public PayPasswordView(String title,String monney, Context mContext, OnPayListener listener) {
		getDecorView( title,monney, mContext, listener);
	}

	public static PayPasswordView getInstance(String title,String monney, Context mContext, OnPayListener listener) {
		return new PayPasswordView(title, monney, mContext, listener);
	}

	public void getDecorView(String title,String monney, Context mContext, OnPayListener listener) {
		this.listener = listener;
		this.mContext = mContext;
//		mView = LayoutInflater.from(mContext).inflate(R.layout.item_paypassword, null);
		mView = LayoutInflater.from(mContext).inflate( R.layout.item_paypassword, null);
		findViewByid();
		setLintenter();
		this.title.setText(title);
		content.setText("消费金额：" + monney+"元");
	}

	private void findViewByid() {


		del = (LinearLayout) mView.findViewById(R.id.pay_keyboard_del);
		cancel = (LinearLayout) mView.findViewById(R.id.pay_keyboard_esc);
		sure = (LinearLayout) mView.findViewById(R.id.pay_keyboard_enter);
		
		// 输入框 TextView
		box1 = (ImageView) mView.findViewById(R.id.pay_box1);
		box2 = (ImageView) mView.findViewById(R.id.pay_box2);
		box3 = (ImageView) mView.findViewById(R.id.pay_box3);
		box4 = (ImageView) mView.findViewById(R.id.pay_box4);
		box5 = (ImageView) mView.findViewById(R.id.pay_box5);
		box6 = (ImageView) mView.findViewById(R.id.pay_box6);

		title = (TextView) mView.findViewById(R.id.pay_title);// 金额
		content = (TextView) mView.findViewById(R.id.pay_content);// 金额

	}

	private void setLintenter() {

		del.setOnClickListener(this);
		cancel.setOnClickListener(this);
		sure.setOnClickListener(this);

	}

	public void parseActionType(ActionEnum type, int size) {
		// TODO Auto-generated method stub
		if (type == ActionEnum.change) 
		{
			updateUi(size);
			
		} else if (type == ActionEnum.delete) 
		{
			listener.onDelPay();
		}else if (type == ActionEnum.cancel) 
		{//取消按钮
			listener.onCancelPay();
		}else if (type == ActionEnum.sure) {//确定按钮
			listener.onSurePay();
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == cancel) {
			parseActionType(ActionEnum.cancel, -1);
		} else if (v == sure) {
			parseActionType(ActionEnum.sure, -1);
		} else if (v == del) {
			parseActionType(ActionEnum.delete, -1);
		}
	}

	/**
	 * 刷新UI
	 */
	private void updateUi(int size) {
		// TODO Auto-generated method stub
		box1.setVisibility(4);
		box2.setVisibility(4);
		box3.setVisibility(4);
		box4.setVisibility(4);
		box5.setVisibility(4);
		box6.setVisibility(4);
		if (size == 0) {
		} else if (size == 1) {
			box1.setVisibility(0);
		} else if (size == 2) {
			box1.setVisibility(0);
			box2.setVisibility(0);
		} else if (size == 3) {
			box1.setVisibility(0);
			box2.setVisibility(0);
			box3.setVisibility(0);
		} else if (size == 4) {
			box1.setVisibility(0);
			box2.setVisibility(0);
			box3.setVisibility(0);
			box4.setVisibility(0);
		} else if (size == 5) {
			box1.setVisibility(0);
			box2.setVisibility(0);
			box3.setVisibility(0);
			box4.setVisibility(0);
			box5.setVisibility(0);
		} else if (size == 6) {
			box1.setVisibility(0);
			box2.setVisibility(0);
			box3.setVisibility(0);
			box4.setVisibility(0);
			box5.setVisibility(0);
			box6.setVisibility(0);

		}
	}

	public interface OnPayListener {
		void onCancelPay();
		void onDelPay();
		void onSurePay();
		
		
	}
	
	private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) 
        {
        	switch(msg.what)
        	{
	        	case 0:
	        		parseActionType( ActionEnum.change, msg.arg2);
	        		break;
        	}
        }
	 };
	 
	public View getView() {
		
		return mView;
	}

	public enum ActionEnum {
		change, delete, cancel, sure
	}


}
