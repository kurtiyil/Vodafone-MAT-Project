/**
 *****************************************************************************
 * Copyright (c) 2015 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Amit M Mangalvedkar - Initial Contribution
 *****************************************************************************
 */

/**
 * This sample shows how we can write a device client which publishes events, in a Registered mode <br>
 * It uses the Java Client Library for IBM Watson IoT Platform
 * This sample code should be executed in a JRE running on the device
 * 
 */

package com.ibm.iotf.sample.client.device;

import java.util.Properties;

import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;

public class RegisteredDeviceEventPublish implements Runnable {

		
	String org=null;
	String type=null;
	String deviceID=null;
	String token=null;
	String password=null;
	int timeLag=0;
	
	public RegisteredDeviceEventPublish(String org, String type, String deviceID, String token, String password, int gap)
	{
		setProperties(org,type,deviceID,token,password, gap);
	}
	
	public void setProperties(String org, String type, String deviceID, String token, String password, int gap)
	{

		this.org=org;
		this.type=type;
		this.deviceID=deviceID;
		this.token=token;
		this.password=password;	
		this.timeLag=gap;
		
	}
		//Disconnect cleanly
	//	myClient.disconnect()

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Properties options = new Properties();

		options.setProperty("org", org);
		options.setProperty("type", type);
		options.setProperty("id", deviceID);
		options.setProperty("auth-method", token);
		options.setProperty("auth-token", password);
		
		DeviceClient myClient = null;
		try {
			//Instantiate the class by passing the properties file
			myClient = new DeviceClient(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Connect to the IBM Watson IoT Platform
		
		myClient.connect();
		
		//Generate a JSON object of the event to be published
		JsonObject event = new JsonObject();
	
		
		//Registered flow allows 0, 1 and 2 QoS
//		while (true)
//		{
//		try {
//			Thread.sleep(this.timeLag*1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		myClient.publishEvent("blink", event, deviceID, org, type);
		System.out.println("SUCCESSFULLY POSTED TO DEVICE ......"+ deviceID);
		myClient.disconnect();
	//	}
		
	}
}
