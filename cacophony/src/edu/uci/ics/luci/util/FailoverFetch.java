package edu.uci.ics.luci.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.util.Pair;
import com.quub.webserver.WebUtil;

import edu.uci.ics.luci.cacophony.directory.Directory;

public class FailoverFetch {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(FailoverFetch.class);
		}
		return log;
	}
	
	/* Number of times the URL has failed and the URL like "localhost:1776" */
	Object dspLock = new Object();
	List<Pair<Long,String>> directoryServerPool = null;
	
	FailoverFetch(){
		directoryServerPool = new ArrayList<Pair<Long,String>>();
	}
	
	public FailoverFetch(String seedServer){
		this();
		fetchDirectoryList(seedServer);
	}
	
	
	
	public void fetchDirectoryList(String directorySeed) {
		String responseString = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Globals.getGlobals().getVersion());

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
					if(!Globals.getGlobals().getVersion().equals(response.getString("version"))){
						getLog().fatal("Wrong version on directory list:"+responseString);
					}
					else{
						if(response.getJSONObject("servers").length() == 0){
							getLog().fatal("No servers in directory directory list:"+responseString);
						}
						else{
			    			JSONObject servers = response.getJSONObject("servers");
			    			for(Iterator<String> k = servers.keys();k.hasNext();){
			    				try{
			    					JSONObject server = servers.getJSONObject((String) k.next());
			    					String namespace = server.getString("namespace");
			    					String targetNamespace = Directory.getInstance().getDirectoryNamespace();
			    					if(targetNamespace.equals(namespace)){
			    						JSONArray urlsForServer = server.getJSONArray("access_routes");
			    						for(int i =0 ; i < urlsForServer.length(); i++){
			    							long p = urlsForServer.getJSONObject(i).getLong("priority_order");
			    							String url = urlsForServer.getJSONObject(i).getString("url");
			    							Pair<Long, String> pair = new Pair<Long, String>(p,url);
			    							synchronized(dspLock){
			    								directoryServerPool.add(pair);
			    							}
			    						}
			    					}
			    				} catch (JSONException e) {
			    					getLog().debug("Something is missing from directory server JSON\n"+e);
			    				}
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
		
		synchronized(dspLock){
			Collections.shuffle(directoryServerPool);
			Collections.sort(directoryServerPool);
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
		List<String> servers = new ArrayList<String>();
		
		synchronized(dspLock){
			for(Pair<Long, String> p:directoryServerPool){
				servers.add(p.getSecond());
			}
		}
		
		while(servers.size()>0){
			String s = servers.remove(0);
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

	private void incrementFailCount(String s) {
		synchronized(dspLock){
			for(int i = 0; i < directoryServerPool.size(); i++){
				if(directoryServerPool.get(i).getSecond().equals(s)){
					Pair<Long, String> p = directoryServerPool.remove(i);
					p.setFirst(p.getFirst()+1);
					directoryServerPool.add(i,p);
				}
			}
			Collections.sort(directoryServerPool);
		}
		
	}

}
