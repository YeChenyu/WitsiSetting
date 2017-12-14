/* Copyright (C) 2009 Mokoid Open Source Project
 * Copyright (C) 2009,2010 Moko365 Inc.
 *
 * Author: Jollen Chen <jollen@moko365.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.witsi.setting.hardwaretest.tradetest.base;


public class TableViewParam
{
//    private static final String TAG = "InputParam";
    public static final String ITEM_PARAM = "TableViewParam";

	
	private ViewType viewType;
	private String ParamName;//编辑框默认值
	private int AppId; //下拉框默认id
	private String lable;
	private int editMax;
	private String[] list;
	private OnListener listener;
	public enum ViewType{
		EDIT_NUM,    
		EDIT_PWD,
		EDIT_OTHER,
		SPINNER,
		BUTTON
	}
	
	public TableViewParam( ViewType type, String defaultText, String lable, int max, OnListener listener) 
	{
		super();
		this.viewType = type;
		this.ParamName = defaultText;
		this.lable = lable;
		this.editMax = max;
		this.listener = listener;
	}

	public TableViewParam( ViewType type, String defaultText, String lable, String[] list, OnListener listener) 
	{
		super();
		
		this.viewType = type;
		this.ParamName = defaultText;
		this.lable = lable;
		this.list = list;
		this.listener = listener;
	}
	
	public TableViewParam( ViewType type, int AppId, String lable, String[] list, OnListener listener) 
	{
		super();
		
		this.viewType = type;
		this.AppId = AppId;
		this.lable = lable;
		this.list = list;
		this.listener = listener;
	}
	
	public int getAppId()
	{
		return this.AppId;
	}
	
	public ViewType getViewType()
	{
		return this.viewType;
	}
	public String getParamName()
	{
		return this.ParamName;
	}
	
	public String getLable()
	{
		return this.lable;
	}
	
	public int getEditMaxNum()
	{
		return this.editMax;
	}
	

	public String[] spinnerList()
	{
		return this.list;
	}
	
	public OnListener funcListener()
	{
		return this.listener;
	}
}