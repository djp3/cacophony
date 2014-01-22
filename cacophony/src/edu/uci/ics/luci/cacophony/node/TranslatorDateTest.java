package edu.uci.ics.luci.cacophony.node;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Attribute;

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
		
		assertEquals(Attribute.DATE, td.translate("2013-05-03").getWekaAttributeType());
		assertEquals("2013-05-03", td.translate("2013-05-03").getValue());
		assertEquals("2013-05-03", td.translate("2013-05-03").getValue());
		assertEquals(null, td.translate("abcdefg"));
		assertEquals(null, td.translate(null));
	}
}
