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
	public void testTranslation(){	
		TranslatorDouble tn = new TranslatorDouble();
		
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
