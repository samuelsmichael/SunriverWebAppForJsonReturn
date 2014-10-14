package com.diamondsoftware.android.commuterhelpertrial;

import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingsManager {
	private SharedPreferences mSharedPreferences;
	private Context mContext;
	
	public SettingsManager(Context context) {
		mSharedPreferences=context.getSharedPreferences(context.getPackageName() + "_preferences", Activity.MODE_PRIVATE);
		mContext=context;
	}
	private String getValue(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}
	private void setValue(String key, String value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(key,value);
		editor.commit();				
	}
	public void setHelpOverlayStateOn(boolean value) {
		Editor editor=mSharedPreferences.edit();
		editor.putString(GlobalStaticValues.KEY_HELP_OVERLAY_STATE, value?"true":"false");
		editor.commit();
	}
	public boolean getHelpOverlayStateOn() {
		String value= getValue(GlobalStaticValues.KEY_HELP_OVERLAY_STATE,"false");
		return value.equals("true")?true:false;
	}
	public void setContinousAlarmOn(boolean value) {
		Editor editor=mSharedPreferences.edit();
		editor.putBoolean(GlobalStaticValues.KEY_CONTINUOUS_ALARM_STATE, value);
		editor.commit();
	}
	public boolean getContinuousAlarmOn() {
		return mSharedPreferences.getBoolean(GlobalStaticValues.KEY_CONTINUOUS_ALARM_STATE, false);
	}
}
