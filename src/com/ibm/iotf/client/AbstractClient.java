/**
 *****************************************************************************
 Copyright (c) 2015-16 IBM Corporation and other Contributors.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Sathiskumar Palaniappan - Extended from DeviceClient
 *****************************************************************************
 *
 */
package com.ibm.iotf.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.SSLContext;

import org.apache.commons.net.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.util.LoggerUtility;

/**
 * A client that handles connections with the IBM Watson IoT Platform. <br>
 * This is an abstract class which has to be extended
 */
public abstract class AbstractClient {
	
	private static final String CLASS_NAME = AbstractClient.class.getName();
	protected static final String CLIENT_ID_DELIMITER = ":";
	
	//protected static final String DOMAIN = "messaging.staging.internetofthings.ibmcloud.com";
	protected static final String DOMAIN = "messaging.internetofthings.ibmcloud.com";
	protected static final int MQTT_PORT = 1883;
	protected static final int MQTTS_PORT = 8883;
	
	/* Wait for 1 second after each attempt for the first 10 attempts*/
	private static final long RATE_0 = TimeUnit.SECONDS.toMillis(1);
	
	/* After 5 attempts throttle the rate of connection attempts to 1 per 10 second */
	private static final int THROTTLE_1 = 5;
	private static final long RATE_1 = TimeUnit.SECONDS.toMillis(10);
	
	/* After 10 attempts throttle the rate of connection attempts to 1 per minute */
	private static final int THROTTLE_2 = 10;
	private static final long RATE_2 = TimeUnit.MINUTES.toMillis(1);
	
	/* After 20 attempts throttle the rate of connection attempts to 1 per 5 minutes */
	private static final int THROTTLE_3 = 20;
	private static final long RATE_3 = TimeUnit.MINUTES.toMillis(5);
	
	protected static final Gson gson = new Gson();
	
	/**
	 * A formatter for ISO 8601 compliant timestamps.
	 */
	protected static final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	protected Properties options;
	protected String clientId;
	protected String clientUsername;
	protected String clientPassword;
	
	protected int messageCount = 0;
	
	protected MqttAsyncClient mqttAsyncClient = null;
	protected MqttConnectOptions mqttClientOptions;
	protected MqttCallback mqttCallback;
	
	// Supported only for DM ManagedClient
	protected MqttClient mqttClient = null;

	/**
	 * Note that this class does not have a default constructor <br>
	 * @param options
	 * 			Properties object which contains different artifacts such as auth-key
	 * 
	 */		
	
	public AbstractClient(Properties options) {
		this.options = options;
	}
	
	/**
	 * This constructor allows external user to pass the existing MqttAsyncClient 
	 * @param mqttAsyncClient
	 */
	protected AbstractClient(MqttAsyncClient mqttAsyncClient) {
		this.mqttAsyncClient = mqttAsyncClient;
	}

	/**
	 * This constructor allows external user to pass the existing MqttClient 
	 * @param mqttClient
	 */
	protected AbstractClient(MqttClient mqttClient) {
		this.mqttClient = mqttClient;
	}
	
	/**
	 * Create the Paho MQTT Client that will underpin the Device client.
	 * @param callback
	 * 			MqttCallback 
	 * @see <a href="Paho Client Library">http://www.eclipse.org/paho/files/javadoc/index.html</a> 
	 * 
	 */	

	protected void createClient(MqttCallback callback) {
		LoggerUtility.info(CLASS_NAME, "createClient", "Org ID    = " + getOrgId() +
				"\n         Client ID    = " + clientId);
		this.mqttAsyncClient = null;
		this.mqttClientOptions = new MqttConnectOptions();
		this.mqttCallback = callback;
	}
	
	/**
	 * Connect to the IBM Watson IoT Platform
	 */
	public void connect() {
		final String METHOD = "connect";
		boolean tryAgain = true;
		int connectAttempts = 0;

		if (getOrgId() == "quickstart") {
			configureMqtt();
		}
		else {
			configureMqtts();
		}
		
		while (tryAgain) {
			connectAttempts++;
			
			LoggerUtility.info(CLASS_NAME, METHOD, "Connecting client "+ this.clientId + " to " + mqttAsyncClient.getServerURI() + 
					" (attempt #" + connectAttempts + ")...");
			
			try {
				mqttAsyncClient.connect(mqttClientOptions).waitForCompletion(1000 * 60);
			} catch (MqttSecurityException e) {
				System.err.println(e.getMessage());
				System.err.println("Looks like one or more connection parameters are wrong !!!");
				System.exit(-1);
				
			} catch (MqttException e) {
				Throwable t = e.getCause();
				if(t != null && t instanceof java.net.UnknownHostException) {
					// We must give up as the host doesn't exist.
					System.err.println(t.getMessage());
					System.err.println("Disconnecting as the host is unreachable !!!");
					System.exit(-1);
				}
				e.printStackTrace();
			}
			
			if (mqttAsyncClient.isConnected()) {
				LoggerUtility.info(CLASS_NAME, METHOD, "Successfully connected "
						+ "to the IBM Watson IoT Platform");
				
				if (LoggerUtility.isLoggable(Level.FINEST)) {
					LoggerUtility.log(Level.FINEST, CLASS_NAME, METHOD, 
							" * Connection attempts: " + connectAttempts);
				}
				
				tryAgain = false;
			} else {
				waitBeforeNextConnectAttempt(connectAttempts);
			}
		}
	}
	
	private void configureMqtt() {
		String serverURI = "tcp://" + getOrgId() + "." + DOMAIN + ":" + MQTT_PORT;
		try {
			mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, null);
			mqttAsyncClient.setCallback(mqttCallback);
			mqttClientOptions = new MqttConnectOptions();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	private void configureMqtts() {
		final String METHOD = "configureMqtts";
		String serverURI = "ssl://" + getOrgId() + "." + DOMAIN + ":" + MQTTS_PORT;
		try {
			mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, null);
			mqttAsyncClient.setCallback(mqttCallback);
			
			mqttClientOptions = new MqttConnectOptions();
			mqttClientOptions.setUserName(clientUsername);
			mqttClientOptions.setPassword(clientPassword.toCharArray());
			mqttClientOptions.setCleanSession(this.isCleanSession());
			
			/* This isn't needed as the production messaging.internetofthings.ibmcloud.com 
			 * certificate should already be in trust chain.
			 * 
			 * See: 
			 *   http://stackoverflow.com/questions/859111/how-do-i-accept-a-self-signed-certificate-with-a-java-httpsurlconnection
			 *   https://gerrydevstory.com/2014/05/01/trusting-x509-base64-pem-ssl-certificate-in-java/
			 *   http://stackoverflow.com/questions/12501117/programmatically-obtain-keystore-from-pem
			 *   https://gist.github.com/sharonbn/4104301
			 * 
			 * CertificateFactory cf = CertificateFactory.getInstance("X.509");
			 * InputStream certFile = AbstractClient.class.getResourceAsStream("messaging.pem");
			 * Certificate ca = cf.generateCertificate(certFile);
			 *
			 * KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			 * keyStore.load(null, null);
			 * keyStore.setCertificateEntry("ca", ca);
			 * TrustManager trustManager = TrustManagerUtils.getDefaultTrustManager(keyStore);
			 * SSLContext sslContext = SSLContextUtils.createSSLContext("TLSv1.2", null, trustManager);
			 * 
			 */
			 
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, null);
			mqttClientOptions.setSocketFactory(sslContext.getSocketFactory());
		} catch (MqttException | GeneralSecurityException e) {
			LoggerUtility.warn(CLASS_NAME, METHOD, "Unable to configure TLSv1.2 connection: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Check whether the clean session is disabled
	 * 
	 * @return
	 */
	private boolean isCleanSession() {
		boolean enabled = true;
		String value = options.getProperty("Clean-Session");
		if(value == null) {
			value = options.getProperty("clean-session");
		}
		if(value != null) {
			enabled = Boolean.parseBoolean(trimedValue(value));
		} 
		return enabled;
	}

	/**
	 * Sleep for a variable period of time between connect attempts.
	 * 
	 * @param attempts
	 *               How many times have we tried (and failed) to connect
	 */
	private void waitBeforeNextConnectAttempt(final int attempts) {
		final String METHOD = "waitBeforeNextConnectAttempt";
		// Log when throttle boundaries are reached
		if (attempts == THROTTLE_3) {
			LoggerUtility.warn(CLASS_NAME, METHOD, String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_3) + "ms");
		}
		else if (attempts == THROTTLE_2) {
			LoggerUtility.warn(CLASS_NAME, METHOD, String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay increased to " + String.valueOf(RATE_2) + "ms");
		}
		else if (attempts == THROTTLE_1) {
			LoggerUtility.info(CLASS_NAME, METHOD, String.valueOf(attempts) + 
					" consecutive failed attempts to connect.  Retry delay set to " + String.valueOf(RATE_1) + "ms");
		}

		try {
			long delay = RATE_0;
			if (attempts >= THROTTLE_3) {
				delay = RATE_3;
			} else if (attempts >= THROTTLE_2) {
				delay = RATE_2;
			} else if (attempts >= THROTTLE_1) {
				delay = RATE_1;
			}
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Disconnect the device from the IBM Watson IoT Platform
	 */
	public void disconnect() {
		final String METHOD = "disconnect";
		LoggerUtility.fine(CLASS_NAME, METHOD, "Disconnecting from the IBM Watson IoT Platform ...");
		try {
			mqttAsyncClient.disconnect();
			LoggerUtility.info(CLASS_NAME, METHOD, "Successfully disconnected "
					+ "from from the IBM Watson IoT Platform");
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Determine whether this device is currently connected to the IBM Watson Internet
	 * of Things Platform.
	 * 
	 * @return Whether the device is connected to the IBM Watson IoT Platform
	 */
	public boolean isConnected() {
		final String METHOD = "isConnected";
		boolean connected = false;
		if (mqttAsyncClient != null) {
			connected = mqttAsyncClient.isConnected();
		} else if (mqttClient != null) {
			connected = mqttClient.isConnected();
		}
		LoggerUtility.log(Level.FINEST, CLASS_NAME, METHOD, "Connected(" + connected + ")");
		return connected;
	}
	
	/**
	 * Provides a human readable String representation of this Device, including the number
	 * of messages sent and the current connect status.
	 * 
	 * @return String representation of the Device.
	 */
	public String toString() {
		return "[" + clientId + "] " + messageCount + " messages sent - Connected = " + String.valueOf(isConnected());
	}

	/**
	 * Parses properties file and returns back an object of Properties class
	 * 
	 * @param propertiesFile
	 * 						File object
	 * @return properties
	 * 						Properties object
	 */	
	public static Properties parsePropertiesFile(File propertiesFile) {
		Properties clientProperties = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(propertiesFile);
			clientProperties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return clientProperties;
		} catch (IOException e) {
			e.printStackTrace();
			return clientProperties;
		}
		return clientProperties;
	}
	
	/*
	 * old style - org
	 * new style - Organization-ID
	 */
	public String getOrgId() {
		String org = null;
		org = options.getProperty("org");
		
		if(org == null) {
			org = options.getProperty("Organization-ID");
		}
		return trimedValue(org);
	}
	
	/*
	 * old style - id
	 * new style - Device-ID
	 */
	public String getDeviceId() {
		String id = null;
		id = options.getProperty("id");
		if(id == null) {
			id = options.getProperty("Device-ID");
		}
		return trimedValue(id);
	}
	
	public static String trimedValue(String value) {
		if(value != null) {
			return value.trim();
		}
		return value;
	}

	/**
	 * Accessor method to retrieve Authendication Method
	 * old style - auth-method
	 * new style - Authentication-Method
	 */	
	public String getAuthMethod() {
		String method = options.getProperty("auth-method");
		if(method == null) {
			method = options.getProperty("Authentication-Method");
		}
		return trimedValue(method);
	}

	/*
	 * old style - auth-token
	 * new style - Authentication-Token
	 */
	public String getAuthToken() {
		String token = options.getProperty("auth-token");
		if(token == null) {
			token = options.getProperty("Authentication-Token");
		}
		return trimedValue(token);
	}

	
	private static void validateNull(String property, String value) throws Exception {
		if(value == null || value == "") {
			throw new Exception(property +" cannot be null or empty !");
		}
	}
	
	/**
	 * @param organization  Organization ID (Either "quickstart" or the registered organization ID)
	 * @param deviceType	Device Type
	 * @param deviceId		Device ID
	 * @param eventName		Name of the Event
	 * @param device 		Boolean value indicating whether the request is originated from device or application
	 * @param authKey		Authentication Method
	 * @param authToken		Authentication Token to securely post this event (Can be null or empty if its quickstart)
	 * @param payload		The message to be published
	 * @return
	 * @throws Exception	throws exception when http post fails
	 */
	protected static int publishEventsThroughHttps(String organization,
			String deviceType,
			String deviceId,
			String eventName,
			boolean device,
			String authKey,
			String authToken,
			Object payload) throws Exception {

		final String METHOD = "publishEventsThroughHttps";

		validateNull("Organization ID", organization);
		validateNull("Device Type", deviceType);
		validateNull("Device ID", deviceId);
		validateNull("Event Name", eventName);
		if("quickstart".equalsIgnoreCase(organization) == false) {
			validateNull("Authentication Method", authKey);
			validateNull("Authentication Token", authToken);
		}

		StringBuilder sb = new StringBuilder();
		
		// Form the URL
		if("quickstart".equalsIgnoreCase(organization)) {
			sb.append("http://");
		} else {
			sb.append("https://");
		}
		sb.append(organization)
			.append(".internetofthings.ibmcloud.com/api/v0002");
			
		if(device == true) {
			sb.append("/device");
		} else {
			sb.append("/application");
		}
		sb.append("/types/")
			.append(deviceType)
			.append("/devices/")
			.append(deviceId)
			.append("/events/")
			.append(eventName);
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "ReST URL::"+sb.toString());
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(System.in));
		
		// Create the payload message in Json format
		JsonObject message = new JsonObject();
		
		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		message.addProperty("ts", timestamp);
		
		JsonElement dataElement = gson.toJsonTree(payload);
		message.add("d", dataElement);
		
		StringEntity input = null;
		try {
			input = new StringEntity(message.toString());
		} catch (UnsupportedEncodingException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, "Unable to carry out the ReST request");
			throw e;
		}
		
		// Create the Http post request
		HttpPost post = new HttpPost(sb.toString());
		post.setEntity(input);
		post.addHeader("Content-Type", "application/json");
		post.addHeader("Accept", "application/json");
		
		if("quickstart".equalsIgnoreCase(organization) == false) {
			byte[] encoding = Base64.encodeBase64(new String(authKey + ":" + authToken).getBytes() );			
			String encodedString = new String(encoding);
			post.addHeader("Authorization", "Basic " + encodedString);
		}
		
		try {
			
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, null, null);

			HttpClient client = HttpClientBuilder.create().setSslcontext(sslContext).build();
			
			HttpResponse response = client.execute(post);
			
			int httpCode = response.getStatusLine().getStatusCode();
			if(httpCode >= 200 && httpCode < 300) {
				return httpCode;
			}
			
			/**
			 * Looks like some error so log the header and response
			 */
			StringBuilder log = new StringBuilder("HTTP Code: "+httpCode);
			log.append("\nURL: ")
				.append(sb.toString())
				.append("\nHeader:\n");
			Header[] headers = response.getAllHeaders();
			for(int i = 0; i < headers.length; i++) {
				log.append(headers[i].getName())
					.append(' ')
					.append(headers[i].getValue())
					.append('\n');
			}
			log.append("\nResponse \n");
			br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			log.append(br.readLine());
			LoggerUtility.severe(CLASS_NAME, METHOD, log.toString());
			
			return httpCode;
		} catch (IOException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, e.getMessage());
			throw e;
		} finally {
			if(br != null) {
				br.close();
			}
		}
	}
}
