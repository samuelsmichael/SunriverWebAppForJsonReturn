package com.diamondsoftware.android.commuterhelpertrial;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;


public class ActivityRecognitionIntentService extends IntentService {
    SharedPreferences settings;
	
	@Override
	public void onCreate() {
		super.onCreate();
        settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
	}
	
	public ActivityRecognitionIntentService(String name) {
		super(name);
	}
	public ActivityRecognitionIntentService() {
		super("wtf");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		int confidence;
		int activityType=DetectedActivity.UNKNOWN;

        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {				
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            confidence = mostProbableActivity.getConfidence();
            /*
             * Get an integer describing the type of activity
             */
            activityType = mostProbableActivity.getType();

            String activityName = getNameFromType(activityType);
            // What is TILTING good for?  
            if(activityType==DetectedActivity.TILTING || activityType==DetectedActivity.UNKNOWN) {
            	return;
            }
            
    		new Logger(
    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
    				"ActivityRecognition", this)
    				.log(activityName, GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

            
            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */
            if(activityType==DetectedActivity.IN_VEHICLE) {
            	Intent intent3=new Intent(this,LocationServiceModern.class)
            		.setAction(GlobalStaticValues.ACTION_START_ACTIVITY_RECOGNITION);
            	startService(intent3);
            } else {
            	Intent intent2=new Intent(this,LocationServiceModern.class)
        		.setAction(GlobalStaticValues.ACTION_STOP_ACTIVITY_RECOGNITION);
        	startService(intent2);
            }
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    }
	
    /**
     * Map detected activity types to strings
     *@param activityType The detected activity type
     *@return A user-readable name for the type
     */
    private static String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }

}
