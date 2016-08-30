package deviceSimulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

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
import com.ibm.iotf.sample.client.device.RegisteredDeviceEventPublish;

public class IotDevices {

	final static int NoOfDevices = 2;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		String org="2m3wff";
		String type="VodafoneMATAsset";
		String deviceid = null;
	//	String id[]={"Device01","Device02", "Device03","Device04","Device05"};
		String token="token";
		String authtoken = null;
	//	String password[]={"qwerty123","QES5NJ2TO8hpFmpaOA","CcXFDmf!qYEOk1Aaqi","i5J1JsjPhu13AzKh2&","UM+OKeBi-wGHtgS2Zy"};
	
		//Function called to create device type
		Devicetype(type, org);
		
		//Function called to register devices in Watson IoT Platform Service
		RegisterDevices(type, org);
		
		for(int i=1; i<=NoOfDevices; i++)
		{
			if(i<10){	
				
				deviceid = "Device0" + i;
				authtoken = "1Q2w3e4r";
			}
			else{
				deviceid = "Device" + i;
				authtoken = "qwerty" + i;
			}
		//	RegisteredDeviceEventPublish.simulateDevice(org, type, deviceID, token, password);
	     new Thread(new RegisteredDeviceEventPublish(org, type, deviceid, token, authtoken,(new Double(Math.random()*10)).intValue()+1)).start();
		}
		
	}
	
	public static void Devicetype(String devicetype, String org){
		
		String devicetypebody = null;
		
		String url = null;
		
		devicetypebody = DevicetypeBody(devicetype);
		
		url = "https://" + org  + ".internetofthings.ibmcloud.com/api/v0002/device/types"; 
		
		HttpPost httpPost = new HttpPost(url);
		StringEntity requestEntity = new StringEntity(
			    devicetypebody,
			    ContentType.APPLICATION_JSON);
			/*
			 * Execute the HTTP Request
			 */

		httpPost.setEntity(requestEntity);
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("Accept", "application/json");
		
		
			byte[] encoding = Base64.encodeBase64(new String("a-2m3wff-o25r68u1jo" + ":" + "jBMTgHWJZoNB5Yt7hL").getBytes() );			
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
	
	public static void RegisterDevices(String devicetype, String org){
		
	//	String devicebody = "[ { \"typeId\": \"WaterPump\", \"deviceId\": \"Device01\",\"deviceInfo\": { \"serialNumber\": \"string\", \"manufacturer\": \"string\", \"model\": \"string\", \"deviceClass\": \"string\",  \"description\": \"string\", \"fwVersion\": \"string\",\"hwVersion\": \"string\",\"descriptiveLocation\": \"string\"}, \"location\": {\"longitude\": 0, \"latitude\": 0,\"elevation\": 0, \"accuracy\": 0, \"measuredDateTime\": \"2016-05-06T10:23:57.999Z\"  }, \"metadata\": {}, \"authToken\": \"qwerty01\"} ]";
		String devicebody = null;
		String deviceid = null;
		String authtoken = null;
		String url = null;
		
		for (int i=1; i<=NoOfDevices; i++){
		
		if(i<10){	
		
			deviceid = "Device0" + i;
			authtoken = "1Q2w3e4r";
		}
		else{
			deviceid = "Device" + i;
			authtoken = "qwerty" + i;
		}
		
		url = "https://" + org + ".internetofthings.ibmcloud.com/api/v0002/bulk/devices/add";
		
		devicebody = DeviceBody(deviceid, devicetype, authtoken);
		
		HttpPost httpPost = new HttpPost(url);
		StringEntity requestEntity = new StringEntity(
			    devicebody,
			    ContentType.APPLICATION_JSON);
			/*
			 * Execute the HTTP Request
			 */

		httpPost.setEntity(requestEntity);
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("Accept", "application/json");
		
		
			byte[] encoding = Base64.encodeBase64(new String("a-2m3wff-o25r68u1jo" + ":" + "jBMTgHWJZoNB5Yt7hL").getBytes() );			
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
