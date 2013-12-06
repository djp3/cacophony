package edu.uci.ics.luci.cacophony.api.directory;

import java.net.InetAddress;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.directory.nodelist.CNodeReference;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerNodeCheckin extends DirectoryRequestHandlerHelper {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerNodeCheckin.class);
		}
		return log;
	}
	
	public HandlerNodeCheckin(Directory d) {
		super(d);
	}


	@Override
	public HandlerNodeCheckin copy() {
		return new HandlerNodeCheckin(getDirectory());
	}
	
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		
		Pair<byte[], byte[]> pair = null;
		
		String reason;
		if((reason = directoryAPIOK(parameters)) != null){
			return super.failResponse(reason,restFunction,parameters);
		}
		
		String jsonDataString = parameters.get("json_data");
		
		JSONObject ret = null;
		
		if(jsonDataString != null){
			try {
				JSONObject json = (JSONObject) JSONValue.parse(jsonDataString);
				if(json != null){
					CNodeReference cnr = CNodeReference.fromJSONObject(json);
				
					getDirectory().updateMetaCNode(cnr);
				
					ret = new JSONObject();
					ret.put("error", "false");
				}
			} catch (ClassCastException e) {
				getLog().warn("Received bad checkin data:"+jsonDataString+"\n"+e);
			}
		}
		
		if(ret == null){
			ret = new JSONObject();
			ret.put("error", "true");
			JSONArray errors = new JSONArray();
			errors.add("Unable to get a valid check_in report");
			ret.put("errors",errors);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}

}


