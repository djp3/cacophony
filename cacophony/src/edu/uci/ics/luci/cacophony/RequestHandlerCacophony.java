package edu.uci.ics.luci.cacophony;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.database.QuubDBConnectionPool;
import com.quub.webserver.AccessControl;
import com.quub.webserver.RequestHandler;
import com.quub.webserver.WebServer;

/**
 * This class receives one request from a REST call, processes it and returns some
 * content to the browser. 
 */
public class RequestHandlerCacophony extends RequestHandler{

	private static final byte[] EOL = {(byte)'\r', (byte)'\n' };
    private static final int HTTP_OK = 200;
    
    public static final String VERSION="1.1";
    
    private static int jobCounter = 0;

	private byte[] readBytes = null;
	
	private CacophonyGlobals globals = null;

	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(RequestHandlerCacophony.class);
		}
		return log;
	}
	

	@Override
	public CacophonyGlobals getGlobals() {
		return this.globals;
	}

	@Override
	public void setGlobals(Globals globals) {
		if(globals == null){
			this.globals = null;
		}
		else{
			if(globals instanceof CacophonyGlobals){
				this.globals = (CacophonyGlobals) globals;
			}
			else{
				getLog().fatal("Initializing with the wrong kind of globals:"+globals.getClass().getCanonicalName());
			}
		}
	}
	
	public RequestHandlerCacophony() {
		this(null,null,null,null,null);
	}
	
	public RequestHandlerCacophony(WebServer ws,AccessControl ac,QuubDBConnectionPool pool,Socket soc) {
		this(ws,ac,pool,soc,false);
	}
	
	public RequestHandlerCacophony(WebServer ws,AccessControl ac,Socket soc, Boolean testing){
		this(ws,ac,null,soc,testing);
	}
	
	public RequestHandlerCacophony(WebServer ws,AccessControl ac,QuubDBConnectionPool pool,Socket soc, Boolean testing){
		
		this.setWebServer(ws);
		this.setAccessControl(ac);
		this.setConnectionPool(pool);
		this.setSocket(soc);
		this.setTesting(testing);
		
		readBytes = new byte[5120];
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

   static public Map<String, String> parseQueryString(String s) {

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
	
	public void run() {
		
		String contentTypeHeader="Content-type:  application/json; charset=utf-8";
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		int nBytes = -1;
		byte[] outputBytes = null;
		
		/*Shuts down the system at the end of the method if true */
		boolean hardQuit = false;
		
		try {
			getLog().info("----------------------");
			String source = getSocket().getInetAddress().toString();
			if(getAccessControl().allowSource(source,true,false)){
				getLog().info("Request Handler #:"+(++jobCounter)+" handling request from " + source);

				bis = new BufferedInputStream(getSocket().getInputStream());
				nBytes = bis.read(readBytes, 0, 5120);

				String request = new String(readBytes, 0, nBytes);
				String requestParameters = new String(readBytes, 0, nBytes);
				
				getLog().debug("First Part of Full Client Request = \n" + request);

				/* figure out the HTTP method */
				boolean getMethod = false;
				boolean postMethod = false;
				int indexGET = request.indexOf("GET");
				int indexPOST = request.indexOf("POST");
				if((indexGET != -1)&&(indexGET < request.indexOf("HTTP",indexGET))){
					getMethod = true;
				}
				else if((indexPOST != -1)&&(indexPOST < request.indexOf("HTTP",indexPOST))){
					postMethod = true;
				}

				/* Capture the request */
				int start = request.indexOf("/");
				int end = request.indexOf("?",start);
				if(end == -1){
					end = request.indexOf(" ",start);
				}
				
				Map<String, String> parameters = null;
				if((start >= 0) &&(end >=0)){
					request = request.substring(start+1, end).trim();
					
					/*Grab the parameters */
					if(getMethod){
						getLog().info("URL Client Request (GET)= "+ request);
						start = requestParameters.indexOf("?");
						end = requestParameters.indexOf(" ",start);
						if((start >=0 ) && (end >=0)){
							requestParameters = requestParameters.substring(start+1,end).trim();
						}
						else{
							getLog().warn("No HTTP (GET) parameters from <"+source+"> url:<"+request+">");
							requestParameters="";
						}
						parameters = parseQueryString(requestParameters);
					}
					else if(postMethod){
						getLog().info("URL Client Request (POST)= "+ request);
						
						start = requestParameters.indexOf("\r\n\r\n");
						
						/*If we didn't get the whole post, try for the second piece */
						if(start >=0 ){
							requestParameters = requestParameters.substring(start+2).trim();
						}
						if(requestParameters.length() == 0){
							nBytes = bis.read(readBytes, 0, 5120);
							requestParameters = new String(readBytes, 0, nBytes);
							if(requestParameters.length()==0){
								getLog().warn("No HTTP (POST) parameters from <"+source+"> url:<"+request+">");
								requestParameters="";
							}
						}

						getLog().debug("requestParameters = "+ requestParameters);
						requestParameters = requestParameters.trim();
						parameters = parseQueryString(requestParameters);
					}
					else{
						getLog().warn("Unhandled HTTP method from <"+source+"> url:<"+request+">");
						requestParameters="";
					}
			
					if(parameters != null){
						/* This will write the parameters to the Java console */
						getLog().info("Parameters"+parameters.toString());
					}
				
			
					if(request.equals("version") || request.equals("")){
						outputBytes = replyWithVersion();
					}
					else if(request.equals("viz")){
						contentTypeHeader="Content-type:  text/html; charset=utf-8";
						outputBytes = Visualization.getWebsite(globals.getConfig(),getTesting());
					}
					else if(request.equals("config")){
						contentTypeHeader="Content-type:  text/plain; charset=utf-8";
						StringWriter w = new StringWriter();
						try {
							globals.getConfig().getLayout().save(w);
						} catch (ConfigurationException e) {
							getLog().error("Unable to write configuration to string."+e);
							w.append("Error:"+e);
						}
						outputBytes = w.toString().getBytes();
					}
					else if(request.equals("shutdown")){
						outputBytes = replyWithGoodbye();
						hardQuit = true;
					}
					else{
						JSONObject newParams = new JSONObject();
						JSONArray errors = new JSONArray();
						errors.put("Unknown web query \""+request+"\"");
						newParams.put("errors",errors);
						outputBytes  = newParams.toString().getBytes();
					}
				}
				else{
					outputBytes = replyWithVersion();
				}
				
				/* This sends things back to the browser */
				bos = new BufferedOutputStream(getSocket().getOutputStream());
				bos.write(("HTTP/1.0 "+ HTTP_OK+" OK").getBytes());
				bos.write(EOL);
				bos.write("Server: quubie:".getBytes());
				bos.write(EOL);
				bos.write(("Date: "+ (new Date())).getBytes());
				bos.write(EOL);
				bos.write(contentTypeHeader.getBytes());
				bos.write(EOL);
				bos.write(EOL);
				bos.write(outputBytes, 0, outputBytes.length);
				bos.flush();
			}
			else{
				getLog().warn("Server silently rejected request from " + source);
			}
		} catch (IOException e) {
			getLog().error(e.toString());
		} catch (JSONException e) {
			getLog().error(e.toString());
		} catch (RuntimeException e) {
			getLog().error(e.toString());
		} finally {
			try {
				if(bis != null){
					bis.close();
				}
			}
			catch (Exception e1) {
				getLog().error(e1.getMessage());
			}
			finally{
				try{
					if(bos != null){
						bos.close();
					}
				}
				catch (Exception e1) {
					getLog().error(e1.getMessage());
				}
				finally{
					try{
						if(getSocket() != null){
							getSocket().close();
						}
					}
					catch (Exception e1) {
						getLog().error(e1.getMessage());
					}
				}
			}
			
			if(hardQuit){
				this.globals.setQuitting(true);
			}
		}
	}




	private byte[] replyWithVersion() throws JSONException {
		byte[] outputBytes;
		
		JSONObject ret = new JSONObject();
		ret.put("version", RequestHandlerCacophony.VERSION);
		ret.put("errors", new JSONArray("[false]"));
		
		outputBytes = ret.toString().getBytes();
		getLog().info("Version is "+RequestHandlerCacophony.VERSION);
		return outputBytes;
	}
	
	
	private byte[] replyWithGoodbye() throws JSONException {
		byte[] outputBytes;
		
		JSONObject ret = new JSONObject();
		ret.put("Saying Goodbye", "true");
		ret.put("errors", new JSONArray("[false]"));
		
		outputBytes = ret.toString().getBytes();
		getLog().info("Handling shutdown");
		return outputBytes;
	}

	
	/**
     * Parse a name in the query string.
     */
	static private String parseName(String s, StringBuffer sb) {
    	
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



}

