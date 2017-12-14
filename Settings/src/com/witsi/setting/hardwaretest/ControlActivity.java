package com.witsi.setting.hardwaretest;
import com.witsi.setting1.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
//import android.hardware.Stm32Manager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.witsi.arq.ArqConverts;
import android.witsi.arq.ArqMisc;
import android.witsi.arq.ArqStatus;

public class ControlActivity extends Activity implements OnClickListener{
	protected static final String TAG = "ControlActivity";

	int test = 0;
	Button btpwr ;
	// PowerManager mPowerManager;
 //   Stm32Manager Stm32S;
    ArqStatus status;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hardware_control_activity);

	//	Stm32S = new Stm32Manager(); 
		status= new ArqStatus(this);
		findViewById(R.id.button_close_T1).setOnClickListener(this);
        findViewById(R.id.button_open_T1).setOnClickListener(this);

        findViewById(R.id.button_KEY_OPEN).setOnClickListener(this);
        findViewById(R.id.button_KEY_CLOSE).setOnClickListener(this);
        
        findViewById(R.id.button_open_service).setOnClickListener(this);
        findViewById(R.id.button_close_service).setOnClickListener(this);
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean GetCpuStatus() {
		ArqMisc arqMisc = new ArqMisc(this);
		int ret;
		int try_num = 3;
    	byte[] version = new byte[32];
    	ret = arqMisc.getSystemInfo(0x01, version);
    	if(ret <= 0) {
    		Log.e(TAG, "Reset P1 failed. ret = " + ret);
    	} else {
    		return true;
    	}
  //  	Stm32S.setBootMode(1);
    	while(true){
//	    	Stm32S.setPower(0);
	    	try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	//    	Stm32S.setPower(1);
	    	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	ret = arqMisc.getSystemInfo(0x01, version);
	    	if(ret <= 0) {
	    		Log.e(TAG, "Reset P1 failed. ret = " + ret);
	    	} else {
	    		return true;
	    	}
	    	try_num --;
	    	if(try_num<=0)return false;
    	}
	}
	public void onClick(View v) {
    	int ItemId = v.getId();
    	Intent intent;
    	
    	switch(ItemId) {
    	case R.id.button_open_T1:
 //   		Stm32S.setBootMode(1);
//			Stm32S.setPower(1);
//			Stm32S.reset();
    		Toast.makeText(ControlActivity.this, "开启P1",Toast.LENGTH_LONG ).show();
     		break;
    	case R.id.button_close_T1:
 //   		Stm32S.setPower(0);
//			Stm32S.setBootMode(0);
 //   		Toast.makeText(ControlActivity.this, "关闭P1",Toast.LENGTH_LONG ).show();
    		break;	
    	case R.id.button_KEY_OPEN:
  //  		Stm32S.setBootMode(0);
  //  		Stm32S.setPower(1);
	//		Stm32S.reset();
    		Toast.makeText(ControlActivity.this, "进入下载模式",Toast.LENGTH_LONG ).show();
    		break;
    	case R.id.button_KEY_CLOSE:
    		Toast.makeText(ControlActivity.this, "开关机时间测试",Toast.LENGTH_LONG ).show();
    		//Stm32S.reset();
    		boolean flag = false;
    		flag = GetCpuStatus();
    		Log.e(TAG, "GetCpuStatus ret = " + flag);
   		break;
    	case R.id.button_open_service:
    		Toast.makeText(ControlActivity.this, "open arq service",Toast.LENGTH_LONG ).show();
  //  		status.OpenArqService();
    		
   		break;
    	case R.id.button_close_service:
    		Toast.makeText(ControlActivity.this, "close arq service",Toast.LENGTH_LONG ).show();
    		//Stm32S.reset();
 //   		status.CloseArqService();
   		break;
    	default:
    		Log.e(TAG, "Error!");
    		return;
    	}
    }

}
