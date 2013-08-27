package com.sdouglas.android.commuteralert;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

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


public class HomeManager {
	private Activity mActivity = null;
	private DbAdapter mDbAdapter = null;
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final int LIMIT_NBR_ACCESSES = 100;
	public static final String GOOGLE_API_KEY = "AIzaSyCiLgS6F41lPD-aHj7yMycVDv38gb1vd2o";

	public static final float CLOSE_TO_RADIUS_IN_METERS = 1000;

	/*
	 * Public Interface
	 * ----------------------------------------------------------
	 * ------------------
	 */
	
	private HomeManager() { // We don't want any empty contructers;
		
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
	public void initialize() {
		Intent intent = new Intent(mActivity, LocationServiceOriginal.class)
				.setAction("JustInitializeLocationManager");

		mActivity.startService(intent);
		Geocoder g = new Geocoder(mActivity);
		SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("modifyingValue", 1);
		editor.commit();

		float latitude = settings.getFloat("latitude", 0);
		float longitude = settings.getFloat("longitude", 0);
		
		// Inform the View so it can adjust the display. Why not do this
		// inside of the View object itself?  The reason is because we want
		// to keep the View as de-coupled as possible from the Controller.
		// this leads to better extensibility.
		if (latitude != 0) {
			String addressDescription=settings.getString("locationString", "");
			Address address=new Address(Locale.getDefault());
			address.setLatitude(Double.valueOf(latitude));
			address.setLongitude(Double.valueOf(longitude));
			address.setAddressLine(0, addressDescription);
			try {
				List<Address> addresses = g.getFromLocation((double) latitude,
						(double) longitude, 8);
				if (addresses != null && addresses.size() > 0)
					newLocation(addresses.get(0));
			} catch (IOException e) {
			}
		} else {
			((HomeImplementer) mActivity).heresYourAddress(null, null);
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
		SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat("latitude", (float) 0);
		editor.putFloat("longitude", (float) 0);
		editor.putString("locationString", "");
		editor.commit();

		// I try to maintain only a single Android LocationManager, which is 
		// in the class LocationService; so I need to tell LocationService to turn off,
		// its requests to Android to give it location updates so as to conserve the battery.
		Intent intent = new Intent(mActivity, LocationServiceOriginal.class)
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
		SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat("latitude", (float) a.getLatitude());
		editor.putFloat("longitude", (float) a.getLongitude());
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
		String addressTextText = null;
		List<Address> addressList = null;
		addressTextText = streetAddress;
		addressTextText = addressTextText.replace("\n", " ");
		Geocoder g = new Geocoder(HomeManager.this.mActivity);
		addressList = g.getFromLocationName(addressTextText, 8);
		return addressList;
	}
	private List<Address> getFromLocationNameUsingCallToGoogle(
			String streetAddress) throws Exception {
		List<Address> addressList = new ArrayList<Address>();
		streetAddress = URLEncoder.encode(streetAddress, "UTF-8");
		String url = "http://maps.googleapis.com/maps/api/geocode/json?address="
				+ streetAddress + "&sensor=true";
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
		JSONArray results = jObj.getJSONArray("results");
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
		try {
			is2.close();
		} catch (Exception eieiee) {
		}
		return addressList;
	}

	public void getTrainStationsNear(Location location,
			ArrayList<Address> runningList, String nextPageToken,
			int nbrOfAccessesLeft, int nthAccessStartingAt1) throws Exception {
		while (true) {
			nextPageToken=getTrainStationsNearPrivate(location,runningList,nextPageToken,nbrOfAccessesLeft,nthAccessStartingAt1);
			nbrOfAccessesLeft--;
			nthAccessStartingAt1++;
			if(nextPageToken==null) {
				getDbAdapter().createCacheItem(location, runningList);
				break;
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
				return null;  
			}
		}
		
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
				+ location.getLatitude()
				+ ","
				+ location.getLongitude()
				+ "&token="
				+ (nextPageToken == null ? "" : nextPageToken)
				+ "&radius=50000&types=train_station&sensor=true&key="
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
				Thread.currentThread().wait(1000);
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
			return nextPageToken;
		} else {
			return null;
		} 
	}

	/*  All web fetches have to be done on a background thread. (See
	 * https://developer.android.com/training/multiple-threads
	 * /communicate-ui.html for a discussion about this.)  The "AsyncTask" 
	 * object manages this.
	 */

	
	public class RetrieveAddressDataForMap extends
			AsyncTask<Location, Void, List<Address>> {
		private Location mLocation = null;

		protected List<Address> doInBackground(Location... locationsParm) {
			if (locationsParm != null) {
				mLocation = locationsParm[0];
				ArrayList<Address> trainStationAddresses = new ArrayList<Address>();
				try {
					getTrainStationsNear(mLocation, trainStationAddresses,
							null, LIMIT_NBR_ACCESSES,1);
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
				((HomeImplementer) mActivity)
						.heresTheTrainStationAddressesToDisplayOnMap(
								(ArrayList) result, mLocation);
			}
		}
	}

	/*  All web fetches have to be done on a background thread. (See
	 * https://developer.android.com/training/multiple-threads
	 * /communicate-ui.html for a discussion about this.)  The "AsyncTask" 
	 * object manages this.
	 */
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
			} catch (Exception e) {
				return null;
			}
		}

		protected void onPostExecute(List<Address> result) {
			// TODO: check this.exception
			// TODO: do something with the feed
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
						StringBuilder sb = new StringBuilder();
						String nl = "";
						for (int c = 0; c < maxIndex; c++) {
							sb.append(nl + addressList.get(i).getAddressLine(c));
							nl = "\n";
						}
						addresses[i] = sb.toString();
						latitudes[i] = addressList.get(i).getLatitude();
						longitudes[i] = addressList.get(i).getLongitude();
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(
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
								}
							});
					builder.setItems(addresses,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Address a = addressList.get(which);
									HomeManager.this.newLocation(a);
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				} else {
					Address a = addressList.get(0);
					newLocation(a);
				}
			} else {
				if (exceptionMessage == null) {
					Toast.makeText(mActivity.getApplicationContext(),
							"No locations found for this address.",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(mActivity.getApplicationContext(),
							exceptionMessage, Toast.LENGTH_LONG).show();
				}
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

	public void newLocation(Address a) {
		((HomeImplementer) mActivity).heresYourAddress(a,
				getReadableFormOfAddress(a));
		armLocationService(a);
		Intent jdItent2 = new Intent(mActivity, LocationServiceOriginal.class)
				.putExtra("LocationAddress", getReadableFormOfAddress(a));
		mActivity.startService(jdItent2);
		((HomeImplementer) mActivity).dropPin(a);
	}

}
