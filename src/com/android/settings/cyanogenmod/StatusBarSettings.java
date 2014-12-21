/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.settings.cyanogenmod;

import android.os.Bundle;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBar";

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
	private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
 	private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

    private static final String STATUS_BAR_BATTERY_STYLE_HIDDEN = "4";
    private static final String STATUS_BAR_BATTERY_STYLE_TEXT = "6";

    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;
	private SwitchPreference mRecentsClearAll;
	private ListPreference mRecentsClearAllLocation;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.status_bar_settings);

		PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        int batteryStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int batteryShowPercent = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);
        
		mRecentsClearAll = (SwitchPreference) prefSet.findPreference(SHOW_CLEAR_ALL_RECENTS);
		mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
		Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
		mRecentsClearAll.setOnPreferenceChangeListener(this);

		mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
		int location = Settings.System.getIntForUser(resolver,
		Settings.System.RECENTS_CLEAR_ALL_LOCATION, 0, UserHandle.USER_CURRENT);
		mRecentsClearAllLocation.setValue(String.valueOf(location));
		mRecentsClearAllLocation.setOnPreferenceChangeListener(this);
		updateRecentsLocation(location);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);

            enableStatusBarBatteryDependents((String) newValue);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) newValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
        } else if (preference == mRecentsClearAll) {
			boolean show = (Boolean) objValue;
			Settings.System.putIntForUser(getActivity().getContentResolver(),
			Settings.System.SHOW_CLEAR_ALL_RECENTS, show ? 1 : 0, UserHandle.USER_CURRENT);
			return true;
		} else if (preference == mRecentsClearAllLocation) {
			int location = Integer.valueOf((String) objValue);
			Settings.System.putIntForUser(getActivity().getContentResolver(),
			Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
				updateRecentsLocation(location);
			return true;
			}
        return false;
    }

    private void enableStatusBarBatteryDependents(String value) {
        boolean enabled = !(value.equals(STATUS_BAR_BATTERY_STYLE_TEXT)
                || value.equals(STATUS_BAR_BATTERY_STYLE_HIDDEN));
        mStatusBarBatteryShowPercent.setEnabled(enabled);
    }

	private void updateRecentsLocation(int value) {
	ContentResolver resolver = getContentResolver();
		Resources res = getResources();
		int summary = -1;

	Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, value);

	if (value == 0) {
		Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 0);
		summary = R.string.recents_clear_all_location_right;
	} else if (value == 1) {
		Settings.System.putInt(resolver, Settings.System.RECENTS_CLEAR_ALL_LOCATION, 1);
		summary = R.string.recents_clear_all_location_left;
	}
	if (mRecentsClearAllLocation != null && summary != -1) {
		mRecentsClearAllLocation.setSummary(res.getString(summary));
	}
}
