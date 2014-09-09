package com.diamondsoftware.android.commuterhelpertrial;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class LocationServiceModern extends LocationService  implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
com.google.android.gms.location.LocationListener{
    private LocationClient mLocationClient;
    LocationRequest mLocationRequest;
	private LocationManager mLocationManager = null;
    SharedPreferences settings;
    @Override
    public void onCreate() {
    	super.onCreate();
        settings = getSharedPreferences(getPREFS_NAME(), MODE_PRIVATE);
    }


	@Override
	protected void initializeLocationManager() {
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval
        long updateInterval=Long.valueOf(settings.getString("locationupdatefrequency","15000"));
        mLocationRequest.setInterval(updateInterval);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(1000);
	}

	private int mDontReenter=0;
	@Override
	protected void beginLocationListening(String additionalInfo) {
		if(mDontReenter==0) {
			mDontReenter=1;
			if(this.mLocationClient==null || !(mLocationClient.isConnected() || mLocationClient.isConnecting())) {
		        mLocationClient = new LocationClient(this, this, this);
		        mLocationClient.connect();
		        if(additionalInfo==null || !additionalInfo.equalsIgnoreCase("CameFromActivityRecognition")) {
					Intent intent=new Intent(this,ActivityRecognitionService.class)
						.setAction(GlobalStaticValues.ACTION_START_ACTIVITY_RECOGNITION);
					startService(intent);
		        }

			}
			mDontReenter=0;
		}
	}
    int mDontReenter3=0;
	@Override
	protected void disarmLocationManagement(String additionalInfo) {
		if(mDontReenter3==0) {
			mDontReenter3=1;
			if (mLocationClient!=null) {
		        if(additionalInfo==null || !additionalInfo.equalsIgnoreCase("CameFromActivityRecognition")) {
					Intent intent=new Intent(this,ActivityRecognitionService.class)
						.setAction(GlobalStaticValues.ACTION_STOP_ACTIVITY_RECOGNITION);
					startService(intent);
		        }
		        if (mLocationClient.isConnected()) {
		            /*
		             * Remove location updates for a listener.
		             * The current Activity is the listener, so
		             * the argument is "this".
		             */
		        	mLocationClient.removeLocationUpdates(this);
		            mLocationClient.disconnect();	
		        }
			}
			mDontReenter3=0;
		}
	}


	private int mDontReenter2=0;
	@Override
	public void onLocationChanged(Location location) {
		if(mDontReenter2==0) {
			mDontReenter2=1;
			SharedPreferences settings2 = getSharedPreferences(
					getPREFS_NAME(), Context.MODE_PRIVATE);
			float distance = Float.valueOf(settings2.getString(
					"LocationDistance", "503"));
			SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
					MODE_PRIVATE);
			double latitude = Double.valueOf(settings
					.getString("latitude", "0"));
			double longitude = Double.valueOf(settings.getString("longitude",
					"0"));
			Location location2 = new Location(getProvider());
			location2.setLatitude(Double.valueOf(latitude));
			location2.setLongitude(Double.valueOf(longitude));
	

			float dx=location.distanceTo(location2);
			if(dx<=distance) {
				notifyUser();
			}
		}
		mDontReenter2=0;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return getLocationManager().getBestProvider(criteria, false);
	}
	private LocationManager getLocationManager() {
		if (mLocationManager == null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}

}
