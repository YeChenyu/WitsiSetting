/* add by cjcheng start {----------------------------------- */
/* support mouse advance 2013-04-25 */
package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.SeekBarDialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.util.Log;
import java.lang.Exception;

public class SmdtMouseAdvancePreference extends SeekBarDialogPreference implements
SeekBar.OnSeekBarChangeListener {
    private final String TAG = "MOUSE_ADVANCE";
    private SeekBar mSeekBar;
    private int value;
    private int mouseStep;
    private Context mContext;

    public SmdtMouseAdvancePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context; 
        try{
            mouseStep = Settings.System.getInt(mContext.getContentResolver(), Settings.System.MOUSE_ADVANCE);
            value = mouseStep/5 - 1;
        } catch(Exception e) {
            Log.e(TAG, "throw Exception");
        }
        Log.e(TAG, "mouse advance value = " + value + "step = " + mouseStep);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(10);
        mSeekBar.setProgress(value);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        value = seekBar.getProgress();
    }

    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult){
            mouseStep = (value + 1) * 5;
        }
        Log.e(TAG, "onDialogClosed mouseStep = " + mouseStep);
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.MOUSE_ADVANCE, mouseStep);
    }
}
/* add by cjcheng end   -----------------------------------} */
