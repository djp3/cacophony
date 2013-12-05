package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.log4j.Logger;

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
	   
	    	Map<String, CNodeReference> cNodesA = ((MetaCNode)a).getCNodeReferences();
	    	Map<String, CNodeReference> cNodesB = ((MetaCNode)b).getCNodeReferences();
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


	public static MetaCNode fromJSONObject(JSONObject j){
		
		MetaCNode c = null;
		if(j != null){
			c = new MetaCNode();
			
			Long creationTime = null;
			try {
				creationTime = Long.parseLong((String) j.get("creation_time"));
				if(creationTime != null){
					c.setCreationTime(creationTime);
				}
			} catch (ClassCastException e) {
			} catch(NumberFormatException e){
			}
			
			String id = null;
			try {
				id = (String) j.get("id");
				if(id != null){
					c.setGuid(id);
				}
			} catch (ClassCastException e) {
			}
			
			String name = null;
			try {
				name = (String) j.get("name");
				if(name != null){
					c.setName(name);
				}
			} catch (ClassCastException e) {
			}
			
			String namespace = null;
			try {
				namespace = (String) j.get("namespace");
				if(namespace != null){
					c.setNamespace(namespace);
				}
			} catch (ClassCastException e) {
			}
			
			Double priority = null;
			try {
				priority = (Double) j.get("priority");
				if(priority != null){
					c.setPriority(priority);
				}
			} catch (ClassCastException e) {
			}
			
			JSONObject configuration = null;
			try {
				configuration = (JSONObject) j.get("configuration");
				if(configuration != null){
					c.setConfiguration(configuration);
				}
			} catch (ClassCastException e) {
			}
			
			Double latitude = null;
			try {
				latitude = (Double) j.get("latitude");
				if(latitude != null){
					c.setLatitude(latitude);
				}
			} catch (ClassCastException e) {
			}
			
			Double longitude = null;
			try {
				longitude = (Double) j.get("longitude");
				if(longitude != null){
					c.setLongitude(longitude);
				}
			} catch (ClassCastException e1) {
			}
			
			Double mapWeight = null;
			try {
				mapWeight = (Double) j.get("map_weight");
				if(mapWeight != null){
					c.setMapWeight(mapWeight);
				}
			} catch (ClassCastException e) {
			}
			
			JSONArray _cNodeReferences= null;
			try {
				_cNodeReferences = (JSONArray) j.get("c_node_references");
			} catch (ClassCastException e) {
			}
			
			if(_cNodeReferences == null){
				c.setCNodeReferences(null);
			}
			else{
				for(int idx = 0; idx < _cNodeReferences.size();idx++){
					JSONObject x = null;
					try{
						x = (JSONObject) _cNodeReferences.get(idx);
					} catch(ClassCastException e){
					}
					
					if( x != null){
						CNodeReference y = CNodeReference.fromJSONObject(x);
						
						if(y.getCNodeGuid() != null){
							c.getCNodeReferences().put(y.getCNodeGuid(),y);
						}
					}
				}
			}
		}
		
		return c;
	}
	public static JSONObject toJSONObject(MetaCNode c) {
		JSONObject jsonObject = null;
		if(c != null){
			jsonObject = new JSONObject();
			
			if(c.getCreationTime() != null){
				jsonObject.put("creation_time", c.getCreationTime());
			}
			if(c.getGuid() != null){
				jsonObject.put("id", c.getGuid());
			}
			if(c.getName() != null){
				jsonObject.put("name", c.getName());
			}
			if(c.getNamespace() != null){
				jsonObject.put("namespace", c.getNamespace());
			}
			if(c.getPriority() != null){
				jsonObject.put("priority", c.getPriority());
			}
			if(c.getConfiguration() != null){
				jsonObject.put("configuration", c.getConfiguration());
			}
			if(c.getLongitude() != null){
				jsonObject.put("longitude", c.getLongitude());
			}
			if(c.getLatitude() != null){
				jsonObject.put("latitude", c.getLatitude());
			}
			if(c.getMapWeight() != null){
				jsonObject.put("map_weight", c.getMapWeight());
			}
			if(c.getCNodeReferences() != null){
				Map<String, CNodeReference> l = c.getCNodeReferences();
				if(l != null){
					JSONArray _cNodeReferences = new JSONArray();
					for(Entry<String, CNodeReference> e:l.entrySet()){
						_cNodeReferences.add(e.getValue().toJSONObject());
					}
					jsonObject.put("c_node_references", _cNodeReferences);
				}
			}
		}
		return jsonObject;
	}

	/** For monitoring when the metaCNode was created, not used for equality **/
	private Long creationTime;
	private String guid;
	private String name;
	private String namespace;
	private Double latitude;
	private Double longitude;
	private Double mapWeight;
	private Double priority;
	private Map<String,CNodeReference> cNodeReferences = Collections.synchronizedMap(new TreeMap<String,CNodeReference>());
	private JSONObject configuration;
	
	public MetaCNode(){
		super();
	}
	
	public Long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
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
	public Map<String, CNodeReference> getCNodeReferences() {
		return cNodeReferences;
	}
	
	public void setCNodeReferences(Map<String, CNodeReference> cNodes) {
		this.cNodeReferences = cNodes;
	}
	
	public boolean equals(Object _that){
		if(this == _that) return true;
		if( !(_that instanceof MetaCNode) ) return false;
		
		MetaCNode that = (MetaCNode) _that;
		
		boolean ret = true;
		
		ret &= !((this.getGuid() == null) ^ (that.getGuid() == null));
		ret &= !((this.getName() == null) ^ (that.getName() == null));
		ret &= !((this.getNamespace() == null) ^ (that.getNamespace() == null));
		ret &= !((this.getLatitude() == null) ^ (that.getLatitude() == null));
		ret &= !((this.getLongitude() == null) ^ (that.getLongitude() == null));
		ret &= !((this.getMapWeight() == null) ^ (that.getMapWeight() == null));
		ret &= !((this.getPriority() == null) ^ (that.getPriority() == null));
		ret &= !((this.getCNodeReferences() == null) ^ (that.getCNodeReferences() == null));
		ret &= !((this.getConfiguration() == null) ^ (that.getConfiguration() == null));
		
		ret &= (this.getGuid() == null) || this.getGuid().equals(that.getGuid());
		ret &= (this.getName() == null) || this.getName().equals(that.getName());
		ret &= (this.getNamespace() == null) || this.getNamespace().equals(that.getNamespace());
		ret &= (this.getLatitude() == null) || this.getLatitude().equals(that.getLatitude());
		ret &= (this.getLongitude() == null) || this.getLongitude().equals(that.getLongitude());
		ret &= (this.getMapWeight() == null) || this.getMapWeight().equals(that.getMapWeight());
		ret &= (this.getPriority() == null) || this.getPriority().equals(that.getPriority());
		ret &= (this.getCNodeReferences() == null) || this.getCNodeReferences().equals(that.getCNodeReferences());
		ret &= (this.getConfiguration() == null) || this.getConfiguration().equals(that.getConfiguration());
		
		return ret;
	}
	
	public int hashCode(){
		int result = HashCodeUtil.SEED;

		result = HashCodeUtil.hash(result,this.getGuid());
		result = HashCodeUtil.hash(result,this.getName());
		result = HashCodeUtil.hash(result,this.getNamespace());
		result = HashCodeUtil.hash(result,this.getLatitude());
		result = HashCodeUtil.hash(result,this.getLongitude());
		result = HashCodeUtil.hash(result,this.getMapWeight());
		result = HashCodeUtil.hash(result,this.getPriority());
		result = HashCodeUtil.hash(result,this.getCNodeReferences());
		result = HashCodeUtil.hash(result,this.getConfiguration());
		
		return(result);
	}
	
	
	public JSONObject toJSONObject() {
		return MetaCNode.toJSONObject(this);
	}

}
