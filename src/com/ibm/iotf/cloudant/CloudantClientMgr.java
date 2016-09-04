package com.ibm.iotf.cloudant;

import java.util.Set;
import java.util.List;
import java.util.Map.Entry;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CloudantClientMgr {

	private static CloudantClient cloudant = null;
	private static Database db = null;
	private static String databaseName = "vodafonemat";
	private static String user = null;
	private static String password = null;
	private static String host = null;

	
	 
	private static void initClient() {
		
		if ( cloudant == null ) {
			synchronized (CloudantClientMgr.class) {
				if ( cloudant != null ) {
					return;
				}				
				cloudant = createClient();			
				
			}// end synchronized
		}
	}
    
	private static CloudantClient createClient() {

		// VCAP_SERVICES is a system environment variable
		// Parse it to obtain the  NoSQL DB connection info
		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
		String serviceName = null;

    	if (VCAP_SERVICES != null) {

			// parse the VCAP JSON structure
			JsonObject obj =  (JsonObject) new JsonParser().parse(VCAP_SERVICES);
			Entry<String, JsonElement> dbEntry = null;
			Set<Entry<String, JsonElement>> entries = obj.entrySet();
			// Look for the VCAP key that holds the cloudant no sql db information
			for (Entry<String, JsonElement> eachEntry : entries) {				
				if (eachEntry.getKey().equals("cloudantNoSQLDB")) {
					dbEntry = eachEntry;
					break;
				}
			}
			if (dbEntry == null) {			
				throw new RuntimeException("Could not find cloudantNoSQLDB key in VCAP_SERVICES env variable");    					
			}

			obj =(JsonObject) ((JsonArray)dbEntry.getValue()).get(0);		
			serviceName = (String)dbEntry.getKey();
			System.out.println("Service Name - "+serviceName);

			obj = (JsonObject) obj.get("credentials");

			user = obj.get("username").getAsString();
			password = obj.get("password").getAsString();
			host = obj.get("host").getAsString();
			
			CloudantClient client = ClientBuilder.account(user)
                    .username(user)
                    .password(password)
                    .disableSSLAuthentication()
                    .build();
			System.out.println("Server Version: " + client.serverVersion());

			// Get a List of all the databases this Cloudant account
			List<String> databases = client.getAllDbs();
			System.out.println("All my databases : ");
			for ( String db : databases ) {
			    System.out.println(db);
			}
			
			return client;

		}
		else {
			throw new RuntimeException("VCAP_SERVICES not found");
		}
	}

	
	public static Database getDB() {
		
		if ( cloudant == null ) {
			initClient();
		}
		
		if(db == null)
		{
			try {
				
				db = cloudant.database(databaseName, true);
			}
			catch(Exception e) {

				throw new RuntimeException("DB Not found", e);
			}
		}
		return db;
	}
	
	public static String getUser()
	{
		return user;
	}
	
	public static String getPassword()
	{
		return password;
	}
	
	public static String getHost()
	{
		return host;
	}
	
	public static String getDatabaseName()
	{
		return databaseName;
	}
	
	private CloudantClientMgr() {
	
	}   
	
    public static Config getConfig(String configName){
		
    	Database db = null;
		try
		{
			
			db = getDB();
			
			List<Config> config = db.findByIndex("\"selector\": { \"config\": \"" + configName + "\" }", Config.class);
	        for (Config conf : config) {
	        	return conf;
	        }
		}
		catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
		
		return null;
    	
    }
	public static String readConfigfromCloudant(String configName) {
		
		return getConfig(configName).getValue();
	}
    
    public static void updateConfig(String configName, String newValue)
    {

    	Config cf = getConfig(configName);
    	cf.setValue(newValue);
    	
    	Database db = null;
		try
		{
			
			db = getDB();
			db.update(cf);
			
		}
		catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    	
    }
    
	private class Config {
		  private String _id; 
		  private String _rev;
		  private String config;
		  private String value;
		  
		  private String getConfig() {
			return config;
		}
		  private void setConfig(String config) {
			this.config = config;
		}
		  private String getValue() {
			return value;
		}
		  private void setValue(String value) {
			this.value = value;
		}
		  
		}

	
}
