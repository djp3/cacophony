package edu.uci.ics.luci.cacophony;


import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CacophonyGlobalsTest {

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
	public void testAssumptions(){
		/*Make sure our assumptions about mapping days to ints is correct */
		assertEquals(Integer.valueOf(Calendar.SUNDAY),Integer.valueOf(1));
		assertEquals(Integer.valueOf(Calendar.MONDAY),Integer.valueOf(2));
		assertEquals(Integer.valueOf(Calendar.TUESDAY),Integer.valueOf(3));
		assertEquals(Integer.valueOf(Calendar.WEDNESDAY),Integer.valueOf(4));
		assertEquals(Integer.valueOf(Calendar.THURSDAY),Integer.valueOf(5));
		assertEquals(Integer.valueOf(Calendar.FRIDAY),Integer.valueOf(6));
		assertEquals(Integer.valueOf(Calendar.SATURDAY),Integer.valueOf(7));
	}

}
