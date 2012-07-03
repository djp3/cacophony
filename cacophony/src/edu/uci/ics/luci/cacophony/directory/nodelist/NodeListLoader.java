package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.util.Map;

import org.json.JSONObject;

public abstract class NodeListLoader {
	abstract public void init(JSONObject options);
	abstract public Map<String,MetaCNode> loadNodeList();
}
