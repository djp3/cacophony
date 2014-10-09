package edu.uci.ics.luci.cacophony.server.responder;

import java.util.Map;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import edu.uci.ics.luci.cacophony.node.CNode;


public class ResponderConfiguration extends CNodeServerResponder {
	

	@Override
	public void handle(JSONObject jo, Map<String, CNode> cnodes) {
		if(jo == null){
			appendError("No \"data\" sent in the incoming JSON into a String");
			return;
		}
		
		String cNode = null;
		try{
			cNode = (String) jo.get("c_node");
		} catch (ClassCastException e1) {
			appendError("Unable to make the \"c_node\" in the incoming JSON into a String\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		catch(RuntimeException e1){
			appendError("Unable to find the \"c_node\" parameter in the incoming JSON\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		
		if(cnodes.containsKey(cNode)){
			CNode x = cnodes.get(cNode);
			if(x == null){
				appendError("The data object for \""+cNode+"\" is explicitly null. Something's wrong.");
				return;
			}
			else{
				appendResponse(x.getConfiguration().toJSONObject());
			}
		}
		else{
			appendError("No information on this server about this cNode: "+cNode);
		}
	}


}
