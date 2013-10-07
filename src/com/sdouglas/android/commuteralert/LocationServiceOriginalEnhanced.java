package com.sdouglas.android.commuteralert;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class LocationServiceOriginalEnhanced extends LocationService {
	private LocationManager mLocationManager = null;
	private Location mLastKnownLocation = null;
	private Boolean mImGettingLocationUpdates;
	private int mDontReenter = 0;
	private int mDontReenter2 = 0;
	private Handler mHandler = null;
	private boolean mHandlerLoopIsActive = false;

	
	@Override
	protected void disarmLocationManagement() {
		getLogger().log("!!!!!!!!!!!!!!!!!!!! disarmLocationManager()", 99);
		stopHandlerLoop();
	}

	@Override
	protected void initializeLocationManager() {
		mImGettingLocationUpdates = false;
		mDontReenter = 0;
		mDontReenter2 = 0;
		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("modifyingValue", 1);
		editor.commit();
		getHandler();
		getLogger().log("Using GPS", 199);
	}

	@Override
	protected void beginLocationListening() {
		startHandlerLoop(1000 * getModifyingValue());
	}

	private Handler getHandler() {
		if (mHandler == null) {
			mHandler = new Handler();
		}
		return mHandler;
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

	private void modifyAlarmMinutes(Boolean increase) {
		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
				Context.MODE_PRIVATE);
		int modifyingValue = settings.getInt("modifyingValue", 1);
		SharedPreferences.Editor editor = settings.edit();
		if (increase) {
			if (modifyingValue < 13) {
				modifyingValue++;
				editor.putInt("modifyingValue", modifyingValue);
				editor.commit();
			}
		}
		if (!increase) {
			if (modifyingValue > 2) {
				modifyingValue--;
				editor.putInt("modifyingValue", modifyingValue);
				editor.commit();
			}
		}
	}

	private void manageLocationNotifications(Location newLocation) {
		if (mDontReenter == 0) {
			mDontReenter++;
			SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
					MODE_PRIVATE);
			double latitude = Double.valueOf(settings
					.getString("latitude", "0"));
			double longitude = Double.valueOf(settings.getString("longitude",
					"0"));

			/* BBHBB */
			StringBuilder sb = new StringBuilder();
			sb.append("Alarm Latitude: " + String.valueOf(latitude) + "\n");
			sb.append("Alarm Longitude: " + String.valueOf(longitude) + "\n");
			if (latitude != 0 && mLastKnownLocation != null) {
				sb.append("mLastKnownLocation.latitude: "
						+ String.valueOf(mLastKnownLocation.getLatitude())
						+ "\n");
				sb.append("mLastKnownLocation.longitude: "
						+ String.valueOf(mLastKnownLocation.getLongitude())
						+ "\n");

				float distance2 = Float.valueOf(settings.getString(
						"LocationDistance", "503"));
				Location location2 = new Location(getProvider());
				location2.setLatitude(Double.valueOf(latitude));
				location2.setLongitude(Double.valueOf(longitude));
				float dx2 = mLastKnownLocation.distanceTo(location2);
				sb.append("Distance from alarm: " + String.valueOf(dx2) + "\n");
				sb.append("Alert distance: " + String.valueOf(distance2) + "\n");
				sb.append("\n");
				getLogger().log(sb.toString(), 99);
			}

			if (latitude != 0 && mLastKnownLocation != null) {
				float distance = Float.valueOf(settings.getString(
						"LocationDistance", "503"));
				Location location = new Location(getProvider());
				location.setLatitude(latitude);
				location.setLongitude(longitude);
				float dx = mLastKnownLocation.distanceTo(location);

				if (dx < distance) {

					SharedPreferences settings2 = getSharedPreferences(
							getPREFS_NAME(), Context.MODE_PRIVATE);
					getLogger()
							.log("****Popping: "
									+ settings2.getString("locationString", ""),
									99);

					notifyUser();
				}
			}
			if (mDontReenter > 0) {
				mDontReenter--;
			}
		}
	}

	private void doS() {
		if (mDontReenter2 == 0) {
			mDontReenter2++;
			String provider = getProvider();

			if (provider == null) {
				provider = LocationManager.GPS_PROVIDER;
			}
			if (getLocationManager().isProviderEnabled(provider)
					&& !mImGettingLocationUpdates) {
				mImGettingLocationUpdates = true;
				getLogger().log("11111111111111 I'm requestion location", 99);
				getLocationManager().requestLocationUpdates(provider, 0, 0,
						new LocationListener() {
							@Override
							public void onLocationChanged(Location location) {
								try {
									getLogger().log(
											"22222222222222 I've got location",
											99);
									SharedPreferences settings = getSharedPreferences(
											getPREFS_NAME(), MODE_PRIVATE);
									if (location.hasAccuracy() == false
											|| location.getAccuracy() < 4 * Float.valueOf(settings
													.getString(
															"LocationDistance",
															"503"))) {
										manageLocationNotifications(location);
										getLocationManager()
												.removeUpdates(this);
										try {
											if ((location.hasSpeed() && location
													.getSpeed() > 2f)
													|| (mLastKnownLocation != null && location
															.distanceTo(mLastKnownLocation) > 100f)) {
												modifyAlarmMinutes(false);
											} else {
												if (location.getSpeed() < 1f) {
													modifyAlarmMinutes(true);
												}
											}
										} catch (Exception ee33dd3) {
										}
										mLastKnownLocation = location;
										mImGettingLocationUpdates = false;
										if (mHandlerLoopIsActive) {
											beginLocationListening();
										}
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
							public void onStatusChanged(String provider,
									int status, Bundle extras) {
							}
						}, Looper.getMainLooper());
			}
			if (mDontReenter2 > 0) {
				mDontReenter2--;
			}
		}
	}
	
	private int getModifyingValue() {
		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
				Context.MODE_PRIVATE);
		return settings.getInt("modifyingValue", 1);
	}

	private void startHandlerLoop(long trigger) {
		mHandlerLoopIsActive = true;
		getLogger().log(
				"aaaaaaaaaaaaaaaaaaa startHandler(triger: "
						+ String.valueOf(trigger), 99);
		getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				getLogger().log("bbbbbbbbbbbbbbbbbbbbbbb Handler Popped(", 99);
				if (mHandlerLoopIsActive) {
					doS();
				}
			}
		},

		trigger);

		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
				Context.MODE_PRIVATE);
		getLogger()
				.log("LocationName: "
						+ settings.getString("locationString", ""), 99);
		/*
		 * getLocationsTimer2().schedule(new TimerTask() { public void run() {
		 * try { doS();
		 * 
		 * } catch (Exception ee) {
		 * 
		 * } } }, trigger, interval);
		 */
	}

	private void stopHandlerLoop() {
		mHandlerLoopIsActive = false;
		/*
		 * if (mLocationsTimer2 != null) { try {
		 * getLogger().log("----------------------- stopMyLocationsTimer2()",
		 * 99);
		 * 
		 * mLocationsTimer2.cancel(); mLocationsTimer2.purge(); } catch
		 * (Exception e) { } mLocationsTimer2 = null; }
		 */
	}
}
