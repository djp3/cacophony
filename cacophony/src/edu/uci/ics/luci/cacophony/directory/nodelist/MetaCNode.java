package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MetaCNode {
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MetaCNode.class);
		}
		return log;
	}
	
	public static class CNodePriorityComparator implements Comparator<MetaCNode>{
		
		need to write a test for this and make it look like the one below
		   
	    public int compare(MetaCNode a,MetaCNode b){
	   
	        double priority1 = ((MetaCNode)a).getPriority();        
	        double priority2 = ((MetaCNode)b).getPriority();
	       
	        if(priority1 > priority2)
	            return 1;
	        else if(priority1 < priority2)
	            return -1;
	        else
	            return 0;    
	    }
	   
	}
	
	public static final Comparator<MetaCNode> MetaCNodeAssignmentComparator = new Comparator<MetaCNode>(){
		
		need to write a test for this
		   
	    public int compare(MetaCNode a,MetaCNode b){
	   
	        double size1 = ((MetaCNode)a).getCNodes().size();
	        double size2 = ((MetaCNode)b).getCNodes().size();
	        double priority1 = ((MetaCNode)a).getPriority();        
	        double priority2 = ((MetaCNode)b).getPriority();
	       
	        if(size1 > size2)
	            return 1;
	        else if(size1 < size2)
	            return -1;
	        else
	        	if(priority1 > priority2)
	        		return 1;
	        	else if(priority1 < priority2)
	        		return -1;
	        	else
	        		return 0;    
	    }
	   
	};

	private String id;
	private String name;
	private Double latitude;
	private Double longitude;
	private Double mapWeight;
	private Double priority;
	private List<URL> cNodes = Collections.synchronizedList(new ArrayList<URL>(1));
	private JSONObject configuration;
	
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
	
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public Double getMapWeight() {
		return mapWeight;
	}
	public void setMapWeight(Double mapWeight) {
		this.mapWeight = mapWeight;
	}
	
	public Double getPriority() {
		return priority;
	}
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	public JSONObject getConfiguration() {
		return configuration;
	}
	public void setConfiguration(JSONObject configuration) {
		this.configuration = configuration;
	}
	public List<URL> getCNodes() {
		return cNodes;
	}
	
	public void setCNodes(List<URL> cNodes) {
		this.cNodes = cNodes;
	}
	
	public static MetaCNode fromJSONObject(JSONObject j){
		
		MetaCNode c = null;
		try{
			c = new MetaCNode();
			String id = j.getString("id");
			c.setId(id);
			String name = j.getString("name");
			c.setName(name);
			Double priority = j.getDouble("priority");
			c.setPriority(priority);
			JSONObject configuration = j.getJSONObject("configuration");
			c.setConfiguration(configuration);
			Double latitude = j.getDouble("latitude");
			c.setLatitude(latitude);
			Double longitude = j.getDouble("longitude");
			c.setLongitude(longitude);
			Double mapWeight = j.getDouble("mapWeight");
			c.setMapWeight(mapWeight);
		} catch (JSONException e) {
		}
		return c;
	}
	
	public JSONObject toJSONObject() {
		return MetaCNode.toJSONObject(this);
	}
	
	public static JSONObject toJSONObject(MetaCNode c) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", c.getId());
			jsonObject.put("name", c.getName());
			jsonObject.put("priority", c.getPriority());
			jsonObject.put("configuration", c.getConfiguration());
			jsonObject.put("longitude", c.getLongitude());
			jsonObject.put("latitude", c.getLatitude());
			jsonObject.put("map_weight", c.getMapWeight());
		} catch (JSONException e) {
			getLog().error("Unable to make JSONObject: id = "+c.getId()+", name = "+c.getName()+"\n"+e);
		}
		return jsonObject;
	}

}
