package com.ibm.iotf.rest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.commons.net.util.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class VodafoneAssetClient {
public static String userAuthenticate (String URL, String username, String password, String company){
	
	
	HttpPost httpPost = new HttpPost(URL+"/UserAuthenticate");
	StringEntity requestEntity = new StringEntity(
			authenticationBody(username,password,company),
		    ContentType.APPLICATION_JSON);
		/*
		 * Execute the HTTP Request
		 */

	httpPost.setEntity(requestEntity);
	httpPost.addHeader("Content-Type", "application/json");
	//httpPost.addHeader("Accept", "application/json");
	
	
		//byte[] encoding = Base64.encodeBase64(new String("a-uy6cof-7cn0vgvrwk" + ":" + "fZfAcg46PjOTS4Kvn8").getBytes() );			
		//String encodedString = new String(encoding);
		//httpPost.addHeader("Authorization", "Basic " + encodedString);
	
	
	try {
		
		//SSLContext sslContext = null;
		//try {
		//	sslContext = SSLContext.getInstance("TLSv1.2");
		//} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		//try {
		//	sslContext.init(null, null, null);
		//} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}

		HttpClient client = HttpClientBuilder.create().build();
		
		HttpResponse response = client.execute(httpPost);

		int httpCode = response.getStatusLine().getStatusCode();


		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		System.out.println(result.toString());
		System.out.println(httpCode);
		return "abc";
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
	return null;
	
}

public static String assetListPerUser (String URL, String username, String token){
	
	
	HttpPost httpPost = new HttpPost(URL+"/AssetlistPerUser");
	StringEntity requestEntity = new StringEntity(
			assetListBody(username,token),
		    ContentType.APPLICATION_JSON);
		/*
		 * Execute the HTTP Request
		 */

	httpPost.setEntity(requestEntity);
	httpPost.addHeader("Content-Type", "application/json");
	//httpPost.addHeader("Accept", "application/json");
	
	
		//byte[] encoding = Base64.encodeBase64(new String("a-uy6cof-7cn0vgvrwk" + ":" + "fZfAcg46PjOTS4Kvn8").getBytes() );			
		//String encodedString = new String(encoding);
		//httpPost.addHeader("Authorization", "Basic " + encodedString);
	
	
	try {
		
		//SSLContext sslContext = null;
		//try {
		//	sslContext = SSLContext.getInstance("TLSv1.2");
		//} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		//try {
		//	sslContext.init(null, null, null);
		//} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}

		HttpClient client = HttpClientBuilder.create().build();
		
		HttpResponse response = client.execute(httpPost);

		int httpCode = response.getStatusLine().getStatusCode();


		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		System.out.println(result.toString());
		System.out.println(httpCode);
		return "abc";
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
	return null;
	
}

public static String authenticationBody (String username, String password, String company){
	
	String body = null;
	
	body = "{ \"UserName\": \"" + username + "\", \"Password\": \"" + password + "\", \"Company\": \"" + company + "\" }";
	
	return body;
}

public static String assetListBody (String username, String token){
	
	String body = null;
	
	body = "{\"loginuser\": { \"UserName\": \"" + username + "\", \"Token\": \"" + token + "\"}}";
	
	return body;
}

}
