package com.sdouglas.android.commuteralert;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

public class LocationServiceOriginalEnhanced extends LocationService {
	private Timer mLocationsTimer2=null;
	private LocationManager mLocationManager=null;
	private Location mLastKnownLocation=null;
	private Boolean mImGettingLocationUpdates;
	private int mDontReenter=0;
	private int mDontReenter2=0;

	@Override
	protected void disarmLocationManagement() {
		stopMyLocationsTimer2();		
	}

	@Override
	protected void initializeLocationManager() {
		mImGettingLocationUpdates=false;
		mDontReenter=0;
		mDontReenter2=0;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("modifyingValue", 1);
		editor.commit();
	}

	@Override
	protected void beginLocationListening() {
		resetmAlarmSender();
	}
	private Timer getLocationsTimer2() {
		if (mLocationsTimer2 == null) {
			mLocationsTimer2 = new Timer("Enhanced");
		}
		return mLocationsTimer2;
	}
	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return getLocationManager().getBestProvider(criteria, false);
	}
	private LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}	
	
	private void startMyLocationsTimer2() {
		startMyLocationsTimer2(1000*getModifyingValue(),1000*getModifyingValue());
	}
	
	
	private void resetmAlarmSender() {
		stopMyLocationsTimer2();
		startMyLocationsTimer2();
	}
	
	private void modifyAlarmMinutes(Boolean increase) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int modifyingValue = settings.getInt("modifyingValue", 1);		
		SharedPreferences.Editor editor = settings.edit();
		if(increase) {
			if(modifyingValue<18) {
				modifyingValue++;
				editor.putInt("modifyingValue", modifyingValue);
				editor.commit();
				resetmAlarmSender();
			}
		}
		if(!increase) {
			if(modifyingValue>2) {
				modifyingValue--;
				editor.putInt("modifyingValue", modifyingValue);
				editor.commit();
				resetmAlarmSender();
			}
		}
	}	
	private void manageLocationNotifications(Location newLocation) {
		if(mDontReenter==0) {
			mDontReenter++;
	        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
	        float latitude = settings.getFloat("latitude", 0);
	        float longitude = settings.getFloat("longitude", 0);
	        
			if(latitude!=0 && mLastKnownLocation != null) {
				float distance = Float.valueOf(settings.getString("LocationDistance", "503"));
	    		Location location = new Location(getProvider());
	    		location.setLatitude(Double.valueOf(latitude));
	    		location.setLongitude(Double.valueOf(longitude));
	    		float dx = mLastKnownLocation.distanceTo(location);
	    		
	    		if(dx<distance) { 
	    			notifyUser();
	    		}
	        }
			if(mDontReenter>0) {
				mDontReenter--;
			}
		}
	}
	
	private void doS() {
		if(mDontReenter2==0) {
			mDontReenter2++;
			String provider=getProvider();
			
	        if(provider==null) {
	        	provider=LocationManager.GPS_PROVIDER;
	        }
	        if(getLocationManager().isProviderEnabled(provider) && !mImGettingLocationUpdates) {  
	        	mImGettingLocationUpdates=true;
				getLocationManager().requestLocationUpdates(getProvider(), 0, 0, new LocationListener() {
					@Override
					public void onLocationChanged(Location location) {
						try {
							if(location.hasAccuracy()==false || location.getAccuracy()<412) {
								manageLocationNotifications(location);
								getLocationManager().removeUpdates(this); 
								try {
									if (
										(location.hasSpeed() && location.getSpeed()>2f) 
											||
									    (mLastKnownLocation != null && location.distanceTo(mLastKnownLocation)> 100f)
									) {
										modifyAlarmMinutes(false);
									} else {
										if(location.getSpeed()<1f) {
											modifyAlarmMinutes(true);
										}
									}
								} catch (Exception ee33dd3) {}
								mLastKnownLocation= location;
								mImGettingLocationUpdates=false;
							}
						} catch (Exception ee) {
							
						}
					}
					@Override
					public void onProviderDisabled(String provider) {
					}
					@Override
					public void onProviderEnabled(String provider) {
					}
					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {
					}
				},Looper.getMainLooper());					
			}
	        if(mDontReenter2>0) {
	        	mDontReenter2--;
	        }
		}
	}

	
	
	private int getModifyingValue() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt("modifyingValue", 1);
	}
	private void startMyLocationsTimer2(long trigger, long interval) {
		getLocationsTimer2().schedule(new TimerTask() {
			public void run() {
				try {
					doS();

				} catch (Exception ee) {
					
				}
			}
		}, trigger, interval);
	}
	
	private void stopMyLocationsTimer2() {
		if (mLocationsTimer2 != null) {
			try {
				mLocationsTimer2.cancel();
				mLocationsTimer2.purge();
			} catch (Exception e) {
			}
			mLocationsTimer2 = null;
		}
	}		
}
