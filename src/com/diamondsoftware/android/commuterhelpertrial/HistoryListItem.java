package com.diamondsoftware.android.commuterhelpertrial;

public class HistoryListItem {
	private String mName;
	private long _id;
	private double mLatitude;
	private double mLongitude;
	public HistoryListItem(String name,long rowId, double latitude, double longitude) {
		mName=name;
		_id=rowId;
		mLatitude=latitude;
		mLongitude=longitude;
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
