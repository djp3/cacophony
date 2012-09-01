package edu.uci.ics.luci.cacophony.api.directory;

import static org.junit.Assert.assertEquals;
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

import com.quub.Globals;
import com.quub.GlobalsTest;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher;
import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;

import edu.uci.ics.luci.cacophony.CacophonyRequestHandlerHelper;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.directory.Directory;

public class HandlerDirectoryNamespaceTest {
	
	private static int testPort = 9020;
	private static synchronized int testPortPlusPlus(){
		int x = testPort;
		testPort++;
		return(x);
	}


	@BeforeClass
	public static void setUpClass() throws Exception {
		Globals.setGlobals(new GlobalsTest());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
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
			requestHandlerRegistry.put("namespace",new HandlerDirectoryNamespace(d));

			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}

	
	@Test
	public void testGetNamespace() {
		workingPort = testPortPlusPlus();
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		startAWebServer(workingPort,d);
		
		String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
		try {
			d.initializeDirectory(new XMLPropertiesConfiguration(configFileName));
		} catch (ConfigurationException e1) {
			fail("");
		}
		
		String directoryNamespace = "test.namespace";
		d.setDirectoryNamespace(directoryNamespace);
		
		String directoryGUID = "DirectoryGUID:Test";
		d.startHeartbeat(directoryGUID);
		
		
		/* Test with bad version */
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/namespace", false, params, 30 * 1000);
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
		
		
		
		/* Test with working case */
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/namespace", false, params, 30 * 1000);
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
				assertEquals(directoryNamespace,response.getString("namespace"));
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
