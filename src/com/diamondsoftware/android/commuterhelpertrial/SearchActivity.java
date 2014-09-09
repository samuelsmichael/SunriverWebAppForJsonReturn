package com.diamondsoftware.android.commuterhelpertrial;

import com.diamondsoftware.android.commuterhelpertrial.R;

import java.util.ArrayList;
import java.util.Locale;

import com.diamondsoftware.android.commuterhelpertrial.HomeManager.LocationAndWantsSurroundingTrainStations;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends FragmentActivity implements WantsSurroundingTrainStations, 
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
    private LocationClient mLocationClient;
	private static final int DIALOG_NICKNAME = 2;
	private long mNickNameDialogId=-100;
	private DbAdapter mDbAdapter=null;
    private MyBroadcastReceiver mBroadcastReceiver;
    private static final String ACTION_HERES_AN_ADDRESS_TO_ARM="ADDRESS_TO_ARM";
    private static final String JUST_FINISH="JUST_FINISH";
    private static final String ACTION_HERES_AN_STREET_ADDRESS_TO_SEEK="ACTION_HERES_AN_STREED_ADDRESS_TO_SEEK";
    
   
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Search");
		setContentView(R.layout.activity_search);
		final EditText intersection=(EditText) findViewById(R.id.searchIntersection);
		final Button addressOrIntersection=(Button) findViewById(R.id.searchButtonAddressOrIntersection);
		final Button trainStations=(Button) findViewById(R.id.searchButtonTrainStations);
		final Button history=(Button) findViewById(R.id.searchButtonHistory);
		final Button back=(Button) findViewById(R.id.searchButtonBack);
		addressOrIntersection.setOnClickListener(new View.OnClickListener() {
			@Override
				public void onClick(View v) {
				
				String locationAddress=
						intersection.getText().toString();
			        Intent broadcastIntent = new Intent();
			        broadcastIntent.setAction(ACTION_HERES_AN_STREET_ADDRESS_TO_SEEK)
			        .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
			        .putExtra("SeekAddressString", locationAddress);
			        // Broadcast whichever result occurred
			        LocalBroadcastManager.getInstance(SearchActivity.this).sendBroadcast(broadcastIntent);			
			        finish();
				}		
		});
		trainStations.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// First ... where are we?
		        mLocationClient = new LocationClient(SearchActivity.this, SearchActivity.this, SearchActivity.this);		
		        mLocationClient.connect();
			}
		});
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchActivity.this.finish();
			}
		});
		history.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
		        // Create a new broadcast receiver to receive updates from the listeners and service
				if(mBroadcastReceiver!=null) {
					LocalBroadcastManager.getInstance(SearchActivity.this).unregisterReceiver(mBroadcastReceiver);
				}
		        mBroadcastReceiver = new MyBroadcastReceiver();
		        // Create an intent filter for the broadcast receiver
		        IntentFilter mIntentFilter = new IntentFilter();

		        // Action for broadcast Intents to arm the address
		        mIntentFilter.addAction(HomeManager.ACTION_HERES_AN_ADDRESS_TO_ARM);
		        mIntentFilter.addAction(JUST_FINISH);

		        // All Location Services sample apps use this category
		        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
		        
		        // Register the broadcast receiver to receive status updates
		        LocalBroadcastManager.getInstance(SearchActivity.this).registerReceiver(mBroadcastReceiver, mIntentFilter);

				Intent intent=new Intent(SearchActivity.this,HistoryList.class);
				startActivity(intent);
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		mNickNameDialogId=settings.getLong("nicknameid", -100);
		if(mNickNameDialogId!=-100) {
			editor.putLong("nicknameid", -100);
			editor.commit();
			showDialog(DIALOG_NICKNAME);
		}
	}
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog dialog=null;
		switch (dialogId) {
		case DIALOG_NICKNAME:
			double longitude=0;
			double latitude = 0;
			String name="";
			String nickName="";
			String nameToUseForSelectIt="";
			Cursor cu=getDbAdapter().getHistoryItemFromId(mNickNameDialogId );
			while(cu.moveToNext()) {
				longitude=cu.getDouble(cu.getColumnIndex("longitude"));
				latitude=cu.getDouble(cu.getColumnIndex("latitude"));
				nickName=cu.getString(cu.getColumnIndex(DbAdapter.KEY_HISTORY_NICKNAME));
				name=cu.getString(cu.getColumnIndex(DbAdapter.KEY_NAME));
			}
			nameToUseForSelectIt=nickName;
			if(nameToUseForSelectIt==null || nameToUseForSelectIt.trim().equals("")) {
				nameToUseForSelectIt=name;
			}
			cu.close();

			LayoutInflater li=this.getLayoutInflater();
			View view=li.inflate(R.layout.activity_history_setnickname,null);
			final EditText editTextNickname=(EditText)view.findViewById(R.id.editTextNickname);
			AlertDialog.Builder builder=new AlertDialog.Builder(this)
				.setView(view)
				.setTitle("Rename " + nameToUseForSelectIt)
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getDbAdapter().setHistoryItemNickname(mNickNameDialogId, editTextNickname.getText().toString());
					}
				});
				editTextNickname.setText(nameToUseForSelectIt);
				dialog=builder.create();
			break;
		default:
			break;
		}
		return dialog;
	}
	private DbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new DbAdapter(this);
		}
		return mDbAdapter;
	}
    /**
     * Define a Broadcast receiver that receives notice that the history item has been chosen,
     * so now we can close finish this page, thereby returning to the main map.
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
 
            if (TextUtils.equals(action, HomeManager.ACTION_HERES_AN_ADDRESS_TO_ARM) || TextUtils.equals(action, JUST_FINISH)) {

                finish();

            // Intent contains information about successful addition or removal of geofences
            }
        }
    }
	public void hereAreTheTrainStationAddresses(
			ArrayList<Address> addresses, Location location) {
		new SearchRailroadStationsDialogFragment(addresses,this).show(getFragmentManager(),"Trains");
	}
    public static class SearchRailroadStationsDialogFragment extends DialogFragment {
    	private CharSequence[] mItems;
    	private ArrayList<Address> mAddresses;
    	private SearchActivity mActivity=null;
    	public SearchRailroadStationsDialogFragment() {super();}
		public SearchRailroadStationsDialogFragment(ArrayList<Address> addresses, SearchActivity activity) {
    		super();
    		mAddresses=addresses;
    		mActivity=activity;
    		mItems=new CharSequence[addresses.size()];
    		for(int c=0;c<addresses.size();c++) {
    			mItems[c]=addresses.get(c).getAddressLine(0);
    		}
    	}
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Train Stations in your Vicinity")
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           
                       }
                   })
                   .setItems(mItems, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						/* It's okay to do this singleton, because Home2 must be in memory if SearchActivity is in memory. */
						if(!Home2.mSingleton.getHomeManager().getSecurityManager().doTrialCheck()) {
							mActivity.finish();
							return;
						}
						
						Address a=new Address(Locale.getDefault());
						a.setLatitude(mAddresses.get(which).getLatitude());
						a.setLongitude(mAddresses.get(which).getLongitude());
						a.setAddressLine(0, mAddresses.get(which).getAddressLine(0));
						mActivity.getDbAdapter().writeOrUpdateHistory(a);
				        Intent broadcastIntent = new Intent();
				        broadcastIntent.setAction(ACTION_HERES_AN_ADDRESS_TO_ARM)
				        .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
				        .putExtra("latitude", mAddresses.get(which).getLatitude())
				        .putExtra("longitude", mAddresses.get(which).getLongitude())
				        .putExtra("name", mAddresses.get(which).getAddressLine(0));
				        // Broadcast whichever result occurred
				        LocalBroadcastManager.getInstance(SearchRailroadStationsDialogFragment.this.getActivity()).sendBroadcast(broadcastIntent);
						getActivity().finish();
					}
				});
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onConnected(Bundle arg0) {
		Location location=mLocationClient.getLastLocation();
		// simulate Scott's address
		//location.setLatitude(40.658421);
		//location.setLongitude(-74.29959);	
		//

		LocationAndWantsSurroundingTrainStations client=new HomeManager(this).new LocationAndWantsSurroundingTrainStations();
		client.mClient=(WantsSurroundingTrainStations)SearchActivity.this;
		client.mLocation=location;
		new HomeManager(this).new RetrieveAddressDataForMap()
		.execute(client);					
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}    
	public String getPREFS_NAME() {
		return getApplicationContext().getPackageName() + "_preferences";
	}
}
