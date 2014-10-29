package com.diamondsoftware.android.commuterhelpertrial;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class LocationServiceModern extends LocationService  implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,
com.google.android.gms.location.LocationListener{
	private static final String ACTION_ETA="actioneta";
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
			
    		new Logger(
    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
    				"beginLocationListening", this)
    				.log("additionalInfo: "+(additionalInfo==null?"null":additionalInfo), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
			
			
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
    		new Logger(
    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
    				"disarmLocationManagement 1", this)
    				.log("additionalInfo: "+(additionalInfo==null?"null":additionalInfo), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

			
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
		            
		    		new Logger(
		    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
		    				"disarmLocationManagement 2", this)
		    				.log("mLocationClient.disconnect()", GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
					

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
			
    		new Logger(
    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
    				"onLocationChanged", this)
    				.log("Latitude: "+String.valueOf(latitude) + " Longitude: "+ String.valueOf(longitude)+ " dx: " + String.valueOf(dx), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

			
			if(dx<=distance) {
				notifyUser();
			} else {
				String notificationTimeTillArrival="";
				if(location.hasSpeed() || true /*I don't know why we no longer get speed*/) {
					float speedNic=location.getSpeed();
					double effectiveSpeedMPHNic=((speedNic)*(double)3600)/1609.34;
		    		new Logger(
		    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
		    				"onLocationChanged", this)
		    				.log("Effective Speed: "+String.valueOf(effectiveSpeedMPHNic), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
					if(effectiveSpeedMPHNic>5 || true /*ditto*/) {
						if(mSettingsManager.getEffectiveLocation()==null) {
							mSettingsManager.setEffectiveLocation(location.getLatitude(), location.getLongitude());
							mSettingsManager.setEffectiveDatetiem(new Date());
							mSettingsManager.setJustPreviousLocation(location.getLatitude(), location.getLongitude());
						}
						LatLng latLng = mSettingsManager.getEffectiveLocation();
						if(latLng!=null) {
				    		new Logger(
				    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
				    				"onLocationChanged", this)
				    				.log("LatLng: "+latLng.toString(), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
							Location effectiveLocation = new Location(getProvider());
							effectiveLocation.setLatitude(latLng.latitude);
							effectiveLocation.setLongitude(latLng.longitude);	
							float dxOriginal=effectiveLocation.distanceTo(location2);
							if(dxOriginal>=dx) {
					    		new Logger(
					    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
					    				"onLocationChanged", this)
					    				.log("dxOriginal: "+String.valueOf(dxOriginal)+" dx: "+String.valueOf(dx), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
								LatLng justPreviousLatLng=mSettingsManager.getJustPreviousLocation();
								mSettingsManager.setJustPreviousLocation(location.getLatitude(), location.getLongitude());
								if(justPreviousLatLng!=null) {
						    		new Logger(
						    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
						    				"onLocationChanged", this)
						    				.log("jusPreviousLatLng: "+justPreviousLatLng.toString(), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

									Location justPreviousLocation=new Location(getProvider());
									justPreviousLocation.setLatitude(justPreviousLatLng.latitude);
									justPreviousLocation.setLongitude(justPreviousLatLng.longitude);
									float justPreviousDx=justPreviousLocation.distanceTo(location2);
									if(justPreviousDx>=dx) { // we're moving closer
							    		new Logger(
							    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
							    				"onLocationChanged", this)
							    				.log("justPreviousDx: "+String.valueOf(justPreviousDx), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
										float effectiveDistance=dxOriginal-dx;
										Date effectiveDate=mSettingsManager.getEffectiveDatetime();
										long nbrOfSecondsSinceStart=GlobalStaticValues.getDateDiff(effectiveDate, new Date(), TimeUnit.SECONDS);
							    		new Logger(
							    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
							    				"onLocationChanged", this)
							    				.log("nbrOfSecondsSinceStart: "+String.valueOf(nbrOfSecondsSinceStart), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

										if(nbrOfSecondsSinceStart>0) {
											float effectiveSpeedMetersPerSecond=effectiveDistance/nbrOfSecondsSinceStart;
								    		new Logger(
								    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
								    				"onLocationChanged", this)
								    				.log("effectiveSpeedMetersPerSecond: "+String.valueOf(effectiveSpeedMetersPerSecond), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

											if(effectiveSpeedMetersPerSecond>0) {
												double effectiveSpeedMilesPerHour=(((double)effectiveDistance/(double)effectiveSpeedMetersPerSecond)*(double)3600)/1609.34;
									    		new Logger(
									    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
									    				"onLocationChanged", this)
									    				.log("effectiveSpeedMilesPerHour: "+String.valueOf(effectiveSpeedMilesPerHour), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
												if(effectiveSpeedMilesPerHour>5) { // otherwise, we're angling
													float secondsLeft=dx/effectiveSpeedMetersPerSecond;	
													int secondsLeftInt=(int)secondsLeft;
													float minutesLeft=secondsLeft/60;
													int minutesLeftWholeNumber=(int)minutesLeft;
													int secondsLeftWholeNumber=secondsLeftInt % 60;
													notificationTimeTillArrival="ETA: "+ String.valueOf(minutesLeftWholeNumber) +
															" m " + String.valueOf(secondsLeftWholeNumber) + "s ";
												} else {
													mSettingsManager.setEffectiveLocation(0, 0);
												}
											}
										}
									} else {
										mSettingsManager.setEffectiveLocation(0, 0);
									}
								}
							} else {
								mSettingsManager.setEffectiveLocation(0, 0);
							}
						}
					}
				}
				if(!notificationTimeTillArrival.equals("")) {
		    		new Logger(
		    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
		    				"onLocationChanged", this)
		    				.log("notificationTimeTillArrival: "+notificationTimeTillArrival, GlobalStaticValues.LOG_LEVEL_NOTIFICATION);

			        Intent broadcastIntent = new Intent();
			        broadcastIntent.setAction(ACTION_ETA)
			        .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
			        .putExtra("eta", notificationTimeTillArrival);
			        // Broadcast whichever result occurred
			        LocalBroadcastManager.getInstance(LocationServiceModern.this).sendBroadcast(broadcastIntent);			
				}
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
