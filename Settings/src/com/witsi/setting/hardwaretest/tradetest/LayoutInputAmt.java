package com.witsi.setting.hardwaretest.tradetest;


import com.witsi.setting1.R;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class LayoutInputAmt{
	
	private static final String TAG = "LayoutInputAmt";

	private Context mContext;
	private EditText editText1;
	private Button okBtn;
	private CountDownTimer mCountDownTimer;
	private OnClickListener mCancleListener;
	private OnClickListener mOkListener;
	public static long MAX_AMOUNT = 2000000000;
	public static long TRANS_MAX_AMOUNT = 999999999;

	private long mAttr;
	private boolean mIsAmtCHkMaxRefund;
	
	public LayoutInputAmt(Context context, 
			OnClickListener cancleListener, OnClickListener oklistener)
	{
		
		mCancleListener = cancleListener;
		mOkListener = oklistener;

		this.mContext = context;
		((Activity) context).setContentView(R.layout.hardware_tradetest_input_layout);
		((Button)((Activity) context).findViewById(R.id.templet_cancel_button))
			.setOnClickListener(cancleListener);
		okBtn = ((Button)((Activity) context).findViewById(R.id.templet_confirm_button));
		okBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mCountDownTimer.cancel();
				okBtn.setFocusable(false);
				mOkListener.onClick(null);
			}
			
		});
		okBtn.setText("下   一   步");
		
		mCountDownTimer = new CountDownTimer(60*1000,100) { 
	    	@Override public void onFinish()
	    	{ 
				Toast.makeText( mContext , "输入超时",
	                    Toast.LENGTH_SHORT).show();
	    		mCancleListener.onClick(null);
	    	}
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
			} 
		}.start();
		
	}

	public void setTitle(String text)
	{
		TextView view = (TextView) ((Activity) mContext).findViewById(R.id.templet_title_text);
		view.setText(text);

	}
	
	public void setLable(String text)
	{
		TextView view = (TextView) ((Activity) mContext).findViewById(R.id.templet_input_text_hint);
		view.setText(text);
	}
	
	public void setEdit1(String text)
	{
		
		((TableRow)((Activity) mContext).findViewById(R.id.templet_tablerow1))
		.setVisibility(View.VISIBLE);
		
		TextView view = (TextView) ((Activity) mContext).findViewById
				(R.id.templet_input_info_title1);
		view.setText(text);

		editText1 = (EditText)((Activity) mContext).findViewById
				(R.id.templet_input_info_edit1);
		editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
		editText1.setFocusable(true);
		editText1.addTextChangedListener(mTextWatcher);
		editText1.setText("0.00");
		
	}
	
	public double getAmt()
	{
		if(editText1.getText() == null)
			return 0;
		if(editText1.getText().length() == 0)
			return 0;
		return Double.parseDouble(editText1.getText().toString());
	}
	
	TextWatcher mTextWatcher = new TextWatcher() {  

	        private String validText = "";
	        private boolean isChanged = false; 
	        
	        @Override  
	        public void onTextChanged(CharSequence s, int start, int before, int count) {  
	            // TODO Auto-generated method stub  
	        }  
	          
	        @Override  
	        public void beforeTextChanged(CharSequence s, int start, int count,  
	                int after) {  
	            // TODO Auto-generated method stub  
	        }  
	          
	        @Override  
	        public void afterTextChanged(Editable s) {  
	            // TODO Auto-generated method stub  
//	        	if(!utils.isAmtFormat(s.toString()))
//	        	{
//if(utils.D)Log.i(TAG, "isn't AmtFormat amt:" + validText);  	        		
//	        		editText1.setText(validText);
//	        	}else
//	        	{
//if(utils.D)Log.i(TAG, "isAmtFormat amt:" + s.toString());  	        		
//	        		validText = s.toString();
//	        	}
                if (isChanged) {// ----->如果字符未改变则返回    
                    return;    
                }    
                String str = s.toString();    
    
                isChanged = true;    
                String cuttedStr = str;    
                /* 删除字符串中的dot */    
                for (int i = str.length() - 1; i >= 0; i--) {    
                    char c = str.charAt(i);    
                    if ('.' == c) {    
                        cuttedStr = str.substring(0, i) + str.substring(i + 1);    
                        break;    
                    }    
                }    
                /* 删除前面多余的0 */    
                int NUM = cuttedStr.length();   
                int zeroIndex = -1;  
                for (int i = 0; i < NUM - 2; i++) {    
                    char c = cuttedStr.charAt(i);    
                    if (c != '0') {    
                        zeroIndex = i;  
                        break;  
                    }else if(i == NUM - 3){  
                        zeroIndex = i;  
                        break;  
                    }  
                }    
                if(zeroIndex != -1){  
                    cuttedStr = cuttedStr.substring(zeroIndex);  
                }  
                /* 不足3位补0 */    
                if (cuttedStr.length() < 3) {    
                    cuttedStr = "0" + cuttedStr;    
                }    
                /* 加上dot，以显示小数点后两位 */    
                cuttedStr = cuttedStr.substring(0, cuttedStr.length() - 2)    
                        + "." + cuttedStr.substring(cuttedStr.length() - 2);    
                
                editText1.setText(cuttedStr);    
    
                editText1.setSelection(editText1.length());    
                isChanged = false;  
	        	
	        	
	        }  
	    };  
	
}