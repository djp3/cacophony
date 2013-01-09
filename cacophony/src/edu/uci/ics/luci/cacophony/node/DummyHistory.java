package edu.uci.ics.luci.cacophony.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.uci.ics.luci.util.FailoverFetch;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class DummyHistory extends CNodeLoader{
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(DummyHistory.class);
		}
		return log;
	}
	
	@Override
	public void init(JSONObject options) {
		getLog().debug(this.getClass().getCanonicalName()+" init called");
	}
	
	@Override
	public List<CNode> loadCNodes(CNodePool parent, FailoverFetch failoverFetch, List<Pair<Long, String>> baseUrls) {
		getLog().debug(this.getClass().getCanonicalName()+" loadCNodes called");
		return new ArrayList<CNode>();
	}

}
