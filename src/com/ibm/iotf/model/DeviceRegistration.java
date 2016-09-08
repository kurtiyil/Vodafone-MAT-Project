package com.ibm.iotf.model;

public class DeviceRegistration {
	private DeviceAuth auth;
	private String date;
	

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public DeviceAuth getAuth() {
		return auth;
	}

	public void setAuth(DeviceAuth auth) {
		this.auth = auth;
	}
	

}
