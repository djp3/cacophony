package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.api.HandlerShutdown;
import edu.uci.ics.luci.cacophony.api.HandlerVersion;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryNamespace;
import edu.uci.ics.luci.cacophony.api.directory.HandlerDirectoryServers;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeAssignment;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeCheckin;
import edu.uci.ics.luci.cacophony.api.directory.HandlerNodeList;
import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.model.KyotoCabinet;
import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;
import edu.uci.ics.luci.utility.datastructure.Pair;
import edu.uci.ics.luci.utility.webserver.AccessControl;
import edu.uci.ics.luci.utility.webserver.HandlerAbstract;
import edu.uci.ics.luci.utility.webserver.RequestDispatcher;
import edu.uci.ics.luci.utility.webserver.WebServer;
import edu.uci.ics.luci.utility.webserver.handlers.HandlerFileServer;

public class CNodeTest {
	
	private static int testPort = 9021;
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
			requestHandlerRegistry.put(null, new HandlerFileServer(edu.uci.ics.luci.cacophony.CacophonyGlobals.class,"/wwwNode/"));
			
			RequestDispatcher requestDispatcher = new RequestDispatcher(requestHandlerRegistry);
			ws = new WebServer(requestDispatcher, port, false, new AccessControl());
			ws.start();
			Globals.getGlobals().addQuittables(ws);
		} catch (RuntimeException e) {
			fail("Couldn't start webserver"+e);
		}
		
		return(ws);
	}

	public Directory startADirectory(){
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		
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
		String namespace = "edu.uci.ics.luci.cacophony";
		d.setDirectoryNamespace(namespace);
		d.startHeartbeat(directoryGUID,urls);
		return(d);
	}
	

	
	
	@Test
	public void testBuildModel(){
		workingPort = testPortPlusPlus();
		Directory d = startADirectory();
		
		startAWebServer(workingPort,d);
		
		/* Start a pool where we fetch a configuration */
		String configFileName = "src/edu/uci/ics/luci/cacophony/CNodeTest.cacophony.c_node_pool.fetch_config.properties";
		XMLPropertiesConfiguration config = null;
		/*
		try {
			config = new XMLPropertiesConfiguration(configFileName);
		} catch (ConfigurationException e) {
			fail("Unable to use requested configuration file:"+configFileName+"\n"+e);
		}
		CNodePool cNPool = new CNodePool(new KyotoCabinet());
		cNPool.launchCNodePool(config,null,null,null);
		assertTrue(cNPool.getPoolSize() == 1);
		cNPool.setQuitting(true);
		cNPool = null;
		*/
		
		/* Start a pool where we load nodes*/
		configFileName = "src/edu/uci/ics/luci/cacophony/CNodeTest.cacophony.c_node_pool.load_nodes.properties";
		config = null;
		try {
			config = new XMLPropertiesConfiguration(configFileName);
		} catch (ConfigurationException e) {
			fail("Unable to use requested configuration file:"+configFileName+"\n"+e);
		}
		CNodePool cNPool = new CNodePool(new KyotoCabinet<String,CNode>());
		cNPool.launchCNodePool(config,null,null,null);
		Globals.getGlobals().addQuittables(cNPool);
		assertTrue(cNPool.getPoolSize() >= 1);
		
		//new PopUpWindow("Click To Stop Test: "+this.getClass().getCanonicalName());
	}
	
	
	@Test
	public void makeAWebServer(){
		workingPort = testPortPlusPlus();
		
		Directory d = new Directory();
		Globals.getGlobals().addQuittables(d);
		
		WebServer ws = startAWebServer(workingPort,d);
		Globals.getGlobals().addQuittables(ws);
		
		//new PopUpWindow("Click To Stop Test: "+this.getClass().getCanonicalName());
	}
	
	@Test
	public void testCalendarAssumptions(){
		long jan012013 = 1357012799000L; // Tuesday 1/1/2013 03:59:59 GMT;
		//Globals.setGlobals(new GlobalsTest());
		Calendar c = Globals.getGlobals().getCalendar(null);
		
		c.setTimeInMillis(jan012013);
		assertEquals(Calendar.TUESDAY,c.get(Calendar.DAY_OF_WEEK));
		
		c.setTimeInMillis(CNode.transformTimeForCalendar(0, jan012013));
		assertEquals(Calendar.MONDAY,c.get(Calendar.DAY_OF_WEEK));
		
		c.setTimeInMillis(CNode.transformTimeForCalendar(-11, jan012013));
		assertEquals(Calendar.MONDAY,c.get(Calendar.DAY_OF_WEEK));
		
		c.setTimeInMillis(CNode.transformTimeForCalendar(0, jan012013 + CNode.ONE_MINUTE));
		assertEquals(Calendar.TUESDAY,c.get(Calendar.DAY_OF_WEEK));
		
		c.setTimeInMillis(jan012013);
		assertEquals(3*60+59, CNode.calculateMinutesSinceMidnight(c));
		
		c.setTimeInMillis(CNode.transformTimeForCalendar(0, jan012013));
		assertEquals(23*60+59, CNode.calculateMinutesSinceMidnight(c));
		
		c.setTimeInMillis(CNode.transformTimeForCalendar(0, jan012013 + CNode.ONE_MINUTE));
		assertEquals(0, CNode.calculateMinutesSinceMidnight(c));
		
		
		
	}

}
