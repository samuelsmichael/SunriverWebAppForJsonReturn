package com.diamondsoftware.android.commuterhelper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class ActivityRecognitionService extends Service implements
ConnectionCallbacks, OnConnectionFailedListener {
    public enum REQUEST_TYPE {START, STOP,CLEANUP}
    private REQUEST_TYPE mRequestType;
    SharedPreferences settings;
    boolean mThenStart;
    /*
     * Store the PendingIntent used to send activity recognition event
     * back to the app
     */
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
	
	private boolean mAmReceiving=false;
	@Override
	public void onDestroy() {		
		stop();
		super.onDestroy();
	}
    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        mThenStart=false;
        mInProgress=false;
        /*
         * Instantiate a new activity recognition client. Since the
         * parent Activity implements the connection listener and
         * connection failure listener, the constructor uses "this"
         * to specify the values of those parameters.
         */
        mActivityRecognitionClient =
                new ActivityRecognitionClient(this, this, this);
        /*
         * Create the PendingIntent that Location Services uses
         * to send activity recognition updates back to this app.
         */
        Intent intent = new Intent(
                this, ActivityRecognitionIntentService.class)
        	.setAction(GlobalStaticValues.ACTION_ACTIVITY_RECOGNITION_CHANGE_ALERT);
        /*
         * Return a PendingIntent that starts the IntentService.
         */
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(intent!=null) {
			String action=intent.getAction();
			if(action!=null) {
				if(action.equals(GlobalStaticValues.ACTION_START_ACTIVITY_RECOGNITION)) {
					start();
				} else {
					if(action.equals(GlobalStaticValues.ACTION_STOP_ACTIVITY_RECOGNITION)) {
						stop();
					}
				}
			}
		}
		return Service.START_STICKY;
	}
    
	protected void stop() {		
		if(!mInProgress) {
	        mRequestType=REQUEST_TYPE.STOP;
	        // Request a connection to Location Services
	        mActivityRecognitionClient.connect();
	        mInProgress=true;
		} else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
	}

	private void start() {
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            mRequestType=REQUEST_TYPE.START;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
        //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		boolean thenStart=false;
	    /*
	     * Called by Location Services once the location client is connected.
	     *
	     * Continue by requesting activity updates.
	     */
		if(mRequestType==REQUEST_TYPE.START ) {
        /*
         * Request activity recognition updates using the preset
         * detection interval and PendingIntent. This call is
         * synchronous.
         */
			int heartbeatFrequency=30000;
			mActivityRecognitionClient.requestActivityUpdates(
                heartbeatFrequency,
                mActivityRecognitionPendingIntent);
				mAmReceiving=true;
		} else {
			if(mRequestType==REQUEST_TYPE.STOP) {
		        mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
				mAmReceiving=false;
				if(this.mThenStart) {
					thenStart=true;
				}
			} else {
				if(mRequestType==REQUEST_TYPE.CLEANUP) {
					if(this.mAmReceiving) {
						mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
					}
				}
			}
		}
        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
        mInProgress = false;
        mActivityRecognitionClient.disconnect();
        if(thenStart) {
	        start();
        }
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
