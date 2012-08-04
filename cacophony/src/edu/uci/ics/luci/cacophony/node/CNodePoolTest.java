package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quub.Globals;
import com.quub.util.Pair;
import com.quub.webserver.AccessControl;
import com.quub.webserver.HandlerAbstract;
import com.quub.webserver.RequestHandlerFactory;
import com.quub.webserver.WebServer;
import com.quub.webserver.WebUtil;
import com.quub.webserver.handlers.HandlerFileServer;
import com.quub.webserver.handlers.HandlerVersion;

import edu.uci.ics.luci.cacophony.CacophonyGlobals;
import edu.uci.ics.luci.cacophony.directory.Directory;
import edu.uci.ics.luci.cacophony.directory.api.HandlerDirectoryNamespace;
import edu.uci.ics.luci.cacophony.directory.api.HandlerDirectoryServers;
import edu.uci.ics.luci.cacophony.directory.api.HandlerNodeAssignment;
import edu.uci.ics.luci.cacophony.directory.api.HandlerNodeCheckin;
import edu.uci.ics.luci.cacophony.directory.api.HandlerNodeList;
import edu.uci.ics.luci.cacophony.directory.api.HandlerShutdown;

public class CNodePoolTest {

	private static int testPort = 9020;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private WebServer ws = null;
	Map<String, Class<? extends HandlerAbstract>> requestHandlerRegistry;
	private Directory d;

	@Before
	public void setUp() throws Exception {
		d = Directory.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		if(d != null){
			d.setQuitting(true);
		}
	}
	
	
	private void startAWebServer() {
		testPort++;
		try {
			requestHandlerRegistry = new HashMap<String, Class<? extends HandlerAbstract>>();
			requestHandlerRegistry.put("",HandlerVersion.class);
			requestHandlerRegistry.put("version",HandlerVersion.class);
			requestHandlerRegistry.put("servers",HandlerDirectoryServers.class);
			requestHandlerRegistry.put("nodes",HandlerNodeList.class);
			requestHandlerRegistry.put("node_assignment",HandlerNodeAssignment.class);
			requestHandlerRegistry.put("node_checkin",HandlerNodeCheckin.class);
			requestHandlerRegistry.put("namespace",HandlerDirectoryNamespace.class);
			requestHandlerRegistry.put("shutdown",HandlerShutdown.class);
			requestHandlerRegistry.put(null,HandlerFileServer.class);
			
			CacophonyGlobals.resetGlobals();
			CacophonyGlobals g = CacophonyGlobals.getGlobals();
			g.setTesting(true);
			RequestHandlerFactory factory = new RequestHandlerFactory(g, requestHandlerRegistry);
			ws = new WebServer(g, factory, null, testPort, false, new AccessControl());
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
			params.put("version", Globals.getGlobals().getVersion());

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
	
	private void startADirectory(){
		
		/* Load up the data */
		String configFileName="src/edu/uci/ics/luci/cacophony/DirectoryTest.cacophony.directory.properties";
		Directory.launchDirectory(configFileName);
		
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
		Pair<Long, String> p = new Pair<Long,String>(0L,url+":"+testPort);
		List<Pair<Long, String>> urls = new ArrayList<Pair<Long,String>>();
		urls.add(p);
			
		/* Start reporting heartbeats */
		String namespace = "test.waitscout.com";
		Directory.getInstance().setDirectoryNamespace(namespace);
		Directory.getInstance().startHeartbeat(directoryGUID,urls);
	}
	
	private void stopADirectory(){
		Directory.getInstance().setQuitting(true);
	}
	
	@Test
	public void testCNodePoolCreation() {
		startAWebServer();
		startADirectory();
		
		
		CNodePool cNPool = CNodePool.launchCNodePool("src/edu/uci/ics/luci/cacophony/CNodeTest.cacophony.c_node_pool.properties");
		assertTrue(cNPool != null);
		
		stopADirectory();
		stopAWebServer();
	}

}
