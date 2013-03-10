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


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SensorNetAttributeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testConstants()
	{
		/*Make sure all the constants are in sync */
		assertTrue(SensorNetAttribute.values().length == SensorNetAttribute.NUMBER_DERIVED+SensorNetAttribute.NUMBER_SENSED);
	}
	
	@Test
	public void testOrdering()
	{
		int count = 0;
		int countDerived = 0;
		for(SensorNetAttribute s = SensorNetAttribute.FIRST_ATTRIBUTE; s != null ; s= s.next()){
			count++;
			if(count==SensorNetAttribute.values().length){
				assertEquals(s,SensorNetAttribute.LAST_ATTRIBUTE);
			}
			assertTrue(s.toSQLTypeString() != null);
			assertTrue(s.toString() != null);
			assertTrue(!s.toString().equals("UNKNOWN"));
			
			if(s.derived()){
				countDerived++;
			}
		}
		assertEquals(SensorNetAttribute.values().length, count);
		assertEquals(SensorNetAttribute.NUMBER_SENSED+SensorNetAttribute.NUMBER_DERIVED, count);
		assertEquals(SensorNetAttribute.NUMBER_DERIVED, countDerived);
	}


}