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
import java.util.Map;
import java.util.TreeSet;

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
import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;

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
	public void testJSONAssumptions(){
		JSONObject parse = (JSONObject)JSONValue.parse("{hello:world}");
		assertTrue(parse != null);
		assertTrue(parse.get("hello").equals("world"));
		assertTrue(parse.get("world") == null);
		parse = (JSONObject)JSONValue.parse("{blah}");
		assertTrue(parse == null);
		
		String testcase = "";
		try{
			parse = (JSONObject)JSONValue.parse(testcase);
			fail("Should parse as string");
		}
		catch(ClassCastException e){
			assertTrue("".equals(JSONValue.parse(testcase)));
		}
		parse = (JSONObject)JSONValue.parse((String)null);
		assertTrue(parse == null);
	}
	
	@Test
	public void testSorting(){
		
		FailoverFetch f = new FailoverFetch();
		Map<String,Long> urlMap = new HashMap<String,Long>();
		
		urlMap.put("localhost:1776",0L);
		urlMap.put("localhost:1776",0L);
		urlMap.put("localhost:1777",1L);
		urlMap.put("localhost:1778",1L);
		urlMap.put("localhost:1779",2L);
		urlMap.put("localhost:1780",3L);
		f.resetUrlPool(urlMap);
		
		for(int i = 0 ;i < 100; i++){
			TreeSet<Pair<Long, String>> servers = new TreeSet<Pair<Long,String>>(f.getUrlPoolCopy());
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
		
		
		FailoverFetch f = new FailoverFetch(FailoverFetch.fetchDirectoryList("localhost:"+workingPort,namespace));
		assertEquals(2,f.getUrlPoolCopy().size());  //Should only be keeping uniques
		
		/* Test to see if the code returns JSON */
			HashMap<String, String> params = new HashMap<String, String>();
			
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", namespace);

			JSONObject response = null;
			try {
				response = f.fetchJSONObject("/servers", false, params, 30 * 1000);
			} catch (MalformedURLException e) {
				fail("Bad URL "+e);
			} catch (IOException e) {
				fail("IO Exception"+e);
			}
		
			//System.out.println(response.toString(5));
				if(((String)response.get("error")).equals("true")){
					fail((String)response.get("errors"));
				}
				assertEquals("false",response.get("error"));
			
				assertTrue(((JSONObject)response.get("servers")).size() > 0);
			
				Long heartbeat = Long.parseLong(((String)((JSONObject) ((JSONObject)response.get("servers")).get(guid)).get("heartbeat")));
				assertTrue(System.currentTimeMillis() - heartbeat < Directory.FIVE_MINUTES);
			
				namespace = ((String)((JSONObject) ((JSONObject)response.get("servers")).get(guid)).get("namespace"));
				assertEquals(d.getDirectoryNamespace(),namespace);
			
				JSONArray servers = ((JSONArray)((JSONObject)((JSONObject)response.get("servers")).get(guid)).get("access_routes"));
				assertEquals(5,servers.size());
				for(int i =0 ; i < servers.size(); i++){
					assertTrue(((String)((JSONObject)servers.get(i)).get("url")).contains(url));
				}
	}

}
