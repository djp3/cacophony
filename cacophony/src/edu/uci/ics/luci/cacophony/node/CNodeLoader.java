package edu.uci.ics.luci.cacophony.node;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.uci.ics.luci.util.FailoverFetch;
import edu.uci.ics.luci.utility.datastructure.Pair;

public abstract class CNodeLoader {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNodeLoader.class);
		}
		return log;
	}
	
	abstract public void init(JSONObject options);
	abstract public List<CNode> loadCNodes(CNodePool parent, FailoverFetch failoverFetch, List<Pair<Long,String>> baseUrls);

}
