package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.server.CNodeServerTest;
import edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoaderTest;

public class CNodeTest {

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
	public void test() {
		try{
			CNode cn = new CNode(null);
			assertTrue(cn.getConfiguration() == null);
			
			CNodeConfiguration cnn = null;
			try{
				JSONObject js = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(CNodeServerTest.makeARandomP2PServerAddress(), "p2p://foo/bar");
				JSONObject data = (JSONObject) js.get("data");
				JSONArray configurations = (JSONArray) data.get("configurations");
				JSONObject configuration = (JSONObject) configurations.get(0);
				cnn = new CNodeConfiguration(configuration);
				cn.setConfiguration(cnn);
				assertTrue(cn.getConfiguration() == cnn);
			}
			catch(IllegalArgumentException e){
				fail("This shouldn't throw an exception");
			}
			
			Thread t = new Thread(cn);
			t.setDaemon(false); //Force a clean shutdown
			t.start();
		}
		catch(RuntimeException e){
			fail("Should not throw an exception:"+e);
		}
	}

}
