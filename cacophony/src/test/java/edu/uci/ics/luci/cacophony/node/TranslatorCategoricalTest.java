package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.minidev.json.JSONObject;

import org.junit.Test;

public class TranslatorCategoricalTest {
	
	@Test
	public void testTranslation(){
		
		String t1 = "red";
		String t2 = "green";
		String t3 = "blue";
		
		TranslatorCategorical tc = new TranslatorCategorical();
		JSONObject mappings = new JSONObject();
		
		mappings.put(t1,t1);
		mappings.put(t2,t2);
		mappings.put(t3,t3);
		
		tc.initialize(mappings);
		
		assertTrue(tc.translate(null) == null);
		
		Categorical<String> c = tc.translate(t1);
		assertEquals(c.getCategory(), t1);
		assertTrue(c.getPossibleCategories().contains(t1));
		assertTrue(c.getPossibleCategories().contains(t2));
		assertTrue(c.getPossibleCategories().contains(t3));
		
		c = tc.translate(t2);
		assertEquals(c.getCategory(), t2);
		assertTrue(c.getPossibleCategories().contains(t1));
		assertTrue(c.getPossibleCategories().contains(t2));
		assertTrue(c.getPossibleCategories().contains(t3));
		
		c = tc.translate(t3);
		assertEquals(c.getCategory(), t3);
		assertTrue(c.getPossibleCategories().contains(t1));
		assertTrue(c.getPossibleCategories().contains(t2));
		assertTrue(c.getPossibleCategories().contains(t3));
	}
}
