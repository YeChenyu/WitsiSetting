package com.witsi.setting.hardwaretest;


import java.util.ArrayList;

import com.witsi.debug.FyLog;
import com.witsi.setting.hardwaretest.tradetest.EmvInitDemo;
import com.witsi.setting.hardwaretest.tradetest.MagcPowerTest;
import com.witsi.setting.hardwaretest.tradetest.SetMKeyDemo;
import com.witsi.setting.hardwaretest.tradetest.SetWKeyDemo;
import com.witsi.setting.hardwaretest.tradetest.TransDemo;
import com.witsi.setting1.R;
import com.witsi.smart.terminal.sdk.api.WtCallback.WtWorkKeyType;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;


import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import android.widget.ListView;

import android.widget.AdapterView.OnItemClickListener;



public class TradeActivity extends Activity implements OnItemClickListener
													, Callback{

	private static final String TAG = "MainActivity";
	public static final int GROUP_ID = 23;
	ListView mListView;
	TextView tv_show;
	Button btn_clear;
	Button btn_return;
	
	Handler handler;
	
	public static boolean isBluetooth = true;
	class FunMenu{
		String msg;
		Class<?> cls;
		public FunMenu(String msg, Class<?> cls)
		{
			this.msg = msg;
			this.cls = cls;
		}
	}
	private ArrayList<FunMenu>  mFunctionArray;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hardware_tradetest_activity);
		
		initData();
		
		initViews();
	}
	
	
	private void initData()
	{
		mFunctionArray = new ArrayList<FunMenu>();
		handler = new Handler(this);
		
		mFunctionArray.add(new FunMenu("1.设置主密钥", SetMKeyDemo.class));
		mFunctionArray.add(new FunMenu("2.设置工作密钥", SetWKeyDemo.class));
		mFunctionArray.add(new FunMenu("3.导入EMV配置", EmvInitDemo.class));
		mFunctionArray.add(new FunMenu("4.交易", TransDemo.class));
		mFunctionArray.add(new FunMenu("5.交易功耗测试", MagcPowerTest.class));
	}
	
	private void initViews() {
		
		mListView = (ListView) findViewById(R.id.listview);
		
		String[] array = new String[mFunctionArray.size()];
		for(int i = 0; i < mFunctionArray.size(); i++)
		{
			array[i] = mFunctionArray.get(i).msg;
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, array);
		
        mListView.setAdapter(arrayAdapter);
        mListView.setOnItemClickListener(this);
        tv_show = (TextView) findViewById(R.id.screen_show);
        btn_clear = (Button) findViewById(R.id.clear);
        btn_return = (Button) findViewById(R.id.back);
        btn_return.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isSleepExit = false;
				ActivityManagers.trunToEntryActivity(TradeActivity.this);
			}
		});
        btn_clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				tv_show.setText("");
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg2) {
		case 0:
			new SetMKeyDemo(TradeActivity.this, handler);
			break;
		case 1:
			new SetWKeyDemo(TradeActivity.this, handler);
			break;
		case 2:
			new EmvInitDemo(TradeActivity.this, handler);
			break;
		case 3:
			startActivity(new Intent(TradeActivity.this, mFunctionArray.get(arg2).cls));
			break;
		case 4:
			startActivityForResult(new Intent(TradeActivity.this, mFunctionArray.get(arg2).cls), 1);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		showMessageBlock("模拟交易总共测试：" + data.getIntExtra("test_cnt", 0) 
				+ "次 \n");
	}
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "onStop");
		if(!isSleepExit){
			finish();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	public static final int MK_SUCCESS = 0;
	public static final int MK_FAILED = 1;
	public static final int WK_SUCCESS = 2;
	public static final int WK_FAILED = 3;
	
	public static final int EMVINIT_SUCC = 4;
	public static final int EMVINIT_IO_ERR = 5;
	public static final int EMVFILE_LOAD_ERR = 6;
	public static final int EMVLIB_LOAD_ERR = 7;
	public static final int EMV_OTHER = 8;
	 
	
	@Override
	public boolean handleMessage(Message arg0) {
		// TODO Auto-generated method stub
		switch (arg0.what) {
		case MK_SUCCESS:
			showMessageBlock("设置主密钥: \n<status>" + "成功: " + arg0.arg1 
					+ " \n");
			break;
		case MK_FAILED:
			showMessageBlock("设置主密钥: \n<status>" + "失败: " + arg0.arg1 
					+ " \n");
			break;
		case WK_SUCCESS:
			if((WtWorkKeyType)arg0.obj == WtWorkKeyType.TYPE_PIN_KEY){
				showMessageBlock("设置工作密钥: \n<status>" + "成功"
						+ " \n");
			}else if((WtWorkKeyType)arg0.obj == WtWorkKeyType.TYPE_MAC_KEY){
				showMessageBlock("设置工作密钥: \n<status>" + "成功"
						+ " \n");
			}else if((WtWorkKeyType)arg0.obj == WtWorkKeyType.TYPE_TRACK_KEY){
				showMessageBlock("设置工作密钥: \n<status>" + "成功"
						+ " \n");
			}
			break;
		case WK_FAILED:
			if((WtWorkKeyType)arg0.obj == WtWorkKeyType.TYPE_PIN_KEY){
				showMessageBlock("设置工作密钥: \n<status>" + "失败"
						+ " \n");
			}else if((WtWorkKeyType)arg0.obj == WtWorkKeyType.TYPE_MAC_KEY){
				showMessageBlock("设置工作密钥: \n<status>" + "失败"
						+ " \n");
			}else if((WtWorkKeyType)arg0.obj == WtWorkKeyType.TYPE_TRACK_KEY){
				showMessageBlock("设置工作密钥: \n<status>" + "失败"
						+ " \n");
			}
			break;
		case EMVINIT_SUCC:
			showMessageBlock("EMV配置: \n<status>" + "成功"
					+ " \n");
			break;
		case EMVINIT_IO_ERR:
			showMessageBlock("EMV配置: \n<status>" + "IO异常"
					+ " \n");
			break;
		case EMVFILE_LOAD_ERR:
			showMessageBlock("EMV配置: \n<status>" + "文件加载失败"
					+ " \n");
			break;
		case EMVLIB_LOAD_ERR:
			showMessageBlock("EMV配置: \n<status>" + "失败"
					+ " \n");
			break;
		case EMV_OTHER:
			showMessageBlock("EMV配置: \n<status>" + "其他错误"
					+ " \n");
			break;
		default:
			break;
		}
		return false;
	}

	private void showMessageBlock(String message) {
        final String text = tv_show.getText().toString();
        FyLog.d(TAG, message);
        tv_show.setText(text + "\n" + newShowMeg(message)+"\n- - - - - -   block - - - - - - ");
    }
	
	private void showMessage(String message) {
        final String text = tv_show.getText().toString();
        FyLog.d(TAG, message);
        tv_show.setText(text + "\n" + newShowMeg(message)+"\n");
    }
	/**
	 * 工具方法－显示日志
	 * @param message
	 * @return
	 */
	private String newShowMeg(String message)
    {
        String temp = new String();
        for(int i=0;i<(message.length()+141)/142;i++)
        {
            if (i != (message.length()+141)/142 -1)
            {
            temp += 
                    (message.substring(i*142, (((i+1)*142)>message.length())
                    ?(message.length()):((i+1)*142)) +"\n");//每180长度,换一行
            }else 
            {
                temp +=
                        message.substring(i*142, message.length());
            }
        }
        return temp;
    }	
	
	
	/************************** 事件监听申明区 ***************************/
	@SuppressWarnings("deprecation")
	//点击返回键填出提示窗口
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // 创建退出对话框  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // 设置对话框标题  
            isExit.setTitle("系统提示");  
            // 设置对话框消息  
            isExit.setMessage("确定要退出吗");  
            // 添加选择按钮并注册监听  
            isExit.setButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					ActivityManagers.clearActivity();
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
  
        }  
        return false;  
          
    }  
}