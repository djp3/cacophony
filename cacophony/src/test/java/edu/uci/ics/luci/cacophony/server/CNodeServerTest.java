package edu.uci.ics.luci.cacophony.server;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoaderTest;
import edu.uci.ics.luci.p2pinterface.P2PInterface;

public class CNodeServerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ConfigurationsDAO.enableTestingMode();
	}

	@After
	public void tearDown() throws Exception {
		ConfigurationsDAO.disableTestingMode();
	}

	@Test
	public void testBasics() {
		CNodeServer cNodeServer = null;
		try{
			cNodeServer = new CNodeServer();
		}
		catch(Exception e){
			System.err.println(e.getMessage());
			fail("This should not fail."+e);
		}
		
		assertTrue(cNodeServer != null);
		
		try{
			cNodeServer.start();
		
		}
		catch(RuntimeException e){
			System.err.println(e.getMessage());
			fail("This should not fail."+e);
		}
	}
	
	

	static Random r = new Random();
	public static long makePositiveLong(){
		long ret;
		while((ret = r.nextLong())<0 );
		return ret;
	}
	
	public static String makeARandomP2PServerAddress(){
		return"edu.uci.ics.luci.cacophony.test."+makePositiveLong();
	}


	@Test
	public void testConfigurationLoad() {
		try{
			/* This is the object we are testing */
			CNodeServer cNodeServer = new CNodeServer(makeARandomP2PServerAddress());
			cNodeServer.start();
			
			/* Make an interface to send messages to the server to test it */
			String testName1 = cNodeServer.getServerName()+"01";
			P2PSinkTest p2pSinkTest = new P2PSinkTest(cNodeServer);
			p2pSinkTest.addPassPhrase("\\Q{\"responses\":[{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_01\"},{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_02\"},{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_03\"}]}\\E");
			P2PInterface p2p = new P2PInterface(testName1, p2pSinkTest);
			p2p.start();
			JSONObject request = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(testName1, testName1);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			String responsesString = P2PSinkTest.waitForResponse(p2pSinkTest);
			JSONObject responsesJSON = (JSONObject)JSONValue.parse(responsesString);
			JSONArray responses = (JSONArray)responsesJSON.get("responses");
			JSONObject response0 = (JSONObject)responses.get(0);
			JSONObject response1 = (JSONObject)responses.get(1);
			JSONObject response2 = (JSONObject)responses.get(2);
			String ID0 = response0.get("clone_ID").toString();
			String ID1 = response1.get("clone_ID").toString();
			String ID2 = response2.get("clone_ID").toString();

			/* Constructed data */
			JSONObject data = (JSONObject)request.get("data");
			JSONArray cnodes = (JSONArray)data.get("c_nodes");
			JSONObject cnode0 = (JSONObject)cnodes.get(0);
			JSONObject cnode1 = (JSONObject)cnodes.get(1);
			JSONObject cnode2 = (JSONObject)cnodes.get(2);
			JSONObject config0 = (JSONObject)cnode0.get("configuration");
			JSONObject config1 = (JSONObject)cnode1.get("configuration");
			JSONObject config2 = (JSONObject)cnode2.get("configuration");
			
			/* Received data */
			JSONObject d1 = cNodeServer.getCNodes().get(ID0).getConfiguration().toJSONObject();
			JSONObject r1 = config0;
			JSONObject d2 = cNodeServer.getCNodes().get(ID1).getConfiguration().toJSONObject();
			JSONObject r2 = config1;
			JSONObject d3 = cNodeServer.getCNodes().get(ID2).getConfiguration().toJSONObject();
			JSONObject r3 = config2;
			assertTrue(!d1.equals(d2));
			assertTrue(!d2.equals(d3));
			assertTrue(!d3.equals(d1));
			assertTrue(!r1.equals(r2));
			assertTrue(!r2.equals(r3));
			assertTrue(!r3.equals(r1));
			
			assertTrue(d1.equals(r1));
			assertTrue(!d1.equals(r2));
			assertTrue(!d1.equals(r3));
			
			assertTrue(!d2.equals(r1));
			assertTrue(d2.equals(r2));
			assertTrue(!d2.equals(r3));
			
			assertTrue(!d3.equals(r1));
			assertTrue(!d3.equals(r2));
			assertTrue(d3.equals(r3));
			
			/* Shut down the server and wait for it */
			synchronized(cNodeServer.getQuittingMonitor()){
				while(!cNodeServer.isQuitting()){
					cNodeServer.stop();
					try {
						if(!cNodeServer.isQuitting()){
							cNodeServer.getQuittingMonitor().wait();
						}
					} catch (InterruptedException e) {
					}
				}
			}
		}
		catch(RuntimeException e){
			System.err.println(e.getMessage());
			fail("This should not fail."+e);
		}
	}

}
