package com.sdouglas.android.commuteralert;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

public abstract class LocationService extends Service  {
	protected abstract void disarmLocationManagement();
	protected abstract void initializeLocationManager();
	protected abstract void beginLocationListening();
    private static String ALERT_TEXT="Alert! Alert! You are arriving at your destination.";

	public static final String PREFS_NAME = "com.sdouglas.android.commuteralert_preferences";
    private NotificationManager mNotificationManager=null;
    private static final int ARMED_NOTIFICATION_ID=3;
    private String mAddressInReadableForm;


	public LocationService() {
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Thread
		.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(this));
		if(intent!= null && intent.getAction()!=null && intent.getAction().equals("notifyuser")) {
			notifyUser();
		} else {
			if(intent!= null && intent.getAction()!=null && intent.getAction().equals("JustDisarm")) {
				/*
				 * I only maintain one NotificationManager because there are several scenarios where
				 * it has to be used:
				 * 	1. When an target is set, we want to put an "ongoing" notice in the notification tray
				 *		so no matter where the user is on his phone, he can always see that his 
				 *		system is armed. "Ongoing" means that it doesn't go away when the user 
				 *		presses on it, nor when the user clicks the X to clear all notifications.
				 *  2. When the user clicks the "disarm" button, we need to remove it.  This is 
				 *  	done right here.
				 *  3. When the alarm goes off, we want to first remove the "ongoing" notification,
				 *  	and then set a new one ... only this new one needs to be "not ongoing", because
				 *  	once the user presses it (or presses the X to clear all notifications), it should go away.
				 */
				disarmLocationManagement();
		    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);	
			} else {
				if(intent!=null  && intent.getAction()!=null && intent.getAction()=="JustInitializeLocationManager") {
					initializeLocationManager();
				} else {
					if (intent!=null) {
						mAddressInReadableForm=intent.getStringExtra("LocationAddress");
				    	Notification.Builder mBuilder=new Notification.Builder(this)
					    	.setSmallIcon(R.drawable.ic_launcher)
					    	.setContentTitle("CommuterAlert is on")
					    	.setContentText(mAddressInReadableForm)
					    	.setOngoing(true);
				    	// Creates an explicit intent for an Activity in your app
				    	Intent resultIntent = new Intent(this,Home2.class);
						PendingIntent pendingIntent = PendingIntent.getActivity(this,
								(int)System.currentTimeMillis(), resultIntent, 0);
				    	mBuilder.setContentIntent(pendingIntent);    	    	
				    	getNotificationManager().notify(ARMED_NOTIFICATION_ID, mBuilder.getNotification());
						// Start up the timer
						beginLocationListening();
					}
				}
			}
		}
	}
	@Override
	public void onDestroy() {
		disarmLocationManagement();
	}
	
	private NotificationManager getNotificationManager() {
		if (mNotificationManager==null) {
			mNotificationManager =
	    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mNotificationManager;
	}

	protected void notifyUser() {
		/* This is it!  We've arrived. Time to wake up our sleeping passenger.*/
    	
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String voicetext=settings.getString("voicetext", ALERT_TEXT);
		
		// 1. Remove the "ongoing" item in the notifications bar 
    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);	
    	// 2. Stop the timer
		disarmLocationManagement();
		
		// 3. Clear out the Model's data
		SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("latitude", (float) 0);
        editor.putFloat("longitude", (float) 0);
        editor.putString("locationString","");
        editor.commit();
        
    	// 4. Send the alert
		Intent jdIntent=new Intent(this, VoiceHelper.class)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
			.setAction("doit");
		startActivity(jdIntent);
	}
}
