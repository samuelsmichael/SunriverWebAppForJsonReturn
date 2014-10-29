package com.diamondsoftware.android.commuterhelper;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.diamondsoftware.android.commuterhelpertrial.util.IabHelper;
import com.diamondsoftware.android.commuterhelpertrial.util.IabResult;
import com.diamondsoftware.android.commuterhelpertrial.util.Inventory;
import com.diamondsoftware.android.commuterhelpertrial.util.Purchase;
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


/*
 * To change from regular version to trial (and visa versa):
 * 		1. Right click on com.diamondsoftware.android.commuterhelper, refactor, rename to com.diamondsoftware.android.commuterhelper
 * 		2. There will be several "import import com.diamondsoftware.android.commuterhelpertrial.R;"s that have to be renamed.
 * 		3. Scan the entire project for TRIAL_VS_NON-TRIAL and follow instructions
 */
public class Home2 extends AbstractActivityForMenu implements HomeImplementer,
		WantsSurroundingTrainStations {
	private GoogleMap mMap = null;
    // Debug tag, for logging
    static final String TAG = "CommuterAlert";
    // Does the user have the premium upgrade?
    static Boolean mIsPremium = null;
    // Current amount of gas in tank, in units
    static Integer mTank=null;

    

    // Does the user have an active subscription to the infinite gas plan?
    static Boolean mSubscribedToInfiniteGas = null;

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";
    static final String SKU_GAS = "gas";

    // SKU for our subscription (infinite gas)
    static final String SKU_INFINITE_GAS = "infinite_gas";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    // The helper object
    IabHelper mHelper;
    
	public static final int TRIAL_ALLOWANCE = 10;

    private static final int ARMED_NOTIFICATION_ID=3;
    private NotificationManager mNotificationManager=null;
	private MapFragment mMapFragment;
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	private HomeManager mHomeManager;
	private static float DEFAULT_ZOOM = 11f;
	private static float DEFAULT_TILT = 0f;
	private static float DEFAULT_BEARING = 0f;
	private Marker mPreviousMarker;
	private boolean mIveAnimated=false;
    private SharedPreferences settings = null;
	private Button disarmButton = null;
	private CompoundButton armedButton = null;
	private TextView currentLocation=null;
	


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
	private static final String ACTION_ETA="actioneta";
	private MyBroadcastReceiver mBroadcastReceiver;
	private IntentFilter mIntentFilter;
	private static String INSTRUCTIONS_MESSAGE = "To select a location\n\n-- Long press the screen\n   at the desired location. \n\n              or\n\n-- Press the Search button.";
	public static String CURRENT_VERSION="1.00";
	private boolean needToBringUpSplashScreen = false;
	public static Home2 mSingleton=null;
	public boolean mIveShownGPSNotEnabledWarning=false;
	private ImageView mHelp1;
	private ImageView mHelp2;
	private ImageView mHelp3;
	private ImageView mHelp4;
	AlertDialog mFAlertDialog;

	public SharedPreferences getSettings() {
		return settings;
	}
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
		setContentView(R.layout.activity_home2);
		
		// load mGas
		loadData();
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA14rS4bA3hKKS0xUq239+qygEqxwkpvDKEtcAptoZXsprt8uL6IY3gO9mbOUcIFc6sQRAGE7+KRXH3tWkHmBIVKjmX1qFvu7HZWdYfZeg1qJUGpI12LHDeFL3c533njNrzP+eHPetmqgbOTexeQbzEuZP8POzXhEXICNMYvgM3MMrXpEbw1IjxTUmMyZfwz5TSfbnqqgyZ4qSxLzb8gAuOQbIe2dYyNMj8IZ+yMH6HBorvXOj2Zig/EaYL7mvcb5/oXmp/jXJjcP408URM2xoSyraxeSTNGp+0c5lQZNLp5ex0/fi3ZbRn3qgATxTAeFktIeBYxlyS/g1A+GEhHzsHwIDAQAB";
		mHelper = new IabHelper(this,base64EncodedPublicKey);
		mHelper.enableDebugLogging(true);
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
        


		mHelp1=(ImageView)findViewById(R.id.image_help_main_1);
		mHelp2=(ImageView)findViewById(R.id.image_help_main_2);
		mHelp3=(ImageView)findViewById(R.id.image_help_main_3);
		mHelp4=(ImageView)findViewById(R.id.image_help_main_4);

		disarmButton = (Button) findViewById(R.id.buttonSearch);
		armedButton = (CompoundButton) findViewById(R.id.switchArmed);
		currentLocation=(TextView)findViewById(R.id.tvCurrentLocation2);

		try {
			CURRENT_VERSION = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			CURRENT_VERSION ="1.0";
		}
		
		settings = getSharedPreferences(getPREFS_NAME(), MODE_PRIVATE);		
		if (Double.valueOf(settings.getString("latitude","0")) == 0) {
			armedButton.setVisibility(View.GONE);
		} else {
			armedButton.setVisibility(View.VISIBLE);
		}

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
		        final Handler handler = new Handler();

		        alert.show();	    	
		    }
		}
		
		mSingleton=this;
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
		// Time left notification
		mIntentFilter.addAction(ACTION_ETA);

		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.add(GregorianCalendar.DATE, -PURGECACHEDAYSOLD);
		getHomeManager().getDbAdapter().purgeCacheOfItemsOlderThan(
				calendar.getTime());

		final CompoundButton armedButton = (CompoundButton) findViewById(R.id.switchArmed);
		armedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CompoundButton) v).isChecked()) {
					SharedPreferences settings = getSharedPreferences(
							getPREFS_NAME(), MODE_PRIVATE);
					if (Double.valueOf(settings.getString("latitude","0")) == 0) {
						new WarningAndInitialDialog(
								"You must select a location before turning on the alarm.",
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
							armedButton.setVisibility(View.GONE);
						} else {
							armedButton.setVisibility(View.VISIBLE);
						}
						refreshHelp();
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
		Intent intent22=getIntent();
		if(intent22!=null) {
			String action=intent22.getAction();
			if(action!=null) {
				if(action.equals("seekingaddress")) {
					getHomeManager();
					if(HomeManager.mPreventReentry==0) {
						HomeManager.mPreventReentry++;
						getHomeManager().manageKeyedInAddress(
							intent22.getStringExtra("SeekAddressString"));
					}
				} else {
					if(action.equals("TrialVersionDialog")) {
						String title=intent22.getExtras().getString("Title", "Setting Location");
						String message=intent22.getExtras().getString("Message", "");
						boolean trialPeriodIsOver=intent22.getExtras().getBoolean("TrialPeriodIsOver");
						TrialVersionDialog tvdd=new TrialVersionDialog(title,message,trialPeriodIsOver,this);
						TrialVersionDialog[] tvdds= new TrialVersionDialog[1];
						tvdds[0]=tvdd;
						new ShowExpiredDialog().execute(tvdds);
					}
				}
			}
		}
	}
    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    // User clicked the "Buy Gas" button
    public void onBuyGasButtonClicked() {
        Log.d(TAG, "Buy gas button clicked.");

        if (mSubscribedToInfiniteGas) {
            complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
            return;
        }

        // launch the gas purchase UI flow.
        // We will be notified of completion via mPurchaseFinishedListener
        Log.d(TAG, "Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        mHelper.launchPurchaseFlow(this, SKU_GAS, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked() {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    // "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
    // flow for subscription.
    public void onInfiniteGasButtonClicked() {
        if (!mHelper.subscriptionsSupported()) {
            complain("Subscriptions not supported on your device yet. Sorry!");
            return;
        }

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        Log.d(TAG, "Launching purchase flow for infinite gas subscription.");
        mHelper.launchPurchaseFlow(this,
                SKU_INFINITE_GAS, IabHelper.ITEM_TYPE_SUBS,
                RC_REQUEST, mPurchaseFinishedListener, payload);
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) {
        		if (!getHomeManager().getSecurityManager().initializeVersion()) {
        			needToBringUpSplashScreen = true;
        		}
            	return;
            }

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            mIsPremium=false;
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            // Do we have the infinite gas plan?
            mSubscribedToInfiniteGas = false;
            Purchase infiniteGasPurchase = inventory.getPurchase(SKU_INFINITE_GAS);
            mSubscribedToInfiniteGas = (infiniteGasPurchase != null &&
                    verifyDeveloperPayload(infiniteGasPurchase));
            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                        + " infinite gas subscription.");

            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                Log.d(TAG, "We have gas. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
                return;
            }
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
    		if (!getHomeManager().getSecurityManager().initializeVersion()) {
    			needToBringUpSplashScreen = true;
    		}

        }
    };
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_GAS)) {
                // bought 1/4 tank of gas. So consume it.
                Log.d(TAG, "Purchase is gas. Starting gas consumption.");
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
            else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
            }
            else if (purchase.getSku().equals(SKU_INFINITE_GAS)) {
                // bought the infinite gas subscription
                Log.d(TAG, "Infinite gas subscription purchased.");
                alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                mTank=10;
                saveData();
                alert("You have purchased 10 usages.");
            }
            else {
                complain("Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };
	
    void saveData() {

        /*
         * WARNING: on a real application, we recommend you save data in a secure way to
         * prevent tampering. For simplicity in this sample, we simply store the data using a
         * SharedPreferences.
         */

        SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
        spe.putInt("tank", mTank);
        spe.commit();
        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    void loadData() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        int tryTank=sp.getInt("tank", 99999);
        if(tryTank==99999) {
        	mTank=null;
        } else {
        	mTank = tryTank;
        }
        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
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
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (mHelper==null||!mHelper.handleActivityResult(requestCode, resultCode, data)) {		
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
        } else {
        	if(mHelper!=null) {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
                return;
        	}
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

	public boolean doTrialCheck() {
		if (mHomeManager.getSecurityManager().isTrialVersion() || mHomeManager.getSecurityManager().isUnregisteredLiveVersion()) {
			if(mHomeManager.getSecurityManager().isTrialVersion()) {
				if (mHomeManager.getSecurityManager().hasExceededTrials()) {
/*					TrialVersionDialog tvdd=new TrialVersionDialog(
							"Trial Period Has Expired",
							"Your trial period is over. In order to continue using Commuter Alert you will have make one of the following purchases.",
							true,this
							);
					tvdd.show(getFragmentManager(), "marre_mais_marre");
			//		TrialVersionDialog[] tvdds= new TrialVersionDialog[1];
				//	tvdds[0]=tvdd;
					//new ShowExpiredDialog().execute(tvdds);
*/
					Intent intent = new Intent(this,Home2.class)
						.setAction("TrialVersionDialog")
						.putExtra("Title", "Trial Period Has Expired")
						.putExtra("Message", "Your trial period is over. In order to continue using Commuter Alert you will have make one of the following purchases.")
						.putExtra("TrialPeriodIsOver", true);
					startActivity(intent);
					
					return false;
				} else {
					mHomeManager.getSecurityManager().incrementTrials();
					if (mHomeManager.getSecurityManager().startWarnings()) {
						Intent intent = new Intent(this,Home2.class)
						.setAction("TrialVersionDialog")
						.putExtra("Title", "Trial Software Alert")
						.putExtra("Message", "Your trial period is nearing its end. You have "+ String.valueOf((TRIAL_ALLOWANCE-mHomeManager.getSecurityManager().getCountUserArmed()+1)) +" trials left.")
						.putExtra("TrialPeriodIsOver", false);
					startActivity(intent);
					}
				}
			} else {
				Intent intent = new Intent(this,Home2.class)
					.setAction("TrialVersionDialog")
					.putExtra("Title", "Unregistered Version")
					.putExtra("Message", "It appears that you are using an un-registered version. In order to continue using Commuter Alert you will have to purchase it. If you think that you have received this message in error, please try to load Commuter Alert again.")
					.putExtra("TrialPeriodIsOver", true);
				startActivity(intent);
				return false;
				
			}
		}
		return true;
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
			mSettingsManager.setHelpOverlayStateOn(true);
			invalidateOptionsMenu();

			new WarningAndInitialDialog("Thank you for using Commuter Alert!",
					"We hope that you find it useful.\n\nPlease ... if you like our app, give it a good rating.\nIf you don't, then please contact us. We will fix all bugs, and take any requests for enhancements very, very seriously.\n\nTo rate our app, or to contact us, press the menu.", Home2.this).show();
		}
	}

	private void setControlState(boolean isArmed,String readableAddress) {
		// Hide the previous pin; otherwise they just continue to accumulate.
		if (mPreviousMarker != null && mPreviousMarker.isVisible()) {
			mPreviousMarker.setVisible(false);
		}
		new Logger(
				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
				"Arm Button", this)
				.log(isArmed?("System is Armed: "+(readableAddress==null?"":readableAddress.replaceAll("\n", " "))):"System is Disarmed", GlobalStaticValues.LOG_LEVEL_NOTIFICATION);


		if (isArmed) {
			armedButton.setChecked(true);
			disarmButton.setVisibility(View.GONE);
			currentLocation.setVisibility(View.VISIBLE);
	//		if(currentLocation.getText().equals("")) {
				currentLocation.setText(settings.getString(GlobalStaticValues.KEY_SpeakableAddress, ""));
//			}
			currentLocation.setSelected(true);
		} else {
			currentLocation.setText("");
			armedButton.setChecked(false);
			disarmButton.setVisibility(View.VISIBLE);
			currentLocation.setVisibility(View.INVISIBLE);
		}
		
	}

	private class TrialVersionDialog extends DialogFragment {
		private String mTitle;
		private String mMessage;
		private boolean mTrialPeriodIsOver;
		private Activity mActivityX;

		private TrialVersionDialog() {
			super();
		}

		public TrialVersionDialog(String title, String message,
				boolean trialPeriodIsOver, Activity activity) {
			super();
			mTitle = title;
			mMessage = message;
			mTrialPeriodIsOver = trialPeriodIsOver;
			mActivityX=activity;
		}
		
		@Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			AlertDialog.Builder builder=null;
			if(!mTrialPeriodIsOver) {
				builder = new AlertDialog.Builder(getActivity());
//						new ContextThemeWrapper(mActivity,
	//							R.style.AlertDialogCustomDark));
				builder.setTitle(mTitle);
				builder.setMessage(mMessage);
				String negativeButtonVerbiage = "Okay";
				builder.setNegativeButton(negativeButtonVerbiage,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								mFAlertDialog.dismiss();
							}
						});
				builder.setCancelable(false);
				mFAlertDialog = builder.create();
				return mFAlertDialog;
			} else {
				builder = new AlertDialog.Builder(getActivity());
//				new ContextThemeWrapper(mActivity,
//							R.style.AlertDialogCustomDark));
		builder.setTitle(mTitle);
		builder.setMessage("Your alerts have expired. Choose from one of the purchase options below, or press your phone's back key to Cancel");
		String negativeButtonVerbiage = "Unlimited: USD 10.99";
		builder.setNegativeButton(negativeButtonVerbiage,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						if(Home2.mSingleton!=null) {
							Home2.mSingleton.onUpgradeAppButtonClicked();
						}
						mFAlertDialog.dismiss();
					}
				});
		builder.setCancelable(true);
		builder.setPositiveButton("1 year: USD 5.99",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						if(Home2.mSingleton!=null) {
							Home2.mSingleton.onInfiniteGasButtonClicked();
						}
						mFAlertDialog.dismiss();	

					}
				});
		builder.setNeutralButton("10 usages: USD 0.99",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						if(Home2.mSingleton!=null) {
							Home2.mSingleton.onBuyGasButtonClicked();
						}
						mFAlertDialog.dismiss();	

					}
				});
		mFAlertDialog = builder.create();
		return mFAlertDialog;

				/*builder = new AlertDialog.Builder(getActivity());
	//					new ContextThemeWrapper(mActivity,
		//						R.style.AlertDialogCustomDark));

				LayoutInflater inflater = getActivity().getLayoutInflater();
				mFAlertDialog = builder.create();

				final View view=inflater.inflate(R.layout.purchase,(ViewGroup)getActivity().getWindow().getDecorView().findViewById(android.R.id.content));
				final Button buttonPermanent = (Button) view.findViewById(R.id.purchase_button_permanent);
				buttonPermanent.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(Home2.mSingleton!=null) {
							Home2.mSingleton.onUpgradeAppButtonClicked();
						}
						mFAlertDialog.dismiss();
					}
				});
				final Button buttonSubscription = (Button) view.findViewById(R.id.purchase_button_subscription); 
				buttonSubscription.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(Home2.mSingleton!=null) {
							Home2.mSingleton.onInfiniteGasButtonClicked();
						}
						mFAlertDialog.dismiss();	
					}
				});
				final Button buttonTenUsages = (Button) view.findViewById(R.id.purchase_button_ticketfortenusages); 
				buttonTenUsages.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(Home2.mSingleton!=null) {
							Home2.mSingleton.onBuyGasButtonClicked();
						}
						mFAlertDialog.dismiss();	
					}
				});
				final Button buttonCancel = (Button) view.findViewById(R.id.purchase_button_cancel); 
				buttonCancel.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {	
						mFAlertDialog.dismiss();
					}
				});
				return mFAlertDialog;*/
			}
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
	class ShowExpiredDialog extends AsyncTask<TrialVersionDialog,Void,TrialVersionDialog> {
		protected TrialVersionDialog doInBackground(TrialVersionDialog...dialogs) {
			TrialVersionDialog tvd=dialogs[0];
			return tvd;
		}
		protected void onPostExecute(TrialVersionDialog result) {
			result.show(getFragmentManager(), "Out ov Trials");
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
						boolean isStation=false;
						/*
						 * If a trial version, and has exceeded the number of
						 * trials
						 */
						if (!doTrialCheck()) {
							return;
						}
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
								useThisAddress = resultF.mAddresses.get(i);
								gotRRStation = true;
								isStation=true;
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
							Editor editor = getSettings().edit();
							editor.putString(GlobalStaticValues.KEY_SpeakableAddress, useThisAddress.getAddressLine(0));
							editor.commit();
							currentLocation.setText(Home2.this.settings.getString(GlobalStaticValues.KEY_SpeakableAddress, ""));
							armTheSystem(useThisAddress,isStation);
						}

					}
				});
			}
		}
	}

	public void armTheSystem(Address useThisAddress, boolean isStation) {
		getHomeManager().getDbAdapter().writeOrUpdateHistory(useThisAddress, isStation);
		getHomeManager().newLocation(useThisAddress);
		mSettingsManager.setEffectiveLocation(0,0);
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
			refreshHelp();
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
				getHomeManager();
				if(HomeManager.mPreventReentry==0) {
					HomeManager.mPreventReentry++;
					getHomeManager().manageKeyedInAddress(
						intent.getStringExtra("SeekAddressString"));
				}
			} else {
				if(TextUtils.equals(action,ACTION_ETA)) {

					String jdText=settings.getString(GlobalStaticValues.KEY_SpeakableAddress, "") +
							" " + intent.getStringExtra("eta");
		    		new Logger(
		    				Integer.parseInt(settings.getString("LoggingLevel", String.valueOf(GlobalStaticValues.LOG_LEVEL_CRITICAL))),
		    				"Home2.onReceive", context)
		    				.log("jdText: "+String.valueOf(jdText), GlobalStaticValues.LOG_LEVEL_NOTIFICATION);
					currentLocation.setText(jdText);
			    	Notification.Builder mBuilder=new Notification.Builder(Home2.this)
			    	.setSmallIcon(R.drawable.ic_launcher_new)
			    	.setContentTitle("Commuter Alert is on")
			    	.setContentText(jdText)
			    	.setOngoing(true);

			    	// Creates an explicit intent for an Activity in your app
			    	Intent resultIntent = new Intent(Home2.this,Home2.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(Home2.this,
							(int)System.currentTimeMillis(), resultIntent, 0);
			    	mBuilder.setContentIntent(pendingIntent);    	    	
			    	getNotificationManager().notify(ARMED_NOTIFICATION_ID, mBuilder.getNotification());

				}
			}
		}
	}
	private NotificationManager getNotificationManager() {
		if (mNotificationManager==null) {
			mNotificationManager =
	    		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mNotificationManager;
	}

	public static class NickNameDialog {
		private Activity mActivity;
		private Address mAddress;

		@SuppressWarnings("unused")
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
			builder.setPositiveButton("Yes, use value above",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							
							if(!nickName.getText().toString().trim().equals("")) {
								mAddress.setAddressLine(0, nickName.getText()
										.toString());
								Editor editor = ((Home2)mActivity).getSettings().edit();
								editor.putString(GlobalStaticValues.KEY_SpeakableAddress, nickName.getText().toString());
								editor.commit();
							}
							((Home2) mActivity).armTheSystem(mAddress,false);
						}
					}).setNegativeButton("No, use original name",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Editor editor = ((Home2)mActivity).getSettings().edit();
							editor.putString(GlobalStaticValues.KEY_SpeakableAddress, mAddress.getAddressLine(0));
							editor.commit();
							((Home2) mActivity).armTheSystem(mAddress,false);
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

		@SuppressWarnings("unused")
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
    public static class NoAddressFoundWarning extends DialogFragment {
		private String mTitle;
		private String mMessage;
		private Activity mActivity;
    	
    	
    	@SuppressWarnings("unused")
		private NoAddressFoundWarning() {
    		super();
    	}
		public NoAddressFoundWarning(String title, String message,
				Activity activity) {
			super();
			mTitle = title;
			mMessage = message;
			mActivity = activity;
		}

		
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mTitle)
            			.setMessage(mMessage)
						.setNegativeButton("Okay",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dismiss();
									}
								});
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

	@Override
	protected void refreshHelp() {
		if(mSettingsManager.getHelpOverlayStateOn()) {
			if (Double.valueOf(settings.getString("latitude","0")) == 0) {
				mHelp1.setVisibility(View.VISIBLE);
				mHelp2.setVisibility(View.VISIBLE);
				mHelp4.setVisibility(View.INVISIBLE);
			} else {
				mHelp4.setVisibility(View.VISIBLE);
				mHelp1.setVisibility(View.INVISIBLE);
				mHelp2.setVisibility(View.INVISIBLE);
			}
			mHelp3.setVisibility(View.VISIBLE);
		} else {
			mHelp1.setVisibility(View.INVISIBLE);
			mHelp2.setVisibility(View.INVISIBLE);
			mHelp3.setVisibility(View.INVISIBLE);
			mHelp4.setVisibility(View.INVISIBLE);
		}
	}
    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
}
