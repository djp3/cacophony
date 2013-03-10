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

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class EnvironmentTest extends Environment{
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testVersioning() {
		assertEquals(getVersionMajor(), MAJOR_VERSION);
		assertEquals(getVersionMinor(), MINOR_VERSION);
		assertEquals(getVersionRevision(), REVISION);
		
		int x = new Random().nextInt(100);
		setVersionMajor(x);
		assertEquals(getVersionMajor(),x);
		
		int y = new Random().nextInt(100);
		setVersionMinor(y);
		assertEquals(getVersionMinor(),y);
		
		int z = new Random().nextInt(100);
		setVersionRevision(z);
		assertEquals(getVersionRevision(), z);
		
		assertTrue(getVersionString().equals(""+x+"."+y+"."+z));
	}
	
	@Test
	public void testOSType() {
		assertEquals(getOSType(),oSType);
		
		setOSType(OSTypes.LINUX);
		assertEquals(getOSType(),OSTypes.LINUX);
		
		assertTrue(getOSStringLong().equals(oSType.toString()+"/"+System.getProperty("os.arch")+"/"+System.getProperty("os.version")));
	}
	
}
