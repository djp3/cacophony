package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quub.Globals;
import com.quub.GlobalsTest;
import com.quub.util.Pair;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestDispatcher;
import com.quub.webserver.WebServer;
import com.quub.webserver.handlers.HandlerFileServer;

import edu.uci.ics.luci.cacophony.api.HandlerShutdown;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryNamespace;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryServers;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeAssignment;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeCheckin;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeList;
import edu.uci.ics.luci.cacophony.directory.Directory;

public class CNodePoolTest {

	private static int testPort = 9021;
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

	private int workingPort;

	@Before
	public void setUp() throws Exception {
	
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	private WebServer startAWebServer(int port,Directory d) {
		WebServer ws = null;
		try {
			HashMap<String, HandlerAbstract> requestHandlerRegistry = new HashMap<String,HandlerAbstract>();
			HandlerAbstract handler =  new HandlerVersion();
			requestHandlerRegistry.put("",handler);
			requestHandlerRegistry.put("version",handler);
			requestHandlerRegistry.put("shutdown",new HandlerShutdown());
			requestHandlerRegistry.put("servers",new HandlerDirectoryServers(d));
			requestHandlerRegistry.put("nodes",new HandlerNodeList(d));
			requestHandlerRegistry.put("node_assignment",new HandlerNodeAssignment(d));
			requestHandlerRegistry.put("node_checkin",new HandlerNodeCheckin(d));
			requestHandlerRegistry.put("namespace",new HandlerDirectoryNamespace(d));
			requestHandlerRegistry.put(null, new HandlerFileServer(com.quub.Globals.class,"/www_test/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		return(ws);
	}
	
	private Directory startADirectory(){
		Directory d = new Directory();
		
		/* Load up the data */
		try {
			String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
			d.initializeDirectory(new XMLPropertiesConfiguration(configFileName));
		} catch (ConfigurationException e1) {
			fail("");
		}
		
		/* Pick a Directory GUID*/
		String directoryGUID = "DirectoryGUID:"+System.currentTimeMillis();
		
		/* Figure out our url */
		String url = null;
		try {
			url = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			try {
				url = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
			}
		}
			
		/* Add a this URL*/
		Pair<Long, String> p = new Pair<Long,String>(0L,url+":"+workingPort);
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		urls.add(p);
			
		/* Start reporting heartbeats */
		String namespace = "test.waitscout.com";
		d.setDirectoryNamespace(namespace);
		d.startHeartbeat(directoryGUID,urls);
		
		Globals.getGlobals().addQuittables(d);
		return(d);
	}
	
	
	@Test
	public void testCNodePoolCreation() {
		workingPort = testPortPlusPlus();
		Directory d = startADirectory();
		startAWebServer(workingPort,d);
		
		List<Pair<Long,String>> baseUrls = new ArrayList<Pair<Long,String>>();
		String url = null;
		try {
			url = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			url ="127.0.0.1";
		}
		baseUrls.add(new Pair<Long,String>(0L,url+":"+workingPort));
		
		String configFileName = "src/edu/uci/ics/luci/cacophony/CNodeTest.cacophony.c_node_pool.properties";
		XMLPropertiesConfiguration config = null;
		try {
			config = new XMLPropertiesConfiguration(configFileName);
		} catch (ConfigurationException e2) {
			fail("Unable to use requested configuration file:"+configFileName);
		}
		
		CNodePool cNPool = new CNodePool().launchCNodePool(config,baseUrls);
		assertTrue(cNPool != null);
		assertTrue(cNPool.getPool().size() == 1);
		//new PopUpWindow("Click To Stop Test:"+this.getClass().getCanonicalName());
		Globals.getGlobals().addQuittables(cNPool);
	}

}
