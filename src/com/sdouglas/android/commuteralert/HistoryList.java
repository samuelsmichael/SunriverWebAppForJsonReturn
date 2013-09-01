package com.sdouglas.android.commuteralert;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class HistoryList extends ListActivity {
	private DbAdapter mDbAdapter=null;
	private Cursor mCursor;
	private SimpleCursorAdapter mAdapter;
	public static final String PREFS_NAME = "com.sdouglas.android.commuteralert_preferences";
    private static final String ACTION_HERES_AN_ADDRESS_TO_ARM="ADDRESS_TO_ARM";

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
		mCursor=getDbAdapter().getHistoryInMostUsedDescendingOrder();
		startManagingCursor(mCursor);
		mAdapter=new SimpleCursorAdapter(
				this,
				android.R.layout.simple_dropdown_item_1line,
				mCursor,
				new String[] {DbAdapter.KEY_NAME},
				new int[] {android.R.id.text1});
		setListAdapter(mAdapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mAdapter.getCursor().moveToPosition(position);
		double latitude=mAdapter.getCursor().getDouble(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_LATITUDE));
		double longitude=mAdapter.getCursor().getDouble(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_LONGITUDE));
		String name=mAdapter.getCursor().getString(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_HISTORY_NICKNAME));
		if(name == null || name.trim().equals("")) {
			name=mAdapter.getCursor().getString(mAdapter.getCursor().getColumnIndex(DbAdapter.KEY_NAME));
		}
		// broadcast the intend so that the system can be armed.
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
	
	private DbAdapter getDbAdapter() {
		if (mDbAdapter == null) {
			mDbAdapter = new DbAdapter(this);
		}
		return mDbAdapter;
	}

	
}
