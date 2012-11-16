package edu.uci.ics.luci.utility.webserver.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerFileServer extends HandlerAbstract {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerFileServer.class);
		}
		return log;
	}


	private String resourcePrefix;
	private Class<?> resourceBaseClass;

	/**
	 * 
	 * @param resourcePrefix, something like "/www/" for finding the relevant files in the package
	 * @param resourceBaseClass, something like Globals.getGlobals().getClass(), which says which package to look in
	 */
	public HandlerFileServer(Class<?> resourceBaseClass,String resourcePrefix){
		super();
		this.resourceBaseClass = resourceBaseClass;
		this.resourcePrefix = resourcePrefix;
	}


	@Override
	public HandlerFileServer copy() {
		return new HandlerFileServer(this.resourceBaseClass,this.resourcePrefix);
	}
	
	
	/**
	 * This returns the version number.
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		Pair<byte[], byte[]> pair = null;
		byte[] ret = null;
		byte[] type = null;
		
		InputStream ios = null;
		
		String resource = resourcePrefix+restFunction;
		ios = resourceBaseClass.getResourceAsStream(resource);
		
		try{
			if(ios != null){
				if(restFunction.endsWith(".css")){
					ret = convertStreamToString(ios).getBytes();
					type = HandlerAbstract.getContentTypeHeader_CSS();
				}
				else if(restFunction.endsWith(".png")){
					ret = IOUtils.toByteArray(ios);
					type = HandlerAbstract.getContentTypeHeader_PNG();
				}
				else if(restFunction.endsWith(".js")){
					ret = convertStreamToString(ios).getBytes();
					type = HandlerAbstract.getContentTypeHeader_JS();
				}else{
					ret = convertStreamToString(ios).getBytes();
					type = HandlerAbstract.getContentTypeHeader_HTML();
				}
			}
			else{
				ret = ("Resource not found:"+resource).getBytes();
				type = HandlerAbstract.getContentTypeHeader_HTML();
			}
			pair = new Pair<byte[],byte[]>(type,ret);
			
		} catch (IOException e) {
			getLog().error("Problem serving up content:"+restFunction+"\n"+e);
		}finally{
			try {
				if(ios != null){
					ios.close();
					ios = null;
				}
			} catch (IOException e) {
			}
		}
		
		
		return pair;
	}
}


