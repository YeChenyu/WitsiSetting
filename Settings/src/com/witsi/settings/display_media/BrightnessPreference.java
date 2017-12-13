/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.witsi.settings.display_media;

import com.witsi.debug.FyLog;
import com.wtisi.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.SeekBarDialogPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class BrightnessPreference extends Preference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {
    // If true, enables the use of the screen auto-brightness adjustment setting.
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT =
            PowerManager.useScreenAutoBrightnessAdjustmentFeature();

    private static final String TAG = BrightnessPreference.class.getSimpleName();
    
    private final int mScreenBrightnessMinimum;
    private final int mScreenBrightnessMaximum;

    private SeekBar mSeekBar;
    private CheckBox mCheckBox;
    private LayoutInflater mInflater;

    private int mOldBrightness;
    private int mOldAutomatic;

    private boolean mAutomaticAvailable;
    private boolean mAutomaticMode;

    private int mCurBrightness = -1;

    private boolean mRestoredOldState;

    private static final int SEEK_BAR_RANGE = 10000;


    public BrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mScreenBrightnessMinimum = pm.getMinimumScreenBrightnessSetting();
        mScreenBrightnessMaximum = pm.getMaximumScreenBrightnessSetting();

        mAutomaticAvailable = context.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        mInflater = LayoutInflater.from(context);
        
        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);

        getContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                mBrightnessModeObserver);
        mRestoredOldState = false;
        
//        final ContentResolver resolver = getContext().getContentResolver();
//        if (positiveResult) {
//            setBrightness(mSeekBar.getProgress(), true);
//        } else {
//            restoreOldState();
//        }
//        resolver.unregisterContentObserver(mBrightnessObserver);
//        resolver.unregisterContentObserver(mBrightnessModeObserver);
    }

    

    @Override
    protected View onCreateView(ViewGroup parent) {
    	// TODO Auto-generated method stub
    	View view = mInflater.inflate(R.layout.preference_dialog_brightness, null);
    	mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setMax(SEEK_BAR_RANGE);
        mOldBrightness = getBrightness();
        mSeekBar.setProgress(mOldBrightness);

        mCheckBox = (CheckBox)view.findViewById(R.id.automatic_mode);
        FyLog.d(TAG, "mAutomaticAvailable="+ mAutomaticAvailable+ " getBrightnessMode(0)="+ getBrightnessMode(0));
        if (mAutomaticAvailable) {
            mOldAutomatic = getBrightnessMode(0);
            mAutomaticMode = mOldAutomatic == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            mCheckBox.setChecked(mAutomaticMode);
            mCheckBox.setOnCheckedChangeListener(this);
            mSeekBar.setEnabled(!mAutomaticMode);
        } else {
        	mCheckBox.setEnabled(false);
            mSeekBar.setEnabled(true);
        }

        mSeekBar.setOnSeekBarChangeListener(this);
    	return view;
    }

    
    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mCurBrightness = -1;
            if(selfChange)
            	onBrightnessChanged();
        }
    };

    private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessModeChanged();
        }
    };
    
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        setBrightness(progress, true);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    /**
     * 亮度自动调节
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        
        mSeekBar.setProgress(getBrightness());
        mSeekBar.setEnabled(!isChecked);
        setMode(isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
              : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        setBrightness(mSeekBar.getProgress(), true);
    }

    
    
    /**
     * 获取亮度  分为自动调节模式和手动调节模式
     * @return
     */
    private int getBrightness() {
        int mode = getBrightnessMode(0);
        float brightness = 0;
        if (USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT
                && mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            brightness = Settings.System.getFloat(getContext().getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, 0);
            brightness = (brightness+1)/2;
        } else {
            if (mCurBrightness < 0) {
                brightness = Settings.System.getInt(getContext().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 100);
            } else {
                brightness = mCurBrightness;
            }
            brightness = (brightness - mScreenBrightnessMinimum)
                    / (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
        }
        return (int)(brightness*SEEK_BAR_RANGE);
    }

    /**
     * 设置亮度
     * @param brightness
     * @param write
     */
    private void setBrightness(int brightness, boolean write) {
        if (mAutomaticMode) {//自动调节模式
            if (USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT) {
                float valf = (((float)brightness*2)/SEEK_BAR_RANGE) - 1.0f;
                if (write) {
				    final ContentResolver resolver = getContext().getContentResolver();
				    Settings.System.putFloat(resolver,
				            Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, valf);
				}
            }
        } else {
            int range = (mScreenBrightnessMaximum - mScreenBrightnessMinimum);
            brightness = (brightness * range)/SEEK_BAR_RANGE + mScreenBrightnessMinimum;
            if (write) {
			    mCurBrightness = -1;
			    final ContentResolver resolver = getContext().getContentResolver();
			    Settings.System.putInt(resolver,
			            Settings.System.SCREEN_BRIGHTNESS, brightness);
			} else {
			    mCurBrightness = brightness;
			}
        }
    }

    private void restoreOldState() {
        if (mRestoredOldState) return;

        if (mAutomaticAvailable) {
            setMode(mOldAutomatic);
        }
        setBrightness(mOldBrightness, false);
        mRestoredOldState = true;
        mCurBrightness = -1;
    }
    
    /**
     * 获取亮度调节模式
     * @param mode
     */
    private void setMode(int mode) {
        mAutomaticMode = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    /**
     * 获取亮度调节模式
     * @param defaultValue
     * @return
     */
    private int getBrightnessMode(int defaultValue) {
        int brightnessMode = defaultValue;
        try {
            brightnessMode = Settings.System.getInt(getContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
        	snfe.printStackTrace();
        }
        return brightnessMode;
    }

    /**
     * seekbar 更新
     */
    private void onBrightnessChanged() {
    	if(mSeekBar != null)
    		mSeekBar.setProgress(getBrightness());
    }

    private void onBrightnessModeChanged() {
    	if(mSeekBar != null)
    		mSeekBar.setProgress(getBrightness());
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        //myState.automatic = mCheckBox.isChecked();
        myState.progress = mSeekBar.getProgress();
        myState.oldAutomatic = mOldAutomatic == 1;
        myState.oldProgress = mOldBrightness;
        myState.curBrightness = mCurBrightness;

        // Restore the old state when the activity or dialog is being paused
        restoreOldState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mOldBrightness = myState.oldProgress;
        mOldAutomatic = myState.oldAutomatic ? 1 : 0;
        setMode(myState.automatic ? 1 : 0);
        setBrightness(myState.progress, false);
        mCurBrightness = myState.curBrightness;
    }

    private static class SavedState extends BaseSavedState {

        boolean automatic;
        boolean oldAutomatic;
        int progress;
        int oldProgress;
        int curBrightness;

        public SavedState(Parcel source) {
            super(source);
            automatic = source.readInt() == 1;
            progress = source.readInt();
            oldAutomatic = source.readInt() == 1;
            oldProgress = source.readInt();
            curBrightness = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(automatic ? 1 : 0);
            dest.writeInt(progress);
            dest.writeInt(oldAutomatic ? 1 : 0);
            dest.writeInt(oldProgress);
            dest.writeInt(curBrightness);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}

