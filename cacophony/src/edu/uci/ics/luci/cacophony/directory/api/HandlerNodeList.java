package edu.uci.ics.luci.cacophony.directory.api;

import java.net.InetAddress;
import java.util.List;
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
import edu.uci.ics.luci.cacophony.Directory;
import edu.uci.ics.luci.cacophony.directory.nodelist.MetaCNode;

public class HandlerNodeList extends HandlerAbstract {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerNodeList.class);
		}
		return log;
	}


	public HandlerNodeList() {
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
	public Pair<byte[], byte[]> handle(String restFunction,Map<String, String> headers, Map<String, String> parameters, InetAddress ip, QuubDBConnectionPool odbcp){
		Pair<byte[], byte[]> pair = null;
		
		
		List<MetaCNode> nodeList = Directory.getInstance().getNodeList();
		JSONArray retServers = new JSONArray();
		for(MetaCNode c:nodeList){
			retServers.put(c.toJSONObject());
		}
		
		String version = Globals.getGlobals().getVersion();
		
		JSONObject ret = new JSONObject();
		try {
			ret.put("version", version);
			ret.put("nodes", retServers);
			ret.put("error", "false");
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.contentTypeHeader_JSON,wrapCallback(parameters,ret.toString()).getBytes());
		
		getLog().info("Version is "+version);
		return pair;
	}
}


