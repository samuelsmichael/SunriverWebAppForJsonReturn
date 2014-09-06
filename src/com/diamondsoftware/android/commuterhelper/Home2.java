package com.diamondsoftware.android.commuterhelper;

import com.diamondsoftware.android.commuterhelper.R;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


/*
 * To change from regular version to trial (and visa versa):
 * 		1. Right click on com.sdouglas.android.commuteralert, refactor, rename to com.sdouglas.android.commuteralerttrial
 * 		2. There will be several "import com.sdouglas.android.commuteralert.R"s that have to be renamed.
 * 		3. Scan the entire project for TRIAL_VS_NON-TRIAL and follow instructions
 */
public class Home2 extends Activity implements HomeImplementer,
		WantsSurroundingTrainStations {
	private GoogleMap mMap = null;
	private MapFragment mMapFragment;
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	private HomeManager mHomeManager;
	private static float DEFAULT_ZOOM = 11f;
	private static float DEFAULT_TILT = 0f;
	private static float DEFAULT_BEARING = 0f;
	private Marker mPreviousMarker;
	private boolean mIveAnimated=false;
	static final float SOMEKINDOFFACTOR = 720; // this factor is the
												// "number of meters" under
												// which when the user presses a
												// train, we assume he meant to
												// press the train, at zoom
												// level 11.
	static final int PURGECACHEDAYSOLD = 100; // number of days, older than
												// which items in the cache are
												// purged.
	private static final String ACTION_HERES_AN_STREET_ADDRESS_TO_SEEK = "ACTION_HERES_AN_STREED_ADDRESS_TO_SEEK";
	private MyBroadcastReceiver mBroadcastReceiver;
	private IntentFilter mIntentFilter;
	private static String INSTRUCTIONS_MESSAGE = "To select a location\n\n-- Long press the screen\n    at the desired location. \n\n              or\n\n-- Press the Search button.";
	private static String SPLASH_SCREEN_MESSAGE = "To use Commuter Alert, select a location by either:\n\n--long pressing the map\n    at the desired location, or\n\n--pressing the Search button\n    located at the bottom-left\n    of the screen.";
	public static String CURRENT_VERSION = "2.00";
	private boolean needToBringUpSplashScreen = false;
	public static Home2 mSingleton=null;
	public boolean mIveShownGPSNotEnabledWarning=false;

	public boolean areWeArmed() {
		try {
			return ((CompoundButton) findViewById(R.id.switchArmed)).isChecked();
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!mIveShownGPSNotEnabledWarning) {
			mIveShownGPSNotEnabledWarning=true;
		    if (! getLocationManager().isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
		        final AlertDialog.Builder builder = new AlertDialog.Builder(					
		        		new ContextThemeWrapper(this,
						R.style.AlertDialogCustomLight));
		        builder.setTitle("GPS is disabled");//
		        builder.setMessage("For best results, your GPS should be enabled. Do you want to enable it?")
		               .setCancelable(false)
		               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		                       startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		                   }
		               })
		               .setNegativeButton("No", new DialogInterface.OnClickListener() {
		                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		                        dialog.cancel();
		                   }
		               });
		        final AlertDialog alert = builder.create();
		        alert.show();	    	
		    }
		}
		
		mSingleton=this;
		if (!getHomeManager().getSecurityManager().initializeVersion()) {
			needToBringUpSplashScreen = true;
		}
		// Create a new broadcast receiver to receive updates from the listeners
		// and service
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
		}
		
		mBroadcastReceiver = new MyBroadcastReceiver();
		// Create an intent filter for the broadcast receiver
		mIntentFilter = new IntentFilter();
		// Action for broadcast Intents containing various types of geofencing
		// errors
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
		// Action for broadcast Intents to arm the address
		mIntentFilter.addAction(ACTION_HERES_AN_STREET_ADDRESS_TO_SEEK);
		// All Location Services sample apps use this category
		mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(GregorianCalendar.DATE, -PURGECACHEDAYSOLD);
		getHomeManager().getDbAdapter().purgeCacheOfItemsOlderThan(
				calendar.getTime());
		setContentView(R.layout.activity_home2);

		final CompoundButton armedButton = (CompoundButton) findViewById(R.id.switchArmed);
		armedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CompoundButton) v).isChecked()) {
					SharedPreferences settings = getSharedPreferences(
							getPREFS_NAME(), MODE_PRIVATE);
					if (Double.valueOf(settings.getString("latitude","0")) == 0) {
						new WarningAndInitialDialog(
								"You must select a location before turning on the alert.",
								INSTRUCTIONS_MESSAGE, Home2.this).show();
						armedButton.setChecked(false);
					}
				}
			}
		});
		armedButton
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (!isChecked) {
							getHomeManager().disarmLocationService();
							setControlState(false,null);
						}
					}

				});
		final Button search = (Button) findViewById(R.id.buttonSearch);
		search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Home2.this, SearchActivity.class);
				startActivity(intent);
			}

		});
	}

	private LocationManager mLocationManager = null;
	
	private LocationManager getLocationManager() {
		if (mLocationManager == null) {
			mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
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

	/*
	 * The Play Services API has to be installed on the user's machine in order
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
				AlertDialog.Builder builder = new AlertDialog.Builder(this,
						AlertDialog.THEME_TRADITIONAL);
				builder.setTitle("Application Alert")
						.setMessage(
								"This application won't run without Google Play Services installed")
						.setPositiveButton("Okay",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home2, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i2 = new Intent(this, Preferences.class);
			startActivity(i2);
			return true;
		case R.id.action_rateapp:
			String uri = "market://details?id="
					+ getPackageName()
							.replace("trial", "");
			Intent ii3 = new Intent(Intent.ACTION_VIEW,
					Uri.parse(uri));
			startActivity(ii3);			
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/*
	 * Do a check to see if the map object (mMap) has already been created. If
	 * not, then we have to prepare for displaying it, and that involves also
	 * "finding initial location" -- which is our location -- and fetching all
	 * of the rail stations in the vicinity. The reason I do this onResume is
	 * that onResume gets called even after a popped up dialog box is present
	 * and then is closed ... which would be the case, say, if the user didn't
	 * have Play Services installed, and was presented with the dialog to
	 * install it.
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
				// mMap.animateCamera(CameraUpdateFactory.zoomTo(mMapZoomLevel));
				mMap.setMyLocationEnabled(true);
				mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
					@Override
					public void onCameraChange(CameraPosition arg0) {
						if(mIveAnimated) {
							SharedPreferences settings = getSharedPreferences(
									getPREFS_NAME(), MODE_PRIVATE);
							SharedPreferences.Editor editor = settings.edit();
							float zoom=arg0.zoom;
							if(zoom<10) {
								zoom=13;
							}
							editor.putFloat("zoom", zoom);
							editor.putFloat("bearing", arg0.bearing);
							editor.putFloat("tilt", arg0.tilt);
							editor.putFloat("whereiamatlat",
									(float) arg0.target.latitude);
							editor.putFloat("whereiamatlng",
									(float) arg0.target.longitude);
							editor.commit();
						}
					}
				});
			}
		} else {
			// mMap.animateCamera(CameraUpdateFactory.zoomTo(mMapZoomLevel));
			mMap.setMyLocationEnabled(true);
		}
	}

	public HomeManager getHomeManager() {
		if (mHomeManager == null) {
			mHomeManager = new HomeManager(this);
		}
		return mHomeManager;
	}

	private void animateCamera(double latitude, double longitude) {
		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),
				MODE_PRIVATE);
		float zoom=settings.getFloat("zoom", DEFAULT_ZOOM);
		mMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(new LatLng(latitude,
						longitude), zoom,
						settings.getFloat("tilt", DEFAULT_TILT), settings
								.getFloat("bearing", DEFAULT_BEARING))));
		mIveAnimated=true;
	}

	public void positionMapToLocation(double latitude, double longitude) {
		if (mMap != null) {
			animateCamera(latitude, longitude);
		}
	}

	@Override
	public void heresYourAddress(Address address, String readableAddress,
			LatLng whereImAt) {

		if (address == null) {
			/*
			 * whereImAt!=null means that we've just initialized and are placing
			 * the map at this spot
			 */
			if (whereImAt != null) {
				SharedPreferences settings = getSharedPreferences(
						getPREFS_NAME(), MODE_PRIVATE);
				positionMapToLocation(settings.getFloat("whereimatlat",
						(float) whereImAt.latitude), settings.getFloat(
						"whereimatlng", (float) whereImAt.longitude));
			}
			setControlState(false,null);
		} else {
			positionMapToLocation(address.getLatitude(), address.getLongitude());
			setControlState(true,readableAddress);
			SharedPreferences settings = getSharedPreferences(
					getPREFS_NAME(), MODE_PRIVATE);
			Editor editor=settings.edit();
			editor.putString("KEY_ReadableAddress", readableAddress);
			editor.commit();
		}
		if (needToBringUpSplashScreen) {
			needToBringUpSplashScreen = false;
			new WarningAndInitialDialog("Thank you for using Commuter Alert!",
					SPLASH_SCREEN_MESSAGE, Home2.this).show();
		}
	}

	private void setControlState(boolean isArmed,String readableAddress) {
		// Hide the previous pin; otherwise they just continue to accumulate.
		if (mPreviousMarker != null && mPreviousMarker.isVisible()) {
			mPreviousMarker.setVisible(false);
		}
		final Button disarmButton = (Button) findViewById(R.id.buttonSearch);
		final CompoundButton armedButton = (CompoundButton) findViewById(R.id.switchArmed);
		final TextView currentLocation=(TextView)findViewById(R.id.tvCurrentLocation2);
		if (isArmed) {
			armedButton.setChecked(true);
			disarmButton.setVisibility(View.GONE);
			currentLocation.setVisibility(View.VISIBLE);
			if(currentLocation.getText().equals("")) {
				currentLocation.setText(readableAddress.replaceAll("\n", " "));
			}
			currentLocation.setSelected(true);
		} else {
			currentLocation.setText("");
			armedButton.setChecked(false);
			disarmButton.setVisibility(View.VISIBLE);
			currentLocation.setVisibility(View.INVISIBLE);
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
	 * I am using an AsyncTask here because its onPostExecute insures that I can
	 * update the UI; and as this whole thing is initiated via a background
	 * thread (we have to make a web call to Google Services in order to fetch
	 * the train stations, and such calls are not allowed by Android to be made
	 * in the UI thread), we will need to "get back on" the UI thread in order
	 * to display it.
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
		 * The class LocationAndAssociatedTrainStations is a combination of two
		 * objects -- the list of addresses of the trains stations, and the
		 * Location object defining where I am at currently. Due to the fact
		 * that the AsyncTask class can only handle one object passed into it, I
		 * was constrained to create a class that holds both of these pieces of
		 * information; both of which are required by this method.
		 */
		protected void onPostExecute(LocationAndAssociatedTrainStations result) {
			final LocationAndAssociatedTrainStations resultF = result;
			if (result != null) {
				// position map to location only if we're not armed
				SharedPreferences settings = getSharedPreferences(
						getPREFS_NAME(), MODE_PRIVATE);
				if (Double.valueOf(settings.getString("latitude", "0")) == 0) {
					positionMapToLocation(result.mLocation.getLatitude(),
							result.mLocation.getLongitude());
				}
				// turn on the little "take me to my current location" icon
				mMap.setMyLocationEnabled(true);
				// now create Markers for all of the trains.
				BitmapDescriptor bmd = BitmapDescriptorFactory
						.fromResource(R.drawable.train4_transparent);
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
				// 1. Moves the marker (hides the old one and creates the new
				// one)
				// 2. If he touches near the train, assume he meant to touch the
				// train.
				// 3. Initiate what needs to be done to arm the system.
				mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
					public void onMapLongClick(LatLng point) {
						/*
						 * If a trial version, and has exceeded the number of
						 * trials
						 */
						if (!getHomeManager().getSecurityManager()
								.doTrialCheck()) {
							return;
						}
						LatLng useThisOne = null;
						if (mPreviousMarker != null) {
							mPreviousMarker.setVisible(false);
						}
						/*
						 * Do a "snap-to-grid" kind of thing. If the guy's
						 * pressing near the train, let's snap him to the train.
						 */

						float zoomLevel = mMap.getCameraPosition().zoom;
						float errorMarginMetersUnderWhichWeAssumeHePressedTheTrain = 0f;
						errorMarginMetersUnderWhichWeAssumeHePressedTheTrain = (float) (SOMEKINDOFFACTOR * (1f / (Math
								.pow(2f, (zoomLevel - 12f)))));
						Address useThisAddress = null;
						// Drop a train bitmap as a marker at each place on the
						// map
						// If he's near a train, then assume he meant to press
						// the train.

						boolean gotRRStation = false;
						for (int i = 0; i < resultF.mAddresses.size(); i++) {
							LatLng latlng2 = new LatLng(resultF.mAddresses.get(
									i).getLatitude(), resultF.mAddresses.get(i)
									.getLongitude());
							float[] results = new float[3];
							Location.distanceBetween(point.latitude,
									point.longitude, latlng2.latitude,
									latlng2.longitude, results);
							if (results[0] < errorMarginMetersUnderWhichWeAssumeHePressedTheTrain) {
								useThisOne = latlng2;
								useThisAddress = resultF.mAddresses.get(i);
								gotRRStation = true;
								break;
							}
						}
						/*
						 * All we're given is a point (latitude and longitude).
						 * Is there an address near it so we can use that
						 * description?
						 */
						try {
							if (useThisAddress == null) {
								Geocoder g = new Geocoder(Home2.this);
								List<Address> addresses = g.getFromLocation(
										(double) point.latitude,
										(double) point.longitude, 2);
								if (addresses != null && addresses.size() > 0) {
									useThisAddress = addresses.get(0);
									/*
									 * Even though we've found a good
									 * displayable name, we still want to drop
									 * the pin in the exact right place.
									 */
									useThisAddress.setLatitude(point.latitude);
									useThisAddress
											.setLongitude(point.longitude);
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
						// ask if user wants to give a nickname
						if (!gotRRStation) {
							new NickNameDialog(Home2.this, useThisAddress)
									.show();
						} else {
							// arm the system
							armTheSystem(useThisAddress);
						}
					}
				});
			}
		}
	}

	public void armTheSystem(Address useThisAddress) {
		getHomeManager().getDbAdapter().writeOrUpdateHistory(useThisAddress);
		getHomeManager().newLocation(useThisAddress);
	}

	@Override
	public void dropPin(Address a) {
		if (mMap != null) {
			LatLng latlng = new LatLng(a.getLatitude(), a.getLongitude());
			Marker marker = mMap.addMarker(new MarkerOptions().position(latlng)
					.title("Here's your destination")
					.snippet("You will be notified when you are near it"));
			marker.showInfoWindow();
			mPreviousMarker = marker;
		}
	}

	/*
	 * This is what the Model calls; but I have to initiate an AsyncTask,
	 * because we're going to be updating in a non-UI thread
	 */
	public void hereAreTheTrainStationAddresses(ArrayList<Address> addresses,
			Location location) {
		LocationAndAssociatedTrainStations t = new LocationAndAssociatedTrainStations(
				location, addresses);
		new ShowMap().execute(t);
	}

	public String getPREFS_NAME() {
		return getApplicationContext().getPackageName() + "_preferences";
	}

	/**
	 * Define a Broadcast receiver that receives updates from connection
	 * listeners and the geofence transition service.
	 */
	public class MyBroadcastReceiver extends BroadcastReceiver {
		/*
		 * Define the required method for broadcast receivers This method is
		 * invoked when a broadcast Intent triggers the receiver
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			/*
			 * Check to see if they've exceeded trials
			 */
			// Check the action code and determine what to do
			String action = intent.getAction();

			if (TextUtils
					.equals(action, ACTION_HERES_AN_STREET_ADDRESS_TO_SEEK)) {
				if(getHomeManager().mPreventReentry==0) {
					getHomeManager();
					HomeManager.mPreventReentry++;
					getHomeManager().manageKeyedInAddress(
						intent.getStringExtra("SeekAddressString"));
				}
			}
		}
	}

	public static class NickNameDialog {
		private Activity mActivity;
		private Address mAddress;

		private NickNameDialog() {
			super();
		}

		public NickNameDialog(Activity activity, Address address) {
			mActivity = activity;
			mAddress = address;
		}

		public void show() {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(mActivity,
							R.style.AlertDialogCustomLight));
			LayoutInflater inflater = mActivity.getLayoutInflater();

			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			final View view=inflater.inflate(R.layout.nickname, null);
			final EditText nickName = (EditText) view.findViewById(R.id.nickname);
			nickName.setText(mAddress.getAddressLine(0));
			builder.setView(view);
			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							
							if(!nickName.getText().toString().trim().equals("")) {
								mAddress.setAddressLine(0, nickName.getText()
										.toString());
							}
							((Home2) mActivity).armTheSystem(mAddress);
						}
					}).setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							((Home2) mActivity).armTheSystem(mAddress);
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	public static class WarningAndInitialDialog {
		private String mTitle;
		private String mMessage;
		private Activity mActivity;

		private WarningAndInitialDialog() {
			super();
		}

		public WarningAndInitialDialog(String title, String message,
				Activity activity) {
			super();
			mTitle = title;
			mMessage = message;
			mActivity = activity;
		}

		public void show() {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(mActivity,
							R.style.AlertDialogCustomLight));
			builder.setTitle(mTitle)
					.setPositiveButton("Okay",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
								}
							}).setMessage(mMessage);
			// Create the AlertDialog object and return it

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

}
