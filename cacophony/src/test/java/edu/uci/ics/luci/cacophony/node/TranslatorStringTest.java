package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Attribute;

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
		
		assertEquals(Attribute.STRING, ts.translate(t1).getWekaAttributeType());
		assertEquals(t1, ts.translate(t1).getValue());
		assertEquals(t2, ts.translate(t2).getValue());
		assertEquals(t3, ts.translate(t3).getValue());
		assertEquals(f1, ts.translate(f1).getValue());
		assertEquals(f2, ts.translate(f2).getValue());
		assertEquals(f3, ts.translate(f3).getValue());
		assertEquals(x1, ts.translate(x1).getValue());
		assertEquals(x2, ts.translate(x2).getValue());
		assertEquals(x3, ts.translate(x3).getValue());
		assertEquals(null, ts.translate(null));
	}
}
