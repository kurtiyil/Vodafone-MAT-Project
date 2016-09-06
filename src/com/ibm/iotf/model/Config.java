package com.ibm.iotf.model;

public class Config {
	  private String _id; 
	  private String _rev;
	  private String config;
	  private String value;
	  
	  public String getConfig() {
		return config;
	}
	  public void setConfig(String config) {
		this.config = config;
	}
	  public String getValue() {
		return value;
	}
	  public void setValue(String value) {
		this.value = value;
	}
}
