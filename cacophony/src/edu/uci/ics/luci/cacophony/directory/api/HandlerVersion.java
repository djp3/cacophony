package edu.uci.ics.luci.cacophony.directory.api;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Pair;
import com.quub.webserver.RequestHandlerHelper;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;

public class HandlerVersion extends RequestHandlerHelper {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerVersion.class);
		}
		return log;
	}


	public HandlerVersion() {
		super();
	}
	
	protected CacophonyGlobals getGlobals(){
		return (CacophonyGlobals) super.getGlobals();
	}
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], String> handle(String restFunction,Map<String, String> headers, Map<String, String> parameters, InetAddress ip, QuubDBConnectionPool odbcp){
		Pair<byte[], String> pair = null;
		
		getGlobals();
		String version = CacophonyGlobals.getVersion();
		JSONObject ret = new JSONObject();
		try {
			ret.put("version", version);
			ret.put("error", "false");
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		
		pair = new Pair<byte[],String>(RequestHandlerHelper.contentTypeHeader_JSON,wrapCallback(parameters,ret.toString()));
		
		getLog().info("Version is "+version);
		return pair;
	}
}


