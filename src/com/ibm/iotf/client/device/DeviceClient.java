package com.ibm.iotf.client.device;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.AbstractClient;
import com.ibm.iotf.model.AssetData;
import com.ibm.iotf.util.LoggerUtility;


/**
 * A client, used by device, that handles connections with the IBM Watson IoT Platform. <br>
 * 
 * This is a derived class from AbstractClient and can be used by embedded devices to handle connections with IBM Watson IoT Platform.
 */
public class DeviceClient extends AbstractClient {
	
	int count = 0;
	Double d = new Double ((Math.random()*30))+1;
	int max = d.intValue();
			
	
	private static final String CLASS_NAME = DeviceClient.class.getName();
	
	private static final Pattern COMMAND_PATTERN = Pattern.compile("iot-2/cmd/(.+)/fmt/(.+)");
	
	private CommandCallback commandCallback = null;
	
	/**
	 * This constructor allows external user to pass the existing MqttAsyncClient 
	 * @param mqttAsyncClient
	 */
	protected DeviceClient(MqttAsyncClient mqttAsyncClient) {
		super(mqttAsyncClient);
	}

	/**
	 * This constructor allows external user to pass the existing MqttClient 
	 * @param mqttClient
	 */
	protected DeviceClient(MqttClient mqttClient) {
		super(mqttClient);
	}
	/**
	 * Create a device client for the IBM Watson IoT Platform. <br>
	 * 
	 * Connecting to a specific account on the IoTF.
	 * @throws Exception 
	 */
	public DeviceClient(Properties options) throws Exception {
		super(options);
		LoggerUtility.fine(CLASS_NAME, "DeviceClient", "options   = " + options);
		this.clientId = "d" + CLIENT_ID_DELIMITER + getOrgId() + CLIENT_ID_DELIMITER + getDeviceType() + CLIENT_ID_DELIMITER + getDeviceId();
		
		if (getAuthMethod() == null) {
			this.clientUsername = null;
			this.clientPassword = null;
		}
		else if (!getAuthMethod().equals("token")) {
			throw new Exception("Unsupported Authentication Method: " + getAuthMethod());
		}
		else {
			// use-token-auth is the only authentication method currently supported
			this.clientUsername = "use-token-auth";
			this.clientPassword = getAuthToken();
		}
		createClient(this.new MqttDeviceCallBack());
	}
	
	/*
	 * old style - type
	 * new style - Device-Type
	 */
	public String getDeviceType() {
		String type = null;
		type = options.getProperty("type");
		if(type == null) {
			type = options.getProperty("Device-Type");
		}
		return trimedValue(type);
	}

	public String getFormat() {
		String format = options.getProperty("format");
		if(format != null && ! format.equals(""))
			return format;
		else
			return "json";
		
	}
	
	/**
	 * Connect to the IBM Watson IoT Platform
	 * 
	 */	
	@Override
	public void connect() {
		super.connect();
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	/*
	 * This method reconnects when the connection is lost due to n/w interruption
	 */
	protected void reconnect() {
		super.connect();
		if (!getOrgId().equals("quickstart")) {
			subscribeToCommands();
		}
	}
	
	private void subscribeToCommands() {
		try {
			mqttAsyncClient.subscribe("iot-2/cmd/+/fmt/" + getFormat(), 2);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
	 * Note that data is published
	 * at Quality of Service (QoS) 0, which means that a successful send does not guarantee
	 * receipt even if the publish has been successful.
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @return Whether the send was successful.
	 */
	public boolean publishEvent(String event, Object data, String deviceID, String org, String type, AssetData as) {
		return publishEvent(event, data, 0, deviceID, org, type, as);
	}

	/**
	 * Publish data to the IBM Watson IoT Platform.<br>
	 * 
	 * This method allows QoS to be passed as an argument
	 * 
	 * @param event
	 *            Name of the dataset under which to publish the data
	 * @param data
	 *            Object to be added to the payload as the dataset
	 * @param qos
	 *            Quality of Service - should be 0, 1 or 2
	 * @return Whether the send was successful.
	 */	
	public boolean publishEvent(String event, Object data, int qos, String deviceID, String org, String type, AssetData as) {
		if (!isConnected()) {
			return false;
		}
		final String METHOD = "publishEvent(2)";
		JsonObject payload = new JsonObject();
		
		String timestamp = ISO8601_DATE_FORMAT.format(new Date());
		payload.addProperty("ts", timestamp);
		
		
		
/*		double temp;
		
		double lower_temp = -50.0;
		double upper_temp = 35.0;

		double lower_pressure = 0;

		double upper_pressure = 120;
		
		double energy_consumption = 150;
		
		double water_usage_lower_value = 0;
		
		double water_usage_higher_value = 2000;
	
		if(count < max){
			
			
			
		 temp = Math.random() * (upper_temp -lower_temp) + lower_temp;
		count++;
		}
		else{
			System.out.println("sending alert for device " + deviceID + "after " + max + "messages");
		  temp = 45.0;
			count = 0;
		}
		double pressure = Math.random() * (upper_pressure - lower_pressure) + lower_pressure;
		
		double water_usage = Math.random() * (water_usage_higher_value - water_usage_lower_value) + water_usage_lower_value;*/
		//Generate a JSON object of the event to be published
	//	JsonObject event = new JsonObject();
		((JsonObject)data).addProperty("BatteryLevel", as.getBatteryLevel());
		((JsonObject)data).addProperty("BatteryVoltage", as.getBatteryVoltage());
		((JsonObject)data).addProperty("Date", as.getDate());
		((JsonObject)data).addProperty("DeviceProfile", as.getDeviceProfile());
		((JsonObject)data).addProperty("LatX", as.getLatX());
		((JsonObject)data).addProperty("LocationString", as.getLocationString());
		((JsonObject)data).addProperty("LongY", as.getLongY());
		((JsonObject)data).addProperty("PacketType", as.getPacketType());
		((JsonObject)data).addProperty("PositionMethod", as.getPositionMethod());
		((JsonObject)data).addProperty("PowerSupplyVoltage", as.getPowerSupplyVoltage());
		((JsonObject)data).addProperty("Satelites", as.getSatelites());
		((JsonObject)data).addProperty("isRoaming", as.getIsRoaming());

			
		JsonElement dataElement = gson.toJsonTree(data);
		
		payload.add("d", dataElement); 
	//	payload.addProperty("deviceId", "ftkgac:IOTsample_devicetype:"+ deviceID);
		payload.addProperty("deviceId", org + ":" + type + ":"+ deviceID);
		
		String topic = "iot-2/evt/" + event + "/fmt/json";
		
		LoggerUtility.fine(CLASS_NAME, METHOD, "Topic   = " + topic);
		LoggerUtility.fine(CLASS_NAME, METHOD, "Payload = " + payload.toString());
		
		MqttMessage msg = new MqttMessage(payload.toString().getBytes(Charset.forName("UTF-8")));
		msg.setQos(qos);
		msg.setRetained(false);
		
		try {
			mqttAsyncClient.publish(topic, msg).waitForCompletion();
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	

	
	private class MqttDeviceCallBack implements MqttCallback {
	
		/**
		 * If we lose connection trigger the connect logic to attempt to
		 * reconnect to the IBM Watson IoT Platform.
		 * 
		 * @param exception
		 *            Throwable which caused the connection to get lost
		 */
		public void connectionLost(Throwable exception) {
			final String METHOD = "connectionLost";
			LoggerUtility.info(CLASS_NAME, METHOD, exception.getMessage());
			reconnect();
		}
		
		/**
		 * A completed deliver does not guarantee that the message is received by the service
		 * because devices send messages with Quality of Service (QoS) 0. <br>
		 * 
		 * The message count
		 * represents the number of messages that were sent by the device without an error on
		 * from the perspective of the device.
		 * @param token
		 *            MQTT delivery token
		 */
		public void deliveryComplete(IMqttDeliveryToken token) {
			final String METHOD = "deliveryComplete";
			LoggerUtility.fine(CLASS_NAME, METHOD, "token " + token.getMessageId());
			messageCount++;
		}
		
		/**
		 * The Device client does not currently support subscriptions.
		 */
		public void messageArrived(String topic, MqttMessage msg) throws Exception {
			final String METHOD = "messageArrived";
			if (commandCallback != null) {
				/* Only check whether the message is a command if a callback 
				 * has been defined, otherwise it is a waste of time
				 * as without a callback there is nothing to process the generated
				 * command.
				 */
				Matcher matcher = COMMAND_PATTERN.matcher(topic);
				if (matcher.matches()) {
					String command = matcher.group(1);
					String format = matcher.group(2);
					Command cmd = new Command(command, format, msg);
					LoggerUtility.fine(CLASS_NAME, METHOD, "Event received: " + cmd.toString());
					commandCallback.processCommand(cmd);
			    }
			}
		}

	}
	
	public void setCommandCallback(CommandCallback callback) {
		this.commandCallback  = callback;
	}
	
	/**
	 * Publish an event to the IBM Watson IoT Platform using HTTP(S)<br>
	 * 
	 * @param eventName  Name of the dataset under which to publish the data
	 * @param payload Object to be added to the payload as the dataset
	 * @return httpcode the return code
	 * @throws Exception if the operation is not successful
	 */
	public int publishEventOverHTTP(String eventName, Object payload) throws Exception {
		String authKey = "use-token-auth";
		return publishEventsThroughHttps(this.getOrgId(), this.getDeviceType(), this.getDeviceId(), 
				eventName, true, authKey, this.getAuthToken(), payload);
	}
	
}
