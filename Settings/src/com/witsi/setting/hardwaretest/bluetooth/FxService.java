package com.witsi.setting.hardwaretest.bluetooth;



import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FxService extends Service 
{
	
    public final static String ACTION_GATT_CONNECTED =
            "com.example.base.FxService.ACTION_GATT_CONNECTED";
    public final static  String CONNECTED_STATE = "CONNECTED_STATE";
    
	//定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;
	
	Button mFloatView;
	
	private static final String TAG = "FxService";
	
	private FxServiceOnClink mFxServiceOnClink = null;
	
	private ServiceBinder serviceBinder = new ServiceBinder();
	
	
	@Override
	public void onCreate() 
	{
		// TODO Auto-generated method stub
		super.onCreate();
		FyLog.i(TAG, "oncreat");
		createFloatView();
        //Toast.makeText(FxService.this, "create FxService", Toast.LENGTH_LONG);		
		
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	public  class ServiceBinder extends Binder implements IFxService {
		

		@Override
		public View getView() {
			// TODO Auto-generated method stub
			return mFloatView;
		}

		@Override
		public void addOnClink(FxServiceOnClink onClink) {
			// TODO Auto-generated method stub
			mFxServiceOnClink = onClink;
		}
    }

	
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return serviceBinder;
	}

	private void createFloatView()
	{
		wmParams = new WindowManager.LayoutParams();
		//获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		//设置window type
		wmParams.type = LayoutParams.TYPE_PHONE; 
		//设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888; 
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = 
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
          LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
          ;
        
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP; 
        
        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;


        
        //设置悬浮窗口长宽数据  
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.hardware_loat_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        
        FyLog.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
        FyLog.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
        FyLog.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
        FyLog.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());      
        
        //浮动窗口按钮
        mFloatView = (Button)mFloatLayout.findViewById(R.id.float_id);
        
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        FyLog.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
        FyLog.i(TAG, "Height/2--->" + mFloatView.getMeasuredHeight()/2);
        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new OnTouchListener() 
        {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				//getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
				wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth()/2;
				//FyLog.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
				FyLog.i(TAG, "RawX" + event.getRawX());
				FyLog.i(TAG, "X" + event.getX());
				//25为状态栏的高度
	            wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight()/2 - 25;
	           // FyLog.i(TAG, "Width/2--->" + mFloatView.getMeasuredHeight()/2);
	            FyLog.i(TAG, "RawY" + event.getRawY());
	            FyLog.i(TAG, "Y" + event.getY());
	             //刷新
	            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
				return false;
			}
		});	

        mFloatView.setOnClickListener(new OnClickListener() 
        {
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				Toast.makeText(FxService.this, "onClick", Toast.LENGTH_SHORT).show();
				if(mFxServiceOnClink != null)
					mFxServiceOnClink.OnClink(v);
			}
		});
	}

	
	@Override
	public void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mGattUpdateReceiver);
		if(mFloatLayout != null)
		{
			mWindowManager.removeView(mFloatLayout);
		}
	}
	
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
            	
            	mFloatView.setText(intent.getStringExtra(CONNECTED_STATE));
            } 
        }
    };
    
    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        return intentFilter;
    }
	
	
}
