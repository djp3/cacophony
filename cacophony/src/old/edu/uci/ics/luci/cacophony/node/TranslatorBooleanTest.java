package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TranslatorBooleanTest {

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
		
		String t1 = "truE";
		String t2 = "TRUE";
		String t3= "true";
		String f1 = "falSE";
		String f2 = "FALSE";
		String f3 = "false";
		String x1 = "apple";
		String x2 = "truer";
		String x3 = "antifalse";
		
		
		
		TranslatorBoolean tb = new TranslatorBoolean();
		
		assertTrue(tb.translatable(t1));
		assertTrue(tb.translatable(t2));
		assertTrue(tb.translatable(t3));
		assertTrue(tb.translatable(f1));
		assertTrue(tb.translatable(f2));
		assertTrue(tb.translatable(f3));
		assertTrue(!tb.translatable(x1));
		assertTrue(!tb.translatable(x2));
		assertTrue(!tb.translatable(x3));
		assertTrue(!tb.translatable(null));
		
		assertTrue(tb.translation(t1));
		assertTrue(tb.translation(t2));
		assertTrue(tb.translation(t3));
		assertTrue(!tb.translation(f1));
		assertTrue(!tb.translation(f2));
		assertTrue(!tb.translation(f3));
		assertTrue(tb.translation(x1) == null);
		assertTrue(tb.translation(x2) == null);
		assertTrue(tb.translation(x3) == null);
		assertTrue(tb.translation(null) == null);
		
		
	}

}
