package com.sdouglas.android.commuteralert;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

public class Home2 extends Activity implements HomeImplementer {
	private GoogleMap mMap = null;
	private MapFragment mMapFragment;
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	public static final String PREFS_NAME = "com.sdouglas.android.commuteralert_preferences";
	private HomeManager mHomeManager;	
	private static float DEFAULT_ZOOM = 11f;
	private static float DEFAULT_TILT=0f;
	private static float DEFAULT_BEARING=0f;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home2);
	}

	@Override
	protected void onStart() {
		super.onStart();
		getHomeManager().ascertainLocationMethod(this);		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (checkPlayServices()) {
			onResumeSetupMapIfNeeded();
		}
	}	
	
	/* The Play Services API has to be installed on the user's machine in order
	 * for the map to show up. I check for it here, and if it isn't present,
	 * then a dialog is presented to the user allowing him to fetch it.
	 */
	private boolean checkPlayServices() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				showPlaystoreAPIErrorDialog(status);
			}
			return false;
		}
		return true;
	}

	public void showPlaystoreAPIErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, this,
				REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_RECOVER_PLAY_SERVICES:
			if (resultCode == RESULT_CANCELED) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						this,AlertDialog.THEME_TRADITIONAL);
				builder.setTitle("Application Alert")
				.setMessage("This application won't run without Google Play Services installed")
				.setPositiveButton("Okay", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				AlertDialog alert = builder.create();
				alert.show();			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.home2, menu);
		return true;
	}

	/* Do a check to see if the map object (mMap) has
	 * already been created.  If not, then we have to prepare for displaying it,
	 * and that involves also "finding initial location" -- which is our location --
	 * and fetching all of the rail stations in the vicinity.
	 * The reason I do this onResume is that onResume gets called even after a popped up dialog box
	 * is present and then is closed ... which would be the case, say, if the user didn't have
	 * Play Services installed, and was presented with the dialog to install it.
	 */
	private void onResumeSetupMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
					R.id.map2);
			mMap = mMapFragment.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// The Map is verified. It is now safe to manipulate the map.
		//		mMap.animateCamera(CameraUpdateFactory.zoomTo(mMapZoomLevel));
				mMap.setMyLocationEnabled(true);
				mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
					@Override
					public void onCameraChange(CameraPosition arg0) {
						SharedPreferences settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putFloat("zoom", arg0.zoom);
						editor.putFloat("bearing", arg0.bearing);
						editor.putFloat("tilt", arg0.tilt);	
						editor.putFloat("whereiamatlat", (float)arg0.target.latitude);
						editor.putFloat("whereiamatlng", (float)arg0.target.longitude);
						editor.commit();
					}					
				});
			}
		} else {
	//		mMap.animateCamera(CameraUpdateFactory.zoomTo(mMapZoomLevel));
			mMap.setMyLocationEnabled(true);
		}
	}
	private HomeManager getHomeManager() {
		if (mHomeManager == null) {
			mHomeManager = new HomeManager(this);
		}
		return mHomeManager;
	}

	private void animateCamera(double latitude, double longitude) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latitude,longitude), 
				settings.getFloat("zoom", DEFAULT_ZOOM), 
				settings.getFloat("tilt", DEFAULT_TILT), 
				settings.getFloat("bearing", DEFAULT_BEARING))));
	}
	
	public void positionMapToLocation(double latitude, double longitude) {
		if(mMap != null) {
			animateCamera(latitude,longitude);
		}
	}
	
	
	@Override
	public void heresYourAddress(Address address, String readableAddress,
			LatLng whereImAt) {
		
		if(address==null) {
			if(whereImAt!=null) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
				positionMapToLocation(settings.getFloat("whereimatlat", (float)whereImAt.latitude) , settings.getFloat("whereimatlng", (float)whereImAt.longitude));
			}
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void heresTheTrainStationAddressesToDisplayOnMap(
			ArrayList<Address> addresses, Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dropPin(Address a) {
		// TODO Auto-generated method stub
		
	}
}
