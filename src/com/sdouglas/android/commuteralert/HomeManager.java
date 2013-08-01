package com.sdouglas.android.commuteralert;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HomeManager implements LocationListener {
    private LocationManager mLocationManager=null;
    private Activity mActivity=null;
    public static final String PREFS_NAME = "MyPrefsFile";

    /*
     * Public Interface  ----------------------------------------------------------------------------
     */
	public void manageKeyedInAddress(String locationAddress) {
        try {
            locationAddress=locationAddress.replace("\n"," ");
            final List<Address> addressList=getFromLocationName(locationAddress, mActivity);
            if(addressList.size()>0) {
                if(addressList.size()>1) { // Popup a display so the user can choose which he wants.
                    final String[] addresses=new String[addressList.size()];
                    final double [] latitudes=new double[addressList.size()];
                    final double [] longitudes=new double[addressList.size()];
                    for(int i=0;i<addressList.size();i++) {

                        int maxIndex=addressList.get(i).getMaxAddressLineIndex();
                        StringBuilder sb=new StringBuilder();
                        String nl="";
                        for(int c=0;c<maxIndex;c++) {
                            sb.append(nl+addressList.get(i).getAddressLine(c));
                            nl="\n";
                        }
                        addresses[i]=sb.toString();
                        latitudes[i]=addressList.get(i).getLatitude();
                        longitudes[i]=addressList.get(i).getLongitude();
                    }
                    AlertDialog.Builder builder=new AlertDialog.Builder(mActivity);
                    builder.setTitle("More than one location found. Pick one.");
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	Toast.makeText(mActivity.getApplicationContext(), "No selection made..", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setItems(addresses, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Address a=addressList.get(which);
                            HomeManager.this.newLocation(a);
                        }
                    });
                    AlertDialog alert=builder.create();
                    alert.show();
                } else {
                    Address a=addressList.get(0);
                    newLocation(a);
                }
            } else {
                Toast.makeText(mActivity.getApplicationContext(), "No locations found for this address. Sometimes this occurs when the system isn't through initializing. Try just pushing the search button again, and if this doesn't work, try refining your address.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(mActivity.getApplicationContext(), "Problem tyring to get address. Msg:"+e.getMessage() + ". This problem often goes away by pushing the button again.", Toast.LENGTH_LONG).show();
        }
		
	}
	public void initialize(Activity activity) {
		mActivity=activity;
		initializeLocationManager();
		Geocoder g=new Geocoder(mActivity);
        SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        float latitude = settings.getFloat("latitude", 0);
        float longitude = settings.getFloat("longitude", 0);
        if(latitude!=0) {
        	try {
				List<Address> addresses= g.getFromLocation ((double)latitude, (double) longitude, 8);
				if(addresses != null && addresses.size()>0)
				newLocation(addresses.get(0));
			} catch (IOException e) {
				/*TODO: If this happens a lot do a background thread to obtain Geocoder results. 
				 *  If we're having trouble with this, then the thing to do is to obtain the list on a backround thread.
				 *  But care has to be taken to now try to update the UI in this thread.  
				 *  See https://developer.android.com/training/multiple-threads/communicate-ui.html for a discussion about this.
				 */
			}
        } else {
        	clearLocation();
        }
	}
	public void disarmLocationService() {
    	SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("latitude", (float) 0);
        editor.putFloat("longitude", (float) 0);
        editor.putString("locationString","");

        editor.commit();

	}
	/*
	 * Private Interface ----------------------------------------------------------------------------
	 */
	private void armLocationService(Address a) {
    	SharedPreferences settings = mActivity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("latitude", (float) a.getLatitude());
        editor.putFloat("longitude", (float) a.getLongitude());
        editor.putString("locationString",getReadableFormOfAddress(a));
        editor.commit();
		
	}
	
	
	private String getReadableFormOfAddress(Address address) {
        int maxIndex=address.getMaxAddressLineIndex();
        StringBuilder sb=new StringBuilder();
        String nl="";
        for(int c=0;c<maxIndex;c++) {
            sb.append(nl+address.getAddressLine(c));
            nl="\n";		
        }
        return sb.toString();
	}
	
    private List<Address> getFromLocationName(String address, Context ctx) throws Exception {
        String addressTextText="410 Williams St Denver CO 80209";
        List<Address> addressList=null;
        try {
            addressTextText=address;
            addressTextText=addressTextText.replace("\n"," ");
            int x=4;
            if(x==3) throw new Exception("Try other method");
            Geocoder g=new Geocoder(ctx);
            addressList = g.getFromLocationName(addressTextText,8);
            if(addressList==null || addressList.size()==0) {
                throw new Exception("Try other method");
            }
            return addressList;
        } catch (Exception e) {
            String url="http://local.yahooapis.com/MapsService/V1/geocode?appid=yDSMLAbV34EUyy1AJrHKqbb1gL4A4xvchBWqr4MaNharntRqZTcCfm5Qs.ugfgTyrdoe4eoGxpM-&location=" +
                    addressTextText;
            url = url.replace(" ", "%20");
            URL u = new URL(url);
            HttpURLConnection conn=(HttpURLConnection)u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            OutputStream out=conn.getOutputStream();
            PrintWriter pw=new PrintWriter(out);
            pw.close();
            InputStream is = conn.getInputStream();
            DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
            DocumentBuilder db=dbf.newDocumentBuilder();
            Document doc=db.parse(is);
            doc.getDocumentElement().normalize();
            Element rootElement=doc.getDocumentElement();
            Element elem=(Element)rootElement.getChildNodes().item(0);
            String stotalResultsReturned=rootElement.getAttribute("precision");
            addressList=new java.util.ArrayList<Address>();
            addressList.add(deriveAddressFromElement(rootElement));
            try {is.close();} catch (Exception eieiee) {}
            return addressList;
        }
    }
    private Address deriveAddressFromElement(Element elem) {
        Address address=new Address(Locale.getDefault());
        String lat=getTextFromElement(elem,"Latitude");
        address.setLatitude(Double.valueOf(lat));
        address.setLongitude(Double.valueOf(getTextFromElement(elem,"Longitude")));
        address.setAddressLine(0, getTextFromElement(elem,"Address"));
        address.setAdminArea(getTextFromElement(elem,"State"));
        address.setLocality(getTextFromElement(elem,"City"));
        address.setCountryCode(getTextFromElement(elem,"Country"));
        return address;
    }
    private String getTextFromElement(Element elem,String name) {
        try {
            NodeList nl=elem.getElementsByTagName(name);
            Element resultaddress=(Element) nl.item(0);
            resultaddress.normalize();
            NodeList array=resultaddress.getChildNodes();
            return array.item(0).getNodeValue();
        } catch (Exception ee33d) {
            return "";
        }
    }
    
    private LocationManager getLocationManager() {
        if(mLocationManager==null) {
            mLocationManager = (android.location.LocationManager) mActivity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }
    private void initializeLocationManager() {
        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String bestProvider = getLocationManager().getBestProvider(criteria, false);
            if(getLocationManager().isProviderEnabled(bestProvider)) {
                getLocationManager().requestLocationUpdates(bestProvider, 20000, 1, this);
                getLocationManager().getLastKnownLocation(bestProvider);
            }
        } catch (Exception ee3) {
        }
    }
    
    private void newLocation(Address a) {
    	((HomeImplementer)mActivity).heresYourAddress(a,getReadableFormOfAddress(a));
    	armLocationService(a);
    }
    private void clearLocation() {
    	((HomeImplementer)mActivity).heresYourAddress(null,null);
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
