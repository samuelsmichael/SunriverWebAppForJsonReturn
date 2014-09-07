package com.diamondsoftware.android.commuterhelper;

import com.diamondsoftware.android.commuterhelper.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.diamondsoftware.android.commuterhelper.GeofenceUtils.REMOVE_TYPE;
import com.diamondsoftware.android.commuterhelper.GeofenceUtils.REQUEST_TYPE;
import com.google.android.gms.location.Geofence;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class LocationServiceGeofencing extends LocationService {

	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences;

	// Store the list of geofences to remove
	private List<String> mGeofenceIdsToRemove;

	// Store the current request
	private REQUEST_TYPE mRequestType;

	// Store the current type of removal
	private REMOVE_TYPE mRemoveType;

	// Persistent storage for geofences
	private SimpleGeofenceStore mPrefs;

	// Remove geofences handler
	private GeofenceRemover mGeofenceRemover;

	/*
	 * Internal lightweight geofence objects for geofence 1 and 2
	 */
	private SimpleGeofence mUIGeofence1;

	public LocationServiceGeofencing() {
		super();
	}

	protected void disarmLocationManagement() {
		// Store the list of geofences to remove
		/*
		 * Remove the geofence by creating a List of geofences to remove and
		 * sending it to Location Services. The List contains the id of geofence
		 * 1 ("1"). The removal happens asynchronously; Location Services calls
		 * onRemoveGeofencesByPendingIntentResult() (implemented in the current
		 * Activity) when the removal is done.
		 */

		// Create a List of 1 Geofence with the ID "1" and store it in the
		// global list
		mGeofenceIdsToRemove = Collections.singletonList("1");

		/*
		 * Record the removal as remove by list. If a connection error occurs,
		 * the app can automatically restart the removal if Google Play services
		 * can fix the error
		 */
		mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

		// Try to remove the geofence
		try {
			mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

			// Catch errors with the provided geofence IDs
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this,
					R.string.remove_geofences_already_requested_error,
					Toast.LENGTH_LONG).show();
		}
		mPrefs.clearGeofence("1");
	}

	protected void initializeLocationManager() {
		// Instantiate a Geofence remover
		mGeofenceRemover = new GeofenceRemover(this);
		// Instantiate the current List of geofences
		mCurrentGeofences = new ArrayList<Geofence>();

		// Instantiate a new geofence storage area
		mPrefs = new SimpleGeofenceStore(this);
		// Store a list of geofences to add
		List<Geofence> geofences = new ArrayList<Geofence>();
		mCurrentGeofences.clear();
		getLogger().log("Using Fences", 199);
	}

	protected void beginLocationListening() {
		mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;
		/*
		 * Create a version of geofence 1 that is "flattened" into individual
		 * fields. This allows it to be stored in SharedPreferences.
		 */
		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
				Context.MODE_PRIVATE);
		double latitude = Double.valueOf(settings.getString("latitude", "0"));
		double longitude = Double.valueOf(settings.getString("longitude", "0"));
		float distance = Float.valueOf(settings.getString("LocationDistance",
				"501"));
		mUIGeofence1 = new SimpleGeofence("1",
		// Get latitude, longitude, and radius from the UI
				Double.valueOf(latitude), Double.valueOf(longitude),
				Float.valueOf(distance),
				// Set the expiration time
				Geofence.NEVER_EXPIRE,
				// Only detect entry transitions
				Geofence.GEOFENCE_TRANSITION_ENTER);

		// Store this flat version in SharedPreferences
		mPrefs.setGeofence("1", mUIGeofence1);
		mCurrentGeofences.add(mUIGeofence1.toGeofence());

		GeofenceRequestor geofences = new GeofenceRequestor(this);
		geofences.addGeofences(mCurrentGeofences);
	}

}
