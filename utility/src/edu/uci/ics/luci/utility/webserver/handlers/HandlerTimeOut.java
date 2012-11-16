package edu.uci.ics.luci.utility.webserver.handlers;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;


import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher.HTTPRequest;

public class HandlerTimeOut extends HandlerAbstract {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(HandlerTimeOut.class);
		}
		return log;
	}
	
	Thread timeOuter;
	
	public HandlerTimeOut(){
		super();
	}
	
	@Override
	public HandlerTimeOut copy() {
		return new HandlerTimeOut();
	}
	
	/**
	 * This should never return. 
	 * @param parameters a map of key and value that was passed through the REST request
	 * @return a pair where the first element is the content type and the bytes are the output bytes to send back
	 */
	@Override
	public Pair<byte[], byte[]> handle(InetAddress ip, HTTPRequest httpRequestType, Map<String, String> headers, String restFunction, Map<String, String> parameters) {
		Pair<byte[], byte[]> pair = null;
		
		timeOuter = new Thread(new Runnable(){
			public void run() {
				while(timeOuter.isAlive()){ //This is dumb, but it eliminates a FindBugs warning
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				
			}});
		timeOuter.setDaemon(true);
		timeOuter.start();
		
		while(timeOuter.isAlive()){
			try {
				timeOuter.join();
			} catch (InterruptedException e1) {
			}
		}
		
		pair = new Pair<byte[],byte[]>(HandlerAbstract.getContentTypeHeader_JSON(),wrapCallback(parameters,"").getBytes());
		return pair;
	}
}


