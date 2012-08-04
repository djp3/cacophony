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
import com.quub.util.Pair;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestHandlerFactory;
import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;
import com.quub.webserver.handlers.HandlerFileServer;
import com.quub.webserver.handlers.HandlerVersion;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.directory.api.HandlerDirectoryNamespace;
import edu.uci.ics.luci.cacophony.directory.api.HandlerDirectoryServers;
import edu.uci.ics.luci.cacophony.directory.api.HandlerNodeList;
import edu.uci.ics.luci.cacophony.directory.api.HandlerShutdown;

public class DirectoryTest {
	
	private static int testPort = 9020;
	private WebServer ws = null;
	Map<String, Class<? extends HandlerAbstract>> requestHandlerRegistry;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}



	private Directory d;

	@Before
	public void setUp() throws Exception {
		d = Directory.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		if(d != null){
			d.setQuitting(true);
		}
	}

	@Test
	public void testStartHeartbeat() {
		if(d==null) fail("");
		/* Pick a guid */
		String guid = "Server GUID from Testing";
		
		d.startHeartbeat(0L, 500L, guid, null);
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
		if(d==null) fail("");
		d.startHeartbeat(0L, 500L);
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
		String directoryPropertiesLocation = "cacophony.directory.properties";
		try {
			XMLPropertiesConfiguration config;
			config = new XMLPropertiesConfiguration(directoryPropertiesLocation);
			assertEquals("value",config.getString("test"));
		} catch (ConfigurationException e1) {
			fail("Problem loading configuration from:"+directoryPropertiesLocation+"\n"+e1);
		}
	}
	


	private void startAWebServer() {
		testPort++;
		try {
			requestHandlerRegistry = new HashMap<String, Class<? extends HandlerAbstract>>();
			requestHandlerRegistry.put("",HandlerVersion.class);
			requestHandlerRegistry.put("version",HandlerVersion.class);
			requestHandlerRegistry.put("servers",HandlerDirectoryServers.class);
			requestHandlerRegistry.put("nodes",HandlerNodeList.class);
			requestHandlerRegistry.put("namespace",HandlerDirectoryNamespace.class);
			requestHandlerRegistry.put("shutdown",HandlerShutdown.class);
			requestHandlerRegistry.put(null,HandlerFileServer.class);
			
			CacophonyGlobals.resetGlobals();
			CacophonyGlobals g = CacophonyGlobals.getGlobals();
			g.setTesting(true);
			RequestHandlerFactory factory = new RequestHandlerFactory(g, requestHandlerRegistry);
			ws = new WebServer(g, factory, null, testPort, false, new AccessControl());
			ws.start();
			g.addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}

	private void stopAWebServer() {
		
		String responseString = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("seriously", "true");
			params.put("version", Globals.getGlobals().getVersion());

			responseString = WebUtil.fetchWebPage("http://localhost:" + testPort + "/shutdown", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			fail("Bad URL"+e);
		} catch (IOException e) {
			fail("IO Exception"+e);
		}
		
		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		while(ws.getWebServer().isAlive()){
			try {
				ws.getWebServer().join();
			} catch (InterruptedException e) {
			}
		}
	}
	
	

	@Test
	public void testRunServersIndefinitely() {
		startAWebServer();
		
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
		Pair<Long, String> p = new Pair<Long,String>(0L,url+":"+testPort);
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		urls.add(p);
		p = new Pair<Long,String>(1L,"foobar.com");
		urls.add(p);
			
		/* Start reporting heartbeats */
		Directory.getInstance().setDirectoryNamespace("testNamespace");
		Directory.getInstance().startHeartbeat(guid,urls);
		
		/* Check to see response is what is expected */
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + testPort + "/servers", false, params, 30 * 1000);
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
				
				assertEquals(Globals.getGlobals().getVersion(),response.getString("version"));
				
				assertTrue(response.getJSONObject("servers").length() > 0);
				
    			Long heartbeat = response.getJSONObject("servers").getJSONObject(guid).getLong("heartbeat");
				assertTrue(System.currentTimeMillis() - heartbeat < Directory.FIVE_MINUTES);
				
    			String namespace = response.getJSONObject("servers").getJSONObject(guid).getString("namespace");
    			assertEquals(Directory.getInstance().getDirectoryNamespace(),namespace);
    			
    			JSONArray servers = response.getJSONObject("servers").getJSONObject(guid).getJSONArray("access_routes");
    			assertEquals(2,servers.length());
    			for(int i =0 ; i < servers.length(); i++){
    				if(servers.getJSONObject(i).getLong("priority_order") == 0){
    					assertEquals(url+":"+testPort,servers.getJSONObject(i).getString("url"));
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
		
		String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
		Directory d = Directory.launchDirectory(configFileName);
		
		//fail("Remove this to run indefinitely");
		//while(true){
			try {
				Thread.sleep(15*60*1000);
			} catch (InterruptedException e) {
			}
		//}
		
		d.setQuitting(true);
		stopAWebServer();
	

	}


}
