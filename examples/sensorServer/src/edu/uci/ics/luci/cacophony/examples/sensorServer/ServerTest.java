package edu.uci.ics.luci.cacophony.examples.sensorServer;


import static org.junit.Assert.*;

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
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.WebUtil;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerVersion;

public class ServerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Globals.setGlobals(null);
	}

	@Before
	public void setUp() throws Exception {
		Globals.setGlobals(new SensorServerGlobals());
	}

	@After
	public void tearDown() throws Exception {
		Globals.getGlobals().setQuitting(true);
	}
	
	@Test
	public void testBadVersion(){
		WebServer ws = null;
		HashMap<String,HandlerAbstract> requestHandlerRegistry;
		

		try {
			requestHandlerRegistry = new HashMap<String, HandlerAbstract>();
			requestHandlerRegistry.put("",new HandlerVersion(Globals.getGlobals().getSystemVersion()));
			requestHandlerRegistry.put("version",new HandlerVersion(Globals.getGlobals().getSystemVersion()));
			requestHandlerRegistry.put("sense",new HandlerSensor());
		
			RequestDispatcher dispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(dispatcher, SensorServerGlobals.DEFAULT_PORT, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("sensor", "ipaddress");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertTrue(response.getString("error").equals("true"));
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
	public void testServer(){
		WebServer ws = null;
		HashMap<String,HandlerAbstract> requestHandlerRegistry;
		

		try {
			requestHandlerRegistry = new HashMap<String, HandlerAbstract>();
			requestHandlerRegistry.put("",new HandlerVersion(Globals.getGlobals().getSystemVersion()));
			requestHandlerRegistry.put("version",new HandlerVersion(Globals.getGlobals().getSystemVersion()));
			requestHandlerRegistry.put("sense",new HandlerSensor());
		
			RequestDispatcher dispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(dispatcher, SensorServerGlobals.DEFAULT_PORT, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		
		String responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		JSONObject response = null;
		try {
			response = new JSONObject(responseString);
			try {
				if(!"false".equals(response.getString("error"))){
					fail("Problem");
				}
				
				String answer  = response.getString("version");
				if(!Globals.getGlobals().getSystemVersion().equals(answer)){
					fail("Problem");
				}
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		
		/**** Ask for sensors **/
		
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "ipaddress");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				String answer  = response.getString("value");
				assertTrue(answer.contains("."));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "volume");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				String answer  = response.getString("value");
				assertTrue(answer.contains("."));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "accelerometer");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				String answer  = response.getString("value");
				assertTrue(answer.contains("."));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "idle");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				Long answer  = response.getLong("value");
				assertTrue(answer >= 0);
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "light");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				Long answer  = response.getLong("value");
				assertTrue(answer >= 0);
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "powersource");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				String answer  = response.getString("value");
				assertTrue(answer.equals("WALL") || answer.equals("BATTERY"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "process");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				String answer  = response.getString("value");
				assertTrue(answer.toLowerCase().contains("eclipse"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "uiactivity");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				Long answer  = response.getLong("value");
				assertTrue(answer >= 0);
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "volume");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				Long answer  = response.getLong("value");
				assertTrue(answer >= 0);
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		/**** Ask for sensors **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "wifi");
			params.put("format", "json");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		response = null;
		try {
			response = new JSONObject(responseString);
			try {
				assertEquals("false",response.getString("error"));
				String answer  = response.getString("value");
				assertTrue(answer.contains(":"));
			} catch (JSONException e) {
				e.printStackTrace();
				fail("No error code");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			fail("Bad JSON Response");
		}
		
		/**** Ask for sensors as html **/
		responseString = null;
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("version", Server.API_VERSION);
			params.put("sensor", "wifi");
			params.put("format", "html");

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/sense", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL:"+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception"+e);
		}
		
		//System.out.println(responseString);

		assertTrue(responseString.contains("html"));
		
		
		
	}

}
