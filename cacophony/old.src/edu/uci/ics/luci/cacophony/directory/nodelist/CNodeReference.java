package edu.uci.ics.luci.cacophony.directory.nodelist;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.util.HashCodeUtil;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class CNodeReference {
	
	private static transient volatile Logger log = null;
	
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(MetaCNode.class);
		}
		return log;
	}
	
	public static CNodeReference fromJSONObject(JSONObject x) {
		CNodeReference c = null;
		if(x != null){
			c = new CNodeReference();
				
			String mcg = null;
			try{
				mcg = (String) x.get("meta_cnode_guid");
				if(mcg != null){
					c.setMetaCNodeGuid(mcg);
				}
			}
			catch(ClassCastException e){
				getLog().warn("meta_cnode_guid isn't a string:"+x);
			}
			
			String cg = null;
			try{
				cg = (String) x.get("cnode_guid");
				if(cg != null){
					c.setCNodeGuid(cg);
				}
			}
			catch(ClassCastException e){
				getLog().warn("cnode_guid isn't a string:"+x);
			}
			
			Long lastHeartbeat = null;
			try{
				lastHeartbeat = Long.parseLong((String) x.get("last_heartbeat"));
				if(lastHeartbeat != null){
					c.setLastHeartbeat(lastHeartbeat);
				}
			}
			catch(ClassCastException e){
				getLog().warn("last_hearbeat isn't a String:"+x);
			}
			catch(NumberFormatException e){
				getLog().warn("last_hearbeat isn't a Long:"+x);
			}
				
			JSONArray ars = null;
			try{
				ars = (JSONArray) x.get("access_routes_for_ui");
				if(ars != null){
					Set<Pair<Long, String>> newArs = new TreeSet<Pair<Long,String>>(Collections.reverseOrder());
					for(int i = 0; i< ars.size(); i++){
						Long priority = Long.parseLong(((String)((JSONObject)ars.get(i)).get("priority")));
						String route = ((String)((JSONObject)ars.get(i)).get("route"));
						if((priority != null) && (route != null)){
							Pair<Long,String> p = new Pair<Long,String>(priority,route);
							newArs.add(p);
						}
					}
					c.setAccessRoutesForUI(newArs);
				}
			}
			catch(ClassCastException e){
				getLog().warn("access_routes_for_ui have unknown classes:"+x);
			}
			catch(NumberFormatException e){
				getLog().warn("access_routes_for_ui have unparseable elements:"+x);
			}
			
			ars = null;
			try{
				ars = (JSONArray) x.get("access_routes_for_api");
				if(ars != null){
					Set<Pair<Long, String>> newArs = new TreeSet<Pair<Long,String>>(Collections.reverseOrder());
					for(int i = 0; i< ars.size(); i++){
						Long priority = Long.parseLong((String)((JSONObject) ars.get(i)).get("priority"));
						String route = ((String)((JSONObject) ars.get(i)).get("route"));
						if((priority != null) && (route != null)){
							Pair<Long,String> p = new Pair<Long,String>(priority,route);
							newArs.add(p);
						}
					}
					c.setAccessRoutesForAPI(newArs);
				}
			}
			catch(ClassCastException e){
				getLog().warn("access_routes_for_api have unknown classes:"+x);
			}
			catch(NumberFormatException e){
				getLog().warn("access_routes_for_api have unparseable elements:"+x);
			}
		}
		return c;
	}
	
	public static JSONObject toJSONObject(CNodeReference c) {
		JSONObject ret = null;
		if(c != null){
			ret = new JSONObject();
			if(c.getMetaCNodeGuid() != null){
				ret.put("meta_cnode_guid", c.getMetaCNodeGuid());
			}
			if(c.getCNodeGuid() != null){
				ret.put("cnode_guid", c.getCNodeGuid());
			}
				
			if(c.getLastHeartbeat() != null){
				ret.put("last_heartbeat", c.getLastHeartbeat().toString());
			}
				
			if(c.getAccessRoutesForUI() != null){
				JSONArray ars = new JSONArray();
				for(Pair<Long,String> p:c.getAccessRoutesForUI()){
					JSONObject ar = new JSONObject();
					ar.put("priority",p.getFirst().toString());
					ar.put("route",p.getSecond());
					ars.add(ar);
				}
				ret.put("access_routes_for_ui", ars);
			}
			
			if(c.getAccessRoutesForAPI() != null){
				JSONArray ars = new JSONArray();
				for(Pair<Long,String> p:c.getAccessRoutesForAPI()){
					JSONObject ar = new JSONObject();
					ar.put("priority",p.getFirst().toString());
					ar.put("route",p.getSecond());
					ars.add(ar);
				}
				ret.put("access_routes_for_api", ars);
			}
		}
		return ret;
	}
	
	
	String metaCNodeGuid = null;
	String cNodeGuid = null;
	Long lastHeartbeat = null; /* Not included in equality tests */
	Set<Pair<Long,String>> accessRoutesForUI = null;
	Set<Pair<Long,String>> accessRoutesForAPI = null;
	
	public String getMetaCNodeGuid() {
		return metaCNodeGuid;
	}
	public void setMetaCNodeGuid(String metaCNodeGuid) {
		this.metaCNodeGuid = metaCNodeGuid;
	}
	public String getCNodeGuid() {
		return cNodeGuid;
	}
	public void setCNodeGuid(String cNodeGuid) {
		this.cNodeGuid = cNodeGuid;
	}
	public Long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(Long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	public Set<Pair<Long, String>> getAccessRoutesForUI() {
		return accessRoutesForUI;
	}
	public void setAccessRoutesForUI(Set<Pair<Long, String>> accessRoutesUI) {
		this.accessRoutesForUI = accessRoutesUI;
	}

	public Set<Pair<Long, String>> getAccessRoutesForAPI() {
		return accessRoutesForAPI;
	}
	public void setAccessRoutesForAPI(Set<Pair<Long, String>> accessRoutesAPI) {
		this.accessRoutesForAPI = accessRoutesAPI;
	}
	
	public boolean equals(Object _that){
		if(this == _that) return true;
		if( !(_that instanceof CNodeReference) ) return false;
		
		CNodeReference that = (CNodeReference) _that;
		
		boolean ret = true;
		
		ret &= !((this.getMetaCNodeGuid() == null) ^ (that.getMetaCNodeGuid() == null));
		ret &= !((this.getCNodeGuid() == null) ^ (that.getCNodeGuid() == null));
		ret &= !((this.getAccessRoutesForUI() == null) ^ (that.getAccessRoutesForUI() == null));
		ret &= !((this.getAccessRoutesForAPI() == null) ^ (that.getAccessRoutesForAPI() == null));
		
		ret &= (this.getMetaCNodeGuid() == null) || this.getMetaCNodeGuid().equals(that.getMetaCNodeGuid());
		ret &= (this.getCNodeGuid() == null) || this.getCNodeGuid().equals(that.getCNodeGuid());
		ret &= (this.getAccessRoutesForUI() == null) || this.getAccessRoutesForUI().equals(that.getAccessRoutesForUI());
		ret &= (this.getAccessRoutesForAPI() == null) || this.getAccessRoutesForAPI().equals(that.getAccessRoutesForAPI());
		
		return ret;
	}
	
	public int hashCode(){
		int result = HashCodeUtil.SEED;

		result = HashCodeUtil.hash(result,this.getMetaCNodeGuid());
		result = HashCodeUtil.hash(result,this.getCNodeGuid());
		result = HashCodeUtil.hash(result,this.getAccessRoutesForUI());
		result = HashCodeUtil.hash(result,this.getAccessRoutesForAPI());
		
		return(result);
	}
	
	public CNodeReference() {
		this(null);
	}
	
	public CNodeReference(CNodeReference a) {
		super();
		if(a != null){
			this.setMetaCNodeGuid(a.getMetaCNodeGuid());
			this.setCNodeGuid(a.getCNodeGuid());
			this.setLastHeartbeat(a.getLastHeartbeat());
			this.setAccessRoutesForUI(a.getAccessRoutesForUI());
			this.setAccessRoutesForAPI(a.getAccessRoutesForAPI());
		}
	}


	public JSONObject toJSONObject() {
		return(CNodeReference.toJSONObject(this));
	}
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append("CNodeReference:{\n");
		if(getMetaCNodeGuid()!=null){
			b.append("\tMetaCNodeGuid:");
			b.append(getMetaCNodeGuid());
			b.append("\n");
		}
		if(getCNodeGuid()!=null){
			b.append("\tCNodeGuid    :");
			b.append(getCNodeGuid());
			b.append("\n");
		}
		if(getLastHeartbeat()!=null){
			b.append("\tLast heartbeat    :");
			b.append(getLastHeartbeat());
			b.append("\n");
		}
		if(getAccessRoutesForUI() != null){
			b.append("\tUI Routes:\n");
			for(Pair<Long, String> p:getAccessRoutesForUI()){
				b.append("\t\t");
				b.append(p.toString());
				b.append("\n");
			}
		}
		if(getAccessRoutesForAPI() != null){
			b.append("\tAPI Routes:\n");
			for(Pair<Long, String> p:getAccessRoutesForAPI()){
				b.append("\t\t");
				b.append(p.toString());
				b.append("\n");
			}
		}
		b.append("}\n");
		return b.toString();
	}

	

}
