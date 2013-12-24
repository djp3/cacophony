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

public class MilestoneTest {
	
	private static final String namespace = "edu.uci.ics.luci.cacophony.milestone";
	
	private static final int portDirectory = 9020;
	private static final int portNodePool = 9021;
	

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
		
		/* Get A directory set up and running */
		
		/* Pick a guid */
		String guid = "DirectoryServer:"+this.getClass().getCanonicalName();
		
		Directory directory = new Directory();
		
		Globals.getGlobals().addQuittables(directory);
		
		/* Initialize Directory */
		String configFileName="src/edu/uci/ics/luci/cacophony/MilestoneTest.cacophony.directory.properties";
		XMLPropertiesConfiguration config = null;
		try {
			config = new XMLPropertiesConfiguration(configFileName);
			directory.initializeDirectory(config);
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName+"\n"+e2);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unable to initialize Directory\n"+e);
		}
		
		/* Set namespaces, only necessary if you want to override the config file */
		directory.setDirectoryNamespace(namespace);
		
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
		
			
		/* Add a real URL where the directory can be found*/
		Pair<Long, String> pathsToDirectory = new Pair<Long,String>(0L,url+":"+portDirectory);
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		urls.add(pathsToDirectory);
		
		/* Clean up anything left over from previous incarnations of the config file */
		directory.removeAllServers();
		
		/* Start reporting heartbeats */
		directory.startHeartbeat(guid,urls);
		
		/* Start cleaning the meta C Node List */
		directory.startMetaCNodeListCleaner();
		
		/* Make webserver for directory */
		WebServer webserverDirectory = startAWebServerForDirectory(portDirectory,directory);
		Globals.getGlobals().addQuittables(webserverDirectory);
		
		/* Check to see response is what is expected */
		checkDirectory(guid, directory, url,portDirectory,url+":"+portDirectory);
		
		
		/* Start a cnode pool */
		List<Pair<Long,String>> pathsToCNodePool = new ArrayList<Pair<Long,String>>();
		
		pathsToCNodePool.add(new Pair<Long,String>(0L,url+":"+portNodePool));
		
		configFileName= "src/edu/uci/ics/luci/cacophony/MilestoneTest.cacophony.c_node_pool.properties";
		try {
			config = new XMLPropertiesConfiguration(configFileName);
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName);
		}
		CNodePool cNPool = new CNodePool(new KyotoCabinet<String,CNode>("fileMilestone")).launchCNodePool(config,null,null,pathsToCNodePool);
		assertTrue(cNPool != null);
		Globals.getGlobals().addQuittables(cNPool);
		while(cNPool.isUpdatingActive()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		/* Make webservers for CNodes */
		WebServer webserverNode = startAWebServerForPool(portNodePool,cNPool);
		Globals.getGlobals().addQuittables(webserverNode);
		
		
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
				if(((String)(response.get("error"))).equals("true")){
					fail((String)response.get("errors"));
				}
				assertEquals("false",((String)response.get("error")));
			
				assertTrue(((JSONObject)response.get("servers")).size() > 0);
				
				Long heartbeat = Long.parseLong((String)((JSONObject)((JSONObject)response.get("servers")).get(guid)).get("heartbeat"));
				assertTrue(System.currentTimeMillis() - heartbeat < Directory.FIVE_MINUTES);
				
				String namespace = ((String)((JSONObject) ((JSONObject) response.get("servers")).get(guid)).get("namespace"));
				assertEquals(directory.getDirectoryNamespace(),namespace);
   			
				JSONArray servers = ((JSONArray) ((JSONObject) ((JSONObject)response.get("servers")).get(guid)).get("access_routes"));
				assertTrue(servers.size() >= 1);
				for(int i =0 ; i < servers.size(); i++){
					if(Long.parseLong((String)((JSONObject)servers.get(i)).get("priority_order")) == 0L){
						assertEquals(url+":"+port,((String)((JSONObject)servers.get(i)).get("url")));
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
