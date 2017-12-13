/*
 * Copyright (C) 2010 The Android Open Source Project
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

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.internal.view.RotationPolicy;
import com.android.settings.DreamSettings;
import com.android.settings.Utils;
import com.witsi.debug.FyLog;
import com.witsi.settings.SettingsPreferenceFragment;
import com.witsi.views.MyAlertDialog;
import com.wtisi.settings.R;

import android.os.PowerManager;
import android.view.DisplayManagerAw;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.DispList;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.WindowManager;
//import softwinner.os.DispInfo;

public class DisplaySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    private static final int FALLBACK_DISPLAY_MODE_TIMEOUT = 10;
    private static final String DISPLAY_MODE_AUTO_KEY = "display_mode_auto";

//add by Lynx 20130402    
//private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
	

    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_WALLPAPER = "wallpaper";
    private static final String KEY_SCREEN_SAVER = "screensaver";
	private static final String KEY_ACCELEROMETER_COORDINATE = "accelerometer_coornadite";
	private static final String KEY_SMART_BRIGHTNESS = "smart_brightness";
	private static final String KEY_SMART_BRIGHTNESS_PREVIEW = "key_smart_brightness_preview";
    private static final String KEY_WIFI_DISPLAY = "wifi_display";
	private static final String KEY_SCREEN_ADAPTION = "screen_adaption";
	private static final String KEY_SCREEN_ADAPTION_SETTING = "screen_adaption_setting";
    private static final String KEY_TV_OUTPUT_MODE = "display_output_mode";
    private static final String KEY_TVBRIGHTNESS = "tvbrightness";
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final String KEY_CONTRAST = "constrat";
    private static final String KEY_SATURATION = "saturation";
    private static final String KEY_TV_SCREEN_TRIMMING = "tv_screen_trimming";
    
    private static final String KEY_SOUND_EFFECTS = "sound_effects";
    private static final String KEY_LOCK_SOUNDS = "lock_sounds";
    
    private static final int DLG_GLOBAL_CHANGE_WARNING = 1;
    private static final int DLG_GLOBAL_SCREEN_TIMEOUT = 2;

    private DisplayManager mDisplayManager;

    private WarnedListPreference mFontSizePref;
	/*显示输出模式*/
	private ListPreference mOutputMode;
	private ArrayList<DispList.DispFormat> mOutputModeItems;
    private final Configuration mCurConfig = new Configuration();
    
    private ListPreference mScreenTimeoutPreference;
	private ListPreference mAccelerometerCoordinate;

	private CheckBoxPreference mSmartBrightness;
	private CheckBoxPreference mSmartBrightnessPreview;
	private CheckBoxPreference mSoundEffects;
	private CheckBoxPreference mLockSounds;
	
    private WifiDisplayStatus mWifiDisplayStatus;
    private Preference mWifiDisplayPreference;
    private AudioManager mAudioManager;
	
	private Preference mScreenAdaption;

//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		View view = inflater.inflate(R.layout.list_preference, container, false);
//    	return view;
//	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.display_settings);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mScreenTimeoutPreference = (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        final long currentTimeout = Settings.System.getLong(resolver, SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(mScreenTimeoutPreference);
        updateTimeoutPreferenceDescription(currentTimeout);

        //字体大小
        mFontSizePref = (WarnedListPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mFontSizePref.setOnPreferenceClickListener(this);
        
        //屏幕自适应
		mScreenAdaption = (Preference)findPreference(KEY_SCREEN_ADAPTION);
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		android.view.Display display = wm.getDefaultDisplay();
		int width     = display.getWidth();
		int height    = display.getHeight();
		Log.d(TAG,"rate1 = " + (width * 3.0f / (height * 5.0f)) +
		 	" rate2 = " + (width * 5.0f / (height * 3.0f)));
		if(((width * 3.0f / (height * 5.0f) == 1.0f) ||
		 	(width * 5.0f / (height * 3.0f) == 1.0f)) && mScreenAdaption!=null)
		{            
			getPreferenceScreen().removePreference(mScreenAdaption) ;
		}
		
		/** 触摸提示音 */
		mSoundEffects = (CheckBoxPreference) findPreference(KEY_SOUND_EFFECTS);
        mSoundEffects.setPersistent(false);
        mSoundEffects.setChecked(Settings.System.getInt(resolver,
                Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0);
        /** 锁屏提示音 */
        mLockSounds = (CheckBoxPreference) findPreference(KEY_LOCK_SOUNDS);
        //add by Lynx 20130416 ###delete lock_sounds
        mLockSounds.setPersistent(false);
        mLockSounds.setChecked(Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) != 0);

        //屏蔽显示
        removePreference(KEY_WIFI_DISPLAY);
        removePreference(KEY_NOTIFICATION_PULSE);
        removePreference(KEY_WALLPAPER);
        removePreference(KEY_CONTRAST);
        removePreference(KEY_SATURATION);
        removePreference(KEY_SCREEN_ADAPTION);
        removePreference(KEY_SCREEN_ADAPTION_SETTING);
    }

    
    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
            new RotationPolicy.RotationPolicyListener() {
        @Override
        public void onChange() {
            updateAccelerometerRotationCheckbox();
        }
    };
    
    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        String summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                summary = preference.getContext().getString(R.string.screen_timeout_summary,
                        entries[best]);
            }
        }
        preference.setSummary(summary);
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            screenTimeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            final int userPreference = Integer.parseInt(screenTimeoutPreference.getValue());
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference.setValue(String.valueOf(userPreference));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }
    
    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        pref.setValueIndex(index);

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
                fontSizeNames[index]));
    }
    
    @Override
    public void onResume() {
        super.onResume();

//        RotationPolicy.registerRotationPolicyListener(getActivity(),
//                mRotationPolicyListener);
//
//        if (mWifiDisplayPreference != null) {
//            getActivity().registerReceiver(mReceiver, new IntentFilter(
//                    DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED));
//            mWifiDisplayStatus = mDisplayManager.getWifiDisplayStatus();
//        }
//
//        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();

        RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                mRotationPolicyListener);

        if (mWifiDisplayPreference != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
    	FyLog.e(TAG, "onCreateDialog()");
        if (dialogId == DLG_GLOBAL_CHANGE_WARNING) {
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title,
                    new Runnable() {
                        public void run() {
                            mFontSizePref.click();
                        }
                    });
        }else 
    	if(dialogId == DLG_GLOBAL_SCREEN_TIMEOUT){
        	return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_screen_timeout_change_title,
                    new Runnable() {
                        public void run() {
                        }
                    });
        }
        return null;
    }

    
    private void updateState() {
        updateAccelerometerRotationCheckbox();
        readFontSizePreference(mFontSizePref);
        updateScreenSaverSummary();
        updateWifiDisplaySummary();
		if(mAccelerometerCoordinate != null)
		{            
			updateAccelerometerCoordinateSummary(mAccelerometerCoordinate.getValue());
		}
    }

    private void updateScreenSaverSummary() {
    }

    private void updateWifiDisplaySummary() {
        if (mWifiDisplayPreference != null) {
            switch (mWifiDisplayStatus.getFeatureState()) {
                case WifiDisplayStatus.FEATURE_STATE_OFF:
                    mWifiDisplayPreference.setSummary(R.string.wifi_display_summary_off);
                    break;
                case WifiDisplayStatus.FEATURE_STATE_ON:
                    mWifiDisplayPreference.setSummary(R.string.wifi_display_summary_on);
                    break;
                case WifiDisplayStatus.FEATURE_STATE_DISABLED:
                default:
                    mWifiDisplayPreference.setSummary(R.string.wifi_display_summary_disabled);
                    break;
            }
        }
    }

    private void updateAccelerometerRotationCheckbox() {
        if (getActivity() == null) return;
	//add by Lynx 20130416 ###delete auto rotate screen
        //mAccelerometer.setChecked(!RotationPolicy.isRotationLocked(getActivity()));
    }


	private void updateAccelerometerCoordinateSummary(Object value)
	{               
		CharSequence[] summaries = getResources().getTextArray(R.array.accelerometer_summaries);
		CharSequence[] values = mAccelerometerCoordinate.getEntryValues();
		for (int i=0; i<values.length; i++) 
		{            
			if (values[i].equals(value)) 
			{                
				mAccelerometerCoordinate.setSummary(summaries[i]);                
				break;            
			}        
		}    
	}

    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

    	if (preference == mSoundEffects) {
            if (mSoundEffects.isChecked()) {
                mAudioManager.loadSoundEffects();
            } else {
                mAudioManager.unloadSoundEffects();
            }
            Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
                    mSoundEffects.isChecked() ? 1 : 0);
        } else if (preference == mLockSounds) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_SOUNDS_ENABLED,
                    mLockSounds.isChecked() ? 1 : 0);
        } 
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
 
//add by Lynx+ 20130402
       if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                updateTimeoutPreferenceDescription(value);
				
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }

//add by Lynx- 20130402

        if (KEY_FONT_SIZE.equals(key)) {
            writeFontSizePreference(objValue);
        }

		if (KEY_ACCELEROMETER_COORDINATE.equals(key))
		{            
			String value = String.valueOf(objValue);
			try 
			{ 
				Settings.System.putString(getContentResolver(),
					Settings.System.ACCELEROMETER_COORDINATE, value);
				
				updateAccelerometerCoordinateSummary(objValue);
			}
			catch (NumberFormatException e) 
			{                
				Log.e(TAG, "could not persist key accelerometer coordinate setting", e); 
			}        
		}

		
		if (KEY_SMART_BRIGHTNESS.equals(key))
		{            
			int value = (Boolean)objValue == true ? 1 : 0;
			Settings.System.putInt(getContentResolver(),
				Settings.System.SMART_BRIGHTNESS_ENABLE, value);
			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
//			pm.setWiseBacklightMode(value);
			if((Boolean)objValue)
			{                
				getPreferenceScreen().addPreference(mSmartBrightnessPreview);
			}
			else
			{            	
				getPreferenceScreen().removePreference(mSmartBrightnessPreview);
				mSmartBrightnessPreview.setChecked(false);            
			}        
		}

        return true;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
                mWifiDisplayStatus = (WifiDisplayStatus)intent.getParcelableExtra(
                        DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
                updateWifiDisplaySummary();
            }
        }
    };

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mFontSizePref) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING);
                return false;
            } else {
                mFontSizePref.click();
            }
        }else if(preference == mScreenTimeoutPreference){
        	showDialog(DLG_GLOBAL_CHANGE_WARNING);
        	return false;
        }
        return false;
    }
    
    private void setOutputMode(ListPreference preference){       
        ArrayList<DispList.DispFormat> items = DispList.getDispList();
        mOutputModeItems = items;
        HashMap<DispList.DispFormat,Integer> strMap = DispList.getItemStringIdList();
        String databaseValue = Settings.System.getString(getContentResolver(), 
                Settings.System.DISPLY_OUTPUT_FORMAT);
        int autotag = 0;
	int size = items.size() + autotag;
        CharSequence[]  entries = new CharSequence[size];
        CharSequence[]  entryValues = new CharSequence[size];
		for (int i = 0; i < size; i++) {
			if (i == 0 && autotag == 1) {
				entries[i] = getResources().getText(
						R.string.display_mode_auto_detect);
				entryValues[i] = String.valueOf(-1);
				continue;
			}
			entries[i] = getResources().getString(strMap.get(items.get(i - autotag)));
			entryValues[i] = String.valueOf(i - autotag);
			if (DispList.ItemName2Code(databaseValue).equals(items.get(i - autotag))) {
				preference.setValue(String.valueOf(i - autotag));
			}
		}
        
        preference.setEntries(entries);
        preference.setEntryValues(entryValues);
        int autoDetect = Settings.System.getInt(getContentResolver(), DISPLAY_MODE_AUTO_KEY,0);
        if(autoDetect == 1){
        	preference.setValue(String.valueOf(-1));
        }
    }
	private void switchDispMode(DispList.DispFormat item) {
		DisplayManagerAw displayManager = (DisplayManagerAw) getSystemService(Context.DISPLAY_SERVICE_AW);
		if (item == null) {
			return;
		}
		if (displayManager.getDisplayOutputType(0) != item.mOutputType
				|| displayManager.getDisplayOutputFormat(0) != item.mFormat) {
			// displayManager.setDisplayOutputType(0, item.mOutputType,
			// item.mFormat);
			displayManager.setDisplayParameter(0, item.mOutputType,
					item.mFormat);
			displayManager
					.setDisplayMode(DisplayManagerAw.DISPLAY_MODE_SINGLE_FB_GPU);
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audioManager == null) {
				Log.w(TAG, "audioManager is null");
				return;
			}
			ArrayList<String> audioOutputChannels = audioManager
					.getActiveAudioDevices(AudioManager.AUDIO_OUTPUT_ACTIVE);
			if (item.mOutputType == DisplayManagerAw.DISPLAY_OUTPUT_TYPE_HDMI
					&& !audioOutputChannels.contains(AudioManager.AUDIO_NAME_HDMI)) {
				audioOutputChannels.clear();
				audioOutputChannels.add(AudioManager.AUDIO_NAME_HDMI);
				audioManager.setAudioDeviceActive(audioOutputChannels,AudioManager.AUDIO_OUTPUT_ACTIVE);
			} else if ((item.mOutputType == DisplayManagerAw.DISPLAY_OUTPUT_TYPE_TV || item.mOutputType == DisplayManagerAw.DISPLAY_OUTPUT_TYPE_VGA)
					&& !audioOutputChannels.contains(AudioManager.AUDIO_NAME_CODEC)) {
				audioOutputChannels.clear();
				audioOutputChannels.add(AudioManager.AUDIO_NAME_CODEC);
				audioManager.setAudioDeviceActive(audioOutputChannels,AudioManager.AUDIO_OUTPUT_ACTIVE);
			}
		}
	}
}
