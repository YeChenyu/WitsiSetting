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
import android.view.KeyEvent;


public class SetMKeyDemo 
{

	private static final String TAG = "SetMKeyDemo";
	private static final boolean D = true;
	private Context context;

	private ProgDialog mProgDialog;
	private Handler handler;
	
	private boolean mIsMagCard = false;
		
	private WtDevContrl mWtDevContrl = WtDevContrl.getInstance();
	
	private TRANS_RESULT mTransResult = TRANS_RESULT.EMV_TRANS_TERMINATE;
	
	public SetMKeyDemo(Context context, Handler handler) {
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
			mProgDialog = new ProgDialog(context, msg);
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

	String key0 = "F616DD76F290635EF616DD76F290635E";// 明文11111111111111111111111111111111
	String verbuf = "0000000000000000";
	String valbuf = "82E13665";
	
	
	private void core()
	{
		showProgDialog("正在设置主密钥");
		mWtDevContrl.clearKeyGroup(TradeActivity.GROUP_ID);
		mWtDevContrl.loadMainKey( TradeActivity.GROUP_ID, key0, verbuf, valbuf, new WtLoadKeyCallback(){

			@Override
			public void onGetLoadKeyState(WtKeyType arg0, WtWorkKeyType arg1,
					WtResult arg2, int arg3) {
				// TODO Auto-generated method stub
				Message msg = handler.obtainMessage();
				if(arg2 == WtResult.WT_SUCC)
				{
					msg.what = TradeActivity.MK_SUCCESS;
					msg.arg1 = arg3;
					mProgDialog.closeView();
				}else
				{
					msg.what = TradeActivity.MK_FAILED;
					msg.arg1 = arg3;
					mProgDialog.closeView();
				}
				handler.sendMessage(msg);
			}
	
		});
	}

	
	public void destroy(){
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

