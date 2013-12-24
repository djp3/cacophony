package edu.uci.ics.luci.cacophony.node;

import edu.uci.ics.luci.utility.datastructure.HashCodeUtil;

/**
 * @author djp3
 * A full address is correctly formes like "p2p://server/this/is/the/path"
 * in this case
 *  server = "server"
 *  path = "/this/is/the/path" 
 *  
 *  This class will always return a fully-formed URL from the toString method,
 *  this despite the fact that it may be constructed in various degenerate ways.
 *  For example, paths should always start with a "/".
 *  
 */
public class CNodeAddress implements Comparable<CNodeAddress>{
	
	private String server;
	private String path;

	CNodeAddress(String cNodeServer,String cNodePath){
		setServer(cNodeServer);
		setPath(cNodePath);
	}
	
	CNodeAddress(String wholepath){
		String working = wholepath.toLowerCase().trim();
		
		if(working.startsWith("p2p://")){
			String strip = wholepath.substring(6);
			int index = strip.indexOf("/");
			if(index != -1){
				this.server = strip.substring(0, index);
				this.path = strip.substring(index,strip.length());
			}
			else{
				this.server = strip;
				this.path = "";
			}
		}
		else if(working.startsWith("//")){
			String strip = wholepath.substring(2);
			int index = strip.indexOf("/");
			if(index != -1){
				this.server = strip.substring(0, index);
				this.path = strip.substring(index,strip.length());
			}
			else{
				this.server = strip;
				this.path = "";
			}
		}
		else{
			int index = wholepath.indexOf("/");
			if(index != -1){
				this.server = wholepath.substring(0, index);
				this.path = wholepath.substring(index,wholepath.length());
			}
			else{
				this.server = wholepath;
				this.path = "";
			}
		}
		
	}
	
	public String toString(){
		if(path.equals("")){
			return ("p2p://"+server+"/");
		}
		else{
			return ("p2p://"+server+path);
		}
	}
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		if(server == null){
			throw new IllegalArgumentException("a CNode can't have a null server");
		}
		this.server = server;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		if(path == null){
			throw new IllegalArgumentException("a CNode can't have a null path");
		}
		if(!path.startsWith("/") && !path.equals("")){
			throw new IllegalArgumentException("a CNode path must start with a \"/\"");
		}
		this.path = path;
	}

	@Override
	public int compareTo(CNodeAddress o) {
		int s = this.server.compareTo(o.server);
		return s == 0 ? this.path.compareTo(o.path): s; 
	}
	
	@Override
	public boolean equals(Object _that) {
		
		if(_that == null) return false;
		if(this == _that) return true;
		if(!(_that instanceof CNodeAddress)) return false; 
		
		CNodeAddress that = (CNodeAddress)_that;
		
		if(this.server.equals(that.server)){
			return this.path.equals(that.path);
		}
		else{
			return false;
		}
	}
	
	public int hashCode(){
		int result = HashCodeUtil.SEED;

		result = HashCodeUtil.hash(result,this.getServer());
		result = HashCodeUtil.hash(result,this.getPath());
		
		return(result);
	}



}
