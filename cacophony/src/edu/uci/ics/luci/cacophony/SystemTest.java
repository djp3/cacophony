package edu.uci.ics.luci.cacophony;

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
import com.quub.webserver.WebUtil;
import com.quub.webserver.handlers.HandlerFileServer;

import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.api.HandlerShutdown;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryNamespace;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryServers;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeAssignment;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeCheckin;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeList;
import edu.uci.ics.luci.cacophony.api.node.HandlerCNodePrediction;
import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodePool;
import edu.uci.ics.luci.util.PopUpWindow;

public class SystemTest {
	
	private static final String namespace = "systemtest.waitscout.com";
	
	private static final int portDirectoryA = 9020;
	private static final int portDirectoryB = 9021;
	private static final int portNodePoolA = 9022;
	private static final int portNodePoolB = 9023;
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Globals.getGlobals().setQuitting(true);
		Globals.setGlobals(null);
	}


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	private WebServer startAWebServerForDirectory(int port,Directory d) {
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
			requestHandlerRegistry.put("servers",new HandlerDirectoryServers(d));
			requestHandlerRegistry.put("nodes",new HandlerNodeList(d));
			requestHandlerRegistry.put("namespace",new HandlerDirectoryNamespace(d));
			requestHandlerRegistry.put("node_assignment",new HandlerNodeAssignment(d));
			requestHandlerRegistry.put("node_checkin",new HandlerNodeCheckin(d));
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/www/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws .start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		return ws;
	}
	
	private WebServer startAWebServerForNode(int port,CNode c) {
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
			requestHandlerRegistry.put("predict",new HandlerCNodePrediction(c));
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/wwwNode/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		return ws;
	}
	

	@Test
	public void testRunServersIndefinitely() {
		
		Globals.setGlobals(new GlobalsTest());
		
		/* Get two directories set up and running */
		
		/* Pick a guid */
		String guidA = "DirectoryServerA:"+this.getClass().getCanonicalName();
		String guidB = "DirectoryServerB:"+this.getClass().getCanonicalName();
		
		
		Directory directoryA = new Directory();
		Directory directoryB = new Directory();
		Globals.getGlobals().addQuittables(directoryA);
		Globals.getGlobals().addQuittables(directoryB);
		
		/* Initialize 2 directories */
		String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
		XMLPropertiesConfiguration config = null;
		try {
			config = new XMLPropertiesConfiguration(configFileName);
			directoryA.initializeDirectory(config);
			directoryB.initializeDirectory(config);
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName);
		}
		
			
		/* Set namespaces, only necessary if you want to override the config file */
		directoryA.setDirectoryNamespace(namespace);
		directoryB.setDirectoryNamespace(namespace);
		
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
			
		/* Add a real URL and a dummy URL */
		Pair<Long, String> pathsToDirectoryA = new Pair<Long,String>(0L,url+":"+portDirectoryA);
		List<Pair<Long, String>> urlsA = new ArrayList<Pair<Long,String>>();
		urlsA.add(pathsToDirectoryA);
		pathsToDirectoryA = new Pair<Long,String>(1L,"foobarA.com");
		urlsA.add(pathsToDirectoryA);
		
		Pair<Long, String> pathsToDirectoryB = new Pair<Long,String>(0L,url+":"+portDirectoryB);
		List<Pair<Long, String>> urlsB = new ArrayList<Pair<Long,String>>();
		urlsB.add(pathsToDirectoryB);
		pathsToDirectoryB = new Pair<Long,String>(1L,"foobarB.com");
		urlsB.add(pathsToDirectoryB);
		
		/* Start reporting heartbeats */
		directoryA.startHeartbeat(guidA,urlsA);
		directoryB.startHeartbeat(guidB,urlsB);
		
		/* Start cleaning the meta C Node List */
		directoryA.startMetaCNodeListCleaner();
		directoryB.startMetaCNodeListCleaner();
		
		/* Make 2 webservers for directories */
		WebServer webserverDirectoryA = startAWebServerForDirectory(portDirectoryA,directoryA);
		WebServer webserverDirectoryB = startAWebServerForDirectory(portDirectoryB,directoryB);
		Globals.getGlobals().addQuittables(webserverDirectoryA);
		Globals.getGlobals().addQuittables(webserverDirectoryB);
		
		/* Check to see response is what is expected */
		/* Check first directory */
		checkDirectory(guidA, directoryA, url,portDirectoryA,"foobarA.com");
		
		
		/* Check to see response is what is expected */
		/* Check second directory */
		checkDirectory(guidB, directoryB, url,portDirectoryB,"foobarB.com");
		
		
		
		
		/*Start the first one */
		List<Pair<Long,String>> baseUrls = new ArrayList<Pair<Long,String>>();
		url = null;
		try {
			url = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			url ="127.0.0.1";
		}
		baseUrls.add(new Pair<Long,String>(0L,url+":"+portNodePoolA));
		
		
		configFileName= "src/edu/uci/ics/luci/cacophony/SystemTest.cacophony.c_node_poolA.properties";
		try {
			config = new XMLPropertiesConfiguration(configFileName);
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName);
		}
		CNodePool cNPoolA = new CNodePool().launchCNodePool(config,null,null,baseUrls);
		assertTrue(cNPoolA != null);
		Globals.getGlobals().addQuittables(cNPoolA);
		
		
		/*Start the second one */
		baseUrls = new ArrayList<Pair<Long,String>>();
		url = null;
		try {
			url = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			url ="127.0.0.1";
		}
		baseUrls.add(new Pair<Long,String>(0L,url+":"+portNodePoolB));
		
		
		configFileName= "src/edu/uci/ics/luci/cacophony/SystemTest.cacophony.c_node_poolB.properties";
		try {
			config = new XMLPropertiesConfiguration(configFileName);
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName);
		}
		CNodePool cNPoolB = new CNodePool().launchCNodePool(config,null,null,baseUrls);
		assertTrue(cNPoolB != null);
		Globals.getGlobals().addQuittables(cNPoolB);
		
		/* Make 2 webservers for CNodes */
		WebServer webserverNodeA = startAWebServerForNode(portNodePoolA,cNPoolA.getPool().get(0));
		WebServer webserverNodeB = startAWebServerForNode(portNodePoolB,cNPoolB.getPool().get(0));
		Globals.getGlobals().addQuittables(webserverNodeA);
		Globals.getGlobals().addQuittables(webserverNodeB);
		
		
		/* Now test that everything is as expected */
		
		
		new PopUpWindow("Close this window to shut down the system: "+this.getClass().getCanonicalName());
	}

	protected void checkDirectory(String guid, Directory directory, String url,int port,String badURL) {
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version",CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace",namespace);

			responseString = WebUtil.fetchWebPage("http://localhost:" + port + "/servers", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL");
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception");
		}
		
		//System.out.println(responseString);

		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			//System.out.println(response.toString(5));
			try {
				if(response.getString("error").equals("true")){
					fail(response.getString("errors"));
				}
				assertEquals("false",response.getString("error"));
			
				assertTrue(response.getJSONObject("servers").length() > 0);
				
				Long heartbeat = response.getJSONObject("servers").getJSONObject(guid).getLong("heartbeat");
				assertTrue(System.currentTimeMillis() - heartbeat < Directory.FIVE_MINUTES);
				
				String namespace = response.getJSONObject("servers").getJSONObject(guid).getString("namespace");
				assertEquals(directory.getDirectoryNamespace(),namespace);
   			
				JSONArray servers = response.getJSONObject("servers").getJSONObject(guid).getJSONArray("access_routes");
				assertEquals(2,servers.length());
				for(int i =0 ; i < servers.length(); i++){
					if(servers.getJSONObject(i).getLong("priority_order") == 0){
						assertEquals(url+":"+port,servers.getJSONObject(i).getString("url"));
					}
					else{
						assertEquals(badURL,servers.getJSONObject(i).getString("url"));
					}
				}
			
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
	}


}
