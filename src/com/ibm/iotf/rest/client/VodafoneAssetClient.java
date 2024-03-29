package com.ibm.iotf.rest.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.ibm.iotf.model.Asset;
import com.ibm.iotf.model.AssetData;
import com.ibm.iotf.model.AssetDataPerDate;
import com.ibm.iotf.model.AssetlistPerUser;
import com.ibm.iotf.model.AuthenticationResponse;
import com.ibm.json.java.JSONObject;


public class VodafoneAssetClient {
	
	
public static String userAuthenticate (String URL, String username, String password, String company) throws ParserConfigurationException{

	HttpPost httpPost = new HttpPost(URL);
	StringEntity requestEntity = new StringEntity(
			authenticationBody(username,password,company),
		    ContentType.APPLICATION_JSON);
		/*
		 * Execute the HTTP Request
		 */

	httpPost.setEntity(requestEntity);
	httpPost.addHeader("Content-Type", "application/json");
	//httpPost.addHeader("Accept", "application/json");
		
	try {
		
		HttpClient client = HttpClientBuilder.create().build();
		
		HttpResponse response = client.execute(httpPost);

		int httpCode = response.getStatusLine().getStatusCode();
		if (httpCode ==200){
			Gson gson = new Gson();
			AuthenticationResponse auresp = gson.fromJson(stream2String(response.getEntity().getContent()), AuthenticationResponse.class);
			return auresp.getToken();
			
/*			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(response.getEntity().getContent());
			doc.getDocumentElement().normalize();

			return doc.getElementsByTagName("Token").item(0).getTextContent();*/
			
		}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return null;
	
}

	public static String stream2String (InputStream is) {
		
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result;
		try {
			result = bis.read();
			while(result != -1) {
			    buf.write((byte) result);
			    result = bis.read();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				if(bis!=null) bis.close();
				if (buf!=null) buf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}}
		return buf.toString();
	}
	public static List<Asset> assetListPerUser (String URL, String username, String token) throws ParserConfigurationException{

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
		
		try {
		
			HttpClient client = HttpClientBuilder.create().build();
		
			HttpResponse response = client.execute(httpPost);

			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode ==200){
				List <Asset> aLPU = new ArrayList <Asset>();
				
				Gson gson = new Gson();
				AssetlistPerUser alpu = gson.fromJson(stream2String(response.getEntity().getContent()), AssetlistPerUser.class);
				ArrayList <Asset> al = alpu.getData();
				
				for (Asset as : alpu.getData())
				{
					aLPU.add(as);
				}
				
				return aLPU;
				
/*				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(response.getEntity().getContent());
				doc.getDocumentElement().normalize();
				
				System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
				
				NodeList nList = doc.getElementsByTagName("e");
				//System.out.println("----------------------------");
				
			
				for (int temp = 0; temp < nList.getLength(); temp++) {

					Node nNode = nList.item(temp);

					//System.out.println("\nCurrent Element :" + nNode.getNodeName());

					if (nNode.getNodeType() == Node.ELEMENT_NODE) {

						Element eElement = (Element) nNode;
						
						Asset a = new Asset();
						a.setAssetId(eElement.getElementsByTagName("AssetId").item(0).getTextContent());
						a.setName(eElement.getElementsByTagName("Name").item(0).getTextContent());
						aLPU.add(a);
					}
				}
				return aLPU;*/
			
			}
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	
	}

	public static List<AssetData> assetDataPerDate (String URL, String assetName, String dateFrom, String dateTo, String username, String token) throws ParserConfigurationException{

		HttpPost httpPost = new HttpPost(URL+"/AssetDataPerDate");
		StringEntity requestEntity = new StringEntity(assetDataPerDateBody(assetName, dateFrom, dateTo, username,token),ContentType.APPLICATION_JSON);
		/*
		 * Execute the HTTP Request
		 */

		httpPost.setEntity(requestEntity);
		httpPost.addHeader("Content-Type", "application/json");
		//httpPost.addHeader("Accept", "application/json");
		
		try {
		
			HttpClient client = HttpClientBuilder.create().build();
		
			HttpResponse response = client.execute(httpPost);

			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode ==200){
				List <AssetData> aLPU = new ArrayList <AssetData>();
				
				Gson gson = new Gson();
				AssetDataPerDate alpu = gson.fromJson(stream2String(response.getEntity().getContent()), AssetDataPerDate.class);
				ArrayList <AssetData> al = alpu.getData();
				
				for (AssetData as : alpu.getData())
				{
					aLPU.add(as);
				}
				
				return aLPU;
					
			}
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
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
	
		body = "{\"loginUser\": { \"UserName\": \"" + username + "\", \"Token\": \"" + token + "\"}}";
	
		return body;
	}
	
	public static String assetDataPerDateBody (String assetName, String dateFrom, String dateTo, String username, String token){
		
		String body = null;
	
		body = "{\"AssetName\": \"" + assetName + "\",\"dateFrom\": \"" + dateFrom + "\", \"dateTo\": \"" + dateTo + "\", \"loginUser\": {\"UserName\": \"" + username + "\", \"Token\": \"" + token + "\"}}";
		
		return body;
	}

}
