package edu.uci.ics.luci.cacophony.sensors;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;

public class LightTest {

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
		Light sensor = Light.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}

	@Test
	public void testSenseAmbientLight() {
		Light sensor = Light.getSensor();
		assertTrue(sensor.senseAmbientLight() != null);
		Light.getLog().info("light is: "+sensor.senseAmbientLight());
	}

	@Test
	public void testSenseAmbientLightBoth() {
		Light sensor = Light.getSensor();
		List<Integer> i = sensor.senseAmbientLightBoth();
		assertTrue(i.size() == 2);
		assertTrue(sensor.senseAmbientLightBoth() != null);
		Light.getLog().info("light is: "+i.get(0)+","+i.get(1));
		
	}
	

	@Test
	public void testDoubleGet() {
		Light i1 = Light.getSensor();
		Light i2 = Light.getSensor();
		assertTrue(i1 == i2);
		assertTrue(Math.abs(i1.senseAmbientLight() - i2.senseAmbientLight()) < 100);
		
		
		/* Make sure ps2 continues to work */
		i2.senseAmbientLight();
		long foo = i2.senseAmbientLight();
		/* If this fails then you were idle for longer than 10 seconds when the test was run */
		assertTrue(foo > 0);
		
	}

}
