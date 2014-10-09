package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TranslatorDateTest {

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
	public void testTranslation(){	
		TranslatorDate td = new TranslatorDate();
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(2013, Calendar.MAY, 03, 0, 0, 0);
		assertEquals(cal.getTime(), td.translate("2013-05-03"));
		assertEquals(null, td.translate("abcdefg"));
		assertEquals(null, td.translate(null));
	}
}
