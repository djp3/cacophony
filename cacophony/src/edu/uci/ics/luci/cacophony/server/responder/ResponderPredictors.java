package edu.uci.ics.luci.cacophony.server.responder;

import java.util.HashMap;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;


/**
{   "c_node":"temperature:cnn",
	"request":"predictors",
	"from": "p2p://provider"
}
{  
   "errors" : [ "This is the first error"],
   "response" : [ "p2p://provider/temp/cnn" ]
}
*/
public class ResponderPredictors extends CNodeServerResponder {

	@Override
	public void handle(JSONObject jo,HashMap<String,JSONObject> configurations) {
		
		if(jo == null){
			addError("No \"data\" sent in the incoming JSON into a String");
			return;
		}
		
		String cNode = null;
		try{
			cNode = (String) jo.get("c_node");
		} catch (ClassCastException e1) {
			addError("Unable to make the \"c_node\" in the incoming JSON into a String\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		catch(RuntimeException e1){
			addError("Unable to find the \"c_node\" parameter in the incoming JSON\n"+jo.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
			return;
		}
		
		if(configurations.containsKey(cNode)){
			JSONObject config = configurations.get(cNode);
			if(config == null){
				addError("Configuration for \""+cNode+"\" is explicitly null.");
				return;
			}
			else{
				JSONArray answer = null;
				try{
					answer = (JSONArray) config.get("predictors");
				} catch (ClassCastException e1) {
					addError("Unable to make the \"predictors\" in the configuration into a JSONArray.  The configuration is badly formatted\n"+config.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
				catch(RuntimeException e1){
					addError("Unable to make the \"predictors\" in the configuration into a JSONArray.  The configuration is badly formatted\n"+config.toJSONString(JSONStyle.NO_COMPRESS)+"\n"+e1);
					return;
				}
				addResponses(answer);
			}
		}
		else{
			addError("No information about this cNode: "+cNode);
		}
	}

}
