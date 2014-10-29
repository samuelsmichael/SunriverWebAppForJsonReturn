package com.diamondsoftware.android.commuterhelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.diamondsoftware.android.commuterhelper.Home2.NickNameDialog;
import com.diamondsoftware.android.commuterhelper.SearchActivity.SearchRailroadStationsDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class HomeManager implements
			GooglePlayServicesClient.ConnectionCallbacks,
			GooglePlayServicesClient.OnConnectionFailedListener {
	private Activity mActivity = null;
	private DbAdapter mDbAdapter = null;
	private LocationManager mLocationManager = null;
    private LocationClient mLocationClient;
    private MyBroadcastReceiver mBroadcastReceiver;
    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;
    public static final String ACTION_HERES_AN_ADDRESS_TO_ARM="ADDRESS_TO_ARM";
	private SecurityManager mSecurityManager = null;
    

	public static final int LIMIT_NBR_ACCESSES = 50;
	public static final String GOOGLE_API_KEY = "AIzaSyCiLgS6F41lPD-aHj7yMycVDv38gb1vd2o";
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	public static final float CLOSE_TO_RADIUS_IN_METERS = 49500;
	public static final float CLOSE_TO_RADIUS_IN_METERS_FOR_SEARCH =100000;
	

	/*
	 * Public Interface
	 * ----------------------------------------------------------
	 * ------------------
	 */
	
	private HomeManager() { // We don't want any empty contructors;
		
	}
	
	private Intent getLocationManagerIntent() {
		return new Intent(mActivity,LocationServiceModern.class);
/*
		SharedPreferences settings = mActivity.getSharedPreferences(getPREFS_NAME(),Context.MODE_PRIVATE);
		String locationManager = settings.getString("locationmanager","gps");
		if(locationManager.equals("gps")) {
			return 	new Intent(mActivity, LocationServiceOriginalEnhanced.class);
		} else {
			if(locationManager.equals("networklocation")) {
				return new Intent(mActivity,LocationServiceGeofencing.class);
			} else {
				return 	new Intent(mActivity, LocationServiceOriginal.class);				
			}
		}
*/		
	}
	public String getPREFS_NAME() {
		return mActivity.getPackageName() + "_preferences";
	}	
	public HomeManager(Activity activity) {
		mActivity=activity;
	}
	
	public void close() {
		if (mDbAdapter != null) {
			try {
				mDbAdapter.close();
			} catch (Exception eee) {
			}
		}
	}
	public void manageKeyedInAddress(String locationAddress) {
		try {
			locationAddress = locationAddress.replace("\n", " ");
			new RetrieveAddressData().execute(locationAddress);
		} catch (Exception e) {
			Toast.makeText(
					mActivity.getApplicationContext(),
					"Problem tyring to get address. Msg:"
							+ e.getMessage()
							+ ". This problem often goes away by pushing the button again.",
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * This method is called by the View (Home) whenever it gets brought into the fore-front, either
	 * when it first starts up (onResume), or when it gets "re-paged" back into memory (onRestart).
	 * It restores the system (based on the data in the Model).  If the Model shows that the
	 * system is armed (latitude not equal 0), then it makes sure that the 
	 * LocationService service is started and that the View displays the correct information; otherwise,
	 * it stops the service, and updates the View accordingly.  It also initializes the "modifyingvalue" to its 
	 * lowest value.  This piece of information (along with the dynamically set MTIMEOALARMINTENSECONDINTERVALS)
	 * is used in order to regulate the frequency of calls being made to the GPS satellite. 
	 * (This is the mechanism by which I minimize the battery drainage). If I ascertain that we're not moving, 
	 * then this frequency is low; otherwise, it's high, as more frequent updates to the location are required.
	 */
	public void initialize(LatLng whereImAt) {
		Intent intent = getLocationManagerIntent()
				.setAction("JustInitializeLocationManager");

		mActivity.startService(intent);
		Geocoder g = new Geocoder(mActivity);
		SharedPreferences settings = mActivity.getSharedPreferences(getPREFS_NAME(),Context.MODE_PRIVATE);
		
		if(mBroadcastReceiver!=null) {
			LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mBroadcastReceiver);
		}
		
        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new MyBroadcastReceiver();
        
        // Create an intent filter for the broadcast receiver
        
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);
        // Action for broadcast Intents to arm the address
        mIntentFilter.addAction(ACTION_HERES_AN_ADDRESS_TO_ARM);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        
        // Register the broadcast receiver to receive status updates
        
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mBroadcastReceiver, mIntentFilter);
		

		double latitude = Double.valueOf(settings.getString("latitude", "0"));
		double longitude = Double.valueOf(settings.getString("longitude", "0"));
		
		// Inform the View so it can adjust the display. Why not do this
		// inside of the View object itself?  The reason is because we want
		// to keep the View as de-coupled as possible from the Controller.
		// this leads to better extensibility.
		if (latitude != 0) {
			String addressDescription=settings.getString("locationString", "");
			Address address=new Address(Locale.getDefault());
			address.setLatitude(latitude);
			address.setLongitude(longitude);
			address.setAddressLine(0, addressDescription);
			try {
				List<Address> addresses = g.getFromLocation( latitude,
						 longitude, 8);
				if (addresses != null && addresses.size() > 0) {
					addresses.get(0).setLatitude(latitude);
					addresses.get(0).setLongitude(longitude);
					((HomeImplementer)mActivity).positionMapToLocation(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
					newLocation(addresses.get(0));
				}
			} catch (IOException e) {
			}
		} else {
			((HomeImplementer) mActivity).heresYourAddress(null, null,whereImAt);
		}
	}

	/*
	 * The system is disarmed either because the user specifically said to by pressing the
	 * "disarm" button; or when the alert occurs.  The "model" (which only consists of
	 * three pieces of data -- latitude, longitude, and description of the target location)
	 * is cleared when the system is disarmed.
	 * 
	 */
	public void disarmLocationService() {
		SharedPreferences settings = mActivity.getSharedPreferences(getPREFS_NAME(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("latitude", "0");
		editor.putString("longitude", "0");
		editor.putString("locationString", "");
		editor.commit();

		// I try to maintain only a single Android LocationManager, which is 
		// in the class LocationService; so I need to tell LocationService to turn off,
		// its requests to Android to give it location updates so as to conserve the battery.
		Intent intent = getLocationManagerIntent()
				.setAction("JustDisarm");
		mActivity.startService(intent);
	}
	public DbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new DbAdapter(mActivity);
		}
		return mDbAdapter;
	}	
	/*
	 * Private Interface
	 * --------------------------------------------------------
	 * --------------------
	 */

	private void armLocationService(Address a) {
		
		SharedPreferences settings = mActivity.getSharedPreferences(getPREFS_NAME(),Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("latitude", String.valueOf(a.getLatitude()));
		editor.putString("longitude", String.valueOf(a.getLongitude()));
		editor.putString("locationString", getReadableFormOfAddress(a));
		editor.commit();

	}

	private String getReadableFormOfAddress(Address address) {
		int maxIndex = address.getMaxAddressLineIndex();
		StringBuilder sb = new StringBuilder();
		String nl = "";
		for (int c = 0; c <= maxIndex; c++) {
			try {
				sb.append(nl + address.getAddressLine(c));
				nl = "\n";
			} catch (Exception e) {
			}
		}
		return sb.toString();
	}

	private List<Address> getFromLocationNameUsingGeocoder(String streetAddress)
			throws IOException {
		try {
		String addressTextText = null;
		List<Address> addressList = null;
		addressTextText = streetAddress;
		addressTextText = addressTextText.replace("\n", " ");
		Geocoder g = new Geocoder(HomeManager.this.mActivity);
		addressList = g.getFromLocationName(addressTextText, 4);
		return addressList;
		} catch (Exception e) {
			return new ArrayList<Address>();
		}
	}
	private List<Address> getFromLocationNameUsingCallToGoogle(
			String streetAddress) throws Exception {
		List<Address> addressList = new ArrayList<Address>();
		try {
			streetAddress = URLEncoder.encode(streetAddress, "UTF-8");
			String url = "http://maps.googleapis.com/maps/api/geocode/json?address="
					+ streetAddress + "&sensor=true";
			
			
            HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?address=" +URLEncoder.encode(streetAddress, "UTF-8")+"&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder sb = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    sb.append((char) b);
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }			
			
	/*		
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.connect();
			OutputStream out = conn.getOutputStream();
			PrintWriter pw = new PrintWriter(out);
			pw.close();
			InputStream is = conn.getInputStream();
			InputStreamReader is2 = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(is2);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			*/
			String json = sb.toString();
			JSONObject jObj = new JSONObject(json);
			JSONArray results = jObj.getJSONArray("results"); 
			String status = jObj.getString("status");
			if (!status.equals( "OVER_QUERY_LIMIT")) {
				for (int i = 0; i < results.length(); i++) {
					JSONObject addressObject = results.getJSONObject(i);
					String formattedAddress = addressObject
							.getString("formatted_address");
					JSONObject geometry = addressObject.getJSONObject("geometry");
					JSONObject location = geometry.getJSONObject("location");
					String lat = location.getString("lat");
					String lng = location.getString("lng");
					/*
					 * All I need is a string representation of the address, along with
					 * its longitude and latitude, so I'm not going to bother breaking
					 * into city, state, zip components.
					 */
					Address address = new Address(Locale.getDefault());
					address.setLatitude(Double.valueOf(lat));
					address.setLongitude(Double.valueOf(lng));
					address.setAddressLine(0, formattedAddress);
					addressList.add(address);
				}
			}
/*			try {
				is2.close();
			} catch (Exception eieiee) {
			} 
*/
		} catch (Exception e3) {
			int bkhere=3;
			int x=bkhere;
		}
		return addressList;
	}

	
	public void getTrainStationsNear(Location location,
			ArrayList<Address> runningList, String nextPageToken,
			int nbrOfAccessesLeft, int nthAccessStartingAt1) throws Exception {
		String rememberLastGoodNextPageToken=null;
		int countRetries=0;
		int priorRunningListCount=0;
		boolean doWriteStationsToCache=true;
		while (true) {
			nextPageToken=getTrainStationsNearPrivate(location,runningList,nextPageToken,nbrOfAccessesLeft,nthAccessStartingAt1);
			if(nextPageToken!=null&&nextPageToken.equalsIgnoreCase("didcache")) {
				nextPageToken=null;
				doWriteStationsToCache=false;
			}
			nbrOfAccessesLeft--;
			nthAccessStartingAt1++;
			/* for some reason, Google returns nothing if called to quickly with "nextpage"*/
			if(priorRunningListCount==runningList.size() &&
					runningList.size()>0 && rememberLastGoodNextPageToken!=null && countRetries<2) {
				countRetries++;
				nextPageToken=rememberLastGoodNextPageToken;
				Thread.currentThread().sleep(2500);
				

			} else {
				countRetries=0;
				if(nextPageToken!=null) {
					rememberLastGoodNextPageToken=nextPageToken;
				}
				priorRunningListCount=runningList.size();
				if(nextPageToken==null || nbrOfAccessesLeft<=0) {
					if(doWriteStationsToCache) {
						getDbAdapter().createCacheItem(location, runningList);
					}
					break;
				}
			}
		}
	}
	
	private String getTrainStationsNearPrivate(Location location,
			ArrayList<Address> runningList, String nextPageToken,
			int nbrOfAccessesLeft, int nthAccessStartingAt1) throws Exception { 
		if(nthAccessStartingAt1==1) {
			// try the cache first
			getDbAdapter().getStationsCloseTo(location, CLOSE_TO_RADIUS_IN_METERS, runningList);
			if (runningList.size()>0) {
				return "didcache";  
			}
		}
		
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
				+ location.getLatitude()
				+ ","
				+ location.getLongitude()
				+ "&pagetoken="
				+ (nextPageToken == null ? "" : nextPageToken)
				+ "&radius=50000&types=train_station|subway_station|transit_station&sensor=true&key="
				+ GOOGLE_API_KEY;
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.connect();
		OutputStream out = conn.getOutputStream();
		PrintWriter pw = new PrintWriter(out);
		pw.close();
		InputStream is = conn.getInputStream();
		InputStreamReader is2 = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(is2);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		String json = sb.toString();
		JSONObject jObj = new JSONObject(json);
		nextPageToken = null;
		try {
			String status = jObj.getString("status");
			if (status == "OVER_QUERY_LIMIT") {
				Thread.currentThread().sleep(1000);
				nbrOfAccessesLeft--;
				nthAccessStartingAt1++;
				return nextPageToken;
			}
		} catch (Exception e) {
		}
		try {
			nextPageToken = jObj.getString("next_page_token");
		} catch (Exception e) {
		}
		JSONArray results = jObj.getJSONArray("results");
		for (int i = 0; i < results.length(); i++) {
			JSONObject geometry = ((JSONObject) results.get(i))
					.getJSONObject("geometry");
			JSONObject location2 = geometry.getJSONObject("location");
			String lat = location2.getString("lat");
			String lng = location2.getString("lng");
			String name = ((JSONObject) results.get(i)).getString("name");
			Address address = new Address(Locale.getDefault());
			address.setLatitude(Double.valueOf(lat));
			address.setLongitude(Double.valueOf(lng));
			address.setAddressLine(0, name);
			runningList.add(address);
		}
		if (nextPageToken != null && nbrOfAccessesLeft > 1) {
	//		Thread.currentThread().sleep(2500);
			return nextPageToken;
		} else {
			return null;
		} 
	}

	public class LocationAndWantsSurroundingTrainStations {
		public Location mLocation;
		public WantsSurroundingTrainStations mClient;
	}
	
	/*  All web fetches have to be done on a background thread. (See
	 * https://developer.android.com/training/multiple-threads
	 * /communicate-ui.html for a discussion about this.)  The "AsyncTask" 
	 * object manages this.
	 */

	
	public class RetrieveAddressDataForMap extends
			AsyncTask<LocationAndWantsSurroundingTrainStations, Void, List<Address>> {
		private Location mLocation = null;
		private WantsSurroundingTrainStations mClient=null;

		protected List<Address> doInBackground(LocationAndWantsSurroundingTrainStations... locationsParm) {
			if (locationsParm != null) {
				mLocation = locationsParm[0].mLocation;
				mClient=locationsParm[0].mClient;
				ArrayList<Address> trainStationAddresses = new ArrayList<Address>();
				try {
					getTrainStationsNear(mLocation, trainStationAddresses,
							null, LIMIT_NBR_ACCESSES,1);
					Address sa=new Address(Locale.getDefault());
					sa.setLatitude(40.655593210761204);
					sa.setLongitude(-74.30356130003929);
					sa.setAddressLine(0, "Cranford Station");
					trainStationAddresses.add(sa); 
				} catch (Exception e) {
					e.printStackTrace();
				}
				return trainStationAddresses;
			} else {
				return null;
			}
		}

		protected void onPostExecute(List<Address> result) {
			if (mLocation != null && result != null) {

				mClient.hereAreTheTrainStationAddresses(
								(ArrayList) result, mLocation);
			}
		}
	}

	public class RetrieveAllAddressesForSearch extends
			AsyncTask<LocationAndWantsSurroundingTrainStations, Void, List<Address>> {
		private Location mLocation = null;
		private WantsSurroundingTrainStations mClient=null;
		
		protected List<Address> doInBackground(LocationAndWantsSurroundingTrainStations... locationsParm) {
			if (locationsParm != null) {
				mLocation = locationsParm[0].mLocation;
				mClient=locationsParm[0].mClient;
				ArrayList<Address> trainStationAddresses = new ArrayList<Address>();
				try {
					getTrainStationsNear(mLocation, trainStationAddresses,
							null, LIMIT_NBR_ACCESSES,1);
					getDbAdapter().getStationsCloseTo(mLocation, CLOSE_TO_RADIUS_IN_METERS_FOR_SEARCH, trainStationAddresses);

		/*				Address sa=new Address(Locale.getDefault());
					sa.setLatitude(40.655593210761204);
					sa.setLongitude(-74.30356130003929);
					sa.setAddressLine(0, "Cranford Station");
					trainStationAddresses.add(sa); */
				} catch (Exception e) {
					e.printStackTrace();
				}
				return trainStationAddresses;
			} else {
				return null;
			}
		}
		
		protected void onPostExecute(List<Address> result) {
			if (mLocation != null && result != null) {
		
				mClient.hereAreTheTrainStationAddresses(
								(ArrayList) result, mLocation);
			}
		}
}

	
	/*  All web fetches have to be done on a background thread. (See
	 * https://developer.android.com/training/multiple-threads
	 * /communicate-ui.html for a discussion about this.)  The "AsyncTask" 
	 * object manages this.
	 */
	public static int mPreventReentry=0;
	class RetrieveAddressData extends AsyncTask<String, Void, List<Address>> {
		private String exceptionMessage = null;

		protected List<Address> doInBackground(String... addressesParm) {
			try {
				String addressTextText = addressesParm[0];
				List<Address> addressList = null;
				try {
					addressList = getFromLocationNameUsingGeocoder(addressTextText);
				} catch (IOException e) {
					exceptionMessage = e.getMessage();
				}
				if (addressList == null || addressList.size() == 0) {
					try {
						addressList = getFromLocationNameUsingCallToGoogle(addressTextText);
					} catch (Exception e) {
						if (exceptionMessage == null) {
							exceptionMessage = e.getMessage();
						} else {
							exceptionMessage += " and " + e.getMessage();
						}
					}
				}
				return addressList;
			} catch (Exception e3) {
				exceptionMessage=e3.getMessage();
				return null;
			}
		}

		protected void onPostExecute(List<Address> result) {
			// TODO: check this.exception
			// TODO: do something with the feed
			try {
				final List<Address> addressList = result;
				if (addressList != null && addressList.size() > 0) {
					if (addressList.size() > 1) { // Popup a display so the user can
													// choose which he wants.
						final String[] addresses = new String[addressList.size()];
						final double[] latitudes = new double[addressList.size()];
						final double[] longitudes = new double[addressList.size()];
						for (int i = 0; i < addressList.size(); i++) {
	
							int maxIndex = addressList.get(i)
									.getMaxAddressLineIndex();
							addresses[i] = getReadableFormOfAddress(addressList.get(i));
							latitudes[i] = addressList.get(i).getLatitude();
							longitudes[i] = addressList.get(i).getLongitude();
						}
						new ManyAddressHits(addresses, latitudes, longitudes, mActivity, addressList, HomeManager.this).show(mActivity.getFragmentManager(),"Addresses");
		/*				AlertDialog.Builder builder = new AlertDialog.Builder(
								mActivity);
						builder.setTitle("More than one location found. Pick one.");
						builder.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Toast.makeText(
												mActivity.getApplicationContext(),
												"No selection made..",
												Toast.LENGTH_SHORT).show();
										dialog.dismiss();
									}
								});

						builder.setItems(addresses, 
								new DialogInterface.OnClickListener() {
	
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Address a = addressList.get(which);
										// Write address to history
										dialog.dismiss();
										try {
											HomeManager.this.newLocationButFirstPrompt(a);
										} catch (Exception e) {
											((Home2) mActivity).armTheSystem(a,false);
										}
									}
								});
						AlertDialog alert = builder.create();
						alert.show();
						*/
					} else {
						Address a = addressList.get(0);
						/* Write address to history*/
						if(Home2.mSingleton==null || !Home2.mSingleton.doTrialCheck()) {
							return;
						}
						try {
							newLocationButFirstPrompt(a);
						} catch (Exception e) {
							((Home2) mActivity).armTheSystem(a,false);
						}
					}
				} else {
					new  Home2.NoAddressFoundWarning("No Address Found", 
							exceptionMessage==null?"Please try a more succinct address. Note also, that if you're in a Wifi area, sometimes better results can be achieved when Wifi is on.":"The address finding mechanism failed with the following message: "+exceptionMessage,
							mActivity).show(mActivity.getFragmentManager(),"NoneFound1");
				}
			}  catch (Exception ee) {
				new  Home2.NoAddressFoundWarning("No Address Found", 
						ee.getMessage()==null?"Please try a more succinct address. Note also, that if you're in a Wifi area, sometimes better results can be achieved when Wifi is on.":"The address finding mechanism failed with the following message: "+ee.getMessage()+" Please try again.",
						mActivity).show(mActivity.getFragmentManager(),"NoneFound1");
				
			} finally {
				mPreventReentry--;
			}
		}
	}

	private Address deriveAddressFromElement(Element elem) {
		Address address = new Address(Locale.getDefault());
		String lat = getTextFromElement(elem, "Latitude");
		address.setLatitude(Double.valueOf(lat));
		address.setLongitude(Double.valueOf(getTextFromElement(elem,
				"Longitude")));
		address.setAddressLine(0, getTextFromElement(elem, "Address"));
		address.setAdminArea(getTextFromElement(elem, "State"));
		address.setLocality(getTextFromElement(elem, "City"));
		address.setCountryCode(getTextFromElement(elem, "Country"));
		return address;
	}

	private String getTextFromElement(Element elem, String name) {
		try {
			NodeList nl = elem.getElementsByTagName(name);
			Element resultaddress = (Element) nl.item(0);
			resultaddress.normalize();
			NodeList array = resultaddress.getChildNodes();
			return array.item(0).getNodeValue();
		} catch (Exception ee33d) {
			return "";
		}
	}
	
	private LocationManager getLocationManager() {
		if (mLocationManager == null) {
			mLocationManager = (android.location.LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}	
	
	private void newLocationButFirstPrompt(Address a) {
		new NickNameDialog(mActivity, a)
		.show();
	}
	
	public void newLocation(Address a) {
		((HomeImplementer) mActivity).heresYourAddress(a,
				getReadableFormOfAddress(a),null);
		armLocationService(a);
		Intent jdItent2 = getLocationManagerIntent()
				.putExtra("LocationAddress", getReadableFormOfAddress(a));
		mActivity.startService(jdItent2);
		((HomeImplementer) mActivity).dropPin(a);
	}


	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return getLocationManager().getBestProvider(criteria, false);
	}

	/*
	 * I've got two methods of managing locations:
	 * 		- Google's Play Services Location API, which is very robust and reliable, but requires the phone have Network Location on.
	 *      - GPS, which is also robust and reliable, but requires the phone have GPS on.
	 * This section of code is for ascertaining the type.  Also, once this is done, then we can initialize the map with the
	 * trains.
	 */
	public void ascertainLocationMethod(Activity activity) {
		mActivity=activity;
        mLocationClient = new LocationClient(mActivity, this, this);		
        mLocationClient.connect();
	}

	
	/* 
	 * Here is where we're going to request our list of trains from.
	 */
	private void getCurrentLocation() {
		SharedPreferences settings = mActivity.getSharedPreferences(getPREFS_NAME(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		Location location=mLocationClient.getLastLocation();
		boolean gpsIsEnabled=false;
	    if ( getLocationManager().isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        gpsIsEnabled=true;
	    }
		
		if(location!=null) {
			// simulate Scott's address
///			location.setLatitude(40.658421);
	//		location.setLongitude(-74.29959);
			//

			editor.putString("locationmanager", "networklocation");
			editor.commit();
			initialize(new LatLng(location.getLatitude(), location.getLongitude()));
			LocationAndWantsSurroundingTrainStations client=new LocationAndWantsSurroundingTrainStations();
			client.mClient=(WantsSurroundingTrainStations)mActivity;
			client.mLocation=location;
			new RetrieveAddressDataForMap()
			.execute(client);
		} else {
			if(gpsIsEnabled) {
				editor.putString("locationmanager", "gps");
				editor.commit();
			
				String provider=getProvider();
		        if(provider==null) {
		        	provider=LocationManager.GPS_PROVIDER;
		        }
		        if(getLocationManager().isProviderEnabled(provider)) {
					getLocationManager().requestLocationUpdates(getProvider(), 0, 0, new LocationListener() {
						@Override
						public void onLocationChanged(Location location) {
							// simulate Scott's address
	//						location.setLatitude(40.658421);
		//					location.setLongitude(-74.29959);
							//
							getLocationManager().removeUpdates(this);
							initialize(new LatLng(location.getLatitude(), location.getLongitude()));
							LocationAndWantsSurroundingTrainStations client=new LocationAndWantsSurroundingTrainStations();
							client.mClient=(WantsSurroundingTrainStations)mActivity;
							client.mLocation=location;
							new RetrieveAddressDataForMap()
							.execute(client);					
		
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
		        } else {
		        }
			}
		}
	}
	
	
	@Override
	public void onConnected(Bundle arg0) {
		getCurrentLocation();
        mLocationClient.disconnect();
	}


	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub		
	}	

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        mActivity,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                // TODO check activity result, and try to re-connect  
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
        	((HomeImplementer) mActivity).showPlaystoreAPIErrorDialog(connectionResult.getErrorCode());	
        }
	}	
    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

            // Intent contains information about successful addition or removal of geofences
            } else {
            	if(TextUtils.equals(action, ACTION_HERES_AN_ADDRESS_TO_ARM)) {
            		
            		/*
            		 * Check to see if they've exceeded their trials allowance.
            		 */
            		Address address=new Address(Locale.getDefault());
            		address.setLatitude(intent.getDoubleExtra("latitude", 0));
            		address.setLongitude(intent.getDoubleExtra("longitude", 0));
            		address.setAddressLine(0, intent.getStringExtra("name"));
            		if(address.getLatitude()!=0 && address.getLongitude()!=0) {
            			HomeManager.this.newLocation(address);
            		}
            	}    
            }
        }

        /**
         * Flip back to "gps" mode because "networklocation" isn't working well
         */
        private void handleGeofenceError(Context context, Intent intent) {
    		SharedPreferences settings = mActivity.getSharedPreferences(getPREFS_NAME(), Context.MODE_PRIVATE);
    		SharedPreferences.Editor editor = settings.edit();
			editor.putString("locationmanager", "gps");
			editor.commit();
			double latitude = Double.valueOf(settings.getString("latitude", "0"));
			double longitude = Double.valueOf(settings.getString("longitude", "0"));
			String addressDescription=settings.getString("locationString", "");

			Intent intent3 = getLocationManagerIntent()
					.setAction("JustInitializeLocationManager");

			mActivity.startService(intent3);			
			
			Address a=new Address(Locale.getDefault());
			a.setLatitude(latitude);
			a.setLongitude(longitude);
			a.setAddressLine(0, addressDescription);
			Intent jdItent2 = getLocationManagerIntent()
					.putExtra("LocationAddress", getReadableFormOfAddress(a));
			mActivity.startService(jdItent2);
        }
    }
	public SecurityManager getSecurityManager() {
		if (mSecurityManager == null) {
			mSecurityManager = new SecurityManager(mActivity);
		}
		return mSecurityManager;
	}	
	
    public static class ManyAddressHits extends DialogFragment {
    	private CharSequence[] mItems;
		final String[] mAddresses;
		final double[] mLatitudes;
		final double[] mLongitudes;
		final List<Address> mAddressList;
		final HomeManager mHomeManager;
    	private Activity mActivity=null;
    	private Timer mLocationsTimer2=null;
    	
    	
    	public ManyAddressHits() {
    		super();
    		mAddresses=null;
    		mActivity=null;
    		mLatitudes=null;
    		mLongitudes=null;
    		mAddressList=null;
    		mHomeManager=null;
    	}
		public ManyAddressHits(String[] addresses, double[] latitudes, double[] longitudes, Activity activity, List<Address> addressList, HomeManager homeManager) {
    		super();
    		mAddresses=addresses;
    		mActivity=activity;
    		mLatitudes=latitudes;
    		mLongitudes=longitudes;
    		mAddressList=addressList;
    		mHomeManager=homeManager;
    	}

		private Timer getLocationsTimer2() {
			if (mLocationsTimer2 == null) {
				mLocationsTimer2 = new Timer("Enhanced");
			}
			return mLocationsTimer2;
		}

		
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle("More than one location found. Pick one.")
            
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Toast.makeText(
												mActivity.getApplicationContext(),
												"No selection made..",
												Toast.LENGTH_SHORT).show();
									}
								})

						.setItems(mAddresses, 
								new DialogInterface.OnClickListener() {
	
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										final Address a = mAddressList.get(which);
										dismiss();
										mHomeManager.newLocationButFirstPrompt(a);
									}
								});
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
	
}
