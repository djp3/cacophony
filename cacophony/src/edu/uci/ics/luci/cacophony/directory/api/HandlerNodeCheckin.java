package edu.uci.ics.luci.cacophony.directory.api;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.directory.Directory;

public class HandlerNodeCheckin extends CacophonyRequestHandlerHelper {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerNodeCheckin.class);
		}
		return log;
	}

	protected CacophonyGlobals getGlobals(){
		return (CacophonyGlobals) Globals.getGlobals();
	}
	
	@Override
	public Pair<byte[], byte[]> handle(String restFunction,Map<String, String> headers, Map<String, String> parameters, InetAddress ip, QuubDBConnectionPool odbcp){
		
		Pair<byte[], byte[]> pair = null;
		
		if(!versionOK(parameters)){
			return super.versionFailResponse(restFunction,parameters);
		}
		
		String jsonDataString = parameters.get("json_data");
		
		JSONObject ret = null;
		
		if(jsonDataString != null){
			try {
				JSONObject json = new JSONObject(jsonDataString);
				String guid = json.getString("guid");
				String nodeId = json.getString("node_id");
				String namespace = json.getString("namespace");
				JSONArray accessRoutes = json.getJSONArray("access_routes");
				
				Directory.getInstance().updateMetaCNode(nodeId, guid, System.currentTimeMillis());
				
				ret = new JSONObject();
				String version = Globals.getGlobals().getVersion();
				try {
					ret.put("version", version);
					ret.put("error", "false");
				} catch (JSONException e) {
					getLog().error("Unable to respond with version:"+e);
				}
			} catch (JSONException e) {
				getLog().warn("Received bad checkin data:"+jsonDataString+"\n"+e);
			}
		}
		
		if(ret == null){
			ret = new JSONObject();
			String version = Globals.getGlobals().getVersion();
			try {
				ret.put("version", version);
				ret.put("error", "true");
				JSONArray errors = new JSONArray();
				errors.put("Unable to get a valid check_in report");
				ret.put("errors",errors);
			} catch (JSONException e) {
				getLog().error("Unable to respond with version:"+e);
			}
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.contentTypeHeader_JSON,wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}
}


