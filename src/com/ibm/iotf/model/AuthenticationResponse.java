package com.ibm.iotf.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthenticationResponse {

	private String Status;
	private String Token;
	private String UserName;
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public String getToken() {
		return Token;
	}
	public void setToken(String token) {
		Token = token;
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	

	
}
