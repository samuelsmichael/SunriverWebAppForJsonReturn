package com.diamondsoftware.android.commuterhelpertrial;



import com.diamondsoftware.android.commuterhelpertrial.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public abstract class LocationService extends Service  {
	protected abstract void disarmLocationManagement(String additionalInfo);
	protected abstract void initializeLocationManager();
	protected abstract void beginLocationListening(String additionalInfo);

    private NotificationManager mNotificationManager=null;
    private static final int ARMED_NOTIFICATION_ID=3;
    private String mAddressInReadableForm;
    private SharedPreferences settings = null;
    protected SettingsManager mSettingsManager;
	
	public LocationService() {
	}
	public String getPREFS_NAME() {
		return getPackageName() + "_preferences";
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		settings = getSharedPreferences(getPREFS_NAME(), MODE_PRIVATE);		
		mSettingsManager=new SettingsManager(this);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void doOnStartStuff(Intent intent, int startId) {
		Thread
		.setDefaultUncaughtExceptionHandler(new CustomExceptionHandlerTimer(this));
		String action=null;
		if(intent!=null) {
			action=intent.getAction();
		}
		if(intent!= null && action!=null && intent.getAction().equals("notifyuser")) {
			try {
				notifyUser();
			} catch (Exception e) {}
		} else {
			if(intent!= null && action!=null && intent.getAction().equals("JustDisarm")) {
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
				
				new Logger(
					Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
					"LocationService.JustDisarm", this)
					.log("Doing JustDisarm", GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

				disarmLocationManagement(null);
		    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);	
			} else {
				if(intent!=null  && action!=null && action=="JustInitializeLocationManager") {
					initializeLocationManager();
				} else {				
					if(intent!=null  && action!=null && action=="JustArm") {
						beginLocationListening(null);
					} else {				
						if(intent!=null && action!=null && action.equals(GlobalStaticValues.ACTION_START_ACTIVITY_RECOGNITION)) {
							beginLocationListening("CameFromActivityRecognition");
						} else {
							if(intent!=null&&action!=null && action.equals(GlobalStaticValues.ACTION_STOP_ACTIVITY_RECOGNITION)) {
								disarmLocationManagement("CameFromActivityRecognition");
							} else {
								if (intent!=null) {
									final String locationAddress=intent.getStringExtra("LocationAddress");
									new Thread(new Runnable(){
										public void run() {
											mAddressInReadableForm=locationAddress;
									    	Notification.Builder mBuilder=new Notification.Builder(LocationService.this)
										    	.setSmallIcon(R.drawable.ic_launcher_new)
										    	.setContentTitle("Commuter Alert is on")
										    	.setContentText(mAddressInReadableForm)
										    	.setOngoing(true);
									    	// Creates an explicit intent for an Activity in your app
									    	Intent resultIntent = new Intent(LocationService.this,Home2.class);
											PendingIntent pendingIntent = PendingIntent.getActivity(LocationService.this,
													(int)System.currentTimeMillis(), resultIntent, 0);
									    	mBuilder.setContentIntent(pendingIntent);    	    	
									    	getNotificationManager().notify(ARMED_NOTIFICATION_ID, mBuilder.getNotification());
											// Start up the timer
											beginLocationListening(null);								
										}
									}).run();			
								}
							}
						}
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
		disarmLocationManagement(null);
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
    	
		// 1. Remove the "ongoing" item in the notifications bar 
    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);	
    	// 2. Stop the timer
		disarmLocationManagement(null);
		
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
