package com.diamondsoftware.android.commuterhelpertrial;

public class HistoryListItem {
	private String mName;
	private long _id;
	private double mLatitude;
	private double mLongitude;
	private int mCount;
	private boolean mIsStation;
	public HistoryListItem(String name,long rowId, double latitude, double longitude, int count, boolean isStation) {
		mName=name;
		mIsStation=isStation;
		_id=rowId;
		mLatitude=latitude;
		mLongitude=longitude;
		mCount=count;
	}
	public boolean ismIsStation() {
		return mIsStation;
	}
	public void setmIsStation(boolean mIsStation) {
		this.mIsStation = mIsStation;
	}
	public void addToCount(int count) {
		mCount+=count;
	}
	public int getmCount() {
		return mCount;
	}
	public void setmCount(int mCount) {
		this.mCount = mCount;
	}
	public double getmLatitude() {
		return mLatitude;
	}
	public void setmLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}
	public double getmLongitude() {
		return mLongitude;
	}
	public void setmLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}
	public String getmName() {
		return mName;
	}
	public void setmName(String mName) {
		this.mName = mName;
	}
	public long getmRowId() {
		return _id;
	}
	public void setmRowId(long mRowId) {
		this._id = mRowId;
	}
	@Override
	public  String toString() {
		return mName;
	}
}
