package com.witsi.setting.hardwaretest;


import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.witsi.arq.*;

public class ArqActivity extends Activity implements OnClickListener {
		private static final String TAG = "ArqActivity";
		
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.hardware_arq_activity);
            
            findViewById(R.id.key_test_btn).setOnClickListener(this);
            findViewById(R.id.magc_test_btn).setOnClickListener(this);
            findViewById(R.id.icc_test_btn).setOnClickListener(this);
            findViewById(R.id.rtc_test_btn).setOnClickListener(this);
            findViewById(R.id.security_test_btn).setOnClickListener(this);
            findViewById(R.id.version_test_btn).setOnClickListener(this);
            findViewById(R.id.buzzer_test_btn).setOnClickListener(this);
            findViewById(R.id.led_test_btn).setOnClickListener(this);
            findViewById(R.id.mem_test_btn).setOnClickListener(this);
            findViewById(R.id.download_app_btn).setOnClickListener(this); 
            findViewById(R.id.printer_app_btn).setOnClickListener(this);    
            findViewById(R.id.button_sleep).setOnClickListener(this);
            
        }

        public void onClick(View v) {
        	int ItemId = v.getId();
        	Intent intent;
        	
        	switch(ItemId) {
        	case R.id.printer_app_btn:
        		intent = new Intent(ArqActivity.this, PrinterActivity.class);
        		break;
        	case R.id.key_test_btn:
        		intent = new Intent(ArqActivity.this, KeyActivity.class);
        		break;
        	case R.id.magc_test_btn:
        		intent = new Intent(ArqActivity.this, MagcActivity.class);
        		break;
        	case R.id.icc_test_btn:
        		intent = new Intent(ArqActivity.this, IccActivity.class);
        		break;
        	case R.id.rtc_test_btn:
        		intent = new Intent(ArqActivity.this, RtcActivity.class);
        		break;
        	case R.id.security_test_btn:
        		intent = new Intent(ArqActivity.this, SecurityActivity.class);
        		break;
        	case R.id.version_test_btn:
        		intent = new Intent(ArqActivity.this, VersionActivity.class);
        		break;   
        	case R.id.buzzer_test_btn:
        		intent = new Intent(ArqActivity.this, BuzzerActivity.class);
        		break;   
        	case R.id.led_test_btn:
        		intent = new Intent(ArqActivity.this, LedActivity.class);
        		break;   
        	case R.id.download_app_btn:
        		intent = new Intent(ArqActivity.this, DwnLoadActivity.class);
        		break; 
        	case R.id.button_sleep:
        	{

        		ArqStatus arq_status;
        		arq_status = new ArqStatus(ArqActivity.this);

        		int ret = 0;
  //      		ret = arq_status.Sleep();
				if(ret < 0) {
					Toast.makeText(ArqActivity.this, "进入休眠模式 失败",Toast.LENGTH_LONG ).show();
				}else{
					Toast.makeText(ArqActivity.this, "进入休眠模式成功",Toast.LENGTH_LONG ).show();
				}
			
        		return; 
        	}     		
        	default:
        		FyLog.e(TAG, "Error!");
        		return;
        	}
        	startActivity(intent);
        }
}