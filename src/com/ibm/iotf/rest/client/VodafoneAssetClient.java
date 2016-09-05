package com.ibm.iotf.rest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import com.ibm.iotf.model.Asset;


public class VodafoneAssetClient {
	
	
public static String userAuthenticate (String URL, String username, String password, String company) throws ParserConfigurationException{

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
		
	try {
		
		HttpClient client = HttpClientBuilder.create().build();
		
		HttpResponse response = client.execute(httpPost);

		int httpCode = response.getStatusLine().getStatusCode();
		if (httpCode ==200){
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(response.getEntity().getContent());
			doc.getDocumentElement().normalize();

			return doc.getElementsByTagName("Token").item(0).getTextContent();
			
		}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return null;
	
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
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(response.getEntity().getContent());
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("e");
			//System.out.println("----------------------------");
			List <Asset> aLPU = null;
			
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				//System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					Asset a = null;
					a.setAssetId(eElement.getElementsByTagName("AssetId").item(0).getTextContent());
					a.setName(eElement.getElementsByTagName("Name").item(0).getTextContent());
					aLPU.add(a);
				}
			}
			return aLPU;
			
		}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
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

public static String XMLPArser (InputStream is, String searchText){
try {

	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(is);

	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();

	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

	return doc.getElementsByTagName(searchText).item(0).getTextContent();

/*	System.out.println("----------------------------");

	for (int temp = 0; temp < nList.getLength(); temp++) {

		Node nNode = nList.item(temp);

		System.out.println("\nCurrent Element :" + nNode.getNodeName());

		if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) nNode;

			if (eElement.getTagName().equals(searchText))
			{
				return eElement.;
			}
			System.out.println("Staff id : " + eElement.getAttribute("id"));
			System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
			System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
			System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
			System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());

		}
	}*/
    } catch (Exception e) {
	e.printStackTrace();
    }
return searchText;

}
}
