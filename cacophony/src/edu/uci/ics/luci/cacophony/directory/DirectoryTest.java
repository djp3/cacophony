package edu.uci.ics.luci.cacophony.directory;

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
import java.util.Map.Entry;

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
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeList;

public class DirectoryTest {
	
	private static int testPort = 9020;
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
	
	private WebServer ws = null;
	HashMap<String, HandlerAbstract> requestHandlerRegistry;
	private int workingPort;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	
	}

	@Test
	public void testStartHeartbeat() {
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		
		/* Pick a guid */
		String guid = "Server GUID from Testing";
		
		d.startHeartbeat(0L, 500L, guid, null);
		d.startMetaCNodeListCleaner();
		try{
			Long heartbeat01 = d.getHeartbeat(guid);
			assertTrue(heartbeat01 != null);
			Thread.sleep(750);
			Long heartbeat02 = d.getHeartbeat(guid);
			assertTrue(heartbeat02 != null);
			assertTrue(!heartbeat01.equals(heartbeat02));
		} catch (InterruptedException e) {
			fail(e.toString());
		}
	}
	
	
	
	@Test
	public void testGetServers() {
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		
		d.startHeartbeat(0L, 500L);
		d.startMetaCNodeListCleaner();
		try{
			String me = InetAddress.getLocalHost().getHostAddress();
			Map<String, JSONObject> list = d.getServers();
			for(Entry<String,JSONObject> e : list.entrySet()){
				JSONObject jsonData = null;
				try{
					jsonData = e.getValue();
					if(e.getKey().equals(me)){
						assertTrue(System.currentTimeMillis()- jsonData.getLong("heartbeat") < Directory.FIVE_MINUTES);
					}
				} catch (JSONException e1) {
					fail("Bad JSON Data in Cassandra ring:\n"+jsonData.toString()+"\n"+e1);
				}
			}
		} catch (UnknownHostException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void testOpenProperties() {
	
		/* Get Directory properties */
		String directoryPropertiesLocation = "directory.cloud2.config.properties";
		try {
			XMLPropertiesConfiguration config;
			config = new XMLPropertiesConfiguration(directoryPropertiesLocation);
			assertEquals("value",config.getString("test"));
		} catch (ConfigurationException e1) {
			fail("Problem loading configuration from:"+directoryPropertiesLocation+"\n"+e1);
		}
	}
	
	private void startAWebServer(int port,Directory d) {
		try {
			requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
			requestHandlerRegistry.put("servers",new HandlerDirectoryServers(d));
			requestHandlerRegistry.put("nodes",new HandlerNodeList(d));
			requestHandlerRegistry.put("namespace",new HandlerDirectoryNamespace(d));
			requestHandlerRegistry.put(null, new HandlerFileServer(com.quub.Globals.class,"/www_test/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}


	

	@Test
	public void testRunServersIndefinitely() {
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		
		try {
			String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
			d.initializeDirectory(new XMLPropertiesConfiguration(configFileName));
		} catch (ConfigurationException e1) {
			fail("");
		}
		
		workingPort =testPortPlusPlus();
		startAWebServer(workingPort,d);
		
		/* Pick a guid */
		String guid = "Server GUID from Testing";
		
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
		Pair<Long, String> p = new Pair<Long,String>(0L,url+":"+workingPort);
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		urls.add(p);
		p = new Pair<Long,String>(1L,"foobar.com");
		urls.add(p);
			
		String myNamespace = "test.namespace";
		/* Start reporting heartbeats */
		d.setDirectoryNamespace(myNamespace);
		d.startHeartbeat(guid,urls);
		d.startMetaCNodeListCleaner();
		
		/* Check to see response is what is expected */
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version",CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace",myNamespace);

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/servers", false, params, 30 * 1000);
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
				assertEquals("false",response.getString("error"));
				
				assertTrue(response.getJSONObject("servers").length() > 0);
				
    			Long heartbeat = response.getJSONObject("servers").getJSONObject(guid).getLong("heartbeat");
				assertTrue(System.currentTimeMillis() - heartbeat < Directory.FIVE_MINUTES);
				
    			String namespace = response.getJSONObject("servers").getJSONObject(guid).getString("namespace");
    			assertEquals(d.getDirectoryNamespace(),namespace);
    			
    			JSONArray servers = response.getJSONObject("servers").getJSONObject(guid).getJSONArray("access_routes");
    			assertEquals(2,servers.length());
    			for(int i =0 ; i < servers.length(); i++){
    				if(servers.getJSONObject(i).getLong("priority_order") == 0){
    					assertEquals(url+":"+workingPort,servers.getJSONObject(i).getString("url"));
    				}
    				else{
    					assertEquals("foobar.com",servers.getJSONObject(i).getString("url"));
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
		
		
		//fail("Remove this to run indefinitely");
		//new PopUpWindow("Close this window to shut down the webserver: "+this.getClass().getCanonicalName());
	}


}
