package edu.uci.ics.luci.cacophony.api;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher.HTTPRequest;



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
	
	@Override
	public HandlerShutdown copy() {
		return new HandlerShutdown();
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
