package com.diamondsoftware.android.commuterhelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import com.diamondsoftware.android.commuterhelper.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class ActivityHistoryList extends AbstractActivityForMenu {
	private SharedPreferences mSharedPreferences;
	private ArrayList<HistoryListItem> mHistoryListItems; 
	private DbAdapter mDbAdapter=null;
	private Cursor mCursor;
    private static final String ACTION_HERES_AN_ADDRESS_TO_ARM="ADDRESS_TO_ARM";
    private HistoryListAdapterII mHistoryListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_history_list);
		mSharedPreferences=getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Activity.MODE_PRIVATE);
		Intent jdIntent=getIntent();
		if(jdIntent!=null) {
			String jdAction=jdIntent.getAction();
			if(jdAction!=null) {
				String latitude=jdIntent.getStringExtra("mLatitude");
				String longitude=jdIntent.getStringExtra("mLongitude");
				String name=jdIntent.getStringExtra("mName");
				if(latitude!=null && longitude!=null && name !=null) {
					selectIt(
						Double.valueOf(latitude), Double.valueOf(longitude), name, jdIntent.getBooleanExtra("mIsStation", true));
				}
				finish();
			}
		}
		final Button cancelHistory=(Button) findViewById(R.id.cancelhistory);
		final TextView empty=(TextView)findViewById(R.id.emptyhistory);
		cancelHistory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(ActivityHistoryList.this, SearchActivity.class);
					startActivity(intent);
					ActivityHistoryList.this.finish();

				} catch (Exception e) {}
			}
		});		
		mCursor=getDbAdapter().getHistoryInMostUsedDescendingOrder();
		mHistoryListItems=new ArrayList<HistoryListItem>();
		//mCursor.moveToFirst();
		while(mCursor.moveToNext()) {
			String name=mCursor.getString(mCursor.getColumnIndex("name"));
			int id=mCursor.getInt(mCursor.getColumnIndex("_id"));
			int count=mCursor.getInt(mCursor.getColumnIndex(DbAdapter.KEY_HISTORY_COUNT));
			HistoryListItem theFoundItem=itemExistsWhoseNameIs(name,mHistoryListItems);
			if(theFoundItem==null) {
				mHistoryListItems.add(new HistoryListItem(name,id,
						mCursor.getDouble(mCursor.getColumnIndex(DbAdapter.KEY_LATITUDE)),
						mCursor.getDouble(mCursor.getColumnIndex(DbAdapter.KEY_LONGITUDE)),count,
						mCursor.getInt(mCursor.getColumnIndex(DbAdapter.KEY_HISTORY_IS_STATION))==1?true:false
						));
			} else {
				theFoundItem.addToCount(count);
			}
		}
		mCursor.close();
		Collections.sort(mHistoryListItems, new Comparator<HistoryListItem>() {
	        @Override
	        public int compare(HistoryListItem  item1, HistoryListItem  item2)
	        {
	        	if(!(item1.getmCount()>=item2.getmCount())) {
	        		return 1;
	        	} else {
	        		return -1;
	        	}
	        }
	    });
		ListView list=(ListView)findViewById(R.id.historyListId);
		if(mHistoryListItems.isEmpty()) {
			list.setVisibility(View.INVISIBLE);
			empty.setVisibility(View.VISIBLE);
		} else {
	        // Click event for single list row
	        list.setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view,
	                    int position, long id) {
	        		HistoryListItem historyListItem=(HistoryListItem)getListViewAdapter().getItem(position);
	        		double latitude=historyListItem.getmLatitude();
	        		double longitude=historyListItem.getmLongitude();
	        		boolean isStation=historyListItem.ismIsStation();
	        		String name=historyListItem.getmName();
	        		// broadcast the intend so that the system can be armed.
	        		selectIt(latitude, longitude, name,isStation);
	           	}
	        });
	        registerForContextMenu(list);
	        list.setVisibility(View.VISIBLE);
	        empty.setVisibility(View.INVISIBLE);
			list.setAdapter(getListViewAdapter());
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    AdapterView.AdapterContextMenuInfo info =
	            (AdapterView.AdapterContextMenuInfo) menuInfo;
	    String title = mHistoryListItems.get(info.position).getmName();

	    menu.setHeaderTitle(title);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.history_contextmenu,menu);
	}	
	public String getPREFS_NAME() {
		return getApplicationContext().getPackageName() + "_preferences";
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		boolean retCode=true;
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		long mId=info.id;
		switch(item.getItemId()) {
		case R.id.history_selectit:
			double longitude=0;
			double latitude = 0;
			String name="";
			String nickName="";
			String nameToUseForSelectIt="";
			boolean isStation=false;
			Cursor cu=getDbAdapter().getHistoryItemFromId(mId);
			while(cu.moveToNext()) {
				longitude=cu.getDouble(cu.getColumnIndex("longitude"));
				latitude=cu.getDouble(cu.getColumnIndex("latitude"));
				nickName=cu.getString(cu.getColumnIndex(DbAdapter.KEY_HISTORY_NICKNAME));
				name=cu.getString(cu.getColumnIndex(DbAdapter.KEY_NAME));
				isStation=cu.getInt(cu.getColumnIndex(DbAdapter.KEY_HISTORY_IS_STATION))==1?true:false;
			}
			nameToUseForSelectIt=nickName;
			if(nameToUseForSelectIt==null || nameToUseForSelectIt.trim().equals("")) {
				nameToUseForSelectIt=name;
			}
			cu.close();
			selectIt(latitude,longitude,nameToUseForSelectIt,isStation);
			finish();
			break;
		case R.id.history_renameit:
			/*
			 * I have to communicate back to the main window whose onRestore method will manage the popping up of another dialog box.
			 */
			SharedPreferences settings = getSharedPreferences(getPREFS_NAME(),MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("nicknameid", mId);
			editor.commit();
			finish();
			Intent intent = new Intent(ActivityHistoryList.this,SearchActivity.class);
			startActivity(intent);
			break;
		case R.id.history_deleteit:
		      AlertDialog.Builder builder = new AlertDialog.Builder (this);
		      builder.setMessage ("Confirm delete "+mHistoryListItems.get(info.position).getmName());
		      builder.setNegativeButton (getString(android.R.string.cancel),
		                                 new DialogInterface.OnClickListener()
		                                 {
		                                   @Override
		                                   public void onClick (DialogInterface dialog, int which)
		                                   {
		                                     
		                                   }
		                                 });
		      builder.setPositiveButton (getString(android.R.string.ok),
		                                 new DialogInterface.OnClickListener()
		                                 {
		                                   @Override
		                                   public void onClick (DialogInterface dialog, int which)
		                                   {
		                                	   ActivityHistoryList.this.getDbAdapter().deleteHistoryItemsWhoseNameIs(mHistoryListItems.get(info.position).getmName());
		                                     Intent intent = new Intent(ActivityHistoryList.this,ActivityHistoryList.class);
		                                     startActivity(intent);
		                                     finish();
		                                   }
		                                 });
		      builder.create().show();
			break;
		default:
			retCode= super.onContextItemSelected(item);
		}
		return retCode;
	}	
	public class HistorySelection {
		double mLatitude;
		double mLongitude;
		String mName;
		boolean mIsStation;
	}
	private void selectIt(double latitude, double longitude, String name, boolean isStation) {
		/* It's okay to do this singleton, because Home2 must be in memory if SearchActivity is in memory. */
		if(!Home2.mSingleton.doTrialCheck()) {
			HistorySelection hs=new HistorySelection();
			hs.mLatitude=latitude;
			hs.mLongitude=longitude;
			hs.mName=name;
			hs.mIsStation=isStation;
			Home2.mPostPaymentManager.setmHistorySelection(hs);
			finish();
			return;
		}
		Editor editor = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", MODE_PRIVATE).edit();
		editor.putString(GlobalStaticValues.KEY_SpeakableAddress, name);
		editor.commit();
		Address a=new Address(Locale.getDefault());
		a.setLatitude(latitude);
		a.setLongitude(longitude);
		a.setAddressLine(0, name);
		getDbAdapter().writeOrUpdateHistory(a,isStation);
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_HERES_AN_ADDRESS_TO_ARM)
        .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
        .putExtra("latitude", latitude)
        .putExtra("longitude", longitude)
        .putExtra("name", name);
        // Broadcast whichever result occurred
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        finish();
	}
	
	@Override
	protected void refreshHelp() {
		ImageView iv=(ImageView)findViewById(R.id.image_help_history_1);
		iv.setVisibility(View.INVISIBLE);
		if(mHistoryListItems!=null && mHistoryListItems.size()>0 && mSettingsManager.getHelpOverlayStateOn()) {
			iv.setVisibility(View.VISIBLE);
		}

	}
	
	private HistoryListAdapterII getListViewAdapter() {
		if(mHistoryListAdapter==null) {
			mHistoryListAdapter= new HistoryListAdapterII(this,mHistoryListItems);
		}
		return mHistoryListAdapter;
	}
	private DbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new DbAdapter(this);
		}
		return mDbAdapter;
	}
	private HistoryListItem itemExistsWhoseNameIs(String theName,ArrayList<HistoryListItem> mItems) {
		for(HistoryListItem item: mItems ) {
			if(item.getmName().equalsIgnoreCase(theName)) {
				return item;
			}
		}
		return null;
	}

}
