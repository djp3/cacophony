package edu.uci.ics.luci.cacophony.sensors;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;

public class UIActivityTest {

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
		UIActivity sensor = UIActivity.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}

	@Test
	public void testSenseUIActivity() {
		UIActivity sensor = UIActivity.getSensor();
		Double senseUIActivity = sensor.senseUIActivity();
		assertTrue(senseUIActivity > 0d);
		UIActivity.getLog().debug("Current UI Activity is :"+senseUIActivity);
	}

}
