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


import android.os.Parcel;
import android.os.Parcelable;


public class InputParam implements Parcelable  
{
//    private static final String TAG = "InputParam";
    public static final String INPUT_PARAM = "InputParam";
	private int mMaxLen = 16;
	private int mMinLen = 16;
	private String mTitle;
	private String mMsg;
	private String mDefaultText;
	private int inputType;
	
	public enum InputMode{
		INPUT_NUM,    
		INPUT_PIN,
		INPUT_AB,
		INPUT_OTHER       
	}
	
	public InputParam( int maxLen, int minLen, String title, String msg, InputMode type) 
	{
		super();
		mMaxLen = maxLen;
		mMinLen = minLen;
		mTitle = title;
		mMsg = msg;
		mDefaultText = null;
		if(type != null)
			inputType = type.ordinal();
	}

	public InputParam( int maxLen, int minLen, String title, String msg, 
			String defaultText,InputMode type) 
	{
		super();
		mMaxLen = maxLen;
		mMinLen = minLen;
		mTitle = title;
		mMsg = msg;
		mDefaultText = defaultText;
		if(type != null)
			inputType = type.ordinal();
	}
	public InputParam(Parcel source)
	{

		mMaxLen = source.readInt();
		mMinLen = source.readInt();
		mTitle = source.readString();
		mMsg = source.readString();
		mDefaultText = source.readString();
		inputType = source.readInt();
	}
	
	 public static final Parcelable.Creator<InputParam> CREATOR = new Creator<InputParam>()  
    {  
 
        @Override  
        public InputParam createFromParcel(Parcel source) {  
            return new InputParam(source);  
        }  
  
        @Override  
        public InputParam[] newArray(int size) {  
            return new InputParam[size];  
        }  
          
    }; 
    
    @Override  
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
    
    @Override  
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

		dest.writeInt(mMaxLen);
		dest.writeInt(mMinLen);
		dest.writeString(mTitle);
		dest.writeString(mMsg);
		dest.writeString(mDefaultText);
		dest.writeInt(inputType);
	}	
	
    public int getMaxLen()
    {
    	return 	mMaxLen ;
    }
    
    public int getMinLen()
    {
    	return 	mMinLen ;
    }
    public String getTitle()
    {
    	return 	mTitle ;
    }
    
    public String getMsg()
    {
    	return 	mMsg ;
    }
    public String getDefaultText()
    {
    	return 	mDefaultText ;
    }
    public InputMode getInputType()
    {
    	switch(inputType)
    	{
    		case 0:	
    			return InputMode.INPUT_NUM;
    		case 1:
    			return InputMode.INPUT_PIN;
    		case 2:
    			return InputMode.INPUT_AB;
    		case 3:
    			return InputMode.INPUT_OTHER;
    		default:
    			return InputMode.INPUT_NUM;
    	}
    }
    
}