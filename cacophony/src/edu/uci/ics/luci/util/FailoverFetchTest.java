package edu.uci.ics.luci.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quub.Globals;
import com.quub.GlobalsTest;
import com.quub.util.Pair;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher;
import com.quub.webserver.WebServer;
import com.quub.webserver.handlers.HandlerFileServer;

import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.api.HandlerShutdown;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryNamespace;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryServers;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeAssignment;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeCheckin;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeList;
import edu.uci.ics.luci.cacophony.directory.Directory;

public class FailoverFetchTest {
	

	private static int testPort = 9020;
	private static synchronized int testPortPlusPlus(){
		int x = testPort;
		testPort++;
		return(x);
	}


	@BeforeClass
	public static void setUpClass() throws Exception {
		while(Globals.getGlobals() != null){
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
			}
		}
		Globals.setGlobals(new GlobalsTest());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		Globals.getGlobals().setQuitting(true);
		Globals.setGlobals(null);
	}

	private int workingPort;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSorting(){
		
		FailoverFetch f = new FailoverFetch();
		
		f.directoryServerPool.put("localhost:1776",0L);
		f.directoryServerPool.put("localhost:1776",0L);
		f.directoryServerPool.put("localhost:1777",1L);
		f.directoryServerPool.put("localhost:1778",1L);
		f.directoryServerPool.put("localhost:1779",2L);
		f.directoryServerPool.put("localhost:1780",3L);
		
		for(int i = 0 ;i < 100; i++){
			TreeSet<Pair<Long, String>> servers = f.orderDirectoryServers();
			assertTrue(servers.pollFirst().getSecond().contains("1776"));
			assertTrue(servers.pollLast().getSecond().contains("1780"));
		}
	}
	
	private WebServer startAWebServer(int port,Directory d) {
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
			requestHandlerRegistry.put("servers",new HandlerDirectoryServers(d));
			requestHandlerRegistry.put("nodes",new HandlerNodeList(d));
			requestHandlerRegistry.put("node_assignment",new HandlerNodeAssignment(d));
			requestHandlerRegistry.put("node_checkin",new HandlerNodeCheckin(d));
			requestHandlerRegistry.put("namespace",new HandlerDirectoryNamespace(d));
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/wwwNode/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		return(ws);
	}

	private Directory startADirectory(){
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		
		/* Load up the data */
		try {
			String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
			d.initializeDirectory(new XMLPropertiesConfiguration(configFileName));
		} catch (ConfigurationException e1) {
			fail("");
		}

		return(d);
	}
	
	
	@Test
	public void testFetchDirectoryList() {
		workingPort = testPortPlusPlus();
		Directory d = startADirectory();
		
		/* Pick a guid */
		String guid = "Test "+this.getClass().getCanonicalName();
		
		/* Figure out our url */
		String url = null;
		try {
			url = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			try {
				url = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
			}
		}
			
		/* Add a bad URL*/
		Pair<Long, String> p = new Pair<Long,String>(0L,url+"foo:"+(workingPort+1));
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		urls.add(p);
		
		/* Add real URL multiple times */
		p = new Pair<Long,String>(1L,url+":"+workingPort);
		urls.add(p);
		p = new Pair<Long,String>(2L,url+":"+workingPort);
		urls.add(p);
		p = new Pair<Long,String>(3L,url+":"+workingPort);
		urls.add(p);
		p = new Pair<Long,String>(4L,url+":"+workingPort);
		urls.add(p);
		
		/* Clean up junk */
		d.removeAllServers();
		
		/* Start reporting heartbeats */
		String namespace = "testNamespace@"+System.currentTimeMillis();
		d.setDirectoryNamespace(namespace);
		d.startHeartbeat(guid,urls);
		
		startAWebServer(workingPort,d);
		
		
		FailoverFetch f = new FailoverFetch("localhost:"+workingPort,namespace);
		assertEquals(2,f.directoryServerPool.size());  //Should only be keeping uniques
		
		/* Test to see if the code returns JSON */
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", namespace);

			JSONObject response = f.fetchJSONObject("/servers", false, params, 30 * 1000);
		
			//System.out.println(response.toString(5));
			try {
				if(response.getString("error").equals("true")){
					fail(response.getString("errors"));
				}
				assertEquals("false",response.getString("error"));
			
				assertTrue(response.getJSONObject("servers").length() > 0);
			
				Long heartbeat = response.getJSONObject("servers").getJSONObject(guid).getLong("heartbeat");
				assertTrue(System.currentTimeMillis() - heartbeat < Directory.FIVE_MINUTES);
			
				namespace = response.getJSONObject("servers").getJSONObject(guid).getString("namespace");
				assertEquals(d.getDirectoryNamespace(),namespace);
			
				JSONArray servers = response.getJSONObject("servers").getJSONObject(guid).getJSONArray("access_routes");
				assertEquals(5,servers.length());
				for(int i =0 ; i < servers.length(); i++){
					assertTrue(servers.getJSONObject(i).getString("url").contains(url));
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			fail("Bad URL "+e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IO Exception"+e1);
		} catch (JSONException e) {
			e.printStackTrace();
			fail("JSON Exception"+e);
		}
	}

}
