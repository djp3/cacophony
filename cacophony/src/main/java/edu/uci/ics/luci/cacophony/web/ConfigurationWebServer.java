package edu.uci.ics.luci.cacophony.web;

import static org.junit.Assert.fail;

import java.util.HashMap;

import edu.uci.ics.luci.cacophony.server.CNodeServer;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;

public class ConfigurationWebServer {
	private static HashMap<String,HandlerAbstract> requestHandlerRegistry;
	private static WebServer ws;
	
	public static void launch(CNodeServer cNodeServer, String host) {
		// TODO If we don't set globals, starting the web server results in a null exception.
		Globals.setGlobals(new ConfigurationServerGlobals());
		try {
			requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			requestHandlerRegistry.put("select", new HandlerConfigCreator());
			requestHandlerRegistry.put("launch", new HandlerCNodeLauncher(cNodeServer));

			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, 80, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittable(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
	}
}
