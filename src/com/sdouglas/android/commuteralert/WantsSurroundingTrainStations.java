package com.sdouglas.android.commuteralert;

import java.util.ArrayList;

import android.location.Address;
import android.location.Location;

public interface WantsSurroundingTrainStations {
	void hereAreTheTrainStationAddresses(ArrayList<Address> addresses,Location location);
}