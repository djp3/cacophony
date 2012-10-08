package edu.uci.ics.luci.cacophony.api;

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

import com.quub.Globals;
import com.quub.GlobalsTest;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher;
import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;


public class HandlerShutdownTest {
	
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
		// These tests shut down manually
		//Globals.getGlobals().setQuitting(true);
		Globals.setGlobals(null);
	}

	private WebServer ws = null;
	
	HashMap<String,HandlerAbstract> requestHandlerRegistry;

	private int workingPort;

	@Before
	public void setUp() throws Exception {
		workingPort = testPortPlusPlus();
		startAWebServer(workingPort);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	private void startAWebServer(int port) {
		try {
			requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put(null,handler);
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());

			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}

	
	@Test
	public void testWebServer() {
		
		/* Test it with just the version command */
		
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/", false, params, 30 * 1000);
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
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/shutdown", false, params, 30 * 1000);
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
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/shutdown", false, params, 30 * 1000);
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
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/shutdown", false, params, 30 * 1000);
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
		
		//Shutdown the web server badly

		responseString1 = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("seriously", "true");
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion()+"foo");
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/shutdown", false, params, 30 * 1000);
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
			params.put("version", CacophonyRequestHandlerHelper.getAPIVersion());
		
			responseString1 = WebUtil.fetchWebPage("http://localhost:" + workingPort + "/shutdown", false, params, 30 * 1000);
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

