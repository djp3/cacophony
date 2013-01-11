package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public abstract class NodeListLoader {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(NodeListLoader.class);
		}
		return log;
	}
	
	abstract public void init(JSONObject options);
	abstract public List<MetaCNode> loadNodeList();
}
