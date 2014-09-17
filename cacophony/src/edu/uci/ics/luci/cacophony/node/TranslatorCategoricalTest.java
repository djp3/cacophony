package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import org.junit.Test;

public class TranslatorCategoricalTest {
	@Test
	public void testTranslation(){
		String t1 = "red";
		// TODO: once list of possible categories is created automatically, need to add support for it here.
		TranslatorCategorical tc = new TranslatorCategorical();
		Categorical<String> c = tc.translate("red");
		assertEquals(c.getCategory(), t1);
		assertEquals(c.getPossibleCategories().get(0), "red");
		assertEquals(c.getPossibleCategories().get(1), "green");
		assertEquals(c.getPossibleCategories().get(2), "blue");
	}
}
