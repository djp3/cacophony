package edu.uci.ics.luci.cacophony.directory.api;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.Directory;
import edu.uci.ics.luci.cacophony.directory.nodelist.MetaCNode;

public class HandlerNodeAssignment extends HandlerAbstract {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerNodeAssignment.class);
		}
		return log;
	}

	protected CacophonyGlobals getGlobals(){
		return (CacophonyGlobals) Globals.getGlobals();
	}
	
	@Override
	public Pair<byte[], byte[]> handle(String restFunction,Map<String, String> headers, Map<String, String> parameters, InetAddress ip, QuubDBConnectionPool odbcp){
		
		Pair<byte[], byte[]> pair = null;
		
		List<MetaCNode> nodeList = Directory.getInstance().getNodeList();
		Collections.sort(nodeList,MetaCNode.ByAssignmentPaucity);
		
		/* Make sure we have all the necessary fields */
		MetaCNode retC = null;
		while((retC == null) && (nodeList.size() > 0)){
			MetaCNode x = nodeList.remove(0);
			if( (x.getId() != null) &&
				(x.getConfiguration() != null)){
				retC = x;
			}
		}
		
		JSONObject ret = new JSONObject();
		
		if(retC != null){
			String version = Globals.getGlobals().getVersion();
			try {
				ret.put("version", version);
				ret.put("node_id", retC.getId());
				ret.put("node_configuration", retC.getConfiguration());
				ret.put("error", "false");
			} catch (JSONException e) {
				getLog().error("Unable to respond with version:"+e);
			}
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.contentTypeHeader_JSON,wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}
}


