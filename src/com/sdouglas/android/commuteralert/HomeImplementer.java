package com.sdouglas.android.commuteralert;

import java.util.ArrayList;

import android.location.Address;
import android.location.Location;

public interface HomeImplementer {
	void heresYourAddress(Address address, String readableAddress);
	void heresTheTrainStationAddressesToDisplayOnMap(ArrayList<Address> addresses,Location location);
	void dropPin(Address a);
	void showPlaystoreAPIErrorDialog(int code);
}
