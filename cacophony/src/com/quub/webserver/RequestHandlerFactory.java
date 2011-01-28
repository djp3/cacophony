package com.quub.webserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.quub.Globals;
import com.quub.database.QuubDBConnectionPool;
import com.quub.util.Pair;

public class RequestHandlerFactory{
	
	private Class<? extends RequestHandler> requestHandlerClass = null;
	
	public RequestHandlerFactory(Globals globals,Class<? extends RequestHandler> requestHandlerClass){
		setGlobals(globals);
		this.requestHandlerClass = requestHandlerClass;
	}
	
	private static Globals _globals = null;
	public Globals getGlobals() {
		return _globals;
	}
	
	public static void setGlobals(Globals g) {
		_globals = g;
	}

	private static transient volatile Logger log = null;
	public Logger getLog(){
		if(log == null){
			log = Logger.getLogger(RequestHandlerFactory.class);
		}
		return log;
	}
	
	
	public Runnable makeHandler(WebServer ws,
			AccessControl accessControl,
			ExecutorService threadExecutor,
			QuubDBConnectionPool connectionPool, Socket soc,
			Boolean testing){
		
		RequestHandler rh = null;
		
		try {
			rh = requestHandlerClass.newInstance();
			if(rh != null){
				rh.setGlobals(getGlobals());
				rh.setWebServer(ws);
				rh.setAccessControl(accessControl);
				rh.setThreadExecutor(threadExecutor);
				rh.setConnectionPool(connectionPool);
				rh.setSocket(soc);
				rh.setTesting(testing);
			}
		} catch (InstantiationException e) {
			getLog().log(Level.ERROR,"Unable to Instantate a Request Handler",e);
		} catch (IllegalAccessException e) {
			getLog().log(Level.ERROR,"",e);
		}
		
		return(rh);
	}

	
	public Pair<String,Map<String,String>> parseSocketInput(Socket socket){
		String command = "";
		Map<String, String> parameters = new HashMap<String,String>();
			
		byte[] readBytes = new byte[5120];

		BufferedInputStream bis = null;
		int nBytes=0;
		
		try {
			bis = new BufferedInputStream(socket.getInputStream());
			nBytes = bis.read(readBytes, 0, 5120);
		} catch (IOException e) {
			getLog().log(Level.ERROR,"Unable to get input from client",e);
		}

		String request = new String(readBytes, 0, nBytes);
	
		getLog().debug("Raw data from first transmission of client = \n" + request);

		/* figure out the HTTP method */
		boolean getMethod = false;
		boolean postMethod = false;
		
		int indexGET = request.indexOf("GET");
		int indexPOST = -1;
		
		if( (indexGET != -1) && (indexGET < request.indexOf("HTTP",indexGET)) ){
			getLog().debug("GET Method Recognized");
			getMethod = true;
		}
		else{
			indexPOST = request.indexOf("POST");
			if((indexPOST != -1)&&(indexPOST < request.indexOf("HTTP",indexPOST))){
				getLog().debug("POST Method Recognized");
				postMethod = true;
			}
		}
		
		if(!getMethod && ! postMethod){
			getLog().error("Unhandled HTTP method");
		}
		else{
			getLog().debug("Raw data, unparsed parameters=\n" + request);
			/* Get the command (example "queryForPresence" */
			/* http://ec2-75-101-176-104.compute-1.amazonaws.com/queryForPresence?user_id=2*/
			int start = request.indexOf("/");
			int end = request.indexOf("?",start);
			if(end == -1){
				end = request.indexOf(" ",start);
			}
			if(end == -1){
				end = request.indexOf("\r",start);
			}
			if(end == -1){
				end = request.indexOf("\r",start);
			}
			if(end == -1){
				end = request.length();
			}
			if((start >= 0) &&(end >=0)){
				command = request.substring(start+1, end).trim();
				getLog().debug("Parsed Command is:"+command);
			}
			
			/*Get the parameters (example user_id = 2)*/
			if(getMethod){
				end = request.indexOf("?",start);
				if(end >=0){
					request = request.substring(end+1);
				}
				else{
					request = "";
				}
			}
			else if(postMethod){
				/*In some cases we don't get the whole post, so check for the second piece */
				try {
					nBytes = bis.read(readBytes, 0, 5120);
					request = request + new String(readBytes, 0, nBytes);
				} catch (IOException e) {
					getLog().log(Level.ERROR,"Something went wrong with second read",e);
				}
				
				end = request.indexOf("\r\n\r\n");
				if(end >= 0){
					request = request.substring(end+4);
				}
			}
			request = request.trim();
				
			if(request.length()==0){
				getLog().info("No parameters found in raw data");
			}
			else{
				parameters = parseQueryString(request);
			}
		}
		return(new Pair<String,Map<String,String>>(command,parameters));
	}
	

	/*
     * Parse a name in the query string.
     */
    private String parseName(String s, StringBuffer sb) {
    	
    	sb.setLength(0);
    	for (int i = 0; i < s.length(); i++) {
    		char c = s.charAt(i); 
    		switch (c) {
    			case '+':
    				sb.append(' ');
    				break;
    			case '%':
    				try {
    					sb.append((char) Integer.parseInt(s.substring(i+1, i+3),  16));
    					i += 2;
    				} catch (NumberFormatException e) {
    					// XXX
    					// need to be more specific about illegal arg
    					throw new IllegalArgumentException();
    				} catch (StringIndexOutOfBoundsException e) {
    					String rest  = s.substring(i);
    					sb.append(rest);
    					if (rest.length()==2)
    						i++;
    				}
    				break;
    			default:
    				sb.append(c);
    			break;
    		}
    	}
    	return sb.toString();
    }


    /**
    *
    * Parses a query string passed from the client to the
    * server and builds a <code>Map</code> object
    * with key-value pairs. 
    * The query string should be in the form of a string
    * packaged by the GET or POST method, that is, it
    * should have key-value pairs in the form <i>key=value</i>,
    * with each pair separated from the next by a &amp; character.
    *
    * <p>A key can appear more than once in the query string
    * with different values. However, the key appears only once in 
    * the map, with its value being rewritten in the case of multiple values sent
    * by the query string.
    * 
    * <p>The keys and values in the map are stored in their
    * decoded form, so
    * any + characters are converted to spaces, and characters
    * sent in hexadecimal notation (like <i>%xx</i>) are
    * converted to ASCII characters.
    *
    * @param s		a string containing the query to be parsed
    *
    * @return		a <code>Map</code> object built
    * 			from the parsed key-value pairs
    *
    * @exception IllegalArgumentException	if the query string 
    *						is invalid
    *
    */

   public Map<String, String> parseQueryString(String s) {

	   if (s == null) {
		   throw new IllegalArgumentException("s is null");
	   }
	
	   Map<String,String> ht = new HashMap<String,String>();
	   StringBuffer sb = new StringBuffer();
	   StringTokenizer st = new StringTokenizer(s, "&");
	
	   while (st.hasMoreTokens()) {
		   String pair = st.nextToken();
		   int pos = pair.indexOf('=');
		   String key;
		   String value;
		   if (pos == -1) {
			   key = pair;
			   value = null;
		   }
		   else{
			   key = parseName(pair.substring(0, pos), sb);
			   value = parseName(pair.substring(pos+1, pair.length()), sb);
		   }
		   if (ht.containsKey(key)) {
			   getLog().warn("http request had repeated keys");
		   }
		   
		   ht.put(key, value);
	   }
	   return ht;
   }
   
   void cleanTime(Map<String,String>parameters, String key){
	   if(parameters.containsKey(key)){
			Long time = null;
			try{
				time = new Long(parameters.get(key));
				if(time > System.currentTimeMillis()/1000){
					getLog().error("Expecting the client to give me seconds, not milliseconds:"+time);
				}
				else{
					parameters.put(key, ""+(time*1000L));
				}
			}
			catch(NumberFormatException e){
				parameters.remove(key);
				getLog().warn("Removing invalid "+key+"string");
			}
		}
   }

}
