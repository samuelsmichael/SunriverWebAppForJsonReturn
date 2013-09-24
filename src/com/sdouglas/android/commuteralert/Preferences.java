package com.sdouglas.android.commuteralert;

import com.sdouglas.android.commuteralert.R;

import android.preference.ListPreference;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
	        setTitle(getString(R.string.app_name)+" Preferences");
	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.preferences);
	        
        } catch (Exception eee) {}
    }

}
