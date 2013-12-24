package edu.uci.ics.luci.cacophony.server.responder;

import static org.junit.Assert.fail;
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

public class ResponderCapabilitiesTest {

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
	public void testResponderCapabilities() {
		try{
			/*Start a cNode Server that should respond to a capability query by default */
			CNodeServer cNodeServer = new CNodeServer(CNodeServerTest.makeARandomP2PServerAddress(),3);
			cNodeServer.start();
			
			/* Make an object to send messages to the server and load 3 configurations */
			{
				String testName1 = cNodeServer.getServerName()+".01";
			
				/* Prep to receive responses from the server*/
				P2PSinkTest p2pSinkTest = new P2PSinkTest(cNodeServer);
				/* Set up the reponse we expect to get */
				p2pSinkTest.addPassPhrase("{\"responses\":[\"c_node_01:OK\",\"c_node_02:OK\",\"c_node_03:OK\"]}");
				
				P2PInterface p2p = new P2PInterface(testName1, p2pSinkTest);
				p2p.start();
				
				/* Load a configuration */
				JSONObject request = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(testName1,testName1);
				p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
				
				/* Wait for a response */
				P2PSinkTest.waitForResponse(p2pSinkTest);
				
			
				p2pSinkTest.addPassPhrase("{\"responses\":[{\"c_nodes\":[\"c_node_03\",\"c_node_01\",\"c_node_02\"],\"server_capacity\":\"3\",\"server_capabilities\":{\"predictors\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderPredictors\",\"load_configurations\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoader\",\"list_c_nodes\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderListCNodes\",\"capabilities\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"null\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"shutdown\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderShutdown\"}}]}");
				request.put("request","capabilities");
				request.remove("data");
				p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
				
				/* Wait for a response */
				P2PSinkTest.waitForResponse(p2pSinkTest);
				
				if(p2pSinkTest.getFail() != null){
					fail(p2pSinkTest.getFail());
				}
			
				//request.put("request","shutdown");
				//request.remove("data");
				//p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
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
		}
		catch(RuntimeException e){
			System.err.println(e.getMessage());
			fail("This should not fail."+e);
		}
	}

}
