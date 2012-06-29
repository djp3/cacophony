package edu.uci.ics.luci.cacophony;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DirectoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}



	private Directory d;

	@Before
	public void setUp() throws Exception {
		d = Directory.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		d.setQuitting(true);
	}

	@Test
	public void testStartHeartbeat() {
		d.startHeartbeat(0L, 500L);
		try{
			String url = null;
			try {
				url = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				url ="127.0.0.1";
			}
			Long heartbeat01 = d.getHeartbeat(url);
			assertTrue(heartbeat01 != null);
			Thread.sleep(750);
			Long heartbeat02 = d.getHeartbeat(url);
			assertTrue(heartbeat02 != null);
			assertTrue(!heartbeat01.equals(heartbeat02));
		} catch (InterruptedException e) {
			fail(e.toString());
		}
	}
	
	
	
	@Test
	public void testGetServers() {
		d.startHeartbeat(0L, 500L);
		try{
			String me = InetAddress.getLocalHost().getHostAddress();
			Map<String, JSONObject> list = d.getServers();
			for(Entry<String,JSONObject> e : list.entrySet()){
				JSONObject jsonData = null;
				try{
					jsonData = e.getValue();
					if(e.getKey().equals(me)){
						assertTrue(System.currentTimeMillis()- jsonData.getLong("heartbeat") < Directory.FIVE_MINUTES);
					}
					else{
						assertTrue(System.currentTimeMillis() - jsonData.getLong("heartbeat") > Directory.FIVE_MINUTES);
					}
				} catch (JSONException e1) {
					fail("Bad JSON Data in Cassandra ring:\n"+jsonData.toString()+"\n"+e1);
				}
			}
		} catch (UnknownHostException e) {
			fail(e.toString());
		}
	}
	
	@Test
	public void testOpenProperties() {
	
		/* Get Directory properties */
		String directoryPropertiesLocation = "cacophony.directory.properties";
		try {
			XMLPropertiesConfiguration config;
			config = new XMLPropertiesConfiguration(directoryPropertiesLocation);
			assertEquals("value",config.getString("test"));
		} catch (ConfigurationException e1) {
			fail("Problem loading configuration from:"+directoryPropertiesLocation+"\n"+e1);
		}
	}

}
