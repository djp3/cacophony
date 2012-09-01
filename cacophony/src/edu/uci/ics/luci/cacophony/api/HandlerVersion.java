package edu.uci.ics.luci.cacophony.api;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher.HTTPRequest;

import edu.uci.ics.luci.cacophony.CacophonyRequestHandlerHelper;

public class HandlerVersion extends CacophonyRequestHandlerHelper{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerVersion.class);
		}
		return log;
	}
	
	@Override
	public HandlerAbstract copy() {
		return new HandlerVersion();
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
			ret.put("version", getAPIVersion());
			ret.put("error", "false");
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		
		return pair;
	}

	
}


