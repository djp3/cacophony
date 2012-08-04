package edu.uci.ics.luci.cacophony.directory.api;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Pair;
import com.quub.webserver.HandlerAbstract;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.directory.Directory;

public class HandlerDirectoryNamespace extends HandlerAbstract {
	
	private transient volatile Logger log = null;
	public Logger getLog(){
		if(this.log == null){
			this.log = Logger.getLogger(HandlerDirectoryNamespace.class);
		}
		return this.log;
	}


	public HandlerDirectoryNamespace() {
		super();
	}
	
	protected CacophonyGlobals getGlobals(){
		return (CacophonyGlobals) super.getGlobals();
	}
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(String restFunction,Map<String, String> headers, Map<String, String> parameters, InetAddress ip, QuubDBConnectionPool odbcp){
		Pair<byte[], byte[]> pair = null;
		
		String version = Globals.getGlobals().getVersion();
		String namespace = Directory.getInstance().getDirectoryNamespace();
		
		JSONObject ret = new JSONObject();
		try {
			ret.put("version", version);
			ret.put("namespace", namespace);
			ret.put("error", "false");
		} catch (JSONException e) {
			getLog().error("Unable to respond with version:"+e);
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.contentTypeHeader_JSON,wrapCallback(parameters,ret.toString()).getBytes());
		
		getLog().info("Version is "+version);
		return pair;
	}
}


