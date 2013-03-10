package edu.uci.ics.luci.cacophony.sensors;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;

public class PowerSourceTest {

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
		PowerSource sensor = PowerSource.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}

	@Test
	public void testGet() {
		PowerSource ps = PowerSource.getSensor();
		boolean wall = ps.sensePowerSource().equals(PowerSourceEnum.WALL);
		boolean battery = ps.sensePowerSource().equals(PowerSourceEnum.BATTERY);
		assertTrue(battery || wall);
		
		PowerSource.getLog().debug("Power Source is:"+ps.sensePowerSource());
	}
	
	@Test
	public void testDoubleGet() {
		PowerSource ps1 = PowerSource.getSensor();
		PowerSource ps2 = PowerSource.getSensor();
		assertTrue(ps1 == ps2);
		assertEquals(ps1.sensePowerSource(),ps2.sensePowerSource());
		
		
		/* Make sure ps2 continues to work */
		boolean wall = ps2.sensePowerSource().equals(PowerSourceEnum.WALL);
		boolean battery = ps2.sensePowerSource().equals(PowerSourceEnum.BATTERY);
		assertTrue(battery || wall);
	}
	
	@Test
	public void testToFromInt() {
		PowerSource ps = PowerSource.getSensor();
		boolean wall = PowerSourceEnum.fromInteger(ps.sensePowerSource().toInteger()).equals(PowerSourceEnum.WALL);
		boolean battery = PowerSourceEnum.fromInteger(ps.sensePowerSource().toInteger()).equals(PowerSourceEnum.BATTERY);
		assertTrue(battery || wall);
		
		PowerSource.getLog().debug("Power Source is:"+ps.sensePowerSource());
	}
	
	
	@Test
	public void testEnum() {
		for(PowerSourceEnum e: PowerSourceEnum.values()){
			assertTrue(PowerSourceEnum.fromInteger(e.toInteger()).equals(e));
			assertTrue(PowerSourceEnum.fromString(e.toString()).equals(e));
		}
		assertTrue(PowerSourceEnum.fromInteger(null)== null);
		assertTrue(PowerSourceEnum.fromString(null)== null);
	}

}
