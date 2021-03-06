package edu.uci.ics.luci.cacophony.api.directory;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.directory.nodelist.MetaCNode;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerNodeList extends DirectoryRequestHandlerHelper {
	

	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerNodeList.class);
		}
		return log;
	}

	
	public HandlerNodeList(Directory d) {
		super(d);
	}


	@Override
	public HandlerNodeList copy() {
		return new HandlerNodeList(getDirectory());
	}
	
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		Pair<byte[], byte[]> pair = null;
		
		Boolean error = false;
		JSONArray errors = new JSONArray();
		
		getLog().info("Handling "+restFunction);
		
		String reason;
		if((reason = directoryAPIOK(parameters)) != null){
			return super.failResponse(reason,restFunction,parameters);
		}
		
		List<MetaCNode> nodeList=null;
		String guids = parameters.get("guids");
		
		/* If guids is null get everything */
		if(guids == null){
			nodeList = getDirectory().getNodeList();
		}
		else{
			/* Convert to a Set */
			JSONArray g;
			try {
				g = (JSONArray) JSONValue.parse(guids);
				if(g != null){
					Set<String> gSet = new HashSet<String>();
					for(int i = 0; i < g.size(); i++){
						String s = null;
						try{
							s = (String) g.get(i);
						} catch (ClassCastException e) {
							error = true;
							errors.add("Unable to create a list of guids from :"+guids);
							break;
						}
						gSet.add(s);
					}
					nodeList = getDirectory().getNodeList(gSet);
				}
			} catch (ClassCastException e) {
				error = true;
				errors.add("Unable to create a list of guids from :"+guids);
			}
		}
		
		JSONArray retServers = new JSONArray();
		
		if(error == false){
			if(nodeList != null){
				for(MetaCNode c:nodeList){
					if(c != null){
						retServers.add(c.toJSONObject());
					}
					else{
						/* if c is null then something is malformed in the backing store.  Probably old serialization */
						getLog().warn("What the heck");
					}
				}
			}
		}
		
		JSONObject ret = new JSONObject();
		
		ret.put("nodes", retServers);
		ret.put("error", error.toString());
		if(error){
			ret.put("errors", errors);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,ret.toString()).getBytes());
		return pair;
	}

}


