package com.sdouglas.android.commuteralert;

import java.util.List;

import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Home extends Activity implements LocationListener {

    private Boolean geocoderIsInitialized=false;
    private LocationManager mLocationManager=null;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
        initializeLocationManager();
        final EditText locationAddress = (EditText) findViewById(R.id.editText);
        final Button deriveFromAddress = (Button) findViewById(R.id.buttonAddress);


        deriveFromAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Address> addressList=null;
                try {
                    String addressTextText=locationAddress.getText().toString();
                    addressTextText=addressTextText.replace("\n"," ");
                    //				addressList = g.getFromLocationName(addressTextText,5);
                    addressList=AddressManager.getFromLocationName(addressTextText, getApplicationContext());
                    if(addressList.size()>0) {
                        if(addressList.size()>1) {
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
                            AlertDialog.Builder builder=new AlertDialog.Builder(Home.this);
                            builder.setTitle("More than one location found. Pick one.");
                            builder.setItems(addresses, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    locationAddress.setText(addresses[which]);
                                    String latitude=String.valueOf(latitudes[which]);
                                    String longitude = String.valueOf(longitudes[which]);
                                }
                            });
                            AlertDialog alert=builder.create();
                            alert.show();
                        } else {
                            Address a=addressList.get(0);
                            int maxIndex=a.getMaxAddressLineIndex();
                            StringBuilder sb=new StringBuilder();
                            String nl="";
                            for(int c=0;c<maxIndex;c++) {
                                sb.append(nl+a.getAddressLine(c));
                                nl="\n";
                            }
                            locationAddress.setText(sb.toString());
                            String latitude=String.valueOf(a.getLatitude());
                            String longitude = String.valueOf(a.getLongitude());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No locations found for this address. Sometimes this occurs when the system isn't through initializing. Try just pushing the search button again, and if this doesn't work, try refining your address.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Problem tyring to get address. Msg:"+e.getMessage() + ". This problem often goes away by pushing the button again.", Toast.LENGTH_LONG).show();
                }
            }
        });
	
	
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
    private LocationManager getLocationManager() {
        if(mLocationManager==null) {
            mLocationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }
    private void initializeLocationManager() {
        if(!geocoderIsInitialized) {
            geocoderIsInitialized=true;
            try {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String bestProvider = getLocationManager().getBestProvider(criteria, false);
                if(getLocationManager().isProviderEnabled(bestProvider)) {
                    getLocationManager().requestLocationUpdates(bestProvider, 20000, 1, this);
                    Location loc=getLocationManager().getLastKnownLocation(bestProvider);
                    int x=4;
                    int y=x;
                }
            } catch (Exception ee3) {
                int x=3;
                int y=x;
            }
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
