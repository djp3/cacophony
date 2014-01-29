package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Attribute;

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
		
		assertEquals(Attribute.NUMERIC, tb.translate(t1).getWekaAttributeType());
		assertTrue((Double)tb.translate(t1).getValue() == 1);
		assertTrue((Double)tb.translate(t2).getValue() == 1);
		assertTrue((Double)tb.translate(t3).getValue() == 1);
		assertTrue((Double)tb.translate(t4).getValue() == 1);
		
		assertTrue((Double)tb.translate(f1).getValue() == 0);
		assertTrue((Double)tb.translate(f2).getValue() == 0);
		assertTrue((Double)tb.translate(f3).getValue() == 0);
		assertTrue((Double)tb.translate(f4).getValue() == 0);
		
		assertTrue(tb.translate(x1) == null);
		assertTrue(tb.translate(x2) == null);
		assertTrue(tb.translate(x3) == null);
		assertTrue(tb.translate(x4) == null);
		
		assertTrue(tb.translate(null) == null);
	}
}
