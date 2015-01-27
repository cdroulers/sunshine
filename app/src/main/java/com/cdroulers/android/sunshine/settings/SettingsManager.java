package com.cdroulers.android.sunshine.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cdroulers.android.sunshine.R;

public class SettingsManager {
    public static String getLocation(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return preferences
                .getString(
                        activity.getString(R.string.pref_location_key),
                        activity.getString(R.string.pref_location_default));
    }

    public static String getUnits(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return preferences
                .getString(
                        activity.getString(R.string.pref_units_key),
                        activity.getString(R.string.pref_units_default));
    }
}
