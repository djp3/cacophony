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
	public void equalsTest(){
		TranslatorBoolean tb1 = new TranslatorBoolean();
		TranslatorBoolean tb2 = new TranslatorBoolean();
		
		assertTrue(!tb1.equals(null));
		assertTrue(!tb1.equals("foo"));
		
		assertEquals(tb1,tb1);
		assertEquals(tb1.hashCode(),tb1.hashCode());
		
		assertEquals(tb2,tb2);
		assertEquals(tb2.hashCode(),tb2.hashCode());
		
		assertEquals(tb1,tb2);
		assertEquals(tb2,tb1);
		assertEquals(tb1.hashCode(),tb2.hashCode());
		assertEquals(tb2.hashCode(),tb1.hashCode());
		
		
	}
	
	@Test
	public void testTranslation(){
		
		String t1 = "truE";
		String t2 = "TRUE";
		String t3 = "true";
		String t4 = " truE  ";
		String f1 = "falSE";
		String f2 = "FALSE";
		String f3 = "false";
		String f4 = " False ";
		String x1 = "apple";
		String x2 = "truer";
		String x3 = "antifalse";
		String x4 = "anti  false  ";
		
		
		TranslatorBoolean tb = new TranslatorBoolean();
		try{
			tb.initialize(null);
		}
		catch(RuntimeException e){
			fail("This shouldn't fail");
		}
		
		assertTrue(tb.translate(t1) == true);
		assertTrue(tb.translate(t2) == true);
		assertTrue(tb.translate(t3) == true);
		assertTrue(tb.translate(t4) == true);
		
		assertTrue(tb.translate(f1) == false);
		assertTrue(tb.translate(f2) == false);
		assertTrue(tb.translate(f3) == false);
		assertTrue(tb.translate(f4) == false);
		
		assertTrue(tb.translate(x1) == null);
		assertTrue(tb.translate(x2) == null);
		assertTrue(tb.translate(x3) == null);
		assertTrue(tb.translate(x4) == null);
		
		assertTrue(tb.translate(null) == null);
	}
}
