package edu.uci.ics.luci.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.util.Pair;
import com.quub.webserver.WebUtil;

import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;

public class FailoverFetch implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5315766361652027469L;
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(FailoverFetch.class);
		}
		return log;
	}
	
	public static void setLog(){
		log = null;
	}
	
	/* Number of times the URL has failed and the URL like "localhost:1776" */
	transient Object dspLock = new Object();
	Map<String,Long> directoryServerPool = null;
	
	FailoverFetch(){
		directoryServerPool = new HashMap<String,Long>();
	}
	
	public FailoverFetch(String seedServer,String namespace){
		this();
		fetchDirectoryList(seedServer,namespace);
	}
	
	
	
	public void fetchDirectoryList(String directorySeed,String namespace) {
		String responseString = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace",namespace); 

			responseString = WebUtil.fetchWebPage("http://"+directorySeed+"/servers", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			getLog().error("Bad URL"+e);
		} catch (IOException e) {
			getLog().error("IO Exception"+e);
		}
		
		JSONObject response = null;
		try{
			response = new JSONObject(responseString);
			try {
				if(!response.getString("error").equals("false")){
					getLog().fatal("Couldn't get directory list:"+responseString);
				}
				else{
					if(response.getJSONObject("servers").length() == 0){
						getLog().fatal("No servers in directory directory list:"+responseString);
					}
					else{
		    			JSONObject servers = response.getJSONObject("servers");
		    			for(Iterator<?> k = servers.keys();k.hasNext();){
		    				try{
		    					JSONObject server = servers.getJSONObject((String) k.next());
	    						JSONArray urlsForServer = server.getJSONArray("access_routes");
	    						for(int i =0 ; i < urlsForServer.length(); i++){
	    							long p = urlsForServer.getJSONObject(i).getLong("priority_order");
	    							String url = urlsForServer.getJSONObject(i).getString("url");
	    							synchronized(dspLock){
	    								/* Make sure each url is only in the list once with the lowest priority */
	    								if(directoryServerPool.containsKey(url)){
	    									Long count = directoryServerPool.get(url);
	    									if(count > p){
	    										directoryServerPool.put(url, p);
	    									}
	    								}
	    								else{
	    									directoryServerPool.put(url,p);
	    								}
	    							}
		    					}
		    				} catch (JSONException e) {
		    					getLog().debug("Something is missing from directory server JSON\n"+e);
		    				}
		    			}
	    				
	    			}
				}
			} catch (JSONException e) {
				getLog().error(e);
			}
		} catch (JSONException e) {
			getLog().error(e);
		}
	}
	
	/**
	 * Fetch a web page's contents. Note that this will change all line breaks
	 * into system line breaks!  
	 *
	 * @param path
	 *            The web-page (or file) to fetch. This method can handle
	 *            permanent redirects, but not javascript or meta redirects. The path is everything after the ip address
	 *            including the "/".  So the path for http:///www.cnn.com/media/index.html should be "/media/index.html"
	 *            The server locations are taken from the pool
	 * @param authenticate
	 * 	 		  True if basic authentication should be used.  In which case vars needs to have
	 *            an entry for "username" and "password".           
	 * @param vars
	 * 			  A Map of params to be sent on the uri. "username" and "password" is removed before
	 *            calling the uri.           
	 * @param timeOutMilliSecs
	 *            The read time out in milliseconds. Zero is not allowed. Note
	 *            that this is not the timeout for the method call, but only for
	 *            the connection. The method call may take longer.
	 * @return The full text of the web page
	 *
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	
	public String fetchWebPage(String path, boolean authenticate, Map<String, String> vars, int timeOutMilliSecs) throws  MalformedURLException, IOException
	{
		String responseString = null;
		TreeSet<Pair<Long, String>> servers = orderDirectoryServers();
		
		while(servers.size()>0){
			String s = servers.pollFirst().getSecond();
			try{
				responseString = WebUtil.fetchWebPage("http://"+s+path, authenticate, vars, timeOutMilliSecs);
				break;
			}
			catch(IOException e){
				incrementFailCount(s);
				if(servers.size() == 0){
					throw e;
				}
			}
		}
		return responseString;

	}
	
	/**
	 * Fetch a web page's contents. If the webpage errors out or fails to parse JSON it's considered an error. 
	 *
	 * @param path
	 *            The URL to fetch. This method can handle
	 *            permanent redirects, but not javascript or meta redirects. The path is everything after the ip address
	 *            including the "/".  So the path for http:///www.cnn.com/media/index.html should be "/media/index.html"
	 *            The server locations are taken from the pool
	 * @param authenticate
	 * 	 		  True if basic authentication should be used.  In which case vars needs to have
	 *            an entry for "username" and "password".           
	 * @param vars
	 * 			  A Map of params to be sent on the uri. "username" and "password" is removed before
	 *            calling the uri.           
	 * @param timeOutMilliSecs
	 *            The read time out in milliseconds. Zero is not allowed. Note
	 *            that this is not the timeout for the method call, but only for
	 *            the connection. The method call may take longer.
	 * @return A JSON object from the URL
	 *
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws JSONException 
	 */
	
	public JSONObject fetchJSONObject(String path, boolean authenticate, Map<String, String> vars, int timeOutMilliSecs) throws  MalformedURLException, IOException, JSONException
	{
		JSONObject ret = null;
		TreeSet<Pair<Long, String>> servers = orderDirectoryServers();
		
		while(servers.size() > 0){
			String s = servers.pollFirst().getSecond();
			try{
				String responseString = WebUtil.fetchWebPage("http://"+s+path, authenticate, vars, timeOutMilliSecs);
				if(responseString != null){
					try{
						ret = new JSONObject(responseString);
						break;
					} catch (JSONException e) {
						incrementFailCount(s);
						if(servers.size() == 0){
							throw e;
						}
					}
				}
			}
			catch(IOException e){
				incrementFailCount(s);
				if(servers.size() == 0){
					throw e;
				}
			}
		}
		return ret;
	}

	protected TreeSet<Pair<Long, String>> orderDirectoryServers() {
		TreeSet<Pair<Long,String>> servers = new TreeSet<Pair<Long,String>>();
		
		/* Get a set ordered by success/priority */
		synchronized(dspLock){
			List<Entry<String, Long>> shuffler = new ArrayList<Entry<String,Long>>();
			for(Entry<String, Long> e: directoryServerPool.entrySet()){
				shuffler.add(e);
			}
			/* Randomly choose among equal priorities */
			Collections.shuffle(shuffler); 
			for(Entry<String, Long> p:shuffler){
				servers.add(new Pair<Long,String>(p.getValue(),p.getKey()));
			}
		}
		return servers;
	}

	private void incrementFailCount(String s) {
		synchronized(dspLock){
			if(directoryServerPool.containsKey(s)){
				Long x = directoryServerPool.get(s);
				directoryServerPool.put(s,x+1);
			}
			else{
				directoryServerPool.put(s,1L);
			}
		}
		
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        setLog();
        dspLock = new Object();
    }

}
