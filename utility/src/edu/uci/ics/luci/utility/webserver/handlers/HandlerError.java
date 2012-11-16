package edu.uci.ics.luci.utility.webserver.handlers;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerError extends HandlerAbstract {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerError.class);
		}
		return log;
	}

	private String version;
	
	public HandlerError(String version) {
		super();
		this.version = version;
	}

	@Override
	public HandlerError copy() {
		return new HandlerError(this.version);
	}


	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		Pair<byte[], byte[]> pair = null;
		
		JSONObject ret = new JSONObject();
		try {
			ret.put("version", version);
			ret.put("error", "true");
			JSONArray errors = new JSONArray();
			errors.put("Unable to respond to query:"+restFunction);
			ret.put("errors",errors);
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		
		getLog().info("Version is "+version);
		return pair;
	}
}


