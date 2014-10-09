package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CategoricalTest {

	@Test
	public void testCategorical() {
		List<String> categories = Arrays.asList("red", "blue", "green");
		Categorical<String> categorical = new Categorical<String>("red", categories);
		assertEquals(categorical.getCategory(), "red");
		assertTrue(categorical.getPossibleCategories().contains("red"));
		assertTrue(categorical.getPossibleCategories().contains("blue"));
		assertTrue(categorical.getPossibleCategories().contains("green"));
		
		try {
			categorical = new Categorical<String>("orange", categories);
		}
		catch (IllegalArgumentException e) {
			return;
		}
		fail("Should not have reached here; an IllegalArgumentException should have been thrown.");
	}
}
