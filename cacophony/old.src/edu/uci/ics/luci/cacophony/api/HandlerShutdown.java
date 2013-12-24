package edu.uci.ics.luci.cacophony.api;

import java.net.InetAddress;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;



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
			ret.put("Saying Goodbye", "true");
			ret.put("error", "false");
		}
		else{
			ret.put("error", "true");
				
			JSONArray errorsA = new JSONArray();
			errorsA.add("Invalid form of shutdown command");
			getLog().info("Invalid form of shutdown command see: http://dev.quub.com/projects/swayr/wiki/Link_Shortener_REST_API#Shutdown-Service");
			ret.put("errors", errorsA);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		
		return pair;
	}
}
