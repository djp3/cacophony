package edu.uci.ics.luci.cacophony.server.responder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.cacophony.node.CNode;
import edu.uci.ics.luci.cacophony.node.PollingPolicy;
import edu.uci.ics.luci.cacophony.server.CNodeServer;
import edu.uci.ics.luci.cacophony.server.CNodeServerTest;
import edu.uci.ics.luci.cacophony.server.ConfigurationsDAO;

public class ResponderConfigurationLoaderTest {

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

//	{
//	    "request": "load_configurations",
//	    "data": {
//	        "configurations": [
//	            {
//	                "predictors": [
//	                    "p2p://edu.uci.ics.luci.cacophony.test.886800950927706967001/c_node_03"
//	                ],
//	                "c_node_name": "c_node_01",
//	                "target": {
//	                    "translator": {
//	                        "classname": "edu.uci.ics.luci.cacophony.node.TranslatorGeneric",
//	                        "options": {
//	                            "a": "thing"
//	                        }
//	                    },
//	                    "path_expression": "/*/*",
//	                    "reg_ex": "temp=(.*)",
//	                    "format": "html",
//	                    "url": "http://cnn.com"
//	                },
//	                "polling": {
//	                    "min_interval": "5000",
//	                    "policy": "on_change"
//	                }
//	            },
//	            {
//	                "predictors": [
//	                    "p2p://edu.uci.ics.luci.cacophony.test.886800950927706967001/c_node_01"
//	                ],
//	                "c_node_name": "c_node_02",
//	                "target": {
//	                    "translator": {
//	                        "classname": "edu.uci.ics.luci.cacophony.node.TranslatorGeneric",
//	                        "options": {
//	                            "b": "other thing"
//	                        }
//	                    },
//	                    "path_expression": "/*/*",
//	                    "reg_ex": "temp=(.*)",
//	                    "format": "html",
//	                    "url": "http://cnn.com"
//	                },
//	                "polling": {
//	                    "min_interval": "5000",
//	                    "policy": "on_change"
//	                }
//	            },
//	            {
//	                "predictors": [
//	                    "p2p://edu.uci.ics.luci.cacophony.test.886800950927706967001/c_node_02"
//	                ],
//	                "c_node_name": "c_node_03",
//	                "target": {
//	                    "translator": {
//	                        "classname": "edu.uci.ics.luci.cacophony.node.TranslatorGeneric",
//	                        "options": {
//	                            "c": "yet another thing"
//	                        }
//	                    },
//	                    "path_expression": "/*/*",
//	                    "reg_ex": "temp=(.*)",
//	                    "format": "html",
//	                    "url": "http://cnn.com"
//	                },
//	                "polling": {
//	                    "min_interval": "5000",
//	                    "policy": "on_change"
//	                }
//	            }
//	        ]
//	    },
//	    "from": "edu.uci.ics.luci.cacophony.test.886800950927706967001"
//	}
	public static JSONObject makeLoadConfigurationRequest(String myServerName,String from) {
		JSONObject request = new JSONObject();
		request.put("request", "load_configurations");
		request.put("from", from);
		
		JSONArray cnodes = new JSONArray();
		
		String cnodeID_1 = "c_node_01";
		String cnodeID_2 = "c_node_02";
		String cnodeID_3 = "c_node_03";
		
		/*** c_node_01 ***/		
		JSONObject configuration = new JSONObject();
		configuration.put("c_node_name", "c_node_01");
		
		JSONArray predictors = new JSONArray();
		predictors.add("p2p://"+myServerName+"/c_node_03");
		configuration.put("predictors", predictors);
		
		JSONArray features = new JSONArray();
		JSONObject feature = new JSONObject();
		feature.put("ID", "ID of feature 1");
		feature.put("name", "feature 1");
		feature.put("url", "http://www.cnn.com");
		feature.put("format", "html");
		feature.put("path_expression", "//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]");
		feature.put("reg_ex", "(.*)");
		JSONObject featureTranslator = new JSONObject();
		featureTranslator.put("classname", "edu.uci.ics.luci.cacophony.node.TranslatorString");
		JSONObject featureOptions = new JSONObject();
		featureOptions.put("a", "thing");
		featureTranslator.put("options", featureOptions);
		feature.put("translator", featureTranslator);
		features.add(feature);
		configuration.put("features", features);
		
		JSONObject target = new JSONObject();
		target.put("ID", "ID of target");
		target.put("name", "name of target");
		target.put("url","http://www.cnn.com");
		target.put("format","html");
		//target.put("path_expression", "//*[@id=\\\"cnn_ftrcntntinner\\\"]/div[9]/div[1]/text()[2]");
		//target.put("reg_ex", "temp=(.*)");
		target.put("path_expression", "//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]");
		target.put("reg_ex", "(.*)");
		JSONObject targetTranslator = new JSONObject();
		targetTranslator.put("classname","edu.uci.ics.luci.cacophony.node.TranslatorString");
		JSONObject targetOptions = new JSONObject();
		targetOptions.put("a", "thing");
		targetTranslator.put("options",targetOptions);
		target.put("translator", targetTranslator);
		configuration.put("target", target);
		
		JSONObject polling = new JSONObject();
		polling.put("policy", PollingPolicy.ON_CHANGE.toString());
		polling.put("min_interval", "5000");
		configuration.put("polling", polling);
		
		JSONObject cnode = new JSONObject();
		cnode.put("ID", cnodeID_1);
		cnode.put("configuration", configuration);
		cnodes.add(cnode);
		
		
		/*** c_node_02 ***/
		configuration = new JSONObject();
		configuration.put("c_node_name", "c_node_02");
		
		predictors = new JSONArray();
		predictors.add("p2p://"+myServerName+"/c_node_01");
		configuration.put("predictors", predictors);
		
		features = new JSONArray();
		feature = new JSONObject();
		feature.put("ID", "ID of feature 1");
		feature.put("name", "feature 1");
		feature.put("url", "http://www.cnn.com");
		feature.put("format", "html");
		feature.put("path_expression", "//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]");
		feature.put("reg_ex", "(.*)");
		featureTranslator = new JSONObject();
		featureTranslator.put("classname", "edu.uci.ics.luci.cacophony.node.TranslatorString");
		featureOptions = new JSONObject();
		featureOptions.put("a", "thing");
		featureTranslator.put("options", featureOptions);
		feature.put("translator", featureTranslator);
		features.add(feature);
		configuration.put("features", features);
		
		target = new JSONObject();
		target.put("ID", "ID of target");
		target.put("name", "name of target");
		target.put("url","http://cnn.com");
		target.put("format","html");
		target.put("path_expression", "//*[@id=\\\"cnn_ftrcntntinner\\\"]/div[9]/div[1]/text()[2]");
		target.put("reg_ex", "temp=(.*)");
		targetTranslator = new JSONObject();
		targetTranslator.put("classname","edu.uci.ics.luci.cacophony.node.TranslatorString");
		targetOptions = new JSONObject();
		targetOptions.put("b", "other thing");
		targetTranslator.put("options",targetOptions);
		target.put("translator", targetTranslator);
		configuration.put("target", target);
		
		polling = new JSONObject();
		polling.put("policy", PollingPolicy.ON_CHANGE.toString());
		polling.put("min_interval", "5000");
		configuration.put("polling", polling);

		cnode = new JSONObject();
		cnode.put("ID", cnodeID_2);
		cnode.put("configuration", configuration);
		cnodes.add(cnode);
		
		
		/*** c_node_03 ***/
		configuration = new JSONObject();
		configuration.put("c_node_name", "c_node_03");
		
		predictors = new JSONArray();
		predictors.add("p2p://"+myServerName+"/c_node_02");
		configuration.put("predictors", predictors);
		
		features = new JSONArray();
		feature = new JSONObject();
		feature.put("ID", "ID of feature 1");
		feature.put("name", "feature 1");
		feature.put("url", "http://www.cnn.com");
		feature.put("format", "html");
		feature.put("path_expression", "//*[@id=\"cnn_ftrcntntinner\"]/div[9]/div[1]/text()[2]");
		feature.put("reg_ex", "(.*)");
		featureTranslator = new JSONObject();
		featureTranslator.put("classname", "edu.uci.ics.luci.cacophony.node.TranslatorString");
		featureOptions = new JSONObject();
		featureOptions.put("a", "thing");
		featureTranslator.put("options", featureOptions);
		feature.put("translator", featureTranslator);
		features.add(feature);
		configuration.put("features", features);
		
		target = new JSONObject();
		target.put("ID", "ID of target");
		target.put("name", "name of target");
		target.put("url","http://cnn.com");
		target.put("format","html");
		target.put("path_expression", "//*[@id=\\\"cnn_ftrcntntinner\\\"]/div[9]/div[1]/text()[2]");
		target.put("reg_ex", "temp=(.*)");
		targetTranslator = new JSONObject();
		targetTranslator.put("classname","edu.uci.ics.luci.cacophony.node.TranslatorString");
		targetOptions = new JSONObject();
		targetOptions.put("c", "yet another thing");
		targetTranslator.put("options",targetOptions);
		target.put("translator", targetTranslator);
		configuration.put("target", target);
		
		polling = new JSONObject();
		polling.put("policy", PollingPolicy.ON_CHANGE.toString());
		polling.put("min_interval", "5000");
		configuration.put("polling", polling);

		cnode = new JSONObject();
		cnode.put("ID", cnodeID_3);
		cnode.put("configuration", configuration);
		cnodes.add(cnode);
		
		JSONObject wrapper = new JSONObject();
		wrapper.put("c_nodes", cnodes);
		request.put("data", wrapper);
		
		return request;
	}
	

	@Test
	public void testConstructor() {
		CNodeServer cns = new CNodeServer();
		
		ResponderConfigurationLoader rcl = new ResponderConfigurationLoader(cns);
		assertTrue(rcl.getParentServer() == cns);
	}
	
	
	@Test
	public void testDegenerate() {
		ResponderConfigurationLoader rcl = null;
		
		try{
			new ResponderConfigurationLoader(null);
			fail("Should throw an exception");
		}
		catch(IllegalArgumentException e){
			//Expected
		}
		
		
		
		String myServerName = CNodeServerTest.makeARandomP2PServerAddress();
		CNodeServer cns = new CNodeServer(myServerName);
		
		rcl = new ResponderConfigurationLoader(cns);
		Map<String,CNode> cNodes = new HashMap<String,CNode>();
		JSONObject jo = (JSONObject) makeLoadConfigurationRequest(myServerName, "p2p://me").get("data");
		jo.put("c_nodes", "Hello There!");
		
		rcl.handle(jo,cNodes);
		
		assertTrue(rcl.constructResponse().get("errors") != null);
		assertTrue(rcl.constructResponse().get("errors").toString().contains("Unable to make the \\\"c_nodes\\\""));
		
		
		
		
		rcl = new ResponderConfigurationLoader(cns);
		jo.put("c_nodes", null);
		rcl.handle(jo,cNodes);
		
		assertTrue(rcl.constructResponse().get("errors") != null);
		assertTrue(rcl.constructResponse().get("errors").toString().contains("Unable to make the \\\"c_nodes\\\""));
		
		
		
		
		
		rcl = new ResponderConfigurationLoader(cns);
		JSONArray breakme = new JSONArray();
		breakme.add(10);
		breakme.add(20);
		jo.put("c_nodes", breakme);
		rcl.handle(jo,cNodes);
		
		assertTrue(rcl.constructResponse().get("errors") != null);
		assertTrue(rcl.constructResponse().get("errors").toString().contains("Unable to make the 0th c_node"));		
	}
	
	@Test
	public void testHandleWithThree() {
		String myServerName = CNodeServerTest.makeARandomP2PServerAddress();
		CNodeServer cns = new CNodeServer(myServerName);
		
		ResponderConfigurationLoader rcl = new ResponderConfigurationLoader(cns);
		
		Map<String,CNode> cNodes = new HashMap<String,CNode>();
		
		rcl.handle((JSONObject) makeLoadConfigurationRequest(myServerName, "p2p://me").get("data"), cNodes);
		assertTrue(rcl.constructResponse().get("errors") == null);
		assertTrue(((JSONArray)rcl.constructResponse().get("responses")).size() > 0 );
		JSONArray responses = (JSONArray)rcl.constructResponse().get("responses");
		JSONObject response1 = (JSONObject)responses.get(0);
		JSONObject response2 = (JSONObject)responses.get(1);
		JSONObject response3 = (JSONObject)responses.get(2);
		
		assertEquals(response1.get("status").toString(), "OK");
		assertEquals(response1.get("source_ID").toString(), "c_node_01");
		assertEquals(response2.get("status").toString(), "OK");
		assertEquals(response2.get("source_ID").toString(), "c_node_02");
		assertEquals(response3.get("status").toString(), "OK");
		assertEquals(response3.get("source_ID").toString(), "c_node_03");
		
	}

}
