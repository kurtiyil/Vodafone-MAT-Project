package com.ibm.iotf.model;

public class Device {
	  private String clientId; 
	  private String typeId;
	  private String deviceId;
	  private DeviceInfo deviceInfo;
	  private DeviceMetadata metadata;
	  private DeviceRegistration registration;
	  private DeviceStatus status;
	  private DeviceRefs refs;
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getTypeId() {
		return typeId;
	}
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}
	public void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
	public DeviceMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(DeviceMetadata metadata) {
		this.metadata = metadata;
	}
	public DeviceRegistration getRegistration() {
		return registration;
	}
	public void setRegistration(DeviceRegistration registration) {
		this.registration = registration;
	}
	public DeviceStatus getStatus() {
		return status;
	}
	public void setStatus(DeviceStatus status) {
		this.status = status;
	}
	public DeviceRefs getRefs() {
		return refs;
	}
	public void setRefs(DeviceRefs refs) {
		this.refs = refs;
	}



}
