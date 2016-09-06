package deviceSimulation;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.ParserConfigurationException;

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
import com.ibm.json.java.JSONObject;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;
import com.ibm.iotf.cloudant.CloudantClientMgr;
import com.ibm.iotf.model.Asset;
import com.ibm.iotf.sample.client.device.RegisteredDeviceEventPublish;
import com.ibm.iotf.rest.client.*;

public class VodafoneMAT {
	
	final static int NoOfDevices = 2;
	static String url = null;
	static String vodafoneURL = "http://localhost:8080/UserService.svc/Rest";
	static String org = null;
	static String devicetype = null;
	static String username = null;
	static String password = null;
	static String token = null;
	static String apikey = null;
	static String apitoken = null;
	static String customerName = null;
	static String deviceTypeURL = null;
	static String deviceAddURL = null;
	static List<Asset> deviceList = new ArrayList <Asset>();
	
	private void initialize()
	{
		org = CloudantClientMgr.readConfigfromCloudant("org");
		devicetype="VodafoneMATAsset";
		customerName = CloudantClientMgr.readConfigfromCloudant("customer");
		username = CloudantClientMgr.readConfigfromCloudant("username");
		password = CloudantClientMgr.readConfigfromCloudant("password");
		apikey = CloudantClientMgr.readConfigfromCloudant("APIKey");
		apitoken = CloudantClientMgr.readConfigfromCloudant("APIToken");
		deviceTypeURL = "https://" + org  + ".internetofthings.ibmcloud.com/api/v0002/device/types";
		deviceAddURL = "https://" + org + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/add";
		
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
		GetDeviveList();
		CheckDeviceList();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss.S");
		System.out.println("Current Date: " + ft.format(dNow));
		Boolean isSuccesfull = ReadDeviceStatus(CloudantClientMgr.readConfigfromCloudant("lastrun"), ft.format(dNow));
	
		if (isSuccesfull){
			CloudantClientMgr.updateConfig("lastrun",ft.format(dNow));
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

	private Boolean ReadDeviceStatus(String lastRunDate, String dNow) {
		for(Asset device : deviceList) {
			try {
				JSONObject jsonobj = VodafoneAssetClient.assetDataPerDate (vodafoneURL, device.getName(), lastRunDate, dNow, username, token);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
		return true;
	}

	private void CheckDeviceList() {

		for(Asset device : deviceList) {
			
			if (CloudantClientMgr.readDevicefromCloudant(device.getName()) == null) {
				CloudantClientMgr.createDevice(device.getName(), device.getAssetId());
				registerDevices(device.getName());
			}
		}	
	}

	private void GetDeviveList() {
		try {
			deviceList=VodafoneAssetClient.assetListPerUser(vodafoneURL, username, apitoken);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void ConnecttoMATPortal() {
		// TODO Auto-generated method stub
		try {
			token = VodafoneAssetClient.userAuthenticate(vodafoneURL, username, password,customerName);
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

public static void registerDevices(String deviceName){
	
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
	createDevicetype();
	
	devicebody = DeviceBody(deviceName, devicetype, "passw0rd");
	
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


public static String DeviceBody(String deviceid, String devicetype, String authtoken){

	String body = null;
	
	body = "[ { \"typeId\": \"" +devicetype + "\", \"deviceId\": \"" + deviceid + "\",\"deviceInfo\": { \"serialNumber\": \"string\", \"manufacturer\": \"string\", \"model\": \"string\", \"deviceClass\": \"string\",  \"description\": \"string\", \"fwVersion\": \"string\",\"hwVersion\": \"string\",\"descriptiveLocation\": \"string\"}, \"location\": {\"longitude\": 0, \"latitude\": 0,\"elevation\": 0, \"accuracy\": 0, \"measuredDateTime\": \"2016-05-06T10:23:57.999Z\"  }, \"metadata\": {}, \"authToken\": \"" + authtoken + "\"} ]";
	
	return body;
}

public static String DevicetypeBody (String devicetype){
	
	String body = null;
	
	body = "{ \"id\": \"" + devicetype + "\", \"description\": \"string\", \"classId\": \"Device\", \"deviceInfo\": { \"serialNumber\": \"string\", \"manufacturer\": \"string\", \"model\": \"string\", \"deviceClass\": \"string\", \"description\": \"string\", \"fwVersion\": \"string\", \"hwVersion\": \"string\",	\"descriptiveLocation\": \"string\" },\"metadata\": {}}";

	return body;
}

	
}
