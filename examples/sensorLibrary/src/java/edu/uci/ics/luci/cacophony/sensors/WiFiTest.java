package edu.uci.ics.luci.cacophony.sensors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map.Entry;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.datastructure.MapComparable;
import edu.uci.ics.luci.utility.datastructure.Pair;

public class WiFiTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Globals.setGlobals(new SensorLibraryGlobals());
	}

	@After
	public void tearDown() throws Exception {
		Globals.getGlobals().setQuitting(true);
	}
	
	
	@Test
	public void testSensingAvailable() {
		WiFi sensor = WiFi.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}

	@Test
	public void testGetAllAPMAC() {
		WiFi sensor = WiFi.getSensor();
		MapComparable<Pair<String, String>, Integer> answer = sensor.getAllAPMAC();
		assertTrue(answer != null);
		WiFi.getLog().debug("multi wifi is: "+answer);
	}

	@Test
	public void testGetAPSSID() {
		WiFi sensor = WiFi.getSensor();
		Pair<Pair<String, String>, Integer> answer = sensor.getAPMAC();
		if(answer == null){
			fail("If your wifi is turned off, then you'll get this error, otherwise it should pass");
		}
		WiFi.getLog().debug("single wifi is: "+answer);
	}
	
	@Test
	public void testMatchingAPSSID() {
		WiFi sensor = WiFi.getSensor();
		MapComparable<Pair<String, String>, Integer> many = sensor.getAllAPMAC();
		if(many == null){
			fail("Can't scan Wifi");
		}
		Pair<Pair<String, String>, Integer> single = sensor.getAPMAC();
		if(single == null){
			fail("If your wifi is turned off, then you'll get this error, otherwise it should pass");
		}
		
		boolean match = false;
		for(Entry<Pair<String, String>, Integer> ap:many.entrySet()){
			if(ap.getKey().getFirst().equals(single.getFirst().getFirst())){
				if(ap.getKey().getSecond().equals(single.getFirst().getSecond())){
					match = true;
				}
				else{
					//System.err.println(""+ap.getKey().getSecond()+" != "+single.getFirst().getSecond());
				}
			}
			else{
				//System.err.println(""+ap.getKey().getFirst()+" != "+single.getFirst().getFirst());
			}
		}
		assertTrue(match);
	}

}
