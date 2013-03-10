package edu.uci.ics.luci.cacophony.sensors;


import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;

public class IdleTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Environment.getInstance();
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
		Idle sensor = Idle.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}
	
	@Test
	public void testIdleSensor(){
		Idle sensor = Idle.getSensor();
		assertTrue(sensor.senseIdleTime() >= 0);
		try {
			long lastIdleTime = 10000;
			System.err.println("Hold still for 5 seconds");
			while(sensor.senseIdleTime() < 5000){
				long thisIdleTime = (sensor.getIdleTimeOut()-sensor.senseIdleTime());
				if(thisIdleTime > lastIdleTime){
					System.err.println("Quit Fidgeting");
				}
				lastIdleTime = thisIdleTime;
				Thread.sleep(100);
			}
			System.err.println("Thanks");
		} catch (InterruptedException e) {
		}
		
	}
	
	
	@Test
	public void testDoubleGet() {
		Idle i1 = Idle.getSensor();
		Idle i2 = Idle.getSensor();
		assertTrue(i1 == i2);
		assertTrue(Math.abs(i1.senseIdleTime() - i2.senseIdleTime()) < 100);
		
		/* Make sure ps2 continues to work */
		i2.senseIdleTime();
		long foo = i2.senseIdleTime();
		/* If this fails then you were idle for longer than 30 seconds when the test was run */
		assertTrue(foo < 30000);
	}

}
