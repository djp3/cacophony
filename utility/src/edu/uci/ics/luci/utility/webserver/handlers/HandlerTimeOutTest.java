package edu.uci.ics.luci.utility.webserver.handlers;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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

public class HandlerTimeOutTest {
	
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
			requestHandlerRegistry.put(null,new HandlerTimeOut());
			
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
		
		try {
			HashMap<String, String> params = new HashMap<String, String>();

			WebUtil.fetchWebPage("http://localhost:" + ws.getPort() + "/", false, params, 5 * 1000);
			fail("Shouldn't have returned cleanly");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail("Bad URL");
		} catch (SocketTimeoutException e) {
			/* This is what we want to happen */
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception");
		}

	}

}
