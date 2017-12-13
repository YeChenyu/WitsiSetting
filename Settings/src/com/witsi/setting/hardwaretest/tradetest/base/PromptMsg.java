
package com.witsi.setting.hardwaretest.tradetest.base;
import android.os.Parcel;
import android.os.Parcelable;

public  class PromptMsg implements Parcelable {
	public static final String PROMPT_MSG = "PromptMsg";
	public String mTitle;
	public String mMsg;
	public int S;
	public PromptMsg(String title, String msg,int s) {
		mTitle = title;
		mMsg = msg;
		S = s;
	}
	
	public PromptMsg(Parcel source) {
		mTitle = source.readString();
		mMsg = source.readString();
		S = source.readInt();
	}
	
	public static final Parcelable.Creator<PromptMsg> CREATOR = new Creator<PromptMsg>()  
    {  
 
        @Override  
        public PromptMsg createFromParcel(Parcel source) {  
            return new PromptMsg(source);  
        }  
  
        @Override  
        public PromptMsg[] newArray(int size) {  
            return new PromptMsg[size];  
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
		dest.writeString(mTitle);
		dest.writeString(mMsg);
		dest.writeInt(S);
	}

}
	