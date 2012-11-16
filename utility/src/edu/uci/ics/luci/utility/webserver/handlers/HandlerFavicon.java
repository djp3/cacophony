package edu.uci.ics.luci.utility.webserver.handlers;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;


import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerFavicon extends HandlerAbstract {
	
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
	
	@Override
	public HandlerFavicon copy() {
		return new HandlerFavicon();
	}
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		return new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_REDIRECT_UNSPECIFIED(),"http://djp3-pc7.ics.uci.edu/cacophony-dev/projects/cacophony/repository/revisions/production/raw/cacophony/doc/graphics/favicon.ico".getBytes());
	}

}


