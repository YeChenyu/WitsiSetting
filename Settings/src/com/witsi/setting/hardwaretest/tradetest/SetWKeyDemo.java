package com.witsi.setting.hardwaretest.tradetest;


import com.witsi.setting.hardwaretest.TradeActivity;
import com.witsi.setting.hardwaretest.tradetest.dialog.ProgDialog;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvExecCallback.TRANS_RESULT;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtKeyType;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtLoadKeyCallback;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtResult;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtWorkKeyType;
import com.witsi.smart.terminal.sdk.api.WtDevContrl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;


public class SetWKeyDemo  
{

	private static final String TAG = "SetWKeyDemo";
	private static final boolean D = true;
	private Context context;

	private ProgDialog mProgDialog;
	private Handler handler;
	
	private boolean mIsMagCard = false;
		
	private WtDevContrl mWtDevContrl = WtDevContrl.getInstance();
	
	private TRANS_RESULT mTransResult = TRANS_RESULT.EMV_TRANS_TERMINATE;
	
	
	public SetWKeyDemo(Context context, Handler handler) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.handler = handler;
		//设备初始化
		devInit(context);
		
		core();
	}
	
    private void devInit(Context context)
    {
    	//设备初始化    	
    	mWtDevContrl.initDev(context);
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


	
	String key1[] = {"950973182317F80B950973182317F80B",  //pin   22222222222222222222222222222222
					"F679786E2411E3DEF679786E2411E3DE",   //mac   33333333333333333333333333333333
					"A0C45C59F1E549BBA0C45C59F1E549BB"};  //track 44444444444444444444444444444444
	
	String verbuf1[] = {"0000000000000000",
						"0000000000000000",
						"0000000000000000"};
	
	String valbuf1[] = {"00962B60","ADC67D84","E2F24340"};
	
	private void core()
	{
		showProgDialog("正在设置工作密钥");
		mWtDevContrl.isConnected();
		mWtDevContrl.loadWorkKey(TradeActivity.GROUP_ID, key1, verbuf1, valbuf1, new WtLoadKeyCallback(){
			
			@Override															
			public void onGetLoadKeyState(WtKeyType type,
					WtWorkKeyType wtype, WtResult result, int code) {
				// TODO Auto-generated method stub
				Log.v(TAG, "onGetLoadKeyState()");
				Message msg = handler.obtainMessage();
				msg.obj = wtype;
				msg.arg1 = code;
				if(result == WtResult.WT_SUCC)
				{
					msg.what = TradeActivity.WK_SUCCESS;
					 mWtDevContrl.setCurrKey(TradeActivity.GROUP_ID);
				}else if(result == WtResult.WT_FAIL)
				{
					msg.what = TradeActivity.WK_FAILED;
				}
				handler.sendMessage(msg);
				if(mProgDialog!=null){
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

