package com.witsi.setting.hardwaretest.tradetest;

import com.witsi.setting1.R;
import com.witsi.setting.hardwaretest.tradetest.base.BaseActivity;
import com.witsi.setting.hardwaretest.tradetest.dialog.ProgDialog;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvExecCallback.TRANS_RESULT;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtGotoCmdModeCallback;
import com.witsi.smart.terminal.sdk.api.WtDevContrl;
import com.witsi.tools.TOOL;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;

public class GotoCmdDemo extends BaseActivity 
{

	private static final String TAG = "GotoCmdDemo";
	private static final boolean D = true;
	

	private ProgDialog mProgDialog;
	
	private boolean mIsMagCard = false;
		
	private WtDevContrl mWtDevContrl = WtDevContrl.getInstance();
	
	private TRANS_RESULT mTransResult = TRANS_RESULT.EMV_TRANS_TERMINATE;
	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(TOOL.isPad(this))
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		}else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 

		}
		init();
		
    	core();
	}
	
	public void init()
	{
			//设备初始化
		devInit();
	}


    private void devInit()
    {
    	
    	//设备初始化    	
    	mWtDevContrl.initDev(this);
    	
   
    }
	
	private void showProgDialog(String msg)
	{
		if(mProgDialog == null)
			mProgDialog = new ProgDialog( GotoCmdDemo.this, msg);
		else
			mProgDialog.setMsg(msg);
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)//返回键无效，目的在输入PIN操作过程不会因为该操作导致异常
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_MENU) {
		     super.openOptionsMenu();
	    }

		return true;
	}


	private void core()
	{
		showProgDialog("命令模式");
		mWtDevContrl.gotoCmdMode(new WtGotoCmdModeCallback(){

			@Override
			public void onExit() {
				// TODO Auto-generated method stub
				finish();
			}
			
		});

	}




	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//必须销毁
		if(mWtDevContrl!=null)
		{
			mWtDevContrl.destroy();
		}
	

    	if(mProgDialog!=null)
    	{
    		mProgDialog.closeView();
    	}
    	

    	
	}

}

