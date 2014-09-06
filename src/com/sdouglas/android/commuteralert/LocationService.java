package com.sdouglas.android.commuteralert;



import com.sdouglas.android.commuteralert.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public abstract class LocationService extends Service  {
	protected abstract void disarmLocationManagement();
	protected abstract void initializeLocationManager();
	protected abstract void beginLocationListening();
    private static String ALERT_TEXT="Alert! Alert! You are arriving at your destination.";

    private NotificationManager mNotificationManager=null;
    private static final int ARMED_NOTIFICATION_ID=3;
    private String mAddressInReadableForm;
	private Logger mLogger=null;

	
	protected Logger getLogger() {
		if(mLogger==null) {
			mLogger=new Logger(0,"LocationServiceOriginalEnhanced",null);
		}
		return mLogger;
	}
	
	public LocationService() {
	}
	public String getPREFS_NAME() {
		return getPackageName() + "_preferences";
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void doOnStartStuff(Intent intent, int startId) {
		Thread
		.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(this));
		if(intent!= null && intent.getAction()!=null && intent.getAction().equals("notifyuser")) {
			try {
				notifyUser();
			} catch (Exception e) {}
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
				getLogger().log("LocationService.onStart..JustDisarm", 99);				

				disarmLocationManagement();
		    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);	
			} else {
				if(intent!=null  && intent.getAction()!=null && intent.getAction()=="JustInitializeLocationManager") {
					initializeLocationManager();
				} else {				
					if (intent!=null) {
						final String locationAddress=intent.getStringExtra("LocationAddress");
						new Thread(new Runnable(){
							public void run() {
								mAddressInReadableForm=locationAddress;
						    	Notification.Builder mBuilder=new Notification.Builder(LocationService.this)
							    	.setSmallIcon(R.drawable.launcher)
							    	.setContentTitle("CommuterAlert is on")
							    	.setContentText(mAddressInReadableForm)
							    	.setOngoing(true);
						    	// Creates an explicit intent for an Activity in your app
						    	Intent resultIntent = new Intent(LocationService.this,Home2.class);
								PendingIntent pendingIntent = PendingIntent.getActivity(LocationService.this,
										(int)System.currentTimeMillis(), resultIntent, 0);
						    	mBuilder.setContentIntent(pendingIntent);    	    	
						    	getNotificationManager().notify(ARMED_NOTIFICATION_ID, mBuilder.getNotification());
								// Start up the timer
								beginLocationListening();								
							}
						}).run();

					}
				}
			}
		}
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		doOnStartStuff(intent,startId);	
		return START_STICKY;
	}		


	@Override
	public void onDestroy() {
		getLogger().log("LocationService.onDestroy", 99);
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
    	
        SharedPreferences settings = getSharedPreferences(getPREFS_NAME(), MODE_PRIVATE);
		
		// 1. Remove the "ongoing" item in the notifications bar 
    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);	
    	// 2. Stop the timer
		disarmLocationManagement();
		
		// 3. Clear out the Model's data
		SharedPreferences.Editor editor = settings.edit();
        editor.putString("latitude", "0");
        editor.putString("longitude", "0");
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
