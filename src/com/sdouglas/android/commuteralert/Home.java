package com.sdouglas.android.commuteralert;

import java.util.ArrayList;
import java.util.List;
import java.util.GregorianCalendar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class Home extends Activity implements HomeImplementer {
	private GoogleMap mMap = null;
	private HomeManager mHomeManager;
	private LocationManager mLocationManager = null;
	private MapFragment mMapFragment;
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	public static final String PREFS_NAME = "MyPrefsFile";
	static final float SOMEKINDOFFACTOR=720; // this factor is the "number of meters" under which when the user presses a train, we assume he meant to press the train, at zoom level 11.
	static final int PURGECACHEDAYSOLD=100; // number of days, older than which items in the cache are purged.
	private Marker mPreviousMarker;

	private HomeManager getHomeManager() {
		if (mHomeManager == null) {
			mHomeManager = new HomeManager(this);
		}
		return mHomeManager;
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GregorianCalendar calendar=new GregorianCalendar();
		calendar.add(GregorianCalendar.DATE, -PURGECACHEDAYSOLD);
		getHomeManager().getDbAdapter().purgeCacheOfItemsOlderThan(calendar.getTime());
		setContentView(R.layout.activity_home);
		if(getIntent()!=null && getIntent().getAction()!=null && getIntent().getAction().equals("showdisarmed")) {
			setControlsVisibility(false, "");
		}
		if (getIntent()!=null && getIntent().getAction()!=null && getIntent().getAction().equals("dovoice")) {
			String voiceData=getIntent().getStringExtra("voicedata");
			Intent jdIntent=new Intent(this, VoiceHelper.class)
				.putExtra("voicedata",voiceData);
			startActivity(jdIntent);			
			setControlsVisibility(false, "");
  		}
		final EditText locationAddress = (EditText) findViewById(R.id.editText);
		final Button deriveFromAddress = (Button) findViewById(R.id.buttonAddress);

		/* User clicks the button after having keyed in an address */
		deriveFromAddress.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getHomeManager().manageKeyedInAddress(locationAddress.getText()
						.toString());
			}
		});
		final CheckBox vibrate = (CheckBox) findViewById(R.id.cbVibrate);
		final CheckBox sound = (CheckBox) findViewById(R.id.cbSound);
		final CheckBox voice = (CheckBox) findViewById(R.id.cbVoice);
		vibrate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
				String oldValue=settings.getString("vibrate", "n");
				SharedPreferences.Editor editor = settings.edit();
				if(oldValue.equals("n")) {
					editor.putString("vibrate","y");
				} else {
					editor.putString("vibrate","n");
				}
				editor.commit();
			}
		});
		sound.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
				String oldValue=settings.getString("sound", "n");
				SharedPreferences.Editor editor = settings.edit();
				if(oldValue.equals("n")) {
					editor.putString("sound","y");
				} else {
					editor.putString("sound","n");
				}
				editor.commit();
			}
		});
		voice.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
				String oldValue=settings.getString("voice", "n");
				SharedPreferences.Editor editor = settings.edit();
				if(oldValue.equals("n")) {
					editor.putString("voice","y");
				} else {
					editor.putString("voice","n");
				}
				editor.commit();
			}
		});
	}

	@Override
	public void onDestroy() {
		mHomeManager.close();
		super.onDestroy();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (checkPlayServices()) {
			setupMapIfNeeded();
		}
		getHomeManager().initialize();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		/* Checking for the installation of Play Services on the user's phone.
		 * I do it here (onRestart occurs both on initial startup, and on whenever
		 * Android "pages" your app back into memory after having taken it out
		 * due to memory squeeze) because if, when the program first verifies for
		 * the presence of Play Store and gets a dialog box that says that it's 
		 * not installed, and the user then goes and fetches it; then, we re-check here.
		 * 
		 */
		if (checkPlayServices()) {
			setupMapIfNeeded();
		}
		getHomeManager().initialize();
	}
	
	/* Whenever the application starts up (onResume), or it gets "paged" back into memory (onRestart),
	 * do a check to see if the map object (mMap) has
	 * already been created.  If not, then we have to prepare for displaying it,
	 * and that involves also "finding initial location" -- which is our location --
	 * and fetching all of the rail stations in the vicinity.
	 */
	private void setupMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
					R.id.map);
			mMap = mMapFragment.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// The Map is verified. It is now safe to manipulate the map.
				mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
				findInitialLocation();
			}
		} else {
			mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//			mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
	//				40,
		//			-105f)));
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
				showErrorDialog(status);
			}
			return false;
		}
		return true;
	}

	void showErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, this,
				REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_RECOVER_PLAY_SERVICES:
			if (resultCode == RESULT_CANCELED) {
				//TODO: Perhaps we could warn the user that the map feature won't work without Play Services on his machine.
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

/*
 * This is for the Android system menu.  I'm not sure what we'd put there,
 * but if we ever think of something, here's where we'd do it.
 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	/*
	 * This method is called by the system (only on the UI thread, of course (See class AsyncTask for way of doing this.)) 
	 * once an address has been fetched, either by means of the user keying in an address, or long-pressing the map. 
	 * It manages the displaying and un-displaying of the UI controls appropriately, positioning the map to
	 * that location, and dropping a red pin.
	 *  
	 * If this method is passed null, this signifies that there is no
	 * location; either because none was found, or that the user cancelled the
	 * pop-up that asked him to choose from the list of many addresses found, or
	 * that the alert has been generated, and we're thereby disarming the system..
	 * 
	 */
	public void heresYourAddress(Address address, String readableAddress) { 
		final Button disarmButton = (Button) findViewById(R.id.btnDisarm);
		
		if (address != null) {
			setControlsVisibility(true, readableAddress);
			positionMapToLocation((double)address.getLatitude(),(double)address.getLongitude());			
			
		} else {
			setControlsVisibility(false, "");
		}
		disarmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getHomeManager().disarmLocationService();
				setControlsVisibility(false, "");

			}
		});
	}

	/*
	 * Some controls are more appropriate to show than others, depending on whether the system is armed or not.
	 * And while we're at it, the "your destination" pin is either removed, or moved.
	 */
	private void setControlsVisibility(Boolean isArmed, String readableAddress) {
		final TextView currentLocation = (TextView) findViewById(R.id.tvCurrentLocation);
		final Button disarmButton = (Button) findViewById(R.id.btnDisarm);
		final TextView systemIsArmed = (TextView) findViewById(R.id.tvCurrentViewHeading);
		final EditText locationAddress = (EditText) findViewById(R.id.editText);
		final Button deriveFromAddress = (Button) findViewById(R.id.buttonAddress);
		final TextView systemStatus = (TextView) findViewById(R.id.tvSystemStatus2);
		final CheckBox vibrate = (CheckBox) findViewById(R.id.cbVibrate);
		final CheckBox sound = (CheckBox) findViewById(R.id.cbSound);
		final CheckBox voice = (CheckBox) findViewById(R.id.cbVoice);
		
		// Hide the previous pin; otherwise they just continue to accumulate.
		if (mPreviousMarker != null && mPreviousMarker.isVisible()) {
			mPreviousMarker.setVisible(false);
		}
		if (isArmed) {
			currentLocation.setText(readableAddress);
			currentLocation.setTextColor(Color.BLUE);
			vibrate.setVisibility(View.GONE);
			sound.setVisibility(View.GONE);
			voice.setVisibility(View.GONE);
			disarmButton.setVisibility(View.VISIBLE);
			systemIsArmed.setVisibility(View.VISIBLE);
			locationAddress.setVisibility(View.GONE);
			deriveFromAddress.setVisibility(View.GONE);
			systemStatus.setText("Armed");
			systemStatus.setTextColor(Color.RED);
		} else {
			vibrate.setVisibility(View.VISIBLE);
			sound.setVisibility(View.VISIBLE);
			voice.setVisibility(View.VISIBLE);
			disarmButton.setVisibility(View.GONE);
			systemIsArmed.setVisibility(View.GONE);
			locationAddress.setVisibility(View.VISIBLE);
			deriveFromAddress.setVisibility(View.VISIBLE);
			systemStatus.setText("Disarmed");
			systemStatus.setTextColor(Color.BLUE);
			setACheckbox("vibrate", vibrate);
			setACheckbox("sound", sound);
			setACheckbox("voice",voice);
		}
	}

	private void setACheckbox(String settingName, CheckBox checkbox) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		String theValue=settings.getString(settingName, "q");
		if(theValue.equals("q")) {
			theValue="y";
			editor.putString("vibrate", theValue);
		}
		if (theValue.equals("n")) {
			checkbox.setChecked(false);
		} else {
			checkbox.setChecked(true);
		}
		editor.commit();
	}
	private LocationManager getLocationManager() {
		if (mLocationManager == null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}

	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return getLocationManager().getBestProvider(criteria, false);
	}
	public void positionMapToLocation(double latitude, double longitude) {
		if(mMap != null) {
		mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
				(float) latitude,
				(float) longitude)));
		}
	}

	/*
	 * I am using an AsyncTask here because its onPostExecute insures that I can
	 * update the UI; and as this whole thing is initiated via a background thread
	 * (we have to make a web call to Google Services in order to fetch the train
	 * stations, and such calls are not allowed by Android to be made in the UI thread), 
	 * we will need to "get back on" the UI thread in order to display it.
	 */
	class ShowMap
			extends
			AsyncTask<LocationAndAssociatedTrainStations, Void, LocationAndAssociatedTrainStations> {
		protected LocationAndAssociatedTrainStations doInBackground(
				LocationAndAssociatedTrainStations... location) {
			try {
				return location[0];
			} catch (Exception e) {
				return null;
			}
		}
		/*
		 * The class LocationAndAssociatedTrainStations is a combination of two objects -- the list
		 * of addresses of the trains stations, and the Location object defining where I am at currently.
		 * Due to the fact that the AsyncTask class can only handle one object passed into it,
		 * I was constrained to create a class that holds both of these pieces of information; both of
		 * which are required by this method.
		 */
		protected void onPostExecute(LocationAndAssociatedTrainStations result) {
			final LocationAndAssociatedTrainStations resultF = result;
			if (result != null) {
				// position map to location (duh)
				positionMapToLocation(result.mLocation.getLatitude(),result.mLocation.getLongitude());
				// turn on the little "take me to my current location" icon
				mMap.setMyLocationEnabled(true);
				// the instruction on how to use the map needs to be shown
				TextView tvId1 = (TextView)findViewById(R.id.tvId1);
				tvId1.setVisibility(View.VISIBLE);
				// now create Markers for all of the trains.
				BitmapDescriptor bmd = BitmapDescriptorFactory
						.fromResource(R.drawable.train1);
				for (int i = 0; i < result.mAddresses.size(); i++) {
					Address address = result.mAddresses.get(i);
					Marker marker = mMap.addMarker(new MarkerOptions()
							.position(
									new LatLng(address.getLatitude(), address
											.getLongitude()))
							.title(address.getAddressLine(0)).icon(bmd));
					marker.showInfoWindow();
				}
				// define an "onMapLongClick" listener that
				// 1. Moves the marker (hides the old one and creates the new one)
				// 2. If he touches near the train, assume he meant to touch the train.
				// 3. Initiate what needs to be done to arm the system.
				mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
					public void onMapLongClick(LatLng point) {
						LatLng useThisOne=null;
						if (mPreviousMarker != null) {
							mPreviousMarker.setVisible(false);
						}
						/*
						 * Do a "snap-to-grid" kind of thing.  If the guy's pressing near the train, let's snap him to the train.
						 */
						
						float zoomLevel=mMap.getCameraPosition().zoom;
						float errorMarginMetersUnderWhichWeAssumeHePressedTheTrain=0f;
						errorMarginMetersUnderWhichWeAssumeHePressedTheTrain=(float) (SOMEKINDOFFACTOR * (1f/(Math.pow(2f,(zoomLevel-12f)))));
						Address useThisAddress = null;
						// Drop a train bitmap as a marker at each place on the map
						// If he's near a train, then assume he meant to press the train.
						for (int i = 0; i < resultF.mAddresses.size(); i++) {
							LatLng latlng2 = new LatLng(resultF.mAddresses.get(
									i).getLatitude(), resultF.mAddresses.get(i)
									.getLongitude());
							float[] results = new float[3];
							Location.distanceBetween(point.latitude,
									point.longitude, latlng2.latitude,
									latlng2.longitude, results);
							if (results[0] < errorMarginMetersUnderWhichWeAssumeHePressedTheTrain ) {
								useThisOne = latlng2;
								useThisAddress = resultF.mAddresses.get(i);
								break;
							}
						}
						/*
						 * All we're given is a point (latitude and longitude).  Is there an address
						 * near it so we can use that description?
						 * 
						 */
						try {
							if (useThisAddress == null) {
								Geocoder g = new Geocoder(Home.this);
								List<Address> addresses = g.getFromLocation(
										(double) point.latitude,
										(double) point.longitude, 2);
								if (addresses != null && addresses.size() > 0) {
									useThisAddress = addresses.get(0);
									/* Even though we've found a good displayable name, we still want to drop the pin in the exact right place.*/
									useThisAddress.setLatitude(point.latitude);
									useThisAddress.setLongitude(point.longitude);
								}
							}
						} catch (Exception e) {
						}
						/*
						 * If not, then just make up a description
						 */
						if (useThisAddress == null) {
							useThisAddress = new Address(null);
							useThisAddress.setLatitude(point.latitude);
							useThisAddress.setLongitude(point.longitude);
							useThisAddress.setAddressLine(1,
									"Address for red marker, below");
						}
						// arm the system
						getHomeManager().newLocation(useThisAddress);
						
					}
				});
			}
		}
	}

	public void dropPin(Address a) {
		if (mMap != null) {
			LatLng latlng=new LatLng(a.getLatitude(),a.getLongitude());
			Marker marker = mMap
					.addMarker(new MarkerOptions()
							.position(latlng)
							.title("Here's your destination")
							.snippet(
									"You will be notified when you are near it"));
			marker.showInfoWindow();
			mPreviousMarker = marker;
		}
	}
	
	private class LocationAndAssociatedTrainStations {
		public Location mLocation;
		public ArrayList<Address> mAddresses;

		LocationAndAssociatedTrainStations(Location location,
				ArrayList<Address> addresses) {
			mLocation = location;
			mAddresses = addresses;
		}
	}

	/*
	 * This is what the Model calls; but I have to initiate an AsyncTask, because we're going to be updating in a non-UI thread
	 */
	public void heresTheTrainStationAddressesToDisplayOnMap(
			ArrayList<Address> addresses, Location location) {
		LocationAndAssociatedTrainStations t = new LocationAndAssociatedTrainStations(
				location, addresses);
		new ShowMap().execute(t);
	}

	/* 
	 * Here is where we're going to request our list of trains from.
	 */
	private void findInitialLocation() {
		String provider = getProvider();
		if (provider == null) {
			provider = LocationManager.GPS_PROVIDER;
		}
		if (getLocationManager().isProviderEnabled(provider)) {
			getLocationManager().requestLocationUpdates(getProvider(), 2000, 1,
					new LocationListener() {
						@Override
						public void onLocationChanged(Location location) {
							
							
							// simulate Scott's address
					//		location.setLatitude(40.658421);
					//		location.setLongitude(-74.29959);
							
							
							getLocationManager().removeUpdates(this);
							getHomeManager().new RetrieveAddressDataForMap()
									.execute(location);
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
							// INeedToo.mSingleton.log("Provider " + provider+
							// " status changed to "+
							// String.valueOf(status)+".", 1);
						}
					}, Looper.getMainLooper());
		} else {
			
		}
	}
}
