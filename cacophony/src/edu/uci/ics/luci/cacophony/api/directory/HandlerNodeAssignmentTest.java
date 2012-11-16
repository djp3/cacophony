package edu.uci.ics.luci.cacophony.api.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.api.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.WebUtil;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;

public class HandlerNodeAssignmentTest {
	
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
			requestHandlerRegistry.put("node_assignment",new HandlerNodeAssignment(d));
			requestHandlerRegistry.put("servers",new HandlerDirectoryServers(d));
			requestHandlerRegistry.put("nodes",new HandlerNodeList(d));
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
	public void testGetNodeAssignment() {
		workingPort = testPortPlusPlus();
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		startAWebServer(workingPort,d);
		
		try {
			String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
			d.initializeDirectory(new XMLPropertiesConfiguration(configFileName));
		} catch (ConfigurationException e1) {
			fail("");
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

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/node_assignment", false, params, 30 * 1000);
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
			response = new JSONObject(responseString);
			try {
				assertEquals("true",response.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		
		/* Do this badly by mismatching namespaces */
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/node_assignment", false, params, 30 * 1000);
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
			response = new JSONObject(responseString);
			try {
				assertEquals("true",response.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		
		
		
		
		/* Do this badly by mismatching versions */
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion()+"foo");
			params.put("namespace", directoryNamespace);

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/node_assignment", false, params, 30 * 1000);
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
			response = new JSONObject(responseString);
			try {
				assertEquals("true",response.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code:"+e);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		
		/* Do this correctly */
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
			params.put("namespace", directoryNamespace);

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/node_assignment", false, params, 30 * 1000);
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
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				assertTrue(response.getString("node_id") != null);
				assertTrue(response.getString("node_configuration") != null);
				assertTrue(response.getString("name") != null);
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
