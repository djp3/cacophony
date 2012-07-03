package edu.uci.ics.luci.cacophony.directory.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quub.Globals;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestHandlerFactory;
import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;
import com.quub.webserver.handlers.HandlerVersion;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;

public class HandlerShutdownTest {
	
	private static int testPort = 9020;

	private WebServer ws = null;
	
	Map<String, Class<? extends HandlerAbstract>> requestHandlerRegistry;


	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testWebServer() {
		
		/* Start the webserver */
		testPort++;
		try {
			requestHandlerRegistry = new HashMap<String, Class<? extends HandlerAbstract>>();
			requestHandlerRegistry.put(null,HandlerVersion.class);
			requestHandlerRegistry.put("",HandlerVersion.class);
			requestHandlerRegistry.put("shutdown",HandlerShutdown.class);
			
			CacophonyGlobals.resetGlobals();
			CacophonyGlobals g = CacophonyGlobals.getGlobals();
			g.setTesting(true);
			RequestHandlerFactory factory = new RequestHandlerFactory(g, requestHandlerRegistry);
			ws = new WebServer(g, factory, null, testPort, false,  new AccessControl());
			ws.start();
			g.addQuittables(ws);
		} catch (RuntimeException e1) {
			fail("Couldn't start webserver"+e1);
		}
		
		/* Test it with just the version command */
		
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + testPort + "/", false, params, 30 * 1000);
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
		
		
		/*Shutdown the web server badly*/

		String responseString1 = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + testPort + "/shutdown", false, params, 30 * 1000);
		} catch (MalformedURLException e3) {
			e3.printStackTrace();
			fail("Bad URL");
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IO Exception");
		}
		
		JSONObject response1 = null;
		try {
			response1 = new JSONObject(responseString1);
			try {
				assertEquals("true",response1.getString("error"));
			} catch (JSONException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		/*Shutdown the web server badly*/
		responseString1 = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Globals.getGlobals().getVersion());
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + testPort + "/shutdown", false, params, 30 * 1000);
		} catch (MalformedURLException e3) {
			e3.printStackTrace();
			fail("Bad URL");
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IO Exception");
		}
		
		response1 = null;
		try {
			response1 = new JSONObject(responseString1);
			try {
				assertEquals("true",response1.getString("error"));
			} catch (JSONException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		/*Shutdown the web server badly*/
		responseString1 = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("seriously", "true");
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + testPort + "/shutdown", false, params, 30 * 1000);
		} catch (MalformedURLException e3) {
			e3.printStackTrace();
			fail("Bad URL");
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IO Exception");
		}
		
		response1 = null;
		try {
			response1 = new JSONObject(responseString1);
			try {
				assertEquals("true",response1.getString("error"));
			} catch (JSONException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
		
		//Shutdown the web server correctly

		responseString1 = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("seriously", "true");
			params.put("version", Globals.getGlobals().getVersion());
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + testPort + "/shutdown", false, params, 30 * 1000);
		} catch (MalformedURLException e3) {
			e3.printStackTrace();
			fail("Bad URL");
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("IO Exception");
		}
		
		response1 = null;
		try {
			response1 = new JSONObject(responseString1);
			try {
				assertEquals("false",response1.getString("error"));
			} catch (JSONException e5) {
				e5.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e2) {
			e2.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
	
		/* Wait for it to shutdown */
		while(ws.getWebServer().isAlive()){
			try {
				ws.getWebServer().join();
			} catch (InterruptedException e) {
			}
		}
		

	}

}

