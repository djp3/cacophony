package edu.uci.ics.luci.cacophony.node;

import static org.junit.Assert.*;
import org.junit.Test;

public class ModelConstantTest {
	@Test
	public void testTrain() {
		Model model = new ModelConstant();
		model.train(null);
	}

	@Test
	public void testPredict() {
		Model model = new ModelConstant();
		model.train(null);
		Object prediction = model.predict(null);
		if ((Integer)prediction != 12345) {
			fail("Incorrect prediction returned.");
		}
	}
}
