package com.diamondsoftware.android.commuterhelpertrial;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
	public static final String KEY_EFFECTIVE_DATETIME ="k_e_t";
	public static final String KEY_EFFECTIVE_LOCATION = "k_e_L";
	public static final String KEY_JUSTPREVIOUS_LOCATION = "J_P_Loc";
	
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = 5000;

    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
    
}
