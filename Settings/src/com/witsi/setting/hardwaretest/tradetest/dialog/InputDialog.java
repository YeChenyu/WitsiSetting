package com.witsi.setting.hardwaretest.tradetest.dialog;

import java.lang.reflect.Field;

import com.witsi.setting1.R;
import com.witsi.setting.hardwaretest.tradetest.base.BaseActivity;
import com.witsi.setting.hardwaretest.tradetest.base.InputParam;
import com.witsi.setting.hardwaretest.tradetest.base.InputParam.InputMode;
import com.witsi.setting.hardwaretest.tradetest.base.PromptMsg;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InputDialog extends BaseActivity { 

	private String TAG = "DevManagePasswd";
	private boolean D = true;
	
	public static final String INPUT_STRING = "INPUT_STRING";
	public static final int RESULT_SUCC = 1;
	public static final int RESPOND_ESC = 2;
    public static final int GET_KEY = 1;
    public static final int GET_KEY_END = 2;
    public static final int  KEY_DEL = 3;
    
	private static final int END_ENTER = 1;

    private EditText mEditText;


	private AlertDialog progressDialog ;
	private InputParam mInputParam;
	
	private boolean activityStatus = true;
	@Override
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
		Intent intent = this.getIntent();
		mInputParam = (InputParam)intent.getParcelableExtra(InputParam.INPUT_PARAM);
		viewDialog();

    } 
    public void onStart() { 
		super.onStart();

    } 
    
    public void onPause()
    {
    	super.onPause();
    	activityStatus = false;
    }
    
    public void onResume()
    {
    	super.onPause();
    	activityStatus = true;
    }
    
    public void finish()
    {
    	super.finish();

  
    }
    
    @SuppressLint("InlinedApi")
	public LinearLayout initView()
    {
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.hardware_input_edit, null);
		TextView textView = (TextView)layout.findViewById(R.id.textView);
		textView.setText(mInputParam.getMsg() + ": ");
		
        mEditText = (EditText)layout.findViewById(R.id.editText);
        if(mInputParam.getInputType() == InputMode.INPUT_NUM)
        {
        	 mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else if(mInputParam.getInputType() == InputMode.INPUT_PIN)
        {
        	mEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        	
        }else if(mInputParam.getInputType() == InputMode.INPUT_AB)
        {

        }else if(mInputParam.getInputType() == InputMode.INPUT_OTHER)
        {
        	
        }
        if(mInputParam.getMaxLen()>0)
        	mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mInputParam.getMaxLen())});
        
        
        if(mInputParam.getDefaultText()!=null)
        {
        	mEditText.setText(mInputParam.getDefaultText());
        }
        return layout;
    }
    
	@SuppressLint("InlinedApi")
	protected void viewDialog()
	{
		LinearLayout layout = initView();
		
        AlertDialog dialog = new AlertDialog.Builder(InputDialog.this)
		.setTitle(mInputParam.getTitle())
     	.setIcon(android.R.drawable.ic_dialog_info)                
     	.setView(layout)
     	.setPositiveButton("确定",new DialogInterface.OnClickListener() {               
			public void onClick(DialogInterface dialog, int which) {
				
				try { 
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
					field.setAccessible(true); 
					field.set(dialog, false);
					} 
				catch (Exception e) { 
					e.printStackTrace(); 
				}//不关闭对话框
				
				mHandler.obtainMessage(GET_KEY_END, END_ENTER, -1).sendToTarget();
				
			}
		})
		.setNegativeButton("取消",new DialogInterface.OnClickListener() {                
	    			public void onClick(DialogInterface dialog, int which) {
	    				finish();
	    			}
	    })
		.setOnKeyListener(new OnKeyListener() {  
            @Override  
            public boolean onKey(DialogInterface dialog, int keyCode,  
                    KeyEvent event) {  
                if (keyCode == KeyEvent.KEYCODE_BACK  
                        && event.getRepeatCount() == 0) {  
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
		progressDialog = dialog;

	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(D) Log.e(TAG, "+++ onKeyDown +++" + ":" +keyCode);
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
		}
		return false;
	}
	
	public void inputSucc()
	{

		String str = mEditText.getText().toString();
		Bundle bundle = new Bundle();
		bundle.putString(INPUT_STRING, str);
		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(RESULT_SUCC, intent);
		finish();
	}
	
	public void errPrompt()
	{
		PromptMsg promptMsg = new PromptMsg( mInputParam.getTitle(), "输入位数不足！", 5);
		Intent intent = new Intent( this,InputDialog.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable( PromptMsg.PROMPT_MSG, promptMsg);
		intent.putExtras(bundle);
		startActivity( intent);	
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(D) Log.d( TAG, "onActivityResult " + resultCode);
    		finish();
	}
	
    private final MyHandler mHandler = new MyHandler() {
        public void handleMessage(Message msg) {
if(D) Log.e(TAG, "+++ MAGC_STATE_RETURN +++" + ":"+msg.what+ ":"+ msg.arg1);
        	switch (msg.what) {
            	case GET_KEY:

            		if( mEditText.getText().length() < mInputParam.getMaxLen())
            		{
            			int position = mEditText.getSelectionStart();// 光标当前位置
            			mEditText.getText().insert(position , "" + msg.arg1);// 插入！！
            		}
            		break;   		
            	case GET_KEY_END:
            		
            		if(mEditText.getText().length() >= mInputParam.getMinLen())
            		{
            			inputSucc();
            			
            		}else
            		{
            			errPrompt();
            		}
            		break;
            	case KEY_DEL:
            		int position = mEditText.getSelectionStart();// 光标当前位置
            		if(position > 0)
            			mEditText.getText().delete(position - 1, position);// 删除光标后的一位
                	break;
        	}
        }
    };



}