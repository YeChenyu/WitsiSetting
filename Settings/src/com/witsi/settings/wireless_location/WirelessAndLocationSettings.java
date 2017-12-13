/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.witsi.settings.wireless_location;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.admin.DevicePolicyManager;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;

import java.util.Observable;
import java.util.Observer;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.AirplaneModeEnabler;
import com.android.settings.Utils;
import com.witsi.debug.FyLog;
import com.witsi.settings.SettingsPreferenceFragment;
import com.wtisi.settings.R;

public class WirelessAndLocationSettings extends SettingsPreferenceFragment 
	implements Preference.OnPreferenceChangeListener{

    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_SMDT_3G_SETTINGS = "smdt_3g_settings";
    private static final String KEY_ETHERNET_SETTINGS = "ethernet_settings";
    private static final String KEY_PPOE_SETTINGS = "pppoe_settings";
    private static final String KEY_WIMAX_SETTINGS = "wimax_settings";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private static final String KEY_PROXY_SETTINGS = "proxy_settings";
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    private static final String KEY_CELL_BROADCAST_SETTINGS = "cell_broadcast_settings";

//    Location Settings
    private static final String KEY_LOCATION_TOGGLE = "location_toggle";
    private static final String KEY_LOCATION_NETWORK = "location_network";
    private static final String KEY_LOCATION_GPS = "location_gps";
    private static final String KEY_ASSISTED_GPS = "assisted_gps";
    
    
    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;

    private AirplaneModeEnabler mAirplaneModeEnabler;
    private CheckBoxPreference mAirplaneModePreference;
    
    // Location Settings
    private CheckBoxPreference mNetwork;
    private CheckBoxPreference mGps;
    private CheckBoxPreference mAssistedGps;
    private SwitchPreference mLocationAccess;

    // These provide support for receiving notification when Location Manager settings change.
    // This is necessary because the Network Location Provider can change settings
    // if the user does not confirm enabling the provider.
    private ContentQueryMap mContentQueryMap;

    private Observer mSettingsObserver;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wireless_settings);

        final boolean isSecondaryUser = UserHandle.myUserId() != UserHandle.USER_OWNER;

        final Activity activity = getActivity();
        mAirplaneModePreference = (CheckBoxPreference) findPreference(KEY_TOGGLE_AIRPLANE);
        mAirplaneModeEnabler = new AirplaneModeEnabler(activity, mAirplaneModePreference);
        String toggleable = Settings.Global.getString(activity.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        
        //enable/disable wimax depending on the value in config.xml
        boolean isWimaxEnabled = !isSecondaryUser && this.getResources().getBoolean(
                com.android.internal.R.bool.config_wimaxEnabled);
        if (!isWimaxEnabled) {
            PreferenceScreen root = getPreferenceScreen();
            Preference ps = (Preference) findPreference(KEY_WIMAX_SETTINGS);
            if (ps != null) root.removePreference(ps);
        } else {
            if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIMAX )
                    && isWimaxEnabled) {
                Preference ps = (Preference) findPreference(KEY_WIMAX_SETTINGS);
                ps.setDependency(KEY_TOGGLE_AIRPLANE);
            }
        }
        // Manually set dependencies for Wifi when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_WIFI)) {
            findPreference(KEY_VPN_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
        }
        if (isSecondaryUser) { // Disable VPN
            removePreference(KEY_VPN_SETTINGS);
        }
        // Manually set dependencies for Bluetooth when not toggleable.
        if (toggleable == null || !toggleable.contains(Settings.Global.RADIO_BLUETOOTH)) {
            // No bluetooth-dependent items in the list. Code kept in case one is added later.
        }
        // Remove Mobile Network Settings if it's a wifi-only device.
        if (isSecondaryUser || Utils.isWifiOnly(getActivity())) {
            removePreference(KEY_MOBILE_NETWORK_SETTINGS);
        }

        // Enable Proxy selector settings if allowed.
        Preference mGlobalProxy = findPreference(KEY_PROXY_SETTINGS);
        DevicePolicyManager mDPM = (DevicePolicyManager)
                activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // proxy UI disabled until we have better app support
        getPreferenceScreen().removePreference(mGlobalProxy);
        mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() == null);

        // Disable Tethering if it's not allowed or if it's a wifi-only device
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (isSecondaryUser || !cm.isTetheringSupported()) {
            getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
        } else {
            Preference p = findPreference(KEY_TETHER_SETTINGS);
            p.setTitle(Utils.getTetheringLabel(cm));
        }

        // Enable link to CMAS app settings depending on the value in config.xml.
        boolean isCellBroadcastAppLinkEnabled = this.getResources().getBoolean(
                com.android.internal.R.bool.config_cellBroadcastAppLinks);
        try {
            if (isCellBroadcastAppLinkEnabled) {
                PackageManager pm = getPackageManager();
                if (pm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver")
                        == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    isCellBroadcastAppLinkEnabled = false;  // CMAS app disabled
                }
            }
        } catch (IllegalArgumentException ignored) {
            isCellBroadcastAppLinkEnabled = false;  // CMAS app not installed
        }
        if (isSecondaryUser || !isCellBroadcastAppLinkEnabled) {
            PreferenceScreen root = getPreferenceScreen();
            Preference ps = findPreference(KEY_CELL_BROADCAST_SETTINGS);
            if (ps != null) root.removePreference(ps);
        }
//        Location
        mLocationAccess = (SwitchPreference) findPreference(KEY_LOCATION_TOGGLE);
        mNetwork = (CheckBoxPreference) findPreference(KEY_LOCATION_NETWORK);
        mGps = (CheckBoxPreference) findPreference(KEY_LOCATION_GPS);
        mAssistedGps = (CheckBoxPreference) findPreference(KEY_ASSISTED_GPS);

        mLocationAccess.setOnPreferenceChangeListener(this);
     // depend on others...
        updateLocationToggles();
        
//        屏蔽显示
        removePreference(KEY_SMDT_3G_SETTINGS);
        removePreference(KEY_ETHERNET_SETTINGS);
        removePreference(KEY_PPOE_SETTINGS);
        removePreference(KEY_VPN_SETTINGS);
        
        removePreference(KEY_ASSISTED_GPS);
        removePreference(KEY_LOCATION_GPS);
    }

    @Override
    public void onStart() {
        super.onStart();
        // listen for Location Manager settings changes
        Cursor settingsCursor = getContentResolver().query(Settings.Secure.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?)",
                new String[]{Settings.Secure.LOCATION_PROVIDERS_ALLOWED},
                null);
        mContentQueryMap = new ContentQueryMap(settingsCursor, Settings.System.NAME, true, null);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mAirplaneModeEnabler.resume();
        
        if (mSettingsObserver == null) {
            mSettingsObserver = new Observer() {
                public void update(Observable o, Object arg) {
                    updateLocationToggles();
                }
            };
        }

        mContentQueryMap.addObserver(mSettingsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();

        mAirplaneModeEnabler.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSettingsObserver != null) {
            mContentQueryMap.deleteObserver(mSettingsObserver);
        }
    }
    
    /*
     * Creates toggles for each available location provider
     */
    private void updateLocationToggles() {
        ContentResolver res = getContentResolver();
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                res, LocationManager.GPS_PROVIDER);
        boolean networkEnabled = Settings.Secure.isLocationProviderEnabled(
                res, LocationManager.NETWORK_PROVIDER);
        //add by Lynx 20130416 ### delete location_gps
	//mGps.setChecked(gpsEnabled);
        mNetwork.setChecked(networkEnabled);
        mLocationAccess.setChecked(gpsEnabled || networkEnabled);
        if (mAssistedGps != null) {
            mAssistedGps.setChecked(Settings.Global.getInt(res,
                    Settings.Global.ASSISTED_GPS_ENABLED, 2) == 1);
            mAssistedGps.setEnabled(gpsEnabled);
        }
    }
    /**
     * Invoked on each preference click in this hierarchy, overrides
     * PreferenceActivity's implementation.  Used to make sure we track the
     * preference click events.
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    	final ContentResolver cr = getContentResolver();
        if (preference == mAirplaneModePreference) {
            // Airplane mode
        	boolean airplane = getAirplaneMode(cr);
			FyLog.d(getTag(), "Airplane mode="+ airplane);
			setAirplaneMode(!airplane);
			findPreference(KEY_MOBILE_NETWORK_SETTINGS).setEnabled(!findPreference(KEY_MOBILE_NETWORK_SETTINGS).isEnabled());
            return true;
        }else 
        if (preference == mNetwork) {
            Settings.Secure.setLocationProviderEnabled(cr,
                    LocationManager.NETWORK_PROVIDER, mNetwork.isChecked());
        } else if (preference == mGps) {
            boolean enabled = mGps.isChecked();
            Settings.Secure.setLocationProviderEnabled(cr,
                    LocationManager.GPS_PROVIDER, enabled);
            if (mAssistedGps != null) {
                mAssistedGps.setEnabled(enabled);
            }
        } else if (preference == mAssistedGps) {
            Settings.Global.putInt(cr, Settings.Global.ASSISTED_GPS_ENABLED,
                    mAssistedGps.isChecked() ? 1 : 0);
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        // Let the intents be launched by the Preference manager
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    
    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (pref.getKey().equals(KEY_LOCATION_TOGGLE)) {
            onToggleLocationAccess((Boolean) newValue);
        }
        return true;
    }
    
    /** Enable or disable all providers when the master toggle is changed. */
    private void onToggleLocationAccess(boolean checked) {
        final ContentResolver cr = getContentResolver();
        Settings.Secure.setLocationProviderEnabled(cr,
                LocationManager.GPS_PROVIDER, checked);
        Settings.Secure.setLocationProviderEnabled(cr,
                LocationManager.NETWORK_PROVIDER, checked);
        updateLocationToggles();
    }
    
    public static boolean isRadioAllowed(Context context, String type) {
        if (!AirplaneModeEnabler.isAirplaneModeOn(context)) {
            return true;
        }
        // Here we use the same logic in onCreate().
        String toggleable = Settings.Global.getString(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    }
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
            // Set Airplane mode based on the return value and checkbox state
            mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
                    mAirplaneModePreference.isChecked());
        }
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_more_networks;
    }
    
    /**
	 * 判断手机是否是飞行模式
	 * @param context
	 * @return
	 */
	public boolean getAirplaneMode(ContentResolver cr){
		int isAirplaneMode = Settings.Global.getInt(cr,
                           Settings.Global.AIRPLANE_MODE_ON, 0) ;
		return (isAirplaneMode == 1) ? true : false;
	}
	
	/**
	 * 设置手机飞行模式
	 * @param context
	 * @param enabling true:设置为飞行模式	false:取消飞行模式
	 */
	@SuppressLint("NewApi")
	protected void setAirplaneMode(boolean setAirPlane) {
		Settings.Global.putInt(getContentResolver(),
		Settings.Global.AIRPLANE_MODE_ON, setAirPlane ? 1 : 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("TestCode", "ellic");
		getActivity().sendBroadcast(intent);
	}
}
