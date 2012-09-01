package edu.uci.ics.luci.cacophony.api.directory;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher.HTTPRequest;

import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.directory.nodelist.MetaCNode;

public class HandlerNodeAssignment extends DirectoryRequestHandlerHelper {


	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerNodeAssignment.class);
		}
		return log;
	}
	
	public HandlerNodeAssignment(Directory d) {
		super(d);
	}

	@Override
	public HandlerNodeAssignment copy() {
		return new HandlerNodeAssignment(getDirectory());
	}

	
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		
		Pair<byte[], byte[]> pair = null;
		
		String reason;
		if((reason = directoryAPIOK(parameters)) != null){
			return super.failResponse(reason,restFunction,parameters);
		}
		
		List<MetaCNode> nodeList = getDirectory().getNodeList();
		Collections.sort(nodeList,MetaCNode.ByAssignmentPaucity);
		
		/* Make sure we have all the necessary fields */
		MetaCNode retC = null;
		while((retC == null) && (nodeList.size() > 0)){
			MetaCNode x = nodeList.remove(0);
			if( (x.getGuid() != null) &&
				(x.getConfiguration() != null)){
				retC = x;
			}
		}
		
		JSONObject ret = new JSONObject();
		
		if(retC != null){
			try {
				ret.put("node_id", retC.getGuid());
				ret.put("node_configuration", retC.getConfiguration());
				ret.put("error", "false");
			} catch (JSONException e) {
				getLog().error("Unable to respond with version:"+e);
			}
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}

}


