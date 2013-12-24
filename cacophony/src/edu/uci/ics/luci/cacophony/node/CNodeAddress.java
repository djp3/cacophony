package edu.uci.ics.luci.cacophony.node;

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
public class CNodeAddress {
	
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


}
