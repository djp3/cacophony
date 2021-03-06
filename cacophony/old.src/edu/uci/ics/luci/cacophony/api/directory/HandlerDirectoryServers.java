package edu.uci.ics.luci.cacophony.api.directory;

import java.net.InetAddress;
import java.util.Map;

import net.minidev.json.JSONObject;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerDirectoryServers extends DirectoryRequestHandlerHelper {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerDirectoryServers.class);
		}
		return log;
	}


	public HandlerDirectoryServers(Directory d) {
		super(d);
	}
	
	@Override
	public HandlerDirectoryServers copy() {
		return new HandlerDirectoryServers(getDirectory());
	}
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		Pair<byte[], byte[]> pair = null;
		
		getLog().info("Handling "+restFunction);
		
		String reason;
		if((reason = directoryAPIOK(parameters)) != null){
			return super.failResponse(reason,restFunction,parameters);
		}
		
		Map<String, JSONObject> servers = getDirectory().getServers();
		JSONObject retServers = new JSONObject(servers);
		
		JSONObject ret = new JSONObject();
		ret.put("servers", retServers);
		ret.put("error", "false");
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}


}


