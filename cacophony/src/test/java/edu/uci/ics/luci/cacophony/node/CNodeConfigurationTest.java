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
		JSONArray cnodes = (JSONArray)data.get("c_nodes");
		JSONObject cnode = (JSONObject)cnodes.get(0);
		JSONObject configuration = (JSONObject)cnode.get("configuration");
		CNodeConfiguration cnc = new CNodeConfiguration(configuration);
		
		cnc.setMyPath(cnc.getMyPath());
		assertEquals(cnc.getMyPath(),(String)configuration.get("c_node_name"));
		
		cnc.setPredictors(cnc.getPredictors());
		for(int i=0; i<((JSONArray)configuration.get("predictors")).size(); i++){
			String predictor = (String)((JSONArray)configuration.get("predictors")).get(i);
			
			CNodeAddress cna = new CNodeAddress(predictor);
			assertTrue(cnc.getPredictors().contains(cna));
		}
		
		cnc.setFeatures(cnc.getFeatures());
		JSONArray featuresJSON = (JSONArray)configuration.get("features");
		for(int i=0; i<(featuresJSON).size(); i++){
			JSONObject featureJSON = (JSONObject)featuresJSON.get(i);
			SensorConfig feature = new SensorConfig(featureJSON);
			
			assertTrue(cnc.getFeatures().contains(feature));
		}
		
		
		JSONObject targetJSON = (JSONObject)configuration.get("target");
		SensorConfig target = new SensorConfig(targetJSON);
		cnc.setTarget(target);
		
		assertEquals(cnc.getTarget().getURL(), (String)targetJSON.get("url"));
		assertEquals(cnc.getTarget().getFormat(), (String)targetJSON.get("format"));
		assertEquals(cnc.getTarget().getRegEx(), (String)targetJSON.get("reg_ex"));
		assertEquals(cnc.getTarget().getPathExpression(), (String)targetJSON.get("path_expression"));
		
		JSONObject translatorJSON = (JSONObject)targetJSON.get("translator");
		assertTrue(translatorJSON != null); // TODO: Is this actually a problem? It probably makes sense that only some sensors need translators
		assertEquals(cnc.getTarget().getTranslator().getClass().getCanonicalName(), (String)translatorJSON.get("classname"));
		assertEquals(cnc.getTarget().getTranslator().getClass().getCanonicalName(), (String)translatorJSON.get("classname"));
		assertEquals(cnc.getTarget().getTranslatorOptions(), translatorJSON.get("options"));

		
		JSONObject polling = (JSONObject) configuration.get("polling");
		cnc.setPollingMinInterval(cnc.getPollingMinInterval());
		assertEquals(cnc.getPollingMinInterval(),Long.valueOf((String)polling.get("min_interval")));
		
		cnc.setPollingPolicy(cnc.getPollingPolicy());
		assertEquals(cnc.getPollingPolicy().toString(),polling.get("policy"));
		
		assertEquals(cnc.toJSONObject(),configuration);		
	}
}
