package edu.uci.ics.luci.cacophony.api.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.directory.nodelist.CNodeReference;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.WebUtil;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;

public class HandlerNodeCheckinTest {
	
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
	
	HashMap<String,HandlerAbstract> requestHandlerRegistry;

	private int workingPort;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	private void startAWebServer(int port,Directory d) {
		try {
			requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("checkin",new HandlerNodeCheckin(d));
			requestHandlerRegistry.put(null,new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/www/"));

			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}

	
	@Test
	public void testGetCheckin() {
		workingPort = testPortPlusPlus();
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		startAWebServer(workingPort,d);
		
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
		
		String directoryNamespace = "test.namespace";
		d.setDirectoryNamespace(directoryNamespace);
		
		String directoryGUID = "DirectoryGUID:Test";
		d.startHeartbeat(directoryGUID);
		
		
		/* Do this badly by mismatching namespaces */
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", directoryNamespace+"foo");

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/checkin", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}
		
		//System.out.println(responseString);

		JSONObject response = null;
		try {
			response = (JSONObject) JSONValue.parse(responseString);
			try {
				assertEquals("true",(String)response.get("error"));
			} catch (ClassCastException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (ClassCastException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		
		/* Do this badly by mismatching namespaces */
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/checkin", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = (JSONObject) JSONValue.parse(responseString);
			try {
				assertEquals("true",(String)response.get("error"));
			} catch (ClassCastException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (ClassCastException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		
		
		
		
		/* Do this badly by mismatching versions */
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion()+"foo");
			params.put("namespace", directoryNamespace);

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/checkin", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = (JSONObject) JSONValue.parse(responseString);
			try {
				assertEquals("true",(String)response.get("error"));
			} catch (ClassCastException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (ClassCastException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		
		/* Do this correctly */
		
		/* Build a CNodeReference */
		CNodeReference cnr = new CNodeReference();
		
		/* Set the identity */
		cnr.setMetaCNodeGuid("MetaCNodeGUIDTest-"+System.currentTimeMillis());
		cnr.setCNodeGuid("CNodeGUIDTest-"+System.currentTimeMillis());

		/* Update this node's heartbeat */
		cnr.setLastHeartbeat(System.currentTimeMillis());
					
		Set<Pair<Long, String>> accessRoutes = new TreeSet<Pair<Long, String>>();
		Pair<Long,String> p = new Pair<Long,String>(0L,"http://www.dummy.com/index.html?node=");
		accessRoutes.add(p);
		cnr.setAccessRoutesForUI(accessRoutes);
					
		accessRoutes = new TreeSet<Pair<Long, String>>();
		p = new Pair<Long,String>(0L,"http://www.dummy.com/predict?version=");
		accessRoutes.add(p);
		cnr.setAccessRoutesForAPI(accessRoutes);
		
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", directoryNamespace);
			params.put("json_data", CNodeReference.toJSONObject(cnr).toJSONString(JSONStyle.LT_COMPRESS));

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/checkin", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception:"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = (JSONObject) JSONValue.parse(responseString);
			try {
				assertEquals("false",(String)response.get("error"));
			} catch (ClassCastException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (ClassCastException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
	}
	
	
	
}
