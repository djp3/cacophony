package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TranslatorIdentityTest {

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
		
		
		
		TranslatorIdentity tb = new TranslatorIdentity();
		
		assertTrue(tb.translatable(t1));
		assertTrue(tb.translatable(t2));
		assertTrue(tb.translatable(t3));
		assertTrue(tb.translatable(f1));
		assertTrue(tb.translatable(f2));
		assertTrue(tb.translatable(f3));
		assertTrue(tb.translatable(x1));
		assertTrue(tb.translatable(x2));
		assertTrue(tb.translatable(x3));
		assertTrue(!tb.translatable(null));
		
		assertEquals(t1,tb.translation(t1));
		assertEquals(t2,tb.translation(t2));
		assertEquals(t3,tb.translation(t3));
		assertEquals(f1,tb.translation(f1));
		assertEquals(f2,tb.translation(f2));
		assertEquals(f3,tb.translation(f3));
		assertEquals(x1,tb.translation(x1));
		assertEquals(x2,tb.translation(x2));
		assertEquals(x3,tb.translation(x3));
		assertTrue(tb.translation(null) == null);
		
		
	}

}
