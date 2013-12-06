package edu.uci.ics.luci.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.WebUtil;

public class FailoverFetch implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -318204566899644128L;
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
	

	/**
	 * This is a convenience function to load up a list of directory servers for initializing a FailoverFetch object 
	 * @param directorySeed, a directory server name to query, like www.ics.uci.luci
	 * @param namespace, the namespace for that directory
	 * @return a map of urls(String) and priorities (Long)
	 */
	static public Map<String,Long> fetchDirectoryList(String directorySeed,String namespace) {
		HashMap<String, Long> ret = new HashMap<String,Long>();
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
			response = (JSONObject) JSONValue.parse(responseString);
		}
		catch(ClassCastException e){
			getLog().error("Directory List did not return a JSONObject:\n"+responseString);
		}
		
		if((response == null) || (!response.get("error").equals("false"))){
			getLog().fatal("Couldn't get directory list:"+responseString);
		}
		else{
			JSONObject servers = null;
			try{
				servers = (JSONObject)response.get("servers");
			}
			catch(ClassCastException e){
				getLog().error("server list is not a JSONObject:\n"+response);
			}
			
			if(servers.size() == 0){
				getLog().fatal("No servers in directory directory list:"+responseString);
			}
			else{
    			for(Entry<String, Object> entry:servers.entrySet()){
    				
    				JSONObject server = null;
    				try{
		    			server = (JSONObject) entry.getValue();
    				}
    				catch(ClassCastException e){
    					getLog().error("server is not a JSONObject:\n"+servers);
    				}
    				
    				if(server != null){
    					JSONArray urlsForServer = null;
    					
    					try{
    						urlsForServer = (JSONArray) server.get("access_routes");
    					}
    					catch(ClassCastException e){
    						getLog().error("access_routes is not a JSONObject:\n"+server);
    					}
    					
    					if(urlsForServer != null){
    						for(int i =0 ; i < urlsForServer.size(); i++){
    							JSONObject url = null;
    							try{
    								url = (JSONObject)urlsForServer.get(i);
    							}
    							catch(ClassCastException e){
    								getLog().error("url is not a JSONObject:\n"+urlsForServer);
    							}
    							
    							if(url != null){
    								Long p = null;
    								try{
    									p = Long.parseLong((String)url.get("priority_order"));
    								}
    								catch(ClassCastException e){
    									getLog().error("priority_order is not a String:\n"+url);
    								}
    								catch(NumberFormatException e){
    									getLog().error("priority_order is not a Long:\n"+url);
    								}
    								
    								if(p != null){
    									String actualUrl = null;
    									try{
    										actualUrl = (String) url.get("url");
    									}
    									catch(ClassCastException e){
    										getLog().error("actual URL is not a String:\n"+url);
    									}
    									
    									if(actualUrl != null){
   											/* Make sure each url is only in the list once with the lowest priority */
   											if(ret.containsKey(actualUrl)){
   												Long count = ret.get(actualUrl);
   												if(count > p){
   													ret.put(actualUrl, p);
   												}
   											}
   											else{
   												ret.put(actualUrl,p);
    										}
    									}
    								}
    							}
    						}
    					}
	    			}
				}
			}
		}
		return(ret);
	}
	
	/* Number of times the URL has failed and the URL like "localhost:1776" */
	transient Object urlPoolLock = new Object();
	private TreeSet<Pair<Long,String>> urlPool = null;
	
	FailoverFetch(){
		urlPool = new TreeSet<Pair<Long,String>>();
	}
	
	
	/**
	 * 
	 * @param urlPool, a set of urls (String) mapped to a priority (Long).  The lower the priority the earlier the url is tried.
	 *  If a url fails it's priority is incremented by one each time and the priorities are reordered. 
	 */
	public FailoverFetch(Map<String,Long> urlPool){
		this();
		resetUrlPool(urlPool);
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
		TreeSet<Pair<Long, String>> servers = new TreeSet<Pair<Long,String>>();
		
		synchronized(urlPoolLock){
			servers.addAll(urlPool);
		}
		
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
	
	public JSONObject fetchJSONObject(String path, boolean authenticate, Map<String, String> vars, int timeOutMilliSecs) throws  MalformedURLException, IOException 
	{
		JSONObject ret = null;
		TreeSet<Pair<Long, String>> servers = new TreeSet<Pair<Long,String>>();
		
		synchronized(urlPoolLock){
			servers.addAll(urlPool);
		}
		
		
		while(servers.size() > 0){
			String s = servers.pollFirst().getSecond();
			try{
				String responseString = WebUtil.fetchWebPage("http://"+s+path, authenticate, vars, timeOutMilliSecs);
				if(responseString != null){
					try{
						ret = (JSONObject) JSONValue.parse(responseString);
						break;
					}
					catch(ClassCastException e){
						getLog().info("response is not a JSONObject:\n"+responseString);
						if(servers.size() == 0){
							throw new IOException(e);
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

	protected void resetUrlPool(Map<String,Long> urlMap) {
		
		/* Get a set ordered by success/priority */
		synchronized(urlPoolLock){
			
			List<Entry<String, Long>> shuffler = new ArrayList<Entry<String,Long>>();
			
			for(Entry<String, Long> e: urlMap.entrySet()){
				shuffler.add(e);
			}
			/* Randomly choose among equal priorities */
			Collections.shuffle(shuffler); 
			
			for(Entry<String, Long> p:shuffler){
				urlPool.add(new Pair<Long,String>(p.getValue(),p.getKey()));
			}
		}
	}
	
	public TreeSet<Pair<Long,String>> getUrlPoolCopy(){
		TreeSet<Pair<Long,String>> ret = null;
		synchronized(urlPoolLock){
			ret = new TreeSet<Pair<Long,String>>(urlPool);
		}
		return(ret);
	}

	private void incrementFailCount(String s) {
		
		boolean found = false;
		synchronized(urlPoolLock){
			TreeSet<Pair<Long, String>> toDelete = new TreeSet<Pair<Long,String>>();
			TreeSet<Pair<Long, String>> toAdd = new TreeSet<Pair<Long,String>>();
			for(Pair<Long, String> p:urlPool){
				if(p.getSecond().equals(s)){
					Long x = p.getFirst();
					toDelete.add(p);
					if(!found){
						toAdd.add(new Pair<Long,String>(x+1,s));
						found = true;
					}
				}
			}
			for(Pair<Long, String> p:toDelete){
				urlPool.remove(p);
			}
			for(Pair<Long,String> p:toAdd){
				urlPool.add(p);
			}
			if(!found){
				urlPool.add(new Pair<Long,String>(1L,s));
			}
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        setLog();
        urlPoolLock = new Object();
    }

}
