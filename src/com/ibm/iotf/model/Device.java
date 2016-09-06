package com.ibm.iotf.model;

public class Device {
	  private String _id; 
	  private String _rev;
	  private String assetId;
	  private String name;
	  
	public String getAssetId() {
		return assetId;
	}
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
