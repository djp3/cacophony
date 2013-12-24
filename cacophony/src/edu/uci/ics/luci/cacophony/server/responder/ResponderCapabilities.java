package edu.uci.ics.luci.cacophony.server.responder;

import java.util.Map;
import java.util.Map.Entry;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import edu.uci.ics.luci.cacophony.node.CNode;


/**
 * @author djp3
 *
 */
public class ResponderCapabilities extends CNodeServerResponder {
	
	private Map<String, CNodeServerResponder> handlers;
	private int maxCNodes;

	public ResponderCapabilities(Map<String, CNodeServerResponder> handlers,int maxCNodes){
		this.handlers = handlers;
		this.maxCNodes = maxCNodes;
	}

	@Override
	public void handle(JSONObject jo, Map<String, CNode> cnodes) {
		if(handlers == null){
			appendError("CNodeServer was incorrectly initialized.  This responder unable to determine capabilties from null");
		}
		else{
			JSONArray response = new JSONArray();
			JSONObject info = new JSONObject();
			info.put("server_capacity",Integer.toString(maxCNodes));
			
			JSONObject h = new JSONObject();
			for(Entry<String, CNodeServerResponder> s: handlers.entrySet()){
				h.put(s.getKey(), s.getValue().getClass().getCanonicalName());
			}
			info.put("server_capabilities", h);
			
			JSONArray c = new JSONArray();
			for(Entry<String, CNode> s: cnodes.entrySet()){
				c.add(s.getKey());
			}
			info.put("c_nodes", c);
			
			response.add(info);
			replaceResponses(response);
		}
	}

}
