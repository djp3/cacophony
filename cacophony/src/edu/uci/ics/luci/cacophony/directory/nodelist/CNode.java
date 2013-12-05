package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.net.URI;
import java.net.URISyntaxException;

import net.minidev.json.JSONObject;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.util.HashCodeUtil;

public class CNode{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNode.class);
		}
		return log;
	}
	
	public static CNode fromJSONObject(JSONObject j){
		
		CNode c = null;
		if(j != null){
			c = new CNode();
			String guid;
			try {
				guid = (String)j.get("guid");
				c.setGuid(guid);
			} catch (ClassCastException e1) {
			}
			
			String _uri = null;
			URI uri;
			try {
				_uri = (String)j.get("uri");
				uri = new URI(_uri);
				c.setUri(uri);
			} catch (ClassCastException e1) {
			} catch (URISyntaxException e) {
				getLog().error("Unable to create uri from:"+_uri);
			}
			
			Long heartbeat;
			try {
				heartbeat = Long.parseLong((String)j.get("last_heartbeat"));
				c.setLastHeartbeat(heartbeat);
			} catch (ClassCastException | NumberFormatException e1) {
			}
		}
		return c;
	}

	public static JSONObject toJSONObject(CNode c) {
		if(c == null){
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("guid", c.getGuid());
			jsonObject.put("last_heartbeat", c.getLastHeartbeat());
			URI _uri = c.getUri();
			if(_uri != null){
				jsonObject.put("uri", _uri.toString());
			}
		} catch (ClassCastException e) {
			getLog().error("Unable to make JSONObject: id = "+c.getGuid()+", last_heartbeat = "+c.getLastHeartbeat()+", uri:"+c.getUri()+"\n"+e);
		}
		return jsonObject;
	}
	
	private String guid;
	private Long lastHeartbeat;
	private URI uri;
	
	public CNode() {
		this(null);
	}
	
	public CNode(CNode a) {
		if(a != null){
			this.setGuid(a.getGuid());
			this.setLastHeartbeat(a.getLastHeartbeat());
			this.setUri(a.getUri());
		}
	}

	
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public Long getLastHeartbeat() {
		return lastHeartbeat;
	}
	public void setLastHeartbeat(Long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	public URI getUri() {
		return uri;
	}
	public void setUri(URI uri) {
		this.uri = uri;
	}

	public JSONObject toJSONObject() {
		return CNode.toJSONObject(this);
	}
	
	public boolean equals(Object _that){
		if(this == _that) return true;
		if( !(_that instanceof CNode) ) return false;
		
		CNode that = (CNode) _that;
		
		boolean ret = true;
		
		ret &= !((this.getGuid() == null) ^ (that.getGuid() == null));
		ret &= !((this.getLastHeartbeat() == null) ^ (that.getLastHeartbeat() == null));
		ret &= !((this.getUri() == null) ^ (that.getUri() == null));
		
		ret &= (this.getGuid() == null) || this.getGuid().equals(that.getGuid());
		ret &= (this.getLastHeartbeat() == null) || this.getLastHeartbeat().equals(that.getLastHeartbeat());
		ret &= (this.getUri() == null) || this.getUri().equals(that.getUri());
		
		return ret;
	}
	
	public int hashCode(){
		int result = HashCodeUtil.SEED;

		result = HashCodeUtil.hash(result,this.getGuid());
		result = HashCodeUtil.hash(result,this.getLastHeartbeat());
		result = HashCodeUtil.hash(result,this.getUri());
		
		return(result);
	}
	
	

}
