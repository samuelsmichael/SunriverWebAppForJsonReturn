package com.diamondsoftware.android.commuterhelpertrial;	

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

public class DbAdapter {
	private static final int DATABASE_VERSION = 9;

	public static final DateFormat mDateFormat = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss.S");

	public static final String KEY_ROWID = "_id";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_FOREIGNID = "_fid";
	public static final String KEY_NAME = "name";
	public static final String KEY_DATECREATED = "datecreated";
	public static final String KEY_HISTORY_COUNT="historycount";
	public static final String KEY_HISTORY_NICKNAME="historynickname";
	public static final String KEY_HISTORY_IS_STATION="isstation";

	private LocationManager mLocationManager=null;
	private final Activity mActivity;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	
	private static final String DATABASE_TABLE_LOCATION = "location";
	private static final String DATABASE_TABLE_STATION = "station";
	public static final String DATABASE_TABLE_HISTORY = "history";


	/* Public interface ---------------------------------------------------------------------- */
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param activity
	 *            the Activity within which to work
	 */
	public DbAdapter(Activity activity) {
		this.mActivity = activity;
	}
	
	public synchronized void setHistoryItemNickname(long id, String nickname) {
		ContentValues values = new ContentValues();
		values.put(KEY_HISTORY_NICKNAME, nickname);
		String whereClause2=KEY_ROWID + "=" + id;
		getSqlDb().update(DATABASE_TABLE_HISTORY, values, whereClause2, null);
	}
	public synchronized Cursor getHistoryItemFromId(long id) {
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		
		String[] projection = {
			KEY_ROWID,
			KEY_LATITUDE,
			KEY_LONGITUDE,
			KEY_NAME,
			KEY_HISTORY_NICKNAME,
			KEY_HISTORY_IS_STATION
		    };

		String whereClause = KEY_ROWID + " = " + id;

		Cursor cu = getSqlDb().query(
			DATABASE_TABLE_HISTORY,  				// The table to query
		    projection,                             // The columns to return
		    whereClause,                           	// The columns for the WHERE clause
		    null,                            		// The values for the WHERE clause
		    null,                                   // don't group the rows
		    null,                                   // don't filter by row groups
		    null	                                // The sort order
		    );
		return cu;
	}
	/*
	 * Create cache information for a location.
	 * Note: this item is already being called on non-UI thread, so we don't have to do it on its own thread.
	 * Also: I synchronize it so as to not interfere with any other simultaneous db fetches 
	 */
	public synchronized void createCacheItem(Location location, ArrayList<Address> addresses) {
		// Create a new map of values, where column names are the keys

		ContentValues values = new ContentValues();
		values.put(KEY_LATITUDE, location.getLatitude());
		values.put(KEY_LONGITUDE, location.getLongitude());
		values.put(KEY_DATECREATED, mDateFormat.format(new GregorianCalendar()
		.getTime()));

		// Insert the new row, returning the primary key value of the new row
		long newRowId = getSqlDb().insert(
				DATABASE_TABLE_LOCATION,
		         null,
		         values);
		for(int i=0;i<addresses.size();i++) {
			Address address=addresses.get(i);
			values = new ContentValues();
			values.put(KEY_LATITUDE, address.getLatitude());
			values.put(KEY_LONGITUDE, address.getLongitude());
			values.put(KEY_FOREIGNID, newRowId);
			values.put(KEY_NAME, address.getAddressLine(0));
			getSqlDb().insert(
					DATABASE_TABLE_STATION,
			         null,
			         values);
		}
	}
	
	
	/*
	 * This method looks for items in the cache.
	 * Note: This item is already being called on non-UI thread, so we don't have to do it on its own thread.
	 * Also: I synchronize it so as to not interfere with any other simultaneous db fetches 
	 * 
	 * 1. From all LOCATIONs
	 * 		a. If it's within the closeToRadiusInMeters
	 * 			i. Load all associated STATIONs into addressList 
	 */
	public synchronized void getStationsCloseTo(Location myLocation, float closeToRadiusInMeters,
			ArrayList<Address> addressList) {
		//int rowIdToUse=0;
		ArrayList<Integer> rowsToUse=new ArrayList<Integer>();
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		
		String[] projection = {
			KEY_ROWID,
			KEY_LATITUDE,
			KEY_LONGITUDE
		    };

		// How you want the results sorted in the resulting Cursor
		String sortOrder = 
				KEY_DATECREATED + " DESC";

		Cursor cu = getSqlDb().query(
			DATABASE_TABLE_LOCATION,  				// The table to query
		    projection,                             // The columns to return
		    null,                                	// The columns for the WHERE clause
		    null,                            		// The values for the WHERE clause
		    null,                                   // don't group the rows
		    null,                                   // don't filter by row groups
		    sortOrder                               // The sort order
		    );
		if(cu.getCount()>0) {
			while(cu.moveToNext()) {
				double locationLongitude=cu.getDouble(cu.getColumnIndex(KEY_LONGITUDE));
				double locationLatitude=cu.getDouble(cu.getColumnIndex(KEY_LATITUDE));
	    		Location location = new Location(getProvider());
	    		location.setLatitude(locationLatitude);
	    		location.setLongitude(locationLongitude);
				double dx=(double)myLocation.distanceTo(location);
				if (dx<=HomeManager.CLOSE_TO_RADIUS_IN_METERS) {
					rowsToUse.add(cu.getInt(cu.getColumnIndex(KEY_ROWID)));
				}
			}
			cu.close();
			if(rowsToUse.size()>0) {
				for(Integer row: rowsToUse) {
					int rowIdToUse = row;
					String whereClause = KEY_FOREIGNID + " = " + String.valueOf(rowIdToUse);
					sortOrder = KEY_NAME + " ASC ";
					String[] projection2 = {
							KEY_LATITUDE,
							KEY_LONGITUDE,
							KEY_NAME
						    };
					cu = getSqlDb().query(
							DATABASE_TABLE_STATION,  				// The table to query
						    projection2,                             // The columns to return
						    whereClause,                                	// The columns for the WHERE clause
						    null,                            		// The values for the WHERE clause
						    null,                                   // don't group the rows
						    null,                                   // don't filter by row groups
						    sortOrder                              	// The sort order
						    );
					if(cu.getCount()>0) {
						while(cu.moveToNext()) {
							double locationLongitude=cu.getDouble(cu.getColumnIndex(KEY_LONGITUDE));
							double locationLatitude=cu.getDouble(cu.getColumnIndex(KEY_LATITUDE));
							String name=cu.getString(cu.getColumnIndex(KEY_NAME));
							Address address=new Address(Locale.getDefault());
							address.setLatitude(locationLatitude);
							address.setLongitude(locationLongitude);
							address.setAddressLine(0, name);
							addressList.add(address);
						}
						cu.close();
					} else {
						cu.close();
					}
				}
			}
		} else {
			cu.close();
		}
		if(addressList!=null && addressList.size()>1) {
			Collections.sort(addressList, new Comparator<Address>() {
		        @Override
		        public int compare(Address  item1, Address  item2)
		        {
		        	return item1.getAddressLine(0).compareTo(item2.getAddressLine(0));
		        }
		    });
		}
	}
	
	public synchronized void deleteHistoryItemsWhoseNameIs(String name) {
		String query = "DELETE FROM "+ DATABASE_TABLE_HISTORY + " WHERE " + this.KEY_NAME + " = '" + name + "' OR "+this.KEY_HISTORY_NICKNAME+"='" + name + "'";
		getSqlDb().execSQL(query);
	}
	
	public synchronized Cursor getHistoryInMostUsedDescendingOrder() {
		String query=
				"SELECT IFNULL("+KEY_HISTORY_NICKNAME +","+KEY_NAME+") as "+KEY_NAME+", " +
						KEY_HISTORY_COUNT+"," +
						KEY_LATITUDE+"," +
						KEY_LONGITUDE+"," +
						KEY_ROWID + "," + KEY_HISTORY_IS_STATION +
		" FROM " +DATABASE_TABLE_HISTORY+ " ORDER BY " + KEY_HISTORY_COUNT + " DESC ";
		return getSqlDb().rawQuery(query,null);
		
		/*

		
		String[] projection = {
				KEY_NAME,
				KEY_HISTORY_NICKNAME,
				KEY_HISTORY_COUNT,
				KEY_LATITUDE,
				KEY_LONGITUDE,
				KEY_ROWID
			    };
		String sortOrder = KEY_HISTORY_COUNT +" DESC";
		Cursor cu = getSqlDb().query(
				DATABASE_TABLE_HISTORY,  				// The table to query
			    projection,             	            // The columns to return
			    null, 					                // The columns for the WHERE clause
			    null,                            		// The values for the WHERE clause
			    null,                                   // don't group the rows
			    null,                                   // don't filter by row groups
			    sortOrder	                                // don't do sort order
			    );
		return cu;
		*/
	}

	public synchronized void writeOrUpdateHistory(Address address, boolean isStation) {
		new Thread(new MyRunnable2(address,isStation)).run();
	}
	
	private class MyRunnable2 implements Runnable {
		private Address mAddress;
		private boolean mIsStation;
		public MyRunnable2(Address address, boolean isStation ) {
			mAddress=address;
			mIsStation=isStation;
		}
		@Override
		public void run() {
			internalWriteOrUpdateHistory(mAddress,mIsStation);
		}
	}
	
	/* User has selected a destination Address
	 * 1. Look for a record (by latitude and longitude
	 * 2. If found, increment its count, otherwise, create a new record
	 */
	private synchronized void internalWriteOrUpdateHistory(Address address, boolean isStation) {
		String[] projection = {
				KEY_ROWID,
				KEY_HISTORY_COUNT
			    };	
		String sortOrder = null;
		String whereClause = KEY_NAME + " = '" + address.getAddressLine(0).replace("'", "''") + "'";
		try {
			Cursor cu = getSqlDb().query(
				DATABASE_TABLE_HISTORY,  				// The table to query
			    projection,             	            // The columns to return
			    whereClause,			                // The columns for the WHERE clause
			    null,                            		// The values for the WHERE clause
			    null,                                   // don't group the rows
			    null,                                   // don't filter by row groups
			    null	                                // don't do sort order
			    );
			if(cu.getCount()>0) {	
				cu.moveToFirst();
				int oldCount=cu.getInt(cu.getColumnIndex(KEY_HISTORY_COUNT));
				oldCount=oldCount+1;
				ContentValues values = new ContentValues();
				values.put(KEY_HISTORY_COUNT, oldCount);
				String whereClause2=KEY_ROWID + "=" + cu.getInt(cu.getColumnIndex(KEY_ROWID));
				getSqlDb().update(DATABASE_TABLE_HISTORY, values, whereClause2, null);
			} else {
				ContentValues values = new ContentValues();
				values.put(KEY_LATITUDE, address.getLatitude());
				values.put(KEY_LONGITUDE, address.getLongitude());
				values.put(KEY_DATECREATED, mDateFormat.format(new GregorianCalendar()
				.getTime()));
				values.put(KEY_HISTORY_COUNT, 1);
				values.put(KEY_NAME, address.getAddressLine(0));
				values.put(KEY_HISTORY_IS_STATION, isStation?1:0);

				// Insert the new row, returning the primary key value of the new row
				long newRowId = getSqlDb().insert(
						DATABASE_TABLE_HISTORY,
				         null,
				         values);
			}
			cu.close();
		} catch (Exception e) {}
	}
	
	private class MyRunnable implements Runnable {
		private Date mAgedThreshhold;
		public MyRunnable(Date agedThreshhold ) {
			mAgedThreshhold=agedThreshhold;
		}
		@Override
		public void run() {
			internalPurgeCacheOfItemsOlderThan(mAgedThreshhold);
		}
	}
	
	/*
	 * Purge the cache of old items
	 * Note: I synchronize it so as to not interfere with any other simultaneous db fetches.
	 * Also: This is called by Home.onCreate ... and so, I have it run on a different thread, so as to not take away from the time it takes the screen to come up.
	 */
	
	public synchronized void purgeCacheOfItemsOlderThan (Date agedThreshhold) {
		new Thread(new MyRunnable(agedThreshhold)).run();
	}
		
	private void internalPurgeCacheOfItemsOlderThan(Date agedThreshhold) {
		
		String[] projection = {
			KEY_ROWID
		    };
		
		String sortOrder = null;
		String whereClause = KEY_DATECREATED + " <= '" + mDateFormat.format(agedThreshhold) + "'";  

		try {
		Cursor cu = getSqlDb().query(
			DATABASE_TABLE_LOCATION,  				// The table to query
		    projection,             	            // The columns to return
		    whereClause,			                // The columns for the WHERE clause
		    null,                            		// The values for the WHERE clause
		    null,                                   // don't group the rows
		    null,                                   // don't filter by row groups
		    sortOrder                               // The sort order
		    );
		if(cu.getCount()>0) {
			while(cu.moveToNext()) {
				int rowId=cu.getInt(cu.getColumnIndex(KEY_ROWID));
				deleteCachedItem(rowId);
			}
		}
		cu.close();
		} catch (Exception ee) {
		}
	}

	public void close() {
		mDbHelper.close();
		mDbHelper = null;
		mDb = null;
	}

	public static String doubleApostophize(String str) 
	{
		if(str==null) {
			return null;
		}
		return str.replaceAll("'", "''");
	}
	
	/* Private interface ---------------------------------------------------------------------- */
	
	/*
	 * Remove items in the database for this location
	 */
	private void deleteCachedItem(int rowId) {
		// Delete Station records 
		String[] selectionArgs = { String.valueOf(rowId) };
		String selection = KEY_FOREIGNID + " = ?";
		getSqlDb().delete(DATABASE_TABLE_STATION, selection, selectionArgs);
		// Delete Location record
		selection=KEY_ROWID + " = ?";
		getSqlDb().delete(DATABASE_TABLE_LOCATION, selection, selectionArgs);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		/**
		 * Database creation sql statement 
		 */
		private static final String CREATE_TABLE_LOCATION = "create table location (" +
				"_id integer primary key autoincrement, " +
				"latitude double not null, " +
				"longitude double not null, datecreated datetime not null); ";
		private static final String CREATE_TABLE_STATION = "create table station (" +
				"_id integer primary key autoincrement, " +
				"_fid integer not null, " +
				"latitude double not null, " +
				"longitude double not null, name text not null ); ";
		private static final String CREATE_TABLE_HISTORY = "create table history (" +
				"_id integer primary key autoincrement, " +
				"latitude double not null, " +
				"longitude double not null, " + 
				"name string not null, " +
				"datecreated datetime not null, " +
				"historycount int not null,"  +
				"historynickname string null, " +
				KEY_HISTORY_IS_STATION + " bit null); ";
				
		
		private static final String DATABASE_NAME = "data";

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(CREATE_TABLE_LOCATION);
			} catch (Exception eieio33) {}
			try {
				db.execSQL(CREATE_TABLE_STATION);
			} catch (Exception eieio33) {}
			try {
				db.execSQL(CREATE_TABLE_HISTORY);
			} catch (Exception eieio33) {}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*
			if(oldVersion <= 0) {
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOCATION);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_STATION);
			} else {
				if (oldVersion==5) {
					db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOCATION);
					db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_STATION);
				} else {
					if (oldVersion<=6) {
						db.execSQL(CREATE_TABLE_HISTORY);
					}
				}
			}
			*/
			if(newVersion==9) {
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_HISTORY);
			}
			onCreate(db);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			int bkhere=3;
			int bkthere=bkhere;
		}

	
	}
	private SQLiteDatabase getSqlDb() {
		if (mDb == null) {
			if (mDbHelper == null) {
				mDbHelper = new DatabaseHelper(mActivity);
			}
			mDb = mDbHelper.getWritableDatabase();
		}
		return mDb;
	}

	private LocationManager getLocationManager() {
		if (mLocationManager == null) {
			mLocationManager = (android.location.LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
		}
		return mLocationManager;
	}

	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		return getLocationManager().getBestProvider(criteria, false);
	}	
	
}
