package com.witsi.setting.hardwaretest.tradetest;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.setting.hardwaretest.TradeActivity;
import com.witsi.setting.hardwaretest.tradetest.base.BaseActivity;
import com.witsi.setting.hardwaretest.tradetest.dialog.KeyBoardDialog;
import com.witsi.setting.hardwaretest.tradetest.dialog.PayPasswordView;
import com.witsi.setting.hardwaretest.tradetest.dialog.PayPasswordView.OnPayListener;
import com.witsi.setting.hardwaretest.tradetest.dialog.ProgDialog;
import com.witsi.smart.terminal.sdk.api.IccInfo;
import com.witsi.smart.terminal.sdk.api.WtCallback.EmvInitState;
import com.witsi.smart.terminal.sdk.api.WtCallback.EmvPinParam;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtCardCheckCallback;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtCardCheckErrResult;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvExecCallback;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvExecCallback.TRANS_RESULT;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvExecErrResult;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvInitCallback;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtEmvPinCompleteState;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtGetDevInfoCallback;
import com.witsi.smart.terminal.sdk.api.WtDevContrl;
import com.witsi.tools.TOOL;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.witsi.arq.ArqEmvLib.ConfirmRes;
import android.witsi.arq.ArqInput;
import android.witsi.arq.EmvParam.TransType;
import android.witsi.arq.onPinListener.ERR_STATE;
import android.witsi.arq.onPinProcessListener;


public class MagcPowerTest extends BaseActivity 
{

	private static final String TAG = "TransDemo";
	
	private static final boolean D = true;

	private ProgDialog mProgDialog;
	
	private boolean mIsMagCard = false;
		
	private WtDevContrl mWtDevContrl = WtDevContrl.getInstance();
	
	private TRANS_RESULT mTransResult = TRANS_RESULT.EMV_TRANS_TERMINATE;
	
	private KeyBoardDialog keyboard;
	
	private StringBuffer mStringBuffer = new StringBuffer();
	private int test_cnt = 0;
	private boolean isFinish = false;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		init();
	}
	
	public void init()
	{
			//设备初始化
		devInit( );
	}
	
	public  Bitmap byteConvertToBitmap(byte[] buffer) {
		if (null == buffer || buffer.length == 0)
			return null;

		return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
	}
	

	
    private void devInit( )
    {
    	//设备初始化
    	mProcessStep = 0;
    	mWtDevContrl.initDev(this );
    	mWtDevContrl.setCurrKey(TradeActivity.GROUP_ID);
    	processCore();
    	
   
    }
	
	private void showProgDialog(String msg)
	{
		if(!isFinish){
			if(mProgDialog == null)
				mProgDialog = new ProgDialog( MagcPowerTest.this, msg);
			else
				mProgDialog.setMsg(msg);
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean onKeyDown(int keyCode, KeyEvent event)//返回键无效，目的在输入PIN操作过程不会因为该操作导致异常
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(mProgDialog
					!= null)
				mProgDialog.dismiss();
			// 创建退出对话框  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // 设置对话框标题  
            isExit.setTitle("系统提示");  
            // 设置对话框消息  
            isExit.setMessage("确定要退出测试吗");  
            // 添加选择按钮并注册监听  
            isExit.setButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					isFinish = true;
					mWtDevContrl.cancel();
					Intent data = new Intent();
					data.putExtra("test_cnt", test_cnt);
					setResult(2, data);
					finish();
				}
			});  
            isExit.setButton2("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // 显示对话框  
            isExit.show();  
			return false;
		}else if (keyCode == KeyEvent.KEYCODE_MENU) {
		     super.openOptionsMenu();
	    }
		return true;
	}
	
	
	private int mProcessStep = 0;
	private void processCore()
	{
    	if(mProgDialog!=null)
    	{
    		mProgDialog.dismiss();
    	}
    	
Log.i( TAG, "processCore() step:" + mProcessStep);
	if(!isFinish)
		switch(mProcessStep++)
		{
			case 0:
				inputAmtView();//输入交易金额
				break;
			case 1:
				checkConnAndGetDevInf();//获取设备信息和密钥设置
				break;	
			case 2:				
				emvInit();//EMV初始化
				break;	
			case 3:
				cardOperateExec();//寻卡并读卡
				break;
			case 4: 
				if(mIsMagCard){
					confirmCardInfo();
				}else{
					processCore();
				}
				break;
			case 5:
				pinExec();//pin输入
				break;
			case 6:
				online();//联机
				break;
			default:
				processStepEnd();
//				print();
				break;
		}
		
	}
	

	LayoutInputAmt mLayoutInputAmt;
	double mCash;
	private void inputAmtView()
	{
		mCash = 0.01;
		processCore();
	}

	
	
	private void getDevInfo()
	{
		mWtDevContrl.getDevInfo(new WtGetDevInfoCallback(){

			@Override
			public void onGetDevInfo(String driveVer, String softwareVer,
					String sn) {
				// TODO Auto-generated method stub
				mDriveVer = driveVer;
				mSoftwareVer = softwareVer;
				mSvr = sn;
				processCore();//执行下一步
			}

			@Override
			public void onGetDevInfoErr() {
				// TODO Auto-generated method stub
				 Toast.makeText( MagcPowerTest.this, "获取设备信息失败",
			                Toast.LENGTH_SHORT).show();   	
				 finish();
			}
		});
	}

	//获取设备信息
	String mDriveVer; //驱动版本号
	String mSoftwareVer;//应用版本号
	private void checkConnAndGetDevInf()
	{
		mIsMagCard = false;
		if(!mWtDevContrl.isConnected())
		{
			mProcessStep = 0; 
			Toast.makeText( MagcPowerTest.this, "连接失败" ,
	                Toast.LENGTH_SHORT).show();   	
			processCore();
			
			return;
		}

		getDevInfo();

	}
	
	//emv内核初始化
	private void emvInit()
	{
		showProgDialog("emv内核初始化");
		mWtDevContrl.initEmv( 0, new WtEmvInitCallback(){

			@Override
			public void onGetEmvInitState(EmvInitState state) {
				// TODO Auto-generated method stub
				if(state == EmvInitState.EMVINIT_SUCC)
				{
					mProgDialog.dismiss();
					processCore();//执行下一步
					
				}else if(state == EmvInitState.EMVINIT_IO_ERR)
				{
					Toast.makeText( MagcPowerTest.this, "IO异常",
			                Toast.LENGTH_SHORT).show();   	
					finish();
				}else if(state == EmvInitState.EMVFILE_LOAD_ERR)
				{
					Toast.makeText( MagcPowerTest.this, "EMV配置文件加载失败",
			                Toast.LENGTH_SHORT).show();   	
					finish();
				}else if(state == EmvInitState.EMVLIB_LOAD_ERR)
				{
					Toast.makeText( MagcPowerTest.this, "EMV内核加载失败",
			                Toast.LENGTH_SHORT).show();   	
					finish();
				}else if(state == EmvInitState.EMVINIT_OTHER_ERR)
				{
					Toast.makeText( MagcPowerTest.this, "未知错误",
			                Toast.LENGTH_SHORT).show();   	
					finish();
				}
			}
			
		});
	}
	
	//寻卡操作
	String mPan;
	String mSvr;
	String mExpiryDate;
	String[] mTrack = new String[3];
	String mIccData;
	String mTrack2;
	private void cardOperateExec()
	{
		mIsMagCard = false;
		showProgDialog("请刷卡或插入卡片");
		//超时60S
Log.e(TAG,"cardOperateExec");
//		mWtDevContrl.cardOperateExecExtend(0x20, null, 60, new WtCardCheckCallback()
		mWtDevContrl.cardOperateExec( 0x20, 5, new WtCardCheckCallback()
//		mWtDevContrl.cardOperateExec( true, 60, new WtCardCheckCallback()
		{

			@Override
			//刷卡磁卡返回
			public void onTrackExistence(String pan, String expiryDate,
					String svr, String[] Track, boolean isIccCard) {
				// TODO Auto-generated method stub
			}
			
			//检测到ICC
			@Override
			public void onIccExistence() {
				// TODO Auto-generated method stub
Log.e(TAG,"onIccExistence");							
				mWtDevContrl.IccPbocExec(TransType.EMV_TRANS_GOODS, mCash, true, mWtEmvExecCallback );
			}
			
			//检测到RF
			@Override
			public void onRfExistence() {
				// TODO Auto-generated method stub
Log.e(TAG,"onRfExistence");					
				mWtDevContrl.QpbocExec(TransType.EMV_TRANS_GOODS, mCash, true, mWtEmvExecCallback);

			}

			@Override
			public void onCardCheckErr(WtCardCheckErrResult result, int errcode) {
				// TODO Auto-generated method stub
Log.i(TAG,"onCardCheckErr");						
				
				if(result == WtCardCheckErrResult.ERR_ESC)
				{
					Toast.makeText( MagcPowerTest.this, "取消退出",
			                Toast.LENGTH_SHORT).show();   	
					finish();					
				}else if(result == WtCardCheckErrResult.ERR_GET_MAG_DATA_FAIL)
				{
					Toast.makeText( MagcPowerTest.this, "刷卡失败:" + errcode,
			                Toast.LENGTH_SHORT).show();   	
					finish();
					
				}else if(result == WtCardCheckErrResult.ERR_IO)
				{
					Toast.makeText( MagcPowerTest.this, "IO异常:" + errcode,
			                Toast.LENGTH_SHORT).show();   	
					finish();
				}else if(result == WtCardCheckErrResult.ERR_MAG_DATA_ENCRYPT_FAIL)
				{
					Toast.makeText( MagcPowerTest.this, "磁道数据加密失败 errCode:" + errcode,
			                Toast.LENGTH_SHORT).show();   	
					finish();
				}else if(result == WtCardCheckErrResult.ERR_TIME_OUT)
				{
					Toast.makeText( MagcPowerTest.this, "5秒刷卡超时",
			                Toast.LENGTH_SHORT).show();   	
					mProgDialog.dismiss();
					mIsMagCard = true;
					mPan = "6212261402002318100";
					mExpiryDate = "20501020";
					mSvr = "6D-43";
					mTrack[0] = "";
					mTrack[1] = "3355893842fjdkfvvdffjwiut43589728472347";
					mTrack[2] = "";
	Log.i(TAG,"mPan:" + mPan);			
	Log.i(TAG,"mExpiryDate:" + mExpiryDate);		
	Log.i(TAG,"svr:" + mSvr);	
	Log.i(TAG,"mTrack[0]:" + mTrack[0]);	
	Log.i(TAG,"mTrack[1]:" + mTrack[1]);		
	Log.i(TAG,"mTrack[2]:" + mTrack[2]);		

					mStringBuffer.append("mPan:" + mPan + "\r\n");
					mStringBuffer.append("mExpiryDate:" + mExpiryDate + "\r\n");
					mStringBuffer.append("svr:" + mSvr + "\r\n");
					mStringBuffer.append("mTrack[0]:" + mTrack[0] + "\r\n");
					mStringBuffer.append("mTrack[1]:" + mTrack[1] + "\r\n");
					mStringBuffer.append("mTrack[2]:" + mTrack[2] + "\r\n");
					processCore();
				}
			}
			
		});
			
	}
	
	
	protected PayPasswordView getDecorViewDialog(String title, double mCash) {

		
		return PayPasswordView.getInstance( title, String.format("%.2f", mCash), MagcPowerTest.this, new OnPayListener() {

			@Override
			public void onSurePay() {// 这里调用验证密码是否正确的请求
				mWtDevContrl.pinBaseOper(ConfirmRes.ENTER);
			}

			@Override
			public void onCancelPay() {
				// TODO Auto-generated method stub
				mWtDevContrl.pinBaseOper(ConfirmRes.ESC);
			}

			@Override
			public void onDelPay() {
				// TODO Auto-generated method stub
				mWtDevContrl.pinBaseOper(ConfirmRes.BACKSPACE);
			}
			
		});
	}
	//卡号确认
	private void confirmCardInfo() {
		// TODO Auto-generated method stub
		String msg = "\n\n\n" +
					"卡号：" + mPan + "\n\n" +
					"卡截止日期：" + mExpiryDate + "\n\n" +
					"服务码：" + mSvr + "\n\n" +
					"Track1：" + mTrack[0] + "\n\n" +
					"Track2：" + mTrack[1] + "\n\n" +
					"Track3：" + mTrack[2] + "\n\n" ;
		
		new LayoutResult( this, "卡号信息", "确认", msg, Gravity.LEFT, new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
			}
		});
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				processCore();
			}
		}, 2000);
	}
	//pin输入
	private void pinExec()
	{
		if(mIsMagCard)
		{
			keyboard = new KeyBoardDialog(MagcPowerTest.this, getDecorViewDialog("请输入联机密码",mCash));
			keyboard.show();
			ArqInput pin = new ArqInput(getBaseContext());
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					FyLog.d(TAG, "ConfirmRes.ENTER");
					mWtDevContrl.pinBaseOper(ConfirmRes.ENTER);
				}
			}, 3000);
			mWtDevContrl.pinSecEntry(2, mPan, null, 6, 6, new onPinProcessListener(){
				@Override
				public void onError(ERR_STATE state, String arg1) {
					// TODO Auto-generated method stub
					Log.v(TAG, " the ERR_STATE is: " + state + " the msg is: " + arg1);
					keyboard.dismiss();
					if(state == ERR_STATE.STATE_PIN_BYPASS)
					{
						Toast.makeText( MagcPowerTest.this, "3秒" + arg1 ,
				                Toast.LENGTH_SHORT).show();   
						mTransResult = TRANS_RESULT.EMV_TRANS_ACCEPT;
						processStepEnd();
					}else if(state == ERR_STATE.STATE_PIN_FAIL)
					{
						Toast.makeText( MagcPowerTest.this, arg1 ,
				                Toast.LENGTH_SHORT).show();   
					}else if(state == ERR_STATE.STATE_PIN_QUIT)
					{
						Toast.makeText( MagcPowerTest.this, arg1 ,
				                Toast.LENGTH_SHORT).show();   
					}else if(state == ERR_STATE.STATE_PIN_TIMEOUT)
					{
						Toast.makeText( MagcPowerTest.this, arg1 ,
				                Toast.LENGTH_SHORT).show();   
					}
//					mTransResult = TRANS_RESULT.EMV_TRANS_DENIAL;
//					processStepEnd();
				}

				@Override
				public void onGetPinData(byte[] pin) {
					// TODO Auto-generated method stub
					keyboard.dismiss();
					mTransResult = TRANS_RESULT.EMV_TRANS_ACCEPT;
					processStepEnd();
					mStringBuffer.append("pin:" + TOOL.hexByte2HexStr(pin) + "\r\n");
					Toast.makeText( MagcPowerTest.this, "PIN输入成功:" +  TOOL.hexByte2HexStr(pin),
			                Toast.LENGTH_SHORT).show();   
				}

				@Override
				public void onPinInputReturn(int num) {
					// TODO Auto-generated method stub
					keyboard.KeyChange(num);
				}
				
			} );
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for (int i = 0; i < 7; i++) {
						keyboard.KeyChange(i);
					}
				}
			}, 500);
			
		}else
		{
			processCore();//ICC 密码输入在EMV流程中处理
		}
		
	}
	
	

	
	
	private void online()
	{
		
		//联机上送，省略，进行联机响应处理
		if(mIsMagCard)
		{
			mTransResult = TRANS_RESULT.EMV_TRANS_ACCEPT;
			processCore();
		}else
		{
			//ICC卡交易联机后的二次授权处理。参数1对应直连规范8583报文的39域，参数2对应55域
			mWtDevContrl.IccPbocOnlineAuthExec("00", null, mWtEmvExecCallback);
		}
	}
	
	private void processStepEnd()
	{
    	if(mProgDialog!=null)
    	{
    		mProgDialog.dismiss();
    	}
    	String msg = "交易终止";
    	if(mTransResult == TRANS_RESULT.EMV_TRANS_ACCEPT)
    	{
    		msg = "交易接受";
    	}else if(mTransResult == TRANS_RESULT.EMV_TRANS_DENIAL)
    	{
    		msg = "交易拒绝";
    	}else if(mTransResult == TRANS_RESULT.EMV_TRANS_FALLBACK)
    	{
    		msg = "降级";
    	}else if(mTransResult == TRANS_RESULT.EMV_TRANS_QPBOC_ACCEPT)
    	{
    		msg = "QPBOC交易接受";
    	}else if(mTransResult == TRANS_RESULT.EMV_TRANS_QPBOC_DENIAL)
    	{
    		msg = "QPBOC拒绝";
    	}else if(mTransResult == TRANS_RESULT.EMV_TRANS_QPBOC_ONLINE)
    	{
    		msg = "QPBOC请求交易联机";
    	}

    	msg = (msg + "\n\n\n第" + ++test_cnt + "次测试结束");
    	if(mTransResult == TRANS_RESULT.EMV_TRANS_FALLBACK){
    		new LayoutResult(MagcPowerTest.this, "消费", "继续交易", msg, Gravity.LEFT, new OnClickListener(){

    			@Override
    			public void onClick(View arg0) {
    				// TODO Auto-generated method stub
    					mProcessStep = 3;
    		    		processCore();
    			}
    			
    		});
    	}else if(!mIsMagCard && mTransResult == TRANS_RESULT.EMV_TRANS_ACCEPT
    			|| mTransResult == TRANS_RESULT.EMV_TRANS_QPBOC_ACCEPT
    			|| mTransResult == TRANS_RESULT.EMV_TRANS_QPBOC_DENIAL
    			|| mTransResult == TRANS_RESULT.EMV_TRANS_QPBOC_ONLINE){
    		String msg1 = "\n" +
					"交易结果：" + msg + "\n\n" +
					"卡号：" + mPan + "\n\n" +
					"应用截止日期：" + mExpiryDate + "\n\n" +
					"卡序列号：" + mSvr + "\n\n" +
					"Track2：" + mTrack2 + "\n\n" +
					"iccData：" + mIccData + "\n\n" ;
//    		Log.v(TAG, "the msg is: " + msg1);
			new LayoutResult(MagcPowerTest.this, "交易结果", "完成", msg1, Gravity.LEFT, new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					finish();
				}
			});
    	}else{
    		new LayoutResult(MagcPowerTest.this, "交易结果", msg, Gravity.LEFT, new OnClickListener(){
    			@Override
    			public void onClick(View arg0) {
    				// TODO Auto-generated method stub
    					finish();
    			}
    		});
    	}
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mProcessStep = 0;
	    		processCore();
			}
		}, 2000);
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
	
	WtEmvExecCallback mWtEmvExecCallback  = new WtEmvExecCallback()
	{

		@Override
		public void onError(WtEmvExecErrResult result, int errorCode) {
			// TODO Auto-generated method stub
			if(D)Log.i(TAG,"onError（）");
			if(mProgDialog!=null)
	    	{
	    		mProgDialog.dismiss();
	    	}
			
			if(result == WtEmvExecErrResult.ERR_GET_DATA_FAIL)
			{
				Toast.makeText( MagcPowerTest.this, "读取ICC数据失败" + errorCode,
		                Toast.LENGTH_SHORT).show();   	
			}else if(result == WtEmvExecErrResult.ERR_TRACK_DATA_ENCRYPT_ERR)
			{
				Toast.makeText( MagcPowerTest.this, "加密磁道数据失败 err:" + errorCode,
		                Toast.LENGTH_SHORT).show();   	
			}
			
			mTransResult = TRANS_RESULT.EMV_TRANS_TERMINATE;
			
			processStepEnd();
		}

		@Override
		public void onEmvGetPan(String arg0) {
			// TODO Auto-generated method stub
			FyLog.v(TAG, "onEmvGetPan（） and the pan is: " + arg0);
			mWtDevContrl.ContinuePbocExec(this);
		}
		@Override
		public void onEmvRequestOnline(String pan, String appExpireDate,
				String cardSn, byte[] pin, byte[] track2, byte[] iccdata) {
			// TODO Auto-generated method stub
			FyLog.v(TAG, "onEmvRequestOnline（） ");
			if(D)Log.i(TAG,"pan:" + pan);
			if(D)Log.i(TAG,"appExpireDate:" + appExpireDate);
			if(D)Log.i(TAG,"cardSn:" + cardSn);
			if(D)Log.i(TAG,"track2:" + TOOL.hexByte2HexStr(track2));
			if(D)Log.i(TAG,"iccdata:" + TOOL.hexByte2HexStr(iccdata));
			if(D)Log.i(TAG,"pin:" + TOOL.hexByte2HexStr(pin));
			
			mStringBuffer.append("pan:" + pan + "\r\n");
			mStringBuffer.append("appExpireDate:" + appExpireDate + "\r\n");
			mStringBuffer.append("cardSn:" + cardSn + "\r\n");
			mStringBuffer.append("track2:" + TOOL.hexByte2HexStr(track2) + "\r\n");
			mStringBuffer.append("iccdata:" + TOOL.hexByte2HexStr(iccdata) + "\r\n");
			if(mProgDialog!=null)
	    	{
	    		mProgDialog.dismiss();
	    	}
			mPan = pan;
			mExpiryDate = appExpireDate;
			mSvr = cardSn;
			mTrack2 = TOOL.hexByte2HexStr(track2);
			mIccData = TOOL.hexByte2HexStr(iccdata);
			mTransResult = TRANS_RESULT.EMV_TRANS_ACCEPT;
			processStepEnd();
		}

		@Override
		public void onIccComplete(TRANS_RESULT result, byte[] iccData, byte[] arg2,
											boolean arg,
											int code) {
			// TODO Auto-generated method stub
			if(D)Log.i(TAG,"onIccComplete（）");
			if(D)Log.i(TAG,"TRANS_RESULT:" + result);
			if(D)Log.i(TAG,"iccData:" + TOOL.hexByte2HexStr(iccData));
			if(mProgDialog!=null)
	    	{
	    		mProgDialog.dismiss();
	    	}
			if(result == TRANS_RESULT.EMV_TRANS_TERMINATE)
			{
				Toast.makeText( MagcPowerTest.this, "交易终止  err:" + code,
		                Toast.LENGTH_SHORT).show();   
			}
			mIccData = TOOL.hexByte2HexStr(iccData);
			IccInfo info  = mWtDevContrl.getIcCardInfo();
			mPan = info.getPan();
			mSvr = info.getCardSn();
			mExpiryDate = info.getAppExpireDate();
			mTrack2 = TOOL.hexByte2HexStr(info.getTrack2());
			String msg1 = "\n" +
					"卡号：" + mPan + "\n\n" +
					"应用截止日期：" + mExpiryDate + "\n\n" +
					"卡序列号：" + mSvr + "\n\n" +
					"Track2：" + mTrack2 + "\n\n" +
					"iccData：" + TOOL.hexByte2HexStr(arg2) + "\n\n" ;
    		Log.v(TAG, "the msg is: " + msg1);
			mTransResult = result;
			processStepEnd();
		}

		@Override
		public EmvPinParam onEmvPinStart(String pan, int type, String lable) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onEmvPinStart Pan:" + pan);
			if(mProgDialog != null)
				mProgDialog.closeView();
			keyboard = new KeyBoardDialog(MagcPowerTest.this, getDecorViewDialog(lable, mCash));
			keyboard.show();
			EmvPinParam param = new EmvPinParam(1, 6, 60);
			return param;
		}

		@Override
		public void onEmvPinKey(int num) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onEmvPinKey");
			keyboard.KeyChange(num);
		}

		@Override
		public void onEmvPinComplete(WtEmvPinCompleteState state, byte[] pin) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onEmvPinComplete");
			keyboard.dismiss();
			if(state == WtEmvPinCompleteState.STATE_PIN_BYPASS)
			{
				Toast.makeText( MagcPowerTest.this, "跳过PIN输入" ,
		                Toast.LENGTH_SHORT).show();   
			}else if(state == WtEmvPinCompleteState.STATE_PIN_FAIL)
			{
				Toast.makeText( MagcPowerTest.this, "PIN处理失败" ,
		                Toast.LENGTH_SHORT).show();   
			}else if(state == WtEmvPinCompleteState.STATE_PIN_QUIT)
			{
				Toast.makeText( MagcPowerTest.this, "取消PIN" ,
		                Toast.LENGTH_SHORT).show();   
			}else if(state == WtEmvPinCompleteState.STATE_PIN_TIMEOUT)
			{
				Toast.makeText( MagcPowerTest.this, "PIN超时" ,
		                Toast.LENGTH_SHORT).show();   
			}else if(state == WtEmvPinCompleteState.STATE_PIN_SUCC)
			{
				mStringBuffer.append("pin:" + TOOL.hexByte2HexStr(pin) + "\r\n");
				Toast.makeText( MagcPowerTest.this, "PIN输入成功:" +  TOOL.hexByte2HexStr(pin),
		                Toast.LENGTH_SHORT).show();   
			}

		}

	};

}

