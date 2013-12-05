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

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
import edu.uci.ics.luci.cacophony.model.KyotoCabinet;
import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodePool;
import edu.uci.ics.luci.util.PopUpWindow;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.WebUtil;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;

public class SystemTest {
	
	private static final String namespace = "edu.uci.ics.luci.cacophony";
	
	private static final int portDirectoryA = 9020;
	private static final int portDirectoryB = 9021;
	private static final int portNodePoolA = 9022;
	private static final int portNodePoolB = 9023;
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		return ws;
	}
	
	
	private WebServer startAWebServerForPool(int port,CNodePool cnp) {
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
			requestHandlerRegistry.put("predict",new HandlerCNodePrediction(cnp));
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
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unable to initialize Directory");
		}
		
			
		/* Set namespaces, only necessary if you want to override the config file */
		directoryA.setDirectoryNamespace(namespace);
		directoryB.setDirectoryNamespace(namespace);
		
		/* Figure out our url */
		String url = null;
		try {
			if(System.getProperty("os.name").contains("Windows")){
				url = "127.0.0.1";
			}
			else{
				try {
					url = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					url = InetAddress.getLocalHost().getHostAddress();
				}
			}
		} catch (UnknownHostException e1) {
			url ="127.0.0.1";
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
		
		/* Clean up anything left over */
		directoryA.removeAllServers();
		directoryB.removeAllServers();
		
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
			if(System.getProperty("os.name").contains("Windows")){
				url = "127.0.0.1";
			}
			else{
				try {
					url = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					url = InetAddress.getLocalHost().getHostAddress();
				}
			}
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
		CNodePool cNPoolA = new CNodePool(new KyotoCabinet<String,CNode>("fileA")).launchCNodePool(config,null,null,baseUrls);
		assertTrue(cNPoolA != null);
		Globals.getGlobals().addQuittables(cNPoolA);
		while(cNPoolA.isUpdatingActive()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		
		/*Start the second one */
		baseUrls = new ArrayList<Pair<Long,String>>();
		url = null;
		try {
			if(System.getProperty("os.name").contains("Windows")){
				url = "127.0.0.1";
			}
			else{
				try {
					url = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					url = InetAddress.getLocalHost().getHostAddress();
				}
			}
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
		CNodePool cNPoolB = new CNodePool(new KyotoCabinet<String,CNode>("fileB")).launchCNodePool(config,null,null,baseUrls);
		assertTrue(cNPoolB != null);
		Globals.getGlobals().addQuittables(cNPoolB);
		
		/* Make 2 webservers for CNodes */
		WebServer webserverNodeA = startAWebServerForPool(portNodePoolA,cNPoolA);
		WebServer webserverNodeB = startAWebServerForPool(portNodePoolB,cNPoolB);
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
			response = (JSONObject) JSONValue.parse(responseString);
			//System.out.println(response.toString(5));
			try {
				if(((String)response.get("error")).equals("true")){
					fail(((String)response.get("errors")));
				}
				assertEquals("false",((String)response.get("error")));
			
				assertTrue(((JSONObject)response.get("servers")).size() > 0);
				
				Long heartbeat = Long.parseLong((String)((JSONObject)((JSONObject)response.get("servers")).get(guid)).get("heartbeat"));
				assertTrue(System.currentTimeMillis() - heartbeat < Directory.FIVE_MINUTES);
				
				String namespace = ((String)((JSONObject)((JSONObject)response.get("servers")).get(guid)).get("namespace"));
				assertEquals(directory.getDirectoryNamespace(),namespace);
   			
				JSONArray servers = ((JSONArray) ((JSONObject) ((JSONObject) response.get("servers")).get(guid)).get("access_routes"));
				assertEquals(2,servers.size());
				for(int i =0 ; i < servers.size(); i++){
					if(Long.parseLong((String)((JSONObject)servers.get(i)).get("priority_order")) == 0L){
						assertEquals(url+":"+port,((String) ((JSONObject) servers.get(i)).get("url")));
					}
					else{
						assertEquals(badURL,((String)((JSONObject)servers.get(i)).get("url")));
					}
				}
			
			} catch (ClassCastException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
	}


}
