package edu.uci.ics.luci.cacophony.directory.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.quub.webserver.AccessControl;
import com.quub.webserver.RequestHandlerFactory;
import com.quub.webserver.RequestHandlerHelper;
import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;

public class HandlerVersionTest {
	
	private static int testPort = 9020;

	private WebServer ws = null;

	Map<String, Class<? extends RequestHandlerHelper>> requestHandlerRegistry;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		startAWebServer();
	}

	@After
	public void tearDown() throws Exception {

		stopAWebServer();
	
		while(ws.getWebServer().isAlive()){
			try {
				ws.getWebServer().join();
			} catch (InterruptedException e) {
			}
		}
	}


	private void startAWebServer() {
		testPort++;
		try {
			requestHandlerRegistry = new TreeMap<String, Class<? extends RequestHandlerHelper>>();
			requestHandlerRegistry.put("",HandlerVersion.class);
			requestHandlerRegistry.put("version",HandlerVersion.class);
			requestHandlerRegistry.put("shutdown",HandlerShutdown.class);
			
			CacophonyGlobals.resetGlobals();
			CacophonyGlobals g = CacophonyGlobals.getGlobals();
			RequestHandlerFactory factory = new RequestHandlerFactory(g, requestHandlerRegistry,true);
			ws = new WebServer(g, factory, null, testPort, false, true, new AccessControl(g));
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
			params.put("version", CacophonyGlobals.getVersion());

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
	
	
	
	public static JSAPResult makeDummyConfig(int testport) throws JSAPException {
		JSAP jsap = new JSAP();
		JSAPResult config = null;
		Switch sw = null;
		FlaggedOption fl = null;
	        
		try{
			sw = new Switch("testing")
                      .setDefault("false") 
                      .setShortFlag('t') 
                      .setLongFlag("testing");
	        
			sw.setHelp("Connect to the testing resources.");
			jsap.registerParameter(sw);
			
			sw = new Switch("staging")
                      .setDefault("false") 
                      .setShortFlag('s') 
                      .setLongFlag("staging");
	        
			sw.setHelp("Connect to the staging resources.");
			jsap.registerParameter(sw);
			
			fl = new FlaggedOption("port")
        			  .setStringParser(JSAP.INTEGER_PARSER)
                      .setDefault(""+testport) 
                      .setRequired(false) 
                      .setShortFlag('p') 
                      .setLongFlag("port");
	        
			fl.setHelp("Which port should I listen for REST commands on");
			
			jsap.registerParameter(fl);
        
			sw = new Switch("help")
        			.setDefault("false") 
        			.setShortFlag('h') 
        			.setLongFlag("help");

			sw.setHelp("Show this help message"); 
			jsap.registerParameter(sw);
        
			String[] args = new String[0];
			config = jsap.parse(args);
		}
		catch(Exception e){
			config=null;
		}
		return config;
	}

	@Test
	public void testWebServer() {
		
		if(ws != null){
			try{
				JSAPResult config = makeDummyConfig(testPort);
				WebServerWarmUp.go(config, ws, "http://localhost");
			} catch (JSAPException e) {
				fail("Didn't expect this");
			}
			catch(RuntimeException e){
				fail("Didn't expect this");
			}
			if(ws.getQuitting()){
				fail("Didn't expect this");
			}
		}

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
		

	}



	@Test
	public void testWebServerVersion() {

		
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
			
			String answer  = response.getString("version");
			assertEquals(CacophonyGlobals.getVersion(),answer);
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		

		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + testPort + "/version", false, params, 30 * 1000);
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
			assertEquals(CacophonyGlobals.getVersion(),answer);
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		

	}
	

	@Test
	public void testBadShutdownVersion() {
		
		String responseString = null;
		
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("seriously", "true");
			params.put("version", CacophonyGlobals.getVersion()+"foo");

			responseString = WebUtil.fetchWebPage("http://localhost:" + testPort + "/shutdown", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL");
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception");
		}
		
		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("true",response.getString("error"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
			JSONArray errors = new JSONArray(response.getString("errors"));
			String thing = errors.getString(0);
			//System.out.println(thing);
			assertTrue(thing.contains("version"));
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
	}
	

}
