package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class CategoricalTest {

	@Test
	public void testCategoricalString() {
		Set<String> categories = new HashSet<String>(Arrays.asList("red", "blue", "green"));
		Categorical<String> categorical = new Categorical<String>("red", categories);
		assertEquals(categorical.getCategory(), "red");
		assertTrue(categorical.getPossibleCategories().contains("red"));
		assertTrue(categorical.getPossibleCategories().contains("blue"));
		assertTrue(categorical.getPossibleCategories().contains("green"));
		
		try {
			new Categorical<String>("orange", categories);
			fail("Should not have reached here; an IllegalArgumentException should have been thrown.");
		}
		catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void testCategoricalInteger() {
		Set<Integer> categories = new HashSet<Integer>(Arrays.asList(1,2,3));
		Categorical<Integer> categorical = new Categorical<Integer>(1, categories);
		assertEquals(categorical.getCategory(), new Integer(1));
		assertTrue(categorical.getPossibleCategories().contains(1));
		assertTrue(categorical.getPossibleCategories().contains(2));
		assertTrue(categorical.getPossibleCategories().contains(3));
		
		try {
			new Categorical<Integer>(4, categories);
			fail("Should not have reached here; an IllegalArgumentException should have been thrown.");
		}
		catch (IllegalArgumentException e) {
		}
	}
}
