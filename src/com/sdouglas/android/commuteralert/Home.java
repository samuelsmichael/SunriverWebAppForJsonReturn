package com.sdouglas.android.commuteralert;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.sdouglas.android.commuteralert.HomeManager.RetrieveAddressData;

import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity implements HomeImplementer {
	private GoogleMap mMap=null;
	private HomeManager mHomeManager;
	private LocationManager mLocationManager=null;
	private MapFragment mMapFragment;
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	
    private HomeManager getHomeManager() {
    	if(mHomeManager==null) {
    		mHomeManager=new HomeManager();
    	}
    	return mHomeManager;
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Intent intent=getIntent();
        final EditText locationAddress = (EditText) findViewById(R.id.editText);
        final Button deriveFromAddress = (Button) findViewById(R.id.buttonAddress);
        
        deriveFromAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mHomeManager.manageKeyedInAddress(locationAddress.getText().toString());
            }
        });
	}

	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	    	mMapFragment=(MapFragment) getFragmentManager().findFragmentById(R.id.map);
	        mMap = mMapFragment.getMap();
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	            // The Map is verified. It is now safe to manipulate the map.
	        	mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
	        	findInitialLocation();
/*
 *  map:cameraBearing="112.5"
  map:cameraTargetLat="-33.796923"
  map:cameraTargetLng="150.922433"
  map:cameraTilt="30"
  map:cameraZoom="13"
  map:mapType="normal"
  map:uiCompass="false"
  map:uiRotateGestures="true"
  map:uiScrollGestures="false"
  map:uiTiltGestures="true"
  map:uiZoomControls="false"
  map:uiZoomGestures="true"/>
 */
	        }
	    }
	}	
	
	@Override 	
	protected void onResume() {
		 super.onResume();
		 if (checkPlayServices()) {
			setUpMapIfNeeded();
		 }
	     getHomeManager().initialize(Home.this);	     
    }
	
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
	      if (resultCode != RESULT_CANCELED) {
	      }
	  }
	  super.onActivityResult(requestCode, resultCode, data);
	}	
	
	@Override 	
	protected void onRestart()
	{
	   super.onRestart();
		 if (checkPlayServices()) {
			setUpMapIfNeeded();
		 }
		 getHomeManager().initialize(Home.this);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	/* Javadoc
	 * If this method is passed null, this means that there is no location.
	 */
	/*
	 * (non-Javadoc)
	 * @see com.sdouglas.android.commuteralert.HomeImplementer#heresYourAddress(android.location.Address, java.lang.String)
	 * 
	 */
	public void heresYourAddress(Address address, String readableAddress) {
		final Button disarmButton=(Button)findViewById(R.id.btnDisarm);
		if(address!=null) {
			setControlsVisibility(true,readableAddress);
		} else {
			setControlsVisibility(false,"");
		}
        disarmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mHomeManager.disarmLocationService();
    			setControlsVisibility(false,"");
            }
        });
	}
	private void setControlsVisibility(Boolean isArmed, String readableAddress) {
		final TextView currentLocation=	(TextView)findViewById(R.id.tvCurrentLocation);
		final Button disarmButton=(Button)findViewById(R.id.btnDisarm);
		final TextView systemIsArmed = (TextView)findViewById(R.id.tvCurrentViewHeading);
        final EditText locationAddress = (EditText) findViewById(R.id.editText);
        final Button deriveFromAddress = (Button) findViewById(R.id.buttonAddress);
        final TextView systemStatus=(TextView) findViewById(R.id.tvSystemStatus2);
		currentLocation.setText(readableAddress);
		currentLocation.setTextColor(Color.BLUE);
		if(isArmed) {
			disarmButton.setVisibility(View.VISIBLE);
			systemIsArmed.setVisibility(View.VISIBLE);
			locationAddress.setVisibility(View.GONE);
			deriveFromAddress.setVisibility(View.GONE);
			systemStatus.setText("Armed");
			systemStatus.setTextColor(Color.RED);
		} else {
			disarmButton.setVisibility(View.GONE);
			systemIsArmed.setVisibility(View.GONE);
			locationAddress.setVisibility(View.VISIBLE);
			deriveFromAddress.setVisibility(View.VISIBLE);
			systemStatus.setText("Disarmed");
			systemStatus.setTextColor(Color.BLUE);
		}
	}
	private LocationManager getLocationManager() {
		if(mLocationManager==null) {
			mLocationManager=(android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}
	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return getLocationManager().getBestProvider(criteria, false);
	}
	/*
	 * I am using an AsyncTask here because its onPostExecute insures that I can update the UI
	 */
    class ShowMap extends AsyncTask<Location, Void, Location> {
	    protected Location doInBackground(Location... location) {
	    	try {
	    		return location[0];
	    	} catch (Exception e) {
	    		return null;
	    	}
	    }
	    protected void onPostExecute(Location result) {
	    	if(result!=null) {
	        	mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng((float)result.getLatitude(),(float)result.getLongitude())));
	    	}
	    }
    }	
	private void findInitialLocation() {
		String provider=getProvider();
        if(provider==null) {
        	provider=LocationManager.GPS_PROVIDER;
        }		
        if(getLocationManager().isProviderEnabled(provider)) {
			getLocationManager().requestLocationUpdates(getProvider(), 2000, 1, new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					getLocationManager().removeUpdates(this); 
		            new ShowMap().execute(location);
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
}
