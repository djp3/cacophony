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
import edu.uci.ics.luci.cacophony.server.ConfigurationsDAO;
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
		ConfigurationsDAO.enableTestingMode();
	}

	@After
	public void tearDown() throws Exception {
		ConfigurationsDAO.disableTestingMode();
	}
	
	

	@Test
	public void testResponderCapabilities() {
		try{
			/*Start a cNode Server that should respond to a capability query by default */
			CNodeServer cNodeServer = new CNodeServer(CNodeServerTest.makeARandomP2PServerAddress());
			cNodeServer.start();
			
			/* Make an object to send messages to the server and load 3 configurations */
			{
				String testName1 = cNodeServer.getServerName()+".01";
			
				/* Prep to receive responses from the server*/
				P2PSinkTest p2pSinkTest = new P2PSinkTest(cNodeServer);
				/* Set up the reponse we expect to get */
				p2pSinkTest.addPassPhrase("\\Q{\"responses\":[{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_01\"},{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_02\"},{\"status\":\"OK\",\"clone_ID\":\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"source_ID\":\"c_node_03\"}]}\\E");
				
				P2PInterface p2p = new P2PInterface(testName1, p2pSinkTest);
				p2p.start();
				
				/* Load a configuration */
				JSONObject request = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(testName1,testName1);
				p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
				
				/* Wait for a response */
				P2PSinkTest.waitForResponse(p2pSinkTest);
				
				p2pSinkTest.addPassPhrase("\\Q{\"responses\":[{\"c_nodes\":[\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\",\"\\E[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\Q\"],\"server_capacity\":\"\\E\\d+\\Q\",\"server_capabilities\":{\"load_configurations\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoader\",\"capabilities\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"configuration\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderConfiguration\",\"null\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderCapabilities\",\"shutdown\":\"edu.uci.ics.luci.cacophony.server.responder.ResponderShutdown\"}}]}\\E");
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
