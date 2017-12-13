package com.witsi.activitys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.witsi.adapter.MainGvAdapter;
import com.witsi.adapter.TimelineAdapter;
import com.witsi.setting1.R;
import com.witsi.views.SinkView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SelfTestActivity extends Activity implements OnItemClickListener
						, Callback	, OnClickListener{

	private String TAG = SelfTestActivity.class.getSimpleName();
	private final boolean D = true;
	private Context context = SelfTestActivity.this;
	
	private LinearLayout ll_back;
	private SinkView sinkView;
	private float mPercent = 0;
    private Thread mThread;
    
    private ListView lv_menu;
    private TimelineAdapter adapter;
    private Button btn_cmd;
    
    private Handler handler;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selftest_activity);
		
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		ll_back.setOnClickListener(this);
		((TextView)findViewById(R.id.tv)).setText("设备自检");
		findViewById(R.id.action_back).findViewById(R.id.sw).setVisibility(View.GONE);
		sinkView = (SinkView) findViewById(R.id.sink);
		sinkView.setOnClickListener(this);
		sinkView.setPercent(1.0f);
		lv_menu = (ListView) findViewById(R.id.lv_menu);
		adapter = new TimelineAdapter(this, getData());
		lv_menu.setAdapter(adapter);
		
		btn_cmd = (Button) findViewById(R.id.btn);
		
		handler = new Handler(this);
		
		
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "这是第1行测试数据");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "这是第2行测试数据");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "这是第3行测试数据");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "这是第4行测试数据");
		list.add(map);
		return list;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.ll_back:
			finish();
			break;
		case R.id.sink:
			btn_cmd.setText("取消");
			mThread = new Thread(new Runnable() {

	            @Override
	            public void run() {

	                mPercent = 0;
	                while (mPercent <= 1) {
	                    sinkView.setPercent(mPercent);
	                    mPercent += 0.01f;
	                    try {
	                        Thread.sleep(40);
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                }
	                //当进度条到100%时，重置初始百分比
//	                mPercent = 0;
//	                sinkView.setPercent(mPercent);
	                handler.sendEmptyMessage(0);
	            }
	        });
	        mThread.start();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean handleMessage(Message arg0) {
		// TODO Auto-generated method stub
		btn_cmd.setText("完成");
		return false;
	}

}
