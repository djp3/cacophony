package edu.uci.ics.luci.cacophony.directory.api;


import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;


public class HandlerShutdown extends CacophonyRequestHandlerHelper {
	
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
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[],String> handle(String restFunction, Map<String,String> headers,Map<String,String> parameters, InetAddress ip, QuubDBConnectionPool odbcp){

		Pair<byte[], String> pair = null;
		boolean shutdown = false;
		
		getLog().info("Handling shutdown");
		
		if(!versionOK(parameters)){
			return super.versionFailResponse(restFunction,parameters);
		}
		
		String seriously = parameters.get("seriously");
		if((seriously != null) && (seriously.equals("true") )){
			shutdown = true;
		}
		
		JSONObject ret = new JSONObject();
		if(shutdown){
			getGlobals().setQuitting(true);
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
		
		pair = new Pair<byte[],String>(HandlerAbstract.contentTypeHeader_JSON,wrapCallback(parameters,ret.toString()));
		
		return pair;
	}

}
