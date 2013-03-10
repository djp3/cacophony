package edu.uci.ics.luci.cacophony.api.node;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.model.KyotoCabinet;
import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.CNodePool;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.WebUtil;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;

public class HandlerCNodePredictionTest {

	private static int testPort = 9021;
	private static synchronized int testPortPlusPlus(){
		int x = testPort;
		testPort++;
		return(x);
	}
	

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

	private int workingPort;

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
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/www_test/"));
			
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
	
	
	private Directory startADirectory(){
		Directory d = new Directory();
		
		/* Load up the data */
		String configFileName = null;
		try {
			configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
			d.initializeDirectory(new XMLPropertiesConfiguration(configFileName));
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail("Unable to initialize Directory");
		}
		
		/* Pick a Directory GUID*/
		String directoryGUID = "DirectoryGUID:"+System.currentTimeMillis();
		
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
			
		/* Add a this URL*/
		Pair<Long, String> p = new Pair<Long,String>(0L,url+":"+workingPort);
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		urls.add(p);
			
		/* Start reporting heartbeats */
		String namespace = "edu.uci.ics.luci.cacophony";
		d.setDirectoryNamespace(namespace);
		d.startHeartbeat(directoryGUID,urls);
		
		Globals.getGlobals().addQuittables(d);
		return(d);
	}
	
	
	@Test
	public void testCNodePoolCreation() {
		workingPort = testPortPlusPlus();
		Directory d = startADirectory();
		startAWebServerForDirectory(workingPort,d);
		
		workingPort = testPortPlusPlus();
		List<Pair<Long,String>> baseUrls = new ArrayList<Pair<Long,String>>();
		String url = null;
		try {
			url = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			url ="127.0.0.1";
		}
		baseUrls.add(new Pair<Long,String>(0L,url+":"+workingPort));
		

		String configFileName = "src/edu/uci/ics/luci/cacophony/CNodeTest.cacophony.c_node_pool.load_nodes.properties";
		XMLPropertiesConfiguration config = null;
		try {
			config = new XMLPropertiesConfiguration(configFileName);
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName+"\n"+e2);
		}
		config.setProperty("namespace", d.getDirectoryNamespace());
		CNodePool cNPool = new CNodePool(new KyotoCabinet<String,CNode>()).launchCNodePool(config,null,null,baseUrls);
		assertTrue(cNPool != null);
		startAWebServerForPool(workingPort,cNPool);
		Globals.getGlobals().addQuittables(cNPool);
		
		/* Test with working case */
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", d.getDirectoryNamespace());
			HashSet<String> poolKeySet = cNPool.getPoolKeySet();
			String key = poolKeySet.iterator().next();
			//params.put("node", cNPool.getFromPool(key).getGraphingTestSet().attribute(0).value(0)+"");
			params.put("node", key);

			WebUtil.fetchWebPage("http://localhost:" + workingPort + "/predict", false, params, 60 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}
		
		//System.out.println(responseString);

		//TODO: Check to see that a prediction gets made.  Right now there is no training data to test on
	/*	
		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				assertTrue(response.getJSONArray("sunday") != null);
				assertTrue(response.getJSONArray("sunday").getJSONObject(0).getLong("x") >= 0L);
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (JSONException e) {
			fail("Bad JSON Response:"+responseString+"\n"+e);
		}*/
		
		
		
		/* Test with working case */
		//["1347073200000","1347076800000","1347080400000"]
		/*
		String testString = "1347073200000";
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", d.getDirectoryNamespace());
			HashSet<String> poolKeySet = cNPool.getPoolKeySet();
			String key = poolKeySet.iterator().next();
			params.put("node", cNPool.getFromPool(key).getGraphingTestSet().attribute(0).value(0)+"");
			JSONArray times = new JSONArray();
			times.put(testString);
			params.put("times", times.toString());

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/predict", false, params, 60 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}*/
		
		//System.out.println(responseString);

		/*
		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				assertTrue(response.getJSONArray("predictions") != null);
				assertEquals(testString,response.getJSONArray("predictions").getJSONObject(0).getString("x"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (JSONException e) {
			fail("Bad JSON Response:"+e);
		}*/
	}

}
