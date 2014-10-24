package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TranslatorDoubleTest {

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
		TranslatorDouble tb1 = new TranslatorDouble();
		TranslatorDouble tb2 = new TranslatorDouble();
		
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
		TranslatorDouble tn = new TranslatorDouble();
		try{
			tn.initialize(null);
		}
		catch(RuntimeException e){
			fail("This shouldn't fail");
		}
		
		assertTrue(tn.translate("3") == 3);
		assertTrue(tn.translate("03") == 3);
		assertTrue(tn.translate("3.050") == 3.05);
		assertTrue(tn.translate("1000000") == 1000000);
		assertTrue(tn.translate("1,000,000") == 1000000);
		assertTrue(tn.translate("10") == 10);
		assertTrue(tn.translate("0") == 0);
		assertTrue(tn.translate("0.0") == 0);
		assertTrue(tn.translate("-3") == -3);
		assertTrue(tn.translate("-03") == -3);
		assertTrue(tn.translate("-04510.03010000") == -4510.0301);
		assertTrue(tn.translate("abc") == null);
		assertTrue(tn.translate("0.0.0") == null);
		assertTrue(tn.translate("-5a") == null);
		assertTrue(tn.translate(null) == null);
	}
}
