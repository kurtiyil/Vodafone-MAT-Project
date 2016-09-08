package deviceSimulation;

import java.io.IOException;
import java.io.InputStream;
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
import org.apache.http.client.methods.HttpGet;
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
import com.ibm.iotf.model.Device;
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
	static String deviceGetURL = null;
	static String defaultLastRunDate = null;
	static List<Asset> deviceList = new ArrayList <Asset>();
	
	private void initialize()
	{
		Config config = CloudantClientMgr.readConfigfromCloudant("**VodafoneMATBridgeConfig**");
		
		
		devicetype="VodafoneMATAsset";
		devicepassword = "passw0rd";
		devicetoken = "token";
		
		org = config.getWiotp().getCredentials().getOrgId();
		apikey = config.getWiotp().getCredentials().getApiKey();
		apitoken = config.getWiotp().getCredentials().getApiToken();
		customerName = config.getMat().getCredentials().getCustomer();
		username = config.getMat().getCredentials().getUserName();
		password = config.getMat().getCredentials().getPassword();
		defaultLastRunDate = config.getStartDate();
		
		
		
		/*		org = CloudantClientMgr.readConfigfromCloudant("org");
		customerName = CloudantClientMgr.readConfigfromCloudant("customer");
		username = CloudantClientMgr.readConfigfromCloudant("username");
		password = CloudantClientMgr.readConfigfromCloudant("password");
		apikey = CloudantClientMgr.readConfigfromCloudant("APIKey");
		apitoken = CloudantClientMgr.readConfigfromCloudant("APIToken");*/
		
		deviceTypeURL = "https://" + org  + ".internetofthings.ibmcloud.com/api/v0002/device/types";
		deviceAddURL = "https://" + org + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/add";
		deviceGetURL = "https://" + org + ".internetofthings.ibmcloud.com/api/v0002/device/types/";
		
		
		//Real Vodafone MAT System
		vodafoneUserURL = "https://matapi.vodafone.com/UserService.svc/Rest/UserAuthenticate";
		vodafoneAssetURL = "https://matapi.vodafone.com/AssetService.svc/rest";
		
		//Mock Service
		//vodafoneUserURL = "http://localhost:8080/UserService.svc/Rest/UserAuthenticate";
		//vodafoneAssetURL = "http://localhost:8080/UserService.svc/Rest";
		
	}
	
	public void doJob() throws InterruptedException, ParseException {
	
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
	
}

	private Boolean ReadDeviceStatus(String dNow) {
		createDevicetype();
		for(Asset device : deviceList) {
			String lastrundate=readLastRunDate(device);
			
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
		
		//https://uy6cof.internetofthings.ibmcloud.com/api/v0002/device/types/VodafoneMATAsset/devices/Kurtulus
		HttpGet httpGet = new HttpGet(deviceGetURL + devicetype + "/devices/" + device.getAssetId());

		httpGet.addHeader("Content-Type", "application/json");
		httpGet.addHeader("Accept", "application/json");
		
		byte[] encoding = Base64.encodeBase64(new String(apikey + ":" + apitoken).getBytes() );			
		String encodedString = new String(encoding);
		httpGet.addHeader("Authorization", "Basic " + encodedString);
		
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
			
			HttpResponse response = client.execute(httpGet);
			
			int httpCode = response.getStatusLine().getStatusCode();
			System.out.println(httpCode);
			if (httpCode == 404) 
			{	
				registerDevices(device.getName(), device.getAssetId());
				return defaultLastRunDate;
			}
			else if (httpCode == 200)
			{
				Gson gson = new Gson();
				Device wiotfdevice = gson.fromJson(VodafoneAssetClient.stream2String(response.getEntity().getContent()), Device.class);

				return wiotfdevice.getMetadata().getLastDataReadDate();
			}	//
				
				
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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
	
	String devicebody = null;
	
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
