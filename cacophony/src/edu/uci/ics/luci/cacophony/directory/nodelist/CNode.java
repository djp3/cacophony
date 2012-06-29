package edu.uci.ics.luci.cacophony.directory.nodelist;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class CNode {
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNode.class);
		}
		return log;
	}

	private String id;
	private String name;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", getId());
			jsonObject.put("name", getName());
		} catch (JSONException e) {
			getLog().error("Unable to make JSONObject: id = "+getId()+", name = "+getName()+"\n"+e);
		}
		return jsonObject;
	}

}
