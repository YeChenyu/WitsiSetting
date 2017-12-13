package com.witsi.setting.hardwaretest.tradetest.dialog;



import com.witsi.setting.hardwaretest.tradetest.base.PromptMsg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;


public class NotificationDialog extends Activity { 

//	private String TAG = "Notification";
//	private boolean D = true;
	public static final int RESULT_ESC = 2;
	private PromptMsg mPrompt;
	private AlertDialog dialog;
//	private CountDownTimer mCountDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        Intent intent = this.getIntent();
		mPrompt = (PromptMsg)intent.getParcelableExtra(PromptMsg.PROMPT_MSG);
		if(mPrompt == null)
		{
			mPrompt = new PromptMsg( "标题", "提示信息", 1);
		}
		
		new CountDownTimer(mPrompt.S*1000,100) { 
	    	@Override public void onFinish()
	    	{ 

		        finish();	
	    	}
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
			} 
		}.start();
		viewNotification();
    } 
    public void onStart() { 
		super.onStart();

    } 
    public void finish()
    {
    	super.finish();
		if(dialog!=null)
		{
			dialog.dismiss();
		}
    }

	protected void viewNotification()
	{
		 dialog = new AlertDialog.Builder(NotificationDialog.this)
     	.setTitle(mPrompt.mTitle)
     	.setIcon(android.R.drawable.ic_dialog_info)                
     	.setMessage("\n" + mPrompt.mMsg + "\n")
     	.setPositiveButton("取消",new DialogInterface.OnClickListener() {		                     
			public void onClick(DialogInterface dialog, int which) {
				dialog = null;
				Intent intent = new Intent();
				setResult(RESULT_ESC, intent);
				finish();
			}
     	})                
		.setOnKeyListener(new OnKeyListener() {  
            @Override  
            public boolean onKey(DialogInterface dialog, int keyCode,  
                    KeyEvent event) {  
                if (keyCode == KeyEvent.KEYCODE_BACK  
                        && event.getRepeatCount() == 0) { 
                	dialog = null;
                	Intent intent = new Intent();
    				setResult(RESULT_ESC, intent);
        			finish();
                }  
                return false;  
            }  
        }).create();
		
    	Window window = dialog.getWindow();   
    	WindowManager.LayoutParams lp = window.getAttributes();   
    	// 设置透明度
    	lp.alpha = 0.95f;   
    	window.setAttributes(lp); 
		
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.show();
	}

}