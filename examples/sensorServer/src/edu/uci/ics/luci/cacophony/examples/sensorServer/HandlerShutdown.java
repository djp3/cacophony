package edu.uci.ics.luci.cacophony.examples.sensorServer;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;



public class HandlerShutdown extends HandlerAbstract{
	
	private static transient volatile Logger log = null;
	
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerShutdown.class);
		}
		return log;
	}

	public HandlerShutdown() {
		super();
	}
	
	@Override
	public HandlerShutdown copy() {
		return new HandlerShutdown();
	}
	
	/**
	 * @return the version of the API are we implementing
	 */
	public static String getAPIVersion() {
		return Server.API_VERSION;
	}
	
	protected String versionOK(Map<String, String> parameters) {
		String trueVersion = getAPIVersion();
		String version = parameters.get("version");
		String noerror = null;
		String error = "API is version: "+trueVersion+". REST call requested: "+version;
		if(version != null){
			if(!trueVersion.equals(version)){
				return error;
			}
			else{
				return noerror;
			}
		}
		else{
			return error;
		}
	}
	

	public Pair<byte[], byte[]> failResponse(String reason,String restFunction,Map<String, String> parameters){
		Pair<byte[], byte[]> pair = null;

		JSONObject ret = new JSONObject();

		try {
			ret.put("error","true");
			JSONArray errors = new JSONArray();
			errors.put("REST call to "+restFunction+" failed");  
			errors.put(reason);
			ret.put("errors",errors);
			
			pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),ret.toString().getBytes());
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		return pair;
	}
	

	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {

		Pair<byte[], byte[]> pair = null;
		boolean shutdown = false;
		
		getLog().info("Handling shutdown");
		
		String reason;
		if((reason = versionOK(parameters)) != null){
			return failResponse(reason,restFunction,parameters);
		}
		
		String seriously = parameters.get("seriously");
		if((seriously != null) && (seriously.equals("true") )){
			shutdown = true;
		}
		
		JSONObject ret = new JSONObject();
		if(shutdown){
			Globals.getGlobals().setQuitting(true);
			try {
				ret.put("Saying Goodbye", "true");
				ret.put("error", "false");
			} catch (JSONException e) {
				getLog().error("Unable to respond with version:"+e);
			}
		}
		else{
			try {
				ret.put("error", "true");
				
				JSONArray errorsA = new JSONArray();
				errorsA.put("Invalid form of shutdown command");
				getLog().info("Invalid form of shutdown command see: http://dev.quub.com/projects/swayr/wiki/Link_Shortener_REST_API#Shutdown-Service");
				ret.put("errors", errorsA);
			} catch (JSONException e) {
				getLog().error("Unable to respond with version:"+e);
			}
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		
		return pair;
	}
}
