package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.util.List;


import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class NodeListLoader {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(NodeListLoader.class);
		}
		return log;
	}
	
	public JSONObject getConfiguration(String id){
		JSONObject ret = new JSONObject();
		try {
			ret.put(NodeConfigurationProperty.PREDICTION_STYLE,NodeConfigurationProperty.PREDICTION_STYLE_MOD);
		} catch (JSONException e) {
			getLog().fatal("This should not fail:"+e);
		}
		return(ret);
	}
	
	abstract public void init(JSONObject options);
	abstract public List<MetaCNode> loadNodeList();
}
