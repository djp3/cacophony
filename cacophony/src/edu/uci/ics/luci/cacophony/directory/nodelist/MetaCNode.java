package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.uci.ics.luci.util.HashCodeUtil;

public class MetaCNode {
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MetaCNode.class);
		}
		return log;
	}
	
	/**
	 *  Sorts a list of MetaCNodes so that the highest priority is first.
	 */
	public static final Comparator<MetaCNode> ByPriority= new Comparator<MetaCNode>(){
		
	    public int compare(MetaCNode a,MetaCNode b){
	   
	        Double priority1 = ((MetaCNode)a).getPriority();        
	        Double priority2 = ((MetaCNode)b).getPriority();
	        
	    	if((priority1 == null) && (priority2 == null)){
	    		return 0;
	    	}
	        
	        if(priority1 == null){ 
	        	return(-1);
	        }
	        
	        if(priority2 == null){ 
	        	return(1);
	        }
	        
	        return(priority2.compareTo(priority1));
	    }
	   
	};
	
	/**
	 * Returns the MetaCNode with the least CNodes tracking it.  If there is a tie it returns the highest priority
	 */
	public static final Comparator<MetaCNode> ByAssignmentPaucity = new Comparator<MetaCNode>(){
		   
	    public int compare(MetaCNode a,MetaCNode b){
	    	
	    	if((a == null) && (b == null)){
	    		return 0;
	    	}
	    	if(a == null){
	    		return -1;
	    	}
	    	if(b == null){
	    		return 1;
	    	}
	   
	    	List<URL> cNodesA = ((MetaCNode)a).getCNodes();
	    	List<URL> cNodesB = ((MetaCNode)b).getCNodes();
	        Double size1 = null;
	        Double size2 = null;
	        
	        if(cNodesA == null){ 
	        	size1 = Double.valueOf(0.0d);
	        }
	        else{
	        	size1 = Double.valueOf(cNodesA.size());
	        }
	        
	        if(cNodesB == null){ 
	        	size2 = Double.valueOf(0.0d);
	        }
	        else{
	        	size2 = Double.valueOf(cNodesB.size());
	        }
	        
	        int comp = size1.compareTo(size2);
	        if(comp != 0){
	        	return(comp);
	        }
	        else{
	        	return(MetaCNode.ByPriority.compare(a,b));
	        }
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
	
	public boolean equals(Object _that){
		if(this == _that) return true;
		if( !(_that instanceof MetaCNode) ) return false;
		
		MetaCNode that = (MetaCNode) _that;
		
		boolean ret = true;
		
		ret &= !((this.getId() == null) ^ (that.getId() == null));
		ret &= !((this.getName() == null) ^ (that.getName() == null));
		ret &= !((this.getLatitude() == null) ^ (that.getLatitude() == null));
		ret &= !((this.getLongitude() == null) ^ (that.getLongitude() == null));
		ret &= !((this.getMapWeight() == null) ^ (that.getMapWeight() == null));
		ret &= !((this.getPriority() == null) ^ (that.getPriority() == null));
		ret &= !((this.getCNodes() == null) ^ (that.getCNodes() == null));
		ret &= !((this.getConfiguration() == null) ^ (that.getConfiguration() == null));
		
		ret &= (this.getId() == null) || this.getId().equals(that.getId());
		ret &= (this.getName() == null) || this.getName().equals(that.getName());
		ret &= (this.getLatitude() == null) || this.getLatitude().equals(that.getLatitude());
		ret &= (this.getLongitude() == null) || this.getLongitude().equals(that.getLongitude());
		ret &= (this.getMapWeight() == null) || this.getMapWeight().equals(that.getMapWeight());
		ret &= (this.getPriority() == null) || this.getPriority().equals(that.getPriority());
		ret &= (this.getCNodes() == null) || this.getCNodes().equals(that.getCNodes());
		ret &= (this.getConfiguration() == null) || this.getConfiguration().equals(that.getConfiguration());
		
		return ret;
	}
	
	public int hashCode(){
		int result = HashCodeUtil.SEED;

		result = HashCodeUtil.hash(result,this.getId());
		result = HashCodeUtil.hash(result,this.getName());
		result = HashCodeUtil.hash(result,this.getLatitude());
		result = HashCodeUtil.hash(result,this.getLongitude());
		result = HashCodeUtil.hash(result,this.getMapWeight());
		result = HashCodeUtil.hash(result,this.getPriority());
		result = HashCodeUtil.hash(result,this.getCNodes());
		result = HashCodeUtil.hash(result,this.getConfiguration());
		
		return(result);
	}
	
	public static MetaCNode fromJSONObject(JSONObject j){
		
		MetaCNode c = null;
		if(j != null){
			c = new MetaCNode();
			String id;
			try {
				id = j.getString("id");
				c.setId(id);
			} catch (JSONException e1) {
			}
			
			String name;
			try {
				name = j.getString("name");
				c.setName(name);
			} catch (JSONException e1) {
			}
			
			Double priority;
			try {
				priority = j.getDouble("priority");
				c.setPriority(priority);
			} catch (JSONException e1) {
			}
			
			JSONObject configuration;
			try {
				configuration = j.getJSONObject("configuration");
				c.setConfiguration(configuration);
			} catch (JSONException e1) {
			}
			
			Double latitude;
			try {
				latitude = j.getDouble("latitude");
				c.setLatitude(latitude);
			} catch (JSONException e1) {
			}
			
			Double longitude;
			try {
				longitude = j.getDouble("longitude");
				c.setLongitude(longitude);
			} catch (JSONException e1) {
			}
			
			Double mapWeight;
			try {
				mapWeight = j.getDouble("map_weight");
				c.setMapWeight(mapWeight);
			} catch (JSONException e1) {
			}
			
			JSONArray _cNodes = null;
			try {
				_cNodes = j.getJSONArray("c_nodes");
			} catch (JSONException e1) {
			}
			if(_cNodes == null){
				c.setCNodes(null);
			}
			else{
				for(int idx = 0; idx < _cNodes.length();idx++){
					try {
						c.getCNodes().add(new URL(_cNodes.getString(idx)));
					} catch (MalformedURLException e) {
						getLog().error("Unable to make MetaCNode from JSON: "+_cNodes.toString()+"\n"+e);
					} catch (JSONException e) {
						getLog().error("Unable to make MetaCNode from JSON: "+_cNodes.toString()+"\n"+e);
					}
				}
			}
		}
		
		return c;
	}
	
	public JSONObject toJSONObject() {
		return MetaCNode.toJSONObject(this);
	}
	
	public static JSONObject toJSONObject(MetaCNode c) {
		if(c == null){
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", c.getId());
			jsonObject.put("name", c.getName());
			jsonObject.put("priority", c.getPriority());
			jsonObject.put("configuration", c.getConfiguration());
			jsonObject.put("longitude", c.getLongitude());
			jsonObject.put("latitude", c.getLatitude());
			jsonObject.put("map_weight", c.getMapWeight());
			List<URL> l = c.getCNodes();
			if(l != null){
				JSONArray _cNodes = new JSONArray();
				for(URL u:l){
					_cNodes.put(u.toString());
				}
				jsonObject.put("c_nodes", _cNodes);
			}
		} catch (JSONException e) {
			getLog().error("Unable to make JSONObject: id = "+c.getId()+", name = "+c.getName()+"\n"+e);
		}
		return jsonObject;
	}

}
