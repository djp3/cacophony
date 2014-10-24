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
	
	
	@Test
	public void testTranslationComparisonDegenerate(){
		TranslatorCategorical tc1 = new TranslatorCategorical();
		TranslatorCategorical tc2 = new TranslatorCategorical();
		assertTrue(tc1.equals(tc2));
		assertEquals(tc1.hashCode(),tc2.hashCode());
		
		assertTrue(tc2.equals(tc1));
		assertEquals(tc2.hashCode(),tc1.hashCode());
		
		assertTrue(tc1.equals(tc1));
		assertEquals(tc1.hashCode(),tc1.hashCode());
	}
	
	@Test
	public void testTranslationComparisonDegenerater2(){
		String t1 = "red";
		String t2 = "green";
		String t3 = "blue";
		
		TranslatorCategorical tc1 = new TranslatorCategorical();
		JSONObject mappings = new JSONObject();
		
		mappings.put(t1,t1);
		mappings.put(t2,t2);
		mappings.put(t3,t3);
		
		tc1.initialize(mappings);
		
		
		TranslatorCategorical tc2 = new TranslatorCategorical();
		assertTrue(!tc1.equals(tc2));
		assertTrue(tc1.hashCode() != tc2.hashCode());
		
		assertTrue(!tc2.equals(tc1));
		assertTrue(tc2.hashCode() != tc1.hashCode());
	}
		
	@Test
	public void testTranslationComparison(){
		
		String t1 = "red";
		String t2 = "green";
		String t3 = "blue";
		
		TranslatorCategorical tc1 = new TranslatorCategorical();
		JSONObject mappings = new JSONObject();
		
		mappings.put(t1,t1);
		mappings.put(t2,t2);
		mappings.put(t3,t3);
		
		tc1.initialize(mappings);
		
		
		TranslatorCategorical tc2 = new TranslatorCategorical();
		tc2.initialize(mappings);
		
		assertTrue(tc1.equals(tc1));
		assertEquals(tc1.hashCode(), tc1.hashCode());
		
		assertTrue(tc1.equals(tc2));
		assertEquals(tc1.hashCode(), tc2.hashCode());
		
		assertTrue(tc2.equals(tc1));
		assertEquals(tc2.hashCode(), tc1.hashCode());
		
		assertTrue(!tc1.equals(null));
		assertTrue(!tc1.equals("test"));
		
	}
}
