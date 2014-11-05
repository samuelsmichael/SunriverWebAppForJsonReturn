package com.diamondsoftware.android.commuterhelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.support.v4.content.LocalBroadcastManager;

import com.diamondsoftware.android.commuterhelper.SearchActivity.SearchRailroadStationsDialogFragment;
import com.google.android.gms.maps.model.LatLng;


public class PostPaymentManager {
	public PostPaymentManager(Home2 home2) {
		mHome2=home2;
		mPressedMapLatLng=null;
		mResultF=null;
		mSearchingAddress=null;
	}
	private Home2 mHome2;
	private LatLng mPressedMapLatLng;
	private Address mSearchingAddress;
	private Address mTrainsAddress;
	public Address getmTrainsAddress() {
		return mTrainsAddress;
	}
	public void setmTrainsAddress(Address mTrainsAddress) {
		this.mTrainsAddress = mTrainsAddress;
		mPressedMapLatLng=null;
		mResultF=null;
		mHistorySelection=null;
		mSearchingAddress=null;
	}
	private ActivityHistoryList.HistorySelection mHistorySelection;
	public Address getmSearchingAddress() {
		return mSearchingAddress;
	}
	public void setmSearchingAddress(Address mSearchingAddress) {
		this.mSearchingAddress = mSearchingAddress;
		mPressedMapLatLng=null;
		mResultF=null;
		mHistorySelection=null;
		mTrainsAddress=null;
	}
	private Home2.LocationAndAssociatedTrainStations mResultF;
	public void doPostPaymentActivities() {
		if(mPressedMapLatLng != null && mResultF!=null) {
			mHome2.doMyOnMapLongClick(mPressedMapLatLng,mResultF);
		}
		if(mSearchingAddress!=null) {
			mHome2.getHomeManager().newLocationButFirstPrompt(mSearchingAddress);
		}
		if(mHistorySelection!=null) {
			Intent intent=new Intent(mHome2,ActivityHistoryList.class)
				.putExtra("mLatitude", String.valueOf(mHistorySelection.mLatitude))
				.putExtra("mLongitude", String.valueOf(mHistorySelection.mLongitude))
				.putExtra("mName", mHistorySelection.mName)
				.putExtra("mIsStation", mHistorySelection.mIsStation)
				.setAction("postpayment");
			mHome2.startActivity(intent);
		}
		if(mTrainsAddress!=null) {
			mHome2.getHomeManager().getDbAdapter().writeOrUpdateHistory(mTrainsAddress,true);
	        Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(SearchActivity.ACTION_HERES_AN_ADDRESS_TO_ARM)
	        .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
	        .putExtra("latitude", mTrainsAddress.getLatitude())
	        .putExtra("longitude", mTrainsAddress.getLongitude())
	        .putExtra("name", mTrainsAddress.getAddressLine(0));
	        // Broadcast whichever result occurred
	        SharedPreferences settings=mHome2.getSharedPreferences(mHome2.getApplicationContext().getPackageName() + "_preferences", mHome2.MODE_PRIVATE);
	        LocalBroadcastManager.getInstance(mHome2).sendBroadcast(broadcastIntent);
			Editor editor = settings.edit();
			editor.putString(GlobalStaticValues.KEY_SpeakableAddress, mTrainsAddress.getAddressLine(0));
			editor.commit();


		}
	}
	public LatLng getmPressedMapLatLng() {
		return mPressedMapLatLng;
	}
	public ActivityHistoryList.HistorySelection getmHistorySelection() {
		return mHistorySelection;
	}
	public void setmHistorySelection(
			ActivityHistoryList.HistorySelection mHistorySelection) {
		this.mHistorySelection = mHistorySelection;
		mPressedMapLatLng=null;
		mResultF=null;
		mSearchingAddress=null;
		mTrainsAddress=null;
		
	}
	public void setmPressedMapLatLng(LatLng mPressedMapLatLng) {
		this.mPressedMapLatLng = mPressedMapLatLng;
		mSearchingAddress=null;
		mHistorySelection=null;
		mTrainsAddress=null;

	}
	public Home2.LocationAndAssociatedTrainStations getmResultF() {
		return mResultF;
	}
	public void setmResultF(Home2.LocationAndAssociatedTrainStations mResultF) {
		this.mResultF = mResultF;
		mSearchingAddress=null;
		mHistorySelection=null;
		mTrainsAddress=null;

	}
}
