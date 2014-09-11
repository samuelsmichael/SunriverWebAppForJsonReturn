package com.diamondsoftware.android.commuterhelpertrial;

import com.diamondsoftware.android.commuterhelpertrial.R;
import java.util.Locale;
import android.location.Address;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HistoryList extends ListActivity {
	private DbAdapter mDbAdapter=null;
	private Cursor mCursor;
	private SimpleCursorAdapter mAdapter;
    private static final String ACTION_HERES_AN_ADDRESS_TO_ARM="ADDRESS_TO_ARM";
	private long mId;
    private static final String JUST_FINISH="JUST_FINISH";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_list);
		Button cancelHistory=(Button) findViewById(R.id.cancelhistory);
		cancelHistory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					HistoryList.this.finish();
				} catch (Exception e) {}
			}
		});		
/*		
		String qqc=	"SELECT IFNULL("+DbAdapter.KEY_HISTORY_NICKNAME +","+DbAdapter.KEY_NAME+") as "+DbAdapter.KEY_NAME+", " +
						DbAdapter.KEY_HISTORY_COUNT+"," +
						DbAdapter.KEY_LATITUDE+"," +
						DbAdapter.KEY_LONGITUDE+"," +
						DbAdapter.KEY_ROWID +
		" FROM " +DbAdapter.DATABASE_TABLE_HISTORY+ " ORDER BY " + DbAdapter.KEY_HISTORY_COUNT + " ASC ";
*/		
		
		
		mCursor=getDbAdapter().getHistoryInMostUsedDescendingOrder();
		startManagingCursor(mCursor);
		mAdapter=new SimpleCursorAdapter(
				this,
				android.R.layout.simple_dropdown_item_1line,
				mCursor,
				new String[] {DbAdapter.KEY_NAME},
				new int[] {android.R.id.text1});
		setListAdapter(mAdapter);
		registerForContextMenu(getListView());
	}
	
	private void selectIt(double latitude, double longitude, String name) {
		/* It's okay to do this singleton, because Home2 must be in memory if SearchActivity is in memory. */
		if(!Home2.mSingleton.getHomeManager().getSecurityManager().doTrialCheck()) {
	        Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(JUST_FINISH)
	        .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

			finish();
			return;
		}

		Editor editor = getSharedPreferences(getPREFS_NAME(), MODE_PRIVATE).edit();
		editor.putString(GlobalStaticValues.KEY_SpeakableAddress, name);
		editor.commit();
		Address a=new Address(Locale.getDefault());
		a.setLatitude(latitude);
		a.setLongitude(longitude);
		a.setAddressLine(0, name);
		getDbAdapter().writeOrUpdateHistory(a);
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_HERES_AN_ADDRESS_TO_ARM)
        .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
        .putExtra("latitude", latitude)
        .putExtra("longitude", longitude)
        .putExtra("name", name);
        // Broadcast whichever result occurred
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mAdapter.getCursor().moveToPosition(position);
		double latitude=mAdapter.getCursor().getDouble(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_LATITUDE));
		double longitude=mAdapter.getCursor().getDouble(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_LONGITUDE));
		String name=mAdapter.getCursor().getString(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_NAME));
		if(name == null || name.trim().equals("")) {
			name=mAdapter.getCursor().getString(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_NAME));
		}
		// broadcast the intend so that the system can be armed.
		selectIt(latitude, longitude, name);
        finish();
	}	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.history_contextmenu,menu);
	}	
	public String getPREFS_NAME() {
		return getApplicationContext().getPackageName() + "_preferences";
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		boolean retCode=true;
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		mId=info.id;
		switch(item.getItemId()) {
		case R.id.history_selectit:
			double longitude=0;
			double latitude = 0;
			String name="";
			String nickName="";
			String nameToUseForSelectIt="";
			Cursor cu=getDbAdapter().getHistoryItemFromId(mId);
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
			selectIt(latitude,longitude,nameToUseForSelectIt);
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
			break;
		default:
			retCode= super.onContextItemSelected(item);
		}
		return retCode;
	}	
	
	private DbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new DbAdapter(this);
		}
		return mDbAdapter;
	}

	
}
