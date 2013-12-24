package edu.uci.ics.luci.cacophony;


import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.luci.utility.Globals;
import edu.uci.ics.luci.utility.GlobalsTest;

public class CacophonyGlobalsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		while(Globals.getGlobals() != null){
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
			}
		}
		Globals.setGlobals(new GlobalsTest());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Globals.getGlobals().setQuitting(true);
		Globals.setGlobals(null);
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
