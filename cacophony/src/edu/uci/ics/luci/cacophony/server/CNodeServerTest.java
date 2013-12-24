package edu.uci.ics.luci.cacophony.server;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;

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
	}

	@After
	public void tearDown() throws Exception {
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
			CNodeServer cNodeServer = new CNodeServer(makeARandomP2PServerAddress(),5);
			cNodeServer.start();
			
			/* Make an interface to send messages to the server to test it */
			String testName1 = cNodeServer.getServerName()+"01";
			P2PSinkTest p2pSinkTest = new P2PSinkTest(cNodeServer);
			p2pSinkTest.addPassPhrase("{\"responses\":[\"c_node_01:OK\",\"c_node_02:OK\",\"c_node_03:OK\"]}");
			P2PInterface p2p = new P2PInterface(testName1, p2pSinkTest);
			p2p.start();
			JSONObject request = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(testName1, testName1);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/*Wait for response to come in*/
			while(p2pSinkTest.getNumberOfPassPhrases() > 0){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			
			/* Constructed data */
			JSONObject data = (JSONObject) request.get("data");
			JSONArray configurations = (JSONArray) data.get("configurations");
			JSONObject configuration0 = (JSONObject) configurations.get(0);
			JSONObject configuration1 = (JSONObject) configurations.get(1);
			JSONObject configuration2 = (JSONObject) configurations.get(2);
			String cNode0 = (String) configuration0.get("c_node_name");
			String cNode1 = (String) configuration1.get("c_node_name");
			String cNode2 = (String) configuration2.get("c_node_name");
			
			/* Received data */
			JSONObject d1 = cNodeServer.getCNodes().get(cNode0).getConfiguration().toJSONObject();
			JSONObject r1 = configuration0;
			JSONObject d2 = cNodeServer.getCNodes().get(cNode1).getConfiguration().toJSONObject();
			JSONObject r2 = configuration1;
			JSONObject d3 = cNodeServer.getCNodes().get(cNode2).getConfiguration().toJSONObject();
			JSONObject r3 = configuration2;
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
