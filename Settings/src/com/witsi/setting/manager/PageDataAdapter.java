/***************************************
 * Project: PagedViewDemo
 * Package: com.magicstudio.pagedviewdemo
 * File:    PageDataAdapter.java
 * Created by dumbbellyang at 2015-5-21 上午9:37:01
 * 
 *
 **************************************/
package com.witsi.setting.manager;

import java.util.ArrayList;
import java.util.List;



import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PageDataAdapter extends PagerAdapter {
	
	MyViewPager viewPager;
	List<LuncherView> views;
	private Context context;
	
	public PageDataAdapter(Context context, MyViewPager view, List<LuncherView> _items){
		this.viewPager = view;
		this.views = _items;
		this.context = context;
	}
	
	@Override
	//获取当前窗体界面数
	public int getCount() {
		// TODO Auto-generated method stub
		return views.size();
	}

	@Override
	//断是否由对象生成界面
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}
	
	//是从ViewGroup中移出当前View
	public void destroyItem(View pagerView, int idx, Object object) { 
		((ViewPager) pagerView).removeView((View) object);
    }  
	
	//返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
	@SuppressLint("ResourceAsColor")
	public Object instantiateItem(View pagerView, int idx){
		((ViewPager) pagerView).addView(views.get(idx));
		viewPager.setObjectForPosition(views.get(idx), idx);
//		if(idx == 0){
//			views.get(idx).setBackgroundColor(R.color.yellow_darkorange);
//		}else if(idx == 1){
//			views.get(idx).setBackgroundColor(R.color.red);
//		}else if(idx == 2){
//			views.get(idx).setBackgroundColor(R.color.black);
//		}else if(idx == 3){
//			views.get(idx).setBackgroundColor(R.color.brown_yellow);
//		}
		return views.get(idx);	

	}
	
}

