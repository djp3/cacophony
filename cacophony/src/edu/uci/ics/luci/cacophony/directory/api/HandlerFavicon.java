package edu.uci.ics.luci.cacophony.directory.api;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;

import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Pair;
import com.quub.webserver.RequestHandlerHelper;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;

public class HandlerFavicon extends RequestHandlerHelper {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerFavicon.class);
		}
		return log;
	}


	public HandlerFavicon() {
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
	public Pair<byte[], String> handle(String restFunction,Map<String, String> headers, Map<String, String> parameters, InetAddress ip, QuubDBConnectionPool odbcp){
		return new Pair<byte[],String>(RequestHandlerHelper.contentTypeHeader_REDIRECT_UNSPECIFIED,"http://djp3-pc7.ics.uci.edu/cacophony-dev/projects/cacophony/repository/revisions/production/raw/cacophony/doc/graphics/favicon.ico");
	}
}


