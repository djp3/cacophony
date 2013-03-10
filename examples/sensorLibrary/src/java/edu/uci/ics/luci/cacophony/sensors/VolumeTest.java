package edu.uci.ics.luci.cacophony.sensors;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;

public class VolumeTest {

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
		Volume sensor = Volume.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}
	
	@Test
	public void testSenseVolume() {
		Volume sensor = Volume.getSensor();
		Double senseVolume = sensor.senseVolume();
		assertTrue(senseVolume >= 0d);
		Volume.getLog().info("Volume is currently: "+senseVolume);
	}

}
