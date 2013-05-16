package com.conorstarrs.doineedmycoat.network;

import java.util.List;
import android.location.Address;

public class LocationInfoVO {
	private List<Address> address;
	private String latitude;
	private String longitude;
	
	public List<Address> getAddress() {
		return address;
	}
	public void setAddress(List<Address> address) {
		this.address = address;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}
