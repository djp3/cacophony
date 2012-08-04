package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quub.Globals;
import com.quub.util.Pair;

import edu.uci.ics.luci.util.FailoverFetch;

public class CNode {
	

	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNode.class);
		}
		return log;
	}

	private FailoverFetch failoverFetch;
	private Boolean lastHeartBeatSuccessful = null;
	private Object lastHeartBeatSuccessfulLock = null;
	private String nodeId;
	private String config;
	private CNodePool myPool;
	
	public final static long ONE_SECOND = 1000L;
	public final static long ONE_MINUTE = 60 * ONE_SECOND;
	public final static long FIVE_MINUTES  = 5 * ONE_MINUTE;
	public final static long ONE_HOUR = 60 * ONE_MINUTE;
	public final static long ONE_DAY = 24 * ONE_HOUR;
	
	public Timer heartbeat;

	public CNode(FailoverFetch failoverFetch, CNodePool cNPool) {
		this.failoverFetch = failoverFetch;
		this.myPool = cNPool;
		this.lastHeartBeatSuccessful = false;
		this.lastHeartBeatSuccessfulLock = new Object();
	}

	public void getANewConfiguration() {
		
		String responseString = null;
		try {
			
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Globals.getGlobals().getVersion());

			responseString = failoverFetch.fetchWebPage("/node_assignment", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}
		
		System.out.println(responseString);

		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			try {
				if(response.getString("error").equals("false")){
					if(response.getString("version").equals(Globals.getGlobals().getVersion())){
						this.nodeId = response.getString("node_id");
						this.config = response.getString("node_configuration");
					}
					
				}
			} catch (JSONException e) {
				getLog().error("Problem getting configuration:"+e);
			}
		} catch (JSONException e) {
			getLog().error("Problem getting configuration:"+e);
		}
	}
	
	/**
	 * 
	 * @param delay How long to wait before first heartbeat goes out in milliseconds. Default is 0
	 * @param period How often to send a heartbeat in milliseconds. Default is 5 minutes
	 * @param guid Some canonical name for the directory, like "cloud2". Default is "Unknown Directory"
	 * @param urls A list of URLs with which to reference this directory with a number indicating preference order. Lower is more preferred.
	 *  Default is just what java thinks the host address is.
	 */
	public void startHeartbeat(Long delay,Long period,String guid, List<Pair<Long,String>> urls){
		
		if(delay == null){
			delay = 0L;
		}
		
		if(period == null){
			period = FIVE_MINUTES;
		}
		
		if(guid == null){
			guid = "Unknown CNode";
		}
		
		if(urls == null){
			urls = new ArrayList<Pair<Long,String>>();
		}
		
		final String localNamespace = myPool.getNamespace();
		if(localNamespace == null){
			getLog().error("Set the namespace before starting the heartbeat");
		}
		
		if(urls.size() == 0){
			String url = null;
			try {
				url = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				url ="127.0.0.1";
			}
			urls.add(new Pair<Long,String>(0L,url));
		}
		
		
	   	final JSONObject localData = new JSONObject();
	   	try {
			localData.put("guid",guid);
			localData.put("node_id",this.nodeId);
			localData.put("namespace", localNamespace);
			JSONArray servers = new JSONArray();
			for(Pair<Long,String> x:urls){
				JSONObject bar = new JSONObject();
				bar.put("priority_order",x.getFirst());
				bar.put("url",x.getSecond());
				servers.put(bar);
			}
			localData.put("access_routes", servers);
	   	} catch (JSONException e) {
	   		getLog().fatal("Something is wrong with JSON:"+localNamespace+"\n"+e);
	   	}
		
		/* If we already started a heartbeat cancel it and restart */
		if(heartbeat != null){
			heartbeat.cancel();
		}
		
		try {
			getLog().error("Starting a CNode -> Directory heartbeat for: "+localData.toString(1));
		} catch (JSONException e1) {
		}
		
		/*Set up the heartbeat to go every 5 minutes;*/
		 heartbeat = new Timer(true);
		 heartbeat.scheduleAtFixedRate(
			    new TimerTask(){
			    	
					@Override
			    	public void run(){
						
						synchronized(lastHeartBeatSuccessfulLock){
							String responseString = null;
							/* Update this nodes heartbeat */
							String now = Long.toString(System.currentTimeMillis());
							try {
								localData.put("heartbeat",now);
							} catch (JSONException e) {
								getLog().fatal("Something is wrong with JSON:"+now+"\n"+e);
							}
						
							try {
								HashMap<String, String> params = new HashMap<String, String>();
								params.put("version", Globals.getGlobals().getVersion());
								params.put("json_data", localData.toString());
								responseString = failoverFetch.fetchWebPage("/node_checkin", false, params, 30 * 1000);
							} catch (MalformedURLException e) {
								getLog().warn("Bad URL when the CNode tried to check in:"+e);
								lastHeartBeatSuccessful = false;
							} catch (IOException e) {
								getLog().warn("IO Exception when the CNode tried to check in:"+e);
								lastHeartBeatSuccessful = false;
							}
							
							if(responseString == null){
								lastHeartBeatSuccessful = false;
							}
							else{
								JSONObject response = null;
								try {
									response = new JSONObject(responseString);
									try {
										assertEquals("false",response.getString("error"));
										lastHeartBeatSuccessful = true;
									} catch (JSONException e) {
										getLog().fatal("Something is wrong with JSON:"+responseString+"\n"+e);
										lastHeartBeatSuccessful = false;
									}
								} catch (JSONException e) {
									getLog().fatal("Something is wrong with JSON:"+responseString+"\n"+e);
									lastHeartBeatSuccessful = false;
								}
							}
						
							lastHeartBeatSuccessfulLock.notifyAll();
						}
					}
					}, delay, period);
		 
		 synchronized(lastHeartBeatSuccessfulLock){
			 while(lastHeartBeatSuccessful == false){
				 try {
					lastHeartBeatSuccessfulLock.wait();
				} catch (InterruptedException e) {
				}
			 }
		 }
		 
	}

	public void launch() {
		startHeartbeat(null,null,null,null);
	}

}
