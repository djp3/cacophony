package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Attribute;

public class TranslatorNumericTest {

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
		TranslatorNumeric tn = new TranslatorNumeric();
		
		assertEquals(Attribute.NUMERIC, tn.translate("3").getWekaAttributeType());
		assertTrue((Double)tn.translate("3").getValue() == 3);
		assertTrue((Double)tn.translate("03").getValue() == 3);
		assertTrue((Double)tn.translate("3.050").getValue() == 3.05);
		assertTrue((Double)tn.translate("1000000").getValue() == 1000000);
		assertTrue((Double)tn.translate("1,000,000").getValue() == 1000000);
		assertTrue((Double)tn.translate("10").getValue() == 10);
		assertTrue((Double)tn.translate("0").getValue() == 0);
		assertTrue((Double)tn.translate("0.0").getValue() == 0);
		assertTrue((Double)tn.translate("-3").getValue() == -3);
		assertTrue((Double)tn.translate("-03").getValue() == -3);
		assertTrue((Double)tn.translate("-04510.03010000").getValue() == -4510.0301);
		assertTrue(tn.translate("abc") == null);
		assertTrue(tn.translate("0.0.0") == null);
		assertTrue(tn.translate("-5a") == null);
		assertTrue(tn.translate(null) == null);
	}
}
