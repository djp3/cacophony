package edu.uci.ics.luci.cacophony.sensors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.datastructure.ListComparable;

public class ProcessTest {

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
		Process sensor = Process.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}

	@Test
	public void testActiveProcess() {
		Process sensor = Process.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.senseActiveProcess().toLowerCase().contains("eclipse"));
		Process.getLog().debug("The current active process is: "+sensor.senseActiveProcess());
	}
	
	@Test
	public void testGetAllProcesses() {
		Process sensor = Process.getSensor();
		assertTrue(sensor.sensingAvailable());
		
		ListComparable<String> runningProcesses;
		try {
			runningProcesses = sensor.senseAllProcesses();
			boolean result = false;
			for(String e:runningProcesses){
				if((e != null) && e.toLowerCase().contains("eclipse")){
					result = true;
				}
			}
			assertTrue(result);
			
			StringBuffer b = new StringBuffer("");
			for(String p: runningProcesses){
				b.append(p);
				b.append("\n");
			}
			Process.getLog().debug("Current running processes are :\n"+b.toString());
		} catch (RuntimeException e) {
			fail("Can't get running processes"+e);
		}
	}

}
