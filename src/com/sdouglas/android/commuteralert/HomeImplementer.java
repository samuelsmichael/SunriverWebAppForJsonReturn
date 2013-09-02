package com.sdouglas.android.commuteralert;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

import android.location.Address;
import android.location.Location;

public interface HomeImplementer {
	void heresYourAddress(Address address, String readableAddress, LatLng whereImAt);
	void dropPin(Address a);
	void showPlaystoreAPIErrorDialog(int code);
	void positionMapToLocation(double latitude, double longitude);
}
