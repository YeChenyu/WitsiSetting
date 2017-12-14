package com.witsi.adapter;

import com.witsi.setting1.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainLvAdapter extends BaseAdapter
{
  private Context context;
  private int[] image;
  private String[] label;

  public MainLvAdapter(Context paramContext, String[] paramArrayOfString, int[] paramArrayOfInt)
  {
    this.context = paramContext;
    this.label = paramArrayOfString;
    this.image = paramArrayOfInt;
  }

  public int getCount()
  {
    return this.label.length;
  }

  public Object getItem(int paramInt)
  {
    return this.label[paramInt];
  }

  public long getItemId(int paramInt)
  {
    return paramInt;
  }

  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
  {
    ImageView localImageView;
    TextView localTextView;
    ViewHolder localViewHolder1 ;
    if (paramView == null)
    {
      localViewHolder1 = new ViewHolder();
      paramView = LayoutInflater.from(this.context).inflate(R.layout.main_lv_item, null);
      localImageView = (ImageView)paramView.findViewById(R.id.iv);
      localTextView = (TextView)paramView.findViewById(R.id.tv);
      localViewHolder1.setIv(localImageView);
      localViewHolder1.setTv(localTextView);
      paramView.setTag(localViewHolder1);
    }else{
    	localViewHolder1 = (ViewHolder)paramView.getTag();
    	localImageView = localViewHolder1.getIv();
    	localTextView = localViewHolder1.getTv();
    }
    
      localTextView.setText(this.label[paramInt]);
      localImageView.setImageResource(this.image[paramInt]);
      return paramView;
  }

  class ViewHolder
  {
    private ImageView iv;
    private TextView tv;

    public ViewHolder()
    {
    }

    public ImageView getIv()
    {
      return this.iv;
    }

    public TextView getTv()
    {
      return this.tv;
    }

    public void setIv(ImageView paramImageView)
    {
      this.iv = paramImageView;
    }

    public void setTv(TextView paramTextView)
    {
      this.tv = paramTextView;
    }
  }
}