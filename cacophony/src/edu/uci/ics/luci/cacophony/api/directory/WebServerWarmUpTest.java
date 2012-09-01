package edu.uci.ics.luci.cacophony.api.directory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;

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

import edu.uci.ics.luci.cacophony.api.HandlerVersion;

public class WebServerWarmUpTest {
	
	private static int testPort = 9030;
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
			requestHandlerRegistry.put("version",handler);

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
		WebServerWarmUp.go(ws,workingPort, "http://localhost");
		assertTrue(!ws.getQuitting());
		
		/* Try with out starting the webserver */
		workingPort = testPortPlusPlus();
		ws = new WebServer(new RequestDispatcher(requestHandlerRegistry), workingPort, false, new AccessControl());
		Globals.getGlobals().addQuittables(ws);
		WebServerWarmUp.go(ws, workingPort, "http://localhost",10*1000,0);
		assertTrue(ws.getQuitting());
	}

}

