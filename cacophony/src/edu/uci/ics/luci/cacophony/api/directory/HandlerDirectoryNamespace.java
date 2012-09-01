package edu.uci.ics.luci.cacophony.api.directory;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher.HTTPRequest;

import edu.uci.ics.luci.cacophony.directory.Directory;

public class HandlerDirectoryNamespace extends DirectoryRequestHandlerHelper {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerDirectoryNamespace.class);
		}
		return log;
	}
	
	@Override
	public HandlerDirectoryNamespace copy() {
		return new HandlerDirectoryNamespace(getDirectory());
	}

	public HandlerDirectoryNamespace(Directory d) {
		super(d);
	}
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		Pair<byte[], byte[]> pair = null;
		
		String reason;
		if((reason = versionOK(parameters)) != null){
			return failResponse(reason,restFunction,parameters);
		}
		
		String namespace = getDirectory().getDirectoryNamespace();
		
		JSONObject ret = new JSONObject();
		try {
			ret.put("namespace", namespace);
			ret.put("error", "false");
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}
}



