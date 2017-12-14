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
    
	//���帡�����ڲ���
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //���������������ò��ֲ����Ķ���
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
		//��ȡWindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		//����window type
		wmParams.type = LayoutParams.TYPE_PHONE; 
		//����ͼƬ��ʽ��Ч��Ϊ����͸��
        wmParams.format = PixelFormat.RGBA_8888; 
        //���ø������ڲ��ɾ۽���ʵ�ֲ���������������������ɼ����ڵĲ�����
        wmParams.flags = 
//          LayoutParams.FLAG_NOT_TOUCH_MODAL |
          LayoutParams.FLAG_NOT_FOCUSABLE
//          LayoutParams.FLAG_NOT_TOUCHABLE
          ;
        
        //������������ʾ��ͣ��λ��Ϊ����ö�
        wmParams.gravity = Gravity.LEFT | Gravity.TOP; 
        
        // ����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
        wmParams.x = 0;
        wmParams.y = 0;


        
        //�����������ڳ�������  
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //��ȡ����������ͼ���ڲ���
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.hardware_loat_layout, null);
        //���mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        
        FyLog.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
        FyLog.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
        FyLog.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
        FyLog.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());      
        
        //�������ڰ�ť
        mFloatView = (Button)mFloatLayout.findViewById(R.id.float_id);
        
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        FyLog.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
        FyLog.i(TAG, "Height/2--->" + mFloatView.getMeasuredHeight()/2);
        //���ü����������ڵĴ����ƶ�
        mFloatView.setOnTouchListener(new OnTouchListener() 
        {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				//getRawX�Ǵ���λ���������Ļ�����꣬getX������ڰ�ť������
				wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth()/2;
				//FyLog.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
				FyLog.i(TAG, "RawX" + event.getRawX());
				FyLog.i(TAG, "X" + event.getX());
				//25Ϊ״̬���ĸ߶�
	            wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight()/2 - 25;
	           // FyLog.i(TAG, "Width/2--->" + mFloatView.getMeasuredHeight()/2);
	            FyLog.i(TAG, "RawY" + event.getRawY());
	            FyLog.i(TAG, "Y" + event.getY());
	             //ˢ��
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
