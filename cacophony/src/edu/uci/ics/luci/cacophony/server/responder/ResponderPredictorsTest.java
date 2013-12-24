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
import edu.uci.ics.luci.p2pinterface.P2PInterface;
import edu.uci.ics.luci.p2pinterface.P2PSink;

public class ResponderPredictorsTest {

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
	
	static class P2PSinkTest implements P2PSink{
		
		private CNodeServer cns;
		public String fail = null;
		private List<String> pass = new ArrayList<String>();

		public P2PSinkTest(CNodeServer cNodeServer) {
			this.cns = cNodeServer;
		}
		
		public void addPassPhrase(String phrase){
			pass.add(phrase);
		}
		

		/**
		 *  This function decodes the incoming messages based on knowing the conventions of the sender.
		 *  Namely that a "null" message element type means the element is UTF-8 encoded bytes and
		 *  there are no other payloads.
		 * @return
		 */
		private String incomingHelper(Map<String, byte []> map){
			
			for(Entry<String, byte[]> e :map.entrySet()){
				String key = e.getKey();
				if((key == null) || (key.equals(""))){
					try {
						return new String(e.getValue(),"UTF-8");
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}
			}
			return null;
		}
		
		public void incoming(Map<String, byte[]> map) {
			String deleteUs = null;
			String incomingMessage = incomingHelper(map);
			boolean ok = false;
			for(String s:pass){
				if(s.equals(incomingMessage)){
					deleteUs = s;
					ok = true;
				}
			}
			if(deleteUs != null){
				pass.remove(deleteUs);
			}
			if(!ok){
				this.cns.stop();
				fail = "Received an unexpected response:\n"+incomingMessage;
			}
		}
	}

	@Test
	public void testResponderCapabilities() {
		try{
			/* Set up the server that we are testing */
			CNodeServer cNodeServer = new CNodeServer(3);
			cNodeServer.start();
			
			/* Make an interface to send messages to the server and load 3 configurations */
			String testName1 = cNodeServer.getServerName()+"01";
			P2PSinkTest p2pSinkTest = new P2PSinkTest(cNodeServer);
			p2pSinkTest.addPassPhrase("{\"responses\":[\"OK:c_node_01\",\"OK:c_node_02\",\"OK:c_node_03\"]}");
			P2PInterface p2p = new P2PInterface(testName1, p2pSinkTest);
			p2p.start();
			JSONObject request = CNodeServerTest.makeLoadConfigurationRequest(testName1);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			request.put("request","list_c_nodes");
			JSONObject data = new JSONObject();
			data.put("c_node","c_node_01");
			request.remove("data");
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Send the request we are testing */
			request.put("request","predictors");
			data = new JSONObject();
			data.put("c_node","c_node_01");
			request.put("data",data);
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/* Shutdown the server*/
			request.put("request","shutdown");
			request.remove("data");
			p2p.sendMessage(cNodeServer.getServerName(), request.toJSONString(JSONStyle.LT_COMPRESS));
			
			/*Wait for the server to shutdown */
			synchronized(cNodeServer.getQuittingMonitor()){
				while(!cNodeServer.isQuitting()){
					try {
						cNodeServer.getQuittingMonitor().wait();
					} catch (InterruptedException e) {
					}
				}
			}
			
			/* Make sure that we didn't get an unexpected message */
			if(p2pSinkTest.fail != null){
				fail(p2pSinkTest.fail);
			}
		}
		catch(RuntimeException e){
			System.err.println(e.getMessage());
			fail("This should not fail."+e);
		}
	}

}
