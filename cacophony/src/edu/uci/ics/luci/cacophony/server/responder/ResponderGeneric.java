package edu.uci.ics.luci.cacophony.server.responder;

import java.util.Map;

import net.minidev.json.JSONObject;
import edu.uci.ics.luci.cacophony.node.CNode;

/** This Responder does nothing by design.  It's used for testing and error conditions */
public class ResponderGeneric extends CNodeServerResponder {
	
	public ResponderGeneric(){
	}
	
	public ResponderGeneric(String error,String response){
		if(error != null){
			appendError(error);
		}
		else{
			if(response != null){
				appendResponse(response);
			}
		}
	}

	@Override
	public void handle(JSONObject jo, Map<String, CNode> cNodes) {
	}
	
}
