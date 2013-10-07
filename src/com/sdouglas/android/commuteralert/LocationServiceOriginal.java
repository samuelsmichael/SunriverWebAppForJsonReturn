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

/*
 * Overview of LocationServiceOriginal
 *	1.  It's a Service, so it doesn't get paged out of memory by Android.  Otherwise we
 *		would not be getting reliable alerts.
 *  2.  It uses a dynamically maintained frequency of asking for the current location.
 *  	When the user isn't moving, it requests these updates a the slowest rate.  As
 *  	the user is moving, the frequency gradually increases; and conversely, when he stops, the
 *  	frequency gradually decreases.  This is how I minimize battery usage.
 *  3.  A timer is used to effect this change of frequency. When the timer goes off, a request
 *  	is made to the GPS.  Then the timer is reset.  The amount of time until the next
 *  	"pop" of the timer is determined by the frequency described above.
 *  4.  2 fields are used to maintain this data:
 *  	-- MTIMEOALARMINTENSECONDINTERVALS goes up and down dynamically, as described above.
 *  	-- The SharedPreference value "modifyingvalue" can be set as another parameter to
 *  		the frequency calculation.  The idea is to -- at some future date -- give the user some control over
 *  		the frequency via Preferences.
 *  		
 */

public class LocationServiceOriginal extends LocationService implements LocationListener {
	private long MTIMEOALARMINTENSECONDINTERVALS=12;
	private Timer mLocationsTimer2=null;
	private LocationManager mLocationManager=null;
	private Location mLastKnownLocation=null;
    private static final float ALARM_RADIUS_IN_METERS=500f;
	private int _jdFY=0;

	public LocationServiceOriginal() {
		super();
	}


	protected void disarmLocationManagement() {
    	stopMyLocationsTimer2();		
	}
	
	

	
    protected void initializeLocationManager() {
        try {
    		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),Context.MODE_PRIVATE);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putInt("modifyingValue", 1);
    		editor.commit();

    		String bestProvider = getProvider();
            if(bestProvider==null) {
            	bestProvider=LocationManager.GPS_PROVIDER;
            }
            if(getLocationManager().isProviderEnabled(bestProvider)) {
                getLocationManager().requestLocationUpdates(bestProvider, 20000, 1, this);
            }
        } catch (Exception ee3) {
        }
    }
	protected void beginLocationListening() {
		getmAlarmSender();
	}
	
	
	private int getModifyingValue() {
        SharedPreferences settings = getSharedPreferences(getPREFS_NAME(), Context.MODE_PRIVATE);
        return settings.getInt("modifyingValue", 1);
	}
	private void getmAlarmSender() {
		startMyLocationsTimer2(500,1000*3*MTIMEOALARMINTENSECONDINTERVALS*getModifyingValue());
	}
	private Timer getLocationsTimer2() {
		if (mLocationsTimer2 == null) {
			mLocationsTimer2 = new Timer("LocationsActivities2");
		}
		return mLocationsTimer2;
	}
	private LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}
	private long getAlarmInTenSecondIntervals() {

		int modifyingValue=1;
		try {
			if(MTIMEOALARMINTENSECONDINTERVALS<12) { 
				modifyingValue = getModifyingValue();
			}
		} catch (Exception eee3) {}

		
		return MTIMEOALARMINTENSECONDINTERVALS*modifyingValue;
	}
	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return getLocationManager().getBestProvider(criteria, false);
	}

	int mDontReenter=0;
	private void manageLocationNotifications(Location newLocation) {
		if(mDontReenter==0) {
			mDontReenter++;
	        SharedPreferences settings = getSharedPreferences(getPREFS_NAME(), MODE_PRIVATE);
	        double latitude = Double.valueOf(settings.getString("latitude", "0"));
	        double longitude = Double.valueOf(settings.getString("longitude", "0"));
	        
			if(latitude!=0 && mLastKnownLocation != null) {
				float distance = Float.valueOf(settings.getString("LocationDistance", "502"));
	    		Location location = new Location(getProvider());
	    		location.setLatitude(latitude);
	    		location.setLongitude(longitude);
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

	private void resetmAlarmSender() {
		stopMyLocationsTimer2();
		getmAlarmSender();
	}
	
	private void modifyAlarmMinutes(Boolean increase) {
		if(increase) {
			if(MTIMEOALARMINTENSECONDINTERVALS<18) {
				MTIMEOALARMINTENSECONDINTERVALS++;
				resetmAlarmSender();
			}
		}
		if(!increase) {
			if(MTIMEOALARMINTENSECONDINTERVALS>2) {
				MTIMEOALARMINTENSECONDINTERVALS--;
				resetmAlarmSender();
			}
		}
	}
	/*
	 * This routine is called each time the timer pops, which, as you remember, occurs at a frequence that is related to
	 * how fast we're moving.  
	 * Note how I use _jdFY as a variable to keep this routine from being re-entered, which would happen if, say, it took
	 * more time to be notified by GPS, than what time it took for the next pop.
	 * The first thing we do is to initiate the request to GPS to be notified.  As soon as we're notified, we call the routine that
	 * checks to see if we're within the dx for notification (which right now is hardcoded to 500 meters 
	 * (see HomeManager.ALARM_RADIUS_IN_METERS), and starts the notification process, if so.
	 * Then we immediately tell the GPS to stop notifying us (so we don't waste the battery). And we record our location
	 * so that the next time this routine is called we'll compare the two and thereby know whether to decrease or increase
	 * the timer-pop frequency.
	 *   
	 */
	private void doS() {
		if(_jdFY==0) {
			_jdFY++;
			long jdInterval=12;
			try {
				jdInterval=getAlarmInTenSecondIntervals();
			} catch (Exception eee) {}
			String provider=getProvider();
			
            if(provider==null) {
            	provider=LocationManager.GPS_PROVIDER;
            }
            if(getLocationManager().isProviderEnabled(provider)) {
				getLocationManager().requestLocationUpdates(getProvider(), 1000*9*jdInterval, 200, new LocationListener() {
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
							}
							if(_jdFY>0) {
								_jdFY--;
							}
						} catch (Exception ee) {
							if(mLastKnownLocation == null) {
							}
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
						//INeedToo.mSingleton.log("Provider " + provider+ " status changed to "+ String.valueOf(status)+".", 1);
					}
				},Looper.getMainLooper());					
            } else {
				if(_jdFY>0) {
					_jdFY--;
				}
            }
		}
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
		
    @Override
    public void onLocationChanged(Location location) {
        getLocationManager().removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
	
}
