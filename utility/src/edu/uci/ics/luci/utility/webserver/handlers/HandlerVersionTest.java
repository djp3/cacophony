package edu.uci.ics.luci.utility.webserver.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.WebUtil;

public class HandlerVersionTest {
	
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
	

	@Before
	public void setUp() throws Exception {
		startAWebServer(testPortPlusPlus());
	}

	@After
	public void tearDown() throws Exception {
	}
	
	

	private void startAWebServer(int port) {
		try {
			requestHandlerRegistry = new HashMap<String, HandlerAbstract>();
			requestHandlerRegistry.put("",new HandlerVersion(Globals.getGlobals().getSystemVersion()));
			requestHandlerRegistry.put("version",new HandlerVersion(Globals.getGlobals().getSystemVersion()));
			
			RequestDispatcher dispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(dispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}

	
	@Test
	public void testWebServer() {
		
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/", false, params, 30 * 1000);
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
		

	}



	@Test
	public void testWebServerVersion() {

		
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/", false, params, 30 * 1000);
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
			try {
				assertEquals("false",response.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
			
			String answer  = response.getString("version");
			assertEquals(Globals.getGlobals().getSystemVersion(),answer);
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		

		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/version", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL");
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception");
		}
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		
			String answer  = response.getString("version");
			assertEquals(Globals.getGlobals().getSystemVersion(),answer);
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		

	}
	

}
