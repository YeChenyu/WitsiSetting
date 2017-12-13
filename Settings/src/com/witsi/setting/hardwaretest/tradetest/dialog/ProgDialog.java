package com.witsi.setting.hardwaretest.tradetest.dialog;



import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProgDialog {
	private static final String TAG = "ProgDialog";
	private static final boolean D = true;

	ProgressDialog mProgressDialog; 
	TextView mTextView = null;
	public ProgDialog(Context context,String msg ) {
		ProgDialogView(context, msg);
	}

	@SuppressLint("ResourceAsColor")
	public void ProgDialogView(Context context,String msg)
	{
//		mTextView = new TextView(context);
//		mTextView.setText(msg);
//		mTextView.setTextSize(30);
//		mTextView.setVisibility(View.VISIBLE);
//		mTextView.setTextColor(R.color.black);
		
		mProgressDialog = new ProgressDialog(context); 
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		mProgressDialog.setView(mTextView);
		mProgressDialog.setMessage(msg); 
		mProgressDialog.setCancelable(false);
		
		mProgressDialog.show();

		View v = mProgressDialog.getWindow().getDecorView();
		setDialogText(v);
		if(D)Log.i(TAG,"mTextView : " + mTextView);

	}
	
	 private void setDialogText(View v) { 

         if (v instanceof ViewGroup) { 
                 ViewGroup parent = (ViewGroup) v; 
                 int count = parent.getChildCount(); 
                 for (int i = 0; i < count; i++) { 

                         View child = parent.getChildAt(i); 
                         if(D)Log.i(TAG,"View : " + child);
                         setDialogText(child); 
                 } 
         } else if (v.toString().indexOf("TextView") >= 0) { 
        	 mTextView = ((TextView) v);
         } 
         return;
 }
	
	public void dismiss()
	{
		 mProgressDialog.dismiss();
	}
	
	public void show()
	{
		mProgressDialog.show();
	}
	
	public void setMsg(String msg)
	{
		if(mTextView != null)
		{
			mTextView.setText(msg);
		}
		mProgressDialog.show();
	}
	
	public void closeView()
	{
		mTextView = null;
		mProgressDialog.dismiss();
		mProgressDialog.cancel();
	}
	
}