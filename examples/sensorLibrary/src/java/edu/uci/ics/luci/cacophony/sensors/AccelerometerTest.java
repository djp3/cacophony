/*
	Copyright 2007-2013
		University of California, Irvine (c/o Donald J. Patterson)
*/
/*
	This file is part of Cacophony

    Cacophony is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cacophony is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cacophony.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.uci.ics.luci.cacophony.sensors;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;

public class AccelerometerTest {
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
			if(log == null){
					log = Logger.getLogger(AccelerometerTest.class);
			}
			return log;
	}

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
		Accelerometer sensor = Accelerometer.getSensor();
		assertTrue(sensor.sensingAvailable());
		assertTrue(sensor.sense() != null);
	}
	
	@Test
	public void testAccelerometer(){
		
		Accelerometer sa = Accelerometer.getSensor();

		List<Double> x = null;
		try{
			x = sa.senseAccelerometer();
			assertTrue(x != null);
		}
		catch(Exception e){
			fail("Unable to sense Acceleration X");
		}
		
		getLog().info("("+x.get(0)+","+x.get(1)+","+x.get(2)+")");
		
		List<Double> foo = sa.senseAccelerometer();
		assertTrue(foo.size() == 3);
	}
	
	@Test
	public void testDoubleGet() {
		Accelerometer a1 = Accelerometer.getSensor();
		Accelerometer a2 = Accelerometer.getSensor();
		assertTrue(a1 == a2);
		List<Double> _a1 = a1.senseAccelerometer();
		List<Double> _a2 = a2.senseAccelerometer();
		assertTrue((_a1 != null)&&(_a2 != null));
		assertEquals(a1.senseAccelerometer(),a2.senseAccelerometer());
		
		/* Make sure ps2 continues to work */
		a2.senseAccelerometer();
		List<Double> foo = a2.senseAccelerometer();
		assertTrue(foo!= null);
		assertTrue(foo.size() == 3);
	}
	

}
