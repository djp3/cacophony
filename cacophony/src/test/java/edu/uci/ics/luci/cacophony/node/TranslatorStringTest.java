package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TranslatorStringTest {

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
		TranslatorString tb1 = new TranslatorString();
		TranslatorString tb2 = new TranslatorString();
		
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
		String t3= "true";
		String f1 = "falSE";
		String f2 = "FALSE";
		String f3 = "false";
		String x1 = "apple";
		String x2 = "truer";
		String x3 = "antifalse";
		
		TranslatorString ts = new TranslatorString();
		try{
			ts.initialize(null);
		}
		catch(RuntimeException e){
			fail("This shouldn't fail");
		}
		
		assertEquals(t1, ts.translate(t1));
		assertEquals(t2, ts.translate(t2));
		assertEquals(t3, ts.translate(t3));
		assertEquals(f1, ts.translate(f1));
		assertEquals(f2, ts.translate(f2));
		assertEquals(f3, ts.translate(f3));
		assertEquals(x1, ts.translate(x1));
		assertEquals(x2, ts.translate(x2));
		assertEquals(x3, ts.translate(x3));
		assertEquals(null, ts.translate(null));
	}
}
