package com.witsi.setting.hardwaretest.tradetest.dialog;


import com.witsi.setting.hardwaretest.tradetest.base.PromptMsg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;


public class AffirmDialog extends Activity { 

	private String TAG = "AffirmDialog";
	private boolean D = true;
	private PromptMsg mPrompt;
	private AlertDialog dialog;
//	public static final int RESULT_SUCC = 1;
	public static final int RESULT_ESC = 2;
//	private CountDownTimer mCountDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        if(D) Log.d( TAG, "onCreate ");
        Intent intent = this.getIntent();
		mPrompt = (PromptMsg)intent.getParcelableExtra(PromptMsg.PROMPT_MSG);
		if(mPrompt == null)
		{
			mPrompt = new PromptMsg( "标题", "提示信息", 1);
		}
		
		viewNotification();
    } 
    public void onStart() { 
		super.onStart();

    } 
    public void finish()
    {
    	super.finish();
    }

	protected void viewNotification()
	{
		 dialog = new AlertDialog.Builder(AffirmDialog.this)
     	.setTitle(mPrompt.mTitle)
     	.setIcon(android.R.drawable.ic_dialog_info)                
     	.setMessage("\n" + mPrompt.mMsg + "\n")
     	.setPositiveButton("确定",new DialogInterface.OnClickListener() {		                     
			public void onClick(DialogInterface dialog, int which) {
				dialog = null;
				Intent intent = new Intent();
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
     	})         
     	.setNegativeButton("取消",new DialogInterface.OnClickListener() {		                     
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