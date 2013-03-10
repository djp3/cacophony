package edu.uci.ics.luci.cacophony.sensors;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;

public class IPAddressTest {

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
		IPAddress sensor = IPAddress.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}

	@Test
	public void testIpAddress() {
		IPAddress sensor = IPAddress.getSensor();
		String x = sensor.senseIPAddress();
		assertTrue(x != null);
		assertTrue(x.equals(sensor.senseIPAddress()));
		IPAddress.getLog().info("Last IP: "+x);
	}
	

	@Test
	public void testDoubleGet() {
		IPAddress i1 = IPAddress.getSensor();
		IPAddress i2 = IPAddress.getSensor();
		assertTrue(i1 == i2);
		assertEquals(i1.senseIPAddress(),i2.senseIPAddress());
		
		/* Make sure ps2 continues to work */
		i2.senseIPAddress();
		String foo = i2.senseIPAddress();
		/* If this fails then you were idle for longer than 10 seconds when the test was run */
		assertTrue(foo != null);
	}

}
