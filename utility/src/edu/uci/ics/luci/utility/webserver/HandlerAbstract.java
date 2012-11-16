package edu.uci.ics.luci.utility.webserver;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;


public abstract class HandlerAbstract {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerAbstract.class);
		}
		return log;
	}
	
	public static byte[] getContentTypeHeader_JSON(){
		return "Content-type:  application/json; charset=UTF-8".getBytes();
	}

	public static byte[] getContentTypeHeader_HTML(){
		return "Content-type:  text/html; charset=UTF-8".getBytes();
	}

	public static byte[] getContentTypeHeader_CSS(){
		return "Content-type:  text/css; charset=UTF-8".getBytes();
	}

	public static byte[] getContentTypeHeader_JS(){
		return "Content-type:  text/javascript; charset=UTF-8".getBytes();
	}

	public static byte[] getContentTypeHeader_PNG(){
		return "Content-type:  image/png; charset=UTF-8".getBytes();
	}

	public static byte[] getContentTypeHeader_REDIRECT_UNSPECIFIED() {
		return "redirect ".getBytes();
	}

	public static byte[] getContentTypeHeader_PROXY(){
		return "proxy ".getBytes();
	}

	public HandlerAbstract(){
		super();
	}
	
	/**
	 * This function should be overridden to actually do something in response to a REST call
	 * @param ip, The ip address from which the request came 
	 * @param httpRequestType, The type of HTTP Request that was received, like: "GET" 
	 * @param headers, the HTML headers in the request 
	 * @param restFunction, the function that was in the URL that caused this code to be invoked, like: "index.html"
	 * @param parameters, a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the second element are the output bytes to send back
	 */
	public abstract Pair<byte[],byte[]> handle(InetAddress ip,HTTPRequest httpRequestType,Map<String, String> headers,String restFunction, Map<String, String> parameters);
	
	/** This function is called to duplicate a Handler before being
	 * dispatched to handle an incoming request.
	 */
	public abstract HandlerAbstract copy();

	protected String wrapCallback(Map<String, String> parameters, String string) {
		if(parameters != null){
			String callback = parameters.get("callback");
			if(callback != null){
				return callback+"("+string+")";
			}
			else{
				return string;
			}
		}
		else{
			return string;
		}
	}
	
	/** From : http://stackoverflow.com/a/5445161
	 * 
	 * @param is
	 * @return
	 */
	protected String convertStreamToString(java.io.InputStream is) {
	    try {
	        return new java.util.Scanner(is).useDelimiter("\\A").next();
	    } catch (java.util.NoSuchElementException e) {
	        return "";
	    }
	}


}
