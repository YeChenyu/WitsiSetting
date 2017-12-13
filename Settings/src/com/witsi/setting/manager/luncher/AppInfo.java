package com.witsi.setting.manager.luncher;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.witsi.setting1.R;

public class AppInfo implements Parcelable{

	public int position_screen = -1;
    public int position_x = -1; // 列位置
    public int position_y = -1;//  行位置
    public float weight = 1.0f;//  TipView在行中所占的比重
    public int back_color = R.color.white;// 背景颜色
    public String icons = "ic_launcher.png";
    public Bitmap bitIcon = null;
    public int label_size = 15;// 应用标签字体大小
    /**
     * The intent used to start the application.
     */
    public Intent intent;
    /**
     * Title of the item
     */
    public String title;
    /**
     * A bitmap version of the application icon.
     */
    public Bitmap iconBitmap;
    public ComponentName componentName;
    
    
    public AppInfo() {
		// TODO Auto-generated constructor stub
	}
    
    public Intent getIntent() {
        return intent;
    }
    public void setIntent(Intent intent) {
		this.intent = intent;
	}
    public void setIcon(Bitmap bitmap)
    {
    	iconBitmap = bitmap;
    }

    public static final Parcelable.Creator<AppInfo> CREATOR = 
    		new Creator<AppInfo>() {
				@Override
				public AppInfo createFromParcel(Parcel arg0) {
					// TODO Auto-generated method stub
					AppInfo obj = new AppInfo();
					obj.position_screen = arg0.readInt();
					obj.position_x = arg0.readInt();
					obj.position_y = arg0.readInt();
					obj.weight = arg0.readFloat();
					obj.icons = arg0.readString();
					obj.title = arg0.readString();
					obj.back_color = arg0.readInt();
					obj.label_size = arg0.readInt();
					obj.bitIcon = (Bitmap) arg0.readParcelable(Bitmap.class.getClassLoader());
					obj.iconBitmap = (Bitmap) arg0.readParcelable(Bitmap.class.getClassLoader());
					obj.intent = (Intent) arg0.readParcelable(Intent.class.getClassLoader());
					obj.componentName = (ComponentName) arg0.readParcelable(ComponentName.class.getClassLoader());
					return obj;
				}

				@Override
				public AppInfo[] newArray(int arg0) {
					// TODO Auto-generated method stub
					return new AppInfo[arg0];
				}
	};
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		arg0.writeInt(position_screen);
		arg0.writeInt(position_x);
		arg0.writeInt(position_y);
		arg0.writeFloat(weight);
		arg0.writeString(icons);
		arg0.writeString(title);
		arg0.writeInt(back_color);
		arg0.writeInt(label_size);
		arg0.writeParcelable(bitIcon, arg1);
		arg0.writeParcelable(iconBitmap, arg1);
		arg0.writeParcelable(intent, arg1);
		arg0.writeParcelable(componentName, arg1);
	}
    
    
}
