package com.sdouglas.android.commuteralert;

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

public class LocationService extends Service implements LocationListener  {
	private long MTIMEOALARMINTENSECONDINTERVALS=12;
    public static final String PREFS_NAME = "MyPrefsFile";
	private Timer mLocationsTimer2=null;
	private LocationManager mLocationManager=null;
	private Location mLastKnownLocation=null;
    private NotificationManager mNotificationManager=null;
    private static final int ARMED_NOTIFICATION_ID=3;
    private String mAddressInReadableForm;

	private int _jdFY=0;

	public LocationService() {
	}
	
	private LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
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
		if(intent.getAction()=="JustDisarm") {
	    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);	
	    	stopMyLocationsTimer2();
		} else {
			if(intent.getAction()=="JustInitializeLocationManager") {
				initializeLocationManager();
			} else {
				mAddressInReadableForm=intent.getStringExtra("LocationAddress");
		    	Notification.Builder mBuilder=new Notification.Builder(this)
			    	.setSmallIcon(R.drawable.ic_launcher)
			    	.setContentTitle("CommuterAlert is armed")
			    	.setContentText(mAddressInReadableForm)
			    	.setOngoing(true);
		    	
		    	// Creates an explicit intent for an Activity in your app
		    	Intent resultIntent = new Intent(this, LocationService.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(this,
						(int)System.currentTimeMillis(), resultIntent, 0);
		    	mBuilder.setContentIntent(pendingIntent);    	    	
		    	getNotificationManager().notify(ARMED_NOTIFICATION_ID, mBuilder.getNotification());
	
				getmAlarmSender();
			}
		}
	}
	@Override
	public void onDestroy() {
		stopMyLocationsTimer2();
	}
	private int getModifyingValue() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
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
	public long getAlarmInTenSecondIntervals() {

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

	private NotificationManager getNotificationManager() {
		if (mNotificationManager==null) {
			mNotificationManager =
	    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mNotificationManager;
	}

	private void sayIt(String it) {
		Intent jdIntent=new Intent(this, VoiceHelper.class)
		.putExtra("voicedata",it);
		jdIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(jdIntent);		

	}

	int mDontReenter=0;
	private void manageLocationNotifications(Location newLocation) {
		if(mDontReenter==0) {
			mDontReenter++;
	        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	        float latitude = settings.getFloat("latitude", 0);
	        float longitude = settings.getFloat("longitude", 0);
			if(latitude!=0 && mLastKnownLocation != null) {
	    		Location location = new Location(getProvider());
	    		location.setLatitude(Double.valueOf(latitude));
	    		location.setLongitude(Double.valueOf(longitude));
	    		float dx = mLastKnownLocation.distanceTo(location);
	    		if(dx<500) { //TODO: parameterize this
	    			/* This is it!  We've arrived. Time to wake up our sleeping passenger*/
	    			// remove the "is armed" notification 
	    			stopMyLocationsTimer2();
	    			SharedPreferences.Editor editor = settings.edit();
	    	        editor.putFloat("latitude", (float) 0);
	    	        editor.putFloat("longitude", (float) 0);
	    	        editor.putString("locationString","");

	    	        editor.commit();
	    	        		    			int armedNotification=settings.getInt("IsArmedNotificationId",0);
	    	    	getNotificationManager().cancel(ARMED_NOTIFICATION_ID);
	    	    	// and create one of our own
	    	    	Notification.Builder mBuilder=new Notification.Builder(this)
			    	.setSmallIcon(R.drawable.ic_launcher)
			    	.setContentTitle("Douglas. Wake up! We are arriving at our destination!")
			    	.setContentText(mAddressInReadableForm)
			    	.setVibrate(new long[] {100,1000,100,1000,100,1000})
			    	.setDefaults(Notification.DEFAULT_SOUND); //TODO: get this from checkboxes in Home
		    	
			    	// Creates an explicit intent for an Activity in your app
			    	Intent resultIntent = new Intent(this, LocationService.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(this,
							(int)System.currentTimeMillis(), resultIntent, 0);
			    	mBuilder.setContentIntent(pendingIntent);    	    	
			    	getNotificationManager().notify(ARMED_NOTIFICATION_ID, mBuilder.getNotification());
			    	sayIt("Douglas ... Wake up! We are arriving at our destination!");
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
	
	private void doS() {
		try {

			if(_jdFY==0) {
				Location jdlocation=getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(jdlocation!=null) {
					_jdFY++;
					long jdInterval=12;
					try {
						jdInterval=getAlarmInTenSecondIntervals();
					} catch (Exception eee) {}
					getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*9*jdInterval, 200, new LocationListener() {
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
									int bkHere=3;
									int bkThere=4;
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
				}
			}
		} catch (Exception e) {
			int bkhere=3;
			int bkthere=bkhere;
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
    private void initializeLocationManager() {
        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String bestProvider = getLocationManager().getBestProvider(criteria, false);
            if(bestProvider==null) {
            	bestProvider=LocationManager.GPS_PROVIDER;
            }
            if(getLocationManager().isProviderEnabled(bestProvider)) {
                getLocationManager().requestLocationUpdates(bestProvider, 20000, 1, this);
                getLocationManager().getLastKnownLocation(bestProvider);
            }
        } catch (Exception ee3) {
        }
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
