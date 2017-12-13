package com.witsi.setting.hardwaretest.tradetest.base;



import com.witsi.setting.hardwaretest.tradetest.base.InputParam.InputMode;
import com.witsi.setting.hardwaretest.tradetest.dialog.AffirmDialog;
import com.witsi.setting.hardwaretest.tradetest.dialog.InputDialog;
import com.witsi.setting.hardwaretest.tradetest.dialog.NotificationDialog;
import com.witsi.tools.TOOL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;

@SuppressLint("HandlerLeak")
public abstract class BaseActivity extends Activity { 

     protected int mTimeOut;
     private TimeThread mTimeThread = null;
     
     /** 处理 handler 回传的信息 */ 
    
     protected void onCreate(Bundle savedInstanceState) { 
         super.onCreate(savedInstanceState); 
  
         requestWindowFeature(Window.FEATURE_NO_TITLE); 
//         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
     }  
     
     public void promptDialog( String title,String msg, int s, int requestCode)
 	{
 		PromptMsg promptMsg = new PromptMsg( title, msg, s);
 		Intent intent = new Intent( this, NotificationDialog.class);
 		Bundle bundle = new Bundle();
 		bundle.putParcelable( PromptMsg.PROMPT_MSG, promptMsg);
 		intent.putExtras(bundle);
 		startActivityForResult( intent, requestCode);	
 	}
     public void affirmDialog( String title,String msg, int requestCode)
 	{
 		PromptMsg promptMsg = new PromptMsg( title, msg, 0);
 		Intent intent = new Intent( this, AffirmDialog.class);
 		Bundle bundle = new Bundle();
 		bundle.putParcelable( PromptMsg.PROMPT_MSG, promptMsg);
 		intent.putExtras(bundle);
 		startActivityForResult( intent, requestCode);	
 	}
     
 	public void inputDialog( int maxLen, int minLen, 
 			String title, String msg, InputMode type, int requestCode)
 	{
 		
 		InputParam param = new InputParam( maxLen, minLen, title, msg, type);
 		Intent intent = new Intent( this,InputDialog.class);
 		Bundle bundle = new Bundle();
 		bundle.putParcelable( InputParam.INPUT_PARAM, param);
 		intent.putExtras(bundle);
 		
 		startActivityForResult( intent, requestCode);	
 	}
 	
 	public void inputDialog( int maxLen, int minLen, 
 			String title, String msg, String defaultText,InputMode type, int requestCode)
 	{
 		
 		InputParam param = new InputParam( maxLen, minLen, title, msg, defaultText, type);
 		Intent intent = new Intent( this,InputDialog.class);
 		Bundle bundle = new Bundle();
 		bundle.putParcelable( InputParam.INPUT_PARAM, param);
 		intent.putExtras(bundle);
 		
 		startActivityForResult( intent, requestCode);	
 	}
     public void setTimeOut(int val)
     {
    	 mTimeOut = val;
    	 mTimeThread = new TimeThread();
    	 mTimeThread.start();
     }
     
     public boolean timeCount()
     {
     	if(mTimeOut > 0)
     	{
     		return false;
     	}
     	
     	return true;
     }

     public class MyHandler extends Handler {
         public MyHandler() {
         }

         public MyHandler(Looper L) {
             super(L);
         }
         
     }
     
     private class TimeThread extends Thread {
 		@SuppressWarnings("unused")
		private boolean flag = true;

         public void run() {
         	while(true){
         		mTimeOut--;
         		TOOL.ProgramSleep(100);
         		if(mTimeOut<=0)
         			setStop();
         	}
         }
         public void setStop() {  
             this.flag = false;  
         }  
     }
     
} 