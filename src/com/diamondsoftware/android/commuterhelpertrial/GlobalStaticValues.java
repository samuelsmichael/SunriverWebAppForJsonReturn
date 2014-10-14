package com.diamondsoftware.android.commuterhelpertrial;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;


public class GlobalStaticValues {

	public static final int LOG_LEVEL_INFORMATION=0;
	public static final int LOG_LEVEL_NOTIFICATION=1;
	public static final int LOG_LEVEL_CRITICAL=2;
	public static final int LOG_LEVEL_FATAL=3;

	public static final String ACTION_ACTIVITY_RECOGNITION_CHANGE_ALERT="ACTIONactivityrecognitionchangealert";
	public static final String ACTION_START_ACTIVITY_RECOGNITION="ACTIONSTARTACTIVITYRECOGNITION";
	public static final String ACTION_STOP_ACTIVITY_RECOGNITION="ACTIONSTOPACTIVITYRECOGNITION";
	public static final String KEY_HELP_OVERLAY_STATE = "HelpOverlayState";
	public static final String KEY_CONTINUOUS_ALARM_STATE="continuousalarm";
	
	public static final String KEY_SpeakableAddress="k_spa";
	
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = 5000;

}
