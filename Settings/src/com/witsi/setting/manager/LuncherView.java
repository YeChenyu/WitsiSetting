package com.witsi.setting.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.witsi.setting1.R;


import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LuncherView extends TableLayout{

	private static final String TAG = LuncherView.class.getSimpleName();
	private final static boolean D = true;
	public static final int TYPE_FULL_ROW = 0;
	public static final int TYPE_HALF_ROW = 1;
	public static final int TYPE_ROW_2_3 = 2;
	public static final int TYPE_ROW_1_3 = 3;
	
	private Context context;
	private TableLayout table;
	private View v;
	private RelativeLayout.LayoutParams mParams;
	private Display dis = null;
	private boolean isAttrs = false;
	private TextView show;
	private ImageView add;
	private int layoutHeight = 0;
	
	public LuncherView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		dis = ((Activity)context).getWindowManager().getDefaultDisplay();

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.manager_table_layout, this);
		table = (TableLayout) v.findViewById(R.id.tl_table);
		show = (TextView) v.findViewById(R.id.tv);
		add = (ImageView) v.findViewById(R.id.iv);
	}

	public LuncherView(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context, attrs);
		dis = ((Activity)context).getWindowManager().getDefaultDisplay();
		this.context = context;
//		TypedArray a = context.obtainStyledAttributes(attrs, 
//				R.styleable.tip_view); 
		table = this;
		isAttrs = true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// disallowing padding in paged view (just pass 0)
//		Log.e("TAG", "onAttachedToWindow(" + widthMeasureSpec +", "+ heightMeasureSpec +")");
		if(isAttrs){
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					widthMeasureSpec, 
					heightMeasureSpec
					);
			this.setLayoutParams(lp);
		}
	}
	
	
	@Override 
	protected void onAttachedToWindow() { 
		super.onAttachedToWindow(); 
//		Log.e("TAG", "onAttachedToWindow()..");
		/**
         * tl_table 布局设置
         */
		mParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 
				LayoutParams.FILL_PARENT) ;
		if (mParams != null) { 
			mParams.width = dis.getWidth() - 10;
			mParams.height = dis.getHeight();
			mParams.topMargin = 0;
			mParams.bottomMargin = 5;
			mParams.leftMargin = 5;
			mParams.rightMargin = 5;
//			mParams.gravity = Gravity.CENTER;
			table.setLayoutParams(mParams); 
		}
		layoutHeight = mParams.height;
	}
	
	private Map<Integer, Map<Integer, TipView>> mapList 
		= new HashMap<Integer, Map<Integer, TipView>>();
	public void addAppInfoData(List<TipView> tips){
		Log.v("TAG", "addAppInfoData()");
		Map<Integer, TipView> map;
		for (TipView tip : tips) {
//			Log.v(TAG, "the x is: "+ tip.getPositionX() + " : " + tip.getPositionY());
			if(mapList.containsKey(tip.getPositionY())){
				map = mapList.get(tip.getPositionY());
			}else{
				map = new HashMap<Integer, TipView>();
				mapList.put(tip.getPositionY(), map);
			}
			if(map != null)
				map.put(tip.getPositionX(), tip);
		}
//		Log.v("TAG", "the map size is: " + mapList.size());
	}
	
	public void addMainTipView(Context context, Map<Integer, Map<Integer, TipView>> mapList){
		Log.v("TAG", "addMainTipView()");
		for (int i = 0; i < mapList.size(); i++) {
			Map<Integer, TipView> map = mapList.get(i);
			TableRow row = new TableRow(context);
			row.setWeightSum(1.0f);
			for (int j = 0; j < map.size(); j++) {
				row.addView(map.get(j));
			}
			table.addView(row);
		}
	}
	public void addMainTipView(Context context){
		Log.v("TAG", "addMainTipView()");
		Set<Integer> setRow = mapList.keySet();	
		for (Iterator iteratorRow = setRow.iterator(); iteratorRow.hasNext();) {
			Integer i = (Integer) iteratorRow.next();
//		for (int i = 0; i < mapList.size(); i++) {
			Map<Integer, TipView> map = mapList.get(i);
			TableRow row = new TableRow(context);
			row.setWeightSum(1.0f);
			Set<Integer> set = map.keySet();	
			for (Iterator iterator = set.iterator(); iterator.hasNext();) {
				Integer j = (Integer) iterator.next();
				row.addView(map.get(j));
			}
			table.addView(row);
		}
	}
	
	public void addLogo(Context context, View v){
		Log.v("TAG", "addLogo()");
		TableRow row = new TableRow(context);
		TableLayout.LayoutParams lp = new TableLayout.LayoutParams(
				TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
		row.setLayoutParams(lp);
		row.setWeightSum(1.0f);
		row.addView(v);
		table.addView(row);
	}

	public final static int WEIGHT_TYPE_1_1_1_1_1 = 0;
	public final static int WEIGHT_TYPE_2_1_1_1 = 1;
	public final static int WEIGHT_TYPE_1_2_1_1 = 2;
	public final static int WEIGHT_TYPE_1_1_2_1 = 3;
	public final static int WEIGHT_TYPE_1_1_1_2 = 4;
	public final static int WEIGHT_TYPE_1_2_2 = 5;
	public final static int WEIGHT_TYPE_2_1_2 = 6;
	public final static int WEIGHT_TYPE_2_2_1 = 7;
	public final static int WEIGHT_TYPE_3_1_1 = 8;
	public final static int WEIGHT_TYPE_1_3_1 = 9;
	public final static int WEIGHT_TYPE_1_1_3 = 10;
	public final static int WEIGHT_TYPE_3_2 = 11;
	public final static int WEIGHT_TYPE_2_3 = 12;
	public final static int WEIGHT_TYPE_1_1 = 13;
	public final static int WEIGHT_TYPE_1 = 14;
	/**
	 * 
	 * @param context
	 * @param tip
	 * @param weightType 比重类型，一行总共5个
	 */
	public void addTipView(Context context, TipView[] tip, int weightType){
		TableRow full_row = new TableRow(context);
		setTipWeight(full_row, tip, weightType);
		for (int i = 0; i < tip.length; i++) {
			full_row.addView(tip[i]);
		}
		table.addView(full_row);
	}
	
	private void setTipWeight(TableRow row, TipView[] tip, int weightType) {
		// TODO Auto-generated method stub
		switch (weightType) {
		case WEIGHT_TYPE_1_1_1_1_1:
			row.setWeightSum(5);
			break;
		case WEIGHT_TYPE_2_1_1_1:
			row.setWeightSum(6.0f);
			tip[0].setWeightType(3.0f);
			tip[1].setWeightType(1.0f);
			tip[2].setWeightType(1.0f);
			tip[3].setWeightType(1.0f);
			break;
		case WEIGHT_TYPE_1_2_1_1:
			row.setWeightSum(6.0f);
			tip[0].setWeightType(1.0f);
			tip[1].setWeightType(3.0f);
			tip[2].setWeightType(1.0f);
			tip[3].setWeightType(1.0f);
			break;
		case WEIGHT_TYPE_1_1_2_1:
			row.setWeightSum(6.0f);
			tip[0].setWeightType(1.0f);
			tip[1].setWeightType(1.0f);
			tip[2].setWeightType(3.0f);
			tip[3].setWeightType(1.0f);
			break;
		case WEIGHT_TYPE_1_1_1_2:
			row.setWeightSum(6.0f);
			tip[0].setWeightType(1.0f);
			tip[1].setWeightType(1.0f);
			tip[2].setWeightType(1.0f);
			tip[3].setWeightType(3.0f);
			break;
		case WEIGHT_TYPE_1_2_2:
			row.setWeightSum(7f);
			tip[0].setWeightType(1.0f);
			tip[1].setWeightType(3.0f);
			tip[2].setWeightType(3.0f);
			break;
		case WEIGHT_TYPE_2_1_2:
			row.setWeightSum(7f);
			tip[0].setWeightType(3.0f);
			tip[1].setWeightType(1.0f);
			tip[2].setWeightType(3.0f);
			break;
		case WEIGHT_TYPE_3_1_1:
			row.setWeightSum(7f);
			tip[0].setWeightType(5.0f);
			tip[1].setWeightType(1.0f);
			tip[2].setWeightType(1.0f);
			break;
		case WEIGHT_TYPE_1_3_1:
			row.setWeightSum(7f);
			tip[0].setWeightType(1.0f);
			tip[1].setWeightType(5.0f);
			tip[2].setWeightType(1.0f);
			break;
		case WEIGHT_TYPE_1_1_3:
			row.setWeightSum(7f);
			tip[0].setWeightType(1.0f);
			tip[1].setWeightType(1.0f);
			tip[2].setWeightType(5.0f);
			break;
		case WEIGHT_TYPE_3_2:
			row.setWeightSum(7f);
			tip[0].setWeightType(5.0f);
			tip[1].setWeightType(2.0f);
			break;
		case WEIGHT_TYPE_2_3:
			row.setWeightSum(7f);
			tip[0].setWeightType(2.0f);
			tip[1].setWeightType(5.0f);
			break;
		default:
			break;
		}
	}
	
	public int isShowVisibility(){
		return show.getVisibility();
	}
	
	public void setShowMsg(String msg){
		show.setText(msg);
	}
	public void setShowVisibility(int visible){
		show.setVisibility(visible);
	}
	public void setAddVisibility(int visible){
		add.setVisibility(visible);
	}
	
	public void setAddOnClickListener(OnClickListener l){
//		add.setOnClickListener(l);
	}
	
	public int getLayoutHeight() {
		return layoutHeight;
	}
}
