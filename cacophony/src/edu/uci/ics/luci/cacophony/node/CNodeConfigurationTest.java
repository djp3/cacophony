package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.server.CNodeServerTest;
import edu.uci.ics.luci.cacophony.server.responder.ResponderConfigurationLoaderTest;

public class CNodeConfigurationTest {

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
			new CNodeConfiguration(null);
			fail("Expected an exception");
		}
		catch(IllegalArgumentException e){
			//expected
		}
	}

	@Test
	public void test() {
		String address = CNodeServerTest.makeARandomP2PServerAddress();
		JSONObject configRequest = ResponderConfigurationLoaderTest.makeLoadConfigurationRequest(address, address);
		JSONObject data = (JSONObject) configRequest.get("data");
		JSONArray configurations = (JSONArray) data.get("configurations");
		JSONObject configuration = (JSONObject) configurations.get(0);
		CNodeConfiguration cnc = new CNodeConfiguration(configuration);
		
		cnc.setMyPath(cnc.getMyPath());
		assertEquals(cnc.getMyPath(),(String)configuration.get("c_node_name"));
		
		cnc.setPredictors(cnc.getPredictors());
		for(int i=0; i< ((JSONArray)configuration.get("predictors")).size(); i++){
			String predictor = (String)((JSONArray)configuration.get("predictors")).get(i);
			
			CNodeAddress cna = new CNodeAddress(predictor);
			assertTrue(cnc.getPredictors().contains(cna));
		}
		
		JSONObject target = (JSONObject) configuration.get("target");
		cnc.setTargetURL(cnc.getTargetURL());
		assertEquals(cnc.getTargetURL(),(String)target.get("url"));
		
		cnc.setTargetFormat(cnc.getTargetFormat());
		assertEquals(cnc.getTargetFormat(),(String)target.get("format"));
		
		cnc.setTargetRegEx(cnc.getTargetRegEx());
		assertEquals(cnc.getTargetRegEx(),(String)target.get("reg_ex"));
		
		cnc.setTargetPathExpression(cnc.getTargetPathExpression());
		assertEquals(cnc.getTargetPathExpression(),(String)target.get("path_expression"));
		
		JSONObject translator = (JSONObject) target.get("translator");
		cnc.setTranslator(cnc.getTranslator());
		assertEquals(cnc.getTranslator().getClass().getCanonicalName(),translator.get("classname"));
		
		cnc.setTranslatorOptions(cnc.getTranslatorOptions());
		assertEquals(cnc.getTranslatorOptions(),translator.get("options"));
		
		JSONObject polling = (JSONObject) configuration.get("polling");
		cnc.setPollingMinInterval(cnc.getPollingMinInterval());
		assertEquals(cnc.getPollingMinInterval(),Long.valueOf((String)polling.get("min_interval")));
		
		cnc.setPollingPolicy(cnc.getPollingPolicy());
		assertEquals(cnc.getPollingPolicy().toString(),polling.get("policy"));
		
		assertEquals(cnc.toJSONObject(),configuration);
		
	}

}
