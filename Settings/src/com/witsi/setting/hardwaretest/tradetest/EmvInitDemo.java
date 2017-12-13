

package com.witsi.setting.hardwaretest.tradetest;


import com.witsi.setting.hardwaretest.TradeActivity;
import com.witsi.setting.hardwaretest.tradetest.dialog.ProgDialog;
import com.witsi.smart.terminal.sdk.api.WtCallback.EmvInitState;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvExecCallback.TRANS_RESULT;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvInitCallback;
import com.witsi.smart.terminal.sdk.api.WtDevContrl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;



public class EmvInitDemo 
{

	private static final String TAG = "SetKekDemo";
	private static final boolean D = true;
	private Context context;

	private ProgDialog mProgDialog;
	private Handler handler;
	
	private boolean mIsMagCard = false;
		
	private WtDevContrl mWtDevContrl = WtDevContrl.getInstance();
	
	private TRANS_RESULT mTransResult = TRANS_RESULT.EMV_TRANS_TERMINATE;
	
	public EmvInitDemo(Context context, Handler handler) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.handler = handler;
		//设备初始化
		devInit(context);
		
	}

    private void devInit(Context context)
    {
  
    	//设备初始化    	
    	mWtDevContrl.initDev(context);
    	core();
    }
	
	private void showProgDialog(String msg)
	{
		if(mProgDialog == null)
			mProgDialog = new ProgDialog( context, msg);
		else
			mProgDialog.setMsg(msg);
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)//返回键无效，目的在输入PIN操作过程不会因为该操作导致异常
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_MENU) {
		     return false;
	    }

		return true;
	}

	
	private void core()
	{
		showProgDialog("emv内核初始化");
		mWtDevContrl.initEmv( 0, new WtEmvInitCallback(){

			@Override
			public void onGetEmvInitState(EmvInitState state) {
				// TODO Auto-generated method stub
				Message msg = handler.obtainMessage();
				if(state == EmvInitState.EMVINIT_SUCC)
				{
					msg.what = TradeActivity.EMVINIT_SUCC;
					
				}else if(state == EmvInitState.EMVINIT_IO_ERR)
				{
					msg.what = TradeActivity.EMVINIT_IO_ERR  ;	
				}else if(state == EmvInitState.EMVFILE_LOAD_ERR)
				{
					msg.what = TradeActivity.EMVFILE_LOAD_ERR  ;	
				}else if(state == EmvInitState.EMVLIB_LOAD_ERR)
				{
					msg.what = TradeActivity.EMVLIB_LOAD_ERR;  	
				}else if(state == EmvInitState.EMVINIT_OTHER_ERR)
				{
					msg.what = TradeActivity.EMV_OTHER; 	
				}
				handler.sendMessage(msg);
				if(mProgDialog!=null)
		    	{
		    		mProgDialog.closeView();
		    	}
			}
			
		});

	}

	
	
	public void destroy() {
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

