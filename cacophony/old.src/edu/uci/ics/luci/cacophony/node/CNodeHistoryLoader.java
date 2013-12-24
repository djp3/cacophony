package edu.uci.ics.luci.cacophony.node;

import net.minidev.json.JSONObject;

import org.apache.log4j.Logger;

import weka.core.Instances;

public abstract class CNodeHistoryLoader {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNodeHistoryLoader.class);
		}
		return log;
	}
	
	abstract public void init(JSONObject options);
	abstract public Instances loadCNodeHistory();
}
