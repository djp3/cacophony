package edu.uci.ics.luci.utility.webserver.handlers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

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

public class HandlerFileServerTest {
	
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
			requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler = new HandlerFileServer(edu.uci.ics.luci.utility.Globals.class,"/www_test/");
			requestHandlerRegistry.put(null,handler);
			
			handler = new HandlerVersion(Globals.getGlobals().getSystemVersion());
			requestHandlerRegistry.put("",handler);
			
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

			responseString = WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/index.html", false, params, 30 * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL");
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception");
		}
		
		assertTrue(responseString.contains("<h1>This is a test html file</h1>"));
		

	}



	

}
