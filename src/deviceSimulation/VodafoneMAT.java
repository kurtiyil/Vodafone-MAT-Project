package deviceSimulation;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.net.util.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import com.ibm.iotf.util.LoggerUtility;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.cloudant.CloudantClientMgr;
import com.ibm.iotf.model.Asset;
import com.ibm.iotf.model.AssetData;
import com.ibm.iotf.model.AssetlistPerUser;
import com.ibm.iotf.model.Config;
import com.ibm.iotf.sample.client.device.RegisteredDeviceEventPublish;
import com.ibm.iotf.rest.client.*;

public class VodafoneMAT {
	
	final static int NoOfDevices = 2;
	static String vodafoneUserURL = null;
	static String vodafoneAssetURL = null;
	//static String vodafoneURL = "http://localhost:8080/UserService.svc/Rest";
	static String org = null;
	static String devicetype = null;
	static String username = null;
	static String password = null;
	static String token = null;
	static String devicepassword = null;
	static String devicetoken = null;
	static String apikey = null;
	static String apitoken = null;
	static String customerName = null;
	static String deviceTypeURL = null;
	static String deviceAddURL = null;
	static List<Asset> deviceList = new ArrayList <Asset>();
	
	private void initialize()
	{
		Config config = CloudantClientMgr.readConfigfromCloudant("**VodafoneMATBridgeConfig**");
		
		
		devicetype="VodafoneMATAsset";
		devicepassword = "passw0rd";
		devicetoken = "token";
		
/*		org = CloudantClientMgr.readConfigfromCloudant("org");
		customerName = CloudantClientMgr.readConfigfromCloudant("customer");
		username = CloudantClientMgr.readConfigfromCloudant("username");
		password = CloudantClientMgr.readConfigfromCloudant("password");
		apikey = CloudantClientMgr.readConfigfromCloudant("APIKey");
		apitoken = CloudantClientMgr.readConfigfromCloudant("APIToken");*/
		
		deviceTypeURL = "https://" + org  + ".internetofthings.ibmcloud.com/api/v0002/device/types";
		deviceAddURL = "https://" + org + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/add";
		
		//Real Vodafone MAT System
		//vodafoneUserURL = "https://matapi.vodafone.com/UserService.svc/Rest/UserAuthenticate";
		//vodafoneAssetURL = "https://matapi.vodafone.com/AssetService.svc/rest";
		
		//Mock Service
		vodafoneUserURL = "http://localhost:8080/UserService.svc/Rest/UserAuthenticate";
		vodafoneAssetURL = "http://localhost:8080/UserService.svc/Rest";
		
	}
	
	public void doJob() throws InterruptedException, ParseException {
	
	
	// TODO Auto-generated method stub

	//String deviceid = null;
//	String id[]={"Device01","Device02", "Device03","Device04","Device05"};
	//String token="token";
	//String authtoken = null;
//	String password[]={"qwerty123","QES5NJ2TO8hpFmpaOA","CcXFDmf!qYEOk1Aaqi","i5J1JsjPhu13AzKh2&","UM+OKeBi-wGHtgS2Zy"};
	
		initialize();
	
		//System.out.println(customerName);
		ConnecttoMATPortal();
		GetDeviceList();
		//CheckDeviceList();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		SimpleDateFormat ft = new SimpleDateFormat ("dd/MM/yyyy");
		System.out.println("Yesterday's date = "+ ft.format(cal.getTime()));

		//Boolean isSuccesfull = ReadDeviceStatus(CloudantClientMgr.readConfigfromCloudant("lastrun"), ft.format(dNow));
		Boolean isSuccesfull = ReadDeviceStatus(ft.format(cal.getTime()));
		if (isSuccesfull){
		//	CloudantClientMgr.updateConfig("lastrun",ft.format(dNow));
			return;
		}
	
	//Function called to create device type
	//Devicetype(type, org);
	
	//Function called to register devices in Watson IoT Platform Service
	//RegisterDevices(type, org);
	
/*	for(int i=1; i<=NoOfDevices; i++)
	{
		if(i<10){	
			
			deviceid = "Device0" + i;
			authtoken = "1Q2w3e4r";
		}
		else{
			deviceid = "Device" + i;
			authtoken = "qwerty" + i;
		}*/
	//	RegisteredDeviceEventPublish.simulateDevice(org, type, deviceID, token, password);
    // new RegisteredDeviceEventPublish(org, type, deviceid, token, authtoken,(new Double(Math.random()*10)).intValue()+1).run();
	//}
}

	private Boolean ReadDeviceStatus(String dNow) {
		createDevicetype();
		for(Asset device : deviceList) {
			String lastrundate=readLastRunDate(device);
			if (lastrundate.equals(null))
			{
				registerDevices(device.getName(), device.getAssetId());
			}
			
			Properties options = new Properties();
			options.setProperty("org", org);
			options.setProperty("type", devicetype);
			options.setProperty("id", device.getAssetId());
			options.setProperty("auth-method", devicetoken);
			options.setProperty("auth-token", devicepassword);
			DeviceClient myClient = null;
			try {
				myClient = new DeviceClient(options);
				myClient.connect();
				List<AssetData> assetData  = VodafoneAssetClient.assetDataPerDate (vodafoneAssetURL, device.getName(), lastrundate, dNow, username, token);
				for (AssetData as : assetData){
					
					//Generate a JSON object of the event to be published
					JsonObject event = new JsonObject();
				
					myClient.publishEvent("blink", event, device.getAssetId(), org, devicetype,as);
					System.out.println("SUCCESSFULLY POSTED TO DEVICE ......"+ device.getAssetId());
				}
				myClient.disconnect();
				updateLastRunDate(device);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	
		
		return true;
	}

	private void updateLastRunDate(Asset device) {
		// TODO Auto-generated method stub
		
	}

	private String readLastRunDate(Asset device) {
		// TODO Auto-generated method stub
		return "10/01/2014";
	}

	private void CheckDeviceList() {
		for(Asset device : deviceList) {
			
			//if (CloudantClientMgr.readDevicefromCloudant(device.getName()) == null) {
			//	CloudantClientMgr.createDevice(device.getName(), device.getAssetId());
				registerDevices(device.getName(), device.getAssetId());
			//}
		}	
	}

	private void GetDeviceList() {
		try {
			deviceList=VodafoneAssetClient.assetListPerUser(vodafoneAssetURL, username, token);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void ConnecttoMATPortal() {
		// TODO Auto-generated method stub
		try {
			token = VodafoneAssetClient.userAuthenticate(vodafoneUserURL, username, password,customerName);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

public static void createDevicetype(){
	
	String devicetypebody = null;
	
	
	
	devicetypebody = DevicetypeBody(devicetype);
	
	//url = "https://" + org  + ".internetofthings.ibmcloud.com/api/v0002/device/types"; 
	
	HttpPost httpPost = new HttpPost(deviceTypeURL);
	StringEntity requestEntity = new StringEntity(
		    devicetypebody,
		    ContentType.APPLICATION_JSON);
		/*
		 * Execute the HTTP Request
		 */

	httpPost.setEntity(requestEntity);
	httpPost.addHeader("Content-Type", "application/json");
	httpPost.addHeader("Accept", "application/json");
	
	
		byte[] encoding = Base64.encodeBase64(new String(apikey + ":" + apitoken).getBytes() );			
		String encodedString = new String(encoding);
		httpPost.addHeader("Authorization", "Basic " + encodedString);
	
	
	try {
		
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLSv1.2");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sslContext.init(null, null, null);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpClient client = HttpClientBuilder.create().setSslcontext(sslContext).build();
		
		HttpResponse response = client.execute(httpPost);
		
		int httpCode = response.getStatusLine().getStatusCode();

		System.out.println(httpCode);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
}

public static void registerDevices(String deviceName, String deviceID){
	
//	String devicebody = "[ { \"typeId\": \"WaterPump\", \"deviceId\": \"Device01\",\"deviceInfo\": { \"serialNumber\": \"string\", \"manufacturer\": \"string\", \"model\": \"string\", \"deviceClass\": \"string\",  \"description\": \"string\", \"fwVersion\": \"string\",\"hwVersion\": \"string\",\"descriptiveLocation\": \"string\"}, \"location\": {\"longitude\": 0, \"latitude\": 0,\"elevation\": 0, \"accuracy\": 0, \"measuredDateTime\": \"2016-05-06T10:23:57.999Z\"  }, \"metadata\": {}, \"authToken\": \"qwerty01\"} ]";
	String devicebody = null;
	/*	String deviceid = null;
	String authtoken = null;
	String url = null;*/
	
/*	for (int i=1; i<=NoOfDevices; i++){
	
	if(i<10){	
	
		deviceid = "Device0" + i;
		authtoken = "1Q2w3e4r";
	}
	else{
		deviceid = "Device" + i;
		authtoken = "qwerty" + i;
	}
	*/
	
	
	devicebody = DeviceBody(deviceID, devicetype, devicepassword, deviceName);
	
	HttpPost httpPost = new HttpPost(deviceAddURL);
	StringEntity requestEntity = new StringEntity(
		    devicebody,
		    ContentType.APPLICATION_JSON);
		/*
		 * Execute the HTTP Request
		 */
	
	httpPost.setEntity(requestEntity);
	httpPost.addHeader("Content-Type", "application/json");
	httpPost.addHeader("Accept", "application/json");
	
	
		byte[] encoding = Base64.encodeBase64(new String(apikey + ":" + apitoken).getBytes() );			
		String encodedString = new String(encoding);
		httpPost.addHeader("Authorization", "Basic " + encodedString);
	
	
	try {
		
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLSv1.2");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sslContext.init(null, null, null);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpClient client = HttpClientBuilder.create().setSslcontext(sslContext).build();
		
		HttpResponse response = client.execute(httpPost);
		
		int httpCode = response.getStatusLine().getStatusCode();

		System.out.println(httpCode);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}

	//} 
	
	
}


public static String DeviceBody(String deviceid, String devicetype, String authtoken, String description){

	String body = null;
	
	body = "[ { \"typeId\": \"" + StringEscapeUtils.escapeJson(devicetype) + "\", \"deviceId\": \"" + StringEscapeUtils.escapeJson(deviceid) + "\",\"deviceInfo\": { \"serialNumber\": \"string\", \"manufacturer\": \"string\", \"model\": \"string\", \"deviceClass\": \"string\",  \"description\": \"" + description +"\", \"fwVersion\": \"string\",\"hwVersion\": \"string\",\"descriptiveLocation\": \"string\"}, \"location\": {\"longitude\": 0, \"latitude\": 0,\"elevation\": 0, \"accuracy\": 0, \"measuredDateTime\": \"2016-05-06T10:23:57.999Z\"  }, \"metadata\": {}, \"authToken\": \"" + StringEscapeUtils.escapeJson(authtoken) + "\"} ]";
	
	return body;
}

public static String DevicetypeBody (String devicetype){
	
	String body = null;
	
	body = "{ \"id\": \"" + StringEscapeUtils.escapeJson(devicetype) + "\", \"description\": \"string\", \"classId\": \"Device\", \"deviceInfo\": { \"serialNumber\": \"string\", \"manufacturer\": \"string\", \"model\": \"string\", \"deviceClass\": \"string\", \"description\": \"string\", \"fwVersion\": \"string\", \"hwVersion\": \"string\",	\"descriptiveLocation\": \"string\" },\"metadata\": {}}";

	return body;
}

	
}
