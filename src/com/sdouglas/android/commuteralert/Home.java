package com.sdouglas.android.commuteralert;

import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Home extends Activity implements HomeImplementer {
	private HomeManager mHomeManager;
	
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

	@Override 	
	protected void onResume()
	{
	   super.onResume();
       getHomeManager().initialize(Home.this);	}
	@Override 	
	protected void onRestart()
	{
	   super.onRestart();
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
}
