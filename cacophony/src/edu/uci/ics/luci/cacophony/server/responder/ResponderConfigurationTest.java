package edu.uci.ics.luci.cacophony.server.responder;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.server.CNodeServer;
import edu.uci.ics.luci.cacophony.server.CNodeServerTest;
import edu.uci.ics.luci.cacophony.server.P2PSinkTest;
import edu.uci.ics.luci.p2pinterface.P2PInterface;
import edu.uci.ics.luci.p2pinterface.P2PSink;

public class ResponderConfigurationTest {

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
	public void testDegenerate() {

		try{
			new ResponderConfigurationLoader(null);
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			//Expected
		}
	}
	
	@Test
	public void testResponder() {
		
		try{
			/* Set up the server that we are testing */
			CNodeServer cNodeServer = new CNodeServer(CNodeServerTest.makeARandomP2PServerAddress(),3);
			cNodeServer.start();
			
			/* Make an interface to send messages to the server and load 3 configurations */
			String testName1 = cNodeServer.getServerName()+"01";
			P2PSinkTest p2pSinkTest = new P2PSinkTest(cNodeServer);
			p2pSinkTest.addPassPhrase("{\"responses\":[\"c_node_01:OK\",\"c_node_02:OK\",\"c_node_03:OK\"]}");
			P2PInterface p2p = new P2PInterface(testName1, p2pSinkTest);
			p2p.start();
			JSONObject request = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(testName1,testName1);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			P2PSinkTest.waitForResponse(p2pSinkTest);
			
			p2pSinkTest.addPassPhrase("{\"responses\":[{\"c_nodes\":[\"c_node_03\",\"c_node_01\",\"c_node_02\"],\"server_capacity\":\"3\",\"server_capabilities\":{\"load_configurations\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoader\",\"capabilities\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"configuration\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderConfiguration\",\"null\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"shutdown\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderShutdown\"}}]}");
			request.put("request","capabilities");
			request.remove("data");
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			P2PSinkTest.waitForResponse(p2pSinkTest);
			
			
			/* Test a bad request */
			p2pSinkTest.addPassPhrase("{\"errors\":[\"No \\\"data\\\" sent in the incoming JSON into a String\"]}");
			request.put("request","configuration");
			request.remove("data");
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			P2PSinkTest.waitForResponse(p2pSinkTest);
			
			
			/* Send the request we are testing */
			p2pSinkTest.addPassPhrase("{\"responses\":[{\"predictors\":[\"p2p://"+cNodeServer.getServerName()+"01/c_node_02\"],\"c_node_name\":\"c_node_03\",\"target\":{\"translator\":{\"classname\":\"edu.uci.ics.luci.cacophony.node.TranslatorGeneric\",\"options\":{\"c\":\"yet another thing\"}},\"path_expression\":\"/*/*\",\"reg_ex\":\"temp=(.*)\",\"format\":\"html\",\"url\":\"http://cnn.com\"},\"polling\":{\"min_interval\":\"5000\",\"policy\":\"ON_CHANGE\"}}]}");
			request.put("request","configuration");
			JSONObject data = new JSONObject();
			data.put("c_node","c_node_03");
			request.put("data",data);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Wait for a response */
			P2PSinkTest.waitForResponse(p2pSinkTest);
			
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
